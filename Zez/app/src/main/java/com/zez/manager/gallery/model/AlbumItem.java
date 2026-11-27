package com.zez.manager.gallery.model;

import android.net.Uri;

public class AlbumItem {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_FAVORITES = 1;
    public static final int TYPE_VIDEOS = 2;
    public static final int TYPE_SCREENSHOTS = 3;
    public static final int TYPE_BIN = 4;

    public final String name;
    public final Uri coverUri;
    public final int count;
    public final long bucketId;
    public final int specialType;

    public AlbumItem(String name, Uri coverUri, int count) {
        this(name, coverUri, count, 0, TYPE_NORMAL);
    }

    public AlbumItem(String name, Uri coverUri, int count, long bucketId, int specialType) {
        this.name = name;
        this.coverUri = coverUri;
        this.count = count;
        this.bucketId = bucketId;
        this.specialType = specialType;
    }

    public boolean isSpecial() {
        return specialType != TYPE_NORMAL;
    }
}
