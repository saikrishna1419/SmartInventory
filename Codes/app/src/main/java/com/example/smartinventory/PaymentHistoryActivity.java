package com.example.smartinventory;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PaymentHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView paymentHistoryRecyclerView;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private List<Payment> paymentHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        // Initialize Firestore and RecyclerView
        db = FirebaseFirestore.getInstance();
        paymentHistoryRecyclerView = findViewById(R.id.recyclerViewPaymentHistory);
        paymentHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paymentHistoryList = new ArrayList<>();
        paymentHistoryAdapter = new PaymentHistoryAdapter(paymentHistoryList);
        paymentHistoryRecyclerView.setAdapter(paymentHistoryAdapter);

        // Fetch the logged-in user's payment history
        fetchPaymentHistory();
    }

    private void fetchPaymentHistory() {
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
                        String username = userTask.getResult().getDocuments().get(0).getString("userName");
                        fetchPaymentsForUser(username);
                    } else {
                        Toast.makeText(this, "Error fetching username", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchPaymentsForUser(String username) {
        db.collection("users")
                .document(username)
                .collection("payment_history")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Payment payment = document.toObject(Payment.class);
                            paymentHistoryList.add(payment);
                        }
                        paymentHistoryAdapter.notifyDataSetChanged(); // Notify adapter to update the UI
                    } else {
                        Log.e("PaymentHistoryActivity", "Error fetching payment history", task.getException());
                        Toast.makeText(this, "Error fetching payment history", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
