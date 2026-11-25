const assert = require("node:assert/strict");
const crypto = require("node:crypto");
const { SnakesLaddersService } = require("../service");
const { SnakesLaddersEvents } = require("../events");
const { GameRuleError } = require("../engine");

const USER_A = "11111111-1111-4111-8111-111111111111";
const USER_B = "22222222-2222-4222-8222-222222222222";
const USER_C = "33333333-3333-4333-8333-333333333333";

function createInMemoryRepository() {
  const users = new Map([
    [USER_A, { id: USER_A, name: "Alice", avatar_url: null }],
    [USER_B, { id: USER_B, name: "Bob", avatar_url: null }],
    [USER_C, { id: USER_C, name: "Mallory", avatar_url: null }],
  ]);
  const invitations = new Map();
  const sessions = new Map();
  const movesBySession = new Map();
  const chatSummaries = [];

  return {
    chatSummaries,
    async init() {},
    async findUserBasic(userId) { return users.get(userId) || null; },
    async createInvitation({ senderId, receiverId, conversationId, ttlMs = 600000 }) {
      const id = crypto.randomUUID();
      const row = {
        id,
        sender_id: senderId,
        receiver_id: receiverId,
        conversation_id: conversationId,
        game_type: "snakes_and_ladders",
        status: "PENDING",
        game_id: null,
        created_at: new Date(),
        expires_at: new Date(Date.now() + ttlMs),
        accepted_at: null,
      };
      invitations.set(id, row);
      return row;
    },
    async getInvitationWithUsers(id) {
      const row = invitations.get(id);
      if (!row) return null;
      return {
        ...row,
        sender_name: users.get(row.sender_id)?.name,
        receiver_name: users.get(row.receiver_id)?.name,
      };
    },
    async markInvitationExpired(id) {
      const r = invitations.get(id);
      if (r && r.status === "PENDING") r.status = "EXPIRED";
      return r;
    },
    async declineInvitation(id) {
      const r = invitations.get(id);
      if (r && r.status === "PENDING") r.status = "DECLINED";
      return r;
    },
    async acceptInvitationTransactional({ invitationId, accepterId }) {
      const inv = await this.getInvitationWithUsers(invitationId);
      if (!inv) return { outcome: "NOT_FOUND" };
      if (inv.sender_id === accepterId) return { outcome: "SELF_ACCEPT_FORBIDDEN", invitation: inv };
      if (inv.receiver_id !== accepterId) return { outcome: "UNAUTHORIZED", invitation: inv };
      if (inv.status === "ACCEPTED" && inv.game_id) {
        return { outcome: "ALREADY_ACCEPTED", invitation: inv, session: sessions.get(inv.game_id) };
      }
      if (inv.status === "DECLINED") return { outcome: "INVALID_STATUS", invitation: inv };
      if (inv.status === "EXPIRED" || new Date(inv.expires_at).getTime() <= Date.now()) {
        invitations.get(invitationId).status = "EXPIRED";
        inv.status = "EXPIRED";
        return { outcome: "EXPIRED", invitation: inv };
      }
      const gameId = crypto.randomUUID();
      const session = {
        id: gameId,
        conversationId: inv.conversation_id,
        invitationId,
        gameType: "snakes_and_ladders",
        status: "ACTIVE",
        boardConfigurationVersion: "sl-board-v1",
        boardConfig: require("../board-config").getBoardConfig("sl-board-v1"),
        players: [
          { userId: inv.sender_id, name: inv.sender_name, seatIndex: 0, tokenColor: "red", currentPosition: 0, turnCount: 0, consecutiveSixes: 0, connectedStatus: "ONLINE" },
          { userId: inv.receiver_id, name: inv.receiver_name, seatIndex: 1, tokenColor: "blue", currentPosition: 0, turnCount: 0, consecutiveSixes: 0, connectedStatus: "ONLINE" },
        ],
        currentTurnIndex: 0,
        currentTurnPlayerId: inv.sender_id,
        winnerId: null,
        resignedBy: null,
        resultPosted: false,
        stateVersion: 1,
        lastMove: null,
        recentMoves: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      sessions.set(gameId, session);
      movesBySession.set(gameId, []);
      const rawInv = invitations.get(invitationId);
      rawInv.status = "ACCEPTED";
      rawInv.game_id = gameId;
      rawInv.accepted_at = new Date();
      return { outcome: "CREATED", invitation: await this.getInvitationWithUsers(invitationId), session };
    },
    async getSessionSnapshot(gameId) {
      return sessions.get(gameId) ? JSON.parse(JSON.stringify(sessions.get(gameId))) : null;
    },
    async updateConnectionStatus(gameId, userId, status) {
      const ses = sessions.get(gameId);
      if (!ses) return null;
      const p = ses.players.find((x) => x.userId === userId);
      if (!p) return null;
      p.connectedStatus = status;
      return this.getSessionSnapshot(gameId);
    },
    async postChatSummaryOnce({ gameId, summaryText }) {
      const ses = sessions.get(gameId);
      if (!ses || ses.resultPosted) return null;
      ses.resultPosted = true;
      chatSummaries.push({ gameId, summaryText });
      return { id: "msg1" };
    },
    async executeLockedSessionUpdate(gameId, mutatorFn) {
      const ses = sessions.get(gameId);
      if (!ses) return { found: false };
      const moves = movesBySession.get(gameId);
      const client = {
        async query(sql, params) {
          if (sql.includes("FROM public.sl_move_history")) {
            const dup = moves.find((m) => m.actionId === params[1]);
            return { rows: dup ? [dup] : [] };
          }
          if (sql.includes("UPDATE public.sl_sessions") && sql.includes("current_turn_player_id")) {
            ses.status = params[1];
            ses.currentTurnPlayerId = params[2];
            ses.currentTurnIndex = params[3];
            ses.winnerId = params[4];
            ses.stateVersion = params[5];
            return { rowCount: 1 };
          }
          if (sql.includes("UPDATE public.sl_sessions") && sql.includes("resigned_by")) {
            ses.status = params[1];
            ses.winnerId = params[2];
            ses.resignedBy = params[3];
            ses.stateVersion = params[4];
            return { rowCount: 1 };
          }
          if (sql.includes("UPDATE public.sl_player_states")) {
            const p = ses.players.find((x) => x.userId === params[1]);
            p.currentPosition = params[2];
            p.turnCount = params[3];
            p.consecutiveSixes = params[4];
            p.connectedStatus = params[5];
            return { rowCount: 1 };
          }
          if (sql.includes("INSERT INTO public.sl_move_history")) {
            const mv = {
              actionId: params[1],
              playerId: params[2],
              diceValue: params[3],
              previousPosition: params[4],
              normalDestination: params[5],
              finalPosition: params[6],
              overshot: params[7],
              specialMoveType: params[8],
              specialMoveFrom: params[9],
              specialMoveTo: params[10],
              stateVersion: params[11],
            };
            moves.unshift(mv);
            ses.lastMove = mv;
            ses.recentMoves = moves.slice(0, 10);
            return { rowCount: 1 };
          }
          return { rows: [], rowCount: 0 };
        },
      };
      const out = await mutatorFn({ client, snapshot: await this.getSessionSnapshot(gameId) });
      return { found: true, ...out, session: await this.getSessionSnapshot(gameId) };
    },
    async getPlayerHistory() { return { stats: { totalGames: 1, totalWins: 1, winRatePercent: 100 }, matches: [] }; },
  };
}

async function runServiceAndMultiplayerTests() {
  const repo = createInMemoryRepository();
  const events = new SnakesLaddersEvents();
  const svc = new SnakesLaddersService(repo, events);

  // 1. Sender cannot invite self; sender cannot accept own invite; stranger cannot view/accept
  await assert.rejects(
    () => svc.createInvitation({ senderId: USER_A, receiverId: USER_A }),
    (e) => e instanceof GameRuleError && e.code === "SELF_INVITE_FORBIDDEN"
  );

  const inv1 = await svc.createInvitation({ senderId: USER_A, receiverId: USER_B });
  const inv2 = await svc.createInvitation({ senderId: USER_A, receiverId: USER_B });
  assert.notEqual(inv1.id, inv2.id); // multiple simultaneous invitations handled safely

  await assert.rejects(
    () => svc.acceptInvitation(inv1.id, USER_A),
    (e) => e instanceof GameRuleError && e.code === "SELF_ACCEPT_FORBIDDEN"
  );
  await assert.rejects(
    () => svc.acceptInvitation(inv1.id, USER_C),
    (e) => e instanceof GameRuleError && e.code === "UNAUTHORIZED"
  );
  console.log("PASS invitation auth: self-accept and unauthorized third-party blocked");

  // 2. Declined invitation cannot be accepted
  await svc.declineInvitation(inv2.id, USER_B);
  await assert.rejects(
    () => svc.acceptInvitation(inv2.id, USER_B),
    (e) => e instanceof GameRuleError && e.code === "INVALID_INVITATION_STATUS"
  );
  console.log("PASS declined invitation cannot be accepted");

  // 3. Expired invitation rejected
  const expiredRaw = await repo.createInvitation({ senderId: USER_A, receiverId: USER_B, ttlMs: -1000 });
  await assert.rejects(
    () => svc.acceptInvitation(expiredRaw.id, USER_B),
    (e) => e instanceof GameRuleError && e.code === "INVITATION_EXPIRED"
  );
  console.log("PASS expired invitation rejected cleanly");

  // 4. Valid accept + repeated accept idempotency (same gameId, no duplicate sessions)
  const accepted1 = await svc.acceptInvitation(inv1.id, USER_B);
  const accepted2 = await svc.acceptInvitation(inv1.id, USER_B);
  assert.equal(accepted1.idempotent, false);
  assert.equal(accepted2.idempotent, true);
  assert.equal(accepted1.session.id, accepted2.session.id);
  const gameId = accepted1.session.id;
  console.log("PASS invitation accepted & repeated acceptance is idempotent");

  // 5. Wrong turn & unauthorized roll rejected; idempotent replay on duplicate actionId; both players see same state
  await assert.rejects(
    () => svc.rollDiceAndMove({ gameId, userId: USER_C, actionId: "stranger" }),
    (e) => e instanceof GameRuleError && e.code === "NOT_A_PARTICIPANT"
  );
  await assert.rejects(
    () => svc.rollDiceAndMove({ gameId, userId: USER_B, actionId: "wrong-turn" }),
    (e) => e instanceof GameRuleError && e.code === "NOT_YOUR_TURN"
  );

  const roll1 = await svc.rollDiceAndMove({ gameId, userId: USER_A, actionId: "act-unique-1" });
  const roll1Dup = await svc.rollDiceAndMove({ gameId, userId: USER_A, actionId: "act-unique-1" });
  assert.equal(roll1.idempotentReplay, false);
  assert.equal(roll1Dup.idempotentReplay, true);
  assert.equal(roll1Dup.move.diceValue, roll1.move.diceValue);
  assert.equal(roll1Dup.session.stateVersion, roll1.session.stateVersion);

  const viewA = await svc.getSession(gameId, USER_A);
  const viewB = await svc.getSession(gameId, USER_B);
  assert.deepEqual(viewA.players.map((p) => p.currentPosition), viewB.players.map((p) => p.currentPosition));
  console.log("PASS roll idempotency, turn checks, and identical synchronized views");

  // 6. Disconnection & reconnection preserve session; resignation finishes & posts chat summary once
  await svc.disconnectPlayer(gameId, USER_B);
  const midSnap = await svc.getSession(gameId, USER_A);
  assert.equal(midSnap.status, "ACTIVE");
  assert.equal(midSnap.players[1].connectedStatus, "OFFLINE");

  const reconnected = await svc.reconnectPlayer(gameId, USER_B);
  assert.equal(reconnected.players[1].connectedStatus, "ONLINE");

  const resigned = await svc.resignGame(gameId, USER_B);
  assert.equal(resigned.status, "FINISHED");
  assert.equal(resigned.winnerId, USER_A);
  assert.equal(repo.chatSummaries.length, 1);

  // Reconnect after finish must not duplicate chat summary
  await svc.reconnectPlayer(gameId, USER_A);
  assert.equal(repo.chatSummaries.length, 1);
  console.log("PASS disconnect/reconnect preservation, resignation, and single chat summary");
}

runServiceAndMultiplayerTests().catch((err) => {
  console.error("FAIL:", err);
  process.exit(1);
});
