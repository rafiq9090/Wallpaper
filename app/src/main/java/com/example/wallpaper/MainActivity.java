package com.example.wallpaper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements CategoryRVAdapter.CategoryOnclickInterface {

    private EditText searchEdt;
    private ImageView searchIV;

    private SearchView searchView;
    private RecyclerView categoryRV, wallpaperRV;
    private ProgressBar loadingPB;
    private ArrayList<String> wallpaperArraylist;
    private ArrayList<CategoryRVModel> categoryRVModelArrayList;
    private CategoryRVAdapter categoryRVAdapter;
    private WallpaperRVAdapter wallpaperRVAdapter;
    private String searchStr;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();

//        searchEdt = findViewById(R.id.idEdtSearch);
//        searchIV = findViewById(R.id.idIVSearch);
        searchView = findViewById(R.id.searchID);
        categoryRV = findViewById(R.id.idRVCategory);
        wallpaperRV = findViewById(R.id.idRVWallpaper);
        loadingPB = findViewById(R.id.idPBLoading);


        // 563492ad6f917000010000010b5ac71ca53c41379be542bbb219efc4

        wallpaperArraylist = new ArrayList<>();
        categoryRVModelArrayList = new ArrayList<>();

        // category adapter set //

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.HORIZONTAL, false);
        categoryRV.setLayoutManager(linearLayoutManager);
        categoryRVAdapter = new CategoryRVAdapter(categoryRVModelArrayList, this, this::onCategoryClick);
        categoryRV.setAdapter(categoryRVAdapter);

        // wallpaper adapter set//

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        wallpaperRV.setLayoutManager(gridLayoutManager);
        wallpaperRVAdapter = new WallpaperRVAdapter(wallpaperArraylist, this);
        wallpaperRV.setAdapter(wallpaperRVAdapter);

        // function calling category//
        getCategories();
        // function calling wallpaper//
        getWallpapers();

//        searchEdt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                searchStr = searchEdt.getText().toString();
//                if (searchStr.isEmpty()) {
//
//                    //Toast.makeText(MainActivity.this, "Please Enter your search query.", Toast.LENGTH_SHORT).show();
//                } else {
//                    // search function calling //
//                    getWallpapersByCategory(searchStr);
//                }
//            }
//        });
//

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getWallpapersByCategory(query);
              return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                getWallpapersByCategory(newText);
                return false;
            }
        });

//        searchView.setOnSearchClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                searchStr = searchEdt.getText().toString();
//                if (searchStr.isEmpty()) {
//                    Toast.makeText(MainActivity.this, "Please Enter your search query.", Toast.LENGTH_SHORT).show();
//                } else {
//                    // search function calling //
//                    getWallpapersByCategory(searchStr);
//                }
//            }
//        });
//        searchView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    private void getWallpapersByCategory(String category) {
        wallpaperArraylist.clear();
        loadingPB.setVisibility(View.VISIBLE);
        String url = "https://api.pexels.com/v1/search?query=" + category + "&per_page=30&page=1";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    loadingPB.setVisibility(View.GONE);
                    JSONArray photos = response.getJSONArray("photos");
                    for (int i = 0; i < photos.length(); i++) {
                        JSONObject photoObj = photos.getJSONObject(i);
                        String imageUrl = photoObj.getJSONObject("src").getString("portrait");

                        wallpaperArraylist.add(imageUrl);
                    }
                    wallpaperRVAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Fail to get data..", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "563492ad6f917000010000010b5ac71ca53c41379be542bbb219efc4");

                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void getWallpapers() {
        wallpaperArraylist.clear();
        loadingPB.setVisibility(View.VISIBLE);
        String url = "https://api.pexels.com/v1/curated?per_page=30&page=1";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    loadingPB.setVisibility(View.GONE);
                    JSONArray photos = response.getJSONArray("photos");
                    for (int i = 0; i < photos.length(); i++) {
                        JSONObject photoObj = photos.getJSONObject(i);
                        String imageUrl = photoObj.getJSONObject("src").getString("portrait");

                        wallpaperArraylist.add(imageUrl);
                    }
                    wallpaperRVAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Fail to get data..", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "563492ad6f917000010000010b5ac71ca53c41379be542bbb219efc4");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);

    }

    private void getCategories() {
        categoryRVModelArrayList.add(new CategoryRVModel("Coffee","https://images.pexels.com/photos/312418/pexels-photo-312418.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Tea","https://images.pexels.com/photos/905485/pexels-photo-905485.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Beach","https://images.pexels.com/photos/1174732/pexels-photo-1174732.jpeg?auto=compress&amp;cs=tinysrgb&amp;dpr=1&amp;w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Sunset","https://images.pexels.com/photos/87812/pexels-photo-87812.jpeg?auto=compress&amp;cs=tinysrgb&amp;dpr=1&amp;w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Cars", "https://images.pexels.com/photos/2365572/pexels-photo-2365572.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Flowers", "https://images.pexels.com/photos/46216/sunflower-flowers-bright-yellow-46216.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Technology", "https://images.pexels.com/photos/3862634/pexels-photo-3862634.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Programming", "https://images.pexels.com/photos/3987066/pexels-photo-3987066.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Nature", "https://images.pexels.com/photos/15286/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Travel", "https://images.pexels.com/photos/4553618/pexels-photo-4553618.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Architecture", "https://images.pexels.com/photos/3172740/pexels-photo-3172740.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Arts", "https://images.pexels.com/photos/1269968/pexels-photo-1269968.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Music", "https://images.pexels.com/photos/167491/pexels-photo-167491.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Abstract", "https://images.pexels.com/photos/5022849/pexels-photo-5022849.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Depression", "https://images.pexels.com/photos/1134204/pexels-photo-1134204.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Ice", "https://images.pexels.com/photos/4667146/pexels-photo-4667146.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Health", "https://images.pexels.com/photos/235922/pexels-photo-235922.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Food", "https://images.pexels.com/photos/376464/pexels-photo-376464.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Ice cream", "https://images.pexels.com/photos/1625235/pexels-photo-1625235.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("People", "https://images.pexels.com/photos/837358/pexels-photo-837358.jpeg?auto=compress&cs=tinysrgb&w=500"));

        categoryRVAdapter.notifyDataSetChanged();









    }

    @Override
    public void onCategoryClick(int position) {
        String category = categoryRVModelArrayList.get(position).getCategory();
        getWallpapersByCategory(category);
    }

}