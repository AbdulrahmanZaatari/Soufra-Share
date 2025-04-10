package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class AddMealActivity extends AppCompatActivity {

    private static final String TAG = "AddMealActivity";
    private static final String BASE_URL = "http://10.0.2.2/soufra_share/";

    private TextInputEditText etName, etPrice, etQuantity, etDescription, etLocation;
    private Spinner spinnerDeliveryOption;
    private Button btnPostMeal;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;

    private int currentUserId = -1; // To associate the meal with the logged-in user

    // Define delivery options (match EditMealActivity and your DB/logic)
    private static final String[] DELIVERY_OPTIONS = {"Pickup Only", "Delivery Available", "Both"};
    private static final int DELIVERY_PICKUP = 0;
    private static final int DELIVERY_AVAILABLE = 1;
    private static final int DELIVERY_BOTH = 2;

    // TODO: Add fields and logic for image selection/upload if needed


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);
        requestQueue = Volley.newRequestQueue(this);

        // --- Get User ID from Intent ---
        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        if (currentUserId == -1) {
            handleDataError("Error: Could not identify user. Please log in again.");
            return; // Exit if user ID is missing
        }
        Log.d(TAG, "User ID for posting: " + currentUserId);
        // ------------------------------

        initializeViews();
        setupPostButton();
    }

    // Handle Up navigation from Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Maybe ask if user wants to discard the draft?
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initializeViews() {
        etName = findViewById(R.id.add_text_meal_name);
        etPrice = findViewById(R.id.add_text_meal_price);
        etQuantity = findViewById(R.id.add_text_meal_quantity);
        etDescription = findViewById(R.id.add_text_meal_description);
        etLocation = findViewById(R.id.add_text_meal_location);
        spinnerDeliveryOption = findViewById(R.id.spinner_add_delivery_option);
        btnPostMeal = findViewById(R.id.button_post_meal);
        progressBar = findViewById(R.id.progress_bar_add_meal);

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DELIVERY_OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeliveryOption.setAdapter(adapter);
        spinnerDeliveryOption.setSelection(0); // Default selection
    }

    private void setupPostButton() {
        btnPostMeal.setOnClickListener(v -> attemptPostMeal());
    }

    private void attemptPostMeal() {
        // --- Input Validation (Similar to EditMealActivity) ---
        String name = Objects.requireNonNull(etName.getText()).toString().trim();
        String priceStr = Objects.requireNonNull(etPrice.getText()).toString().trim();
        String quantityStr = Objects.requireNonNull(etQuantity.getText()).toString().trim();
        String description = Objects.requireNonNull(etDescription.getText()).toString().trim();
        String location = Objects.requireNonNull(etLocation.getText()).toString().trim();
        int deliveryOptionPosition = spinnerDeliveryOption.getSelectedItemPosition();
        int deliveryOptionValue = deliveryOptionPosition;

        if (TextUtils.isEmpty(name)) { showError(etName, "Meal name required"); return; }
        if (TextUtils.isEmpty(priceStr)) { showError(etPrice, "Price required"); return; }
        if (TextUtils.isEmpty(quantityStr)) { showError(etQuantity, "Quantity required"); return; }
        if (TextUtils.isEmpty(description)) { showError(etDescription, "Description required"); return; }
        if (TextUtils.isEmpty(location)) { showError(etLocation, "Location required"); return; }


        double price;
        int quantity;
        try { price = Double.parseDouble(priceStr); if (price < 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { showError(etPrice, "Invalid price"); return; }
        try { quantity = Integer.parseInt(quantityStr); if (quantity <= 0) throw new NumberFormatException(); } // Quantity should be > 0
        catch (NumberFormatException e) { showError(etQuantity, "Invalid quantity (must be > 0)"); return; }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", currentUserId);
            requestBody.put("name", name);
            requestBody.put("price", price);
            requestBody.put("quantity", quantity);
            requestBody.put("location", location);
            requestBody.put("delivery_option", deliveryOptionValue);
            requestBody.put("description", description);
            // TODO: Add "image_paths" if handling image uploads
            // String imagePaths = getUploadedImagePaths(); // Implement this
            // requestBody.put("image_paths", imagePaths);

        } catch (JSONException e) {
            Log.e(TAG, "JSONException creating post request body", e);
            Toast.makeText(this, "Error preparing data", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        String url = BASE_URL + "meals.php";

        Log.d(TAG, "Sending POST request to: " + url);
        Log.d(TAG, "Request Body: " + requestBody.toString());

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    showLoading(false);
                    Log.d(TAG, "Post Response: " + response.toString());
                    try {
                        String message = response.optString("message", "Meal posted successfully!");
                        Toast.makeText(AddMealActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing post response", e);
                        Toast.makeText(AddMealActivity.this, "Post successful (response parsing issue).", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // Assume success if 2xx response
                        finish();
                    }
                },
                error -> {
                    showLoading(false);
                    handleVolleyError(error);
                });

        requestQueue.add(postRequest);
    }

    private void showError(TextInputEditText field, String message) {
        field.setError(message);
        field.requestFocus();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnPostMeal.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnPostMeal.setEnabled(true);
        }
    }

    private void handleDataError(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void handleVolleyError(com.android.volley.VolleyError error) {
        Log.e(TAG, "Volley error: ", error);
        String message = "An error occurred while posting.";
        if (error.networkResponse != null) {
            message += " Status Code: " + error.networkResponse.statusCode;
        } else if (error.getMessage() != null) {
            message = error.getMessage();
        }
        Toast.makeText(AddMealActivity.this, message, Toast.LENGTH_LONG).show();
    }
}