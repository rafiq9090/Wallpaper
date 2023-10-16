package com.example.wallpaper;

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

    private ArrayList<String> wallapaperRVArrayList;
    private Context context;

    public WallpaperRVAdapter(ArrayList<String> wallapaperRVArrayList, Context context) {
        this.wallapaperRVArrayList = wallapaperRVArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public WallpaperRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wallpaper_rv_item, parent, false);
        return new WallpaperRVAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperRVAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(context).load(wallapaperRVArrayList.get(position)).into(holder.wallpaperIV);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, WallpaerActivity.class);
                i.putExtra("imageUrl", wallapaperRVArrayList.get(position));
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wallapaperRVArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView wallpaperIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wallpaperIV = itemView.findViewById(R.id.idIVWallpaper);
        }
    }
}
