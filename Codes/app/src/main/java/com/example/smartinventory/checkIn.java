package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class checkIn extends AppCompatActivity {

    private LinearLayout productQuantityLayout;
    private Button addMoreBTN;
    private Button checkinBTN;
    private EditText dateET;
    private EditText trackidET;
    private EditText usernameET;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        productQuantityLayout = findViewById(R.id.productQuantityLayout);
        addMoreBTN = findViewById(R.id.addMoreBTN);
        checkinBTN = findViewById(R.id.checkinBTN);
        dateET = findViewById(R.id.dateET);
        trackidET = findViewById(R.id.trackidET);
        usernameET = findViewById(R.id.usernameET);

        db = FirebaseFirestore.getInstance();

        // Set the current date
        String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
        dateET.setText(currentDate);

        // Add initial row
        addNewRow();

        // Add more button click listener
        addMoreBTN.setOnClickListener(v -> addNewRow());

        // Check-in button click listener
        checkinBTN.setOnClickListener(v -> checkInData());
    }

    private void addNewRow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View rowView = inflater.inflate(R.layout.product_quantity_row, productQuantityLayout, false);
        productQuantityLayout.addView(rowView);
    }

    private void checkInData() {
        String trackingId = trackidET.getText().toString().trim();
        String username = usernameET.getText().toString().trim();
        String date = dateET.getText().toString().trim();

        if (TextUtils.isEmpty(trackingId) || TextUtils.isEmpty(username) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the batch operation
        WriteBatch batch = db.batch();

        // Create an empty document for the user in the users collection
        DocumentReference userRef = db.collection("users").document(username);
        batch.set(userRef, new HashMap<>()); // Create an empty document using an empty map

        // Create an empty document for the tracking ID in the inventory subcollection
        DocumentReference trackingRef = userRef.collection("inventory").document(trackingId);
        batch.set(trackingRef, new HashMap<>()); // Create an empty document using an empty map

        // Collect all product details
        for (int i = 0; i < productQuantityLayout.getChildCount(); i++) {
            View rowView = productQuantityLayout.getChildAt(i);

            EditText upcET = rowView.findViewById(R.id.upcET);
            EditText productNameET = rowView.findViewById(R.id.productNameET);
            EditText quantityET = rowView.findViewById(R.id.quantityET);

            String upc = upcET.getText().toString().trim();
            String productName = productNameET.getText().toString().trim();
            String quantityStr = quantityET.getText().toString().trim();

            if (TextUtils.isEmpty(upc) || TextUtils.isEmpty(productName) || TextUtils.isEmpty(quantityStr)) {
                continue; // Skip empty rows
            }

            // Create InventoryItem object with quantity as String
            InventoryItem item = new InventoryItem(trackingId, upc, productName, quantityStr, date, null);

            // Create a reference to the document in the `items` subcollection under the `trackingID` document
            DocumentReference itemRef = trackingRef.collection("items").document(); // Auto-generate document ID for each product

            // Set data to the document
            batch.set(itemRef, item);
        }

        // Commit the batch operation
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(checkIn.this, "Check-in successful", Toast.LENGTH_SHORT).show();
                    chargeCheckInPayment(username, trackingId);
                    fetchAndLogData(username, trackingId); // Fetch and log data after successful check-in

                    fetchAndLogData(username, trackingId);
                    // Start WarehouseInventoryActivity to refresh data
                    //Intent intent = new Intent(checkIn.this, WarehouseInventoryActivity.class);
                    //intent.putExtra("refresh", true); // Flag to indicate data refresh
                    //startActivity(intent);
                    //finish(); // Optional: finish the current activity
                })
                .addOnFailureListener(e -> Toast.makeText(checkIn.this, "Error saving data", Toast.LENGTH_SHORT).show());
    }


    private void chargeCheckInPayment(String username, String trackingId) {
        double checkInPaymentAmount = 15.0; // Flat rate for check-in
        String checkInDate = getCurrentDate(); // Get current date for payment
        int numberOfDays = calculateNumberOfDays(checkInDate); // Calculate number of days

        // Reference to the check-in payment document
        DocumentReference paymentRef = db.collection("users").document(username)
                .collection("payments").document("checkInPayment");

        // Fetch existing payment data
        paymentRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    double currentPaymentAmount = 0.0;

                    if (documentSnapshot.exists()) {
                        // Get the existing payment amount
                        currentPaymentAmount = documentSnapshot.getDouble("amount") != null ? documentSnapshot.getDouble("amount") : 0.0;
                        currentPaymentAmount += checkInPaymentAmount; // Add $15 to the existing amount

                        // Update the document with the new amount
                        paymentRef.update("amount", currentPaymentAmount)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Charged $" + checkInPaymentAmount + " for check-in", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error updating check-in payment", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Create new payment document
                        Payment payment = new Payment(username, trackingId, checkInPaymentAmount, checkInDate, numberOfDays);
                        paymentRef.set(payment)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Charged $" + checkInPaymentAmount + " for check-in", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error charging payment", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching payment document", Toast.LENGTH_SHORT).show();
                });

        // Now also handle the storage payment calculation
        StoragePaymentCalculator paymentCalculator = new StoragePaymentCalculator(username);
        paymentCalculator.updateStoragePayment(checkInDate); // Pass the check-in date for storage payment calculation
    }


    // Helper method to calculate number of days from check-in date to current date
    private int calculateNumberOfDays(String checkInDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            Date checkIn = sdf.parse(checkInDate);
            Date currentDate = new Date();

            if (checkIn != null) {
                long differenceInMillis = currentDate.getTime() - checkIn.getTime();
                return (int) (differenceInMillis / (1000 * 60 * 60 * 24)); // Convert to days
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if there's an error
    }

    // Helper method to get current date
    private String getCurrentDate() {
        return new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
    }

    private void fetchTrackingIdData(String username, String trackingId) {
        Log.d("FirestoreData", "Fetching data for username: " + username + ", trackingId: " + trackingId);

        DocumentReference trackingDocRef = db.collection("users")
                .document(username)
                .collection("inventory")
                .document(trackingId);

        trackingDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Handle the document data
                        Log.d("FirestoreData", "Fetched tracking ID document successfully.");
                    } else {
                        Log.e("FirestoreData", "Tracking ID document does not exist!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreData", "Error fetching tracking ID document", e);
                });
    }

    private void fetchAndLogData(String username, String trackingId) {
        Log.d("FirestoreData", "Fetching data for username: " + username + ", trackingId: " + trackingId);

        // Reference to the tracking ID document
        DocumentReference trackingDocRef = db.collection("users").document(username)
                .collection("inventory").document(trackingId);

        // Fetch the tracking ID document
        trackingDocRef.get()
                .addOnSuccessListener(trackingDoc -> {
                    if (trackingDoc.exists()) {
                        Log.d("FirestoreData", "Tracking ID: " + trackingDoc.getId());
                        // Log other tracking document fields here if necessary
                        // Fetch the items subcollection
                        trackingDocRef.collection("items").get()
                                .addOnSuccessListener(itemSnapshots -> {
                                    for (DocumentSnapshot itemSnapshot : itemSnapshots) {
                                        InventoryItem item = itemSnapshot.toObject(InventoryItem.class);
                                        if (item != null) {
                                            Log.d("FirestoreData", "Item UPC: " + item.getUpc() +
                                                    ", Product Name: " + item.getProductName() +
                                                    ", Quantity: " + item.getQuantity() +
                                                    ", Date: " + item.getDate() +
                                                    ", Note: " + item.getNote());
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("FirestoreData", "Error fetching items", e));
                    } else {
                        Log.e("FirestoreData", "No such tracking ID document!");
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreData", "Error fetching tracking ID document", e));
    }
}
