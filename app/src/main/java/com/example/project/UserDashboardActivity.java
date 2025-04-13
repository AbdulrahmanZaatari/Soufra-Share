package com.example.project;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDashboardActivity extends AppCompatActivity {

    private int userId;
    private TextView usernameTextView, fullNameTextView, emailTextView, phoneTextView, locationTextView;
    private ImageView profilePictureImageView;
    private RatingBar ratingBar;
    private Button editDetailsButton, saveDetailsButton, orderHistoryButton, salesReportsButton, editProfilePictureButton;
    private EditText editUsernameEditText, editFullNameEditText, editEmailEditText, editPhoneEditText, editLocationEditText;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private RequestQueue requestQueue;
    private boolean isEditMode = false;
    private Bitmap selectedProfilePictureBitmap;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri selectedImageUri = result.getData().getData();
                            try {
                                selectedProfilePictureBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                                profilePictureImageView.setImageBitmap(selectedProfilePictureBitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        requestQueue = Volley.newRequestQueue(this);

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupBottomNavigation();
        loadUserDetails();
        loadUserReviews();

        editDetailsButton.setOnClickListener(v -> enableEditMode());
        saveDetailsButton.setOnClickListener(v -> saveUserDetails());
        editProfilePictureButton.setOnClickListener(v -> openImagePicker());
        orderHistoryButton.setOnClickListener(v -> Toast.makeText(this, "Order History will be implemented later", Toast.LENGTH_SHORT).show());
        salesReportsButton.setOnClickListener(v -> Toast.makeText(this, "Sales Reports will be implemented later", Toast.LENGTH_SHORT).show());
    }

    private void initializeViews() {
        usernameTextView = findViewById(R.id.text_username);
        fullNameTextView = findViewById(R.id.text_full_name);
        emailTextView = findViewById(R.id.text_email);
        phoneTextView = findViewById(R.id.text_phone);
        locationTextView = findViewById(R.id.text_location);
        profilePictureImageView = findViewById(R.id.image_profile_picture);
        ratingBar = findViewById(R.id.rating_bar);
        editDetailsButton = findViewById(R.id.button_edit_details);
        saveDetailsButton = findViewById(R.id.button_save_details);
        orderHistoryButton = findViewById(R.id.button_order_history);
        salesReportsButton = findViewById(R.id.button_sales_reports);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        editProfilePictureButton = findViewById(R.id.button_edit_profile_picture);

        editUsernameEditText = findViewById(R.id.edit_text_username);
        editFullNameEditText = findViewById(R.id.edit_text_full_name);
        editEmailEditText = findViewById(R.id.edit_text_email);
        editPhoneEditText = findViewById(R.id.edit_text_phone);
        editLocationEditText = findViewById(R.id.edit_text_location);

        // Initially hide edit views and save button
        setEditMode(false);

        reviewsRecyclerView = findViewById(R.id.recycler_view_reviews);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_orders) {
                Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.navigation_postings) {
                Intent intent = new Intent(UserDashboardActivity.this, PostingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.navigation_dashboard) {
                return true;
            }
            return false;
        });
    }

    private void loadUserDetails() {
        String url = "http://10.0.2.2/Soufra_Share/users.php?id=" + userId;
        Log.d("UserDashboard", "Loading user details from URL: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("UserDashboard", "User details response: " + response.toString());
                    try {
                        usernameTextView.setText(response.getString("username"));
                        fullNameTextView.setText(response.getString("full_name"));
                        emailTextView.setText(response.getString("email"));
                        phoneTextView.setText(response.getString("phone_num"));
                        locationTextView.setText(response.getString("location"));

                        if (response.has("rating") && !response.isNull("rating")) {
                            ratingBar.setRating((float) response.getDouble("rating"));
                        } else {
                            ratingBar.setRating(0.0f);
                        }

                        String profilePictureUrl = response.optString("profile_picture");
                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            Glide.with(UserDashboardActivity.this)
                                    .load("http://10.0.2.2/Soufra_Share/" + profilePictureUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .into(profilePictureImageView);
                        } else {
                            profilePictureImageView.setImageResource(R.drawable.ic_person);
                        }

                        // Set initial values for edit texts
                        editUsernameEditText.setText(response.getString("username"));
                        editFullNameEditText.setText(response.getString("full_name"));
                        editEmailEditText.setText(response.getString("email"));
                        editPhoneEditText.setText(response.getString("phone_num"));
                        editLocationEditText.setText(response.getString("location"));

                    } catch (JSONException e) {
                        Log.e("UserDashboard", "Error parsing user details: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(UserDashboardActivity.this, "Error parsing user details", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e("UserDashboard", "Error loading user details: " + error.getMessage());
            error.printStackTrace();
            Toast.makeText(UserDashboardActivity.this, "Error loading user details", Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(request);
    }

    private void loadUserReviews() {
        String url = "http://10.0.2.2/Soufra_Share/reviews.php?reviewee_id=" + userId;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        reviewList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject reviewObject = response.getJSONObject(i);
                            Review review = new Review(
                                    reviewObject.getInt("review_id"),
                                    reviewObject.getInt("reviewer_id"),
                                    reviewObject.getString("reviewer_username"),
                                    reviewObject.getInt("reviewee_id"),
                                    reviewObject.getInt("rating"),
                                    reviewObject.getString("comment"),
                                    reviewObject.getString("review_date")
                            );
                            reviewList.add(review);
                        }
                        reviewAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(UserDashboardActivity.this, "Error parsing reviews", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            error.printStackTrace();
            Toast.makeText(UserDashboardActivity.this, "Error loading reviews", Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(request);
    }

    private void enableEditMode() {
        isEditMode = true;
        setEditMode(true);
    }

    private void disableEditMode() {
        isEditMode = false;
        setEditMode(false);
    }

    private void setEditMode(boolean enable) {
        usernameTextView.setVisibility(enable ? View.GONE : View.VISIBLE);
        fullNameTextView.setVisibility(enable ? View.GONE : View.VISIBLE);
        emailTextView.setVisibility(enable ? View.GONE : View.VISIBLE);
        phoneTextView.setVisibility(enable ? View.GONE : View.VISIBLE);
        locationTextView.setVisibility(enable ? View.GONE : View.VISIBLE);
        editDetailsButton.setVisibility(enable ? View.GONE : View.VISIBLE);
        editProfilePictureButton.setVisibility(enable ? View.VISIBLE : View.GONE);
        saveDetailsButton.setVisibility(enable ? View.VISIBLE : View.GONE);

        editUsernameEditText.setVisibility(enable ? View.VISIBLE : View.GONE);
        editFullNameEditText.setVisibility(enable ? View.VISIBLE : View.GONE);
        editEmailEditText.setVisibility(enable ? View.VISIBLE : View.GONE);
        editPhoneEditText.setVisibility(enable ? View.VISIBLE : View.GONE);
        editLocationEditText.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream); // Adjust quality as needed
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveUserDetails() {
        String newUsername = editUsernameEditText.getText().toString().trim();
        String newFullName = editFullNameEditText.getText().toString().trim();
        String newEmail = editEmailEditText.getText().toString().trim();
        String newPhone = editPhoneEditText.getText().toString().trim();
        String newLocation = editLocationEditText.getText().toString().trim();
        String profilePictureBase64 = bitmapToBase64(selectedProfilePictureBitmap);

        String url = "http://10.0.2.2/Soufra_Share/users.php?action=updateUserDetails";
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        params.put("username", newUsername);
        params.put("full_name", newFullName);
        params.put("email", newEmail);
        params.put("phone_num", newPhone);
        params.put("location", newLocation);
        if (profilePictureBase64 != null && !profilePictureBase64.isEmpty()) {
            params.put("profile_picture", profilePictureBase64);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(UserDashboardActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                            loadUserDetails();
                            disableEditMode();
                        } else {
                            Toast.makeText(UserDashboardActivity.this, "Failed to update details: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(UserDashboardActivity.this, "Error parsing update response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            error.printStackTrace();
            Toast.makeText(UserDashboardActivity.this, "Error updating user details", Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(request);
    }
}