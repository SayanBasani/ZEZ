const crypto = require("node:crypto");
const { URL } = require("node:url");
const realtimeEvents = require("./realtimeEvents");

const WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

// Binary packet types shared with Android CallMediaEngine
const PKT_AUDIO = 0x01;
const PKT_VIDEO = 0x02;
const PKT_PING = 0x03;
const PKT_PONG = 0x04;
const PKT_CTRL = 0x05;

// Active call rooms: callId -> Map<userId, WebSocketClient>
const callRooms = new Map();
// Active notification subscribers: userId -> Set<WebSocketClient>
const notifySubscribers = new Map();

function encodeWsFrame(opcode, payloadBuf) {
  const payload = Buffer.isBuffer(payloadBuf) ? payloadBuf : Buffer.from(payloadBuf || "");
  const len = payload.length;
  let header;
  if (len < 126) {
    header = Buffer.alloc(2);
    header[0] = 0x80 | opcode;
    header[1] = len;
  } else if (len < 65536) {
    header = Buffer.alloc(4);
    header[0] = 0x80 | opcode;
    header[1] = 126;
    header.writeUInt16BE(len, 2);
  } else {
    header = Buffer.alloc(10);
    header[0] = 0x80 | opcode;
    header[1] = 127;
    header.writeBigUInt64BE(BigInt(len), 2);
  }
  return Buffer.concat([header, payload]);
}

function createWsClient(socket) {
  let closed = false;
  let recvBuffer = Buffer.alloc(0);
  let fragmentedOpcode = 0;
  let fragmentedChunks = [];

  const client = {
    sendBinary(buf) {
      if (closed || socket.destroyed || !socket.writable) return false;
      try {
        // Prevent unbounded socket write backlog on very slow connections
        if (socket.writableLength > 512 * 1024) {
          return false;
        }
        socket.write(encodeWsFrame(0x02, buf));
        return true;
      } catch {
        return false;
      }
    },
    sendText(str) {
      if (closed || socket.destroyed || !socket.writable) return false;
      try {
        socket.write(encodeWsFrame(0x01, Buffer.from(String(str), "utf8")));
        return true;
      } catch {
        return false;
      }
    },
    sendPing() {
      if (closed || socket.destroyed || !socket.writable) return;
      try {
        socket.write(encodeWsFrame(0x09, Buffer.alloc(0)));
      } catch {}
    },
    close() {
      if (closed) return;
      closed = true;
      try {
        socket.write(encodeWsFrame(0x08, Buffer.alloc(0)));
      } catch {}
      try {
        socket.end();
      } catch {}
      if (typeof client.onClose === "function") client.onClose();
    },
    isClosed() {
      return closed || socket.destroyed;
    },
    writableLength() {
      return socket.writableLength || 0;
    },
    onMessage: null,
    onClose: null,
  };

  function handleCompleteFrame(opcode, payload) {
    if (opcode === 0x08) {
      client.close();
      return;
    }
    if (opcode === 0x09) {
      // Respond to Ping with Pong
      if (!closed && !socket.destroyed && socket.writable) {
        try {
          socket.write(encodeWsFrame(0x0a, payload));
        } catch {}
      }
      return;
    }
    if (opcode === 0x0a) {
      return;
    }
    if (typeof client.onMessage === "function") {
      client.onMessage(opcode, payload);
    }
  }

  socket.on("data", (chunk) => {
    recvBuffer = Buffer.concat([recvBuffer, chunk]);
    while (recvBuffer.length >= 2) {
      const b0 = recvBuffer[0];
      const b1 = recvBuffer[1];
      const fin = (b0 & 0x80) !== 0;
      const opcode = b0 & 0x0f;
      const masked = (b1 & 0x80) !== 0;
      let payloadLen = b1 & 0x7f;
      let offset = 2;

      if (payloadLen === 126) {
        if (recvBuffer.length < 4) return;
        payloadLen = recvBuffer.readUInt16BE(2);
        offset = 4;
      } else if (payloadLen === 127) {
        if (recvBuffer.length < 10) return;
        const bigLen = recvBuffer.readBigUInt64BE(2);
        if (bigLen > 4n * 1024n * 1024n) {
          client.close();
          return;
        }
        payloadLen = Number(bigLen);
        offset = 10;
      }

      if (payloadLen > 4 * 1024 * 1024) {
        client.close();
        return;
      }

      const maskLen = masked ? 4 : 0;
      const totalFrameLen = offset + maskLen + payloadLen;
      if (recvBuffer.length < totalFrameLen) return;

      let payload = Buffer.from(recvBuffer.subarray(offset + maskLen, totalFrameLen));
      if (masked) {
        const maskKey = recvBuffer.subarray(offset, offset + 4);
        for (let i = 0; i < payload.length; i++) {
          payload[i] ^= maskKey[i & 3];
        }
      }

      recvBuffer = recvBuffer.subarray(totalFrameLen);

      if (opcode === 0x08 || opcode === 0x09 || opcode === 0x0a) {
        handleCompleteFrame(opcode, payload);
        continue;
      }

      if (opcode !== 0x00) {
        if (fin) {
          handleCompleteFrame(opcode, payload);
        } else {
          fragmentedOpcode = opcode;
          fragmentedChunks = [payload];
        }
      } else if (fragmentedOpcode !== 0) {
        fragmentedChunks.push(payload);
        if (fin) {
          const combined = Buffer.concat(fragmentedChunks);
          const op = fragmentedOpcode;
          fragmentedOpcode = 0;
          fragmentedChunks = [];
          handleCompleteFrame(op, combined);
        }
      }
    }
  });

  const markClosed = () => {
    if (!closed) {
      closed = true;
      if (typeof client.onClose === "function") client.onClose();
    }
  };
  socket.on("close", markClosed);
  socket.on("error", markClosed);
  socket.setNoDelay(true);
  socket.setKeepAlive(true, 15000);

  return client;
}

