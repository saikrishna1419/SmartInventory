package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class PaymentPageActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView totalPaymentAmountTextView;
    private double totalAmount = 0;  // Store the total amount to be paid

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_page);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize TextView for displaying total payment amount
        totalPaymentAmountTextView = findViewById(R.id.paymentAmountTextView);

        // Fetch logged-in username and then payment data
        getLoggedInUsername(new UsernameCallback() {
            @Override
            public void onUsernameReceived(String username) {
                // Fetch and calculate the total payment after getting the username
                fetchAndCalculateTotalPayment(username);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PaymentPageActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Add Pay Now button click listener
        Button payNowButton = findViewById(R.id.payButton);
        payNowButton.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentPageActivity.this, CardPaymentActivity.class);
            intent.putExtra("TOTAL_AMOUNT", totalAmount); // Pass the total amount to CardPaymentActivity
            startActivity(intent);
        });
    }

    // Fetch all payment documents for the user and calculate the total amount
    private void fetchAndCalculateTotalPayment(String username) {
        db.collection("users").document(username)
                .collection("payments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        totalAmount = 0;  // Reset total amount before calculation

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Double amount = document.getDouble("amount");
                            if (amount != null) {
                                totalAmount += amount;  // Sum up all payment amounts
                            }
                        }

                        // Display the total amount to the user
                        totalPaymentAmountTextView.setText("Total Amount: $" + totalAmount);
                    } else {
                        Log.e("PaymentPageActivity", "Error fetching payments", task.getException());
                        Toast.makeText(this, "Error fetching payment data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getLoggedInUsername(UsernameCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not logged in");
            return;
        }

        String email = currentUser.getEmail();

        // Fetch username from Firestore
        db.collection("customers")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                        String username = userTask.getResult().getDocuments().get(0).getString("userName");
                        callback.onUsernameReceived(username);
                    } else {
                        callback.onError("Error fetching username");
                    }
                });
    }

    public interface UsernameCallback {
        void onUsernameReceived(String username);
        void onError(String error);
    }
}
