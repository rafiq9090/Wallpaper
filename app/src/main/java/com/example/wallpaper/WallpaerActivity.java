package com.example.wallpaper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shashank.sony.fancytoastlib.FancyToast;

public class WallpaerActivity extends AppCompatActivity {

   private ImageView wallpaperIV;
   private Button setWallpaperBtn;
   private String imaUrl;
   WallpaperManager wallpaperManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaer);
        getSupportActionBar().hide();

        wallpaperIV = findViewById(R.id.idIVWallpapers);
        setWallpaperBtn = findViewById(R.id.idBtnSetWallpaper);
        imaUrl = getIntent().getStringExtra("imageUrl");
        Glide.with(this).load(imaUrl).into(wallpaperIV);
        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        setWallpaperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Glide.with(WallpaerActivity.this).asBitmap().load(imaUrl).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(WallpaerActivity.this, "Fail to load image..", Toast.LENGTH_SHORT).show();
                        return false;
                    }


                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            wallpaperManager.setBitmap(resource);
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(WallpaerActivity.this, "Fail to set wallpaper..", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                }).submit();
                FancyToast.makeText(WallpaerActivity.this,"Wallpaper set to home screen.",FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,false).show();
            }
        });

    }
}