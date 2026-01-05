package com.rafiq.livewallpaper;

public class WallpaperModel {
    private String imageUrl;
    private String videoUrl;
    private String highResUrl;
    private boolean isVideo;

    public WallpaperModel(String imageUrl, String videoUrl, String highResUrl, boolean isVideo) {
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.highResUrl = highResUrl;
        this.isVideo = isVideo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getHighResUrl() {
        return highResUrl;
    }

    public boolean isVideo() {
        return isVideo;
    }
}
