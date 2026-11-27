package com.zez.manager.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.zez.manager.R;

import java.util.LinkedList;

/**
 * Clean real-time telemetry line graph view with smooth curve and fill.
 */
public class TelemetryGraphView extends View {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    private final LinkedList<Float> dataPoints = new LinkedList<>();
    private int maxPoints = 25;
    private int lineColor = Color.parseColor("#2563EB");
    private int fillColor = Color.parseColor("#202563EB");

    public TelemetryGraphView(Context context) {
        super(context);
        init();
    }

    public TelemetryGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TelemetryGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        TypedValue typedValue = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.colorThemePrimary, typedValue, true)
                || getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)) {
            lineColor = typedValue.data;
            fillColor = (lineColor & 0x00FFFFFF) | 0x26000000;
        }

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setColor(lineColor);
        linePaint.setPathEffect(new CornerPathEffect(12f));

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1.5f);
        gridPaint.setColor(Color.parseColor("#15808080"));

        // Initialize with default baseline
        for (int i = 0; i < maxPoints; i++) {
            dataPoints.add(10f + (float) (Math.random() * 15f));
        }
    }

    public void setGraphColor(int strokeColor, int areaColor) {
        this.lineColor = strokeColor;
        this.fillColor = areaColor;
        linePaint.setColor(lineColor);
        fillPaint.setColor(fillColor);
        invalidate();
    }

    public void addDataPoint(float percentage) {
        percentage = Math.max(0f, Math.min(100f, percentage));
        if (dataPoints.size() >= maxPoints) {
            dataPoints.removeFirst();
        }
        dataPoints.add(percentage);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || dataPoints.isEmpty()) return;

        // Draw horizontal grid lines (25%, 50%, 75%)
        for (int i = 1; i <= 3; i++) {
            float y = h - (h * (i * 0.25f));
            canvas.drawLine(0, y, w, y, gridPaint);
        }

        float stepX = (float) w / (maxPoints - 1);
        linePath.reset();
        fillPath.reset();

        float firstX = 0f;
        float firstY = h - (dataPoints.get(0) / 100f * (h - 8f)) - 4f;
        linePath.moveTo(firstX, firstY);
        fillPath.moveTo(firstX, h);
        fillPath.lineTo(firstX, firstY);

        for (int i = 1; i < dataPoints.size(); i++) {
            float x = i * stepX;
            float val = dataPoints.get(i);
            float y = h - (val / 100f * (h - 8f)) - 4f;
            linePath.lineTo(x, y);
            fillPath.lineTo(x, y);
        }

        float lastX = (dataPoints.size() - 1) * stepX;
        fillPath.lineTo(lastX, h);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }
}
