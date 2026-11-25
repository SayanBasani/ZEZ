const crypto = require("node:crypto");

/**
 * Cryptographically secure 6-sided dice generator (integer 1..6).
 * Executed strictly on the server; client-supplied dice values are never trusted.
 */
function rollDice() {
  return crypto.randomInt(1, 7);
}

module.exports = {
  rollDice,
};
