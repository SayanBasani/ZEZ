const GAME_TYPE = "snakes_and_ladders";

const INVITATION_STATUS = Object.freeze({
  PENDING: "PENDING",
  ACCEPTED: "ACCEPTED",
  DECLINED: "DECLINED",
  EXPIRED: "EXPIRED",
  CANCELLED: "CANCELLED",
});

const SESSION_STATUS = Object.freeze({
  WAITING: "WAITING",
  ACTIVE: "ACTIVE",
  FINISHED: "FINISHED",
  ABANDONED: "ABANDONED",
});

const SPECIAL_MOVE_TYPE = Object.freeze({
  NONE: "NONE",
  SNAKE: "SNAKE",
  LADDER: "LADDER",
});

const CONNECTION_STATUS = Object.freeze({
  ONLINE: "ONLINE",
  OFFLINE: "OFFLINE",
});

const DEFAULT_PLAYER_COLORS = Object.freeze(["red", "blue", "green", "yellow"]);

const GAME_EVENTS = Object.freeze({
  INVITE_RECEIVED: "game:invite:received",
  INVITE_ACCEPTED: "game:invite:accepted",
  INVITE_DECLINED: "game:invite:declined",
  INVITE_EXPIRED: "game:invite:expired",
  STARTED: "game:started",
  DICE_ROLLED: "game:dice:rolled",
  PLAYER_MOVED: "game:player:moved",
  SNAKE_ACTIVATED: "game:snake:activated",
  LADDER_ACTIVATED: "game:ladder:activated",
  TURN_CHANGED: "game:turn:changed",
  FINISHED: "game:finished",
  PLAYER_DISCONNECTED: "game:player:disconnected",
  PLAYER_RECONNECTED: "game:player:reconnected",
  STATE_UPDATED: "game:state:updated",
});

module.exports = {
  GAME_TYPE,
  INVITATION_STATUS,
  SESSION_STATUS,
  SPECIAL_MOVE_TYPE,
  CONNECTION_STATUS,
  DEFAULT_PLAYER_COLORS,
  GAME_EVENTS,
};
