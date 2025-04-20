package com.example.project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderHistoryActivity extends AppCompatActivity {

    private static final String TAG = "OrderHistoryActivity";
    private RecyclerView recyclerView;
    private OrderHistoryAdapter adapter;
    private List<Order> orderList;
    private ProgressBar progressBar;
    private static final String URL_ORDERS = "http://10.0.2.2/Soufra_Share/get_order_history.php";

    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_USER_ID = "user_id";

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 123; // Add this constant

    private int getLoggedInUserId() {
        Log.d(TAG, "getLoggedInUserId() called");
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(KEY_USER_ID, -1);
        Log.d(TAG, "getLoggedInUserId() returned: " + userId);
        return userId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_order_history);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();

        Log.d(TAG, "Calling loadOrders() from onCreate()");
        loadOrders();
    }

    private void loadOrders() {
        Log.d(TAG, "loadOrders() called");
        progressBar.setVisibility(View.VISIBLE);
        int userId = getLoggedInUserId();
        Log.d(TAG, "loadOrders() - Retrieved userId: " + userId);

        if (userId == -1) {
            Log.w(TAG, "loadOrders() - User not logged in");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "loadOrders() - Creating StringRequest");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_ORDERS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "loadOrders() - onResponse() called");
                        Log.d(TAG, "loadOrders() - Response: " + response);
                        progressBar.setVisibility(View.GONE);
                        try {
                            Log.d(TAG, "loadOrders() - Parsing JSON response");
                            JSONArray jsonArray = new JSONArray(response);
                            Log.d(TAG, "loadOrders() - JSON array length: " + jsonArray.length());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Log.d(TAG, "loadOrders() - Processing order at index: " + i);
                                JSONObject orderObject = jsonArray.getJSONObject(i);
                                int orderId = orderObject.getInt("order_id");
                                double totalPrice = orderObject.getDouble("total_price");
                                String orderDate = orderObject.getString("order_date");

                                Log.d(TAG, "loadOrders() - Order details - ID: " + orderId + ", Date: " + orderDate + ", Total: " + totalPrice);
                                Order order = new Order(orderId, orderDate, totalPrice);
                                orderList.add(order);
                                Log.d(TAG, "loadOrders() - Order added to list. List size: " + orderList.size());
                            }
                            Log.d(TAG, "loadOrders() - Creating and setting adapter");
                            adapter = new OrderHistoryAdapter(OrderHistoryActivity.this, orderList);
                            recyclerView.setAdapter(adapter);
                            Log.d(TAG, "loadOrders() - Adapter set");
                        } catch (JSONException e) {
                            Log.e(TAG, "loadOrders() - JSONException: " + e.getMessage(), e);
                            Toast.makeText(getApplicationContext(), "Error parsing orders", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "loadOrders() - onErrorResponse() called. Error: " + error.toString(), error);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Error fetching orders: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Log.d(TAG, "loadOrders() - getParams() called");
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                Log.d(TAG, "loadOrders() - Params: " + params.toString());
                return params;
            }
        };

        Log.d(TAG, "loadOrders() - Getting RequestQueue instance");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        Log.d(TAG, "loadOrders() - Adding request to the queue");
        requestQueue.add(stringRequest);
        Log.d(TAG, "loadOrders() - Request added to the queue");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted. Please click download again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot download receipt.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }
}