package com.example.smartinventory;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class RequestDetailsActivity extends AppCompatActivity {

    private EditText quantityET;
    private EditText labelNumberET;
    private EditText addressET;
    private EditText usernameET;
    private EditText pincodeET;
    private EditText stateET;
    private EditText countryET;
    private Button sendButton;

    private FirebaseFirestore db;
    private String productName;
    private String upc;
    private int quantity;
    private String username;
    private String trackingId = "T1234"; // Hardcoded tracking ID for now
    private static final String TAG = "RequestDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Get logged-in user's email as username (you can modify to use username if stored separately)
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            username = currentUser.getEmail(); // Or getDisplayName() if you store the username separately
        } else {
            Log.e(TAG, "No logged-in user.");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return; // Prevent further execution if no user is logged in
        }

        // Initialize views
        quantityET = findViewById(R.id.quantityET);
        labelNumberET = findViewById(R.id.labelNumberET);
        addressET = findViewById(R.id.addressET);
        usernameET = findViewById(R.id.usernameET);
        pincodeET = findViewById(R.id.pincodeET);
        stateET = findViewById(R.id.stateET);
        countryET = findViewById(R.id.countryET);
        sendButton = findViewById(R.id.sendButton);

        // Get product details from Intent
        productName = getIntent().getStringExtra("productName");
        upc = getIntent().getStringExtra("upc");
        quantity = getIntent().getIntExtra("quantity", 0);

        // Set product details to the UI
        TextView productNameTV = findViewById(R.id.productNameTV);
        TextView upcTV = findViewById(R.id.upcTV);

        productNameTV.setText(productName);
        upcTV.setText(upc);
        quantityET.setText(String.valueOf(quantity));

        // Handle send button click
        sendButton.setOnClickListener(v -> {
            String labelNumber = labelNumberET.getText().toString();
            String address = addressET.getText().toString();
            String pincode = pincodeET.getText().toString();
            String state = stateET.getText().toString();
            String country = countryET.getText().toString();


            // Call the method to fetch the product and update quantity
            fetchProductAndUpdateQuantity(labelNumber, address, username, pincode, state, country);
        });
    }

    // Method to fetch product and update its quantity in Firestore
    private void fetchProductAndUpdateQuantity(String labelNumber, String address, String username, String pincode, String state, String country) {
        if (username == null || trackingId == null) {
            Log.e(TAG, "Username or tracking ID is null");
            Toast.makeText(this, "Error: Username or tracking ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(username)
                .collection("inventory")
                .document(trackingId)
                .collection("items")
                .whereEqualTo("productName", productName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // Update quantity logic
                            querySnapshot.getDocuments().get(0).getReference().update("quantity", quantity - Integer.parseInt(quantityET.getText().toString()))
                                    .addOnSuccessListener(aVoid -> {
                                        // Save Request Details
                                        saveRequestDetailsToFirestore(labelNumber, address, username, pincode, state, country);
                                        Toast.makeText(RequestDetailsActivity.this, "Quantity updated and request saved", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error updating quantity: ", e));
                        } else {
                            Log.d(TAG, "No product found with the given name.");
                            Toast.makeText(RequestDetailsActivity.this, "Product not found in Firestore.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    // Method to save request details to Firestore
    private void saveRequestDetailsToFirestore(String labelNumber, String address, String username, String pincode, String state, String country) {
        String status = "New";  // Default status
        RequestDetails requestDetails = new RequestDetails(productName, upc, Integer.parseInt(quantityET.getText().toString()), labelNumber, address, username, pincode, state, country, status);
        db.collection("requests")
                .add(requestDetails)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Request details saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving request details: ", e));
    }
}
