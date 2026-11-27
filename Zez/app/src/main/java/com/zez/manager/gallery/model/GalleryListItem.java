package com.zez.manager.gallery.model;

public class GalleryListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_MEDIA  = 1;

    public final int       type;
    public final String    header;
    public final MediaItem media;

    public GalleryListItem(String header) {
        this.type   = TYPE_HEADER;
        this.header = header;
        this.media  = null;
    }

    public GalleryListItem(MediaItem media) {
        this.type   = TYPE_MEDIA;
        this.header = null;
        this.media  = media;
    }
}
