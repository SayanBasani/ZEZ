package com.zez.manager.gallery.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

public class ImageEditHelper {

    public static final int FILTER_ORIGINAL = 0;
    public static final int FILTER_VIVID    = 1;
    public static final int FILTER_WARM     = 2;
    public static final int FILTER_COOL     = 3;
    public static final int FILTER_MONO     = 4;
    public static final int FILTER_FADED    = 5;

    public static Bitmap rotate(Bitmap source, float degrees) {
        if (source == null || degrees == 0) return source;
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap source, boolean horizontal, boolean vertical) {
        if (source == null) return null;
        Matrix matrix = new Matrix();
        matrix.postScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap crop(Bitmap source, int x, int y, int width, int height) {
        if (source == null) return null;
        int safeX = Math.max(0, Math.min(x, source.getWidth() - 1));
        int safeY = Math.max(0, Math.min(y, source.getHeight() - 1));
        int safeW = Math.max(1, Math.min(width, source.getWidth() - safeX));
        int safeH = Math.max(1, Math.min(height, source.getHeight() - safeY));
        return Bitmap.createBitmap(source, safeX, safeY, safeW, safeH);
    }

    public static Bitmap applyAdjustmentsAndFilter(Bitmap source, float brightness, float contrast, float saturation, int filterType) {
        if (source == null) return null;

        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        ColorMatrix cm = new ColorMatrix();

        // 1. Brightness (-100 to +100 -> value -100..100)
        ColorMatrix cmBrightness = new ColorMatrix(new float[]{
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0
        });

        // 2. Contrast (0.5 to 1.5 -> scale & offset)
        float scale = contrast;
        float translate = (-0.5f * scale + 0.5f) * 255f;
        ColorMatrix cmContrast = new ColorMatrix(new float[]{
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });

        // 3. Saturation (0.0 to 2.0)
        ColorMatrix cmSaturation = new ColorMatrix();
        cmSaturation.setSaturation(saturation);

        // Combine base adjustments
        cm.postConcat(cmBrightness);
        cm.postConcat(cmContrast);
        cm.postConcat(cmSaturation);

        // 4. Preset Filter
        if (filterType == FILTER_VIVID) {
            ColorMatrix cmVivid = new ColorMatrix();
            cmVivid.setSaturation(1.4f);
            ColorMatrix cmVividContrast = new ColorMatrix(new float[]{
                    1.1f, 0, 0, 0, -10f,
                    0, 1.1f, 0, 0, -10f,
                    0, 0, 1.1f, 0, -10f,
                    0, 0, 0, 1, 0
            });
            cm.postConcat(cmVividContrast);
            cm.postConcat(cmVivid);
        } else if (filterType == FILTER_WARM) {
            ColorMatrix cmWarm = new ColorMatrix(new float[]{
                    1.2f, 0, 0, 0, 10f,
                    0, 1.05f, 0, 0, 5f,
                    0, 0, 0.85f, 0, -10f,
                    0, 0, 0, 1, 0
            });
            cm.postConcat(cmWarm);
        } else if (filterType == FILTER_COOL) {
            ColorMatrix cmCool = new ColorMatrix(new float[]{
                    0.85f, 0, 0, 0, -10f,
                    0, 1.0f, 0, 0, 0f,
                    0, 0, 1.25f, 0, 15f,
                    0, 0, 0, 1, 0
            });
            cm.postConcat(cmCool);
        } else if (filterType == FILTER_MONO) {
            ColorMatrix cmMono = new ColorMatrix();
            cmMono.setSaturation(0f);
            cm.postConcat(cmMono);
        } else if (filterType == FILTER_FADED) {
            ColorMatrix cmFaded = new ColorMatrix(new float[]{
                    0.9f, 0, 0, 0, 25f,
                    0, 0.9f, 0, 0, 25f,
                    0, 0, 0.9f, 0, 25f,
                    0, 0, 0, 1, 0
            });
            ColorMatrix cmSat = new ColorMatrix();
            cmSat.setSaturation(0.7f);
            cm.postConcat(cmFaded);
            cm.postConcat(cmSat);
        }

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(source, 0, 0, paint);
        return result;
    }
}
