package com.zez.app.games.snakesladders;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight square 10x10 Snakes & Ladders board renderer with smooth path stepping,
 * ladder climb, and snake slide animations.
 */
public class SnakesLaddersBoardView extends View {

    private SnakesLaddersBoardConfig boardConfig = SnakesLaddersBoardConfig.defaultConfig();
    private final List<SnakesLaddersModels.PlayerState> players = new ArrayList<>();
    private final Map<String, PointF> animatedOverrides = new HashMap<>();
    private String activeTurnPlayerId = null;

    private final Paint lightTilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blueTilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint specialBadgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outerFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint numFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint numStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint ladderRailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ladderShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ladderRungPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint snakeShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint snakeBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint snakeSpotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint snakeEyeWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint snakePupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint tokenHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tokenShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tokenFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tokenRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tokenTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF boardRect = new RectF();
    private Animator currentAnimator;

    public SnakesLaddersBoardView(Context context) {
        super(context);
        init();
    }

    public SnakesLaddersBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnakesLaddersBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        blueTilePaint.setColor(Color.parseColor("#1E40AF"));
        lightTilePaint.setColor(Color.parseColor("#FEF9C3"));

        gridBorderPaint.setStyle(Paint.Style.STROKE);
        gridBorderPaint.setColor(Color.parseColor("#2A0F2442"));
        gridBorderPaint.setStrokeWidth(1.5f);

        outerFramePaint.setStyle(Paint.Style.STROKE);
        outerFramePaint.setColor(Color.parseColor("#E2E8F0"));
        outerFramePaint.setStrokeWidth(6f);

        numFillPaint.setColor(Color.WHITE);
        numFillPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        numFillPaint.setTextAlign(Paint.Align.CENTER);

        numStrokePaint.setColor(Color.parseColor("#0F172A"));
        numStrokePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        numStrokePaint.setStyle(Paint.Style.STROKE);
        numStrokePaint.setTextAlign(Paint.Align.CENTER);

        subLabelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        subLabelPaint.setTextAlign(Paint.Align.CENTER);

        ladderShadowPaint.setStyle(Paint.Style.STROKE);
        ladderShadowPaint.setStrokeCap(Paint.Cap.ROUND);
        ladderShadowPaint.setColor(Color.parseColor("#55000000"));

        ladderRailPaint.setStyle(Paint.Style.STROKE);
        ladderRailPaint.setStrokeCap(Paint.Cap.ROUND);
        ladderRailPaint.setColor(Color.parseColor("#D99B3B"));

        ladderRungPaint.setStyle(Paint.Style.STROKE);
        ladderRungPaint.setStrokeCap(Paint.Cap.ROUND);
        ladderRungPaint.setColor(Color.parseColor("#F5C361"));

        snakeShadowPaint.setStyle(Paint.Style.STROKE);
        snakeShadowPaint.setStrokeCap(Paint.Cap.ROUND);
        snakeShadowPaint.setStrokeJoin(Paint.Join.ROUND);
        snakeShadowPaint.setColor(Color.parseColor("#44000000"));

        snakeBodyPaint.setStyle(Paint.Style.STROKE);
        snakeBodyPaint.setStrokeCap(Paint.Cap.ROUND);
        snakeBodyPaint.setStrokeJoin(Paint.Join.ROUND);
        snakeBodyPaint.setColor(Color.parseColor("#EF2D3D"));

        snakeSpotPaint.setStyle(Paint.Style.FILL);
        snakeSpotPaint.setColor(Color.parseColor("#991B1B"));

        snakeEyeWhitePaint.setStyle(Paint.Style.FILL);
        snakeEyeWhitePaint.setColor(Color.WHITE);

        snakePupilPaint.setStyle(Paint.Style.FILL);
        snakePupilPaint.setColor(Color.parseColor("#0F172A"));

        tokenShadowPaint.setStyle(Paint.Style.FILL);
        tokenShadowPaint.setColor(Color.parseColor("#55000000"));

        tokenRimPaint.setStyle(Paint.Style.STROKE);
        tokenRimPaint.setColor(Color.WHITE);

