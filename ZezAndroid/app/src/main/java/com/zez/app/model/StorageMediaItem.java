package com.zez.app.model;

import com.zez.app.util.QuackyStorageHelper;

import java.io.File;

public class StorageMediaItem {
    private final ChatMessage message;
    private final File localFile;
    private final String mediaUrl;
    private final String mediaType;
    private final String fileName;
    private final long fileSizeBytes;
    private final long timestamp;
    private boolean isSelected;

    public StorageMediaItem(ChatMessage message, File localFile, String mediaUrl, String mediaType, String fileName, long fileSizeBytes, long timestamp) {
        this.message = message;
        this.localFile = localFile;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType != null ? mediaType.toLowerCase() : "image";
        this.fileName = fileName != null ? fileName : "media";
        this.fileSizeBytes = fileSizeBytes > 0 ? fileSizeBytes : (localFile != null && localFile.exists() ? localFile.length() : 250 * 1024L);
        this.timestamp = timestamp;
        this.isSelected = false;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public File getLocalFile() {
        return localFile;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getFormattedSize() {
        return QuackyStorageHelper.formatFileSize(fileSizeBytes);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
