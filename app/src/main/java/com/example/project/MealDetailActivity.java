package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;

public class MealDetailActivity extends AppCompatActivity {

    private static final String TAG = "MealDetailActivity";
    private ImageView mealImageView;
    private TextView mealNameTextView;
    private TextView mealDescriptionTextView;
    private TextView mealPriceTextView;
    private TextView mealLocationTextView;
    private TextView mealDeliveryOptionTextView;
    private EditText quantityEditText;
    private Button addToCartButton;
    private Button goBackButton; // Added Go Back Button
    private Button cartButton; // Added Cart Button
    private Meal currentMeal;
    private RequestQueue requestQueue;
    private String baseUrl = "http://10.0.2.2/Soufra_Share/"; // Define your base URL here

    // Replace with the actual logged-in user ID
    private int loggedInUserId = 1; // Placeholder for user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);
        Log.d(TAG, "onCreate() called");

        // Initialize views
        mealImageView = findViewById(R.id.meal_image_view);
        mealNameTextView = findViewById(R.id.meal_name_text_view);
        mealDescriptionTextView = findViewById(R.id.meal_description_text_view);
        mealPriceTextView = findViewById(R.id.meal_price_text_view);
        mealLocationTextView = findViewById(R.id.meal_location_text_view);
        mealDeliveryOptionTextView = findViewById(R.id.meal_delivery_option_text_view);
        quantityEditText = findViewById(R.id.quantity_edit_text);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        goBackButton = findViewById(R.id.go_back_button);
        cartButton = findViewById(R.id.cart_button);
        Log.d(TAG, "Views initialized");

        requestQueue = Volley.newRequestQueue(this);
        Log.d(TAG, "RequestQueue initialized");

        // Get the Meal object passed from the previous activity
        currentMeal = (Meal) getIntent().getSerializableExtra("meal");
        Log.d(TAG, "Retrieved Meal object from intent: " + currentMeal);

        if (currentMeal != null) {
            mealNameTextView.setText(currentMeal.getName());
            Log.d(TAG, "Meal Name: " + currentMeal.getName());
            mealDescriptionTextView.setText(currentMeal.getDescription());
            Log.d(TAG, "Meal Description: " + currentMeal.getDescription());
            mealPriceTextView.setText("Price: $" + String.format("%.2f", currentMeal.getPrice()));
            Log.d(TAG, "Meal Price: $" + String.format("%.2f", currentMeal.getPrice()));
            mealLocationTextView.setText("Location: " + currentMeal.getLocation());
            Log.d(TAG, "Meal Location: " + currentMeal.getLocation());
            mealDeliveryOptionTextView.setText("Delivery Available: " + (currentMeal.getDeliveryOption() == 1 ? "Yes" : "No"));
            Log.d(TAG, "Delivery Option: " + (currentMeal.getDeliveryOption() == 1 ? "Yes" : "No"));

            String imagePathsJson = currentMeal.getImagePaths();
            if (imagePathsJson != null && !imagePathsJson.isEmpty() && !imagePathsJson.equals("[]")) {
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<String>>() {}.getType();
                    List<String> imagePaths = gson.fromJson(imagePathsJson, listType);

                    if (!imagePaths.isEmpty()) {
                        String imageUrl = baseUrl + "uploads/" + imagePaths.get(0);
                        Log.d(TAG, "Loading meal image from: " + imageUrl);
                        Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.sushi) // Placeholder image while loading
                                .error(R.drawable.sushi)       // Error image if loading fails
                                .into(mealImageView);
                    } else {
                        mealImageView.setImageResource(R.drawable.sushi); // Placeholder if no image paths
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing image paths: " + e.getMessage());
                    mealImageView.setImageResource(R.drawable.sushi); // Placeholder on error
                }
            } else {
                mealImageView.setImageResource(R.drawable.sushi); // Placeholder if no image paths
            }
            Log.d(TAG, "Meal image loading initiated");

            // Set click listener for the add to cart button
            addToCartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "addToCartButton clicked");
                    String quantityStr = quantityEditText.getText().toString().trim();
                    Log.d(TAG, "Entered quantity: \"" + quantityStr + "\"");
                    if (quantityStr.isEmpty()) {
                        Toast.makeText(MealDetailActivity.this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int quantity = Integer.parseInt(quantityStr);
                        Log.d(TAG, "Parsed quantity: " + quantity);
                        if (quantity <= 0) {
                            Toast.makeText(MealDetailActivity.this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        addToCart(currentMeal.getMealId(), quantity);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing quantity: " + e.getMessage());
                        Toast.makeText(MealDetailActivity.this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Log.d(TAG, "addToCartButton OnClickListener set");
        } else {
            // Handle the case where no meal was passed
            Log.e(TAG, "Error: Could not load meal details - Meal object is null");
            Toast.makeText(this, "Error: Could not load meal details", Toast.LENGTH_SHORT).show();
            finish(); // Go back to the previous activity
        }

        // Set click listener for the Go Back button
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "goBackButton clicked");
                Intent intent = new Intent(MealDetailActivity.this, MainActivity.class); // Assuming MainActivity is your orders page
                startActivity(intent);
                finish();
            }
        });
        Log.d(TAG, "goBackButton OnClickListener set");

        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "cartButton clicked");
                Intent intent = new Intent(MealDetailActivity.this, CartActivity.class); // Assuming CartActivity is your cart page
                startActivity(intent);
            }
        });
        Log.d(TAG, "cartButton OnClickListener set");
    }

    private void addToCart(int mealId, int quantity) {
        Log.d(TAG, "addToCart() called with mealId: " + mealId + ", quantity: " + quantity);
        String url = baseUrl + "cart.php";
        Log.d(TAG, "AddToCart URL: " + url);
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(com.android.volley.Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String TAG_RESPONSE = "AddToCartResponse";
                        Log.d(TAG_RESPONSE, "Raw Server Response Length: " + response.length()); // Log the length
                        if (response.length() > 4000) {
                            int chunkCount = response.length() / 4000;
                            for (int i = 0; i <= chunkCount; i++) {
                                int max = 4000 * (i + 1);
                                if (max >= response.length()) {
                                    Log.d(TAG_RESPONSE, "Raw Server Response Chunk " + i + ": " + response.substring(4000 * i));
                                } else {
                                    Log.d(TAG_RESPONSE, "Raw Server Response Chunk " + i + ": " + response.substring(4000 * i, max));
                                }
                            }
                        } else {
                            Log.d(TAG_RESPONSE, "Raw Server Response: " + response);
                        }
                        try {
                            org.json.JSONObject jsonResponse = new org.json.JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");
                            Log.d(TAG_RESPONSE, "Status: " + status + ", Message: " + message);

                            if (status.equals("success")) {
                                Toast.makeText(MealDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MealDetailActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (org.json.JSONException e) {
                            Log.e(TAG_RESPONSE, "JSON Parsing Error: " + e.getMessage());
                            Toast.makeText(MealDetailActivity.this, "Error parsing server response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(com.android.volley.VolleyError error) {
                        Log.e("AddToCartError", "Volley Error: " + error.getMessage());
                        Toast.makeText(MealDetailActivity.this, "Error adding to cart: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("user_id", String.valueOf(loggedInUserId));
                params.put("meal_id", String.valueOf(mealId));
                params.put("quantity", String.valueOf(quantity));
                Log.d(TAG, "AddToCart Parameters: " + params);
                return params;
            }
        };
        requestQueue.add(request);
        Log.d(TAG, "AddToCart Volley request added to queue");
    }
}