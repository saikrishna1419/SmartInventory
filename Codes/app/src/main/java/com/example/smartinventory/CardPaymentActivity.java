package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class CardPaymentActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String username; // Field to store username
    private double totalAmount; // To hold the total amount

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch logged-in username
        getLoggedInUsername();

        // Get the total amount passed from PaymentPageActivity
        totalAmount = getIntent().getDoubleExtra("paymentAmount", 0.0);

        EditText cardNumberET = findViewById(R.id.cardNumberET);
        EditText expiryDateET = findViewById(R.id.expiryDateET);
        EditText cvvET = findViewById(R.id.cvvET);

        // Add validation to card number, expiry date, and CVV fields
        setupCardNumberValidation(cardNumberET);
        setupExpiryDateValidation(expiryDateET);
        setupCVVValidation(cvvET);

        // Payment button click listener
        findViewById(R.id.submitPaymentBtn).setOnClickListener(v -> processPayment());
    }

    private void setupCardNumberValidation(EditText cardNumberET) {
        cardNumberET.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFormatting) return;
                isFormatting = true;

                // Limit input to 12 digits
                String rawText = s.toString().replace("-", "").replace(" ", "");
                if (rawText.length() > 16) {
                    rawText = rawText.substring(0, 16); // Limit to 12 digits
                }

                // Format input as 4-4-4
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < rawText.length(); i++) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ");
                    formatted.append(rawText.charAt(i));
                }

                cardNumberET.setText(formatted.toString());
                cardNumberET.setSelection(formatted.length());
                isFormatting = false;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupExpiryDateValidation(EditText expiryDateET) {
        expiryDateET.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFormatting) return;
                isFormatting = true;

                StringBuilder formatted = new StringBuilder(s.toString().replace("/", ""));
                if (formatted.length() > 2) {
                    formatted.insert(2, "/");
                }

                expiryDateET.setText(formatted.toString());
                expiryDateET.setSelection(formatted.length());
                isFormatting = false;

                // Validate expiry date
                if (formatted.length() == 5) {
                    int month = Integer.parseInt(formatted.substring(0, 2));
                    int year = Integer.parseInt("20" + formatted.substring(3));
                    Calendar now = Calendar.getInstance();
                    int currentMonth = now.get(Calendar.MONTH) + 1;
                    int currentYear = now.get(Calendar.YEAR);

                    if (month < 1 || month > 12 || year < currentYear || (year == currentYear && month < currentMonth)) {
                        expiryDateET.setError("Invalid expiry date");
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCVVValidation(EditText cvvET) {
        cvvET.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFormatting) return;
                isFormatting = true;

                // Limit input to 3 digits
                if (s.length() > 3) {
                    s = s.subSequence(0, 3);
                }

                cvvET.setText(s);
                cvvET.setSelection(s.length());
                isFormatting = false;

                // Validate CVV length
                if (s.length() > 0 && !s.toString().matches("\\d{1,3}")) {
                    cvvET.setError("CVV must be 3 digits");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();

        String last4Digits = cardNumber.substring(cardNumber.length() - 4);
        String currentDateTime = getCurrentDateTime();
        String trackingId = generateRandomTrackingId();
        int numberOfDays = 0;

        Payment payment = new Payment(username, trackingId, last4Digits, totalAmount, currentDateTime, numberOfDays);
        savePaymentToFirestore(payment);
    }

    private void savePaymentToFirestore(Payment payment) {
        db.collection("users").document(payment.getUsername())
                .collection("payment_history")
                .add(payment)
                .addOnSuccessListener(documentReference -> {
                    Log.d("CardPaymentActivity", "Payment recorded successfully in payment_history!");
                    Toast.makeText(this, "Payment recorded successfully!", Toast.LENGTH_SHORT).show();

                    clearUserPayments(payment.getUsername());
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("CardPaymentActivity", "Error recording payment", e);
                    Toast.makeText(this, "Error recording payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearUserPayments(String username) {
        db.collection("users").document(username)
                .collection("payments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                        Log.d("CardPaymentActivity", "User payments cleared successfully.");
                    } else {
                        Log.e("CardPaymentActivity", "Error clearing payments", task.getException());
                    }
                });
    }

    private String generateRandomTrackingId() {
        Random random = new Random();
        return "TRK" + random.nextInt(100000);
    }

    private void getLoggedInUsername() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = currentUser.getEmail();

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

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
