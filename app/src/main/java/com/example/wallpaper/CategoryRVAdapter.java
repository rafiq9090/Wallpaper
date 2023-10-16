package com.example.wallpaper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CategoryRVAdapter extends RecyclerView.Adapter<CategoryRVAdapter.ViewHoder> {

    private ArrayList<CategoryRVModel> categoryRVModelArrayList;
    private Context context;
    private CategoryOnclickInterface categoryOnclickInterface;

    public CategoryRVAdapter(ArrayList<CategoryRVModel> categoryRVModelArrayList, Context context, CategoryOnclickInterface categoryOnclickInterface) {
        this.categoryRVModelArrayList = categoryRVModelArrayList;
        this.context = context;
        this.categoryOnclickInterface = categoryOnclickInterface;
    }

    @NonNull
    @Override
    public CategoryRVAdapter.ViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_rv_item,parent,false);
        return new CategoryRVAdapter.ViewHoder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryRVAdapter.ViewHoder holder, @SuppressLint("RecyclerView") int position) {
        CategoryRVModel categoryRVModel = categoryRVModelArrayList.get(position);
        holder.categoryTV.setText(categoryRVModel.getCategory());
        Glide.with(context).load(categoryRVModel.getCategoryIVUrl()).into(holder.categoryIV);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryOnclickInterface.onCategoryClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryRVModelArrayList.size();
    }

    public class ViewHoder extends RecyclerView.ViewHolder {

        private TextView categoryTV;
        private ImageView categoryIV;

        public ViewHoder(@NonNull View itemView) {
            super(itemView);

            categoryTV = itemView.findViewById(R.id.idTVCategory);
            categoryIV = itemView.findViewById(R.id.idIVCategory);
        }
    }
    public interface CategoryOnclickInterface{
        void onCategoryClick(int position);
    }
}
