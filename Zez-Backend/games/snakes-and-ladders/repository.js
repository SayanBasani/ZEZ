const crypto = require("node:crypto");
const { getBoardConfig } = require("./board-config");
const {
  GAME_TYPE,
  INVITATION_STATUS,
  SESSION_STATUS,
  SPECIAL_MOVE_TYPE,
  CONNECTION_STATUS,
  DEFAULT_PLAYER_COLORS,
} = require("./types");

let tablesInitialized = false;

async function ensureTables(pool) {
  if (tablesInitialized) return;
  await pool.query(`
    CREATE TABLE IF NOT EXISTS public.sl_invitations (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      sender_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      receiver_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      conversation_id UUID REFERENCES public.chat_conversations(id) ON DELETE SET NULL,
      game_type VARCHAR(64) NOT NULL DEFAULT '${GAME_TYPE}',
      status VARCHAR(24) NOT NULL DEFAULT '${INVITATION_STATUS.PENDING}',
      game_id UUID,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      expires_at TIMESTAMPTZ NOT NULL,
      accepted_at TIMESTAMPTZ
    );
    CREATE INDEX IF NOT EXISTS idx_sl_inv_receiver ON public.sl_invitations(receiver_id, status, created_at DESC);
    CREATE INDEX IF NOT EXISTS idx_sl_inv_sender ON public.sl_invitations(sender_id, created_at DESC);

    CREATE TABLE IF NOT EXISTS public.sl_sessions (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      conversation_id UUID REFERENCES public.chat_conversations(id) ON DELETE SET NULL,
      invitation_id UUID REFERENCES public.sl_invitations(id) ON DELETE SET NULL,
      game_type VARCHAR(64) NOT NULL DEFAULT '${GAME_TYPE}',
      status VARCHAR(24) NOT NULL DEFAULT '${SESSION_STATUS.ACTIVE}',
      player1_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      player2_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      current_turn_player_id UUID NOT NULL,
      current_turn_index INTEGER NOT NULL DEFAULT 0,
      winner_id UUID,
      resigned_by UUID,
      board_configuration_version VARCHAR(32) NOT NULL DEFAULT 'sl-board-v1',
      state_version INTEGER NOT NULL DEFAULT 1,
      result_posted BOOLEAN NOT NULL DEFAULT FALSE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      finished_at TIMESTAMPTZ
    );
    CREATE INDEX IF NOT EXISTS idx_sl_sessions_players ON public.sl_sessions(player1_id, player2_id, updated_at DESC);

    CREATE TABLE IF NOT EXISTS public.sl_player_states (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      game_id UUID NOT NULL REFERENCES public.sl_sessions(id) ON DELETE CASCADE,
      user_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      seat_index INTEGER NOT NULL DEFAULT 0,
      token_color VARCHAR(32) NOT NULL,
      current_position INTEGER NOT NULL DEFAULT 0,
      turn_count INTEGER NOT NULL DEFAULT 0,
      consecutive_sixes INTEGER NOT NULL DEFAULT 0,
      connected_status VARCHAR(24) NOT NULL DEFAULT '${CONNECTION_STATUS.ONLINE}',
      last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      UNIQUE (game_id, user_id)
    );
    CREATE INDEX IF NOT EXISTS idx_sl_player_states_game ON public.sl_player_states(game_id, seat_index);

    CREATE TABLE IF NOT EXISTS public.sl_move_history (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      game_id UUID NOT NULL REFERENCES public.sl_sessions(id) ON DELETE CASCADE,
      action_id VARCHAR(128) NOT NULL,
      player_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      dice_value INTEGER NOT NULL,
      previous_position INTEGER NOT NULL,
      normal_destination INTEGER NOT NULL,
      final_position INTEGER NOT NULL,
      overshot BOOLEAN NOT NULL DEFAULT FALSE,
      special_move_type VARCHAR(24) NOT NULL DEFAULT '${SPECIAL_MOVE_TYPE.NONE}',
      special_move_from INTEGER,
      special_move_to INTEGER,
      state_version INTEGER NOT NULL,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      UNIQUE (game_id, action_id)
    );
    CREATE INDEX IF NOT EXISTS idx_sl_moves_game_ver ON public.sl_move_history(game_id, state_version DESC);
  `);
  tablesInitialized = true;
}

function resetInitFlagForTests() {
  tablesInitialized = false;
}