function acceptUpgrade(req, socket) {
  const key = req.headers["sec-websocket-key"];
  if (!key) return null;
  const accept = crypto
    .createHash("sha1")
    .update(key + WS_GUID)
    .digest("base64");

  socket.write(
    [
      "HTTP/1.1 101 Switching Protocols",
      "Upgrade: websocket",
      "Connection: Upgrade",
      `Sec-WebSocket-Accept: ${accept}`,
      "\r\n",
    ].join("\r\n")
  );
  return createWsClient(socket);
}

function handleCallSocket(urlObj, client) {
  const callId = urlObj.searchParams.get("callId");
  const userId = urlObj.searchParams.get("userId");
  if (!callId || !userId) {
    client.close();
    return;
  }

  let room = callRooms.get(callId);
  if (!room) {
    room = new Map();
    callRooms.set(callId, room);
  }
  const prev = room.get(userId);
  if (prev && prev !== client) {
    prev.close();
  }
  room.set(userId, client);

  const pingInterval = setInterval(() => {
    if (!client.isClosed()) client.sendPing();
  }, 15000);

  client.onMessage = (opcode, payload) => {
    if (opcode !== 0x02 || !payload || payload.length < 1) return;
    const pktType = payload[0];

    if (pktType === PKT_PING) {
      // Echo back Pong immediately with original client timestamp
      const pong = Buffer.alloc(payload.length);
      payload.copy(pong);
      pong[0] = PKT_PONG;
      client.sendBinary(pong);
      return;
    }

    const r = callRooms.get(callId);
    if (!r) return;

    for (const [peerId, peerClient] of r.entries()) {
      if (peerId === userId || peerClient.isClosed()) continue;
      // On slow links (backlog > 96 KB), drop non-essential video frames cleanly while always delivering audio/ctrl
      if (pktType === PKT_VIDEO && peerClient.writableLength() > 96 * 1024) {
        continue;
      }
      peerClient.sendBinary(payload);
    }
  };

  client.onClose = () => {
    clearInterval(pingInterval);
    const r = callRooms.get(callId);
    if (r && r.get(userId) === client) {
      r.delete(userId);
      if (r.size === 0) {
        callRooms.delete(callId);
      }
    }
  };
}

function handleNotifySocket(urlObj, client) {
  const userId = urlObj.searchParams.get("userId");
  if (!userId) {
    client.close();
    return;
  }

  let subs = notifySubscribers.get(userId);
  if (!subs) {
    subs = new Set();
    notifySubscribers.set(userId, subs);
  }
  subs.add(client);

  const onMessageEvent = (msg) => {
    client.sendText(JSON.stringify({ type: "message", message: msg }));
  };
  const onCallEvent = (call) => {
    client.sendText(JSON.stringify({ type: "call_incoming", call }));
  };
  const onCallEndedEvent = (payload) => {
    client.sendText(JSON.stringify({ type: "call_ended", ...payload }));
  };

  realtimeEvents.on(`message:${userId}`, onMessageEvent);
  realtimeEvents.on(`call_incoming:${userId}`, onCallEvent);
  realtimeEvents.on(`call_ended:${userId}`, onCallEndedEvent);

  const pingInterval = setInterval(() => {
    if (!client.isClosed()) {
      client.sendPing();
    }
  }, 20000);

  client.onMessage = (_opcode, payload) => {
    try {
      const txt = payload.toString("utf8");
      if (txt === "ping") client.sendText("pong");
    } catch {}
  };

  client.onClose = () => {
    clearInterval(pingInterval);
    realtimeEvents.removeListener(`message:${userId}`, onMessageEvent);
    realtimeEvents.removeListener(`call_incoming:${userId}`, onCallEvent);
    realtimeEvents.removeListener(`call_ended:${userId}`, onCallEndedEvent);
    const set = notifySubscribers.get(userId);
    if (set) {
      set.delete(client);
      if (set.size === 0) notifySubscribers.delete(userId);
    }
  };
}

function notifyCallEnded(callId, status = "ended") {
  const room = callRooms.get(String(callId));
  if (!room) return;
  const ctrl = Buffer.concat([
    Buffer.from([PKT_CTRL]),
    Buffer.from(JSON.stringify({ type: status, callId: String(callId) }), "utf8"),
  ]);
  for (const peerClient of room.values()) {
    if (!peerClient.isClosed()) {
      peerClient.sendBinary(ctrl);
    }
  }
}

function attachRealtimeWebSockets(server) {
  if (!server || server._quackyWsAttached) return;
  server._quackyWsAttached = true;

  server.on("upgrade", (req, socket) => {
    try {
      const urlObj = new URL(req.url || "/", `http://${req.headers.host || "localhost"}`);
      if (urlObj.pathname === "/ws/call") {
        const client = acceptUpgrade(req, socket);
        if (client) handleCallSocket(urlObj, client);
        else socket.destroy();
      } else if (urlObj.pathname === "/ws/notify") {
        const client = acceptUpgrade(req, socket);
        if (client) handleNotifySocket(urlObj, client);
        else socket.destroy();
      } else {
        socket.destroy();
      }
    } catch {
      try {
        socket.destroy();
      } catch {}
    }
  });
}

module.exports = {
  attachRealtimeWebSockets,
  notifyCallEnded,
};
