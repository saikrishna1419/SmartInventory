package com.example.smartinventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailET;
    private Button resetButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        emailET = findViewById(R.id.emailET);
        resetButton = findViewById(R.id.resetButton);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        resetButton.setOnClickListener(v -> checkEmailAndSendResetLink());
    }

    private void checkEmailAndSendResetLink() {
        String email = emailET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter your email!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("customers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean emailFound = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentEmail = document.getString("email");
                            if (email.equals(documentEmail)) {
                                emailFound = true;
                                sendPasswordResetEmail(email);
                                break;
                            }
                        }

                        if (!emailFound) {
                            checkManagersForEmail(email); // Check managers collection if email not found in customers
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Error accessing customers collection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkManagersForEmail(String email) {
        db.collection("managers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean emailFound = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentEmail = document.getString("email");
                            if (email.equals(documentEmail)) {
                                emailFound = true;
                                sendPasswordResetEmail(email);
                                break;
                            }
                        }

                        if (!emailFound) {
                            Toast.makeText(getApplicationContext(), "Email not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Error accessing managers collection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Reset email sent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
