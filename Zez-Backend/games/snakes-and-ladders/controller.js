const express = require("express");
const { GameRuleError } = require("./engine");
const { isValidUuid } = require("./validation");

function extractRequesterId(req) {
  return (
    req.header("X-User-Id") ||
    (req.body && req.body.userId) ||
    (req.query && req.query.userId) ||
    null
  );
}

function handleError(res, err) {
  if (err instanceof GameRuleError) {
    return res.status(err.statusCode || 400).json({
      success: false,
      code: err.code,
      error: err.message,
    });
  }
  console.error("[Snakes&Ladders] Controller error:", err);
  return res.status(500).json({
    success: false,
    code: "INTERNAL_ERROR",
    error: "Unexpected game server error.",
  });
}

function createSnakesLaddersController({ service, events }) {
  const router = express.Router();

  router.get("/board-config", (_req, res) => {
    res.json({ success: true, boardConfig: service.getPublicBoardConfig() });
  });

  router.get("/history", async (req, res) => {
    try {
      const userId = extractRequesterId(req);
      const history = await service.getHistory(userId);
      return res.json({ success: true, ...history });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.post("/invitations", async (req, res) => {
    try {
      const { senderId: bodySenderId, receiverId, conversationId } = req.body || {};
      const headerUserId = req.header("X-User-Id");
      const senderId = bodySenderId || headerUserId;
      if (headerUserId && bodySenderId && headerUserId !== bodySenderId) {
        return res.status(403).json({ success: false, code: "FORBIDDEN", error: "Authenticated user mismatch." });
      }
      const invitation = await service.createInvitation({
        senderId,
        receiverId,
        conversationId: isValidUuid(conversationId) ? conversationId : null,
      });
      return res.status(201).json({ success: true, invitation });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.get("/invitations/:id", async (req, res) => {
    try {
      res.set("Cache-Control", "no-store, no-cache, must-revalidate");
      const requesterId = extractRequesterId(req);
      const result = await service.getInvitation(req.params.id, requesterId);
      return res.json({ success: true, ...result });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.get("/invitations/:id/events", async (req, res) => {
    try {
      res.set("Cache-Control", "no-store, no-cache, must-revalidate");
      const requesterId = extractRequesterId(req);
      const state = await service.getInvitation(req.params.id, requesterId);
      if (state.invitation.status !== "PENDING") {
        return res.json({ success: true, changed: true, ...state });
      }
      const waitRes = await events.waitForInvitationUpdate(req.params.id, 20000);
      if (waitRes.changed && waitRes.packet) {
        const refreshed = await service.getInvitation(req.params.id, requesterId);
        return res.json({ success: true, changed: true, event: waitRes.packet.event, ...refreshed });
      }
      return res.json({ success: true, changed: false, timeout: true, ...state });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.post("/invitations/:id/accept", async (req, res) => {
    try {
      const requesterId = extractRequesterId(req);
      const { invitation, session, idempotent } = await service.acceptInvitation(req.params.id, requesterId);
      return res.json({ success: true, invitation, session, game: session, idempotent });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.post("/invitations/:id/decline", async (req, res) => {
    try {
      const requesterId = extractRequesterId(req);
      const invitation = await service.declineInvitation(req.params.id, requesterId);
      return res.json({ success: true, invitation });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.get("/:gameId", async (req, res) => {
    try {
      res.set("Cache-Control", "no-store, no-cache, must-revalidate");
      const requesterId = extractRequesterId(req);
      const session = await service.getSession(req.params.gameId, requesterId);
      return res.json({ success: true, session, game: session });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.get("/:gameId/events", async (req, res) => {
    try {
      res.set("Cache-Control", "no-store, no-cache, must-revalidate");
      const requesterId = extractRequesterId(req);
      const sinceVersion = parseInt(req.query.sinceVersion || "0", 10);
      const current = await service.getSession(req.params.gameId, requesterId);
      if (current.stateVersion > sinceVersion || current.status === "FINISHED" || current.status === "ABANDONED") {
        return res.json({ success: true, changed: true, session: current, game: current });
      }
      const waitRes = await events.waitForSessionUpdate(req.params.gameId, 20000);
      if (waitRes.changed && waitRes.packet && waitRes.packet.session) {
        return res.json({
          success: true,
          changed: true,
          event: waitRes.packet.event,
          move: waitRes.packet.move || null,
          session: waitRes.packet.session,
          game: waitRes.packet.session,
        });
      }
      return res.json({ success: true, changed: false, timeout: true, session: current, game: current });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.post("/:gameId/roll", async (req, res) => {
    try {
      const userId = extractRequesterId(req);
      const { actionId } = req.body || {};
      const result = await service.rollDiceAndMove({
        gameId: req.params.gameId,
        userId,
        actionId,
      });
      return res.json({
        success: true,
        idempotentReplay: result.idempotentReplay,
        move: result.move,
        session: result.session,
        game: result.session,
      });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.post("/:gameId/resign", async (req, res) => {
    try {
      const userId = extractRequesterId(req);
      const session = await service.resignGame(req.params.gameId, userId);
      return res.json({ success: true, session, game: session });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.post("/:gameId/reconnect", async (req, res) => {
    try {
      const userId = extractRequesterId(req);
      const session = await service.reconnectPlayer(req.params.gameId, userId);
      return res.json({ success: true, session, game: session });
    } catch (err) {
      return handleError(res, err);
    }
  });

  router.post("/:gameId/disconnect", async (req, res) => {
    try {
      const userId = extractRequesterId(req);
      const session = await service.disconnectPlayer(req.params.gameId, userId);
      return res.json({ success: true, session, game: session });
    } catch (err) {
      return handleError(res, err);
    }
  });

  return router;
}

module.exports = {
  createSnakesLaddersController,
};
