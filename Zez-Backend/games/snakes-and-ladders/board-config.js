const DEFAULT_BOARD_CONFIG = Object.freeze({
  version: "sl-board-v1",
  boardSize: 100,
  rows: 10,
  columns: 10,
  minPlayers: 2,
  maxPlayers: 2,
  snakes: Object.freeze([
    Object.freeze({ head: 22, tail: 2 }),
    Object.freeze({ head: 34, tail: 7 }),
    Object.freeze({ head: 59, tail: 40 }),
    Object.freeze({ head: 75, tail: 51 }),
    Object.freeze({ head: 88, tail: 71 }),
    Object.freeze({ head: 98, tail: 24 }),
  ]),
  ladders: Object.freeze([
    Object.freeze({ start: 5, end: 26 }),
    Object.freeze({ start: 11, end: 49 }),
    Object.freeze({ start: 56, end: 64 }),
    Object.freeze({ start: 60, end: 79 }),
    Object.freeze({ start: 74, end: 93 }),
    Object.freeze({ start: 80, end: 82 }),
  ]),
});

const BOARD_CONFIGS = Object.freeze({
  "sl-board-v1": DEFAULT_BOARD_CONFIG,
});

function getBoardConfig(version = "sl-board-v1") {
  return BOARD_CONFIGS[version] || DEFAULT_BOARD_CONFIG;
}

module.exports = {
  DEFAULT_BOARD_CONFIG,
  BOARD_CONFIGS,
  getBoardConfig,
};
