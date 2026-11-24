package com.zez.app.music.download;

import com.zez.app.music.model.MusicTrack;

import java.io.File;
import java.util.Locale;

import okhttp3.Call;

public class DownloadTask {
    public static final int STATUS_QUEUED = 0;
    public static final int STATUS_DOWNLOADING = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_FAILED = 3;
    public static final int STATUS_CANCELLED = 4;

    private final String videoId;
    private final String title;
    private final String artist;
    private final String thumbnailUrl;
    private final String fileName;
    private final MusicTrack originalTrack;

    private File destinationFile;
    private long totalBytes = 0;
    private long downloadedBytes = 0;
    private int percent = 0;
    private long speedBytesPerSec = 0;
    private int status = STATUS_QUEUED;
    private String errorMessage = null;

    private transient Call activeCall;

    public DownloadTask(MusicTrack track, String fileName, File destinationFile) {
        this.originalTrack = track;
        this.videoId = track.getId();
        this.title = track.getTitle();
        this.artist = track.getArtist();
        this.thumbnailUrl = track.getThumbnailUrl();
        this.fileName = fileName;
        this.destinationFile = destinationFile;
    }

    public String getVideoId() { return videoId; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getFileName() { return fileName; }
    public MusicTrack getOriginalTrack() { return originalTrack; }

    public File getDestinationFile() { return destinationFile; }
    public void setDestinationFile(File destinationFile) { this.destinationFile = destinationFile; }

    public long getTotalBytes() { return totalBytes; }
    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
        updatePercent();
    }

    public long getDownloadedBytes() { return downloadedBytes; }
    public void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
        updatePercent();
    }

    private void updatePercent() {
        if (totalBytes > 0) {
            this.percent = (int) Math.min(100, Math.max(0, (downloadedBytes * 100) / totalBytes));
        } else {
            this.percent = 0;
        }
    }

    public int getPercent() { return percent; }
    public void setPercent(int percent) { this.percent = percent; }

    public long getSpeedBytesPerSec() { return speedBytesPerSec; }
    public void setSpeedBytesPerSec(long speedBytesPerSec) { this.speedBytesPerSec = speedBytesPerSec; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Call getActiveCall() { return activeCall; }
    public void setActiveCall(Call activeCall) { this.activeCall = activeCall; }

    public String getSpeedFormatted() {
        if (speedBytesPerSec <= 0) return "0 KB/s";
        if (speedBytesPerSec < 1024 * 1024) {
            return String.format(Locale.US, "%.1f KB/s", speedBytesPerSec / 1024.0);
        } else {
            return String.format(Locale.US, "%.2f MB/s", speedBytesPerSec / (1024.0 * 1024.0));
        }
    }

    public String getSizeFormatted() {
        double downloadedMb = downloadedBytes / (1024.0 * 1024.0);
        if (totalBytes > 0) {
            double totalMb = totalBytes / (1024.0 * 1024.0);
            return String.format(Locale.US, "%.1f MB / %.1f MB", downloadedMb, totalMb);
        } else if (downloadedBytes > 0) {
            return String.format(Locale.US, "%.1f MB", downloadedMb);
        } else {
            return "Preparing...";
        }
    }
}