class SnakesLaddersRepository {
  constructor(pool) {
    this.pool = pool;
  }

  async init() {
    await ensureTables(this.pool);
  }

  async findUserBasic(userId, client = this.pool) {
    const res = await client.query(
      "SELECT id, name, avatar_url FROM public.chat_users WHERE id = $1",
      [userId]
    );
    return res.rows[0] || null;
  }

  async createInvitation({ id, senderId, receiverId, conversationId = null, ttlMs = 10 * 60 * 1000 }) {
    await this.init();
    const invId = id || crypto.randomUUID();
    const expiresAt = new Date(Date.now() + ttlMs);
    const res = await this.pool.query(
      `INSERT INTO public.sl_invitations (id, sender_id, receiver_id, conversation_id, game_type, status, expires_at)
       VALUES ($1, $2, $3, $4, $5, $6, $7)
       RETURNING *`,
      [invId, senderId, receiverId, conversationId, GAME_TYPE, INVITATION_STATUS.PENDING, expiresAt]
    );
    return res.rows[0];
  }

  async getInvitationWithUsers(invitationId, client = this.pool) {
    await this.init();
    const res = await client.query(
      `SELECT inv.*,
              su.name AS sender_name, su.avatar_url AS sender_avatar,
              ru.name AS receiver_name, ru.avatar_url AS receiver_avatar
       FROM public.sl_invitations inv
       JOIN public.chat_users su ON su.id = inv.sender_id
       JOIN public.chat_users ru ON ru.id = inv.receiver_id
       WHERE inv.id = $1`,
      [invitationId]
    );
    return res.rows[0] || null;
  }

  async markInvitationExpired(invitationId, client = this.pool) {
    const res = await client.query(
      `UPDATE public.sl_invitations
       SET status = $2
       WHERE id = $1 AND status = $3
       RETURNING *`,
      [invitationId, INVITATION_STATUS.EXPIRED, INVITATION_STATUS.PENDING]
    );
    return res.rows[0] || null;
  }

  async declineInvitation(invitationId) {
    await this.init();
    const res = await this.pool.query(
      `UPDATE public.sl_invitations
       SET status = $2
       WHERE id = $1 AND status = $3
       RETURNING *`,
      [invitationId, INVITATION_STATUS.DECLINED, INVITATION_STATUS.PENDING]
    );
    return res.rows[0] || null;
  }

