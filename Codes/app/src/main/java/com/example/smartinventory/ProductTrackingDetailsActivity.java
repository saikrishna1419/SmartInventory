package com.example.smartinventory;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductTrackingDetailsActivity extends AppCompatActivity {

    private TextView productNameTV;
    private TextView labelNumberTV;
    private TextView statusHistoryTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_tracking_details);

        productNameTV = findViewById(R.id.productNameTV);
        labelNumberTV = findViewById(R.id.labelNumberTV);
        statusHistoryTV = findViewById(R.id.statusHistoryTV);

        // Get data from intent
        String productName = getIntent().getStringExtra("productName");
        String labelNumber = getIntent().getStringExtra("labelNumber"); // Get label number
        ArrayList<Map<String, Object>> statusHistory = (ArrayList<Map<String, Object>>) getIntent().getSerializableExtra("statusHistory");

        // Set product name and label number
        productNameTV.setText("Product Name: " + productName);
        labelNumberTV.setText("Label Number: " + labelNumber);

        // Display status history
        if (statusHistory != null) {
            StringBuilder statusInfo = new StringBuilder();
            for (Map<String, Object> status : statusHistory) {
                String statusStr = (String) status.get("status");
                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) status.get("timestamp");

                // Convert Timestamp to Date and format it
                String time = (timestamp != null) ? timestamp.toDate().toString() : "Unknown time";
                statusInfo.append("Status: ").append(statusStr).append("\n")
                        .append("Time: ").append(time).append("\n\n");
            }
            statusHistoryTV.setText(statusInfo.toString());
        }
    }
}
