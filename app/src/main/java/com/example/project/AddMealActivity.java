package com.example.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddMealActivity extends AppCompatActivity {

    private static final String TAG = "AddMealActivity";
    private static final String BASE_URL = "http://10.0.2.2/soufra_share/";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2;

    private TextInputEditText etName, etPrice, etQuantity, etDescription, etLocation;
    private Spinner spinnerDeliveryOption;
    private Button btnPostMeal, btnSelectImage;
    private ImageView addImageMeal;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private Uri selectedImageUri;

    private int currentUserId = -1; // To associate the meal with the logged-in user

    // Define delivery options (match EditMealActivity and your DB/logic)
    private static final String[] DELIVERY_OPTIONS = {"Pickup Only", "Delivery Available", "Both"};
    private static final int DELIVERY_PICKUP = 0;
    private static final int DELIVERY_AVAILABLE = 1;
    private static final int DELIVERY_BOTH = 2;

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
        setupImageSelection();
    }

    private void setupImageSelection() {
        btnSelectImage.setOnClickListener(v -> checkStoragePermissionAndOpenGallery());
    }

    private void checkStoragePermissionAndOpenGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission is required to select an image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            addImageMeal.setImageURI(selectedImageUri);
        }
    }

    // Handle Up navigation from Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
        btnSelectImage = findViewById(R.id.button_select_image);
        addImageMeal = findViewById(R.id.add_image_meal);
        progressBar = findViewById(R.id.progressBar);

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
        // --- Input Validation ---
        String name = Objects.requireNonNull(etName.getText()).toString().trim();
        String priceStr = Objects.requireNonNull(etPrice.getText()).toString().trim();
        String quantityStr = Objects.requireNonNull(etQuantity.getText()).toString().trim();
        String description = Objects.requireNonNull(etDescription.getText()).toString().trim();
        String location = Objects.requireNonNull(etLocation.getText()).toString().trim();
        int deliveryOptionPosition = spinnerDeliveryOption.getSelectedItemPosition();
        int deliveryOptionValue = deliveryOptionPosition;

        if (TextUtils.isEmpty(name)) {
            showError(etName, "Meal name required");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            showError(etPrice, "Price required");
            return;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            showError(etQuantity, "Quantity required");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            showError(etDescription, "Description required");
            return;
        }
        if (TextUtils.isEmpty(location)) {
            showError(etLocation, "Location required");
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError(etPrice, "Invalid price");
            return;
        }
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) throw new NumberFormatException(); // Quantity should be > 0
        } catch (NumberFormatException e) {
            showError(etQuantity, "Invalid quantity (must be > 0)");
            return;
        }

        showLoading(true);
        String url = BASE_URL + "meals.php";

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        showLoading(false);
                        String resultResponse = new String(response.data);
                        Log.d(TAG, "Post Response: " + resultResponse);
                        try {
                            JSONObject jsonObject = new JSONObject(resultResponse);
                            String message = jsonObject.optString("message", "Meal posted successfully!");
                            Toast.makeText(AddMealActivity.this, message, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing post response", e);
                            Toast.makeText(AddMealActivity.this, "Post successful (response parsing issue).", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showLoading(false);
                handleVolleyError(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                if (headers == null || headers.isEmpty()) {
                    headers = new HashMap<>();
                }
                headers.put("Content-Type", "multipart/form-data; boundary=" + getBoundary());
                return headers;
            }

            private String getBoundary() {
                return "apiclient" + System.currentTimeMillis();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(currentUserId));
                params.put("name", name);
                params.put("price", String.valueOf(price));
                params.put("quantity", String.valueOf(quantity));
                params.put("location", location);
                params.put("delivery_option", String.valueOf(deliveryOptionValue));
                params.put("description", description);
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                String boundary = getBoundary();

                try {
                    // Populate string parameters
                    Map<String, String> params = getParams();
                    if (params != null && params.size() > 0) {
                        for (Map.Entry<String, String> entry : params.entrySet()) {
                            buildPart(dos, entry.getKey(), entry.getValue(), boundary);
                        }
                    }

                    // Populate byte data (image)
                    if (selectedImageUri != null) {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        byte[] imageData = getBytes(inputStream);
                        String filename = new File(selectedImageUri.getPath()).getName();
                        buildPart(dos, "meal_image", filename, imageData, boundary);
                    }

                    // End of multipart/form-data. Add the closing boundary.
                    dos.writeBytes("--" + boundary + "--" + "\r\n");

                    return bos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            private void buildPart(DataOutputStream dataOutputStream, String key, String value, String boundary) throws IOException {
                dataOutputStream.writeBytes("--" + boundary + "\r\n");
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + "\r\n");
                dataOutputStream.writeBytes("\r\n");
                dataOutputStream.writeBytes(value + "\r\n");
            }

            private void buildPart(DataOutputStream dataOutputStream, String key, String filename, byte[] fileData, String boundary) throws IOException {
                dataOutputStream.writeBytes("--" + boundary + "\r\n");
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + filename + "\"" + "\r\n");
                dataOutputStream.writeBytes("Content-Type: image/jpeg" + "\r\n"); // Adjust content type as needed
                dataOutputStream.writeBytes("\r\n");
                dataOutputStream.write(fileData);
                dataOutputStream.writeBytes("\r\n");
            }
        };

        requestQueue.add(multipartRequest);
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        byte[] bytesResult = null;
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            bytesResult = output.toByteArray();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                    // Ignored
                }
            }
        }
        return bytesResult;
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