  async acceptInvitationTransactional({ invitationId, accepterId, starterIndex, boardConfigVersion = "sl-board-v1" }) {
    await this.init();
    const client = await this.pool.connect();
    try {
      await client.query("BEGIN");
      const lockRes = await client.query(
        `SELECT inv.*,
                su.name AS sender_name, su.avatar_url AS sender_avatar,
                ru.name AS receiver_name, ru.avatar_url AS receiver_avatar
         FROM public.sl_invitations inv
         JOIN public.chat_users su ON su.id = inv.sender_id
         JOIN public.chat_users ru ON ru.id = inv.receiver_id
         WHERE inv.id = $1
         FOR UPDATE OF inv`,
        [invitationId]
      );
      if (lockRes.rows.length === 0) {
        await client.query("ROLLBACK");
        return { outcome: "NOT_FOUND" };
      }

      const inv = lockRes.rows[0];
      if (inv.sender_id === accepterId) {
        await client.query("ROLLBACK");
        return { outcome: "SELF_ACCEPT_FORBIDDEN", invitation: inv };
      }
      if (inv.receiver_id !== accepterId) {
        await client.query("ROLLBACK");
        return { outcome: "UNAUTHORIZED", invitation: inv };
      }
      if (inv.status === INVITATION_STATUS.ACCEPTED && inv.game_id) {
        await client.query("COMMIT");
        const existingSession = await this.getSessionSnapshot(inv.game_id);
        return { outcome: "ALREADY_ACCEPTED", invitation: inv, session: existingSession };
      }
      if (inv.status === INVITATION_STATUS.DECLINED || inv.status === INVITATION_STATUS.CANCELLED) {
        await client.query("ROLLBACK");
        return { outcome: "INVALID_STATUS", invitation: inv };
      }
      if (inv.status === INVITATION_STATUS.EXPIRED || new Date(inv.expires_at).getTime() <= Date.now()) {
        await client.query(
          "UPDATE public.sl_invitations SET status = $2 WHERE id = $1",
          [invitationId, INVITATION_STATUS.EXPIRED]
        );
        await client.query("COMMIT");
        inv.status = INVITATION_STATUS.EXPIRED;
        return { outcome: "EXPIRED", invitation: inv };
      }

      const gameId = crypto.randomUUID();
      const playersMeta = [
        { userId: inv.sender_id, name: inv.sender_name, avatarUrl: inv.sender_avatar, color: DEFAULT_PLAYER_COLORS[0] },
        { userId: inv.receiver_id, name: inv.receiver_name, avatarUrl: inv.receiver_avatar, color: DEFAULT_PLAYER_COLORS[1] },
      ];
      const chosenStarterIndex = Number.isInteger(starterIndex) ? starterIndex : crypto.randomInt(0, playersMeta.length);
      const firstTurnUserId = playersMeta[chosenStarterIndex].userId;

      await client.query(
        `INSERT INTO public.sl_sessions (
           id, conversation_id, invitation_id, game_type, status,
           player1_id, player2_id, current_turn_player_id, current_turn_index,
           board_configuration_version, state_version
         ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, 1)`,
        [
          gameId,
          inv.conversation_id,
          invitationId,
          GAME_TYPE,
          SESSION_STATUS.ACTIVE,
          playersMeta[0].userId,
          playersMeta[1].userId,
          firstTurnUserId,
          chosenStarterIndex,
          boardConfigVersion,
        ]
      );

      for (let i = 0; i < playersMeta.length; i++) {
        const p = playersMeta[i];
        await client.query(
          `INSERT INTO public.sl_player_states (
             game_id, user_id, seat_index, token_color, current_position, turn_count, consecutive_sixes, connected_status
           ) VALUES ($1, $2, $3, $4, 0, 0, 0, $5)`,
          [gameId, p.userId, i, p.color, CONNECTION_STATUS.ONLINE]
        );
      }

      const updInv = await client.query(
        `UPDATE public.sl_invitations
         SET status = $2, accepted_at = NOW(), game_id = $3
         WHERE id = $1
         RETURNING *`,
        [invitationId, INVITATION_STATUS.ACCEPTED, gameId]
      );

      await client.query("COMMIT");
      const session = await this.getSessionSnapshot(gameId);
      return {
        outcome: "CREATED",
        invitation: { ...inv, ...updInv.rows[0] },
        session,
      };
    } catch (err) {
      await client.query("ROLLBACK").catch(() => {});
      throw err;
    } finally {
      client.release();
    }
  }

  async getSessionSnapshot(gameId, client = this.pool) {
    await this.init();
    const sesRes = await client.query("SELECT * FROM public.sl_sessions WHERE id = $1", [gameId]);
    if (sesRes.rows.length === 0) return null;
    const ses = sesRes.rows[0];

    const psRes = await client.query(
      `SELECT ps.*, u.name, u.avatar_url
       FROM public.sl_player_states ps
       JOIN public.chat_users u ON u.id = ps.user_id
       WHERE ps.game_id = $1
       ORDER BY ps.seat_index ASC`,
      [gameId]
    );

    const mvRes = await client.query(
      `SELECT mv.*, u.name AS player_name
       FROM public.sl_move_history mv
       LEFT JOIN public.chat_users u ON u.id = mv.player_id
       WHERE mv.game_id = $1
       ORDER BY mv.state_version DESC
       LIMIT 10`,
      [gameId]
    );

    return this._formatSnapshot(ses, psRes.rows, mvRes.rows);
  }

  async executeLockedSessionUpdate(gameId, mutatorFn) {
    await this.init();
    const client = await this.pool.connect();
    try {
      await client.query("BEGIN");
      const sesRes = await client.query(
        "SELECT * FROM public.sl_sessions WHERE id = $1 FOR UPDATE",
        [gameId]
      );
      if (sesRes.rows.length === 0) {
        await client.query("ROLLBACK");
        return { found: false };
      }
      const ses = sesRes.rows[0];

      const psRes = await client.query(
        `SELECT ps.*, u.name, u.avatar_url
         FROM public.sl_player_states ps
         JOIN public.chat_users u ON u.id = ps.user_id
         WHERE ps.game_id = $1
         ORDER BY ps.seat_index ASC
         FOR UPDATE OF ps`,
        [gameId]
      );

      const snapshot = this._formatSnapshot(ses, psRes.rows, []);
      const result = await mutatorFn({ client, sessionRow: ses, playerRows: psRes.rows, snapshot });

      if (result.abort) {
        await client.query("ROLLBACK");
        return { found: true, ...result };
      }

      await client.query("COMMIT");
      const updated = await this.getSessionSnapshot(gameId);
      return { found: true, ...result, session: updated };
    } catch (err) {
      await client.query("ROLLBACK").catch(() => {});
      throw err;
    } finally {
      client.release();
    }
  }

