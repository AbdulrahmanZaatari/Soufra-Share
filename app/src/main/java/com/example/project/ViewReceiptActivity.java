package com.example.project;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class ViewReceiptActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_receipt);

        webView = findViewById(R.id.webViewReceipt);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setJavaScriptEnabled(true);

        String pdfUrl = getIntent().getStringExtra("PDF_URL");

        if (pdfUrl != null) {
            webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + pdfUrl);
        } else {
            finish();
        }
    }
}