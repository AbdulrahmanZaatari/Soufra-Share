package com.example.project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private TextView totalPriceTextView;
    private TextView emptyCartTextView;
    private int userId = 1; // Replace with the actual user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecyclerView = findViewById(R.id.cart_recycler_view);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        totalPriceTextView = findViewById(R.id.total_price_text_view);
        emptyCartTextView = findViewById(R.id.empty_cart_text_view);

        cartAdapter = new CartAdapter(this, cartItems);
        cartRecyclerView.setAdapter(cartAdapter);

        // Set up delete click listener
        cartAdapter.setOnDeleteClickListener(position -> {
            CartItem itemToDelete = cartItems.get(position);
            deleteItemFromCart(itemToDelete.getCartId(), position);
        });

        fetchCartData(userId);
    }

    private void fetchCartData(int userId) {
        String url = "http://10.0.2.2/Soufra_Share/get_cart.php?user_id=" + userId;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Gson gson = new Gson();
                    Type cartListType = new TypeToken<List<CartItem>>() {}.getType();
                    List<CartItem> fetchedCartItems = gson.fromJson(response, cartListType);
                    cartItems.clear();
                    cartItems.addAll(fetchedCartItems);
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                    checkEmptyCart();
                    Log.d("CartActivity", "Cart data fetched successfully: " + response);
                }, error -> {
            Log.e("CartActivity", "Error fetching cart data: " + error.getMessage());
            checkEmptyCart(); // Still check for empty state in case of error
        });

        requestQueue.add(stringRequest);
    }

    private void deleteItemFromCart(int cartId, int position) {
        String url = "http://10.0.2.2/Soufra_Share/delete_from_cart.php?cart_id=" + cartId;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, // Using GET for simplicity, but DELETE might be more appropriate
                response -> {
                    try {
                        Gson gson = new Gson();
                        java.util.Map<String, String> map = gson.fromJson(response, new TypeToken<java.util.Map<String, String>>(){}.getType());
                        if (map.containsKey("status") && map.get("status").equals("success")) {
                            Log.d("CartActivity", "Item deleted successfully: " + response);
                            cartItems.remove(position);
                            cartAdapter.notifyItemRemoved(position);
                            updateTotalPrice();
                            checkEmptyCart();
                        } else {
                            Log.e("CartActivity", "Error deleting item: " + map.get("message"));
                            // Handle error (e.g., show a message to the user)
                        }
                    } catch (Exception e) {
                        Log.e("CartActivity", "Error parsing delete response: " + e.getMessage());
                    }
                }, error -> {
            Log.e("CartActivity", "Error deleting item: " + error.getMessage());
            // Handle error (e.g., show a message to the user)
        });
        requestQueue.add(stringRequest);
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPriceTextView.setText(String.format("$%.2f", total));
    }

    private void checkEmptyCart() {
        if (cartItems.isEmpty()) {
            emptyCartTextView.setVisibility(View.VISIBLE);
            cartRecyclerView.setVisibility(View.GONE);
        } else {
            emptyCartTextView.setVisibility(View.GONE);
            cartRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}