package com.example.project;
import androidx.core.view.WindowCompat;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
// Removed unused StringRequest import
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit; // Import TimeUnit

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_LOGIN_TIMESTAMP = "loginTimestamp";
    private static final long SESSION_TIMEOUT_MS = TimeUnit.HOURS.toMillis(1);

    private RecyclerView ordersRecyclerView;
    private MealAdapter mealAdapter;
    private List<Meal> mealList;
    private List<Meal> originalMealList;
    private RequestQueue requestQueue;
    private EditText searchBar;
    private ImageView filterButton;
    private BottomNavigationView bottomNavigationView;
    private List<Tag> allTags;
    private boolean[] selectedTags;
    private String[] tagNames;
    private String currentQuery = "";
    private String currentTagFilter = "";
    private String currentPriceFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        long loginTimestamp = prefs.getLong(KEY_LOGIN_TIMESTAMP, -1);
        long currentTime = System.currentTimeMillis();

        // Check if timestamp exists and is within the timeout period
        if (loginTimestamp == -1 || (currentTime - loginTimestamp > SESSION_TIMEOUT_MS)) {
            // Session expired or user not logged in
            // Clear any potentially stale timestamp
            prefs.edit().remove(KEY_LOGIN_TIMESTAMP).apply();

            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
        } else {
            // Session is valid, proceed to load MainActivity
            setContentView(R.layout.activity_main);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            initializeViews();
            setupRecyclerView();
            setupSearchBar();
            setupFilterButton();
            setupBottomNavigation();

            requestQueue = Volley.newRequestQueue(this);
            fetchTags();
            fetchMeals();
        }
    }

    protected void onResume() {
        super.onResume();
        fetchMeals();
    }

    private void initializeViews() {
        ordersRecyclerView = findViewById(R.id.orders_recycler_view);
        searchBar = findViewById(R.id.search_bar);
        filterButton = findViewById(R.id.filter_button);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        allTags = new ArrayList<>();
        mealList = new ArrayList<>();
        originalMealList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mealAdapter = new MealAdapter(mealList);
        ordersRecyclerView.setAdapter(mealAdapter);
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterButton() {
        filterButton.setOnClickListener(v -> {
            // Ensure tags are loaded before showing dialog
            if (tagNames != null && tagNames.length > 0) {
                showFilterDialog();
            } else {
                Toast.makeText(MainActivity.this, "Loading filters...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_orders);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_orders) {
                return true;
            } else if (id == R.id.navigation_postings) {
                Intent intent = new Intent(MainActivity.this, PostingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();

                return true;
            } else if (id == R.id.navigation_dashboard) {
                SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

                int userId = prefs.getInt("user_id", -1);

                if (userId != -1) {
                    Intent intent = new Intent(MainActivity.this, UserDashboardActivity.class);
                    intent.putExtra("USER_ID", userId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();

                    return true;
                } else {
                    Toast.makeText(MainActivity.this, "User ID not found in preferences.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            return false;
        });
    }

    // --- Data Fetching ---
    private void fetchMeals() {
        String url = "http://10.0.2.2/Soufra_Share/meals.php?action=getAllWithUserDetails";
        Log.d("FetchMeals", "Fetching all meals from: " + url); // Log URL
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("FetchMeals", "Received response: " + response.length() + " items");
                    try {
                        originalMealList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject mealObject = response.getJSONObject(i);
                            Meal meal = parseMeal(mealObject);
                            originalMealList.add(meal);
                        }
                        java.util.Collections.sort(originalMealList);


                        applyFilters();
                        Log.d("FetchMeals", "Finished parsing and sorting meals. Original list size: " + originalMealList.size());
                    } catch (JSONException e) {
                        Log.e("FetchMeals", "JSON parsing error", e);
                        Toast.makeText(MainActivity.this, "Error parsing meal data", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e("FetchMeals", "Volley error fetching meals", error);
            Toast.makeText(MainActivity.this, "Error fetching meals: " + error.getMessage(), Toast.LENGTH_LONG).show();
        });
        requestQueue.add(request);
    }

    private void fetchTags() {
        String url = "http://10.0.2.2/Soufra_Share/tags.php";
        Log.d("FetchTags", "Fetching tags from: " + url);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("FetchTags", "Received response: " + response.length() + " tags");
                    try {
                        allTags.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject tagObject = response.getJSONObject(i);
                            allTags.add(new Tag(tagObject.getInt("tag_id"), tagObject.getString("name")));
                        }
                        tagNames = new String[allTags.size()];
                        for (int i = 0; i < allTags.size(); i++) {
                            tagNames[i] = allTags.get(i).getName();
                        }
                        selectedTags = new boolean[allTags.size()];
                        Log.d("FetchTags", "Finished parsing tags. Count: " + allTags.size());
                    } catch (JSONException e) {
                        Log.e("FetchTags", "JSON parsing error", e);
                        Toast.makeText(MainActivity.this, "Error parsing tags", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e("FetchTags", "Volley error fetching tags", error);
            Toast.makeText(MainActivity.this, "Error fetching tags: " + error.getMessage(), Toast.LENGTH_LONG).show();
        });
        requestQueue.add(request);
    }

    // --- Filtering Logic ---
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter By");

        View filterView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        EditText minPriceEditText = filterView.findViewById(R.id.min_price);
        EditText maxPriceEditText = filterView.findViewById(R.id.max_price);

        builder.setView(filterView);


        parseAndSetPriceFilter(minPriceEditText, maxPriceEditText);
        boolean[] dialogSelectedTags = selectedTags.clone();
        builder.setMultiChoiceItems(tagNames, dialogSelectedTags, (dialog, which, isChecked) -> {
            dialogSelectedTags[which] = isChecked;
        });

        builder.setPositiveButton("Apply", (dialog, which) -> {
            selectedTags = dialogSelectedTags;

            StringBuilder selectedTagsString = new StringBuilder();
            for (int i = 0; i < selectedTags.length; i++) {
                if (selectedTags[i]) {
                    if (selectedTagsString.length() > 0) {
                        selectedTagsString.append(",");
                    }
                    selectedTagsString.append(allTags.get(i).getTagId());
                }
            }
            currentTagFilter = selectedTagsString.toString();
            String minPrice = minPriceEditText.getText().toString().trim();
            String maxPrice = maxPriceEditText.getText().toString().trim();
            updateCurrentPriceFilter(minPrice, maxPrice);

            applyFilters();
        });
        builder.setNegativeButton("Cancel", null);
        builder.setNeutralButton("Clear Filters", (dialog, which) -> {
            currentQuery = "";
            currentTagFilter = "";
            currentPriceFilter = "";
            searchBar.setText("");
            selectedTags = new boolean[allTags.size()];
            applyFilters();
            Toast.makeText(MainActivity.this, "Filters Cleared", Toast.LENGTH_SHORT).show();
        });


        builder.show();
    }

    private void parseAndSetPriceFilter(EditText minPriceEditText, EditText maxPriceEditText) {
        minPriceEditText.setText("");
        maxPriceEditText.setText("");

        if (currentPriceFilter == null || currentPriceFilter.isEmpty()) {
            return;
        }

        try {
            if (currentPriceFilter.startsWith("Under $")) {
                maxPriceEditText.setText(currentPriceFilter.substring(7).trim());
            } else if (currentPriceFilter.startsWith("Over $")) {
                minPriceEditText.setText(currentPriceFilter.substring(6).trim());
            } else if (currentPriceFilter.contains("-")) {
                String[] prices = currentPriceFilter.split("-");
                if (prices.length == 2) {
                    minPriceEditText.setText(prices[0].trim());
                    maxPriceEditText.setText(prices[1].trim());
                }
            }
        } catch (Exception e) {
            Log.e("FilterDialog", "Error parsing existing price filter", e);
            currentPriceFilter = "";
        }
    }

    private void updateCurrentPriceFilter(String minPrice, String maxPrice) {
        boolean hasMin = !minPrice.isEmpty();
        boolean hasMax = !maxPrice.isEmpty();

        if (hasMin && hasMax) {
            try {
                Double.parseDouble(minPrice);
                Double.parseDouble(maxPrice);
                currentPriceFilter = minPrice + "-" + maxPrice;
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format in price", Toast.LENGTH_SHORT).show();
                currentPriceFilter = "";
            }
        } else if (hasMin) {
            try {
                Double.parseDouble(minPrice);
                currentPriceFilter = "Over $" + minPrice;
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format in min price", Toast.LENGTH_SHORT).show();
                currentPriceFilter = "";
            }
        } else if (hasMax) {
            try {
                Double.parseDouble(maxPrice);
                currentPriceFilter = "Under $" + maxPrice;
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format in max price", Toast.LENGTH_SHORT).show();
                currentPriceFilter = "";
            }
        } else {
            currentPriceFilter = "";
        }
        Log.d("FilterDialog", "Updated currentPriceFilter: " + currentPriceFilter);
    }


    private void applyFilters() {
        Map<String, String> params = new HashMap<>();
        boolean hasFilter = false;
        String baseUrl = "http://10.0.2.2/Soufra_Share/meals.php";
        String action = "getAllWithUserDetails";

        if (!currentQuery.isEmpty() || !currentTagFilter.isEmpty() || !currentPriceFilter.isEmpty()) {
            action = "filter";
            hasFilter = true;
            Log.d("ApplyFilters", "Applying filters: Query='" + currentQuery + "', Tags='" + currentTagFilter + "', Price='" + currentPriceFilter + "'");

            if (!currentQuery.isEmpty()) {
                params.put("query", currentQuery);
            }
            if (!currentTagFilter.isEmpty()) {
                params.put("tags", currentTagFilter);
            }
            if (!currentPriceFilter.isEmpty()) {
                try {
                    if (currentPriceFilter.startsWith("Under $")) {
                        params.put("maxPrice", currentPriceFilter.substring(7).trim());
                    } else if (currentPriceFilter.startsWith("Over $")) {
                        params.put("minPrice", currentPriceFilter.substring(6).trim());
                    } else if (currentPriceFilter.contains("-")) {
                        String[] prices = currentPriceFilter.split("-");
                        if (prices.length == 2) {
                            params.put("minPrice", String.valueOf(Double.parseDouble(prices[0].trim())));
                            params.put("maxPrice", String.valueOf(Double.parseDouble(prices[1].trim())));
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.e("ApplyFilters", "Invalid price range format during filter application: " + currentPriceFilter, e);
                    Toast.makeText(this, "Invalid price filter format, ignoring price.", Toast.LENGTH_SHORT).show();
                    params.remove("minPrice");
                    params.remove("maxPrice");
                }
            }
        } else {
            Log.d("ApplyFilters", "No filters applied, fetching all meals.");
        }

        // Build URL with parameters
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?action=").append(action);
        if (hasFilter) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                try {
                    // Basic URL encoding for parameter values (safer for Volley)
                    urlBuilder.append("&").append(entry.getKey()).append("=").append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (java.io.UnsupportedEncodingException e) {
                    Log.e("ApplyFilters", "URL Encoding failed", e);
                }
            }
        }

        String finalUrl = urlBuilder.toString();
        Log.d("ApplyFilters", "Requesting URL: " + finalUrl);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, finalUrl, null,
                response -> {
                    Log.d("ApplyFilters", "Received filtered response: " + response.length() + " items");
                    try {
                        mealList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject mealObject = response.getJSONObject(i);
                            mealList.add(parseMeal(mealObject));
                        }
                        java.util.Collections.sort(mealList);
                        mealAdapter.notifyDataSetChanged();
                        Log.d("ApplyFilters", "Adapter notified. Displaying " + mealList.size() + " meals.");
                    } catch (JSONException e) {
                        Log.e("ApplyFilters", "JSON parsing error on filtered results", e);
                        Toast.makeText(MainActivity.this, "Error parsing filtered meals", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e("ApplyFilters", "Volley error fetching filtered meals", error);
            Toast.makeText(MainActivity.this, "Error fetching filtered meals: " + error.getMessage(), Toast.LENGTH_LONG).show();
            mealList.clear();
            mealAdapter.notifyDataSetChanged();
        });
        requestQueue.add(request);
    }


    private Meal parseMeal(JSONObject mealObject) throws JSONException {
        Meal meal = new Meal(
                mealObject.optInt("meal_id"),
                mealObject.optInt("user_id"),
                mealObject.optString("name", "N/A"),
                mealObject.optDouble("price", 0.0),
                mealObject.optInt("quantity", 0),
                mealObject.optString("location", ""),
                mealObject.optInt("delivery_option", -1),
                mealObject.optString("description", ""),
                mealObject.optString("image_paths", ""),
                mealObject.optString("created_at", ""),
                mealObject.optString("username", "Unknown User"),
                mealObject.optString("profile_picture", ""),
                mealObject.optDouble("rating", 0.0)
        );
        meal.setCreatedAt(mealObject.optString("created_at", ""));
        return meal;
    }


    private static class Tag {
        private final int tagId;
        private final String name;

        public Tag(int tagId, String name) {
            this.tagId = tagId;
            this.name = name;
        }

        public int getTagId() {
            return tagId;
        }

        public String getName() {
            return name;
        }

        @NonNull
        @Override
        public String toString() {
            return name + " (ID: " + tagId + ")";
        }
    }
}