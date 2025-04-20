package com.example.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostingsActivity extends AppCompatActivity implements PostingAdapter.OnPostingActionListener {

    private static final String TAG = "PostingsActivity";
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String BASE_URL = "http://10.0.2.2/soufra_share/";

    private RecyclerView postingsRecyclerView;
    private PostingAdapter postingAdapter;
    private List<Meal> userPostingsList;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddPosting;
    private TextView textViewEmptyPostings;
    private RequestQueue requestQueue;
    private int currentUserId = -1;
    private ActivityResultLauncher<Intent> mealActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_postings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.postings_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); 
            return WindowInsetsCompat.CONSUMED;
        });

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId == -1) {
            handleUserNotLoggedIn();
            return; // Stop execution
        }
        Log.d(TAG, "Current User ID: " + currentUserId);

        requestQueue = Volley.newRequestQueue(this);
        userPostingsList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        setupBottomNavigation();
        setupFab();
        setupActivityResultLauncher();

        fetchUserPostings();
    }

    private void initializeViews() {
        postingsRecyclerView = findViewById(R.id.postings_recycler_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation_postings);
        fabAddPosting = findViewById(R.id.fab_add_posting);
        textViewEmptyPostings = findViewById(R.id.text_view_empty_postings);
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView() called");
        postingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postingAdapter = new PostingAdapter(this, userPostingsList, this);
        postingsRecyclerView.setAdapter(postingAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_postings);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_postings) {
                return true;
            } else if (id == R.id.navigation_orders) {
                navigateTo(MainActivity.class);
                return true;
            } else if (id == R.id.navigation_dashboard) {
                SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);

                if (userId != -1) {
                    Intent intent = new Intent(PostingsActivity.this, UserDashboardActivity.class);
                    intent.putExtra("USER_ID", userId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return true;
                } else {
                    Toast.makeText(PostingsActivity.this, "User ID not found in preferences.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            return false;
        });
    }
    private void setupFab() {
        fabAddPosting.setOnClickListener(v -> {
            Log.d(TAG, "FAB clicked - Launching AddMealActivity");
            Intent intent = new Intent(PostingsActivity.this, AddMealActivity.class);
            // Pass the current user ID to AddMealActivity if needed there
            intent.putExtra("USER_ID", currentUserId);
            mealActivityResultLauncher.launch(intent);
        });
    }


    private void setupActivityResultLauncher() {
        mealActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "Received OK result from Add/Edit Meal Activity. Refreshing postings.");
                        fetchUserPostings(); // Refresh the list on successful add/edit
                    } else {
                        Log.d(TAG, "Received Cancel or other result code from Add/Edit Meal Activity.");
                    }
                });
    }

    private void fetchUserPostings() {
        if (currentUserId == -1) return;
        String url = BASE_URL + "meals.php?user_id=" + currentUserId;
        Log.d(TAG, "Fetching user postings from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "Received " + response.length() + " postings.");
                    Log.d(TAG, "Raw JSON Response: " + response.toString());
                    userPostingsList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject mealObject = response.getJSONObject(i);
                            Meal meal = parseMealFromJson(mealObject);
                            if (meal != null) {
                                userPostingsList.add(meal);
                            }
                        }
                        postingAdapter.updatePostings(userPostingsList);
                        checkEmptyState();
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        Toast.makeText(PostingsActivity.this, "Error parsing postings", Toast.LENGTH_SHORT).show();
                        checkEmptyState();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error fetching postings", error);
                    Toast.makeText(PostingsActivity.this, "Error fetching your postings", Toast.LENGTH_SHORT).show();
                    checkEmptyState();
                });
        requestQueue.add(request);
    }

    private Meal parseMealFromJson(JSONObject mealObject) {
        if (mealObject == null) return null;
        try {
            return new Meal(
                    mealObject.optInt("meal_id", -1),
                    mealObject.optInt("user_id", -1),
                    mealObject.optString("name", "N/A"),
                    mealObject.optDouble("price", 0.0),
                    mealObject.optInt("quantity", 0),
                    mealObject.optString("location", ""),
                    mealObject.optInt("delivery_option", 0),
                    mealObject.optString("description", ""),
                    mealObject.optString("image_paths", "[]"),
                    mealObject.optString("created_at", ""),
                    mealObject.optString("username", null),
                    mealObject.optString("profile_picture", null),
                    mealObject.optDouble("rating", 0.0)
            );
        } catch (Exception e) {
            Log.e(TAG, "Error parsing single meal object: " + mealObject.toString(), e);
            return null; // Return null if parsing fails
        }
    }


    private void deletePosting(final Meal meal, final int position) {
        String url = BASE_URL + "meals.php?meal_id=" + meal.getMealId(); // Append meal_id to the URL
        Log.d(TAG, "Attempting to delete meal ID: " + meal.getMealId() + " at URL: " + url);

        StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Log.d(TAG, "Delete Response: " + response);
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        String message = responseObject.optString("message", "Posting deleted successfully.");
                        Toast.makeText(PostingsActivity.this, message, Toast.LENGTH_SHORT).show();
                        postingAdapter.removeItem(position);
                        checkEmptyState();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON response", e);
                        Toast.makeText(this, "Deletion successful (response parsing issue).", Toast.LENGTH_SHORT).show();
                        postingAdapter.removeItem(position);
                        checkEmptyState();
                    }
                },
                this::handleVolleyError);

        requestQueue.add(deleteRequest);
    }

    private void checkEmptyState() {
        if (postingAdapter.getItemCount() == 0) {
            textViewEmptyPostings.setVisibility(View.VISIBLE);
            postingsRecyclerView.setVisibility(View.GONE);
        } else {
            textViewEmptyPostings.setVisibility(View.GONE);
            postingsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void handleUserNotLoggedIn() {
        Log.e(TAG, "User ID not found in SharedPreferences. Redirecting to login.");
        Toast.makeText(this, "Error: User session invalid. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, SignIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(PostingsActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void handleVolleyError(VolleyError error) {
        Log.e(TAG, "Volley error: ", error);
        String message = "An error occurred.";
        if (error.networkResponse != null) {
            message += " Status Code: " + error.networkResponse.statusCode;
            if (error.networkResponse.data != null) {
                try {
                    String errorData = new String(error.networkResponse.data);
                    Log.e(TAG, "Volley error data: " + errorData);
                    JSONObject errorJson = new JSONObject(errorData);
                    if (errorJson.has("message")) {
                        message += ": " + errorJson.getString("message");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing error data", e);
                }
            }
        } else if (error.getMessage() != null) {
            message = error.getMessage();
        }
        Toast.makeText(PostingsActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEditClick(Meal meal) {
        Log.d(TAG, "Edit clicked for meal: " + meal.getName() + " (ID: " + meal.getMealId() + ")");
        Intent intent = new Intent(PostingsActivity.this, EditMealActivity.class);
        intent.putExtra("EDIT_MEAL_DATA", meal);
        mealActivityResultLauncher.launch(intent);
    }

    @Override
    public void onDeleteClick(final Meal meal, final int position) {
        Log.d(TAG, "Delete confirmation for meal: " + meal.getName() + " (ID: " + meal.getMealId() + ")");
        new AlertDialog.Builder(this)
                .setTitle("Delete Posting")
                .setMessage("Are you sure you want to delete '" + meal.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deletePosting(meal, position))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}