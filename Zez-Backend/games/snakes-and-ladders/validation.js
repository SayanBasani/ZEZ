const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function isValidUuid(value) {
  return typeof value === "string" && UUID_REGEX.test(value.trim());
}

function validateBoardConfig(config) {
  const errors = [];
  if (!config || typeof config !== "object") {
    return { valid: false, errors: ["Board config must be an object"] };
  }

  const { boardSize, rows, columns, snakes, ladders, minPlayers, maxPlayers } = config;
  if (!Number.isInteger(rows) || !Number.isInteger(columns) || rows <= 0 || columns <= 0) {
    errors.push("rows and columns must be positive integers");
  }
  if (boardSize !== rows * columns) {
    errors.push("boardSize must equal rows * columns");
  }
  if (!Number.isInteger(minPlayers) || !Number.isInteger(maxPlayers) || minPlayers < 2 || maxPlayers < minPlayers) {
    errors.push("minPlayers/maxPlayers configuration invalid");
  }
  if (!Array.isArray(snakes) || !Array.isArray(ladders)) {
    errors.push("snakes and ladders must be arrays");
    return { valid: false, errors };
  }

  const usedStarts = new Set();
  const usedEnds = new Set();
  const allEndpoints = new Set();

  for (const snake of snakes) {
    if (!snake || !Number.isInteger(snake.head) || !Number.isInteger(snake.tail)) {
      errors.push("Snake endpoints must be integers");
      continue;
    }
    if (snake.head < 2 || snake.head >= boardSize) {
      errors.push(`Snake head ${snake.head} must be strictly between 2 and ${boardSize - 1}`);
    }
    if (snake.tail < 1 || snake.tail >= boardSize) {
      errors.push(`Snake tail ${snake.tail} must be within 1..${boardSize - 1}`);
    }
    if (snake.head <= snake.tail) {
      errors.push(`Snake head (${snake.head}) must exceed tail (${snake.tail})`);
    }
    if (usedStarts.has(snake.head) || allEndpoints.has(snake.head)) {
      errors.push(`Conflicting or chained snake head endpoint ${snake.head}`);
    }
    if (allEndpoints.has(snake.tail)) {
      errors.push(`Conflicting or duplicate snake tail endpoint ${snake.tail}`);
    }
    usedStarts.add(snake.head);
    usedEnds.add(snake.tail);
    allEndpoints.add(snake.head);
    allEndpoints.add(snake.tail);
  }

  for (const ladder of ladders) {
    if (!ladder || !Number.isInteger(ladder.start) || !Number.isInteger(ladder.end)) {
      errors.push("Ladder endpoints must be integers");
      continue;
    }
    if (ladder.start < 2 || ladder.start >= boardSize) {
      errors.push(`Ladder start ${ladder.start} must be within 2..${boardSize - 1}`);
    }
    if (ladder.end < 2 || ladder.end > boardSize) {
      errors.push(`Ladder end ${ladder.end} must be within 2..${boardSize}`);
    }
    if (ladder.end <= ladder.start) {
      errors.push(`Ladder end (${ladder.end}) must exceed start (${ladder.start})`);
    }
    if (usedStarts.has(ladder.start) || allEndpoints.has(ladder.start)) {
      errors.push(`Conflicting or chained ladder start endpoint ${ladder.start}`);
    }
    if (allEndpoints.has(ladder.end)) {
      errors.push(`Conflicting or duplicate ladder end endpoint ${ladder.end}`);
    }
    usedStarts.add(ladder.start);
    usedEnds.add(ladder.end);
    allEndpoints.add(ladder.start);
    allEndpoints.add(ladder.end);
  }

  return {
    valid: errors.length === 0,
    errors,
  };
}

function assertValidBoardConfig(config) {
  const res = validateBoardConfig(config);
  if (!res.valid) {
    throw new Error(`Invalid board configuration: ${res.errors.join("; ")}`);
  }
  return config;
}

module.exports = {
  isValidUuid,
  validateBoardConfig,
  assertValidBoardConfig,
};
