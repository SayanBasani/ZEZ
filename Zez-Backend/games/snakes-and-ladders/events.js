const EventEmitter = require("node:events");
const { GAME_EVENTS } = require("./types");

class SnakesLaddersEvents {
  constructor() {
    this.emitter = new EventEmitter();
    this.emitter.setMaxListeners(2000);
  }

  emitSessionEvent(gameId, eventName, payload) {
    const packet = {
      event: eventName,
      gameId,
      timestamp: new Date().toISOString(),
      ...payload,
    };
    this.emitter.emit(`session:${gameId}`, packet);
  }

  emitInvitationEvent(invitationId, eventName, payload) {
    const packet = {
      event: eventName,
      invitationId,
      timestamp: new Date().toISOString(),
      ...payload,
    };
    this.emitter.emit(`invite:${invitationId}`, packet);
  }

  waitForSessionUpdate(gameId, timeoutMs = 20000) {
    return new Promise((resolve) => {
      let settled = false;
      const cleanup = () => {
        if (settled) return;
        settled = true;
        clearTimeout(timer);
        this.emitter.removeListener(`session:${gameId}`, onUpdate);
      };
      const onUpdate = (packet) => {
        cleanup();
        resolve({ changed: true, timeout: false, packet });
      };
      const timer = setTimeout(() => {
        cleanup();
        resolve({ changed: false, timeout: true, packet: null });
      }, timeoutMs);
      this.emitter.once(`session:${gameId}`, onUpdate);
    });
  }

  waitForInvitationUpdate(invitationId, timeoutMs = 20000) {
    return new Promise((resolve) => {
      let settled = false;
      const cleanup = () => {
        if (settled) return;
        settled = true;
        clearTimeout(timer);
        this.emitter.removeListener(`invite:${invitationId}`, onUpdate);
      };
      const onUpdate = (packet) => {
        cleanup();
        resolve({ changed: true, timeout: false, packet });
      };
      const timer = setTimeout(() => {
        cleanup();
        resolve({ changed: false, timeout: true, packet: null });
      }, timeoutMs);
      this.emitter.once(`invite:${invitationId}`, onUpdate);
    });
  }
}

module.exports = {
  SnakesLaddersEvents,
  snakesLaddersEvents: new SnakesLaddersEvents(),
  GAME_EVENTS,
};
