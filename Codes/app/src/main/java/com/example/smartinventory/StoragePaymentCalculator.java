package com.example.smartinventory;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StoragePaymentCalculator {

    private FirebaseFirestore db;
    private String username;

    public StoragePaymentCalculator(String username) {
        this.db = FirebaseFirestore.getInstance();
        this.username = username;
    }

    // Calculate the number of days since check-in
    public long calculateDaysBetweenDates(String checkInDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        try {
            Date checkIn = sdf.parse(checkInDate);
            Date currentDate = new Date();
            long diffInMillis = currentDate.getTime() - checkIn.getTime();
            return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            Log.e("StoragePaymentCalc", "Error parsing date", e);
            return 0;
        }
    }

    // Calculate the storage cost based on number of days
    public double calculateStorageCost(long daysStored) {
        double dailyRate = 5.0; // $5 per day
        return daysStored * dailyRate;
    }

    // Check if storagePayment document exists and create it if necessary
    public void updateStoragePayment(String checkInDate) {
        DocumentReference paymentRef = db.collection("users").document(username)
                .collection("payments").document("storagePayment");

        paymentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // Document exists, update the existing document
                    updateStorageCost(paymentRef, checkInDate);
                } else {
                    // Document does not exist, create and initialize the document
                    initializeStoragePayment(paymentRef, checkInDate);
                }
            } else {
                Log.e("StoragePaymentCalc", "Error checking document existence", task.getException());
            }
        });
    }

    // Update existing storage payment document with the new storage cost and days
    private void updateStorageCost(DocumentReference paymentRef, String checkInDate) {
        long daysStored = calculateDaysBetweenDates(checkInDate);
        double paymentAmount = calculateStorageCost(daysStored);

        paymentRef.update("storageCost", paymentAmount, "daysStored", daysStored) // Update both fields
                .addOnSuccessListener(aVoid -> Log.d("StoragePaymentCalc", "Storage payment updated successfully"))
                .addOnFailureListener(e -> Log.e("StoragePaymentCalc", "Error updating storage payment", e));
    }

    // Initialize the storage payment document
    private void initializeStoragePayment(DocumentReference paymentRef, String checkInDate) {
        long daysStored = calculateDaysBetweenDates(checkInDate);
        double paymentAmount = calculateStorageCost(daysStored);

        // Create a new storage payment document
        paymentRef.set(new Payment(username, (String) null, paymentAmount, getCurrentDate(), (int) daysStored))
                .addOnSuccessListener(aVoid -> Log.d("StoragePaymentCalc", "Storage payment document created successfully"))
                .addOnFailureListener(e -> Log.e("StoragePaymentCalc", "Error creating storage payment document", e));
    }

    // Helper method to get current date
    private String getCurrentDate() {
        return new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
    }
}
