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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
            updateRequestStatus(requestId, newStatus, productName, quantity, username); // Pass productName and quantity for inventory update
        });

        container.addView(requestView);
    }

    private void updateRequestStatus(String requestId, String newStatus, String productName, String quantityStr, String username) {
        // Create a map for the status update
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("status", newStatus);
        statusUpdate.put("timestamp", FieldValue.serverTimestamp()); // Use server timestamp for accuracy

        // Update the current status and timestamp
        db.collection("requests").document(requestId)
                .update("status", newStatus, "timestamp", FieldValue.serverTimestamp())
                .addOnSuccessListener(aVoid -> {
                    // Add the new status to history
                    addStatusToHistory(requestId, newStatus);

                    // If the status is "Processing", update the inventory
                    if (newStatus.equals("Processing")) {
                        updateInventory(username, productName, quantityStr); // Pass username from the request
                    }

                    // If the status is changed to "Shipping", add the shipping cost
                    if (newStatus.equals("Shipped")) {
                        addShippingCostToUser(requestId, "$15"); // Add shipping cost
                    }

                    Toast.makeText(Request.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Request.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                    Log.e("RequestDetails", "Error updating request status: ", e);
                });
    }

    private void addShippingCostToUser(String requestId, String shippingCost) {
        db.collection("requests").document(requestId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the username from the request document
                        String username = documentSnapshot.getString("username");

                        // Reference to the user's payments document in Firebase
                        db.collection("users").document(username).collection("payments")
                                .document("paymentDetails")
                                .get()
                                .addOnSuccessListener(paymentDoc -> {
                                    if (paymentDoc.exists()) {
                                        // If the document exists, update the shipping cost
                                        db.collection("users").document(username).collection("payments")
                                                .document("checkoutPayment")
                                                .update("amount", FieldValue.increment(15)) // Add $15 to the shipping cost
                                                .addOnSuccessListener(aVoid -> Log.d("RequestDetails", "Shipping cost added successfully."))
                                                .addOnFailureListener(e -> Log.e("RequestDetails", "Error adding shipping cost: ", e));
                                    } else {
                                        // If the document doesn't exist, create it and set the initial shipping cost
                                        Map<String, Object> paymentData = new HashMap<>();
                                        paymentData.put("amount", 15); // Set the initial shipping cost
                                        db.collection("users").document(username).collection("payments")
                                                .document("checkoutPayment")
                                                .set(paymentData)
                                                .addOnSuccessListener(aVoid -> Log.d("RequestDetails", "Payment details created with shipping cost."))
                                                .addOnFailureListener(e -> Log.e("RequestDetails", "Error creating payment details: ", e));
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("RequestDetails", "Error fetching payment details: ", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("RequestDetails", "Error fetching request details: ", e));
    }


    private void addStatusToHistory(String requestId, String newStatus) {
        // First, set the server timestamp in the document
        db.collection("requests").document(requestId)
                .update("timestamp", FieldValue.serverTimestamp())
                .addOnSuccessListener(aVoid -> {
                    // After setting the timestamp, retrieve it
                    db.collection("requests").document(requestId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // Get the updated timestamp
                                    Object timestamp = documentSnapshot.get("timestamp");

                                    // Create a map for the history entry
                                    Map<String, Object> historyEntry = new HashMap<>();
                                    historyEntry.put("status", newStatus);
                                    historyEntry.put("timestamp", timestamp);

                                    // Add the history entry to the statusHistory field
                                    db.collection("requests").document(requestId)
                                            .update("statusHistory", FieldValue.arrayUnion(historyEntry))
                                            .addOnSuccessListener(aVoid1 -> {
                                                Log.d("RequestDetails", "Status history updated successfully.");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("RequestDetails", "Error updating status history: ", e);
                                            });
                                }
                            })
                            .addOnFailureListener(e -> Log.e("RequestDetails", "Error retrieving timestamp: ", e));
                })
                .addOnFailureListener(e -> Log.e("RequestDetails", "Error setting server timestamp: ", e));
    }

    private void updateInventory(String username, String productNameFromRequest, String requestQuantity) {
        Log.d("UpdateInventory", "Request quantity to deduct: " + requestQuantity);
        Log.d("UpdateInventory", "username: " + username);

        db.collection("users").document(username).collection("inventory")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.e("UpdateInventory", "No inventory found for user: " + username);
                        Toast.makeText(Request.this, "No inventory found for the user", Toast.LENGTH_SHORT).show();
                    } else {
                        AtomicBoolean productFound = new AtomicBoolean(false);

                        // Loop through all tracking IDs in the inventory collection
                        for (DocumentSnapshot trackingDoc : querySnapshot) {
                            String trackingId = trackingDoc.getId(); // Get tracking ID
                            Log.d("UpdateInventory", "Checking tracking ID: " + trackingId);

                            // For each tracking ID, get the 'items' subcollection
                            db.collection("users").document(username)
                                    .collection("inventory").document(trackingId).collection("items")
                                    .get()
                                    .addOnSuccessListener(itemSnapshot -> {
                                        for (DocumentSnapshot itemDoc : itemSnapshot) {
                                            String productNameInInventory = itemDoc.getString("productName");

                                            Log.d("UpdateInventory", "Checking product: " + productNameInInventory);

                                            if (productNameInInventory != null && productNameInInventory.equals(productNameFromRequest)) {
                                                productFound.set(true);

                                                // Get the current quantity from the inventory
                                                String quantityStr = itemDoc.getString("quantity");
                                                String[] parts = quantityStr.split("\\(");
                                                int currentQuantity = Integer.parseInt(parts[0].trim()); // Extract the total quantity

                                                Log.d("UpdateInventory", "Current quantity in inventory: " + currentQuantity);

                                                int requestQ = Integer.parseInt(requestQuantity);
                                                int newQuantity = currentQuantity - requestQ; // Deduct based on request quantity

                                                Log.d("UpdateInventory", "New quantity after deduction: " + newQuantity);

                                                // Update the quantity field
                                                String updatedQuantityStr = String.valueOf(newQuantity); // Deduct quantity
                                                itemDoc.getReference().update("quantity", updatedQuantityStr)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(Request.this, "Inventory updated successfully", Toast.LENGTH_SHORT).show();
                                                            Log.d("UpdateInventory", "Inventory updated successfully for tracking ID: " + trackingId);
                                                            // Log the deduction
                                                            logDeduction(productNameFromRequest, requestQ);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(Request.this, "Failed to update inventory", Toast.LENGTH_SHORT).show();
                                                            Log.e("UpdateInventory", "Error updating inventory for tracking ID: " + trackingId, e);
                                                        });

                                                break; // Exit the loop once the product is found and updated
                                            }
                                        }

                                        if (!productFound.get()) {
                                            Log.e("UpdateInventory", "Product not found in inventory for the name: " + productNameFromRequest);
                                            Toast.makeText(Request.this, "Product not found in inventory", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("UpdateInventory", "Error fetching items for tracking ID: " + trackingId, e);
                                        Toast.makeText(Request.this, "Failed to fetch items for tracking ID: " + trackingId, Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateInventory", "Error fetching inventory for user: " + username, e);
                    Toast.makeText(Request.this, "Failed to fetch inventory", Toast.LENGTH_SHORT).show();
                });
    }

    private void logDeduction(String productName, int deductedAmount) {
        Map<String, Object> deductionData = new HashMap<>();
        deductionData.put("productName", productName);
        deductionData.put("deductedAmount", deductedAmount);
        deductionData.put("timestamp", FieldValue.serverTimestamp()); // Use server timestamp for accuracy

        // Add the deduction to the 'deductions' collection
        db.collection("deductions")
                .add(deductionData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("LogDeduction", "Deduction logged with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("LogDeduction", "Error logging deduction: ", e);
                });
    }


    private void navigateToRequestDetails(String requestId) {
        Intent intent = new Intent(Request.this, RequestDetails.class);
        intent.putExtra("requestId", requestId);
        startActivity(intent);
    }
}
