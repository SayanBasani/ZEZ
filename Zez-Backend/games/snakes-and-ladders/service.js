const { getBoardConfig } = require("./board-config");
const { rollDice } = require("./dice");
const { GameRuleError, applyDiceRoll, resignSession } = require("./engine");
const { snakesLaddersEvents, GAME_EVENTS } = require("./events");
const { isValidUuid } = require("./validation");
const {
  INVITATION_STATUS,
  SESSION_STATUS,
  SPECIAL_MOVE_TYPE,
  CONNECTION_STATUS,
} = require("./types");

class SnakesLaddersService {
  constructor(repository, events = snakesLaddersEvents) {
    this.repo = repository;
    this.events = events;
  }

  async createInvitation({ senderId, receiverId, conversationId = null }) {
    if (!isValidUuid(senderId) || !isValidUuid(receiverId)) {
      throw new GameRuleError("INVALID_USER_ID", "Valid senderId and receiverId UUIDs are required", 400);
    }
    if (senderId === receiverId) {
      throw new GameRuleError("SELF_INVITE_FORBIDDEN", "Cannot challenge yourself to Snakes & Ladders", 400);
    }

    const [sender, receiver] = await Promise.all([
      this.repo.findUserBasic(senderId),
      this.repo.findUserBasic(receiverId),
    ]);
    if (!sender || !receiver) {
      throw new GameRuleError("USER_NOT_FOUND", "Both players must be authenticated users", 404);
    }

    const row = await this.repo.createInvitation({ senderId, receiverId, conversationId });
    const invitation = {
      ...row,
      sender_name: sender.name,
      sender_avatar: sender.avatar_url,
      receiver_name: receiver.name,
      receiver_avatar: receiver.avatar_url,
    };

    this.events.emitInvitationEvent(invitation.id, GAME_EVENTS.INVITE_RECEIVED, { invitation });
    return invitation;
  }

  async getInvitation(invitationId, requesterId) {
    if (!isValidUuid(invitationId) || !isValidUuid(requesterId)) {
      throw new GameRuleError("INVALID_INPUT", "Valid invitationId and userId required", 400);
    }
    const inv = await this.repo.getInvitationWithUsers(invitationId);
    if (!inv) {
      throw new GameRuleError("INVITATION_NOT_FOUND", "Invitation not found", 404);
    }
    if (inv.sender_id !== requesterId && inv.receiver_id !== requesterId) {
      throw new GameRuleError("UNAUTHORIZED", "Not authorized to view this invitation", 403);
    }

    if (inv.status === INVITATION_STATUS.PENDING && new Date(inv.expires_at).getTime() <= Date.now()) {
      await this.repo.markInvitationExpired(inv.id);
      inv.status = INVITATION_STATUS.EXPIRED;
    }

    let session = null;
    if (inv.game_id) {
      session = await this.repo.getSessionSnapshot(inv.game_id);
    }

    return { invitation: inv, session };
  }

  async acceptInvitation(invitationId, accepterId) {
    if (!isValidUuid(invitationId) || !isValidUuid(accepterId)) {
      throw new GameRuleError("INVALID_INPUT", "Valid invitationId and userId are required", 400);
    }

    const res = await this.repo.acceptInvitationTransactional({ invitationId, accepterId });

    switch (res.outcome) {
      case "NOT_FOUND":
        throw new GameRuleError("INVITATION_NOT_FOUND", "Invitation not found", 404);
      case "SELF_ACCEPT_FORBIDDEN":
        throw new GameRuleError("SELF_ACCEPT_FORBIDDEN", "Sender cannot accept their own invitation", 403);
      case "UNAUTHORIZED":
        throw new GameRuleError("UNAUTHORIZED", "Only the invited player can accept", 403);
      case "INVALID_STATUS":
        throw new GameRuleError("INVALID_INVITATION_STATUS", `Invitation is already ${res.invitation.status}`, 409);
      case "EXPIRED":
        this.events.emitInvitationEvent(invitationId, GAME_EVENTS.INVITE_EXPIRED, { invitation: res.invitation });
        throw new GameRuleError("INVITATION_EXPIRED", "Invitation has expired", 410);
      case "ALREADY_ACCEPTED":
        return { invitation: res.invitation, session: res.session, idempotent: true };
      case "CREATED":
        this.events.emitInvitationEvent(invitationId, GAME_EVENTS.INVITE_ACCEPTED, {
          invitation: res.invitation,
          session: res.session,
        });
        this.events.emitSessionEvent(res.session.id, GAME_EVENTS.STARTED, { session: res.session });
        return { invitation: res.invitation, session: res.session, idempotent: false };
      default:
        throw new GameRuleError("INTERNAL_ERROR", "Unexpected invitation outcome", 500);
    }
  }

