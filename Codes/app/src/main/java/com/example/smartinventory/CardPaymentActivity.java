package com.example.smartinventory;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

public class CardPaymentActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String username; // Field to store username
    private double totalAmount = 0; // To hold the total amount

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch logged-in username
        getLoggedInUsername();

        // Payment button click listener
        findViewById(R.id.submitPaymentBtn).setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        EditText cardNumberET = findViewById(R.id.cardNumberET);
        EditText expiryDateET = findViewById(R.id.expiryDateET);
        EditText cvvET = findViewById(R.id.cvvET);
        EditText cardHolderNameET = findViewById(R.id.cardHolderNameET);

        String cardNumber = cardNumberET.getText().toString().trim();
        String expiryDate = expiryDateET.getText().toString().trim();
        String cvv = cvvET.getText().toString().trim();
        String cardHolderName = cardHolderNameET.getText().toString().trim();

        if (TextUtils.isEmpty(cardNumber) || TextUtils.isEmpty(expiryDate) || TextUtils.isEmpty(cvv) || TextUtils.isEmpty(cardHolderName)) {
            Toast.makeText(this, "Please fill in all card details", Toast.LENGTH_SHORT).show();
            return;
        }

        // Process the payment here (Integrate with payment gateway)
        // For this example, we'll assume the payment is successful
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();

        // Update the total payment amount in Firestore to 0
        updateTotalPaymentAmountToZero();
    }

    private void updateTotalPaymentAmountToZero() {
        if (username == null) {
            Toast.makeText(this, "Username is null. Cannot update payment amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch all payment documents for the user to calculate total amount
        db.collection("users").document(username)
                .collection("payments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        totalAmount = 0; // Reset total amount
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Double amount = document.getDouble("amount");
                            if (amount != null) {
                                totalAmount += amount;
                            }
                        }
                        Log.d("CardPaymentActivity", "Total amount before update: " + totalAmount);

                        // Update all payment documents to zero
                        updatePaymentAmountsToZero();
                    } else {
                        Log.e("CardPaymentActivity", "Error fetching payments", task.getException());
                        Toast.makeText(this, "Error fetching payment records", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePaymentAmountsToZero() {
        db.collection("users").document(username)
                .collection("payments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().update("amount", 0)
                                    .addOnSuccessListener(aVoid -> Log.d("CardPaymentActivity", "Payment amount updated to 0 successfully"))
                                    .addOnFailureListener(e -> Log.e("CardPaymentActivity", "Error updating payment amount", e));
                        }
                        // Finish activity after updates
                        finish();
                    } else {
                        Log.e("CardPaymentActivity", "Error fetching payments for update", task.getException());
                        Toast.makeText(this, "Error updating payment amounts", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getLoggedInUsername() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = currentUser.getEmail();

        // Fetch username from Firestore
        db.collection("customers")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                        username = userTask.getResult().getDocuments().get(0).getString("userName");
                        Log.d("CardPaymentActivity", "Fetched username: " + username);
                    } else {
                        Toast.makeText(this, "Error fetching username", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
