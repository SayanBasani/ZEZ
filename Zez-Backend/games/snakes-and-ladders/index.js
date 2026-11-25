const { pool } = require("../../src/db");
const { SnakesLaddersRepository, ensureTables } = require("./repository");
const { SnakesLaddersService } = require("./service");
const { createSnakesLaddersController } = require("./controller");
const { snakesLaddersEvents } = require("./events");

const repository = new SnakesLaddersRepository(pool);
const service = new SnakesLaddersService(repository, snakesLaddersEvents);
const router = createSnakesLaddersController({ service, events: snakesLaddersEvents });

async function initializeSnakesLaddersSchema() {
  await ensureTables(pool);
}

module.exports = {
  router,
  repository,
  service,
  events: snakesLaddersEvents,
  initializeSnakesLaddersSchema,
};
