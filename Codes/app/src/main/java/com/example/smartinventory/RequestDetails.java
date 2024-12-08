package com.example.smartinventory;

import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RequestDetails extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout requestContainer;
    private Spinner statusSpinner;
    private Button updateStatusButton, downloadPdfButton;

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
                        String pdfUrl = documentSnapshot.getString("pdfUrl");

                        // Inflate the request_item.xml layout and populate the details
                        inflateRequestDetailsLayout(productName, quantityStr, username, address, country,
                                labelNumber, pincode, state, upc, currentStatus, requestId, pdfUrl);
                    } else {
                        Log.e("RequestDetails", "No request found for ID: " + requestId);
                    }
                })
                .addOnFailureListener(e -> Log.e("RequestDetails", "Error loading request details: ", e));
    }

    private void inflateRequestDetailsLayout(String productName, String quantityStr, String username, String address,
                                             String country, String labelNumber, String pincode, String state,
                                             String upc, String currentStatus, String requestId, String pdfUrl) {
        // Inflate the request_item.xml layout
        View requestView = LayoutInflater.from(this).inflate(R.layout.request_item, requestContainer, false);

        // Find and set the request details TextView
        TextView requestDetailsTV = requestView.findViewById(R.id.requestDetailsTV);
        requestDetailsTV.setText("Label Number: " + labelNumber +
                "\nProduct: " + productName + "\nUPC: " + upc +
                "\nQuantity: " + quantityStr + "\nUsername: " + username +
                "\nAddress: " + address + "\nState: " + state +
                "\nCountry: " + country + "\nPincode: " + pincode);

        // Find the Spinner and Buttons from request_item.xml
        statusSpinner = requestView.findViewById(R.id.statusSpinner);
        updateStatusButton = requestView.findViewById(R.id.updateStatusButton);
        downloadPdfButton = requestView.findViewById(R.id.downloadPdfButton);

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
            updateRequestStatus(requestId, selectedStatus, productName, quantityStr, username); // Move inventory deduction here
        });

        // Handle the download PDF button click
        downloadPdfButton.setOnClickListener(v -> {
            if (pdfUrl != null && !pdfUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "PDF URL is not available", Toast.LENGTH_SHORT).show();
            }
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

                    Toast.makeText(RequestDetails.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RequestDetails.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                    Log.e("RequestDetails", "Error updating request status: ", e);
                });
    }

    // Method to add shipping cost to the user's payment details
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


        private void updateInventory (String username, String productNameFromRequest, String
        requestQuantity){
            Log.d("UpdateInventory", "Request quantity to deduct: " + requestQuantity);
            Log.d("UpdateInventory", "username: " + username);

            db.collection("users").document(username).collection("inventory")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            Log.e("UpdateInventory", "No inventory found for user: " + username);
                            Toast.makeText(this, "No inventory found for the user", Toast.LENGTH_SHORT).show();
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
                                                                Toast.makeText(this, "Inventory updated successfully", Toast.LENGTH_SHORT).show();
                                                                Log.d("UpdateInventory", "Inventory updated successfully for tracking ID: " + trackingId);
                                                                // Log the deduction
                                                                logDeduction(productNameFromRequest, requestQ);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(this, "Failed to update inventory", Toast.LENGTH_SHORT).show();
                                                                Log.e("UpdateInventory", "Error updating inventory for tracking ID: " + trackingId, e);
                                                            });

                                                    break; // Exit the loop once the product is found and updated
                                                }
                                            }

                                            if (!productFound.get()) {
                                                Log.e("UpdateInventory", "Product not found in inventory for the name: " + productNameFromRequest);
                                                Toast.makeText(this, "Product not found in inventory", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("UpdateInventory", "Error fetching items for tracking ID: " + trackingId, e);
                                            Toast.makeText(this, "Failed to fetch items for tracking ID: " + trackingId, Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UpdateInventory", "Error fetching inventory for user: " + username, e);
                        Toast.makeText(this, "Failed to fetch inventory", Toast.LENGTH_SHORT).show();
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
}
