package com.example.wallpaper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WallpaerActivity extends AppCompatActivity {

    private ImageView wallpaperIV;
    private VideoView videoView;
    private Button setWallpaperBtn;
    private ProgressBar progressBar;
    private String imgUrl;
    private String videoUrl;
    private boolean isVideo;
    private WallpaperManager wallpaperManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaer);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        wallpaperIV = findViewById(R.id.idIVWallpapers);
        videoView = findViewById(R.id.idVideoView);
        setWallpaperBtn = findViewById(R.id.idBtnSetWallpaper);
        progressBar = findViewById(R.id.idPBWallpaper);

        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

        imgUrl = getIntent().getStringExtra("imageUrl");
        isVideo = getIntent().getBooleanExtra("isVideo", false);
        videoUrl = getIntent().getStringExtra("videoUrl");

        if (isVideo) {
            wallpaperIV.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            playPreviewVideo();
        } else {
            videoView.setVisibility(View.GONE);
            wallpaperIV.setVisibility(View.VISIBLE);
            loadImage();
        }

        setWallpaperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideo) {
                    setLiveWallpaper();
                } else {
                    setStaticWallpaper();
                }
            }
        });
    }

    private void loadImage() {
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(this).load(imgUrl).listener(new RequestListener<android.graphics.drawable.Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                    Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                    Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(wallpaperIV);
    }

    private void playPreviewVideo() {
        // Just stream for preview
        Uri uri = Uri.parse(videoUrl);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> {
            progressBar.setVisibility(View.GONE);
            mp.setLooping(true);
            videoView.start();
        });
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setStaticWallpaper() {
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(WallpaerActivity.this).asBitmap().load(imgUrl).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target,
                    boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(WallpaerActivity.this, "Fail to load image..", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource,
                    boolean isFirstResource) {
                try {
                    wallpaperManager.setBitmap(resource);
                    FancyToast.makeText(WallpaerActivity.this, "Wallpaper set to home screen.", FancyToast.LENGTH_SHORT,
                            FancyToast.SUCCESS, false).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(WallpaerActivity.this, "Fail to set wallpaper..", Toast.LENGTH_SHORT).show();
                } finally {
                    progressBar.setVisibility(View.GONE);
                }
                return false;
            }
        }).submit();
    }

    private void setLiveWallpaper() {
        // Download logic + Set Service
        progressBar.setVisibility(View.VISIBLE);
        setWallpaperBtn.setEnabled(false);
        setWallpaperBtn.setText("Downloading...");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Use Glide to download file to cache
                File file = Glide.with(getApplicationContext())
                        .asFile()
                        .load(videoUrl)
                        .submit()
                        .get();

                // Copy to internal files dir
                File destFile = new File(getFilesDir(), "live_wallpaper.mp4");
                copyFile(file, destFile);

                // Save path
                SharedPreferences prefs = getSharedPreferences("WALLPAPER_PREFS", MODE_PRIVATE);
                prefs.edit().putString("video_path", destFile.getAbsolutePath()).apply();

                // Trigger update
                Intent intent = new Intent(VideoWallpaperService.VIDEO_PARAMS_CONTROL_ACTION);
                intent.putExtra(VideoWallpaperService.KEY_ACTION, VideoWallpaperService.ACTION_UPDATE_VIDEO);
                sendBroadcast(intent);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    setWallpaperBtn.setEnabled(true);
                    setWallpaperBtn.setText("Set Wallpaper");

                    // Open Wallpaper Chooser
                    Intent wallpaperIntent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                    wallpaperIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            new ComponentName(WallpaerActivity.this, VideoWallpaperService.class));
                    // Fallback for older devices/different ROMs
                    if (wallpaperIntent.resolveActivity(getPackageManager()) == null) {
                        wallpaperIntent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
                    }
                    startActivity(wallpaperIntent);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    setWallpaperBtn.setEnabled(true);
                    setWallpaperBtn.setText("Set Wallpaper");
                    Toast.makeText(WallpaerActivity.this, "Failed to download video", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void copyFile(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source);
                OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
}