package com.example.wallpaper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CategoryRVAdapter.CategoryOnclickInterface {

    private SearchView searchView;
    private RecyclerView categoryRV, wallpaperRV;
    private ProgressBar loadingPB;
    private ArrayList<WallpaperModel> wallpaperArraylist;
    private ArrayList<CategoryRVModel> categoryRVModelArrayList;
    private CategoryRVAdapter categoryRVAdapter;
    private WallpaperRVAdapter wallpaperRVAdapter;
    private String PEXELS_API_KEY = "563492ad6f917000010000010b5ac71ca53c41379be542bbb219efc4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide support action bar if using our own toolbar or full screen
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        searchView = findViewById(R.id.searchID);
        categoryRV = findViewById(R.id.idRVCategory);
        wallpaperRV = findViewById(R.id.idRVWallpaper);
        loadingPB = findViewById(R.id.idPBLoading);

        wallpaperArraylist = new ArrayList<>();
        categoryRVModelArrayList = new ArrayList<>();

        // Category Adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.HORIZONTAL,
                false);
        categoryRV.setLayoutManager(linearLayoutManager);
        categoryRVAdapter = new CategoryRVAdapter(categoryRVModelArrayList, this, this::onCategoryClick);
        categoryRV.setAdapter(categoryRVAdapter);

        // Wallpaper Adapter
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        wallpaperRV.setLayoutManager(gridLayoutManager);
        wallpaperRVAdapter = new WallpaperRVAdapter(wallpaperArraylist, this);
        wallpaperRV.setAdapter(wallpaperRVAdapter);

        getCategories();
        getWallpapers(); // Initial load

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getWallpapersByCategory(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void getWallpapersByCategory(String category) {
        wallpaperArraylist.clear();
        loadingPB.setVisibility(View.VISIBLE);

        if (category.equalsIgnoreCase("Live Wallpapers")) {
            getLiveWallpapers();
            return;
        }

        String url = "https://api.pexels.com/v1/search?query=" + category + "&per_page=30&page=1";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            loadingPB.setVisibility(View.GONE);
                            JSONArray photos = response.getJSONArray("photos");
                            for (int i = 0; i < photos.length(); i++) {
                                JSONObject photoObj = photos.getJSONObject(i);
                                String imageUrl = photoObj.getJSONObject("src").getString("portrait");
                                wallpaperArraylist.add(new WallpaperModel(imageUrl, null, false));
                            }
                            wallpaperRVAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                            loadingPB.setVisibility(View.GONE);
                        }
                    }
                }, error -> {
                    loadingPB.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Fail to get data..", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", PEXELS_API_KEY);
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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            loadingPB.setVisibility(View.GONE);
                            JSONArray photos = response.getJSONArray("photos");
                            for (int i = 0; i < photos.length(); i++) {
                                JSONObject photoObj = photos.getJSONObject(i);
                                String imageUrl = photoObj.getJSONObject("src").getString("portrait");
                                wallpaperArraylist.add(new WallpaperModel(imageUrl, null, false));
                            }
                            wallpaperRVAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                            loadingPB.setVisibility(View.GONE);
                        }
                    }
                }, error -> {
                    loadingPB.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Fail to get data..", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", PEXELS_API_KEY);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void getLiveWallpapers() {
        // Fetch videos from Pexels (using 'nature' as a default query for live
        // wallpapers or similar)
        // You could also add specific live wallpaper categories
        String url = "https://api.pexels.com/videos/search?query=nature&per_page=15&page=1";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            loadingPB.setVisibility(View.GONE);
                            JSONArray videos = response.getJSONArray("videos");
                            for (int i = 0; i < videos.length(); i++) {
                                JSONObject videoObj = videos.getJSONObject(i);
                                String imageUrl = videoObj.getString("image"); // Thumbnail

                                JSONArray videoFiles = videoObj.getJSONArray("video_files");
                                String videoUrl = null;

                                // Find a suitable video file (e.g., HD)
                                for (int j = 0; j < videoFiles.length(); j++) {
                                    JSONObject file = videoFiles.getJSONObject(j);
                                    String quality = file.getString("quality");
                                    if (quality.equalsIgnoreCase("hd")) {
                                        videoUrl = file.getString("link");
                                        break;
                                    }
                                }
                                // Fallback if no HD found
                                if (videoUrl == null && videoFiles.length() > 0) {
                                    videoUrl = videoFiles.getJSONObject(0).getString("link");
                                }

                                if (videoUrl != null) {
                                    wallpaperArraylist.add(new WallpaperModel(imageUrl, videoUrl, true));
                                }
                            }
                            wallpaperRVAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                            loadingPB.setVisibility(View.GONE);
                        }
                    }
                }, error -> {
                    loadingPB.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Fail to get live wallpapers..", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", PEXELS_API_KEY);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void getCategories() {
        // Added Live Wallpapers category at the top
        categoryRVModelArrayList.add(new CategoryRVModel("Live Wallpapers",
                "https://images.pexels.com/photos/2559594/pexels-photo-2559594.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Abstract",
                "https://images.pexels.com/photos/5022849/pexels-photo-5022849.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Nature",
                "https://images.pexels.com/photos/15286/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Technology",
                "https://images.pexels.com/photos/3862634/pexels-photo-3862634.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Cars",
                "https://images.pexels.com/photos/2365572/pexels-photo-2365572.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Flowers",
                "https://images.pexels.com/photos/46216/sunflower-flowers-bright-yellow-46216.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Programming",
                "https://images.pexels.com/photos/3987066/pexels-photo-3987066.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Travel",
                "https://images.pexels.com/photos/4553618/pexels-photo-4553618.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Architecture",
                "https://images.pexels.com/photos/3172740/pexels-photo-3172740.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Arts",
                "https://images.pexels.com/photos/1269968/pexels-photo-1269968.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Music",
                "https://images.pexels.com/photos/167491/pexels-photo-167491.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Coffee",
                "https://images.pexels.com/photos/312418/pexels-photo-312418.jpeg?auto=compress&cs=tinysrgb&w=500"));

        categoryRVAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCategoryClick(int position) {
        String category = categoryRVModelArrayList.get(position).getCategory();
        getWallpapersByCategory(category);
    }
}