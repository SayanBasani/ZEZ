package com.zez.manager.gallery.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.zez.manager.gallery.model.AlbumItem;
import com.zez.manager.gallery.model.MediaItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MediaHelper {

    public static List<MediaItem> getImagesAndVideos(Context context) {
        List<MediaItem> items = new ArrayList<>();
        if (context == null) return items;

        String sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";

        // 1. Query Images safely
        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cImg = null;
        try {
            cImg = context.getContentResolver().query(imagesUri, null, null, null, sortOrder);
            if (cImg != null && cImg.moveToFirst()) {
                int iId = cImg.getColumnIndex(MediaStore.Images.Media._ID);
                int iName = cImg.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int iSize = cImg.getColumnIndex(MediaStore.Images.Media.SIZE);
                int iDate = cImg.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                int iW = cImg.getColumnIndex(MediaStore.Images.Media.WIDTH);
                int iH = cImg.getColumnIndex(MediaStore.Images.Media.HEIGHT);
                int iBkt = cImg.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int iBktId = cImg.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);

                do {
                    long id = iId >= 0 ? cImg.getLong(iId) : -1;
                    if (id < 0) continue;
                    Uri uri = ContentUris.withAppendedId(imagesUri, id);
                    String name = iName >= 0 ? cImg.getString(iName) : "Photo_" + id;
                    long size = iSize >= 0 ? cImg.getLong(iSize) : 0;
                    long date = iDate >= 0 ? cImg.getLong(iDate) : System.currentTimeMillis() / 1000;
                    int w = iW >= 0 ? cImg.getInt(iW) : 0;
                    int h = iH >= 0 ? cImg.getInt(iH) : 0;
                    String bkt = iBkt >= 0 ? cImg.getString(iBkt) : "Photos";
                    if (bkt == null || bkt.trim().isEmpty()) bkt = "Photos";
                    long bktId = 0;
                    if (iBktId >= 0) {
                        try { bktId = cImg.getLong(iBktId); } catch (Exception ignored) {}
                    }
                    items.add(new MediaItem(id, uri, name, size, date, false, 0, w, h, bkt, bktId));
                } while (cImg.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cImg != null) cImg.close();
        }

        // 2. Query Videos safely
        Uri videosUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cVid = null;
        try {
            cVid = context.getContentResolver().query(videosUri, null, null, null, sortOrder);
            if (cVid != null && cVid.moveToFirst()) {
                int iId = cVid.getColumnIndex(MediaStore.Video.Media._ID);
                int iName = cVid.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                int iSize = cVid.getColumnIndex(MediaStore.Video.Media.SIZE);
                int iDate = cVid.getColumnIndex(MediaStore.Video.Media.DATE_ADDED);
                int iDur = cVid.getColumnIndex(MediaStore.Video.Media.DURATION);
                int iW = cVid.getColumnIndex(MediaStore.Video.Media.WIDTH);
                int iH = cVid.getColumnIndex(MediaStore.Video.Media.HEIGHT);
                int iBkt = cVid.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                int iBktId = cVid.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);

                do {
                    long id = iId >= 0 ? cVid.getLong(iId) : -1;
                    if (id < 0) continue;
                    Uri uri = ContentUris.withAppendedId(videosUri, id);
                    String name = iName >= 0 ? cVid.getString(iName) : "Video_" + id;
                    long size = iSize >= 0 ? cVid.getLong(iSize) : 0;
                    long date = iDate >= 0 ? cVid.getLong(iDate) : System.currentTimeMillis() / 1000;
                    long dur = iDur >= 0 ? cVid.getLong(iDur) : 0;
                    int w = iW >= 0 ? cVid.getInt(iW) : 0;
                    int h = iH >= 0 ? cVid.getInt(iH) : 0;
                    String bkt = iBkt >= 0 ? cVid.getString(iBkt) : "Videos";
                    if (bkt == null || bkt.trim().isEmpty()) bkt = "Videos";
                    long bktId = 0;
                    if (iBktId >= 0) {
                        try { bktId = cVid.getLong(iBktId); } catch (Exception ignored) {}
                    }
                    items.add(new MediaItem(id, uri, name, size, date, true, dur, w, h, bkt, bktId));
                } while (cVid.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cVid != null) cVid.close();
        }

        items.sort((a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
        return items;
    }

    public static List<AlbumItem> getAlbums(Context context) {
        List<MediaItem> all = getImagesAndVideos(context);

        // Filter out items in recycle bin
        List<MediaItem> activeList = new ArrayList<>();
        for (MediaItem item : all) {
            if (!RecycleBinHelper.isRecycled(context, item.getId())) {
                activeList.add(item);
            }
        }

        // 1. Check favorites
        Set<String> favIds = FavoritesHelper.getFavoriteIds(context);
        List<MediaItem> favItems = new ArrayList<>();
        List<MediaItem> videoItems = new ArrayList<>();
        List<MediaItem> screenshotItems = new ArrayList<>();

        Map<String, List<MediaItem>> map = new LinkedHashMap<>();
        Map<String, Long> bucketIdMap = new LinkedHashMap<>();

        for (MediaItem item : activeList) {
            if (favIds.contains(String.valueOf(item.getId()))) {
                favItems.add(item);
            }
            if (item.isVideo()) {
                videoItems.add(item);
            }
            String bktName = item.getBucketName();
            if (bktName != null && bktName.toLowerCase().contains("screenshot")) {
                screenshotItems.add(item);
            }

            if (!map.containsKey(bktName)) {
                map.put(bktName, new ArrayList<>());
                bucketIdMap.put(bktName, item.getBucketId());
            }
            map.get(bktName).add(item);
        }

        List<AlbumItem> albums = new ArrayList<>();

        // Add Special Pinned Albums at Top:
        // A. Favorites
        if (!favItems.isEmpty()) {
            albums.add(new AlbumItem("Favorites", favItems.get(0).getUri(), favItems.size(), 0, AlbumItem.TYPE_FAVORITES));
        } else {
            albums.add(new AlbumItem("Favorites", null, 0, 0, AlbumItem.TYPE_FAVORITES));
        }

        // B. Videos special album
        if (!videoItems.isEmpty()) {
            albums.add(new AlbumItem("Videos", videoItems.get(0).getUri(), videoItems.size(), 0, AlbumItem.TYPE_VIDEOS));
        }

        // C. Screenshots special album if exists
        if (!screenshotItems.isEmpty()) {
            albums.add(new AlbumItem("Screenshots", screenshotItems.get(0).getUri(), screenshotItems.size(), 0, AlbumItem.TYPE_SCREENSHOTS));
        }

        // Normal User / App Buckets
        for (Map.Entry<String, List<MediaItem>> entry : map.entrySet()) {
            String bktName = entry.getKey();
            if (!screenshotItems.isEmpty() && bktName != null && bktName.toLowerCase().contains("screenshot")) {
                continue;
            }
            List<MediaItem> members = entry.getValue();
            long bId = bucketIdMap.containsKey(bktName) ? bucketIdMap.get(bktName) : 0;
            albums.add(new AlbumItem(bktName, members.get(0).getUri(), members.size(), bId, AlbumItem.TYPE_NORMAL));
        }

        // D. Recycle Bin smart collection (at bottom of gallery albums)
        List<RecycleBinHelper.RecycledItem> recycledItems = RecycleBinHelper.getRecycledItems(context);
        Uri binCover = null;
        if (recycledItems != null && !recycledItems.isEmpty()) {
            try {
                if (recycledItems.get(0).uriString != null) {
                    binCover = Uri.parse(recycledItems.get(0).uriString);
                }
            } catch (Exception ignored) {}
        }
        int binCount = recycledItems != null ? recycledItems.size() : 0;
        albums.add(new AlbumItem("Recycle Bin", binCover, binCount, 0, AlbumItem.TYPE_BIN));

        return albums;
    }

    /**
     * Get media items belonging to a specific album bucket or special album.
     */
    public static List<MediaItem> getMediaForAlbum(Context context, String albumName, long bucketId, int specialType) {
        List<MediaItem> all = getImagesAndVideos(context);
        List<MediaItem> result = new ArrayList<>();
        Set<String> favIds = FavoritesHelper.getFavoriteIds(context);

        for (MediaItem item : all) {
            if (RecycleBinHelper.isRecycled(context, item.getId())) {
                continue;
            }

            if (specialType == AlbumItem.TYPE_FAVORITES) {
                if (favIds.contains(String.valueOf(item.getId()))) {
                    result.add(item);
                }
            } else if (specialType == AlbumItem.TYPE_VIDEOS) {
                if (item.isVideo()) {
                    result.add(item);
                }
            } else if (specialType == AlbumItem.TYPE_SCREENSHOTS) {
                String bkt = item.getBucketName();
                if (bkt != null && bkt.toLowerCase().contains("screenshot")) {
                    result.add(item);
                }
            } else {
                // Normal bucket matching by bucketId or bucketName
                if (bucketId != 0 && item.getBucketId() == bucketId) {
                    result.add(item);
                } else if (albumName != null && albumName.equalsIgnoreCase(item.getBucketName())) {
                    result.add(item);
                }
            }
        }
        return result;
    }
}
