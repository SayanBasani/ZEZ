package com.zez.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatPreferencesHelper;

public class TicTacToeActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_ID = "extra_game_id";
    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_HOST_ID = "extra_host_id";
    public static final String EXTRA_HOST_NAME = "extra_host_name";
    public static final String EXTRA_GUEST_ID = "extra_guest_id";
    public static final String EXTRA_GUEST_NAME = "extra_guest_name";
    public static final String RESULT_GAME_ENDED = "result_game_ended";
    public static final String RESULT_GAME_ID = "result_game_id";
    public static final String RESULT_GAME_SUMMARY = "result_game_summary";

    private final TextView[] cells = new TextView[9];
    private LinearLayout cardPlayerX;
    private LinearLayout cardPlayerO;
    private TextView tvXName;
    private TextView tvOName;
    private TextView tvXScore;
    private TextView tvOScore;
    private TextView tvDrawsScore;
    private TextView tvRound;
    private TextView tvStatus;
    private TextView tvSubstatus;
    private Button btnRematch;
    private Button btnFinish;
    private TextView btnEndGame;
    private LinearLayout gridBoard;

    private String gameId;
    private String conversationId;
    private String myUserId;
    private String myName;
    private String extraHostId;
    private String extraHostName;
    private String extraGuestId;
    private String extraGuestName;

    private volatile ChatApiClient.GameState currentGame;
    private volatile int currentVersion = 0;
    private volatile boolean isSyncActive = false;
    private volatile boolean isActionInFlight = false;
    private boolean didEndGameHere = false;

    private Thread syncThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final String[] lastRenderedBoard = new String[]{"", "", "", "", "", "", "", "", ""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#0B1120"));
            getWindow().setNavigationBarColor(Color.parseColor("#0B1120"));
        }

        myUserId = ChatPreferencesHelper.getUserId(this);
        myName = ChatPreferencesHelper.getUserName(this);

        Intent intent = getIntent();
        gameId = intent.getStringExtra(EXTRA_GAME_ID);
        conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
        extraHostId = intent.getStringExtra(EXTRA_HOST_ID);
        extraHostName = intent.getStringExtra(EXTRA_HOST_NAME);
        extraGuestId = intent.getStringExtra(EXTRA_GUEST_ID);
        extraGuestName = intent.getStringExtra(EXTRA_GUEST_NAME);

        if (gameId == null || gameId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid game session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        sizeBoardCleanly();
        setupListeners();
        initializeSession();
    }

    private void bindViews() {
        cardPlayerX = findViewById(R.id.card_player_x);
        cardPlayerO = findViewById(R.id.card_player_o);
        tvXName = findViewById(R.id.tv_x_name);
        tvOName = findViewById(R.id.tv_o_name);
        tvXScore = findViewById(R.id.tv_x_score);
        tvOScore = findViewById(R.id.tv_o_score);
        tvDrawsScore = findViewById(R.id.tv_draws_score);
        tvRound = findViewById(R.id.tv_ttt_round);
        tvStatus = findViewById(R.id.tv_ttt_status);
        tvSubstatus = findViewById(R.id.tv_ttt_substatus);
        btnRematch = findViewById(R.id.btn_ttt_rematch);
        btnFinish = findViewById(R.id.btn_ttt_finish);
        btnEndGame = findViewById(R.id.btn_ttt_end_game);
        gridBoard = findViewById(R.id.grid_ttt_board);

        int[] ids = {
                R.id.btn_cell_0, R.id.btn_cell_1, R.id.btn_cell_2,
                R.id.btn_cell_3, R.id.btn_cell_4, R.id.btn_cell_5,
                R.id.btn_cell_6, R.id.btn_cell_7, R.id.btn_cell_8
        };
        for (int i = 0; i < 9; i++) {
            cells[i] = findViewById(ids[i]);
        }

        if (extraHostName != null && !extraHostName.isEmpty()) tvXName.setText(extraHostName);
        if (extraGuestName != null && !extraGuestName.isEmpty()) tvOName.setText(extraGuestName);
    }

    private void sizeBoardCleanly() {
        if (gridBoard == null) return;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int maxSide = Math.min(dm.widthPixels - (int) (48 * dm.density), (int) (332 * dm.density));
        if (dm.heightPixels < (int) (720 * dm.density)) {
            maxSide = Math.min(maxSide, (int) (268 * dm.density));
        }
        gridBoard.getLayoutParams().width = maxSide;
        gridBoard.getLayoutParams().height = maxSide;
        gridBoard.requestLayout();
    }

    private void setupListeners() {
        ImageButton btnBack = findViewById(R.id.btn_ttt_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        btnFinish.setOnClickListener(v -> finish());

        for (int i = 0; i < 9; i++) {
            final int index = i;
            cells[i].setOnClickListener(v -> onCellClicked(index));
        }

        btnRematch.setOnClickListener(v -> {
            if (isActionInFlight) return;
            isActionInFlight = true;
            btnRematch.setEnabled(false);
            btnRematch.setText("Starting next round...");
            ChatApiClient.rematchGame(this, gameId, myUserId, new ChatApiClient.ChatCallback<ChatApiClient.GameState>() {
                @Override
                public void onSuccess(ChatApiClient.GameState data) {
                    isActionInFlight = false;
                    btnRematch.setEnabled(true);
                    btnRematch.setText("Play Next Round");
                    if (data != null) {
                        currentVersion = Math.max(currentVersion, data.version);
                        applyGameState(data);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    isActionInFlight = false;
                    btnRematch.setEnabled(true);
                    btnRematch.setText("Play Next Round");
                    Toast.makeText(TicTacToeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnEndGame.setOnClickListener(v -> confirmEndGame());
    }

    private void initializeSession() {
        tvStatus.setText("Joining Tic Tac Toe...");
        tvSubstatus.setText("Syncing game room");

        ChatApiClient.getGame(this, gameId, new ChatApiClient.ChatCallback<ChatApiClient.GameState>() {
            @Override
            public void onSuccess(ChatApiClient.GameState data) {
                if (data == null) return;
                currentVersion = Math.max(currentVersion, data.version);
                applyGameState(data);
                if (myUserId != null && !data.guestJoined && myUserId.equals(data.guestId) && !"ended".equals(data.status)) {
                    joinExistingGame();
                } else {
                    startLiveSyncLoop();
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Attempt create if host opened room before backend finished create, or join if guest
                if (extraHostId != null && extraGuestId != null && !extraHostId.isEmpty() && !extraGuestId.isEmpty()) {
                    ChatApiClient.createGame(
                            TicTacToeActivity.this,
                            gameId,
                            conversationId,
                            extraHostId,
                            extraGuestId,
                            extraHostName,
                            extraGuestName,
                            new ChatApiClient.ChatCallback<ChatApiClient.GameState>() {
                                @Override
                                public void onSuccess(ChatApiClient.GameState created) {
                                    if (created != null) {
                                        currentVersion = Math.max(currentVersion, created.version);
                                        applyGameState(created);
                                        if (myUserId != null && myUserId.equals(created.guestId) && !created.guestJoined) {
                                            joinExistingGame();
                                        } else {
                                            startLiveSyncLoop();
                                        }
                                    }
                                }

                                @Override
                                public void onError(String createErr) {
                                    tvStatus.setText("Game server unavailable");
                                    tvSubstatus.setText(createErr != null ? createErr : "Please deploy backend game update");
                                }
                            }
                    );
                } else {
                    joinExistingGame();
                }
            }
        });
    }

    private void joinExistingGame() {
        if (myUserId == null) return;
        ChatApiClient.joinGame(this, gameId, myUserId, new ChatApiClient.ChatCallback<ChatApiClient.GameState>() {
            @Override
            public void onSuccess(ChatApiClient.GameState data) {
                if (data != null) {
                    currentVersion = Math.max(currentVersion, data.version);
                    applyGameState(data);
                }
                startLiveSyncLoop();
            }

            @Override
            public void onError(String errorMessage) {
                tvStatus.setText("Could not join match");
                tvSubstatus.setText(errorMessage != null ? errorMessage : "Check connection or server update");
                startLiveSyncLoop();
            }
        });
    }

    private void startLiveSyncLoop() {
        if (syncThread != null && syncThread.isAlive()) return;
        isSyncActive = true;
        syncThread = new Thread(() -> {
            while (isSyncActive) {
                try {
                    ChatApiClient.GameEventsResult ev =
                            ChatApiClient.listenGameEventsSync(getApplicationContext(), gameId, currentVersion);
                    if (!isSyncActive) break;
                    if (ev != null && ev.game != null) {
                        if (ev.game.version != currentVersion || ev.changed) {
                            currentVersion = Math.max(currentVersion, ev.game.version);
                            mainHandler.post(() -> applyGameState(ev.game));
                        }
                        if ("ended".equalsIgnoreCase(ev.game.status)) {
                            break;
                        }
                    } else if (ev == null) {
                        Thread.sleep(1500);
                    }
                } catch (InterruptedException ie) {
                    break;
                } catch (Exception e) {
                    try { Thread.sleep(1500); } catch (Exception ignored) {}
                }
            }
        }, "TttLiveSync");
        syncThread.setDaemon(true);
        syncThread.start();
    }

    private void onCellClicked(int index) {
        ChatApiClient.GameState g = currentGame;
        if (g == null || isActionInFlight || myUserId == null) return;

        String st = g.status != null ? g.status : "waiting";
        if ("ended".equals(st) || "won".equals(st) || "draw".equals(st)) return;

        if (g.board[index] != null && !g.board[index].isEmpty()) return;

        boolean isSelfPlay = g.hostId != null && g.hostId.equals(g.guestId);
        if (!isSelfPlay && !myUserId.equals(g.currentTurn)) {
            Toast.makeText(this, "Wait for opponent's move", Toast.LENGTH_SHORT).show();
            return;
        }

        cells[index].performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        isActionInFlight = true;

        ChatApiClient.makeGameMove(this, gameId, myUserId, index, new ChatApiClient.ChatCallback<ChatApiClient.GameState>() {
            @Override
            public void onSuccess(ChatApiClient.GameState data) {
                isActionInFlight = false;
                if (data != null) {
                    currentVersion = Math.max(currentVersion, data.version);
                    applyGameState(data);
                }
            }

            @Override
            public void onError(String errorMessage) {
                isActionInFlight = false;
                Toast.makeText(TicTacToeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyGameState(ChatApiClient.GameState g) {
        if (g == null || isFinishing() || isDestroyed()) return;
        currentGame = g;

        String hostDisplay = (g.hostName != null && !g.hostName.isEmpty()) ? g.hostName : "Player X";
        String guestDisplay = (g.guestName != null && !g.guestName.isEmpty()) ? g.guestName : "Player O";
        if (myUserId != null && myUserId.equals(g.hostId) && !hostDisplay.endsWith(" (You)")) {
            hostDisplay = hostDisplay + " (You)";
        } else if (myUserId != null && myUserId.equals(g.guestId) && !guestDisplay.endsWith(" (You)")) {
            guestDisplay = guestDisplay + " (You)";
        }

        tvXName.setText(hostDisplay);
        tvOName.setText(guestDisplay);
        tvXScore.setText(String.valueOf(g.hostScore));
        tvOScore.setText(String.valueOf(g.guestScore));
        tvDrawsScore.setText("Draws\n" + g.drawsCount);
        tvRound.setText("Round " + g.roundNumber);

        boolean xActive = "playing".equals(g.status) || "waiting".equals(g.status);
        boolean isXTurn = xActive && g.hostId != null && g.hostId.equals(g.currentTurn);
        boolean isOTurn = xActive && g.guestId != null && g.guestId.equals(g.currentTurn) && !isXTurn;

        stylePlayerCard(cardPlayerX, isXTurn, Color.parseColor("#38BDF8"));
        stylePlayerCard(cardPlayerO, isOTurn, Color.parseColor("#FB7185"));

        boolean[] isWinningCell = new boolean[9];
        if (g.winningLine != null) {
            for (int idx : g.winningLine) {
                if (idx >= 0 && idx < 9) isWinningCell[idx] = true;
            }
        }

        boolean isEnded = "ended".equals(g.status);
        for (int i = 0; i < 9; i++) {
            String val = g.board[i] != null ? g.board[i] : "";
            String prev = lastRenderedBoard[i];
            cells[i].setText(val);
            if ("X".equals(val)) {
                cells[i].setTextColor(Color.parseColor("#38BDF8"));
            } else if ("O".equals(val)) {
                cells[i].setTextColor(Color.parseColor("#FB7185"));
            }

            if (!val.isEmpty() && !val.equals(prev)) {
                cells[i].setScaleX(0.45f);
                cells[i].setScaleY(0.45f);
                cells[i].setAlpha(0.3f);
                cells[i].animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(1.6f))
                        .setDuration(240)
                        .start();
            }
            lastRenderedBoard[i] = val;

            styleBoardCell(cells[i], isWinningCell[i], !isEnded);
            if (isWinningCell[i]) {
                cells[i].animate().scaleX(1.06f).scaleY(1.06f).setDuration(220).start();
            } else if (val.isEmpty()) {
                cells[i].setScaleX(1f);
                cells[i].setScaleY(1f);
                cells[i].setAlpha(1f);
            }
        }

        boolean amIHost = myUserId != null && myUserId.equals(g.hostId);
        boolean isSelfPlay = g.hostId != null && g.hostId.equals(g.guestId);
        boolean isMyTurn = isSelfPlay || (myUserId != null && myUserId.equals(g.currentTurn));
        String mySymbol = amIHost ? "X" : "O";

        if ("waiting".equals(g.status)) {
            if (amIHost) {
                tvStatus.setText(isMyTurn ? "Your Turn (X) • Waiting for " + g.guestName : "Waiting for friend...");
                tvSubstatus.setText("You can make your first move now or wait for them to tap Join");
            } else {
                tvStatus.setText("Joining match...");
                tvSubstatus.setText("Syncing live board state");
            }
            btnRematch.setVisibility(View.GONE);
        } else if ("playing".equals(g.status)) {
            if (isSelfPlay) {
                int moves = 0;
                for (String c : g.board) if (!c.isEmpty()) moves++;
                tvStatus.setText((moves % 2 == 0 ? "X" : "O") + "'s Turn");
                tvSubstatus.setText("Solo test / pass-and-play mode");
            } else if (isMyTurn) {
                tvStatus.setText("Your Turn (" + mySymbol + ")");
                tvSubstatus.setText("Tap an empty square to play");
            } else {
                String opp = amIHost ? g.guestName : g.hostName;
                tvStatus.setText(opp + "'s Turn");
                tvSubstatus.setText("Waiting for opponent's move...");
            }
            btnRematch.setVisibility(View.GONE);
        } else if ("won".equals(g.status)) {
            boolean iWon = myUserId != null && myUserId.equals(g.winnerId);
            String winnerName = (g.winnerId != null && g.winnerId.equals(g.hostId)) ? g.hostName : g.guestName;
            tvStatus.setText(iWon ? "You Won Round " + g.roundNumber + "!" : winnerName + " Won Round " + g.roundNumber);
            tvSubstatus.setText("Score: " + g.hostName + " " + g.hostScore + " - " + g.guestScore + " " + g.guestName);
            btnRematch.setVisibility(View.VISIBLE);
        } else if ("draw".equals(g.status)) {
            tvStatus.setText("Round " + g.roundNumber + " is a Draw!");
            tvSubstatus.setText("Tap Play Next Round to break the tie");
            btnRematch.setVisibility(View.VISIBLE);
        } else if ("ended".equals(g.status)) {
            tvStatus.setText("Match Ended");
            tvSubstatus.setText("Final: " + g.hostName + " " + g.hostScore + " - " + g.guestScore + " " + g.guestName + " (" + g.drawsCount + " Draws)");
            btnRematch.setVisibility(View.GONE);
            btnEndGame.setVisibility(View.GONE);
            btnFinish.setText("Return to Chat");
        }
    }

    private void stylePlayerCard(LinearLayout card, boolean highlighted, int activeStrokeColor) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#1A1F2E"));
        gd.setCornerRadius(16 * getResources().getDisplayMetrics().density);
        int strokePx = (int) ((highlighted ? 2.5f : 1.2f) * getResources().getDisplayMetrics().density);
        gd.setStroke(strokePx, highlighted ? activeStrokeColor : Color.parseColor("#2E364D"));
        card.setBackground(gd);
        card.animate()
                .scaleX(highlighted ? 1.03f : 0.98f)
                .scaleY(highlighted ? 1.03f : 0.98f)
                .alpha(highlighted ? 1f : 0.78f)
                .setDuration(180)
                .start();
    }

    private void styleBoardCell(TextView cell, boolean isWinning, boolean enabled) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(16 * getResources().getDisplayMetrics().density);
        if (isWinning) {
            gd.setColor(Color.parseColor("#1E3A2F"));
            gd.setStroke((int) (2.5f * getResources().getDisplayMetrics().density), Color.parseColor("#10B981"));
        } else {
            gd.setColor(Color.parseColor("#161D2E"));
            gd.setStroke((int) (1.5f * getResources().getDisplayMetrics().density), Color.parseColor("#2E364D"));
        }
        cell.setBackground(gd);
        cell.setEnabled(enabled);
    }

    private void confirmEndGame() {
        ChatApiClient.GameState g = currentGame;
        if (g != null && "ended".equals(g.status)) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("End Game?")
                .setMessage("This will close the live match for both players and post the final score in chat.")
                .setPositiveButton("End Match", (d, w) -> performEndGame())
                .setNegativeButton("Keep Playing", null)
                .show();
    }

    private void performEndGame() {
        btnEndGame.setEnabled(false);
        ChatApiClient.endGame(this, gameId, myUserId, new ChatApiClient.ChatCallback<ChatApiClient.GameState>() {
            @Override
            public void onSuccess(ChatApiClient.GameState data) {
                didEndGameHere = true;
                String summary = buildSummary(data != null ? data : currentGame);
                Intent res = new Intent();
                res.putExtra(RESULT_GAME_ENDED, true);
                res.putExtra(RESULT_GAME_ID, gameId);
                res.putExtra(RESULT_GAME_SUMMARY, summary);
                setResult(RESULT_OK, res);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                didEndGameHere = true;
                Intent res = new Intent();
                res.putExtra(RESULT_GAME_ENDED, true);
                res.putExtra(RESULT_GAME_ID, gameId);
                res.putExtra(RESULT_GAME_SUMMARY, buildSummary(currentGame));
                setResult(RESULT_OK, res);
                finish();
            }
        });
    }

    private String buildSummary(ChatApiClient.GameState g) {
        if (g == null) return "Tic Tac Toe match ended";
        return "Tic Tac Toe Final Score: " + g.hostName + " " + g.hostScore + " - " + g.guestScore + " " + g.guestName
                + " (" + g.drawsCount + " Draws)";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSyncActive = false;
        if (syncThread != null) {
            syncThread.interrupt();
            syncThread = null;
        }
    }
}
