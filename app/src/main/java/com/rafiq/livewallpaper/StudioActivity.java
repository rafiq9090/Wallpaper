package com.rafiq.livewallpaper;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.CheckBox;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class StudioActivity extends AppCompatActivity {

    // Views
    private ImageView previewIV, toolBg, toolText, toolFont, toolColor, toolDraw;
    private TextView activeTextView; // The currently selected text
    private List<TextView> textViews = new java.util.ArrayList<>();
    private TextView panelTitle;
    private EditText etText;
    private ChipGroup chipGroupType, chipGroupFont;
    private MaterialButton btnGenerate, btnAddText, btnEraser, btnClearDraw, btnDeleteText;
    private Slider sliderBrushSize;
    private FrameLayout previewContainer;
    private View gridOverlay;
    private DrawingView drawingView;
    private ProgressBar progressBar;
    private FloatingActionButton fabSave;
    private LinearLayout layoutBackgroundTools, layoutTextTools, layoutFontTools, layoutColorTools, layoutDrawTools;
    private CardView controlPanel;

    // Logic
    private String PEXELS_API_KEY = "563492ad6f917000010000010b5ac71ca53c41379be542bbb219efc4";
    private ScaleGestureDetector scaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private float dX, dY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studio);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        try {
            initViews();
            setupListeners();
            // Load initial content
            togglePanel(layoutBackgroundTools, "Background Design", toolBg);

            // Initial Text Setup
            TextView initialText = findViewById(R.id.studioTextTV);
            addTextToCanvas(initialText); // Register initial text
            setActiveText(initialText);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        previewIV = findViewById(R.id.studioPreviewIV);
        // Note: studioTextTV is handled via addTextToCanvas now, but we find it to init
        // list
        etText = findViewById(R.id.etStudioText);
        chipGroupType = findViewById(R.id.chipGroupType);
        chipGroupFont = findViewById(R.id.chipGroupFont);
        btnGenerate = findViewById(R.id.btnGenerateBackground);
        btnAddText = findViewById(R.id.btnAddText);
        btnDeleteText = findViewById(R.id.btnDeleteText);
        previewContainer = findViewById(R.id.previewContainer);
        progressBar = findViewById(R.id.studioLoadingPB);
        fabSave = findViewById(R.id.fabSave);
        panelTitle = findViewById(R.id.panelTitle);
        controlPanel = findViewById(R.id.controlPanel);
        gridOverlay = findViewById(R.id.gridOverlay);
        drawingView = findViewById(R.id.drawingView);

        // Draw Tools
        btnEraser = findViewById(R.id.btnEraser);
        btnClearDraw = findViewById(R.id.btnClearDraw);
        sliderBrushSize = findViewById(R.id.sliderBrushSize);

        // Sidebar Tools (Right)
        toolBg = findViewById(R.id.toolBg);
        toolText = findViewById(R.id.toolText);
        toolFont = findViewById(R.id.toolFont);
        toolColor = findViewById(R.id.toolColor);
        toolDraw = findViewById(R.id.toolDraw);

        // Layouts
        layoutBackgroundTools = findViewById(R.id.layoutBackgroundTools);
        layoutTextTools = findViewById(R.id.layoutTextTools);
        layoutFontTools = findViewById(R.id.layoutFontTools);
        layoutColorTools = findViewById(R.id.layoutColorTools);
        layoutDrawTools = findViewById(R.id.layoutDrawTools);

        // Setup helper listeners...
        setupColorListener(R.id.bgBlack, 0xFF000000);
        setupColorListener(R.id.bgWhite, 0xFFFFFFFF);
        setupColorListener(R.id.bgGray, 0xFF9E9E9E);
        setupColorListener(R.id.bgBlue, 0xFF2196F3);
        setupColorListener(R.id.bgTeal, 0xFF009688);
        setupColorListener(R.id.bgGreen, 0xFF4CAF50);
        setupColorListener(R.id.bgLime, 0xFFCDDC39);
        setupColorListener(R.id.bgYellow, 0xFFFFEB3B);
        setupColorListener(R.id.bgAmber, 0xFFFFC107);
        setupColorListener(R.id.bgOrange, 0xFFFF5722);
        setupColorListener(R.id.bgRed, 0xFFF44336);
        setupColorListener(R.id.bgPink, 0xFFE91E63);
        setupColorListener(R.id.bgPurple, 0xFF9C27B0);

        // Gradients
        setupGradientListener(R.id.grad1, 0xFF2196F3, 0xFF9C27B0);
        setupGradientListener(R.id.grad2, 0xFFFF9800, 0xFFE91E63);
        setupGradientListener(R.id.grad3, 0xFF009688, 0xFF8BC34A);
        setupGradientListener(R.id.grad4, 0xFF3F51B5, 0xFF00BCD4);
        setupGradientListener(R.id.grad5, 0xFF673AB7, 0xFFE040FB);
        setupGradientListener(R.id.grad6, 0xFF000000, 0xFF434343);

        // Text Colors (Existing)
        findViewById(R.id.colorWhite).setOnClickListener(v -> {
            if (activeTextView != null)
                activeTextView.setTextColor(0xFFFFFFFF);
        });
        findViewById(R.id.colorYellow).setOnClickListener(v -> {
            if (activeTextView != null)
                activeTextView.setTextColor(0xFFFFEB3B);
        });
        findViewById(R.id.colorCyan).setOnClickListener(v -> {
            if (activeTextView != null)
                activeTextView.setTextColor(0xFF00BCD4);
        });
        findViewById(R.id.colorPink).setOnClickListener(v -> {
            if (activeTextView != null)
                activeTextView.setTextColor(0xFFE91E63);
        });
        findViewById(R.id.colorNeonGreen).setOnClickListener(v -> {
            if (activeTextView != null)
                activeTextView.setTextColor(0xFF39FF14);
        });
        findViewById(R.id.colorOrange).setOnClickListener(v -> {
            if (activeTextView != null)
                activeTextView.setTextColor(0xFFFF5722);
        });

        // Shortcuts for brevity, assuming existing colors are set up in setupListeners
        // or implicitly

        // Font Selection
        chipGroupFont.setOnCheckedChangeListener((group, checkedId) -> applyTextStyle());

        Slider sliderRotate = findViewById(R.id.sliderRotate);
        sliderRotate.addOnChangeListener((slider, value, fromUser) -> {
            if (activeTextView != null)
                activeTextView.setRotation(value);
        });

        CheckBox cbBold = findViewById(R.id.cbBold);
        if (cbBold != null)
            cbBold.setOnCheckedChangeListener((bv, isChecked) -> applyTextStyle());

        CheckBox cbUnderline = findViewById(R.id.cbItalic);
        if (cbUnderline != null) {
            cbUnderline.setText("Underline");
            cbUnderline.setOnCheckedChangeListener((bv, isChecked) -> {
                if (activeTextView != null) {
                    if (isChecked)
                        activeTextView.setPaintFlags(activeTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    else
                        activeTextView.setPaintFlags(activeTextView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                }
            });
        }
    }

    private void applyTextStyle() {
        if (activeTextView == null)
            return;
        int fontId = chipGroupFont.getCheckedChipId();
        CheckBox cbBold = findViewById(R.id.cbBold);
        boolean isBold = cbBold != null && cbBold.isChecked();

        Typeface baseTypeface = Typeface.DEFAULT;
        if (fontId == R.id.fontModern)
            baseTypeface = Typeface.SANS_SERIF;
        else if (fontId == R.id.fontSerif)
            baseTypeface = Typeface.SERIF;
        else if (fontId == R.id.fontMono)
            baseTypeface = Typeface.MONOSPACE;
        else if (fontId == R.id.fontCursive)
            baseTypeface = Typeface.create("cursive", Typeface.NORMAL);
        else if (fontId == R.id.fontNeon)
            baseTypeface = Typeface.DEFAULT_BOLD;

        if (fontId == R.id.fontNeon) {
            activeTextView.setTypeface(baseTypeface);
            activeTextView.setShadowLayer(15, 0, 0, activeTextView.getCurrentTextColor());
        } else {
            activeTextView.setShadowLayer(0, 0, 0, 0); // Clear shadow for others
            if (isBold) {
                activeTextView.setTypeface(Typeface.create(baseTypeface, Typeface.BOLD));
            } else {
                activeTextView.setTypeface(baseTypeface);
            }
        }
    }

    // ... setupColorListener methods kept ...

    private void setActiveText(TextView tv) {
        // Deselect previous active text if any
        if (activeTextView != null) {
            activeTextView.setBackground(null); // Clear background
        }
        this.activeTextView = tv;
        etText.setText(tv.getText());
        // Highlight the active text
        activeTextView.setBackgroundResource(R.drawable.text_selection_border); // Assuming you have a drawable for
                                                                                // selection
        // Could update font/color UI to match reading from tv
    }

    private void addTextToCanvas(TextView tv) {
        if (!textViews.contains(tv)) {
            textViews.add(tv);
            if (tv.getParent() == null) {
                previewContainer.addView(tv);
            }
        }

        ScaleGestureDetector scaleDetector = new ScaleGestureDetector(this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        float scale = tv.getScaleX() * detector.getScaleFactor();
                        scale = Math.max(0.5f, Math.min(scale, 5.0f));
                        tv.setScaleX(scale);
                        tv.setScaleY(scale);
                        return true;
                    }
                });

        tv.setOnTouchListener((v, event) -> {
            setActiveText((TextView) v);
            scaleDetector.onTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    gridOverlay.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!scaleDetector.isInProgress()) {
                        v.animate().x(event.getRawX() + dX).y(event.getRawY() + dY).setDuration(0).start();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    gridOverlay.setVisibility(View.GONE);
                    break;
            }
            return true;
        });
    }

    private void setupColorListener(int viewId, int color) {
        View v = findViewById(viewId);
        if (v != null)
            v.setOnClickListener(val -> setSolidBackground(color));
    }

    private void setupGradientListener(int viewId, int startColor, int endColor) {
        View v = findViewById(viewId);
        if (v != null) {
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TL_BR, new int[] { startColor, endColor });
            gd.setCornerRadius(16f);
            v.setBackground(gd); // Set preview on the tool icon

            v.setOnClickListener(val -> {
                previewIV.setImageDrawable(null);
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                        new int[] { startColor, endColor });
                previewIV.setBackground(bg);
            });
        }
    }

    private void setSolidBackground(int color) {
        previewIV.setImageDrawable(null);
        previewIV.setBackgroundColor(color);
    }

    private void setupListeners() {
        // Sidebar Navigation
        toolBg.setOnClickListener(v -> togglePanel(layoutBackgroundTools, "Background Design", toolBg));
        toolText.setOnClickListener(v -> togglePanel(layoutTextTools, "Edit Text", toolText));
        toolFont.setOnClickListener(v -> togglePanel(layoutFontTools, "Font Style", toolFont));
        toolColor.setOnClickListener(v -> togglePanel(layoutColorTools, "Color Palette", toolColor));
        if (toolDraw != null)
            toolDraw.setOnClickListener(v -> togglePanel(layoutDrawTools, "Draw Mode", toolDraw));

        // Text Update
        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (activeTextView != null)
                    activeTextView.setText(s.toString().isEmpty() ? "Tap to Edit" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Add Text Button
        if (btnAddText != null) {
            btnAddText.setOnClickListener(v -> {
                TextView newTv = new TextView(this);
                newTv.setText("New Text");
                newTv.setTextColor(0xFFFFFFFF);
                newTv.setTextSize(32);
                newTv.setTypeface(null, Typeface.BOLD);
                newTv.setShadowLayer(4, 2, 2, 0x80000000);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = android.view.Gravity.CENTER;
                newTv.setLayoutParams(params);
                // previewContainer.addView(newTv); // Let addTextToCanvas handle this safely
                addTextToCanvas(newTv);
                setActiveText(newTv);
                setActiveText(newTv);
            });
        }

        // Delete Text Button
        if (btnDeleteText != null) {
            btnDeleteText.setOnClickListener(v -> {
                if (activeTextView != null) {
                    previewContainer.removeView(activeTextView);
                    textViews.remove(activeTextView);
                    activeTextView = null;
                    etText.setText("");
                } else {
                    Toast.makeText(this, "Select text to delete", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Drawing Controls
        if (sliderBrushSize != null) {
            sliderBrushSize.addOnChangeListener((slider, value, fromUser) -> {
                if (drawingView != null)
                    drawingView.setBrushSize(value);
            });
        }
        if (btnEraser != null)
            btnEraser.setOnClickListener(v -> {
                if (drawingView != null)
                    drawingView.setErase(true);
            });
        if (btnClearDraw != null)
            btnClearDraw.setOnClickListener(v -> {
                if (drawingView != null)
                    drawingView.startNew();
            });
        // For now, selecting a color resets erase.
        setupDrawColor(R.id.drawBlack, 0xFF000000);
        setupDrawColor(R.id.drawWhite, 0xFFFFFFFF);
        setupDrawColor(R.id.drawRed, 0xFFF44336);
        setupDrawColor(R.id.drawGreen, 0xFF4CAF50);
        setupDrawColor(R.id.drawBlue, 0xFF2196F3);
        setupDrawColor(R.id.drawYellow, 0xFFFFEB3B);

        // Generate BG
        btnGenerate.setOnClickListener(v -> generateBackground());

        // Save / Set Wallpaper
        fabSave.setOnClickListener(v -> showSetWallpaperDialog());
    }

    private void setupDrawColor(int viewId, int color) {
        View v = findViewById(viewId);
        if (v != null) {
            v.setOnClickListener(click -> {
                if (drawingView != null) {
                    drawingView.setColor(color);
                    drawingView.setErase(false);
                }
            });
        }
    }

    private void togglePanel(LinearLayout visibleLayout, String title, ImageView activeTool) {
        layoutBackgroundTools.setVisibility(View.GONE);
        layoutTextTools.setVisibility(View.GONE);
        layoutFontTools.setVisibility(View.GONE);
        layoutColorTools.setVisibility(View.GONE);
        if (layoutDrawTools != null)
            layoutDrawTools.setVisibility(View.GONE);

        visibleLayout.setVisibility(View.VISIBLE);
        panelTitle.setText(title);

        // Enable DrawingView only when Drawing Tools are active
        if (drawingView != null) {
            drawingView.setEnabled(visibleLayout == layoutDrawTools);
        }
    }

    private void generateBackground() {
        Chip typeChip = findViewById(chipGroupType.getCheckedChipId());
        String type = (typeChip != null) ? typeChip.getText().toString() : "Abstract";
        String query = type + " wallpaper 4k";
        fetchPexelsImage(query);
    }

    private void fetchPexelsImage(String query) {
        progressBar.setVisibility(View.VISIBLE);
        int randomPage = new Random().nextInt(10) + 1;
        String url = "https://api.pexels.com/v1/search?query=" + query + "&per_page=1&page=" + randomPage;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                progressBar.setVisibility(View.GONE);
                JSONArray photos = response.getJSONArray("photos");
                if (photos.length() > 0) {
                    JSONObject photoObj = photos.getJSONObject(0);
                    // Fetch highest resolution
                    String imageUrl = photoObj.getJSONObject("src").getString("original");
                    Glide.with(this).load(imageUrl).into(previewIV);
                } else {
                    Toast.makeText(this, "No images found.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
            }
        }, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load.", Toast.LENGTH_SHORT).show();
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

    private void showSetWallpaperDialog() {
        // Temporarily hide selection border
        if (activeTextView != null)
            activeTextView.setBackground(null);

        Bitmap bitmap = getBitmapFromView(previewContainer);

        // Restore selection border
        if (activeTextView != null)
            activeTextView.setBackgroundResource(R.drawable.text_selection_border);

        String[] options = { "Home Screen", "Lock Screen", "Both Screens" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Wallpaper");
        builder.setItems(options, (dialog, which) -> {
            int flags = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                switch (which) {
                    case 0:
                        flags = WallpaperManager.FLAG_SYSTEM;
                        break;
                    case 1:
                        flags = WallpaperManager.FLAG_LOCK;
                        break;
                    case 2:
                        flags = WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK;
                        break;
                }
                setWallpaper(bitmap, flags);
            } else {
                setWallpaper(bitmap, WallpaperManager.FLAG_SYSTEM);
            }
        });
        builder.show();
    }

    private void setWallpaper(Bitmap bitmap, int flags) {
        WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                manager.setBitmap(bitmap, null, true, flags);
            } else {
                manager.setBitmap(bitmap);
            }
            FancyToast.makeText(this, "Wallpaper Updated! 🎨", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false)
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