  async declineInvitation(invitationId, declinerId) {
    if (!isValidUuid(invitationId) || !isValidUuid(declinerId)) {
      throw new GameRuleError("INVALID_INPUT", "Valid invitationId and userId are required", 400);
    }

    const existing = await this.repo.getInvitationWithUsers(invitationId);
    if (!existing) {
      throw new GameRuleError("INVITATION_NOT_FOUND", "Invitation not found", 404);
    }
    if (existing.receiver_id !== declinerId && existing.sender_id !== declinerId) {
      throw new GameRuleError("UNAUTHORIZED", "Not authorized to decline this invitation", 403);
    }
    if (existing.status === INVITATION_STATUS.ACCEPTED) {
      throw new GameRuleError("ALREADY_ACCEPTED", "Invitation already accepted", 409);
    }
    if (existing.status === INVITATION_STATUS.DECLINED) {
      return existing;
    }

    const declined = await this.repo.declineInvitation(invitationId);
    const updated = { ...existing, ...(declined || { status: INVITATION_STATUS.DECLINED }) };
    this.events.emitInvitationEvent(invitationId, GAME_EVENTS.INVITE_DECLINED, { invitation: updated });
    return updated;
  }

  async getSession(gameId, requesterId) {
    if (!isValidUuid(gameId) || !isValidUuid(requesterId)) {
      throw new GameRuleError("INVALID_INPUT", "Valid gameId and userId required", 400);
    }
    const snapshot = await this.repo.getSessionSnapshot(gameId);
    if (!snapshot) {
      throw new GameRuleError("SESSION_NOT_FOUND", "Game session not found", 404);
    }
    const isParticipant = snapshot.players.some((p) => p.userId === requesterId);
    if (!isParticipant) {
      throw new GameRuleError("UNAUTHORIZED", "You are not a participant in this game", 403);
    }
    return snapshot;
  }

  async rollDiceAndMove({ gameId, userId, actionId }) {
    if (!isValidUuid(gameId) || !isValidUuid(userId)) {
      throw new GameRuleError("INVALID_INPUT", "Valid gameId and userId required", 400);
    }
    if (!actionId || typeof actionId !== "string" || !actionId.trim()) {
      throw new GameRuleError("MISSING_ACTION_ID", "Unique actionId is required", 400);
    }
    const cleanActionId = actionId.trim().slice(0, 128);

    const txRes = await this.repo.executeLockedSessionUpdate(gameId, async ({ client, snapshot }) => {
      // Idempotency check inside locked transaction
      const dupRes = await client.query(
        `SELECT * FROM public.sl_move_history WHERE game_id = $1 AND action_id = $2`,
        [gameId, cleanActionId]
      );
      if (dupRes.rows.length > 0) {
        return {
          duplicate: true,
          existingMove: dupRes.rows[0],
        };
      }

      const diceValue = rollDice();
      const transition = applyDiceRoll(snapshot, {
        userId,
        actionId: cleanActionId,
        diceValue,
      });

      const { nextState, moveRecord, turnChanged } = transition;

      // Update session
      await client.query(
        `UPDATE public.sl_sessions
         SET status = $2,
             current_turn_player_id = $3,
             current_turn_index = $4,
             winner_id = $5,
             state_version = $6,
             updated_at = NOW(),
             finished_at = CASE WHEN $7::boolean THEN NOW() ELSE finished_at END
         WHERE id = $1`,
        [
          gameId,
          nextState.status,
          nextState.currentTurnPlayerId,
          nextState.currentTurnIndex,
          nextState.winnerId,
          nextState.stateVersion,
          nextState.status === SESSION_STATUS.FINISHED,
        ]
      );

      // Update moving player state + online presence
      const movedPlayer = nextState.players.find((p) => p.userId === userId);
      await client.query(
        `UPDATE public.sl_player_states
         SET current_position = $3,
             turn_count = $4,
             consecutive_sixes = $5,
             connected_status = $6,
             last_seen_at = NOW()
         WHERE game_id = $1 AND user_id = $2`,
        [
          gameId,
          userId,
          movedPlayer.currentPosition,
          movedPlayer.turnCount,
          movedPlayer.consecutiveSixes,
          CONNECTION_STATUS.ONLINE,
        ]
      );

      // Record move history
      await client.query(
        `INSERT INTO public.sl_move_history (
           game_id, action_id, player_id, dice_value, previous_position,
           normal_destination, final_position, overshot,
           special_move_type, special_move_from, special_move_to, state_version
         ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)`,
        [
          gameId,
          moveRecord.actionId,
          moveRecord.playerId,
          moveRecord.diceValue,
          moveRecord.previousPosition,
          moveRecord.normalDestination,
          moveRecord.finalPosition,
          moveRecord.overshot,
          moveRecord.specialMoveType,
          moveRecord.specialMoveFrom,
          moveRecord.specialMoveTo,
          moveRecord.stateVersion,
        ]
      );

      return {
        duplicate: false,
        moveRecord,
        turnChanged,
        isFinished: nextState.status === SESSION_STATUS.FINISHED,
      };
    });

    if (!txRes.found) {
      throw new GameRuleError("SESSION_NOT_FOUND", "Game session not found", 404);
    }

    if (txRes.duplicate) {
      return {
        idempotentReplay: true,
        session: txRes.session,
        move: txRes.session.lastMove,
      };
    }

    const { session, moveRecord, turnChanged, isFinished } = txRes;
    this.events.emitSessionEvent(gameId, GAME_EVENTS.DICE_ROLLED, { session, move: moveRecord });
    this.events.emitSessionEvent(gameId, GAME_EVENTS.PLAYER_MOVED, { session, move: moveRecord });

    if (moveRecord.specialMoveType === SPECIAL_MOVE_TYPE.SNAKE) {
      this.events.emitSessionEvent(gameId, GAME_EVENTS.SNAKE_ACTIVATED, { session, move: moveRecord });
    } else if (moveRecord.specialMoveType === SPECIAL_MOVE_TYPE.LADDER) {
      this.events.emitSessionEvent(gameId, GAME_EVENTS.LADDER_ACTIVATED, { session, move: moveRecord });
    }
    if (turnChanged) {
      this.events.emitSessionEvent(gameId, GAME_EVENTS.TURN_CHANGED, { session, move: moveRecord });
    }
    if (isFinished) {
      await this._publishWinSummaryIfNeeded(session, "reached Square 100");
      this.events.emitSessionEvent(gameId, GAME_EVENTS.FINISHED, { session, move: moveRecord });
    } else {
      this.events.emitSessionEvent(gameId, GAME_EVENTS.STATE_UPDATED, { session, move: moveRecord });
    }

    return {
      idempotentReplay: false,
      session,
      move: moveRecord,
    };
  }

