package com.zez.manager.gallery.util;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

public class ZoomImageView extends AppCompatImageView {

    public interface OnTapListener {
        void onTap();
    }

    private final Matrix matrix = new Matrix();
    private final float[] matrixValues = new float[9];

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private OnTapListener onTapListener;

    private float baseScale = 1.0f;
    private float minScale = 1.0f;
    private float maxScale = 4.0f;
    private boolean isLongScreenshot = false;

    private int viewWidth = 0;
    private int viewHeight = 0;
    private boolean imageFitted = false;

    public ZoomImageView(Context context) {
        super(context);
        init(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void setOnTapListener(OnTapListener listener) {
        this.onTapListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        fitImage();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        imageFitted = false;
        fitImage();
    }

    public void fitImage() {
        Drawable drawable = getDrawable();
        if (drawable == null || viewWidth <= 0 || viewHeight <= 0) return;

        int dw = drawable.getIntrinsicWidth();
        int dh = drawable.getIntrinsicHeight();
        if (dw <= 0 || dh <= 0) return;

        float ratio = (float) dh / (float) dw;
        isLongScreenshot = (ratio >= 1.75f);

        matrix.reset();

        if (isLongScreenshot) {
            // Long screenshot: fit width exactly, align at top (y = 0) so top of document/page is visible
            baseScale = (float) viewWidth / (float) dw;
            matrix.postScale(baseScale, baseScale);
            matrix.postTranslate(0f, 0f);
        } else {
            // Standard image: fit center
            float scaleX = (float) viewWidth / (float) dw;
            float scaleY = (float) viewHeight / (float) dh;
            baseScale = Math.min(scaleX, scaleY);
            matrix.postScale(baseScale, baseScale);

            float transX = (viewWidth - (dw * baseScale)) / 2f;
            float transY = (viewHeight - (dh * baseScale)) / 2f;
            matrix.postTranslate(transX, transY);
        }

        minScale = baseScale;
        maxScale = Math.max(baseScale * 4.5f, 4.0f);

        setImageMatrix(matrix);
        imageFitted = true;
    }

    public void resetZoom() {
        fitImage();
    }

    private RectF getImageBounds() {
        RectF bounds = new RectF();
        Drawable d = getDrawable();
        if (d != null) {
            bounds.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(bounds);
        }
        return bounds;
    }

    private float getScale() {
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    private void clampAndApply() {
        RectF bounds = getImageBounds();
        float deltaX = 0f;
        float deltaY = 0f;

        // Horizontal bounds
        if (bounds.width() <= viewWidth) {
            deltaX = (viewWidth - bounds.width()) / 2f - bounds.left;
        } else {
            if (bounds.left > 0) {
                deltaX = -bounds.left;
            } else if (bounds.right < viewWidth) {
                deltaX = viewWidth - bounds.right;
            }
        }

        // Vertical bounds
        if (bounds.height() <= viewHeight) {
            deltaY = (viewHeight - bounds.height()) / 2f - bounds.top;
        } else {
            if (bounds.top > 0) {
                deltaY = -bounds.top;
            } else if (bounds.bottom < viewHeight) {
                deltaY = viewHeight - bounds.bottom;
            }
        }

        if (deltaX != 0 || deltaY != 0) {
            matrix.postTranslate(deltaX, deltaY);
        }
        setImageMatrix(matrix);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean scaleActive = scaleDetector.isInProgress();
        boolean handled = scaleDetector.onTouchEvent(event);
        handled |= gestureDetector.onTouchEvent(event);

        if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            float currentScale = getScale();
            if (currentScale < minScale) {
                animateScale(currentScale, minScale, viewWidth / 2f, viewHeight / 2f);
            } else {
                clampAndApply();
            }
        }

        return handled || super.onTouchEvent(event);
    }

    private void animateScale(float fromScale, float toScale, float focusX, float focusY) {
        ValueAnimator animator = ValueAnimator.ofFloat(fromScale, toScale);
        animator.setDuration(220);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float val = (Float) animation.getAnimatedValue();
            float curr = getScale();
            float factor = val / curr;
            matrix.postScale(factor, factor, focusX, focusY);
            clampAndApply();
        });
        animator.start();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float currentScale = getScale();
            float newScale = currentScale * scaleFactor;

            if (newScale < minScale * 0.75f) {
                scaleFactor = (minScale * 0.75f) / currentScale;
            } else if (newScale > maxScale * 1.35f) {
                scaleFactor = (maxScale * 1.35f) / currentScale;
            }

            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            clampAndApply();

            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (onTapListener != null) {
                onTapListener.onTap();
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float currentScale = getScale();
            float targetScale;
            if (currentScale > baseScale * 1.3f) {
                targetScale = minScale;
            } else {
                targetScale = Math.min(baseScale * 2.8f, maxScale);
            }
            animateScale(currentScale, targetScale, e.getX(), e.getY());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (scaleDetector.isInProgress()) return false;

            float currentScale = getScale();
            RectF bounds = getImageBounds();

            if (getParent() != null) {
                if (currentScale > baseScale * 1.05f) {
                    boolean atLeftEdge = (bounds.left >= -1f && distanceX < 0);
                    boolean atRightEdge = (bounds.right <= viewWidth + 1f && distanceX > 0);
                    getParent().requestDisallowInterceptTouchEvent(!atLeftEdge && !atRightEdge);
                } else if (isLongScreenshot) {
                    if (Math.abs(distanceY) > Math.abs(distanceX)) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            }

            matrix.postTranslate(-distanceX, -distanceY);
            clampAndApply();
            return true;
        }
    }
}
