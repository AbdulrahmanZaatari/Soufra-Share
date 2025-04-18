package com.example.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesReportListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private String PREF_NAME = "MyAppPrefs";
    private SalesReportListAdapter adapter;
    private List<SalesRecord> salesRecordList;
    private String baseUrl = "http://10.0.2.2/Soufra_Share/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_report_list);

        recyclerView = findViewById(R.id.recyclerViewSalesReports);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        salesRecordList = new ArrayList<>();
        adapter = new SalesReportListAdapter(this, salesRecordList);
        recyclerView.setAdapter(adapter);

        fetchSalesDates();
    }

    private void fetchSalesDates() {
        String url = baseUrl + "get_sales_dates.php";

        int loggedInUserId = getLoggedInUserId();

        com.android.volley.toolbox.StringRequest stringRequest = new com.android.volley.toolbox.StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("SalesReport", "Response: " + response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String saleDate = jsonObject.getString("sale_date");
                            double totalSales = jsonObject.getDouble("total_sales");
                            salesRecordList.add(new SalesRecord(id, saleDate, totalSales));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("SalesReport", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing sales data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("SalesReport", "Volley Error: " + error.getMessage());
                    Toast.makeText(this, "Error fetching sales data", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(loggedInUserId)); // Send userId as seller_id
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private int getLoggedInUserId() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getInt("user_id", -1 );
    }
}