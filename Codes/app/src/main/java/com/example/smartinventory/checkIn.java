package com.example.smartinventory;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Date;
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

            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create InventoryItem object
            InventoryItem item = new InventoryItem(trackingId, upc, productName, quantity, date, null);

            // Create a reference to the document in the `items` subcollection under the `trackingID` document
            DocumentReference itemRef = db.collection("users").document(username)
                    .collection("inventory").document(trackingId)
                    .collection("items").document(); // Auto-generate document ID for each product

            // Set data to the document
            batch.set(itemRef, item);
        }

        // Commit the batch operation
        batch.commit()
                .addOnSuccessListener(aVoid -> Toast.makeText(checkIn.this, "Check-in successful", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(checkIn.this, "Error saving data", Toast.LENGTH_SHORT).show());
    }

}
