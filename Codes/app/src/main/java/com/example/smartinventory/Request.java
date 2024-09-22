package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Request extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        db = FirebaseFirestore.getInstance();

        // Load requests based on their status
        loadRequests("New", R.id.newRequestsLayout);
        loadRequests("Processing", R.id.pendingRequestsLayout);
        loadRequests("Shipped", R.id.completedRequestsLayout);

        // Set onClickListeners for status TextViews
        findViewById(R.id.newRequestsTV).setOnClickListener(v -> {
            Toast.makeText(this, "Viewing New Requests", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.pendingRequestsTV).setOnClickListener(v -> {
            Toast.makeText(this, "Viewing Pending Requests", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.completedRequestsTV).setOnClickListener(v -> {
            Toast.makeText(this, "Viewing Completed Requests", Toast.LENGTH_SHORT).show();
        });
    }

    protected void onResume() {
        super.onResume();
        // Reload requests when the activity is resumed
        loadRequests("New", R.id.newRequestsLayout);
        loadRequests("Pending", R.id.pendingRequestsLayout);
        loadRequests("Completed", R.id.completedRequestsLayout);
    }

    private void loadRequests(String status, int containerId) {
        db.collection("requests")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.d("Request", "No requests found for status: " + status);
                    } else {
                        LinearLayout container = findViewById(containerId);
                        container.removeAllViews(); // Clear previous views
                        for (DocumentSnapshot document : querySnapshot) {
                            String productName = document.getString("productName");
                            Object quantityObj = document.get("quantity");
                            int quantity = quantityObj instanceof Long ? ((Long) quantityObj).intValue() : (Integer) quantityObj;
                            String requestId = document.getId();
                            String username = document.getString("username");

                            addRequestToLayout(container, productName, quantity, requestId, username, status);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Request", "Error fetching requests: ", e);
                });
    }

    private void addRequestToLayout(LinearLayout container, String productName, int quantity, String requestId, String username, String status) {
        View requestView = LayoutInflater.from(this).inflate(R.layout.request_item, container, false);

        TextView requestDetailsTV = requestView.findViewById(R.id.requestDetailsTV);
        Spinner statusSpinner = requestView.findViewById(R.id.statusSpinner);
        Button updateStatusButton = requestView.findViewById(R.id.updateStatusButton);

        requestDetailsTV.setText(productName + " - Quantity: " + quantity);

        // Set onClickListener to navigate to RequestItem.java with request details
        requestView.setOnClickListener(v -> navigateToRequestItem(requestId, username, status));

        updateStatusButton.setOnClickListener(v -> {
            String newStatus = statusSpinner.getSelectedItem().toString();
            updateRequestStatus(requestId, newStatus);
        });

        container.addView(requestView);
    }

    private void updateRequestStatus(String requestId, String newStatus) {
        db.collection("requests").document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if ("Cancelled".equals(newStatus)) {
                        db.collection("requests").document(requestId)
                                .delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(Request.this, "Request cancelled and removed.", Toast.LENGTH_SHORT).show();
                                    recreate(); // Refresh activity
                                })
                                .addOnFailureListener(e -> Log.e("Request", "Failed to delete request.", e));
                    } else {
                        Toast.makeText(Request.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                        recreate();  // Refresh the activity to move the request to the respective section
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Request.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                });
    }


    private void navigateToRequestItem(String requestId, String username, String status) {
        Log.d("Request", "Navigating to RequestItem with requestId: " + requestId);
        Intent intent = new Intent(Request.this, RequestItem.class);
        intent.putExtra("requestId", requestId);  // Pass the requestId
        intent.putExtra("username", username);    // Pass the username
        intent.putExtra("status", status);        // Pass the status
        startActivity(intent);
    }


}
