package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private TextView totalPriceTextView;
    private TextView emptyCartTextView;
    private Button orderNowButton;
    private Button backButton;
    private int userId = 1;
    private String userEmail = "user@example.com";

    // Removed senderEmail and senderPassword

    public interface QuantityCheckCallback {
        void onQuantityChecked(boolean isAvailable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting onCreate");
        setContentView(R.layout.activity_cart);
        Log.d(TAG, "onCreate: setContentView finished");

        backButton = findViewById(R.id.back_button);
        Log.d(TAG, "onCreate: backButton found: " + (backButton != null));
        cartRecyclerView = findViewById(R.id.cart_recycler_view);
        Log.d(TAG, "onCreate: cartRecyclerView found: " + (cartRecyclerView != null));
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "onCreate: LinearLayoutManager set");

        totalPriceTextView = findViewById(R.id.total_price_text_view);
        Log.d(TAG, "onCreate: totalPriceTextView found: " + (totalPriceTextView != null));
        emptyCartTextView = findViewById(R.id.empty_cart_text_view);
        Log.d(TAG, "onCreate: emptyCartTextView found: " + (emptyCartTextView != null));
        orderNowButton = findViewById(R.id.order_now_button);
        Log.d(TAG, "onCreate: orderNowButton found: " + (orderNowButton != null));

        cartAdapter = new CartAdapter(this, cartItems);
        Log.d(TAG, "onCreate: CartAdapter created");
        cartRecyclerView.setAdapter(cartAdapter);
        Log.d(TAG, "onCreate: CartAdapter set to RecyclerView");

        // Set up delete click listener
        cartAdapter.setOnDeleteClickListener(position -> {
            Log.d(TAG, "onCreate: Delete click listener triggered for position: " + position);
            CartItem itemToDelete = cartItems.get(position);
            deleteItemFromCart(itemToDelete.getCartId(), position);
        });
        Log.d(TAG, "onCreate: Delete click listener set");

        orderNowButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: Order Now button clicked");
            if (!cartItems.isEmpty()) {
                showConfirmationDialog();
            } else {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d(TAG, "onCreate: Order Now click listener set");

        // Set up back button click listener
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: Back button clicked");
            finish();
        });
        Log.d(TAG, "onCreate: Back button click listener set");

        fetchCartData(userId);
        Log.d(TAG, "onCreate: fetchCartData called");
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Order")
                .setMessage("You are about to place orders for items from different sellers. Do you want to continue?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Log.d(TAG, "showConfirmationDialog: User confirmed order");
                    processOrderForMultipleSellers();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Log.d(TAG, "showConfirmationDialog: User cancelled order");
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void processOrderForMultipleSellers() {
        Log.d(TAG, "processOrderForMultipleSellers: Starting order processing for multiple sellers");
        final Map<Integer, List<CartItem>> itemsBySeller = new HashMap<>();
        final int totalItems = cartItems.size();
        final boolean[] sellerFetchCompleted = new boolean[totalItems]; // To track completion of seller ID fetching
        final int[] itemsProcessed = {0}; // Counter for processed items

        for (int i = 0; i < cartItems.size(); i++) {
            final int index = i;
            CartItem cartItem = cartItems.get(index);
            int mealId = cartItem.getMealId();
            String url = "http://10.0.2.2/Soufra_Share/get_meal_seller.php?meal_id=" + mealId;
            Log.d(TAG, "processOrderForMultipleSellers: Fetching seller ID for meal ID: " + mealId + " from URL: " + url);

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        Log.d(TAG, "processOrderForMultipleSellers: Response received for meal ID " + mealId + ": " + response);
                        try {
                            Gson gson = new Gson();
                            java.util.Map<String, Integer> map = gson.fromJson(response, new TypeToken<java.util.Map<String, Integer>>() {
                            }.getType());
                            if (map.containsKey("seller_id")) {
                                int sellerId = map.get("seller_id");
                                Log.d(TAG, "processOrderForMultipleSellers: Seller ID fetched for meal ID " + mealId + ": " + sellerId);
                                if (!itemsBySeller.containsKey(sellerId)) {
                                    itemsBySeller.put(sellerId, new ArrayList<>());
                                }
                                itemsBySeller.get(sellerId).add(cartItem);
                            } else {
                                Log.e(TAG, "processOrderForMultipleSellers: Seller ID not found in response for meal ID " + mealId);
                                Toast.makeText(this, "Error fetching seller information for one or more items", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "processOrderForMultipleSellers: Error parsing response for meal ID " + mealId, e);
                            Toast.makeText(this, "Error fetching seller information for one or more items", Toast.LENGTH_SHORT).show();
                        } finally {
                            sellerFetchCompleted[index] = true;
                            itemsProcessed[0]++;
                            if (itemsProcessed[0] == totalItems) {
                                // All seller IDs have been fetched, now place orders
                                placeOrdersBySeller(itemsBySeller);
                            }
                        }
                    }, error -> {
                Log.e(TAG, "processOrderForMultipleSellers: Error fetching seller ID for meal ID " + mealId + ": " + error.getMessage());
                Toast.makeText(this, "Error fetching seller information for one or more items", Toast.LENGTH_SHORT).show();
                sellerFetchCompleted[index] = true;
                itemsProcessed[0]++;
                if (itemsProcessed[0] == totalItems) {
                    // All (attempted) seller ID fetches completed
                    placeOrdersBySeller(itemsBySeller);
                }
            });
            requestQueue.add(stringRequest);
            Log.d(TAG, "processOrderForMultipleSellers: Request added to queue for meal ID: " + mealId);
        }
    }

    private void placeOrdersBySeller(Map<Integer, List<CartItem>> itemsBySeller) {
        Log.d(TAG, "placeOrdersBySeller: Placing orders for each seller");
        for (Map.Entry<Integer, List<CartItem>> entry : itemsBySeller.entrySet()) {
            int sellerId = entry.getKey();
            List<CartItem> items = entry.getValue();
            placeOrderForSeller(sellerId, items);
        }
    }

    private void placeOrderForSeller(int sellerId, List<CartItem> items) {
        Log.d(TAG, "placeOrderForSeller: Placing order for seller ID: " + sellerId + " with " + items.size() + " items");

        final int[] itemsChecked = {0};
        final boolean[] allAvailable = {true};
        final List<CartItem> unavailableItems = new ArrayList<>();

        for (CartItem cartItem : items) {
            checkMealQuantity(cartItem.getMealId(), cartItem.getQuantity(), isAvailable -> {
                itemsChecked[0]++;
                if (!isAvailable) {
                    allAvailable[0] = false;
                    unavailableItems.add(cartItem);
                    Toast.makeText(CartActivity.this, "Quantity not available for " + cartItem.getMealName(), Toast.LENGTH_LONG).show();
                }

                if (itemsChecked[0] == items.size()) {
                    if (allAvailable[0]) {
                        processOrderItemsForSeller(sellerId, items);
                    } else {
                        Log.w(TAG, "placeOrderForSeller: Not all items available for seller ID " + sellerId);
                        // Optionally, you can inform the user about all unavailable items here
                    }
                }
            });
        }
    }

    private void processOrderItemsForSeller(int sellerId, List<CartItem> items) {
        double totalPrice = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();
        List<Integer> mealIdsToDecrease = new ArrayList<>();
        List<Integer> quantitiesToDecrease = new ArrayList<>();
        StringBuilder orderSummary = new StringBuilder();

        for (CartItem cartItem : items) {
            totalPrice += cartItem.getPrice() * cartItem.getQuantity();
            orderItems.add(new OrderItem(cartItem.getMealId(), cartItem.getQuantity(), cartItem.getPrice()));
            mealIdsToDecrease.add(cartItem.getMealId());
            quantitiesToDecrease.add(cartItem.getQuantity());
            orderSummary.append(cartItem.getQuantity()).append(" x ").append(cartItem.getMealName()).append(" ($").append(String.format("%.2f", cartItem.getPrice())).append(")\n");
        }

        Gson gson = new Gson();
        String orderItemsJson = gson.toJson(orderItems);
        final double finalTotalPrice = totalPrice; // Make totalPrice final for the inner class
        final String finalOrderSummary = orderSummary.toString();

        String url = "http://10.0.2.2/Soufra_Share/place_order.php"; // Replace with your actual URL
        Log.d(TAG, "placeOrderForSeller: Sending order request to: " + url);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        com.android.volley.toolbox.StringRequest stringRequest = new com.android.volley.toolbox.StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "placeOrderForSeller: Response received for seller ID " + sellerId + ": " + response);
                    try {
                        java.util.Map<String, String> map = gson.fromJson(response, new TypeToken<java.util.Map<String, String>>() {
                        }.getType());
                        if (map.containsKey("status") && map.get("status").equals("success")) {
                            Toast.makeText(this, "Order placed successfully for seller " + sellerId + "!", Toast.LENGTH_SHORT).show();
                            // Decrease the quantity in the meals table
                            for (int i = 0; i < mealIdsToDecrease.size(); i++) {
                                decreaseMealQuantity(mealIdsToDecrease.get(i), quantitiesToDecrease.get(i));
                            }
                            // Remove the ordered items from the cart
                            cartItems.removeAll(items);
                            cartAdapter.notifyDataSetChanged();
                            updateTotalPrice();
                            checkEmptyCart();
                            // Removed sendConfirmationEmail call
                        } else {
                            Toast.makeText(this, "Error placing order for seller " + sellerId + ": " + map.get("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "placeOrderForSeller: Error parsing response for seller ID " + sellerId, e);
                        Toast.makeText(this, "Error processing order response for seller " + sellerId, Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e(TAG, "placeOrderForSeller: Error placing order for seller ID " + sellerId + ": " + error.getMessage());
            Toast.makeText(this, "Error placing order for seller " + sellerId, Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("buyer_id", String.valueOf(userId));
                params.put("seller_id", String.valueOf(sellerId));
                params.put("total_price", String.valueOf(finalTotalPrice));
                params.put("order_items", orderItemsJson); // Send the list of order items as JSON
                return params;
            }
        };

        requestQueue.add(stringRequest);
        Log.d(TAG, "placeOrderForSeller: Request added to queue for seller ID: " + sellerId);
    }

    private void checkMealQuantity(int mealId, int quantity, QuantityCheckCallback callback) {
        String url = "http://10.0.2.2/Soufra_Share/check_meal_quantity.php?meal_id=" + mealId + "&quantity=" + quantity;
        Log.d(TAG, "checkMealQuantity: Checking availability for meal ID " + mealId + ", quantity ordered: " + quantity);
        Log.d(TAG, "checkMealQuantity: URL being called: " + url);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "checkMealQuantity: Response received: " + response);
                    boolean isAvailable = false;
                    try {
                        Gson gson = new Gson();
                        java.util.Map<String, Object> map = gson.fromJson(response, new TypeToken<java.util.Map<String, Object>>() {
                        }.getType());
                        if (map.containsKey("available")) {
                            isAvailable = (boolean) map.get("available");
                        } else {
                            Log.e(TAG, "checkMealQuantity: 'available' key not found in response");
                        }
                        if (map.containsKey("quantity_available")) {
                            Log.d(TAG, "checkMealQuantity: Quantity available in DB for meal ID " + mealId + ": " + ((Number) map.get("quantity_available")).intValue());
                        } else if (map.containsKey("message")) {
                            Log.d(TAG, "checkMealQuantity: Message from server: " + map.get("message"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "checkMealQuantity: Error parsing response", e);
                    } finally {
                        callback.onQuantityChecked(isAvailable); // Execute the callback with the result
                    }
                }, error -> {
            Log.e(TAG, "checkMealQuantity: Error checking quantity: " + error.getMessage());
            callback.onQuantityChecked(false); // Execute the callback with false in case of error
        });
        requestQueue.add(stringRequest);
        Log.d(TAG, "checkMealQuantity: Request added to queue");
    }

    private void decreaseMealQuantity(int mealId, int quantity) {
        String url = "http://10.0.2.2/Soufra_Share/decrease_meal_quantity.php?meal_id=" + mealId + "&quantity=" + quantity;
        Log.d(TAG, "decreaseMealQuantity: Decreasing quantity for meal ID " + mealId + " by " + quantity);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "decreaseMealQuantity: Response received: " + response);
                    try {
                        Gson gson = new Gson();
                        java.util.Map<String, String> map = gson.fromJson(response, new TypeToken<java.util.Map<String, String>>() {
                        }.getType());
                        if (map.containsKey("status") && map.get("status").equals("success")) {
                            Log.d(TAG, "decreaseMealQuantity: Quantity decreased successfully for meal ID " + mealId);
                        } else {
                            Log.e(TAG, "decreaseMealQuantity: Error decreasing quantity for meal ID " + mealId + ": " + map.get("message"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "decreaseMealQuantity: Error parsing response", e);
                    }
                }, error -> {
            Log.e(TAG, "decreaseMealQuantity: Error decreasing quantity: " + error.getMessage());
        });
        requestQueue.add(stringRequest);
    }

    // Removed sendConfirmationEmail method

    private void fetchCartData(int userId) {
        String url = "http://10.0.2.2/Soufra_Share/get_cart.php?user_id=" + userId;
        Log.d(TAG, "fetchCartData: Fetching data from URL: " + url);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "fetchCartData: Response received: " + response);
                    Gson gson = new Gson();
                    Type cartListType = new TypeToken<List<CartItem>>() {}.getType();
                    List<CartItem> fetchedCartItems = gson.fromJson(response, cartListType);
                    cartItems.clear();
                    cartItems.addAll(fetchedCartItems);
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                    checkEmptyCart();
                    Log.d(TAG, "fetchCartData: Cart data fetched and updated");
                }, error -> {
            Log.e(TAG, "fetchCartData: Error fetching cart data: " + error.getMessage());
            checkEmptyCart(); // Still check for empty state in case of error
        });

        requestQueue.add(stringRequest);
        Log.d(TAG, "fetchCartData: Request added to queue");
    }

    private void deleteItemFromCart(int cartId, int position) {
        String url = "http://10.0.2.2/Soufra_Share/delete_from_cart.php?cart_id=" + cartId;
        Log.d(TAG, "deleteItemFromCart: Deleting item with cartId: " + cartId + " from URL: " + url);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, // Using GET for simplicity, but DELETE might be more appropriate
                response -> {
                    Log.d(TAG, "deleteItemFromCart: Response received: " + response);
                    try {
                        Gson gson = new Gson();
                        java.util.Map<String, String> map = gson.fromJson(response, new TypeToken<java.util.Map<String, String>>() {
                        }.getType());
                        if (map.containsKey("status") && map.get("status").equals("success")) {
                            Log.d(TAG, "deleteItemFromCart: Item deleted successfully");
                            cartItems.remove(position);
                            cartAdapter.notifyItemRemoved(position);
                            updateTotalPrice();
                            checkEmptyCart();
                        } else {
                            Log.e(TAG, "deleteItemFromCart: Error deleting item: " + map.get("message"));
                            Toast.makeText(this, "Error deleting item: " + map.get("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "deleteItemFromCart: Error parsing delete response: " + e.getMessage());
                        Toast.makeText(this, "Error processing delete response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e(TAG, "deleteItemFromCart: Error deleting item: " + error.getMessage());
            Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(stringRequest);
        Log.d(TAG, "deleteItemFromCart: Request added to queue");
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPriceTextView.setText(String.format("$%.2f", total));
        Log.d(TAG, "updateTotalPrice: Total price updated to: $" + total);
    }

    private void checkEmptyCart() {
        if (cartItems.isEmpty()) {
            emptyCartTextView.setVisibility(View.VISIBLE);
            cartRecyclerView.setVisibility(View.GONE);
            Log.d(TAG, "checkEmptyCart: Cart is empty");
        } else {
            emptyCartTextView.setVisibility(View.GONE);
            cartRecyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "checkEmptyCart: Cart is not empty");
        }
    }

    private void processOrder() {
        Log.d(TAG, "processOrder: Starting order processing");
        if (!cartItems.isEmpty()) {
            showConfirmationDialog();
        } else {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper class to send order items to the backend
    private static class OrderItem {
        private int meal_id;
        private int quantity;
        private double price;

        public OrderItem(int meal_id, int quantity, double price) {
            this.meal_id = meal_id;
            this.quantity = quantity;
            this.price = price;
        }

        public int getMeal_id() {
            return meal_id;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }
    }
}