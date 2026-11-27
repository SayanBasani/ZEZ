package com.zez.app.games.snakesladders;

import java.util.ArrayList;
import java.util.List;

public final class SnakesLaddersModels {

    private SnakesLaddersModels() {}

    public static class PlayerState {
        public String userId;
        public String name;
        public String avatarUrl;
        public int seatIndex;
        public String tokenColor;
        public int currentPosition;
        public int turnCount;
        public int consecutiveSixes;
        public String connectedStatus;
    }

    public static class MoveRecord {
        public String actionId;
        public String playerId;
        public String playerName;
        public int diceValue;
        public int previousPosition;
        public int normalDestination;
        public int finalPosition;
        public boolean overshot;
        public String specialMoveType; // NONE, SNAKE, LADDER
        public Integer specialMoveFrom;
        public Integer specialMoveTo;
        public boolean grantedExtraTurn;
        public boolean isWin;
        public int stateVersion;
    }

    public static class SessionState {
        public String id;
        public String conversationId;
        public String invitationId;
        public String gameType;
        public String status; // WAITING, ACTIVE, FINISHED, ABANDONED
        public String boardConfigurationVersion;
        public SnakesLaddersBoardConfig boardConfig = SnakesLaddersBoardConfig.defaultConfig();
        public final List<PlayerState> players = new ArrayList<>();
        public int currentTurnIndex;
        public String currentTurnPlayerId;
        public String winnerId;
        public String resignedBy;
        public int stateVersion = 1;
        public MoveRecord lastMove;
    }

    public static class InvitationState {
        public String id;
        public String senderId;
        public String receiverId;
        public String senderName;
        public String receiverName;
        public String conversationId;
        public String status; // PENDING, ACCEPTED, DECLINED, EXPIRED, CANCELLED
        public String gameId;
        public String expiresAt;
    }

    public static class InvitationResult {
        public InvitationState invitation;
        public SessionState session;
        public boolean idempotent;
    }

    public static class RollResult {
        public boolean idempotentReplay;
        public MoveRecord move;
        public SessionState session;
    }

    public static class SessionEventsResult {
        public boolean changed;
        public boolean timeout;
        public String event;
        public MoveRecord move;
        public SessionState session;
    }

    public static class InvitationEventsResult {
        public boolean changed;
        public boolean timeout;
        public String event;
        public InvitationState invitation;
        public SessionState session;
    }

    public static class HistoryMatch {
        public String gameId;
        public String opponentId;
        public String opponentName;
        public String opponentAvatar;
        public String winnerId;
        public String winnerName;
        public boolean didWin;
        public String resignedBy;
        public int myFinalPosition;
        public int opponentFinalPosition;
        public long durationSeconds;
        public String createdAt;
        public String finishedAt;
    }

    public static class HistoryResponse {
        public int totalGames;
        public int totalWins;
        public int winRatePercent;
        public long totalPlaySeconds;
        public final List<HistoryMatch> matches = new ArrayList<>();
    }
}
