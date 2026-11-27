package com.zez.manager.gallery.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generates standard Microsoft PowerPoint (.pptx) presentations
 * with each selected image placed cleanly on its own 16:9 slide.
 */
public class PptxGenerator {

    private static final String TAG = "PptxGenerator";

    public static boolean createPresentation(Context context, List<String> imageUris, File outputFile) {
        if (imageUris == null || imageUris.isEmpty() || outputFile == null) return false;

        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(outputFile));
            int slideCount = imageUris.size();

            // 1. [Content_Types].xml
            StringBuilder ctXml = new StringBuilder();
            ctXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
            ctXml.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n");
            ctXml.append("  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n");
            ctXml.append("  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n");
            ctXml.append("  <Default Extension=\"jpeg\" ContentType=\"image/jpeg\"/>\n");
            ctXml.append("  <Default Extension=\"jpg\" ContentType=\"image/jpeg\"/>\n");
            ctXml.append("  <Default Extension=\"png\" ContentType=\"image/png\"/>\n");
            ctXml.append("  <Override PartName=\"/ppt/presentation.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml\"/>\n");
            for (int i = 1; i <= slideCount; i++) {
                ctXml.append("  <Override PartName=\"/ppt/slides/slide").append(i).append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slide+xml\"/>\n");
            }
            ctXml.append("</Types>");
            writeZipEntry(zos, "[Content_Types].xml", ctXml.toString().getBytes(StandardCharsets.UTF_8));

            // 2. _rels/.rels
            String rootRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                    "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"ppt/presentation.xml\"/>\n" +
                    "</Relationships>";
            writeZipEntry(zos, "_rels/.rels", rootRels.getBytes(StandardCharsets.UTF_8));

            // 3. ppt/presentation.xml
            StringBuilder presXml = new StringBuilder();
            presXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
            presXml.append("<p:presentation xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">\n");
            presXml.append("  <p:sldMasterIdLst/>\n");
            presXml.append("  <p:sldIdLst>\n");
            for (int i = 1; i <= slideCount; i++) {
                presXml.append("    <p:sldId id=\"").append(255 + i).append("\" r:id=\"rId").append(i).append("\"/>\n");
            }
            presXml.append("  </p:sldIdLst>\n");
            presXml.append("  <p:sldSz cx=\"12192000\" cy=\"6858000\" type=\"screen16x9\"/>\n");
            presXml.append("  <p:notesSz cx=\"6858000\" cy=\"9144000\"/>\n");
            presXml.append("</p:presentation>");
            writeZipEntry(zos, "ppt/presentation.xml", presXml.toString().getBytes(StandardCharsets.UTF_8));

            // 4. ppt/_rels/presentation.xml.rels
            StringBuilder presRels = new StringBuilder();
            presRels.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
            presRels.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n");
            for (int i = 1; i <= slideCount; i++) {
                presRels.append("  <Relationship Id=\"rId").append(i).append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide\" Target=\"slides/slide").append(i).append(".xml\"/>\n");
            }
            presRels.append("</Relationships>");
            writeZipEntry(zos, "ppt/_rels/presentation.xml.rels", presRels.toString().getBytes(StandardCharsets.UTF_8));

            // 5. Slides & Media
            long slideWidthEmu = 12192000L;
            long slideHeightEmu = 6858000L;

            for (int i = 1; i <= slideCount; i++) {
                String uriStr = imageUris.get(i - 1);
                byte[] imgBytes = readImageBytes(context, uriStr);
                if (imgBytes == null) continue;

                // Write media file
                String mediaName = "image" + i + ".jpeg";
                writeZipEntry(zos, "ppt/media/" + mediaName, imgBytes);

                // Write slide rels
                String slideRelXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                        "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"../media/" + mediaName + "\"/>\n" +
                        "</Relationships>";
                writeZipEntry(zos, "ppt/slides/_rels/slide" + i + ".xml.rels", slideRelXml.getBytes(StandardCharsets.UTF_8));

                // Calculate aspect-fit placement
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length, opts);
                int imgW = opts.outWidth > 0 ? opts.outWidth : 1920;
                int imgH = opts.outHeight > 0 ? opts.outHeight : 1080;

                double scale = Math.min((double) slideWidthEmu / imgW, (double) slideHeightEmu / imgH);
                long targetW = Math.round(imgW * scale);
                long targetH = Math.round(imgH * scale);
                long offX = (slideWidthEmu - targetW) / 2L;
                long offY = (slideHeightEmu - targetH) / 2L;

                // Write slide XML
                StringBuilder sldXml = new StringBuilder();
                sldXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
                sldXml.append("<p:sld xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">\n");
                sldXml.append("  <p:cSld>\n");
                sldXml.append("    <p:spTree>\n");
                sldXml.append("      <p:nvGrpSpPr><p:cNvPr id=\"1\" name=\"\"/><p:cNvGrpSpPr/><p:grpSpPr/></p:nvGrpSpPr>\n");
                sldXml.append("      <p:pic>\n");
                sldXml.append("        <p:nvPicPr><p:cNvPr id=\"2\" name=\"Picture ").append(i).append("\"/><p:cNvPicPr><a:picLocks noChangeAspect=\"1\"/></p:cNvPicPr><p:nvPr/></p:nvPicPr>\n");
                sldXml.append("        <p:blipFill><a:blip r:embed=\"rId1\"/><a:stretch><a:fillRect/></a:stretch></p:blipFill>\n");
                sldXml.append("        <p:spPr>\n");
                sldXml.append("          <a:xfrm><a:off x=\"").append(offX).append("\" y=\"").append(offY).append("\"/><a:ext cx=\"").append(targetW).append("\" cy=\"").append(targetH).append("\"/></a:xfrm>\n");
                sldXml.append("          <a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>\n");
                sldXml.append("        </p:spPr>\n");
                sldXml.append("      </p:pic>\n");
                sldXml.append("    </p:spTree>\n");
                sldXml.append("  </p:cSld>\n");
                sldXml.append("</p:sld>");

                writeZipEntry(zos, "ppt/slides/slide" + i + ".xml", sldXml.toString().getBytes(StandardCharsets.UTF_8));
            }

            zos.finish();
            zos.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error generating PPTX presentation", e);
            return false;
        } finally {
            if (zos != null) {
                try { zos.close(); } catch (Exception ignored) {}
            }
        }
    }

    private static void writeZipEntry(ZipOutputStream zos, String entryName, byte[] data) throws Exception {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }

    private static byte[] readImageBytes(Context context, String uriStr) {
        if (uriStr == null) return null;
        try {
            Uri uri = uriStr.startsWith("content://") || uriStr.startsWith("file://")
                    ? Uri.parse(uriStr)
                    : Uri.fromFile(new File(uriStr));
            InputStream is;
            if ("file".equalsIgnoreCase(uri.getScheme()) || uriStr.startsWith("/")) {
                String p = uri.getPath() != null ? uri.getPath() : uriStr;
                is = new java.io.FileInputStream(new File(p));
            } else {
                is = context.getContentResolver().openInputStream(uri);
            }
            if (is == null) return null;

            // Compress to JPEG for compact presentation size
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            if (bmp == null) return null;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            bmp.recycle();
            return baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Failed reading image bytes for PPTX: " + uriStr, e);
            return null;
        }
    }
}