  async updateConnectionStatus(gameId, userId, status) {
    await this.init();
    const upd = await this.pool.query(
      `UPDATE public.sl_player_states
       SET connected_status = $3, last_seen_at = NOW()
       WHERE game_id = $1 AND user_id = $2
       RETURNING *`,
      [gameId, userId, status]
    );
    if (upd.rowCount === 0) return null;
    await this.pool.query(
      "UPDATE public.sl_sessions SET updated_at = NOW() WHERE id = $1",
      [gameId]
    );
    return this.getSessionSnapshot(gameId);
  }

  async postChatSummaryOnce({ gameId, senderId, receiverId, conversationId, summaryText }) {
    if (!senderId || !receiverId || !summaryText) return null;
    const lockRes = await this.pool.query(
      `UPDATE public.sl_sessions
       SET result_posted = TRUE
       WHERE id = $1 AND result_posted = FALSE
       RETURNING id, conversation_id`,
      [gameId]
    );
    if (lockRes.rowCount === 0) return null;

    let convId = conversationId || lockRes.rows[0].conversation_id;
    if (!convId) {
      const [u1, u2] = senderId < receiverId ? [senderId, receiverId] : [receiverId, senderId];
      const convRes = await this.pool.query(
        `SELECT id FROM public.chat_conversations WHERE user1_id = $1 AND user2_id = $2 LIMIT 1`,
        [u1, u2]
      );
      convId = convRes.rows[0] ? convRes.rows[0].id : null;
    }
    if (!convId) return null;

    const msgText = `[game_end:${GAME_TYPE}|${gameId}|${summaryText}]`;
    const msgRes = await this.pool.query(
      `INSERT INTO public.chat_messages (
         conversation_id, sender_id, receiver_id, message_text, mode, status
       ) VALUES ($1, $2, $3, $4, 'normal', 'sent')
       RETURNING *`,
      [convId, senderId, receiverId, msgText]
    );
    await this.pool.query(
      `UPDATE public.chat_conversations
       SET last_message_text = $1, last_message_time = $2, deleted_for_users = '[]'::jsonb
       WHERE id = $3`,
      [msgText, msgRes.rows[0].created_at, convId]
    );
    return msgRes.rows[0];
  }

