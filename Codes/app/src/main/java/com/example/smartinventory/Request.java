package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Request extends AppCompatActivity {

    private FirebaseFirestore db;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        db = FirebaseFirestore.getInstance();
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Load requests initially
        loadAllRequests();

        // Swipe-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadAllRequests();
            swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation once data is loaded
        });
    }

    // Function to load all request statuses
    private void loadAllRequests() {
        loadRequests("New", R.id.newRequestsLayout);
        loadRequests("Processing", R.id.pendingRequestsLayout);
        loadRequests("Shipped", R.id.completedRequestsLayout);
    }

    private void loadRequests(String status, int containerId) {
        db.collection("requests")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.d("Request", "No requests found for status: " + status);
                        Toast.makeText(Request.this, "No requests found", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Request", "Found " + querySnapshot.size() + " requests for status: " + status);
                        LinearLayout container = findViewById(containerId);
                        container.removeAllViews(); // Clear previous views
                        for (DocumentSnapshot document : querySnapshot) {
                            String productName = document.getString("productName");
                            Object quantityObj = document.get("quantity");
                            String quantity = "0"; // Changed to String for consistent handling

                            // Robust quantity handling
                            if (quantityObj instanceof Long) {
                                quantity = String.valueOf(((Long) quantityObj).intValue());
                            } else if (quantityObj instanceof Integer) {
                                quantity = String.valueOf(quantityObj);
                            } else if (quantityObj instanceof String) {
                                quantity = (String) quantityObj;
                            }

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

    private void addRequestToLayout(LinearLayout container, String productName, String quantity, String requestId, String username, String status) {
        View requestView = LayoutInflater.from(this).inflate(R.layout.request_item, container, false);

        TextView requestDetailsTV = requestView.findViewById(R.id.requestDetailsTV);
        Spinner statusSpinner = requestView.findViewById(R.id.statusSpinner);
        Button updateStatusButton = requestView.findViewById(R.id.updateStatusButton);

        requestDetailsTV.setText(productName + " - Quantity: " + quantity);

        // Initialize Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.request_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        // Set the current status in the Spinner
        int spinnerPosition = adapter.getPosition(status); // Get the position of the current status
        statusSpinner.setSelection(spinnerPosition); // Set the selected item

        // Navigate to RequestDetails.java with request details when clicked
        requestView.setOnClickListener(v -> navigateToRequestDetails(requestId));

        updateStatusButton.setOnClickListener(v -> {
            String newStatus = statusSpinner.getSelectedItem().toString();
            updateRequestStatus(requestId, newStatus, productName, quantity); // Pass productName and quantity for inventory update
        });

        container.addView(requestView);
    }

    private void updateRequestStatus(String requestId, String newStatus, String productName, String quantity) {
        db.collection("requests").document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (newStatus.equals("Processing")) {
                        updateInventory(productName, quantity); // Pass quantity for the update
                    }
                    Toast.makeText(Request.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                    loadAllRequests();  // Reload requests without recreating the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Request.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                    Log.e("Request", "Error updating request status: ", e);
                });
    }

    private void updateInventory(String productNameFromRequest, String requestQuantity) {
        Log.d("UpdateInventory", "Request quantity to deduct: " + requestQuantity);

        db.collection("users").document("saikrishna11.bathula@gmail.com")
                .collection("inventory").document("T1234").collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean productFound = false;

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String productNameInInventory = documentSnapshot.getString("productName");

                        Log.d("UpdateInventory", "Checking product: " + productNameInInventory);

                        if (productNameInInventory != null && productNameInInventory.equals(productNameFromRequest)) {
                            productFound = true;

                            // Get the current quantity from the inventory
                            String quantityStr = documentSnapshot.getString("quantity"); // e.g., "100(-50)"
                            String[] parts = quantityStr.split("\\(");
                            int currentQuantity = Integer.parseInt(parts[0].trim()); // Extract the total quantity

                            Log.d("UpdateInventory", "Current quantity in inventory: " + currentQuantity);

                            int requestQ = Integer.parseInt(requestQuantity);
                            // Calculate new quantity
                            int newQuantity = currentQuantity - requestQ; // Deduct based on request quantity
                            Log.d("UpdateInventory", "New quantity after deduction: " + newQuantity);

                            // Prepare updated quantity string in the format "newQuantity(-deductedQuantity)"
                            String updatedQuantityStr = String.valueOf(newQuantity); // Set deducted quantity to 0
                            Log.d("UpdateInventory", "Updating inventory with: " + updatedQuantityStr);

                            documentSnapshot.getReference().update("quantity", updatedQuantityStr)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Request.this, "Inventory updated successfully", Toast.LENGTH_SHORT).show();
                                        Log.d("UpdateInventory", "Inventory updated successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Request.this, "Failed to update inventory", Toast.LENGTH_SHORT).show();
                                        Log.e("UpdateInventory", "Error updating inventory: ", e);
                                    });
                            break; // Exit loop once the product is found and updated
                        }
                    }

                    if (!productFound) {
                        Log.e("UpdateInventory", "Product not found in inventory for the name: " + productNameFromRequest);
                        Toast.makeText(Request.this, "Product not found in inventory", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Request.this, "Failed to fetch inventory", Toast.LENGTH_SHORT).show();
                    Log.e("UpdateInventory", "Error fetching inventory: ", e);
                });
    }

    private void navigateToRequestDetails(String requestId) {
        Intent intent = new Intent(Request.this, RequestDetails.class);
        intent.putExtra("requestId", requestId);
        startActivity(intent);
    }
}
