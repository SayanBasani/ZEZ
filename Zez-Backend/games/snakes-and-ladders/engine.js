const crypto = require("node:crypto");
const { getBoardConfig } = require("./board-config");
const { assertValidBoardConfig } = require("./validation");
const {
  GAME_TYPE,
  SESSION_STATUS,
  SPECIAL_MOVE_TYPE,
  CONNECTION_STATUS,
  DEFAULT_PLAYER_COLORS,
} = require("./types");

class GameRuleError extends Error {
  constructor(code, message, statusCode = 409) {
    super(message);
    this.name = "GameRuleError";
    this.code = code;
    this.statusCode = statusCode;
  }
}

function createInitialSessionState({
  gameId,
  conversationId = null,
  invitationId = null,
  playersInput,
  starterIndex = null,
  boardConfigVersion = "sl-board-v1",
  customColors = null,
}) {
  const boardConfig = assertValidBoardConfig(getBoardConfig(boardConfigVersion));

  if (!Array.isArray(playersInput)) {
    throw new GameRuleError("INVALID_PLAYERS", "playersInput must be an array", 400);
  }
  if (playersInput.length < boardConfig.minPlayers || playersInput.length > boardConfig.maxPlayers) {
    throw new GameRuleError(
      "INVALID_PLAYER_COUNT",
      `Session requires between ${boardConfig.minPlayers} and ${boardConfig.maxPlayers} players`,
      400
    );
  }

  const seenUserIds = new Set();
  const players = playersInput.map((p, index) => {
    if (!p || !p.userId) {
      throw new GameRuleError("INVALID_PLAYER", "Each player must include userId", 400);
    }
    if (seenUserIds.has(p.userId)) {
      throw new GameRuleError("DUPLICATE_PLAYER", "Duplicate player userId in session", 400);
    }
    seenUserIds.add(p.userId);

    const assignedColor =
      (Array.isArray(customColors) && customColors[index]) ||
      p.color ||
      DEFAULT_PLAYER_COLORS[index % DEFAULT_PLAYER_COLORS.length];

    return {
      userId: p.userId,
      name: p.name || `Player ${index + 1}`,
      avatarUrl: p.avatarUrl || null,
      seatIndex: index,
      tokenColor: assignedColor,
      currentPosition: 0,
      turnCount: 0,
      consecutiveSixes: 0,
      connectedStatus: p.connectedStatus || CONNECTION_STATUS.ONLINE,
    };
  });

  const resolvedStarter =
    Number.isInteger(starterIndex) && starterIndex >= 0 && starterIndex < players.length
      ? starterIndex
      : crypto.randomInt(0, players.length);

  return {
    id: gameId,
    conversationId,
    invitationId,
    gameType: GAME_TYPE,
    status: SESSION_STATUS.ACTIVE,
    boardConfigurationVersion: boardConfig.version,
    boardConfig,
    players,
    currentTurnIndex: resolvedStarter,
    currentTurnPlayerId: players[resolvedStarter].userId,
    winnerId: null,
    stateVersion: 1,
    lastMove: null,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    finishedAt: null,
  };
}

function buildLookupMaps(boardConfig) {
  const snakesByHead = new Map();
  for (const s of boardConfig.snakes) {
    snakesByHead.set(s.head, s);
  }
  const laddersByStart = new Map();
  for (const l of boardConfig.ladders) {
    laddersByStart.set(l.start, l);
  }
  return { snakesByHead, laddersByStart };
}

/**
 * Pure authoritative state transition for one dice roll.
 */
