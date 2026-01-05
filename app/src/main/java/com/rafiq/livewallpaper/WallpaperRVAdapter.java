package com.rafiq.livewallpaper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class WallpaperRVAdapter extends RecyclerView.Adapter<WallpaperRVAdapter.ViewHolder> {

    private ArrayList<WallpaperModel> wallpaperArrayList;
    private Context context;

    public WallpaperRVAdapter(ArrayList<WallpaperModel> wallpaperArrayList, Context context) {
        this.wallpaperArrayList = wallpaperArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public WallpaperRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wallpaper_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperRVAdapter.ViewHolder holder,
            @SuppressLint("RecyclerView") int position) {
        WallpaperModel model = wallpaperArrayList.get(position);
        Glide.with(context).load(model.getImageUrl()).into(holder.wallpaperIV);

        if (model.isVideo()) {
            holder.videoBadge.setVisibility(View.VISIBLE);
        } else {
            holder.videoBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, WallpaerActivity.class);
                i.putExtra("imageUrl", model.getHighResUrl());
                i.putExtra("isVideo", model.isVideo());
                if (model.isVideo()) {
                    i.putExtra("videoUrl", model.getVideoUrl());
                }
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wallpaperArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView wallpaperIV;
        ImageView videoBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wallpaperIV = itemView.findViewById(R.id.idIVWallpaper);
            videoBadge = itemView.findViewById(R.id.idIVVideoBadge);
        }
    }
}
