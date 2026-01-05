package com.example.wallpaper;

public class WallpaperModel {
    private String imageUrl;
    private String videoUrl;
    private boolean isVideo;

    public WallpaperModel(String imageUrl, String videoUrl, boolean isVideo) {
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.isVideo = isVideo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public boolean isVideo() {
        return isVideo;
    }
}
