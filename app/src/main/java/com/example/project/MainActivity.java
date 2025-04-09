package com.example.project;

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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
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
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

        if (!isLoggedIn) {
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_main);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            ordersRecyclerView = findViewById(R.id.orders_recycler_view);
            ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mealList = new ArrayList<>();
            originalMealList = new ArrayList<>();
            mealAdapter = new MealAdapter(mealList);
            ordersRecyclerView.setAdapter(mealAdapter);

            searchBar = findViewById(R.id.search_bar);
            filterButton = findViewById(R.id.filter_button);
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            allTags = new ArrayList<>();

            requestQueue = Volley.newRequestQueue(this);
            fetchMeals();
            fetchTags(); // Fetch tags for filtering

            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s.toString();
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            filterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFilterDialog();
                }
            });

            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.navigation_orders) {
                        return true;
                    } else if (id == R.id.navigation_postings) {
                        Intent intent = new Intent(MainActivity.this, PostingsActivity.class);
                        startActivity(intent);
                        return true;
                    } else if (id == R.id.navigation_dashboard) {
                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void fetchMeals() {
        String url = "http://10.0.2.2/Soufra_Share/meals.php?action=getAllWithUserDetails";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            mealList.clear();
                            originalMealList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject mealObject = response.getJSONObject(i);
                                Meal meal = new Meal(
                                        mealObject.getInt("meal_id"),
                                        mealObject.getInt("user_id"),
                                        mealObject.getString("name"),
                                        mealObject.getDouble("price"),
                                        mealObject.getInt("quantity"),
                                        mealObject.getString("location"),
                                        mealObject.getInt("delivery_option"),
                                        mealObject.getString("description"),
                                        mealObject.getString("image_paths"),
                                        mealObject.getString("created_at"),
                                        mealObject.getString("username"),
                                        mealObject.getString("profile_picture"),
                                        mealObject.getDouble("rating")
                                );
                                mealList.add(meal);
                                originalMealList.add(meal);
                            }
                            applyFilters(); // Apply current filters after fetching new data
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MainActivity.this, "Error fetching meals", Toast.LENGTH_SHORT).show();
                Log.e("Volley Error", error.toString());
            }
        });
        requestQueue.add(request);
    }

    private void fetchTags() {
        String url = "http://10.0.2.2/Soufra_Share/tags.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error parsing tags", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MainActivity.this, "Error fetching tags", Toast.LENGTH_SHORT).show();
                Log.e("Volley Error", error.toString());
            }
        });
        requestQueue.add(request);
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter By");

        View filterView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        EditText minPriceEditText = filterView.findViewById(R.id.min_price);
        EditText maxPriceEditText = filterView.findViewById(R.id.max_price);

        builder.setView(filterView);

        // Set existing price filter values if available
        if (currentPriceFilter.startsWith("Under $")) {
            maxPriceEditText.setText("10");
        } else if (currentPriceFilter.equals("$10 - $20")) {
            minPriceEditText.setText("10");
            maxPriceEditText.setText("20");
        } else if (currentPriceFilter.equals("$20 - $50")) {
            minPriceEditText.setText("20");
            maxPriceEditText.setText("50");
        } else if (currentPriceFilter.equals("Over $50")) {
            minPriceEditText.setText("50");
        }

        builder.setMultiChoiceItems(tagNames, selectedTags, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selectedTags[which] = isChecked;
            }
        });

        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

                if (!minPrice.isEmpty() && !maxPrice.isEmpty()) {
                    currentPriceFilter = minPrice + "-" + maxPrice;
                } else if (!minPrice.isEmpty()) {
                    currentPriceFilter = "Over $" + minPrice;
                } else if (!maxPrice.isEmpty()) {
                    currentPriceFilter = "Under $" + maxPrice;
                } else {
                    currentPriceFilter = ""; // Clear price filter
                }

                applyFilters();
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void applyFilters() {
        mealList.clear();
        String url = "http://10.0.2.2/Soufra_Share/meals.php?action=filter";
        Map<String, String> params = new HashMap<>();
        boolean hasFilter = false;

        if (!currentQuery.isEmpty()) {
            params.put("query", currentQuery);
            hasFilter = true;
        }
        if (!currentTagFilter.isEmpty()) {
            params.put("tags", currentTagFilter);
            hasFilter = true;
        }
        if (!currentPriceFilter.isEmpty()) {
            if (currentPriceFilter.startsWith("Under $")) {
                String maxPrice = currentPriceFilter.substring(7);
                params.put("maxPrice", maxPrice);
            } else if (currentPriceFilter.startsWith("Over $")) {
                String minPrice = currentPriceFilter.substring(6);
                params.put("minPrice", minPrice);
            } else if (currentPriceFilter.contains("-")) {
                String[] prices = currentPriceFilter.split("-");
                if (prices.length == 2) {
                    try {
                        double minPrice = Double.parseDouble(prices[0]);
                        double maxPrice = Double.parseDouble(prices[1]);
                        params.put("minPrice", String.valueOf(minPrice));
                        params.put("maxPrice", String.valueOf(maxPrice));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid price range format", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            hasFilter = true;
        }

        StringBuilder urlBuilder = new StringBuilder(url);

        if (hasFilter) {
            urlBuilder.append("&");
            List<String> paramList = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                paramList.add(entry.getKey() + "=" + entry.getValue());
            }
            urlBuilder.append(String.join("&", paramList));
        } else {
            // If no filters are applied, fetch all meals
            urlBuilder = new StringBuilder("http://10.0.2.2/Soufra_Share/meals.php?action=getAllWithUserDetails");
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, urlBuilder.toString(), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            mealList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject mealObject = response.getJSONObject(i);
                                Meal meal = new Meal(
                                        mealObject.getInt("meal_id"),
                                        mealObject.getInt("user_id"),
                                        mealObject.getString("name"),
                                        mealObject.getDouble("price"),
                                        mealObject.getInt("quantity"),
                                        mealObject.getString("location"),
                                        mealObject.getInt("delivery_option"),
                                        mealObject.getString("description"),
                                        mealObject.getString("image_paths"),
                                        mealObject.getString("created_at"),
                                        mealObject.getString("username"),
                                        mealObject.getString("profile_picture"),
                                        mealObject.getDouble("rating")
                                );
                                mealList.add(meal);
                            }
                            mealAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error parsing filtered meals", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MainActivity.this, "Error fetching filtered meals", Toast.LENGTH_SHORT).show();
                Log.e("Volley Error", error.toString());
            }
        });
        requestQueue.add(request);
    }
    // Inner class for Tag
    private static class Tag {
        private int tagId;
        private String name;

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
    }
}