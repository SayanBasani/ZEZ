package com.zez.manager.gallery.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Robust helper to generate standard A4/high-res PDF documents
 * from a list of images, and manage export directories.
 */
public class PdfDocumentHelper {

    private static final String TAG = "PdfDocumentHelper";

    // Standard A4 dimensions in points (72 points per inch)
    public static final int PAGE_WIDTH_A4 = 595;
    public static final int PAGE_HEIGHT_A4 = 842;

    public static File getExportDirectory(Context context) {
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "PDFs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String generateDefaultFileName(String extension) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "Zez_" + timeStamp + "." + extension;
    }

    public static boolean createPdf(Context context, List<String> imageUris, File outputFile) {
        if (imageUris == null || imageUris.isEmpty() || outputFile == null) return false;

        PdfDocument document = new PdfDocument();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);

        try {
            int pageNumber = 1;
            for (String uriStr : imageUris) {
                Bitmap bitmap = loadScaledBitmap(context, uriStr, PAGE_WIDTH_A4 * 2, PAGE_HEIGHT_A4 * 2);
                if (bitmap == null) continue;

                // Create A4 Page
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH_A4, PAGE_HEIGHT_A4, pageNumber).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                // Draw crisp white background
                canvas.drawColor(Color.WHITE);

                // Scale image into page maintaining aspect ratio
                int bmpW = bitmap.getWidth();
                int bmpH = bitmap.getHeight();
                float scale = Math.min((float) PAGE_WIDTH_A4 / bmpW, (float) PAGE_HEIGHT_A4 / bmpH);

                int drawW = Math.round(bmpW * scale);
                int drawH = Math.round(bmpH * scale);
                int left = (PAGE_WIDTH_A4 - drawW) / 2;
                int top = (PAGE_HEIGHT_A4 - drawH) / 2;

                Rect destRect = new Rect(left, top, left + drawW, top + drawH);
                canvas.drawBitmap(bitmap, null, destRect, paint);

                document.finishPage(page);
                bitmap.recycle();
                pageNumber++;
            }

            FileOutputStream fos = new FileOutputStream(outputFile);
            document.writeTo(fos);
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error generating PDF document", e);
            return false;
        } finally {
            document.close();
        }
    }

    private static Bitmap loadScaledBitmap(Context context, String uriStr, int maxW, int maxH) {
        try {
            Uri uri = uriStr.startsWith("content://") || uriStr.startsWith("file://")
                    ? Uri.parse(uriStr)
                    : Uri.fromFile(new File(uriStr));

            InputStream isBounds;
            if ("file".equalsIgnoreCase(uri.getScheme()) || uriStr.startsWith("/")) {
                String p = uri.getPath() != null ? uri.getPath() : uriStr;
                isBounds = new java.io.FileInputStream(new File(p));
            } else {
                isBounds = context.getContentResolver().openInputStream(uri);
            }
            if (isBounds == null) return null;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(isBounds, null, opts);
            isBounds.close();

            opts.inSampleSize = calculateInSampleSize(opts, maxW, maxH);
            opts.inJustDecodeBounds = false;
            opts.inPreferredConfig = Bitmap.Config.RGB_565; // save memory

            InputStream isActual;
            if ("file".equalsIgnoreCase(uri.getScheme()) || uriStr.startsWith("/")) {
                String p = uri.getPath() != null ? uri.getPath() : uriStr;
                isActual = new java.io.FileInputStream(new File(p));
            } else {
                isActual = context.getContentResolver().openInputStream(uri);
            }
            Bitmap bmp = BitmapFactory.decodeStream(isActual, null, opts);
            if (isActual != null) isActual.close();
            return bmp;
        } catch (Exception e) {
            Log.e(TAG, "Error decoding bitmap from URI: " + uriStr, e);
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
