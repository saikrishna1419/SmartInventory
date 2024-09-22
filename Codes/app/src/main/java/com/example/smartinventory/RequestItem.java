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

import com.google.firebase.firestore.FirebaseFirestore;

public class RequestItem extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout requestContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_item);

        db = FirebaseFirestore.getInstance();
        requestContainer = findViewById(R.id.requestContainer);

        String requestId = getIntent().getStringExtra("requestId");

        if (requestId != null) {
            loadRequestDetails(requestId);
        } else {
            Toast.makeText(RequestItem.this, "Request ID not provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRequestDetails(String requestId) {
        Log.d("RequestItem", "Fetching request details for requestId: " + requestId);

        db.collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String productName = documentSnapshot.getString("productName");
                        int quantity = documentSnapshot.getLong("quantity").intValue();
                        String username = documentSnapshot.getString("username");
                        String address = documentSnapshot.getString("address");
                        String state = documentSnapshot.getString("state");
                        String country = documentSnapshot.getString("country");
                        String pincode = documentSnapshot.getString("pincode");
                        String status = documentSnapshot.getString("status");

                        // Add request details to layout
                        addRequestToLayout(productName, quantity, requestId, username, address, state, country, pincode, status);
                    } else {
                        Toast.makeText(RequestItem.this, "Request not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("RequestItem", "Error fetching request details: ", e));
    }

    private void addRequestToLayout(String productName, int quantity, String requestId, String username, String address, String state, String country, String pincode, String status) {
        View requestView = LayoutInflater.from(this).inflate(R.layout.request_item, requestContainer, false);

        TextView requestDetailsTV = requestView.findViewById(R.id.requestDetailsTV);
        Spinner statusSpinner = requestView.findViewById(R.id.statusSpinner);
        Button updateStatusButton = requestView.findViewById(R.id.updateStatusButton);

        // Display all details including the newly added fields
        requestDetailsTV.setText("Product: " + productName + "\nQuantity: " + quantity +
                "\nUsername: " + username + "\nAddress: " + address +
                "\nState: " + state + "\nCountry: " + country + "\nPincode: " + pincode);

        // Set up status spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.request_status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);
        statusSpinner.setSelection(adapter.getPosition(status));

        updateStatusButton.setOnClickListener(v -> {
            String newStatus = statusSpinner.getSelectedItem().toString();
            updateRequestStatus(requestId, newStatus);
        });

        requestContainer.addView(requestView);
    }

    private void updateRequestStatus(String requestId, String newStatus) {
        db.collection("requests").document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if ("Cancelled".equals(newStatus)) {
                        db.collection("requests").document(requestId)
                                .delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(RequestItem.this, "Request cancelled and removed.", Toast.LENGTH_SHORT).show();
                                    finish(); // Go back to Request activity
                                });
                    } else {
                        Toast.makeText(RequestItem.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to Request activity
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(RequestItem.this, "Failed to update status", Toast.LENGTH_SHORT).show());
    }

}
