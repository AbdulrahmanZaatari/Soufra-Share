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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MealDetailActivity extends AppCompatActivity {

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

    // Replace with the actual logged-in user ID
    private int loggedInUserId = 1; // Placeholder for user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

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

        requestQueue = Volley.newRequestQueue(this);

        // Get the Meal object passed from the previous activity
        currentMeal = (Meal) getIntent().getSerializableExtra("meal");

        if (currentMeal != null) {
            // Populate views with meal details
            mealNameTextView.setText(currentMeal.getName());
            mealDescriptionTextView.setText(currentMeal.getDescription());
            mealPriceTextView.setText("Price: $" + String.format("%.2f", currentMeal.getPrice()));
            mealLocationTextView.setText("Location: " + currentMeal.getLocation());
            mealDeliveryOptionTextView.setText("Delivery Available: " + (currentMeal.getDeliveryOption() == 1 ? "Yes" : "No"));

            // Load meal image (replace with your actual image loading logic later)
            mealImageView.setImageResource(R.drawable.meal); // Placeholder image

            // Set click listener for the add to cart button
            addToCartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String quantityStr = quantityEditText.getText().toString().trim();
                    if (quantityStr.isEmpty()) {
                        Toast.makeText(MealDetailActivity.this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity <= 0) {
                        Toast.makeText(MealDetailActivity.this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addToCart(currentMeal.getMealId(), quantity);
                }
            });
        } else {
            // Handle the case where no meal was passed
            Toast.makeText(this, "Error: Could not load meal details", Toast.LENGTH_SHORT).show();
            finish(); // Go back to the previous activity
        }

        // Set click listener for the Go Back button
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MealDetailActivity.this, MainActivity.class); // Assuming MainActivity is your orders page
                startActivity(intent);
                finish(); // Optional: Finish the current activity
            }
        });

        // Set click listener for the Cart button
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MealDetailActivity.this, CartActivity.class); // Assuming CartActivity is your cart page
                startActivity(intent);
            }
        });
    }

    private void addToCart(int mealId, int quantity) {
        String url = "http://10.0.2.2/Soufra_Share/cart.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String TAG = "AddToCart";
                        Log.d(TAG, "Raw Server Response Length: " + response.length()); // Log the length
                        if (response.length() > 4000) {
                            int chunkCount = response.length() / 4000;
                            for (int i = 0; i <= chunkCount; i++) {
                                int max = 4000 * (i + 1);
                                if (max >= response.length()) {
                                    Log.d(TAG, "Raw Server Response Chunk " + i + ": " + response.substring(4000 * i));
                                } else {
                                    Log.d(TAG, "Raw Server Response Chunk " + i + ": " + response.substring(4000 * i, max));
                                }
                            }
                        } else {
                            Log.d(TAG, "Raw Server Response: " + response);
                        }
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if (status.equals("success")) {
                                Toast.makeText(MealDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MealDetailActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
                            Toast.makeText(MealDetailActivity.this, "Error parsing server response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("AddToCart", "Volley Error: " + error.getMessage());
                        Toast.makeText(MealDetailActivity.this, "Error adding to cart: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(loggedInUserId)); // Replace with actual user ID retrieval
                params.put("meal_id", String.valueOf(mealId));
                params.put("quantity", String.valueOf(quantity));
                return params;
            }
        };
        requestQueue.add(request);
    }
}