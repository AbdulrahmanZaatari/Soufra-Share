package com.example.project;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 123;
    private Context context;
    private List<Order> orderList;

    public OrderHistoryAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order currentOrder = orderList.get(position);
        holder.textViewOrderId.setText("Order ID: " + currentOrder.getOrderId());
        holder.textViewOrderDate.setText("Date: " + currentOrder.getOrderDate());
        holder.textViewTotalPrice.setText("Total: $" + String.format("%.2f", currentOrder.getTotalAmount()));

        String baseUrl = "http://10.0.2.2/Soufra_Share/"; // Your base URL

        holder.buttonDownloadReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int orderId = currentOrder.getOrderId();
                String downloadUrl = baseUrl + "download_receipt.php?order_id=" + orderId;
                startDownload(downloadUrl, "receipt_order_" + orderId + ".pdf");
            }
        });

        holder.buttonViewReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int orderId = currentOrder.getOrderId();
                String pdfUrl = baseUrl + "download_receipt.php?order_id=" + orderId;
                Log.d("OrderHistoryAdapter", "View PDF URL: " + pdfUrl); // Added logging here
                Intent intent = new Intent(context, ViewReceiptActivity.class);
                intent.putExtra("PDF_URL", pdfUrl);
                context.startActivity(intent);
            }
        });
    }

    private void startDownload(String downloadUrl, String filename) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setTitle(filename);
        request.setDescription("Downloading receipt...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(context, "Downloading receipt...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error initializing download manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewOrderId;
        public TextView textViewOrderDate;
        public TextView textViewTotalPrice;
        public Button buttonDownloadReceipt;
        public Button buttonViewReceipt; // Add this

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            textViewTotalPrice = itemView.findViewById(R.id.textViewTotalPrice);
            buttonDownloadReceipt = itemView.findViewById(R.id.buttonDownloadReceipt);
            buttonViewReceipt = itemView.findViewById(R.id.buttonViewReceipt);
        }
    }
}