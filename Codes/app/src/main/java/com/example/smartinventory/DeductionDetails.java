package com.example.smartinventory;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DeductionDetails extends AppCompatActivity {
    private FirebaseFirestore db;
    private LinearLayout linearLayout; // Reference to LinearLayout to add TextViews dynamically

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deduction_details);

        db = FirebaseFirestore.getInstance();
        linearLayout = findViewById(R.id.dynamicLayout); // Reference to the LinearLayout in your layout XML

        // Call method to fetch deductions
        fetchDeductions();
    }

    private void fetchDeductions() {
        db.collection("deductions")
                .orderBy("timestamp") // Ensure you have a "timestamp" field in your Firestore documents
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productName = document.getString("productName");
                            // Fetch the deductedAmount as a Number and convert it to a String
                            Object deductedAmountObj = document.get("deductedAmount");
                            String deductedAmount;

                            if (deductedAmountObj instanceof String) {
                                deductedAmount = (String) deductedAmountObj;
                            } else if (deductedAmountObj instanceof Number) {
                                deductedAmount = String.valueOf(((Number) deductedAmountObj).doubleValue());
                            } else {
                                deductedAmount = "Invalid Amount"; // Handle unexpected types
                            }

                            // Get the Timestamp object
                            Timestamp timestamp = document.getTimestamp("timestamp");
                            String formattedTimestamp = formatTimestamp(timestamp); // Format the timestamp

                            // Create TextViews dynamically for each deduction
                            createDeductionView(productName, deductedAmount, formattedTimestamp);
                        }
                    } else {
                        Toast.makeText(this, "Error fetching deductions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createDeductionView(String productName, String deductedAmount, String formattedTimestamp) {
        // Create new TextViews
        TextView productNameTV = new TextView(this);
        TextView deductedAmountTV = new TextView(this);
        TextView timestampTV = new TextView(this);
        View separatorView = new View(this); // Create a View for the separator

        // Set text and styles
        productNameTV.setText("Product Name: " + productName);
        deductedAmountTV.setText("Deducted Amount: " + deductedAmount);
        timestampTV.setText("Date & Time: " + formattedTimestamp);

        productNameTV.setTextSize(18);
        deductedAmountTV.setTextSize(16);
        timestampTV.setTextSize(14);
        timestampTV.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Set properties for the separator
        separatorView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2 // Height of the separator
        ));
        separatorView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray)); // Color of the line

        // Add TextViews and separator to the LinearLayout
        linearLayout.addView(productNameTV);
        linearLayout.addView(deductedAmountTV);
        linearLayout.addView(timestampTV);
        linearLayout.addView(separatorView); // Add the separator line after each deduction
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            // Desired output format
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            return outputFormat.format(date);
        } else {
            return "Invalid Date"; // Handle null timestamp
        }
    }
}
