package com.example.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // Added import
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso; // Added import

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    private int userId;
    private TextView usernameTextView;
    private TextView fullNameTextView;
    private TextView locationTextView;
    private TextView aboutTextView;
    private RecyclerView mealsRecyclerView;
    private RecyclerView reviewsRecyclerView;
    private UserMealsAdapter userMealsAdapter; // Using UserMealsAdapter
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private Button submitReviewButton;
    private ImageView profilePictureTop; // Added ImageView

    private static final String URL_USERS = "http://10.0.2.2/Soufra_Share/users.php?id=";
    private static final String URL_MEALS_BY_USER = "http://10.0.2.2/Soufra_Share/meals.php?user_id=";
    private static final String URL_REVIEWS_BY_USER = "http://10.0.2.2/Soufra_Share/reviews.php?reviewee_id=";
    private static final String URL_CREATE_REVIEW = "http://10.0.2.2/Soufra_Share/reviews.php";

    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_USER_ID = "user_id";

    private int getLoggedInUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        usernameTextView = findViewById(R.id.usernameTextView);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        locationTextView = findViewById(R.id.locationTextView);
        aboutTextView = findViewById(R.id.aboutTextView);
        mealsRecyclerView = findViewById(R.id.mealsRecyclerView);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        ratingBar = findViewById(R.id.ratingBar);
        commentEditText = findViewById(R.id.commentEditText);
        submitReviewButton = findViewById(R.id.submitReviewButton);
        profilePictureTop = findViewById(R.id.profilePictureTop); // Find ImageView

        // Set layout manager for mealsRecyclerView to horizontal
        LinearLayoutManager layoutManagerMeals = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mealsRecyclerView.setLayoutManager(layoutManagerMeals);

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        reviewsRecyclerView.setAdapter(reviewAdapter);

        userMealsAdapter = new UserMealsAdapter(this, new ArrayList<>()); // Initialize UserMealsAdapter
        mealsRecyclerView.setAdapter(userMealsAdapter);

        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId != -1) {
            Log.d(TAG, "Received USER_ID: " + userId);
            loadUserProfile();
            loadUserMeals();
            loadUserReviews();

            // Set OnClickListener for submitting a review
            submitReviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitReview();
                }
            });
        } else {
            Log.e(TAG, "No USER_ID received in Intent");
            Toast.makeText(this, "Error: Could not load user profile", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserProfile() {
        String url = URL_USERS + userId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "User Profile Response: " + response);
                        try {
                            JSONObject userObject = new JSONObject(response);
                            usernameTextView.setText(userObject.getString("username"));
                            fullNameTextView.setText(userObject.getString("full_name"));
                            locationTextView.setText(userObject.getString("location"));
                            if (userObject.has("about")) {
                                aboutTextView.setText(userObject.getString("about"));
                            } else {
                                aboutTextView.setText("No about information provided.");
                            }

                            // Load profile picture at the top
                            if (userObject.has("profile_picture") && !userObject.getString("profile_picture").isEmpty()) {
                                String profilePictureUrl = "http://10.0.2.2/Soufra_Share/" + userObject.getString("profile_picture");
                                Log.d(TAG, "Loading top profile picture from: " + profilePictureUrl);
                                Picasso.get()
                                        .load(profilePictureUrl)
                                        .placeholder(R.drawable.ic_person)
                                        .error(R.drawable.ic_person)
                                        .into(profilePictureTop);
                            } else {
                                profilePictureTop.setImageResource(R.drawable.ic_person); // Set default if no picture
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing user profile JSON: " + e.getMessage());
                            Toast.makeText(UserProfileActivity.this, "Error loading user profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching user profile: " + error.getMessage());
                        Toast.makeText(UserProfileActivity.this, "Error loading user profile", Toast.LENGTH_SHORT).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void loadUserMeals() {
        String url = URL_MEALS_BY_USER + userId + "&action=getUserMealsWithDetails";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "User Meals Response: " + response);
                        try {
                            JSONArray mealsArray = new JSONArray(response);
                            List<Meal> mealList = new ArrayList<>();
                            for (int i = 0; i < mealsArray.length(); i++) {
                                JSONObject mealObject = mealsArray.getJSONObject(i);
                                Meal meal = new Meal(
                                        mealObject.getInt("meal_id"),
                                        mealObject.getInt("user_id"),
                                        mealObject.getString("name"),
                                        mealObject.getDouble("price"),
                                        mealObject.getInt("quantity"),
                                        mealObject.getString("location"),
                                        mealObject.getInt("delivery_option"),
                                        mealObject.getString("description"),
                                        mealObject.optString("image_paths"), // Use optString to handle potential null values
                                        mealObject.optString("created_at"),
                                        mealObject.optString("username"),    // Now these should be present
                                        mealObject.optString("profile_picture"),
                                        mealObject.optDouble("rating", 0.0) // Use optDouble with a default value
                                );
                                mealList.add(meal);
                            }
                            userMealsAdapter = new UserMealsAdapter(UserProfileActivity.this, mealList);
                            mealsRecyclerView.setAdapter(userMealsAdapter);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing user meals JSON: " + e.getMessage());
                            Toast.makeText(UserProfileActivity.this, "Error loading user's meals", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching user meals: " + error.getMessage());
                        Toast.makeText(UserProfileActivity.this, "Error loading user's meals", Toast.LENGTH_SHORT).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void loadUserReviews() {
        String url = URL_REVIEWS_BY_USER + userId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "User Reviews Response: " + response);
                        try {
                            JSONArray reviewsArray = new JSONArray(response);
                            reviewList.clear();
                            for (int i = 0; i < reviewsArray.length(); i++) {
                                JSONObject reviewObject = reviewsArray.getJSONObject(i);
                                Review review = new Review(
                                        reviewObject.getInt("review_id"),
                                        reviewObject.getInt("reviewer_id"),
                                        reviewObject.getString("reviewer_username"),
                                        reviewObject.getInt("reviewee_id"),
                                        reviewObject.getInt("rating"),
                                        reviewObject.getString("comment"),
                                        reviewObject.getString("review_date"),
                                        reviewObject.getString("reviewer_profile_picture")
                                );
                                reviewList.add(review);
                            }
                            reviewAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing user reviews JSON: " + e.getMessage());
                            Toast.makeText(UserProfileActivity.this, "Error loading user's reviews", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching user reviews: " + error.getMessage());
                        Toast.makeText(UserProfileActivity.this, "Error loading user's reviews", Toast.LENGTH_SHORT).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void submitReview() {
        int reviewerId = getLoggedInUserId();
        if (reviewerId == -1) {
            Toast.makeText(this, "You must be logged in to leave a review.", Toast.LENGTH_SHORT).show();
            return;
        }
        float rating = ratingBar.getRating();
        String comment = commentEditText.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_REVIEW,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Submit Review Response: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.has("error")) {
                                Toast.makeText(UserProfileActivity.this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();

                                // Create a new Review object
                                SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                                String reviewerUsername = "You"; // Or fetch the actual username if available
                                String reviewerProfilePicture = null; // You might not have this immediately

                                Review newReview = new Review(
                                        jsonObject.getInt("review_id"),
                                        reviewerId,
                                        reviewerUsername,
                                        userId,
                                        (int) rating,
                                        comment,
                                        "Just now", // Or format the current time
                                        reviewerProfilePicture
                                );

                                // Add the new review to the list and notify the adapter
                                reviewList.add(0, newReview); // Add at the beginning to show immediately
                                reviewAdapter.notifyItemInserted(0);
                                reviewsRecyclerView.scrollToPosition(0); // Scroll to the new review

                                // Clear the rating and comment fields
                                ratingBar.setRating(0);
                                commentEditText.setText("");
                            } else {
                                Toast.makeText(UserProfileActivity.this, "Error submitting review: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing submit review response: " + e.getMessage());
                            Toast.makeText(UserProfileActivity.this, "Error submitting review.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error submitting review: " + error.getMessage());
                        Toast.makeText(UserProfileActivity.this, "Error submitting review.", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("reviewer_id", reviewerId);
                    jsonBody.put("reviewee_id", userId);
                    jsonBody.put("rating", rating);
                    jsonBody.put("comment", comment);
                    final String requestBody = jsonBody.toString();
                    return requestBody.getBytes("utf-8");
                } catch (JSONException | UnsupportedEncodingException e) {
                    Log.e(TAG, "Error creating JSON body: " + e.getMessage());
                    return null;
                }
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}