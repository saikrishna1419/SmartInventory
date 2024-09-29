package com.example.smartinventory;

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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RequestDetails extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout requestContainer;
    private Spinner statusSpinner;
    private Button updateStatusButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_item);

        db = FirebaseFirestore.getInstance();
        requestContainer = findViewById(R.id.requestContainer);

        // Get the requestId from the Intent
        String requestId = getIntent().getStringExtra("requestId");

        if (requestId != null) {
            loadRequestDetails(requestId);
        } else {
            Toast.makeText(this, "No request details available", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRequestDetails(String requestId) {
        db.collection("requests").document(requestId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Extract request details
                        String productName = documentSnapshot.getString("productName");
                        String quantityStr = documentSnapshot.getString("quantity");
                        String username = documentSnapshot.getString("username");
                        String address = documentSnapshot.getString("address");
                        String country = documentSnapshot.getString("country");
                        String labelNumber = documentSnapshot.getString("labelNumber");
                        String pincode = documentSnapshot.getString("pincode");
                        String state = documentSnapshot.getString("state");
                        String upc = documentSnapshot.getString("upc");
                        String currentStatus = documentSnapshot.getString("status");

                        // Inflate the request_item.xml layout and populate the details
                        inflateRequestDetailsLayout(productName, quantityStr, username, address, country,
                                labelNumber, pincode, state, upc, currentStatus, requestId);
                    } else {
                        Log.e("RequestDetails", "No request found for ID: " + requestId);
                    }
                })
                .addOnFailureListener(e -> Log.e("RequestDetails", "Error loading request details: ", e));
    }

    private void inflateRequestDetailsLayout(String productName, String quantityStr, String username, String address,
                                             String country, String labelNumber, String pincode, String state,
                                             String upc, String currentStatus, String requestId) {
        // Inflate the request_item.xml layout
        View requestView = LayoutInflater.from(this).inflate(R.layout.request_item, requestContainer, false);

        // Find and set the request details TextView
        TextView requestDetailsTV = requestView.findViewById(R.id.requestDetailsTV);
        requestDetailsTV.setText("Label Number: " + labelNumber +
                "\nProduct: " + productName + "\nUPC: " + upc +
                "\nQuantity: " + quantityStr + "\nUsername: " + username +
                "\nAddress: " + address + "\nState: " + state +
                "\nCountry: " + country + "\nPincode: " + pincode);

        // Find the Spinner and Button from request_item.xml
        statusSpinner = requestView.findViewById(R.id.statusSpinner);
        updateStatusButton = requestView.findViewById(R.id.updateStatusButton);

        // Set up the Spinner with status options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.request_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        // Set the current status in the Spinner
        setStatusInSpinner(currentStatus);

        // Handle the update status button click
        updateStatusButton.setOnClickListener(v -> {
            String selectedStatus = statusSpinner.getSelectedItem().toString();
            updateRequestStatus(requestId, selectedStatus, quantityStr);
        });

        // Add the view to the request container
        requestContainer.addView(requestView);
    }

    // Helper method to preselect the current status in the Spinner
    private void setStatusInSpinner(String currentStatus) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) statusSpinner.getAdapter();
        if (currentStatus != null) {
            int spinnerPosition = adapter.getPosition(currentStatus);
            statusSpinner.setSelection(spinnerPosition);
        }
    }

    private void updateRequestStatus(String requestId, String newStatus, String quantityStr) {
        db.collection("requests").document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (newStatus.equals("Processing")) {
                        // Get the product name for comparison
                        db.collection("requests").document(requestId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String productName = documentSnapshot.getString("productName");

                                        // Pass both productName and quantityStr to updateInventory
                                        updateInventory(productName, quantityStr);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(RequestDetails.this, "Failed to fetch product name", Toast.LENGTH_SHORT).show();
                                });
                    }
                    Toast.makeText(RequestDetails.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RequestDetails.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                    Log.e("RequestDetails", "Error updating request status: ", e);
                });
    }

    private void updateInventory(String productNameFromRequest, String quantityStr) {
        db.collection("users").document("saikrishna11.bathula@gmail.com")
                .collection("inventory").document("T1234").collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean productFound = false;

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String productNameInInventory = documentSnapshot.getString("productName");

                        if (productNameInInventory != null && productNameInInventory.equals(productNameFromRequest)) {
                            productFound = true;

                            // Get the current quantity from the inventory
                            String quantityStrInInventory = documentSnapshot.getString("quantity"); // e.g., "30(-9)"
                            String[] parts = quantityStrInInventory.split("\\(");
                            int currentQuantity = Integer.parseInt(parts[0]); // Get the original quantity

                            // Calculate the new quantity using the quantity from updateRequestStatus
                            int requestQuantity = Integer.parseInt(quantityStr); // Quantity from the request
                            int newQuantity = currentQuantity - requestQuantity; // Subtract request quantity

                            // Construct the new quantity string
                            String newQuantityStr = newQuantity + ""; // Assuming you want to set the last entered quantity to 0

                            // Update the quantity for the matched product
                            documentSnapshot.getReference().update("quantity", newQuantityStr)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RequestDetails.this, "Inventory updated successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RequestDetails.this, "Failed to update inventory", Toast.LENGTH_SHORT).show();
                                        Log.e("RequestDetails", "Error updating inventory: ", e);
                                    });
                            break; // Exit loop once the product is found and updated
                        }
                    }

                    if (!productFound) {
                        Log.e("RequestDetails", "Product not found in inventory for the name: " + productNameFromRequest);
                        Toast.makeText(RequestDetails.this, "Product not found in inventory", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RequestDetails.this, "Failed to fetch inventory", Toast.LENGTH_SHORT).show();
                    Log.e("RequestDetails", "Error fetching inventory: ", e);
                });
    }

}
