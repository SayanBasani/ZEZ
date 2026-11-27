package com.zez.manager.music.model;

import java.io.Serializable;
import java.util.Objects;

public class MusicTrack implements Serializable {
    private String id;
    private String title;
    private String artist;
    private String duration;
    private String thumbnailUrl;
    private String views;
    private boolean isDownloaded;
    private String localFilePath;

    public MusicTrack() {}

    public MusicTrack(String id, String title, String artist, String duration, String thumbnailUrl, String views) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.thumbnailUrl = thumbnailUrl;
        this.views = views;
        this.isDownloaded = false;
        this.localFilePath = null;
    }

    public String getId() {
        return id;
    }

    public String getVideoId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist != null ? artist : "Unknown Artist";
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration != null ? duration : "--:--";
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getThumbnailUrl() {
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            return thumbnailUrl;
        }
        if (id != null && !id.isEmpty()) {
            return "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg";
        }
        return "";
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getViews() {
        return views != null ? views : "";
    }

    public void setViews(String views) {
        this.views = views;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicTrack that = (MusicTrack) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