  async getPlayerHistory(userId, limit = 30) {
    await this.init();
    const res = await this.pool.query(
      `SELECT s.*,
              p1.current_position AS p1_pos, p1.token_color AS p1_color,
              p2.current_position AS p2_pos, p2.token_color AS p2_color,
              u1.name AS p1_name, u1.avatar_url AS p1_avatar,
              u2.name AS p2_name, u2.avatar_url AS p2_avatar
       FROM public.sl_sessions s
       JOIN public.sl_player_states p1 ON p1.game_id = s.id AND p1.user_id = s.player1_id
       JOIN public.sl_player_states p2 ON p2.game_id = s.id AND p2.user_id = s.player2_id
       JOIN public.chat_users u1 ON u1.id = s.player1_id
       JOIN public.chat_users u2 ON u2.id = s.player2_id
       WHERE (s.player1_id = $1 OR s.player2_id = $1)
         AND s.status IN ('FINISHED', 'ABANDONED')
       ORDER BY COALESCE(s.finished_at, s.updated_at) DESC
       LIMIT $2`,
      [userId, limit]
    );

    let totalGames = 0;
    let totalWins = 0;
    let totalPlaySeconds = 0;

    const matches = res.rows.map((r) => {
      totalGames += 1;
      const isP1 = r.player1_id === userId;
      const didWin = r.winner_id === userId;
      if (didWin) totalWins += 1;
      const startMs = new Date(r.created_at).getTime();
      const endMs = new Date(r.finished_at || r.updated_at).getTime();
      const durationSeconds = Math.max(0, Math.round((endMs - startMs) / 1000));
      totalPlaySeconds += durationSeconds;

      const opponentId = isP1 ? r.player2_id : r.player1_id;
      const opponentName = isP1 ? r.p2_name : r.p1_name;
      const opponentAvatar = isP1 ? r.p2_avatar : r.p1_avatar;
      const myFinalPosition = Number(isP1 ? r.p1_pos : r.p2_pos);
      const opponentFinalPosition = Number(isP1 ? r.p2_pos : r.p1_pos);
      const winnerName = r.winner_id === r.player1_id ? r.p1_name : (r.winner_id === r.player2_id ? r.p2_name : null);

      return {
        gameId: r.id,
        gameType: r.game_type,
        opponentId,
        opponentName,
        opponentAvatar,
        winnerId: r.winner_id,
        winnerName,
        didWin,
        resignedBy: r.resigned_by || null,
        myFinalPosition,
        opponentFinalPosition,
        durationSeconds,
        createdAt: r.created_at,
        finishedAt: r.finished_at || r.updated_at,
      };
    });

    const statsRes = await this.pool.query(
      `SELECT COUNT(*)::int AS total_finished,
              COUNT(*) FILTER (WHERE winner_id = $1)::int AS total_won,
              COALESCE(SUM(EXTRACT(EPOCH FROM (COALESCE(finished_at, updated_at) - created_at))), 0)::bigint AS total_secs
       FROM public.sl_sessions
       WHERE (player1_id = $1 OR player2_id = $1)
         AND status IN ('FINISHED', 'ABANDONED')`,
      [userId]
    );
    const agg = statsRes.rows[0] || { total_finished: totalGames, total_won: totalWins, total_secs: totalPlaySeconds };
    const countAll = Number(agg.total_finished || 0);
    const countWins = Number(agg.total_won || 0);

    return {
      stats: {
        totalGames: countAll,
        totalWins: countWins,
        winRatePercent: countAll > 0 ? Math.round((countWins * 100) / countAll) : 0,
        totalPlaySeconds: Math.max(0, Number(agg.total_secs || 0)),
      },
      matches,
    };
  }

  _formatSnapshot(ses, playerRows, moveRows) {
    const boardConfig = getBoardConfig(ses.board_configuration_version);
    const players = (playerRows || []).map((pr) => ({
      userId: pr.user_id,
      name: pr.name || "Player",
      avatarUrl: pr.avatar_url || null,
      seatIndex: Number(pr.seat_index || 0),
      tokenColor: pr.token_color,
      currentPosition: Number(pr.current_position || 0),
      turnCount: Number(pr.turn_count || 0),
      consecutiveSixes: Number(pr.consecutive_sixes || 0),
      connectedStatus: pr.connected_status || CONNECTION_STATUS.ONLINE,
      lastSeenAt: pr.last_seen_at,
    }));

    const recentMoves = (moveRows || []).map((m) => ({
      actionId: m.action_id,
      playerId: m.player_id,
      playerName: m.player_name || "Player",
      diceValue: Number(m.dice_value),
      previousPosition: Number(m.previous_position),
      normalDestination: Number(m.normal_destination),
      finalPosition: Number(m.final_position),
      overshot: !!m.overshot,
      specialMoveType: m.special_move_type || SPECIAL_MOVE_TYPE.NONE,
      specialMoveFrom: m.special_move_from != null ? Number(m.special_move_from) : null,
      specialMoveTo: m.special_move_to != null ? Number(m.special_move_to) : null,
      stateVersion: Number(m.state_version),
      createdAt: m.created_at,
    }));

    return {
      id: ses.id,
      conversationId: ses.conversation_id || null,
      invitationId: ses.invitation_id || null,
      gameType: ses.game_type || GAME_TYPE,
      status: ses.status,
      boardConfigurationVersion: ses.board_configuration_version,
      boardConfig,
      players,
      currentTurnIndex: Number(ses.current_turn_index || 0),
      currentTurnPlayerId: ses.current_turn_player_id,
      winnerId: ses.winner_id || null,
      resignedBy: ses.resigned_by || null,
      stateVersion: Number(ses.state_version || 1),
      lastMove: recentMoves.length > 0 ? recentMoves[0] : null,
      recentMoves,
      createdAt: ses.created_at,
      updatedAt: ses.updated_at,
      finishedAt: ses.finished_at || null,
    };
  }
}

module.exports = {
  SnakesLaddersRepository,
  ensureTables,
  resetInitFlagForTests,
};
