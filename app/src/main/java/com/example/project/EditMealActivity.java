package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem; // For Up button
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar; // Example using Toolbar

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout; // Optional for error setting

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects; // For Objects.requireNonNull

public class EditMealActivity extends AppCompatActivity {

    private static final String TAG = "EditMealActivity";
    private static final String BASE_URL = "http://10.0.2.2/soufra_share/";
    private TextInputEditText etName, etPrice, etQuantity, etDescription, etLocation;
    private Spinner spinnerDeliveryOption;
    private Button btnSaveChanges;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;

    private Meal mealToEdit;
    private int currentUserId = -1;
    private static final String[] DELIVERY_OPTIONS = {"Pickup Only", "Delivery Available", "Both"};
    private static final int DELIVERY_PICKUP = 0;
    private static final int DELIVERY_AVAILABLE = 1;
    private static final int DELIVERY_BOTH = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_meal);
        requestQueue = Volley.newRequestQueue(this);
        initializeViews();

        if (getIntent().hasExtra("EDIT_MEAL_DATA")) {
            mealToEdit = (Meal) getIntent().getSerializableExtra("EDIT_MEAL_DATA");
            if (mealToEdit != null) {
                currentUserId = mealToEdit.getUserId();
                populateFields();
            } else {
                handleDataError("Error: Could not deserialize meal data.");
            }
        } else {
            handleDataError("Error: No meal data provided for editing.");
        }

        setupSaveButton();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initializeViews() {
        etName = findViewById(R.id.edit_text_meal_name);
        progressBar = findViewById(R.id.progressBar);
        etPrice = findViewById(R.id.edit_text_meal_price);
        etQuantity = findViewById(R.id.edit_text_meal_quantity);
        etDescription = findViewById(R.id.edit_text_meal_description);
        etLocation = findViewById(R.id.edit_text_meal_location);
        spinnerDeliveryOption = findViewById(R.id.spinner_edit_delivery_option);
        btnSaveChanges = findViewById(R.id.button_save_meal_changes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DELIVERY_OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeliveryOption.setAdapter(adapter);
    }

    private void populateFields() {
        if (mealToEdit == null) return;

        etName.setText(mealToEdit.getName());
        etPrice.setText(String.valueOf(mealToEdit.getPrice()));
        etQuantity.setText(String.valueOf(mealToEdit.getQuantity()));
        etDescription.setText(mealToEdit.getDescription());
        etLocation.setText(mealToEdit.getLocation());

        int deliveryOptionValue = mealToEdit.getDeliveryOption();
        if (deliveryOptionValue >= 0 && deliveryOptionValue < DELIVERY_OPTIONS.length) {
            spinnerDeliveryOption.setSelection(deliveryOptionValue);
        } else {
            spinnerDeliveryOption.setSelection(0);
            Log.w(TAG,"Invalid delivery option value ("+deliveryOptionValue+") for meal, defaulting spinner.");
        }

    }

    private void setupSaveButton() {
        btnSaveChanges.setOnClickListener(v -> attemptSaveChanges());
    }

    private void attemptSaveChanges() {
        String name = Objects.requireNonNull(etName.getText()).toString().trim();
        String priceStr = Objects.requireNonNull(etPrice.getText()).toString().trim();
        String quantityStr = Objects.requireNonNull(etQuantity.getText()).toString().trim();
        String description = Objects.requireNonNull(etDescription.getText()).toString().trim();
        String location = Objects.requireNonNull(etLocation.getText()).toString().trim();
        int deliveryOptionPosition = spinnerDeliveryOption.getSelectedItemPosition();
        int deliveryOptionValue = deliveryOptionPosition;


        if (TextUtils.isEmpty(name)) {
            showError(etName, "Meal name cannot be empty"); return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            showError(etPrice, "Price cannot be empty"); return;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            showError(etQuantity, "Quantity cannot be empty"); return;
        }
        if (TextUtils.isEmpty(description)) {
            showError(etDescription, "Description cannot be empty"); return;
        }
        if (TextUtils.isEmpty(location)) {
            showError(etLocation, "Location cannot be empty"); return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) throw new NumberFormatException("Price cannot be negative");
        } catch (NumberFormatException e) {
            showError(etPrice, "Invalid price format"); return;
        }
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) throw new NumberFormatException("Quantity cannot be negative");
        } catch (NumberFormatException e) {
            showError(etQuantity, "Invalid quantity format"); return;
        }


        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("meal_id", mealToEdit.getMealId());
            requestBody.put("user_id", currentUserId);
            requestBody.put("name", name);
            requestBody.put("price", price);
            requestBody.put("quantity", quantity);
            requestBody.put("location", location);
            requestBody.put("delivery_option", deliveryOptionValue);
            requestBody.put("description", description);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException creating update request body", e);
            Toast.makeText(this, "Error creating update data", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);
        String url = BASE_URL + "meals.php";

        Log.d(TAG, "Sending PUT request to: " + url);
        Log.d(TAG, "Request Body: " + requestBody.toString());

        JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.PUT, url, requestBody,
                response -> {
                    showLoading(false);
                    Log.d(TAG, "Update Response: " + response.toString());
                    try {
                        String message = response.optString("message", "Meal updated successfully!");
                        Toast.makeText(EditMealActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing update response", e);
                        Toast.makeText(EditMealActivity.this,"Update successful (response parsing issue).", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                },
                error -> {
                    showLoading(false);
                    handleVolleyError(error);
                });

        requestQueue.add(updateRequest);
    }

    private void showError(TextInputEditText field, String message) {
        field.setError(message);
        field.requestFocus();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSaveChanges.setEnabled(false);
            btnSaveChanges.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSaveChanges.setEnabled(true);
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
        String message = "An error occurred during update.";
        if (error.networkResponse != null) {
            message += " Status Code: " + error.networkResponse.statusCode;
            // Optionally parse error response body
        } else if (error.getMessage() != null) {
            message = error.getMessage();
        }
        Toast.makeText(EditMealActivity.this, message, Toast.LENGTH_LONG).show();
    }
}