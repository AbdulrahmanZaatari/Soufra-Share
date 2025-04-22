package com.example.project;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SalesReportListAdapter extends RecyclerView.Adapter<SalesReportListAdapter.ViewHolder> {

    private Context context;
    private List<SalesRecord> salesRecordList;
    private String baseUrl = "http://10.0.2.2/Soufra_Share/";
    private static final String PREF_NAME = "MyAppPrefs";

    public SalesReportListAdapter(Context context, List<SalesRecord> salesRecordList) {
        this.context = context;
        this.salesRecordList = salesRecordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sales_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SalesRecord salesRecord = salesRecordList.get(position);
        holder.textViewSaleDate.setText("Date: " + salesRecord.getSaleDate());
        holder.textViewTotalSales.setText("Total Sales: $" + String.format("%.2f", salesRecord.getTotalSales()));

        holder.buttonDownloadReport.setOnClickListener(v -> {
            String saleDate = salesRecord.getSaleDate();
            int userId = getUserId();

            if (userId == -1) {
                Toast.makeText(context, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
                return;
            }

            String downloadUrl = baseUrl + "generate_sales_report.php?date=" + saleDate + "&user_id=" + userId;
            String filename = "sales_report_" + saleDate + ".pdf";
            downloadPdf(downloadUrl, filename);
        });
    }

    private int getUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private void downloadPdf(String downloadUrl, String filename) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setTitle(filename);
        request.setDescription("Downloading report...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(context, "Downloading report...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error initializing download manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return salesRecordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSaleDate;
        TextView textViewTotalSales;
        Button buttonDownloadReport;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSaleDate = itemView.findViewById(R.id.textViewSaleDate);
            textViewTotalSales = itemView.findViewById(R.id.textViewTotalSales);
            buttonDownloadReport = itemView.findViewById(R.id.buttonDownloadReport);
        }
    }
}
