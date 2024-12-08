package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrackingActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout trackingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        trackingLayout = findViewById(R.id.trackingLayout);
        db = FirebaseFirestore.getInstance();

        loadTrackingDetails();
    }

    private void loadTrackingDetails() {
        // Fetch the tracking details
        db.collection("requests")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String productName = document.getString("productName");
                        String labelNumber = document.getString("labelNumber"); // Get the label number
                        List<Map<String, Object>> statusHistory = (List<Map<String, Object>>) document.get("statusHistory");

                        if (productName != null && labelNumber != null && statusHistory != null) {
                            // Create a TextView for each product with label number and product name
                            TextView productTextView = new TextView(this);
                            productTextView.setText("Label: " + labelNumber + "\nProduct Name: " + productName); // Format text
                            productTextView.setTextSize(18);
                            productTextView.setPadding(16, 16, 16, 16);
                            productTextView.setOnClickListener(v -> {
                                Intent intent = new Intent(TrackingActivity.this, ProductTrackingDetailsActivity.class);
                                intent.putExtra("productName", productName); // Pass product name
                                intent.putExtra("labelNumber", labelNumber); // Pass label number
                                intent.putExtra("statusHistory", (ArrayList<Map<String, Object>>) statusHistory); // Pass status history
                                startActivity(intent);
                            });
                            trackingLayout.addView(productTextView);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("TrackingActivity", "Error loading tracking details: ", e));
    }
}
