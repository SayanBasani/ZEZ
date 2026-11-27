package com.zez.app.model;

import android.net.Uri;

public class MediaItem {
    private long    id;
    private Uri     uri;
    private String  name;
    private long    size;
    private long    dateAdded;
    private boolean isVideo;
    private long    duration;
    private int     width;
    private int     height;
    private String  bucketName;
    private long    bucketId;

    public MediaItem(long id, Uri uri, String name, long size, long dateAdded,
                     boolean isVideo, long duration, int width, int height, String bucketName) {
        this(id, uri, name, size, dateAdded, isVideo, duration, width, height, bucketName, 0);
    }

    public MediaItem(long id, Uri uri, String name, long size, long dateAdded,
                     boolean isVideo, long duration, int width, int height, String bucketName, long bucketId) {
        this.id         = id;
        this.uri        = uri;
        this.name       = name;
        this.size       = size;
        this.dateAdded  = dateAdded;
        this.isVideo    = isVideo;
        this.duration   = duration;
        this.width      = width;
        this.height     = height;
        this.bucketName = bucketName;
        this.bucketId   = bucketId;
    }

    public long    getId()         { return id; }
    public Uri     getUri()        { return uri; }
    public String  getName()       { return name; }
    public long    getSize()       { return size; }
    public long    getDateAdded()  { return dateAdded; }
    public boolean isVideo()       { return isVideo; }
    public long    getDuration()   { return duration; }
    public int     getWidth()      { return width; }
    public int     getHeight()     { return height; }
    public String  getBucketName() { return bucketName != null ? bucketName : "Unknown"; }
    public long    getBucketId()   { return bucketId; }
}