        tokenTextPaint.setColor(Color.WHITE);
        tokenTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        tokenTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int size = (h > 0) ? Math.min(w, h) : w;
        if (size <= 0) size = w;
        setMeasuredDimension(size, size);
    }

    public void setBoardConfig(SnakesLaddersBoardConfig config) {
        this.boardConfig = config != null ? config : SnakesLaddersBoardConfig.defaultConfig();
        invalidate();
    }

    public void setStateImmediate(List<SnakesLaddersModels.PlayerState> newPlayers, String activePlayerId) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
            currentAnimator = null;
        }
        animatedOverrides.clear();
        players.clear();
        if (newPlayers != null) {
            for (SnakesLaddersModels.PlayerState p : newPlayers) {
                SnakesLaddersModels.PlayerState copy = new SnakesLaddersModels.PlayerState();
                copy.userId = p.userId;
                copy.name = p.name;
                copy.seatIndex = p.seatIndex;
                copy.tokenColor = p.tokenColor;
                copy.currentPosition = p.currentPosition;
                copy.connectedStatus = p.connectedStatus;
                players.add(copy);
            }
        }
        this.activeTurnPlayerId = activePlayerId;
        invalidate();
    }

    /**
     * Animates step-by-step path traversal followed smoothly by ladder climb or snake slide.
     */
    public void animateMove(SnakesLaddersModels.MoveRecord move,
                            List<SnakesLaddersModels.PlayerState> targetPlayers,
                            String nextTurnPlayerId,
                            Runnable onComplete) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
            currentAnimator = null;
        }

        if (move == null || move.overshot || move.previousPosition == move.finalPosition
                || !ValueAnimator.areAnimatorsEnabled() || getWidth() <= 0) {
            setStateImmediate(targetPlayers, nextTurnPlayerId);
            if (onComplete != null) onComplete.run();
            return;
        }

        // Preserve non-moving opponents at their current targets cleanly while animating mover
        players.clear();
        for (SnakesLaddersModels.PlayerState p : targetPlayers) {
            SnakesLaddersModels.PlayerState copy = new SnakesLaddersModels.PlayerState();
            copy.userId = p.userId;
            copy.name = p.name;
            copy.seatIndex = p.seatIndex;
            copy.tokenColor = p.tokenColor;
            copy.currentPosition = p.userId.equals(move.playerId) ? move.previousPosition : p.currentPosition;
            copy.connectedStatus = p.connectedStatus;
            players.add(copy);
        }
        this.activeTurnPlayerId = nextTurnPlayerId;

        List<Integer> stepSquares = SnakesLaddersCoordinates.buildStepPath(move.previousPosition, move.normalDestination);
        List<PointF> stepWaypoints = new ArrayList<>();
        stepWaypoints.add(getPixelCenterForSquare(move.previousPosition));
        for (int sq : stepSquares) {
            stepWaypoints.add(getPixelCenterForSquare(sq));
        }

        boolean hasSpecial = "LADDER".equals(move.specialMoveType) || "SNAKE".equals(move.specialMoveType);
        Path specialPath = null;
        if (hasSpecial) {
            PointF spStart = getPixelCenterForSquare(move.normalDestination);
            PointF spEnd = getPixelCenterForSquare(move.finalPosition);
            if ("SNAKE".equals(move.specialMoveType)) {
                specialPath = buildSnakePath(spStart, spEnd, getWidth() / 10f);
            } else {
                specialPath = new Path();
                specialPath.moveTo(spStart.x, spStart.y);
                specialPath.lineTo(spEnd.x, spEnd.y);
            }
        }

        final Path finalSpecialPath = specialPath;
        final PathMeasure specialMeasure = specialPath != null ? new PathMeasure(specialPath, false) : null;

        final int numSteps = Math.max(1, stepWaypoints.size() - 1);
        final long stepDuration = numSteps * 220L;
        final long pauseDuration = hasSpecial ? 260L : 0L;
        final long specialDuration = hasSpecial ? 650L : 0L;
        final long totalDuration = stepDuration + pauseDuration + specialDuration;

        final float stepPhaseEnd = (float) stepDuration / totalDuration;
        final float pausePhaseEnd = (float) (stepDuration + pauseDuration) / totalDuration;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(Math.max(300L, totalDuration));
        animator.setInterpolator(new android.view.animation.LinearInterpolator());

        animator.addUpdateListener(anim -> {
            float t = (float) anim.getAnimatedValue();
            PointF pt = new PointF();

            if (t <= stepPhaseEnd) {
                // Step-by-step hopping square by square (e.g. 10 -> 11 -> 12 -> 13 -> 14 -> 15)
                float stepFraction = stepPhaseEnd > 0 ? (t / stepPhaseEnd) : 1f;
                float scaled = stepFraction * numSteps;
                int stepIdx = Math.min(numSteps - 1, (int) scaled);
                float stepFrac = scaled - stepIdx;

                PointF a = stepWaypoints.get(stepIdx);
                PointF b = stepWaypoints.get(stepIdx + 1);

                // Distinct parabolic hop arc for each individual square jump
                float hopHeight = Math.max(16f, getWidth() * 0.055f);
                float arc = (float) (Math.sin(stepFrac * Math.PI) * hopHeight);

                pt.x = a.x + (b.x - a.x) * stepFrac;
                pt.y = a.y + (b.y - a.y) * stepFrac - arc;
            } else if (t <= pausePhaseEnd) {
                // Settle on the square where the snake head or ladder bottom is
                PointF dest = getPixelCenterForSquare(move.normalDestination);
                pt.x = dest.x;
                pt.y = dest.y;
            } else {
                // Smoothly slide along snake or climb up ladder
                float specialFrac = (pausePhaseEnd < 1f) ? (t - pausePhaseEnd) / (1f - pausePhaseEnd) : 1f;
                float eased = (float) (0.5f - 0.5f * Math.cos(specialFrac * Math.PI));
                float[] pos = new float[2];
                if (specialMeasure != null) {
                    specialMeasure.getPosTan(specialMeasure.getLength() * Math.min(1f, Math.max(0f, eased)), pos, null);
                    pt.x = pos[0];
                    pt.y = pos[1];
                } else {
                    PointF end = getPixelCenterForSquare(move.finalPosition);
                    pt.x = end.x;
                    pt.y = end.y;
                }
            }

            animatedOverrides.put(move.playerId, pt);
            invalidate();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled) {
                    setStateImmediate(targetPlayers, nextTurnPlayerId);
                    if (onComplete != null) onComplete.run();
                }
            }
        });

        currentAnimator = animator;
        animator.start();
    }

    private PointF getPixelCenterForSquare(int square) {
        int rows = boardConfig.rows;
        int cols = boardConfig.columns;
        float pad = Math.max(4f, getWidth() * 0.012f);
        float side = Math.max(10f, getWidth() - pad * 2f);
        if (square <= 0) {
            return new PointF(pad + (side / cols) * 0.35f, pad + side - (side / rows) * 0.25f);
        }
        float nx = SnakesLaddersCoordinates.normalizedCenterX(square, rows, cols);
        float ny = SnakesLaddersCoordinates.normalizedCenterY(square, rows, cols);
        return new PointF(pad + nx * side, pad + ny * side);
    }

    private Path buildSnakePath(PointF head, PointF tail, float cell) {
        Path p = new Path();
        p.moveTo(head.x, head.y);
        float dx = tail.x - head.x;
        float dy = tail.y - head.y;
        float dist = (float) Math.hypot(dx, dy);
        if (dist < 1f) {
            p.lineTo(tail.x, tail.y);
            return p;
        }
        float nx = -dy / dist;
        float ny = dx / dist;
        float wave = Math.max(cell * 0.52f, dist * 0.16f);

        float cx1 = head.x + dx * 0.33f + nx * wave;
        float cy1 = head.y + dy * 0.33f + ny * wave;
        float midX = head.x + dx * 0.50f;
        float midY = head.y + dy * 0.50f;
        float cx2 = head.x + dx * 0.67f - nx * wave;
        float cy2 = head.y + dy * 0.67f - ny * wave;

        p.quadTo(cx1, cy1, midX, midY);
        p.quadTo(cx2, cy2, tail.x, tail.y);
        return p;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float pad = Math.max(4f, w * 0.012f);
        float side = w - pad * 2f;
        boardRect.set(pad, pad, pad + side, pad + side);

        int rows = boardConfig.rows;
        int cols = boardConfig.columns;
        float cellW = side / cols;
        float cellH = side / rows;

        numFillPaint.setTextSize(cellH * 0.36f);
        numStrokePaint.setTextSize(cellH * 0.36f);
        numStrokePaint.setStrokeWidth(Math.max(2.2f, cellH * 0.055f));
        subLabelPaint.setTextSize(cellH * 0.20f);

        // 1. Draw 10x10 checkerboard tiles and numbers 1..100
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                float left = pad + col * cellW;
                float top = pad + row * cellH;
                boolean isBlue = ((row + col) % 2) == 0;
                canvas.drawRect(left, top, left + cellW, top + cellH, isBlue ? blueTilePaint : lightTilePaint);

                int sq = SnakesLaddersCoordinates.gridToSquare(row, col, rows, cols);

                if (boardConfig.isLadderStart(sq)) {
                    specialBadgePaint.setColor(Color.parseColor("#3310B981"));
                    canvas.drawRect(left + 2f, top + 2f, left + cellW - 2f, top + cellH - 2f, specialBadgePaint);
                } else if (boardConfig.isSnakeHead(sq)) {
                    specialBadgePaint.setColor(Color.parseColor("#33EF4444"));
                    canvas.drawRect(left + 2f, top + 2f, left + cellW - 2f, top + cellH - 2f, specialBadgePaint);
                } else if (sq == 100) {
                    specialBadgePaint.setColor(Color.parseColor("#44F59E0B"));
                    canvas.drawRect(left + 2f, top + 2f, left + cellW - 2f, top + cellH - 2f, specialBadgePaint);
                }

                canvas.drawRect(left, top, left + cellW, top + cellH, gridBorderPaint);

                float cx = left + cellW * 0.5f;
                float baseline = top + cellH * 0.56f;
                String label = String.valueOf(sq);
                canvas.drawText(label, cx, baseline, numStrokePaint);
                canvas.drawText(label, cx, baseline, numFillPaint);

                if (sq == 1) {
                    subLabelPaint.setColor(Color.parseColor("#0F172A"));
                    canvas.drawText("START", cx, top + cellH * 0.86f, subLabelPaint);
                } else if (sq == 100) {
                    subLabelPaint.setColor(Color.WHITE);
                    canvas.drawText("WIN", cx, top + cellH * 0.86f, subLabelPaint);
                }
            }
        }

        canvas.drawRect(boardRect, outerFramePaint);

        // 2. Draw Ladders
        drawLadders(canvas, cellW);

        // 3. Draw Snakes
        drawSnakes(canvas, cellW);

        // 4. Draw Player Tokens cleanly (supporting shared squares & animated movement)
        drawTokens(canvas, cellW);
    }

    private void drawLadders(Canvas canvas, float cell) {
        float railHalf = cell * 0.18f;
        float railWidth = Math.max(3f, cell * 0.08f);
        float rungWidth = Math.max(2.5f, cell * 0.065f);
        ladderShadowPaint.setStrokeWidth(railWidth + 2f);
        ladderRailPaint.setStrokeWidth(railWidth);
        ladderRungPaint.setStrokeWidth(rungWidth);

        for (SnakesLaddersBoardConfig.LadderDef ladder : boardConfig.ladders) {
            PointF start = getPixelCenterForSquare(ladder.start);
            PointF end = getPixelCenterForSquare(ladder.end);
            float dx = end.x - start.x;
            float dy = end.y - start.y;
            float len = (float) Math.hypot(dx, dy);
            if (len < 1f) continue;
            float nx = (-dy / len) * railHalf;
            float ny = (dx / len) * railHalf;

            // Shadow rails
            canvas.drawLine(start.x + nx + 2f, start.y + ny + 3f, end.x + nx + 2f, end.y + ny + 3f, ladderShadowPaint);
            canvas.drawLine(start.x - nx + 2f, start.y - ny + 3f, end.x - nx + 2f, end.y - ny + 3f, ladderShadowPaint);

            // Rungs
            int rungs = Math.max(3, Math.round(len / (cell * 0.42f)));
            for (int r = 1; r < rungs; r++) {
                float t = (float) r / rungs;
                float mx = start.x + dx * t;
                float my = start.y + dy * t;
                canvas.drawLine(mx - nx, my - ny, mx + nx, my + ny, ladderRungPaint);
            }

            // Side rails
            canvas.drawLine(start.x + nx, start.y + ny, end.x + nx, end.y + ny, ladderRailPaint);
            canvas.drawLine(start.x - nx, start.y - ny, end.x - nx, end.y - ny, ladderRailPaint);
        }
    }

    private void drawSnakes(Canvas canvas, float cell) {
        float strokeW = Math.max(5f, cell * 0.22f);
        snakeShadowPaint.setStrokeWidth(strokeW + 3f);
        snakeBodyPaint.setStrokeWidth(strokeW);

        for (SnakesLaddersBoardConfig.SnakeDef snake : boardConfig.snakes) {
            PointF head = getPixelCenterForSquare(snake.head);
            PointF tail = getPixelCenterForSquare(snake.tail);
            Path path = buildSnakePath(head, tail, cell);

            canvas.save();
            canvas.translate(2f, 4f);
            canvas.drawPath(path, snakeShadowPaint);
            canvas.restore();

            canvas.drawPath(path, snakeBodyPaint);

            // Decorative spine spots along snake path
            PathMeasure pm = new PathMeasure(path, false);
            float totalLen = pm.getLength();
            float[] pt = new float[2];
            int spots = Math.max(3, Math.round(totalLen / (cell * 0.45f)));
            for (int i = 1; i < spots; i++) {
                pm.getPosTan(totalLen * ((float) i / spots), pt, null);
                canvas.drawCircle(pt[0], pt[1], strokeW * 0.24f, snakeSpotPaint);
            }

            // Friendly snake head & eyes
            float headRadius = strokeW * 0.85f;
            canvas.drawCircle(head.x, head.y, headRadius, snakeBodyPaint.getColor() != 0 ? createFillCirclePaint(snakeBodyPaint.getColor()) : snakeSpotPaint);

            float eyeOffset = headRadius * 0.42f;
            float eyeR = headRadius * 0.34f;
            canvas.drawCircle(head.x - eyeOffset, head.y - eyeOffset * 0.2f, eyeR, snakeEyeWhitePaint);
            canvas.drawCircle(head.x + eyeOffset, head.y - eyeOffset * 0.2f, eyeR, snakeEyeWhitePaint);
            canvas.drawCircle(head.x - eyeOffset, head.y - eyeOffset * 0.2f, eyeR * 0.52f, snakePupilPaint);
            canvas.drawCircle(head.x + eyeOffset, head.y - eyeOffset * 0.2f, eyeR * 0.52f, snakePupilPaint);
        }
    }

    private Paint createFillCirclePaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        return p;
    }

    private void drawTokens(Canvas canvas, float cell) {
        if (players.isEmpty()) return;

        // Group static players by position to apply clean horizontal/radial offsets on shared squares
        Map<Integer, List<SnakesLaddersModels.PlayerState>> bySquare = new HashMap<>();
        for (SnakesLaddersModels.PlayerState p : players) {
            if (animatedOverrides.containsKey(p.userId)) continue;
            bySquare.computeIfAbsent(p.currentPosition, k -> new ArrayList<>()).add(p);
        }

        float baseRadius = Math.max(10f, cell * 0.32f);
        tokenRimPaint.setStrokeWidth(Math.max(2.5f, cell * 0.06f));
        tokenTextPaint.setTextSize(baseRadius * 1.15f);

        for (SnakesLaddersModels.PlayerState p : players) {
            PointF center = animatedOverrides.get(p.userId);
            float radius = baseRadius;
            if (center == null) {
                center = getPixelCenterForSquare(p.currentPosition);
                List<SnakesLaddersModels.PlayerState> group = bySquare.get(p.currentPosition);
                if (group != null && group.size() > 1) {
                    int idx = group.indexOf(p);
                    float spread = cell * 0.20f;
                    float angle = (float) ((2 * Math.PI * idx) / group.size());
                    center = new PointF(center.x + (float) Math.cos(angle) * spread,
                            center.y + (float) Math.sin(angle) * spread);
                    radius = baseRadius * 0.85f;
                }
                float margin = radius + 4f;
                center.x = Math.max(margin, Math.min(getWidth() - margin, center.x));
                center.y = Math.max(margin, Math.min(getHeight() - margin, center.y));
            }

            int fillColor = resolveTokenColor(p.tokenColor);
            if (p.userId != null && p.userId.equals(activeTurnPlayerId)) {
                tokenHaloPaint.setStyle(Paint.Style.FILL);
                tokenHaloPaint.setColor(Color.argb(90, Color.red(fillColor), Color.green(fillColor), Color.blue(fillColor)));
                canvas.drawCircle(center.x, center.y, radius * 1.38f, tokenHaloPaint);
            }

            canvas.drawCircle(center.x + 2f, center.y + 4f, radius, tokenShadowPaint);
            tokenFillPaint.setStyle(Paint.Style.FILL);
            tokenFillPaint.setColor(fillColor);
            canvas.drawCircle(center.x, center.y, radius, tokenFillPaint);
            canvas.drawCircle(center.x, center.y, radius, tokenRimPaint);

            String initial = (p.name != null && !p.name.trim().isEmpty())
                    ? String.valueOf(Character.toUpperCase(p.name.trim().charAt(0)))
                    : "P";
            float textY = center.y - ((tokenTextPaint.descent() + tokenTextPaint.ascent()) / 2f);
            canvas.drawText(initial, center.x, textY, tokenTextPaint);
        }
    }

    public static int resolveTokenColor(String colorName) {
        if ("blue".equalsIgnoreCase(colorName)) return Color.parseColor("#2563EB");
        if ("green".equalsIgnoreCase(colorName)) return Color.parseColor("#10B981");
        if ("yellow".equalsIgnoreCase(colorName)) return Color.parseColor("#F59E0B");
        return Color.parseColor("#EF4444");
    }

    @Override
    protected void onDetachedFromWindow() {
        if (currentAnimator != null) {
            currentAnimator.cancel();
            currentAnimator = null;
        }
        super.onDetachedFromWindow();
    }
}
