const assert = require("node:assert/strict");
const { getBoardConfig } = require("../board-config");
const { squareToCoordinates, coordinatesToSquare, squareCenterNormalized } = require("../coordinates");
const { validateBoardConfig, assertValidBoardConfig } = require("../validation");
const { rollDice } = require("../dice");
const { createInitialSessionState, applyDiceRoll, resignSession, GameRuleError } = require("../engine");

const USER_A = "11111111-1111-4111-8111-111111111111";
const USER_B = "22222222-2222-4222-8222-222222222222";
const USER_C = "33333333-3333-4333-8333-333333333333";

function makeSession(posA = 0, posB = 0, starterIndex = 0, boardConfig = null) {
  const s = createInitialSessionState({
    gameId: "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
    playersInput: [
      { userId: USER_A, name: "Alice" },
      { userId: USER_B, name: "Bob" },
    ],
    starterIndex,
    boardConfigVersion: "sl-board-v1",
  });
  s.players[0].currentPosition = posA;
  s.players[1].currentPosition = posB;
  if (boardConfig) {
    s.boardConfig = boardConfig;
  }
  return s;
}

function runAllEngineTests() {
  // 1. All 100 coordinate tests & inverse bijective check
  const seenCoords = new Set();
  for (let sq = 1; sq <= 100; sq++) {
    const c = squareToCoordinates(sq);
    assert.equal(c.square, sq);
    assert.ok(c.rowFromTop >= 0 && c.rowFromTop < 10);
    assert.ok(c.col >= 0 && c.col < 10);
    const key = `${c.rowFromTop},${c.col}`;
    assert.ok(!seenCoords.has(key), `Duplicate grid cell ${key}`);
    seenCoords.add(key);
    assert.equal(coordinatesToSquare(c.rowFromTop, c.col), sq);
    const norm = squareCenterNormalized(sq);
    assert.ok(norm.x > 0 && norm.x < 1 && norm.y > 0 && norm.y < 1);
  }
  assert.equal(seenCoords.size, 100);
  assert.deepEqual({ r: squareToCoordinates(1).rowFromTop, c: squareToCoordinates(1).col }, { r: 9, c: 0 });
  assert.deepEqual({ r: squareToCoordinates(10).rowFromTop, c: squareToCoordinates(10).col }, { r: 9, c: 9 });
  assert.deepEqual({ r: squareToCoordinates(11).rowFromTop, c: squareToCoordinates(11).col }, { r: 8, c: 9 });
  assert.deepEqual({ r: squareToCoordinates(20).rowFromTop, c: squareToCoordinates(20).col }, { r: 8, c: 0 });
  assert.deepEqual({ r: squareToCoordinates(21).rowFromTop, c: squareToCoordinates(21).col }, { r: 7, c: 0 });
  assert.deepEqual({ r: squareToCoordinates(100).rowFromTop, c: squareToCoordinates(100).col }, { r: 0, c: 0 });
  assert.throws(() => squareToCoordinates(0), RangeError);
  assert.throws(() => squareToCoordinates(101), RangeError);
  console.log("PASS coordinates: all 100 zigzag squares verified");

  // 2. Board config validation & cryptographic dice range
  const cfg = getBoardConfig("sl-board-v1");
  assertValidBoardConfig(cfg);
  const badCfg = validateBoardConfig({
    boardSize: 100, rows: 10, columns: 10, minPlayers: 2, maxPlayers: 2,
    snakes: [{ head: 10, tail: 25 }],
    ladders: [{ start: 25, end: 50 }],
  });
  assert.equal(badCfg.valid, false);
  for (let i = 0; i < 100; i++) {
    const d = rollDice();
    assert.ok(Number.isInteger(d) && d >= 1 && d <= 6);
  }
  console.log("PASS board config validation + secure RNG dice range (1..6)");

  // 3. Initial position = 0, rolling 1 enters 1, rolling 6 enters 6 (no 6 required)
  const sInit = makeSession(0, 0, 0);
  assert.equal(sInit.players[0].currentPosition, 0);
  assert.equal(sInit.players[1].currentPosition, 0);
  assert.equal(sInit.players[0].tokenColor, "red");
  assert.equal(sInit.players[1].tokenColor, "blue");

  const r1 = applyDiceRoll(sInit, { userId: USER_A, actionId: "a1", diceValue: 1 });
  assert.equal(r1.nextState.players[0].currentPosition, 1);
  assert.equal(r1.nextState.currentTurnPlayerId, USER_B);

  const r6FromZero = applyDiceRoll(makeSession(0, 0, 0), { userId: USER_A, actionId: "a2", diceValue: 6 });
  assert.equal(r6FromZero.nextState.players[0].currentPosition, 6);
  assert.equal(r6FromZero.nextState.currentTurnPlayerId, USER_A);
  console.log("PASS initial entry (dice 1->1, 6->6 + extra turn)");

  // 4. Normal movement & turn switch
  const rNorm = applyDiceRoll(makeSession(15, 10, 0), { userId: USER_A, actionId: "a3", diceValue: 4 });
  assert.equal(rNorm.nextState.players[0].currentPosition, 19);
  assert.equal(rNorm.moveRecord.specialMoveType, "NONE");
  assert.equal(rNorm.nextState.currentTurnPlayerId, USER_B);
  console.log("PASS normal movement & turn switching");

  // 5. Every ladder works
  for (const ladder of cfg.ladders) {
    const startPos = ladder.start - 3;
    const s = makeSession(startPos, 0, 0);
    const res = applyDiceRoll(s, { userId: USER_A, actionId: `lad-${ladder.start}`, diceValue: 3 });
    assert.equal(res.moveRecord.normalDestination, ladder.start);
    assert.equal(res.moveRecord.specialMoveType, "LADDER");
    assert.equal(res.moveRecord.finalPosition, ladder.end);
    assert.equal(res.nextState.players[0].currentPosition, ladder.end);
  }
  console.log(`PASS all ${cfg.ladders.length} configured ladders activate accurately`);

  // 6. Every snake works
  for (const snake of cfg.snakes) {
    const startPos = snake.head - 2;
    const s = makeSession(startPos, 0, 0);
    const res = applyDiceRoll(s, { userId: USER_A, actionId: `snk-${snake.head}`, diceValue: 2 });
    assert.equal(res.moveRecord.normalDestination, snake.head);
    assert.equal(res.moveRecord.specialMoveType, "SNAKE");
    assert.equal(res.moveRecord.finalPosition, snake.tail);
    assert.equal(res.nextState.players[0].currentPosition, snake.tail);
  }
  console.log(`PASS all ${cfg.snakes.length} configured snakes activate accurately`);

  // 7. Passing over snake or ladder does NOT activate it
  const passSnake = applyDiceRoll(makeSession(20, 0, 0), { userId: USER_A, actionId: "passS", diceValue: 4 });
  assert.equal(passSnake.moveRecord.specialMoveType, "NONE");
  assert.equal(passSnake.nextState.players[0].currentPosition, 24);
  const passLadder = applyDiceRoll(makeSession(3, 0, 0), { userId: USER_A, actionId: "passL", diceValue: 4 });
  assert.equal(passLadder.moveRecord.specialMoveType, "NONE");
  assert.equal(passLadder.nextState.players[0].currentPosition, 7);
  console.log("PASS passing over snakes and ladders does not trigger them");

  // 8. Six grants extra turn, multiple sixes allowed (no 3-six penalty)
  let sMulti = makeSession(0, 0, 0);
  for (let i = 1; i <= 4; i++) {
    const step = applyDiceRoll(sMulti, { userId: USER_A, actionId: `six-${i}`, diceValue: 6 });
    sMulti = step.nextState;
    assert.equal(sMulti.currentTurnPlayerId, USER_A);
    assert.equal(sMulti.players[0].consecutiveSixes, i);
  }
  assert.equal(sMulti.players[0].currentPosition, 24);
  console.log("PASS rolling multiple sixes grants continuous extra turns without penalty");

  // 9. Exact 100 rule (overshoot stays in place, no bounce-back) & shared squares
  const sOver = makeSession(96, 15, 0);
  const resOver = applyDiceRoll(sOver, { userId: USER_A, actionId: "over1", diceValue: 5 });
  assert.equal(resOver.moveRecord.overshot, true);
  assert.equal(resOver.nextState.players[0].currentPosition, 96);
  assert.equal(resOver.nextState.status, "ACTIVE");

  const sShare = makeSession(14, 15, 0);
  const resShare = applyDiceRoll(sShare, { userId: USER_A, actionId: "share1", diceValue: 1 });
  assert.equal(resShare.nextState.players[0].currentPosition, 15);
  assert.equal(resShare.nextState.players[1].currentPosition, 15);
  console.log("PASS exact 100 overshoot stay-in-place & shared-square coexistence");

  // 10. Exact 100 win & stop further rolls + win via 6 immediately ends game
  const sWin = makeSession(94, 50, 0);
  const resWin = applyDiceRoll(sWin, { userId: USER_A, actionId: "win6", diceValue: 6 });
  assert.equal(resWin.nextState.players[0].currentPosition, 100);
  assert.equal(resWin.nextState.status, "FINISHED");
  assert.equal(resWin.nextState.winnerId, USER_A);
  assert.equal(resWin.moveRecord.grantedExtraTurn, false);
  assert.throws(
    () => applyDiceRoll(resWin.nextState, { userId: USER_A, actionId: "postWin", diceValue: 2 }),
    (err) => err instanceof GameRuleError && err.code === "GAME_ALREADY_FINISHED"
  );
  console.log("PASS winning at 100 (including via 6) terminates session & blocks further rolls");

  // 11. Winning via ladder ending at 100
  const altConfig = assertValidBoardConfig({
    version: "sl-test-win-ladder",
    boardSize: 100,
    rows: 10,
    columns: 10,
    minPlayers: 2,
    maxPlayers: 2,
    snakes: [{ head: 40, tail: 10 }],
    ladders: [{ start: 80, end: 100 }],
  });
  const sLadWin = makeSession(78, 12, 0, altConfig);
  const resLadWin = applyDiceRoll(sLadWin, { userId: USER_A, actionId: "ladWin", diceValue: 2 });
  assert.equal(resLadWin.nextState.players[0].currentPosition, 100);
  assert.equal(resLadWin.nextState.status, "FINISHED");
  assert.equal(resLadWin.nextState.winnerId, USER_A);
  console.log("PASS winning immediately via ladder ending at 100");

  // 12. Security / turn / unauthorized / resignation checks
  const sAuth = makeSession(10, 10, 0);
  assert.throws(
    () => applyDiceRoll(sAuth, { userId: USER_B, actionId: "wrongTurn", diceValue: 3 }),
    (err) => err instanceof GameRuleError && err.code === "NOT_YOUR_TURN"
  );
  assert.throws(
    () => applyDiceRoll(sAuth, { userId: USER_C, actionId: "stranger", diceValue: 3 }),
    (err) => err instanceof GameRuleError && err.code === "NOT_A_PARTICIPANT"
  );
  const resResign = resignSession(sAuth, USER_A);
  assert.equal(resResign.status, "FINISHED");
  assert.equal(resResign.winnerId, USER_B);
  console.log("PASS wrong-turn, unauthorized user, and resignation checks");
}

runAllEngineTests();
