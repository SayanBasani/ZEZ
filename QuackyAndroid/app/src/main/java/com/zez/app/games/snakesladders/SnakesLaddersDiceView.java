package com.zez.app.games.snakesladders;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.zez.app.theme.ThemeManager;

/**
 * Lightweight animated 6-sided dice view. Animation only visualizes rolling;
 * the final face value is always supplied authoritatively by the server.
 */
public class SnakesLaddersDiceView extends View {

    private int value = 1;
    private ValueAnimator rollAnimator;
    private int animatedPreviewValue = 1;
    private float tiltDegrees = 0f;

    private final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public SnakesLaddersDiceView(Context context) {
        super(context);
        init();
    }

    public SnakesLaddersDiceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnakesLaddersDiceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bodyPaint.setStyle(Paint.Style.FILL);
        borderPaint.setStyle(Paint.Style.STROKE);
        pipPaint.setStyle(Paint.Style.FILL);
    }

    public void setValue(int diceValue) {
        this.value = Math.max(1, Math.min(6, diceValue));
        this.animatedPreviewValue = this.value;
        this.tiltDegrees = 0f;
        stopRollingAnimation();
        invalidate();
    }

    public void rollDiceToValue(int diceValue, long durationMs, Runnable onSettled) {
        stopRollingAnimation();
        final int target = Math.max(1, Math.min(6, diceValue));
        this.value = target;

        if (!ValueAnimator.areAnimatorsEnabled()) {
            setValue(target);
            if (onSettled != null) onSettled.run();
            return;
        }

        rollAnimator = ValueAnimator.ofFloat(0f, 1f);
        rollAnimator.setDuration(Math.max(500L, durationMs));
        rollAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator(1.4f));

        final java.util.Random rnd = new java.util.Random();
        rollAnimator.addUpdateListener(anim -> {
            float f = (float) anim.getAnimatedValue();
            if (f < 0.80f) {
                animatedPreviewValue = 1 + rnd.nextInt(6);
                tiltDegrees = (float) Math.sin(f * Math.PI * 8.0) * (22f * (1f - f * 0.5f));
                float scale = 1.0f + (float) Math.sin(f * Math.PI) * 0.16f;
                setScaleX(scale);
                setScaleY(scale);
            } else {
                animatedPreviewValue = target;
                float local = (f - 0.80f) / 0.20f;
                tiltDegrees = (float) Math.sin((1f - local) * Math.PI * 2.0) * 5f;
                float scale = 1.0f + (1f - local) * 0.08f;
                setScaleX(scale);
                setScaleY(scale);
            }
            invalidate();
        });

        rollAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                stopRollingAnimation();
                setValue(target);
                animate().scaleX(1.15f).scaleY(1.15f).setDuration(100).withEndAction(() -> {
                    animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).withEndAction(() -> {
                        if (onSettled != null) onSettled.run();
                    }).start();
                }).start();
            }
        });

        rollAnimator.start();
    }

    public void startRollingAnimation() {
        stopRollingAnimation();
        if (!ValueAnimator.areAnimatorsEnabled()) return;
        rollAnimator = ValueAnimator.ofFloat(0f, 1f);
        rollAnimator.setDuration(420);
        rollAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rollAnimator.setInterpolator(new LinearInterpolator());
        rollAnimator.addUpdateListener(anim -> {
            float f = (float) anim.getAnimatedValue();
            animatedPreviewValue = 1 + ((int) (f * 6f)) % 6;
            tiltDegrees = (float) Math.sin(f * Math.PI * 4.0) * 10f;
            invalidate();
        });
        rollAnimator.start();
    }

    public void stopRollingAnimation() {
        if (rollAnimator != null) {
            rollAnimator.cancel();
            rollAnimator = null;
        }
        tiltDegrees = 0f;
        setScaleX(1f);
        setScaleY(1f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        boolean dark = ThemeManager.isDarkMode(getContext());
        bodyPaint.setColor(dark ? Color.parseColor("#F8FAFC") : Color.WHITE);
        borderPaint.setColor(dark ? Color.parseColor("#CBD5E1") : Color.parseColor("#334155"));
        borderPaint.setStrokeWidth(Math.max(3f, w * 0.055f));
        pipPaint.setColor(animatedPreviewValue == 6 ? Color.parseColor("#DC2626") : Color.parseColor("#0F172A"));

        float pad = w * 0.08f;
        rect.set(pad, pad, w - pad, h - pad);
        float radius = w * 0.22f;

        canvas.save();
        canvas.rotate(tiltDegrees, w / 2f, h / 2f);
        canvas.drawRoundRect(rect, radius, radius, bodyPaint);
        canvas.drawRoundRect(rect, radius, radius, borderPaint);

        float cx = w * 0.5f;
        float cy = h * 0.5f;
        float offset = w * 0.23f;
        float pipR = w * 0.085f;
        int face = animatedPreviewValue;

        if (face == 1 || face == 3 || face == 5) {
            canvas.drawCircle(cx, cy, pipR, pipPaint);
        }
        if (face >= 2) {
            canvas.drawCircle(cx - offset, cy - offset, pipR, pipPaint);
            canvas.drawCircle(cx + offset, cy + offset, pipR, pipPaint);
        }
        if (face >= 4) {
            canvas.drawCircle(cx + offset, cy - offset, pipR, pipPaint);
            canvas.drawCircle(cx - offset, cy + offset, pipR, pipPaint);
        }
        if (face == 6) {
            canvas.drawCircle(cx - offset, cy, pipR, pipPaint);
            canvas.drawCircle(cx + offset, cy, pipR, pipPaint);
        }
        canvas.restore();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopRollingAnimation();
        super.onDetachedFromWindow();
    }
}
