const { EventEmitter } = require("node:events");

const realtimeEvents = new EventEmitter();
realtimeEvents.setMaxListeners(0);

module.exports = realtimeEvents;