  async resignGame(gameId, userId) {
    if (!isValidUuid(gameId) || !isValidUuid(userId)) {
      throw new GameRuleError("INVALID_INPUT", "Valid gameId and userId required", 400);
    }

    const txRes = await this.repo.executeLockedSessionUpdate(gameId, async ({ client, snapshot }) => {
      const nextState = resignSession(snapshot, userId);
      await client.query(
        `UPDATE public.sl_sessions
         SET status = $2,
             winner_id = $3,
             resigned_by = $4,
             state_version = $5,
             updated_at = NOW(),
             finished_at = NOW()
         WHERE id = $1`,
        [gameId, nextState.status, nextState.winnerId, userId, nextState.stateVersion]
      );
      return { resigned: true };
    });

    if (!txRes.found) {
      throw new GameRuleError("SESSION_NOT_FOUND", "Game session not found", 404);
    }

    await this._publishWinSummaryIfNeeded(txRes.session, "won by resignation");
    this.events.emitSessionEvent(gameId, GAME_EVENTS.FINISHED, { session: txRes.session });
    return txRes.session;
  }

  async reconnectPlayer(gameId, userId) {
    if (!isValidUuid(gameId) || !isValidUuid(userId)) {
      throw new GameRuleError("INVALID_INPUT", "Valid gameId and userId required", 400);
    }
    const existing = await this.getSession(gameId, userId);
    const prevPlayer = existing.players.find((p) => p.userId === userId);
    const wasOffline = prevPlayer && prevPlayer.connectedStatus === CONNECTION_STATUS.OFFLINE;
    const updated = await this.repo.updateConnectionStatus(gameId, userId, CONNECTION_STATUS.ONLINE);
    if (wasOffline && updated) {
      this.events.emitSessionEvent(gameId, GAME_EVENTS.PLAYER_RECONNECTED, {
        userId,
        session: updated,
      });
    }
    return updated || existing;
  }

  async disconnectPlayer(gameId, userId) {
    if (!isValidUuid(gameId) || !isValidUuid(userId)) {
      throw new GameRuleError("INVALID_INPUT", "Valid gameId and userId required", 400);
    }
    const updated = await this.repo.updateConnectionStatus(gameId, userId, CONNECTION_STATUS.OFFLINE);
    if (updated) {
      this.events.emitSessionEvent(gameId, GAME_EVENTS.PLAYER_DISCONNECTED, {
        userId,
        session: updated,
      });
    }
    return updated;
  }

  async getHistory(userId) {
    if (!isValidUuid(userId)) {
      throw new GameRuleError("INVALID_USER_ID", "Valid userId required", 400);
    }
    return this.repo.getPlayerHistory(userId);
  }

  getPublicBoardConfig(version = "sl-board-v1") {
    return getBoardConfig(version);
  }

  async _publishWinSummaryIfNeeded(session, reasonText) {
    if (!session || !session.winnerId) return;
    const winner = session.players.find((p) => p.userId === session.winnerId);
    const loser = session.players.find((p) => p.userId !== session.winnerId);
    if (!winner || !loser) return;

    const summary = `Snakes & Ladders: ${winner.name} (${reasonText}, Pos ${winner.currentPosition}) defeated ${loser.name} (Pos ${loser.currentPosition})`;
    await this.repo
      .postChatSummaryOnce({
        gameId: session.id,
        senderId: winner.userId,
        receiverId: loser.userId,
        conversationId: session.conversationId,
        summaryText: summary,
      })
      .catch(() => null);
  }
}

module.exports = {
  SnakesLaddersService,
};
