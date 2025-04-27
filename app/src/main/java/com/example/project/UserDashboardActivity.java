package com.example.project;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

// Keep Volley imports
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
// Remove unused StringRequest
// import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
// Remove unused Glide
// import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

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
    private TextView usernameTextView, fullNameTextView, emailTextView, phoneTextView, locationTextView, aboutTextView;
    private EditText editUsernameEditText, editFullNameEditText, editEmailEditText, editPhoneEditText, editLocationEditText, editAboutEditText;

    private ImageView profilePictureImageView;
    private RatingBar ratingBar;
    private Button editDetailsButton, saveDetailsButton, orderHistoryButton, salesReportsButton, editProfilePictureButton;

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
                                Log.d("UserDashboard", "New profile picture selected."); // Keep useful logs
                            } catch (IOException e) {
                                e.printStackTrace(); // Keep stack trace log for debugging
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                                selectedProfilePictureBitmap = null;
                            }
                        } else {
                            Log.d("UserDashboard", "Image picking cancelled or failed."); // Keep useful logs
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
        salesReportsButton.setOnClickListener(v -> Toast.makeText(UserDashboardActivity.this, "Sales Reports will be implemented later", Toast.LENGTH_SHORT).show());

        orderHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });
        salesReportsButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, SalesReportListActivity.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        usernameTextView = findViewById(R.id.text_username);
        fullNameTextView = findViewById(R.id.text_full_name);
        emailTextView = findViewById(R.id.text_email);
        phoneTextView = findViewById(R.id.text_phone);
        locationTextView = findViewById(R.id.text_location);
        aboutTextView = findViewById(R.id.text_about); // Initialize About TextView

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
        editAboutEditText = findViewById(R.id.edit_text_about);

        setEditMode(false);
        editEmailEditText.setEnabled(false);

        reviewsRecyclerView = findViewById(R.id.recycler_view_reviews);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_orders) {
                selectedProfilePictureBitmap = null;
                Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.navigation_postings) {
                selectedProfilePictureBitmap = null;
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
                        usernameTextView.setText(response.optString("username"));
                        fullNameTextView.setText(response.optString("full_name"));
                        emailTextView.setText(response.optString("email"));
                        phoneTextView.setText(response.optString("phone_num", "N/A"));
                        locationTextView.setText(response.optString("location", "N/A"));
                        aboutTextView.setText(response.optString("about", "No description provided."));
                        if (response.has("rating") && !response.isNull("rating")) {
                            ratingBar.setRating((float) response.optDouble("rating", 0.0));
                        } else {
                            ratingBar.setRating(0.0f);
                        }


                        String profilePicturePath = response.optString("profile_picture");
                        if (profilePicturePath != null && !profilePicturePath.isEmpty() && !profilePicturePath.equals("null")) { // Also check for string "null" if PHP sends it like that
                            String fullImageUrl = "http://10.0.2.2/Soufra_Share/" + profilePicturePath;
                            Picasso.get()
                                    .load(fullImageUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .into(profilePictureImageView);
                            Log.d("UserDashboard", "Attempting to load PFP from URL: " + fullImageUrl);
                        } else {
                            profilePictureImageView.setImageResource(R.drawable.ic_person);
                            Log.d("UserDashboard", "No PFP path found or path is empty, showing default.");
                        }


                        // Set initial values for edit texts (for edit mode)
                        editUsernameEditText.setText(response.optString("username"));
                        editFullNameEditText.setText(response.optString("full_name"));
                        editEmailEditText.setText(response.optString("email"));
                        editPhoneEditText.setText(response.optString("phone_num"));
                        editLocationEditText.setText(response.optString("location"));
                        editAboutEditText.setText(response.optString("about"));
                        selectedProfilePictureBitmap = null;

                    }
                    catch (Exception e) {
                        Log.e("UserDashboard", "Unexpected error in loadUserDetails onResponse: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(UserDashboardActivity.this, "Error processing user data", Toast.LENGTH_SHORT).show();
                    }

                }, error -> {
            Log.e("UserDashboard", "Error loading user details: " + error.toString());
            String errorMessage = "Error loading user details";
            if (error.networkResponse != null) {
                errorMessage += ": Status Code " + error.networkResponse.statusCode;
                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    Log.e("UserDashboard", "Load error response body: " + responseBody);
                    JSONObject errorJson = new JSONObject(responseBody);
                    if (errorJson.has("message")) {
                        errorMessage += " - " + errorJson.getString("message");
                    }
                } catch (Exception e) { /* ignore parsing error */ }
            } else {
                errorMessage += ": " + error.getMessage();
            }
            Toast.makeText(UserDashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
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
                                    reviewObject.optInt("review_id"),
                                    reviewObject.optInt("reviewer_id"),
                                    reviewObject.optString("reviewer_username"),
                                    reviewObject.optInt("reviewee_id"),
                                    reviewObject.optInt("rating"),
                                    reviewObject.optString("comment"),
                                    reviewObject.optString("review_date"),
                                    reviewObject.optString("reviewer_profile_picture")
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
        editEmailEditText.setEnabled(false);
        selectedProfilePictureBitmap = null;
    }

    private void disableEditMode() {
        isEditMode = false;
        setEditMode(false);
        editEmailEditText.setEnabled(true);
        selectedProfilePictureBitmap = null;
    }

    private void setEditMode(boolean enable) {
        usernameTextView.setVisibility(enable ? View.GONE : View.VISIBLE);
        fullNameTextView.setVisibility(enable ? View.GONE : View.VISIBLE);
        emailTextView.setVisibility(enable ? View.GONE : View.VISIBLE);
        phoneTextView.setVisibility(enable ? View.GONE : View.VISIBLE);
        locationTextView.setVisibility(enable ? View.GONE : View.VISIBLE);
        aboutTextView.setVisibility(enable ? View.GONE : View.VISIBLE);

        editUsernameEditText.setVisibility(enable ? View.VISIBLE : View.GONE);
        editFullNameEditText.setVisibility(enable ? View.VISIBLE : View.GONE);
        editEmailEditText.setVisibility(enable ? View.VISIBLE : View.GONE);
        editPhoneEditText.setVisibility(enable ? View.VISIBLE : View.GONE);
        editLocationEditText.setVisibility(enable ? View.VISIBLE : View.GONE);
        editAboutEditText.setVisibility(enable ? View.VISIBLE : View.GONE);

        editDetailsButton.setVisibility(enable ? View.GONE : View.VISIBLE);
        saveDetailsButton.setVisibility(enable ? View.VISIBLE : View.GONE);
        editProfilePictureButton.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveUserDetails() {
        String newUsername = editUsernameEditText.getText().toString().trim();
        String newFullName = editFullNameEditText.getText().toString().trim();
        String newEmail = emailTextView.getText().toString();
        String newPhone = editPhoneEditText.getText().toString().trim();
        String newLocation = editLocationEditText.getText().toString().trim();
        String newAbout = editAboutEditText.getText().toString().trim();


        String url = "http://10.0.2.2/Soufra_Share/users.php?action=updateUserDetails";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", userId);
            requestBody.put("username", newUsername);
            requestBody.put("full_name", newFullName);
            requestBody.put("email", newEmail);
            requestBody.put("phone_num", newPhone);
            requestBody.put("location", newLocation);
            requestBody.put("about", newAbout);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(UserDashboardActivity.this, "Error creating JSON request for text update", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest textUpdateRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    Log.d("UserDashboard", "Text update response: " + response.toString()); // Keep useful logs
                    try {
                        String message = response.optString("message", "Unknown response");
                        if (response.optBoolean("success", true)) {
                            Toast.makeText(UserDashboardActivity.this, "Text details updated successfully", Toast.LENGTH_SHORT).show();
                            if (selectedProfilePictureBitmap != null) {
                                Log.d("UserDashboard", "Text update successful, proceeding to upload PFP.");
                                uploadProfilePicture();
                            } else {
                                Log.d("UserDashboard", "Text update successful, no new PFP selected.");
                                Toast.makeText(UserDashboardActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                                loadUserDetails();
                                disableEditMode();
                            }
                        } else {
                            Toast.makeText(UserDashboardActivity.this, "Failed to update text details: " + message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace(); // Keep stack trace log for debugging
                        Toast.makeText(UserDashboardActivity.this, "Error processing text update response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("UserDashboard", "Error updating text details: " + error.toString());
                    String errorMessage = "Error updating text details";
                    if (error.networkResponse != null) {
                        errorMessage += ": Status Code " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            Log.e("UserDashboard", "Text update error response body: " + responseBody); // Keep error logs
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMessage += " - " + errorJson.getString("message");
                            }
                        } catch (Exception e) { /* ignore parsing error */ }
                    } else {
                        errorMessage += ": " + error.getMessage();
                    }
                    Toast.makeText(UserDashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(textUpdateRequest);
    }

    private void uploadProfilePicture() {
        if (selectedProfilePictureBitmap == null) {
            Log.d("UserDashboard", "No new profile picture selected for upload.");
            Toast.makeText(UserDashboardActivity.this, "No new picture selected.", Toast.LENGTH_SHORT).show();
            loadUserDetails();
            disableEditMode();
            return;
        }
        String url = "http://10.0.2.2/Soufra_Share/upload_profile_picture.php";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            String jsonResponse = new String(response.data);
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            Log.d("UserDashboard", "PFP upload response: " + jsonResponse);

                            if (jsonObject.optBoolean("success", false)) {
                                Toast.makeText(UserDashboardActivity.this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                // Keep original error message from PHP response
                                Toast.makeText(UserDashboardActivity.this, "Failed to upload profile picture: " + jsonObject.optString("message", "Unknown error"), Toast.LENGTH_SHORT).show();
                            }
                            loadUserDetails();
                            disableEditMode();
                            selectedProfilePictureBitmap = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(UserDashboardActivity.this, "Error parsing PFP upload response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadUserDetails();
                            disableEditMode();
                            selectedProfilePictureBitmap = null;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        String errorMessage = "Error uploading profile picture";
                        if (error.networkResponse != null) {
                            errorMessage += ": Status Code " + error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                Log.e("UserDashboard", "PFP upload error response body: " + responseBody); // Keep error logs
                                JSONObject errorJson = new JSONObject(responseBody);
                                if (errorJson.has("message")) {
                                    errorMessage += " - " + errorJson.getString("message");
                                }
                            } catch (Exception e) { /* ignore parsing error */ }
                        } else {
                            errorMessage += ": " + error.getMessage();
                        }
                        Toast.makeText(UserDashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        loadUserDetails();
                        disableEditMode();
                        selectedProfilePictureBitmap = null;
                    }
                });

        multipartRequest.addStringParam("user_id", String.valueOf(userId));
        Log.d("UserDashboard", "Adding user_id param to PFP upload: " + userId); // Keep useful logs

        if (selectedProfilePictureBitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedProfilePictureBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] profilePictureBytes = byteArrayOutputStream.toByteArray();
            String filename = "profile_picture_" + userId + "_" + System.currentTimeMillis() + ".jpg";
            multipartRequest.addFile("profile_image", filename, profilePictureBytes, "image/jpeg");
            Log.d("UserDashboard", "Adding profile_image file to PFP upload with filename: " + filename); // Keep useful logs
        }

        requestQueue.add(multipartRequest);
        Log.d("UserDashboard", "PFP upload request added to queue.");
    }

}