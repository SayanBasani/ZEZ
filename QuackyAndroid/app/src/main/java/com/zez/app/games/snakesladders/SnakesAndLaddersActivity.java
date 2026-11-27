package com.zez.app.games.snakesladders;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.zez.app.R;
import com.zez.app.theme.ThemeManager;
import com.zez.app.util.ChatPreferencesHelper;

import java.util.UUID;

public class SnakesAndLaddersActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_ID = "extra_sl_game_id";
    public static final String EXTRA_INVITATION_ID = "extra_sl_invitation_id";
    public static final String EXTRA_CONVERSATION_ID = "extra_sl_conversation_id";
    public static final String EXTRA_PEER_ID = "extra_sl_peer_id";
    public static final String EXTRA_PEER_NAME = "extra_sl_peer_name";
    public static final String RESULT_PLAY_AGAIN_INVITE_ID = "result_sl_play_again_invite_id";

    private TextView tvConnStatus;
    private LinearLayout cardP1;
    private LinearLayout cardP2;
    private TextView tvP1Token;
    private TextView tvP2Token;
    private View dotP1Online;
    private View dotP2Online;
    private TextView tvP1Name;
    private TextView tvP2Name;
    private TextView tvP1Meta;
    private TextView tvP2Meta;
    private TextView tvTurnBanner;
    private TextView tvSubstatus;
    private SnakesLaddersBoardView boardView;
    private SnakesLaddersDiceView diceView;
    private Button btnRollDice;
    private ImageButton btnRules;
    private ImageButton btnSound;
    private ImageButton btnExit;
    private ImageButton btnBack;
    private ImageButton btnHistory;
    private ImageButton btnMenu;

    private LinearLayout resultOverlay;
    private TextView tvResultTitle;
    private TextView tvResultDetails;
    private Button btnPlayAgain;
    private Button btnReturnChat;
    private Button btnViewDetails;

    private String myUserId;
    private String gameId;
    private String invitationId;
    private String conversationId;
    private String peerId;
    private String peerName;

    private volatile SnakesLaddersModels.SessionState currentSession;
    private volatile int currentVersion = 0;
    private volatile boolean isSyncActive = false;
    private volatile boolean isAnimatingMove = false;
    private volatile boolean isRollInFlight = false;
    private volatile boolean isSoundEnabled = true;

    private String lastHandledActionId = null;
    private Thread syncThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snakes_and_ladders);

        myUserId = ChatPreferencesHelper.getUserId(this);
        Intent intent = getIntent();
        gameId = intent.getStringExtra(EXTRA_GAME_ID);
        invitationId = intent.getStringExtra(EXTRA_INVITATION_ID);
        conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
        peerId = intent.getStringExtra(EXTRA_PEER_ID);
        peerName = intent.getStringExtra(EXTRA_PEER_NAME);

        if (myUserId == null || ((gameId == null || gameId.trim().isEmpty())
                && (invitationId == null || invitationId.trim().isEmpty()))) {
            Toast.makeText(this, "Invalid Snakes & Ladders session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 65);
        } catch (Exception ignored) {}

        bindViews();
        setupListeners();
        initializeSession();
    }

    private void bindViews() {
        tvConnStatus = findViewById(R.id.tv_sl_conn_status);
        cardP1 = findViewById(R.id.card_sl_p1);
        cardP2 = findViewById(R.id.card_sl_p2);
        tvP1Token = findViewById(R.id.tv_sl_p1_token);
        tvP2Token = findViewById(R.id.tv_sl_p2_token);
        dotP1Online = findViewById(R.id.dot_sl_p1_online);
        dotP2Online = findViewById(R.id.dot_sl_p2_online);
        tvP1Name = findViewById(R.id.tv_sl_p1_name);
        tvP2Name = findViewById(R.id.tv_sl_p2_name);
        tvP1Meta = findViewById(R.id.tv_sl_p1_meta);
        tvP2Meta = findViewById(R.id.tv_sl_p2_meta);
        tvTurnBanner = findViewById(R.id.tv_sl_turn_banner);
        tvSubstatus = findViewById(R.id.tv_sl_substatus);
        boardView = findViewById(R.id.view_sl_board);
        diceView = findViewById(R.id.view_sl_dice);
        btnRollDice = findViewById(R.id.btn_sl_roll_dice);
        btnRules = findViewById(R.id.btn_sl_rules);
        btnSound = findViewById(R.id.btn_sl_sound);
        btnExit = findViewById(R.id.btn_sl_exit);
        btnBack = findViewById(R.id.btn_sl_back);
        btnHistory = findViewById(R.id.btn_sl_history);
        btnMenu = findViewById(R.id.btn_sl_menu);

        resultOverlay = findViewById(R.id.layout_sl_result_overlay);
        tvResultTitle = findViewById(R.id.tv_sl_result_title);
        tvResultDetails = findViewById(R.id.tv_sl_result_details);
        btnPlayAgain = findViewById(R.id.btn_sl_play_again);
        btnReturnChat = findViewById(R.id.btn_sl_return_chat);
        btnViewDetails = findViewById(R.id.btn_sl_view_details);

        btnRollDice.setEnabled(false);
        btnRollDice.setAlpha(0.48f);
        tintCircle(tvP1Token, Color.parseColor("#EF4444"));
        tintCircle(tvP2Token, Color.parseColor("#2563EB"));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> handleLeaveAttempt());
        btnExit.setOnClickListener(v -> handleLeaveAttempt());
        btnHistory.setOnClickListener(v -> openHistory());
        btnRules.setOnClickListener(v -> showRulesDialog());
        btnSound.setOnClickListener(v -> toggleSound());
        btnMenu.setOnClickListener(this::showGameMenu);
        btnRollDice.setOnClickListener(v -> performRollDice());

        btnPlayAgain.setOnClickListener(v -> triggerPlayAgain());
        btnReturnChat.setOnClickListener(v -> finish());
        btnViewDetails.setOnClickListener(v -> openHistory());
    }

    @Override
    public void onBackPressed() {
        handleLeaveAttempt();
    }

    private void handleLeaveAttempt() {
        SnakesLaddersModels.SessionState s = currentSession;
        if (s != null && "ACTIVE".equals(s.status)) {
            new AlertDialog.Builder(this)
                    .setTitle("Leave Match Screen?")
                    .setMessage("Your game stays saved on the server so you can return anytime from chat, or you can resign.")
                    .setPositiveButton("Save & Return to Chat", (d, w) -> finish())
                    .setNeutralButton("Resign Match", (d, w) -> performResign())
                    .setNegativeButton("Stay", null)
                    .show();
            return;
        }
        finish();
    }

    private void showGameMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, "Game Rules");
        menu.getMenu().add(0, 2, 1, "Game History & Stats");
        if (currentSession != null && "ACTIVE".equals(currentSession.status)) {
            menu.getMenu().add(0, 3, 2, "Resign Match");
        }
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showRulesDialog();
                return true;
            } else if (item.getItemId() == 2) {
                openHistory();
                return true;
            } else if (item.getItemId() == 3) {
                new AlertDialog.Builder(this)
                        .setTitle("Resign Game?")
                        .setMessage("Resigning ends the match and awards the win to your opponent.")
                        .setPositiveButton("Resign", (d, w) -> performResign())
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void toggleSound() {
        isSoundEnabled = !isSoundEnabled;
        btnSound.setImageResource(isSoundEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        Toast.makeText(this, isSoundEnabled ? "Game sound ON" : "Game sound Muted", Toast.LENGTH_SHORT).show();
    }

    private void openHistory() {
        startActivity(new Intent(this, SnakesLaddersHistoryActivity.class));
    }

    private void showRulesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Snakes & Ladders Rules")
                .setMessage(
                        "• Both players begin outside the board (Pos 0).\n" +
                        "• Roll any number 1–6 to enter the board immediately.\n" +
                        "• Land on the bottom of a Ladder to climb up.\n" +
                        "• Land on a Snake head to slide down to its tail.\n" +
                        "• Passing over a snake or ladder has no effect.\n" +
                        "• Roll a 6 to earn an extra roll!\n" +
                        "• Both players can occupy the same square safely.\n" +
                        "• Exact roll required to land on square 100 and win."
                )
                .setPositiveButton("Got it", null)
                .show();
    }

    private void initializeSession() {
        if (gameId != null && !gameId.trim().isEmpty()) {
            loadAndReconnectSession(gameId);
            return;
        }
        // Waiting for invitation acceptance from opponent
        tvConnStatus.setText("Waiting for challenge response");
        tvTurnBanner.setText("Challenge Sent!");
        tvSubstatus.setText("Waiting for " + (peerName != null ? peerName : "opponent") + " to accept...");
        btnRollDice.setEnabled(false);
        startInvitationWaitLoop();
    }

    private void startInvitationWaitLoop() {
        isSyncActive = true;
        syncThread = new Thread(() -> {
            while (isSyncActive && (gameId == null || gameId.trim().isEmpty())) {
                try {
                    SnakesLaddersModels.InvitationEventsResult ev =
                            SnakesLaddersApiClient.listenInvitationEventsSync(getApplicationContext(), invitationId, myUserId);
                    if (!isSyncActive) break;
                    if (ev != null && ev.invitation != null) {
                        String st = ev.invitation.status;
                        if ("ACCEPTED".equals(st) && ev.invitation.gameId != null) {
                            gameId = ev.invitation.gameId;
                            if (ev.session != null) {
                                mainHandler.post(() -> {
                                    applySessionSnapshot(ev.session, false);
                                    startLiveSessionLoop();
                                });
                            } else {
                                mainHandler.post(() -> loadAndReconnectSession(gameId));
                            }
                            return;
                        } else if ("DECLINED".equals(st) || "EXPIRED".equals(st)) {
                            final String finalSt = st;
                            mainHandler.post(() -> {
                                tvConnStatus.setText(finalSt);
                                tvTurnBanner.setText("DECLINED".equals(finalSt) ? "Challenge Declined" : "Challenge Expired");
                                tvSubstatus.setText("Return to chat to send another game challenge.");
                            });
                            return;
                        }
                    } else {
                        Thread.sleep(1500);
                    }
                } catch (InterruptedException ie) {
                    break;
                } catch (Exception e) {
                    try { Thread.sleep(1500); } catch (Exception ignored) {}
                }
            }
        }, "SLInviteWait");
        syncThread.setDaemon(true);
        syncThread.start();
    }

    private void loadAndReconnectSession(String targetGameId) {
        tvConnStatus.setText("Connecting...");
        SnakesLaddersApiClient.reconnect(this, targetGameId, myUserId, new SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>() {
            @Override
            public void onSuccess(SnakesLaddersModels.SessionState data) {
                if (data != null) {
                    applySessionSnapshot(data, false);
                }
                startLiveSessionLoop();
            }

            @Override
            public void onError(String message, int httpCode, String code) {
                tvConnStatus.setText("Disconnected");
                tvTurnBanner.setText("Unable to load match");
                tvSubstatus.setText(message);
                startLiveSessionLoop();
            }
        });
    }

    private void startLiveSessionLoop() {
        if (gameId == null) return;
        isSyncActive = true;
        if (syncThread != null && syncThread.isAlive()) return;
        syncThread = new Thread(() -> {
            while (isSyncActive) {
                try {
                    SnakesLaddersModels.SessionEventsResult ev =
                            SnakesLaddersApiClient.listenSessionEventsSync(getApplicationContext(), gameId, myUserId, currentVersion);
                    if (!isSyncActive) break;
                    if (ev != null && ev.session != null) {
                        mainHandler.post(() -> {
                            tvConnStatus.setText("Live");
                            tvConnStatus.setTextColor(Color.parseColor("#10B981"));
                            boolean shouldAnimate = ev.changed && ev.session.lastMove != null
                                    && (lastHandledActionId == null || !lastHandledActionId.equals(ev.session.lastMove.actionId));
                            applySessionSnapshot(ev.session, shouldAnimate);
                        });
                        if ("FINISHED".equals(ev.session.status) || "ABANDONED".equals(ev.session.status)) {
                            break;
                        }
                    } else {
                        mainHandler.post(() -> {
                            tvConnStatus.setText("Reconnecting...");
                            tvConnStatus.setTextColor(Color.parseColor("#F59E0B"));
                        });
                        Thread.sleep(1500);
                    }
                } catch (InterruptedException ie) {
                    break;
                } catch (Exception e) {
                    try { Thread.sleep(1500); } catch (Exception ignored) {}
                }
            }
        }, "SLSessionSync");
        syncThread.setDaemon(true);
        syncThread.start();
    }

    private void performRollDice() {
        SnakesLaddersModels.SessionState s = currentSession;
        if (s == null || isRollInFlight || isAnimatingMove) return;
        if (!"ACTIVE".equals(s.status)) return;
        if (myUserId == null || !myUserId.equals(s.currentTurnPlayerId)) {
            Toast.makeText(this, "Wait for opponent...", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRollDice.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        isRollInFlight = true;
        btnRollDice.setEnabled(false);
        btnRollDice.setAlpha(0.48f);
        btnRollDice.setText("ROLLING...");
        diceView.startRollingAnimation();
        playTone(ToneGenerator.TONE_PROP_BEEP, 60);

        final String actionId = UUID.randomUUID().toString();
        SnakesLaddersApiClient.rollDice(this, gameId, myUserId, actionId, new SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.RollResult>() {
            @Override
            public void onSuccess(SnakesLaddersModels.RollResult data) {
                isRollInFlight = false;
                if (data != null && data.session != null) {
                    boolean animate = data.move != null && (lastHandledActionId == null || !lastHandledActionId.equals(data.move.actionId));
                    applySessionSnapshot(data.session, animate);
                } else {
                    diceView.stopRollingAnimation();
                    updateControlsAndBanner(currentSession);
                }
            }

            @Override
            public void onError(String message, int httpCode, String code) {
                isRollInFlight = false;
                diceView.stopRollingAnimation();
                Toast.makeText(SnakesAndLaddersActivity.this, message, Toast.LENGTH_SHORT).show();
                updateControlsAndBanner(currentSession);
            }
        });
    }

    private void applySessionSnapshot(SnakesLaddersModels.SessionState session, boolean animateLastMove) {
        if (session == null || isFinishing() || isDestroyed()) return;
        currentSession = session;
        currentVersion = Math.max(currentVersion, session.stateVersion);
        if (session.boardConfig != null) {
            boardView.setBoardConfig(session.boardConfig);
        }
        tvConnStatus.setText("Live");
        tvConnStatus.setTextColor(Color.parseColor("#10B981"));

        SnakesLaddersModels.MoveRecord move = session.lastMove;
        if (animateLastMove && move != null && (lastHandledActionId == null || !lastHandledActionId.equals(move.actionId))) {
            lastHandledActionId = move.actionId;
            isAnimatingMove = true;
            btnRollDice.setEnabled(false);
            btnRollDice.setAlpha(0.48f);
            showMoveFeedback(move, session);

            // Roll dice with tumbling animation for both self and opponent
            diceView.rollDiceToValue(move.diceValue, 650L, () -> {
                boardView.animateMove(move, session.players, session.currentTurnPlayerId, () -> {
                    if (isFinishing() || isDestroyed()) return;
                    isAnimatingMove = false;
                    if ("LADDER".equals(move.specialMoveType)) {
                        playTone(ToneGenerator.TONE_PROP_ACK, 180);
                    } else if ("SNAKE".equals(move.specialMoveType)) {
                        playTone(ToneGenerator.TONE_PROP_NACK, 200);
                    }
                    bindPlayerCards(session);
                    updateControlsAndBanner(session);
                    if ("FINISHED".equals(session.status)) {
                        showFinishedOverlay(session);
                    }
                });
            });
        } else {
            if (move != null && lastHandledActionId == null) {
                lastHandledActionId = move.actionId;
                diceView.setValue(move.diceValue);
            }
            isAnimatingMove = false;
            boardView.setStateImmediate(session.players, session.currentTurnPlayerId);
            bindPlayerCards(session);
            updateControlsAndBanner(session);
            if ("FINISHED".equals(session.status)) {
                showFinishedOverlay(session);
            }
        }
    }

    private void showMoveFeedback(SnakesLaddersModels.MoveRecord move, SnakesLaddersModels.SessionState session) {
        boolean isMyMove = myUserId != null && myUserId.equals(move.playerId);
        String actor = isMyMove ? "You" : (move.playerName != null ? move.playerName : "Opponent");
        if (move.overshot) {
            tvTurnBanner.setText(actor + " rolled " + move.diceValue + " (need exact roll for 100!)");
            tvSubstatus.setText("Token stays at position " + move.finalPosition);
        } else if ("LADDER".equals(move.specialMoveType)) {
            tvTurnBanner.setText(isMyMove ? "You rolled " + move.diceValue + " — You climbed a ladder!"
                    : actor + " rolled " + move.diceValue + " & climbed a ladder!");
            tvSubstatus.setText("Climbed " + move.specialMoveFrom + " → " + move.specialMoveTo);
        } else if ("SNAKE".equals(move.specialMoveType)) {
            tvTurnBanner.setText(isMyMove ? "Oh no! The snake caught you!"
                    : actor + " slid down a snake!");
            tvSubstatus.setText("Slid " + move.specialMoveFrom + " → " + move.specialMoveTo);
        } else {
            tvTurnBanner.setText(actor + " rolled " + move.diceValue + ".");
            tvSubstatus.setText("Moving " + move.previousPosition + " → " + move.finalPosition);
        }
    }

    private void bindPlayerCards(SnakesLaddersModels.SessionState session) {
        if (session.players.size() < 2) return;
        SnakesLaddersModels.PlayerState p1 = session.players.get(0);
        SnakesLaddersModels.PlayerState p2 = session.players.get(1);

        boolean p1IsMe = myUserId != null && myUserId.equals(p1.userId);
        boolean p2IsMe = myUserId != null && myUserId.equals(p2.userId);

        tvP1Name.setText(p1.name + (p1IsMe ? " (You)" : ""));
        tvP2Name.setText(p2.name + (p2IsMe ? " (You)" : ""));

        String c1 = p1.tokenColor != null ? p1.tokenColor.toUpperCase() : "RED";
        String c2 = p2.tokenColor != null ? p2.tokenColor.toUpperCase() : "BLUE";
        tvP1Meta.setText(c1 + " · Pos " + p1.currentPosition);
        tvP2Meta.setText(c2 + " · Pos " + p2.currentPosition);

        tvP1Token.setText(initialFor(p1.name));
        tvP2Token.setText(initialFor(p2.name));
        tintCircle(tvP1Token, SnakesLaddersBoardView.resolveTokenColor(p1.tokenColor));
        tintCircle(tvP2Token, SnakesLaddersBoardView.resolveTokenColor(p2.tokenColor));

        dotP1Online.getBackground().setTint(Color.parseColor("ONLINE".equals(p1.connectedStatus) ? "#22C55E" : "#94A3B8"));
        dotP2Online.getBackground().setTint(Color.parseColor("ONLINE".equals(p2.connectedStatus) ? "#22C55E" : "#94A3B8"));

        boolean activeGame = "ACTIVE".equals(session.status);
        boolean p1Turn = activeGame && p1.userId.equals(session.currentTurnPlayerId);
        boolean p2Turn = activeGame && p2.userId.equals(session.currentTurnPlayerId);

        highlightCard(cardP1, p1Turn, SnakesLaddersBoardView.resolveTokenColor(p1.tokenColor));
        highlightCard(cardP2, p2Turn, SnakesLaddersBoardView.resolveTokenColor(p2.tokenColor));
    }

    private String initialFor(String name) {
        if (name == null || name.trim().isEmpty()) return "P";
        return String.valueOf(Character.toUpperCase(name.trim().charAt(0)));
    }

    private void tintCircle(View view, int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        view.setBackground(gd);
    }

    private void highlightCard(LinearLayout card, boolean active, int strokeColor) {
        GradientDrawable gd = new GradientDrawable();
        float d = getResources().getDisplayMetrics().density;
        gd.setCornerRadius(12 * d);
        gd.setColor(ThemeManager.resolveThemeColor(this, R.attr.colorThemeCardBg));
        gd.setStroke((int) ((active ? 2.5f : 1f) * d),
                active ? strokeColor : ThemeManager.resolveThemeColor(this, R.attr.colorThemeBorder));
        card.setBackground(gd);
        card.animate()
                .scaleX(active ? 1.03f : 0.98f)
                .scaleY(active ? 1.03f : 0.98f)
                .alpha(active ? 1f : 0.8f)
                .setDuration(180)
                .start();
    }

    private void updateControlsAndBanner(SnakesLaddersModels.SessionState session) {
        if (session == null) return;
        if ("FINISHED".equals(session.status)) {
            btnRollDice.setEnabled(false);
            btnRollDice.setAlpha(0.48f);
            btnRollDice.setText("GAME ENDED");
            SnakesLaddersModels.PlayerState winPlayer = null;
            for (SnakesLaddersModels.PlayerState p : session.players) {
                if (session.winnerId != null && session.winnerId.equals(p.userId)) {
                    winPlayer = p;
                    break;
                }
            }
            boolean iWon = myUserId != null && myUserId.equals(session.winnerId);
            String wName = winPlayer != null ? winPlayer.name : "Player";
            tvTurnBanner.setText(iWon ? "You Won!" : "Winner: " + wName);
            tvSubstatus.setText(session.resignedBy != null ? "Ended by resignation" : "Reached Square 100");
            return;
        }

        SnakesLaddersModels.PlayerState opponent = null;
        SnakesLaddersModels.PlayerState me = null;
        for (SnakesLaddersModels.PlayerState p : session.players) {
            if (myUserId != null && myUserId.equals(p.userId)) me = p;
            else opponent = p;
        }

        boolean myTurn = myUserId != null && myUserId.equals(session.currentTurnPlayerId);
        boolean canRoll = myTurn && !isRollInFlight && !isAnimatingMove;
        btnRollDice.setEnabled(canRoll);
        btnRollDice.setAlpha(canRoll ? 1.0f : 0.48f);
        btnRollDice.setText(myTurn ? "ROLL DICE" : "OPPONENT TURN");

        if (opponent != null && "OFFLINE".equals(opponent.connectedStatus)) {
            tvTurnBanner.setText(myTurn ? "Your turn! Roll the dice." : "Waiting for opponent...");
            tvSubstatus.setText("Your opponent disconnected. Waiting for reconnection...");
            return;
        }

        if (myTurn) {
            if (me != null && me.currentPosition == 99) {
                tvTurnBanner.setText("One more step to victory!");
                tvSubstatus.setText("Roll 1 to win! Your turn.");
            } else if (session.lastMove != null && session.lastMove.grantedExtraTurn
                    && myUserId.equals(session.lastMove.playerId)) {
                tvTurnBanner.setText("You rolled a 6. Roll again!");
                tvSubstatus.setText("Bonus turn active");
            } else {
                tvTurnBanner.setText("Your turn! Roll the dice.");
                tvSubstatus.setText("Tap ROLL DICE to advance your token");
            }
        } else {
            tvTurnBanner.setText("Waiting for opponent...");
            tvSubstatus.setText((opponent != null ? opponent.name : "Opponent") + " is rolling the dice");
        }
    }

    private void showFinishedOverlay(SnakesLaddersModels.SessionState session) {
        playTone(ToneGenerator.TONE_PROP_ACK, 300);
        SnakesLaddersModels.PlayerState winner = null;
        SnakesLaddersModels.PlayerState loser = null;
        for (SnakesLaddersModels.PlayerState p : session.players) {
            if (session.winnerId != null && session.winnerId.equals(p.userId)) winner = p;
            else loser = p;
        }

        boolean iWon = myUserId != null && myUserId.equals(session.winnerId);
        String winnerName = winner != null ? winner.name : "Player";
        tvResultTitle.setText(iWon ? "You Won! (" + winnerName + ")" : "Winner: " + winnerName);

        StringBuilder sb = new StringBuilder();
        if (winner != null) sb.append("Final position: ").append(winner.currentPosition);
        if (loser != null) sb.append("\nOpponent: ").append(loser.name).append(" (Pos ").append(loser.currentPosition).append(")");
        if (session.resignedBy != null) sb.append("\nEnded by resignation");
        tvResultDetails.setText(sb.toString());

        resultOverlay.setAlpha(0f);
        resultOverlay.setScaleX(0.92f);
        resultOverlay.setScaleY(0.92f);
        resultOverlay.setVisibility(View.VISIBLE);
        resultOverlay.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(260)
                .start();
    }

    private void performResign() {
        if (gameId == null) {
            finish();
            return;
        }
        SnakesLaddersApiClient.resignGame(this, gameId, myUserId, new SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>() {
            @Override
            public void onSuccess(SnakesLaddersModels.SessionState data) {
                if (data != null) applySessionSnapshot(data, false);
            }

            @Override
            public void onError(String message, int httpCode, String code) {
                Toast.makeText(SnakesAndLaddersActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void triggerPlayAgain() {
        String targetOppId = peerId;
        if (targetOppId == null && currentSession != null) {
            for (SnakesLaddersModels.PlayerState p : currentSession.players) {
                if (!p.userId.equals(myUserId)) {
                    targetOppId = p.userId;
                    break;
                }
            }
        }
        if (targetOppId == null) {
            finish();
            return;
        }
        btnPlayAgain.setEnabled(false);
        SnakesLaddersApiClient.createInvitation(this, myUserId, targetOppId, conversationId,
                new SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.InvitationState>() {
                    @Override
                    public void onSuccess(SnakesLaddersModels.InvitationState inv) {
                        Intent res = new Intent();
                        res.putExtra(RESULT_PLAY_AGAIN_INVITE_ID, inv.id);
                        setResult(RESULT_OK, res);
                        finish();
                    }

                    @Override
                    public void onError(String message, int httpCode, String code) {
                        btnPlayAgain.setEnabled(true);
                        Toast.makeText(SnakesAndLaddersActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void playTone(int toneType, int durationMs) {
        if (!isSoundEnabled || toneGenerator == null) return;
        try {
            toneGenerator.startTone(toneType, durationMs);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gameId != null && currentSession != null && "ACTIVE".equals(currentSession.status)) {
            SnakesLaddersApiClient.notifyDisconnectBestEffort(getApplicationContext(), gameId, myUserId);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (gameId != null) {
            loadAndReconnectSession(gameId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSyncActive = false;
        diceView.stopRollingAnimation();
        if (syncThread != null) {
            syncThread.interrupt();
            syncThread = null;
        }
        if (toneGenerator != null) {
            try { toneGenerator.release(); } catch (Exception ignored) {}
            toneGenerator = null;
        }
    }
}