function applyDiceRoll(currentState, { userId, actionId, diceValue, boardConfig: overrideConfig = null }) {
  if (!currentState) {
    throw new GameRuleError("SESSION_NOT_FOUND", "Game session does not exist", 404);
  }
  if (!actionId || typeof actionId !== "string" || !actionId.trim()) {
    throw new GameRuleError("MISSING_ACTION_ID", "Unique actionId is required", 400);
  }
  if (!Number.isInteger(diceValue) || diceValue < 1 || diceValue > 6) {
    throw new GameRuleError("INVALID_DICE_VALUE", "Dice value must be an integer 1..6", 400);
  }
  if (currentState.status === SESSION_STATUS.FINISHED || currentState.status === SESSION_STATUS.ABANDONED) {
    throw new GameRuleError("GAME_ALREADY_FINISHED", "Game has already finished", 409);
  }
  if (currentState.status !== SESSION_STATUS.ACTIVE && currentState.status !== SESSION_STATUS.WAITING) {
    throw new GameRuleError("GAME_NOT_ACTIVE", "Game is not active", 409);
  }

  const players = (currentState.players || []).map((p) => ({ ...p }));
  const actorIndex = players.findIndex((p) => p.userId === userId);
  if (actorIndex === -1) {
    throw new GameRuleError("NOT_A_PARTICIPANT", "You are not a participant in this game", 403);
  }

  let turnIndex = Number.isInteger(currentState.currentTurnIndex) ? currentState.currentTurnIndex : -1;
  if (turnIndex < 0 || turnIndex >= players.length) {
    turnIndex = players.findIndex((p) => p.userId === currentState.currentTurnPlayerId);
  }
  if (turnIndex !== actorIndex) {
    throw new GameRuleError("NOT_YOUR_TURN", "Wait for opponent's turn", 409);
  }

  const boardConfig = assertValidBoardConfig(
    overrideConfig || currentState.boardConfig || getBoardConfig(currentState.boardConfigurationVersion)
  );
  const { snakesByHead, laddersByStart } = buildLookupMaps(boardConfig);

  const player = players[actorIndex];
  const previousPosition = Number(player.currentPosition || 0);
  const candidate = previousPosition + diceValue;

  let normalDestination = candidate;
  let finalPosition = candidate;
  let overshot = false;
  let specialMoveType = SPECIAL_MOVE_TYPE.NONE;
  let specialMoveFrom = null;
  let specialMoveTo = null;

  if (candidate > boardConfig.boardSize) {
    overshot = true;
    normalDestination = previousPosition;
    finalPosition = previousPosition;
  } else {
    const ladder = laddersByStart.get(normalDestination);
    const snake = snakesByHead.get(normalDestination);
    if (ladder) {
      specialMoveType = SPECIAL_MOVE_TYPE.LADDER;
      specialMoveFrom = ladder.start;
      specialMoveTo = ladder.end;
      finalPosition = ladder.end;
    } else if (snake) {
      specialMoveType = SPECIAL_MOVE_TYPE.SNAKE;
      specialMoveFrom = snake.head;
      specialMoveTo = snake.tail;
      finalPosition = snake.tail;
    }
  }

  player.currentPosition = finalPosition;
  player.turnCount = Number(player.turnCount || 0) + 1;
  player.consecutiveSixes = diceValue === 6 ? Number(player.consecutiveSixes || 0) + 1 : 0;

  const isWin = finalPosition === boardConfig.boardSize;
  const grantedExtraTurn = !isWin && diceValue === 6;

  let nextTurnIndex = turnIndex;
  if (!isWin && !grantedExtraTurn) {
    nextTurnIndex = (turnIndex + 1) % players.length;
  }

  const nowIso = new Date().toISOString();
  const nextVersion = Number(currentState.stateVersion || 1) + 1;
  const nextStatus = isWin ? SESSION_STATUS.FINISHED : SESSION_STATUS.ACTIVE;
  const winnerId = isWin ? player.userId : null;

  const moveRecord = {
    actionId: actionId.trim(),
    playerId: player.userId,
    playerName: player.name,
    tokenColor: player.tokenColor,
    diceValue,
    previousPosition,
    normalDestination,
    finalPosition,
    overshot,
    specialMoveType,
    specialMoveFrom,
    specialMoveTo,
    grantedExtraTurn,
    isWin,
    stateVersion: nextVersion,
    createdAt: nowIso,
  };

  const nextState = {
    ...currentState,
    status: nextStatus,
    players,
    currentTurnIndex: nextTurnIndex,
    currentTurnPlayerId: players[nextTurnIndex].userId,
    winnerId,
    stateVersion: nextVersion,
    lastMove: moveRecord,
    updatedAt: nowIso,
    finishedAt: isWin ? nowIso : null,
  };

  return {
    nextState,
    moveRecord,
    turnChanged: !isWin && nextTurnIndex !== turnIndex,
  };
}

function resignSession(currentState, userId) {
  if (!currentState) {
    throw new GameRuleError("SESSION_NOT_FOUND", "Game session does not exist", 404);
  }
  const players = (currentState.players || []).map((p) => ({ ...p }));
  const resignIndex = players.findIndex((p) => p.userId === userId);
  if (resignIndex === -1) {
    throw new GameRuleError("NOT_A_PARTICIPANT", "Not a participant in this game", 403);
  }
  if (currentState.status === SESSION_STATUS.FINISHED || currentState.status === SESSION_STATUS.ABANDONED) {
    throw new GameRuleError("GAME_ALREADY_FINISHED", "Game has already ended", 409);
  }

  const opponent = players.find((p) => p.userId !== userId) || null;
  const nowIso = new Date().toISOString();
  const nextVersion = Number(currentState.stateVersion || 1) + 1;

  return {
    ...currentState,
    status: SESSION_STATUS.FINISHED,
    players,
    winnerId: opponent ? opponent.userId : null,
    resignedBy: userId,
    stateVersion: nextVersion,
    updatedAt: nowIso,
    finishedAt: nowIso,
  };
}

module.exports = {
  GameRuleError,
  createInitialSessionState,
  applyDiceRoll,
  resignSession,
};
