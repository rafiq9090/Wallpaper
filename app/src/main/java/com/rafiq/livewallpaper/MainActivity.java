package com.rafiq.livewallpaper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.cardview.widget.CardView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CategoryRVAdapter.CategoryOnclickInterface {

    private SearchView searchView;
    private RecyclerView categoryRV, wallpaperRV;
    private ProgressBar loadingPB;
    private CardView pickGalleryCV;
    private SwitchMaterial liveWallpaperSwitch;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<WallpaperModel> wallpaperArraylist;
    private ArrayList<CategoryRVModel> categoryRVModelArrayList;
    private CategoryRVAdapter categoryRVAdapter;
    private WallpaperRVAdapter wallpaperRVAdapter;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private InterestManager interestManager;
    private String currentQuery = "nature"; // Default query

    private String PEXELS_API_KEY = "563492ad6f917000010000010b5ac71ca53c41379be542bbb219efc4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        searchView = findViewById(R.id.searchID);
        categoryRV = findViewById(R.id.idRVCategory);
        wallpaperRV = findViewById(R.id.idRVWallpaper);
        loadingPB = findViewById(R.id.idPBLoading);
        pickGalleryCV = findViewById(R.id.idCVPickGallery);
        liveWallpaperSwitch = findViewById(R.id.idSwitchLive);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            searchContent(currentQuery);
        });

        interestManager = new InterestManager(this);
        wallpaperArraylist = new ArrayList<>();
        categoryRVModelArrayList = new ArrayList<>();

        setupAdapters();
        setupGalleryLauncher();

        getCategories();
        getInitialContent();

        setupSearchListener();

        pickGalleryCV.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");
            galleryLauncher.launch(intent);
        });

        com.google.android.material.floatingactionbutton.FloatingActionButton fabCreate = findViewById(
                R.id.idFabCreate);
        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StudioActivity.class);
            startActivity(intent);
        });
    }

    private void setupAdapters() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.HORIZONTAL,
                false);
        categoryRV.setLayoutManager(linearLayoutManager);
        categoryRVAdapter = new CategoryRVAdapter(categoryRVModelArrayList, this, this::onCategoryClick);
        categoryRV.setAdapter(categoryRVAdapter);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        wallpaperRV.setLayoutManager(staggeredGridLayoutManager);
        wallpaperRVAdapter = new WallpaperRVAdapter(wallpaperArraylist, this);
        wallpaperRV.setAdapter(wallpaperRVAdapter);
    }

    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedVideoUri = result.getData().getData();
                        if (selectedVideoUri != null) {
                            Intent i = new Intent(MainActivity.this, WallpaerActivity.class);
                            i.putExtra("isVideo", true);
                            i.putExtra("videoUrl", selectedVideoUri.toString());
                            i.putExtra("isLocal", true);
                            startActivity(i);
                        }
                    }
                });
    }

    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    interestManager.addInterest(query);
                    searchContent(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void getInitialContent() {
        // Load interesting content on start
        String query = interestManager.getRecommendationQuery();
        searchContent(query);
    }

    private void searchContent(String query) {
        currentQuery = query;
        if (!swipeRefreshLayout.isRefreshing()) {
            wallpaperArraylist.clear();
            loadingPB.setVisibility(View.VISIBLE);
            wallpaperRVAdapter.notifyDataSetChanged();
        } else {
            wallpaperArraylist.clear();
        }

        boolean isLiveSearch = liveWallpaperSwitch.isChecked();
        if (query.equalsIgnoreCase("Live Wallpapers")) {
            isLiveSearch = true;
            query = "nature"; // Default live query
        } else if (query.equalsIgnoreCase("For You")) {
            query = interestManager.getRecommendationQuery();
            // Decide if we show live or static for 'For You' - let's mix or default to
            // static unless switch is on
        }

        if (isLiveSearch) {
            getLiveWallpapers(query);
        } else {
            getStaticWallpapers(query);
        }
    }

    private void getStaticWallpapers(String query) {
        String url = "https://api.pexels.com/v1/search?query=" + query + "&per_page=30&page=1";

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                loadingPB.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                JSONArray photos = response.getJSONArray("photos");
                for (int i = 0; i < photos.length(); i++) {
                    JSONObject photoObj = photos.getJSONObject(i);
                    String imageUrl = photoObj.getJSONObject("src").getString("portrait");
                    String highResUrl = photoObj.getJSONObject("src").getString("large2x");
                    wallpaperArraylist.add(new WallpaperModel(imageUrl, null, highResUrl, false));
                }
                wallpaperRVAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                loadingPB.setVisibility(View.GONE);
            }
        }, error -> {
            loadingPB.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(MainActivity.this, "Failed to load wallpapers", Toast.LENGTH_SHORT).show();
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

    private void getLiveWallpapers(String query) {
        String url = "https://api.pexels.com/videos/search?query=" + query + "&per_page=15&page=1";

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                loadingPB.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                JSONArray videos = response.getJSONArray("videos");
                for (int i = 0; i < videos.length(); i++) {
                    JSONObject videoObj = videos.getJSONObject(i);
                    String imageUrl = videoObj.getString("image");
                    JSONArray videoFiles = videoObj.getJSONArray("video_files");
                    String videoUrl = null;

                    for (int j = 0; j < videoFiles.length(); j++) {
                        JSONObject file = videoFiles.getJSONObject(j);
                        String quality = file.getString("quality");
                        if (quality.equalsIgnoreCase("hd")) {
                            videoUrl = file.getString("link");
                            break;
                        }
                    }
                    if (videoUrl == null && videoFiles.length() > 0) {
                        videoUrl = videoFiles.getJSONObject(0).getString("link");
                    }

                    if (videoUrl != null) {
                        wallpaperArraylist.add(new WallpaperModel(imageUrl, videoUrl, imageUrl, true));
                    }
                }
                wallpaperRVAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                loadingPB.setVisibility(View.GONE);
            }
        }, error -> {
            loadingPB.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(MainActivity.this, "Failed to load live wallpapers", Toast.LENGTH_SHORT).show();
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
        categoryRVModelArrayList.add(new CategoryRVModel("For You",
                "https://images.pexels.com/photos/346529/pexels-photo-346529.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Live Wallpapers",
                "https://images.pexels.com/photos/2559594/pexels-photo-2559594.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("iPhone 16 Style",
                "https://images.pexels.com/photos/6985132/pexels-photo-6985132.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("iOS 18 Concept",
                "https://images.pexels.com/photos/6687834/pexels-photo-6687834.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Nature",
                "https://images.pexels.com/photos/15286/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Abstract",
                "https://images.pexels.com/photos/5022849/pexels-photo-5022849.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Cars",
                "https://images.pexels.com/photos/2365572/pexels-photo-2365572.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Flowers",
                "https://images.pexels.com/photos/46216/sunflower-flowers-bright-yellow-46216.jpeg?auto=compress&cs=tinysrgb&w=500"));
        categoryRVModelArrayList.add(new CategoryRVModel("Technology",
                "https://images.pexels.com/photos/3862634/pexels-photo-3862634.jpeg?auto=compress&cs=tinysrgb&w=500"));
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

        categoryRVAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCategoryClick(int position) {
        String category = categoryRVModelArrayList.get(position).getCategory();
        // Record interest only for standard categories
        if (!category.equals("For You") && !category.equals("Live Wallpapers")) {
            interestManager.addInterest(category);
        }

        // Custom search mapping for stylistic categories
        if (category.equals("iPhone 16 Style")) {
            searchContent("abstract gradient wallpaper");
        } else if (category.equals("iOS 18 Concept")) {
            searchContent("glassmorphism abstract");
        } else {
            searchContent(category);
        }
    }
}