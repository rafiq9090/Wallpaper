package com.rafiq.livewallpaper;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.IOException;

public class VideoWallpaperService extends WallpaperService {

    public static final String VIDEO_PARAMS_CONTROL_ACTION = "com.rafiq.livewallpaper.VIDEO_PARAMS_CONTROL_ACTION";
    public static final String KEY_ACTION = "action";
    public static final int ACTION_UPDATE_VIDEO = 1;

    @Override
    public Engine onCreateEngine() {
        return new VideoEngine();
    }

    class VideoEngine extends Engine {
        private MediaPlayer mediaPlayer;
        private BroadcastReceiver broadcastReceiver;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            IntentFilter intentFilter = new IntentFilter(VIDEO_PARAMS_CONTROL_ACTION);
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int action = intent.getIntExtra(KEY_ACTION, -1);
                    if (action == ACTION_UPDATE_VIDEO) {
                        playVideo();
                    }
                }
            };
            registerReceiver(broadcastReceiver, intentFilter);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            playVideo();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if (broadcastReceiver != null) {
                unregisterReceiver(broadcastReceiver);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                } else {
                    playVideo();
                }
            } else {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
        }

        private void playVideo() {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            SharedPreferences prefs = getSharedPreferences("WALLPAPER_PREFS", MODE_PRIVATE);
            String videoPath = prefs.getString("video_path", null);

            if (videoPath == null)
                return;

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setSurface(getSurfaceHolder().getSurface());
            mediaPlayer.setVolume(0, 0); // Mute video
            mediaPlayer.setLooping(true);

            try {
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(videoPath));
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
