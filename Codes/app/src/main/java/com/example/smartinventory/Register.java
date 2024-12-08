package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText userNameET, emailET, passwordET, confirmPasswordET, contactET;
    private Spinner typeUserSP;
    private Button registerBT;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userNameET = findViewById(R.id.UserNameET);
        emailET = findViewById(R.id.EmailET); // New EditText for Email
        passwordET = findViewById(R.id.PasswordET);
        confirmPasswordET = findViewById(R.id.Password2ET);
        contactET = findViewById(R.id.ContactET);
        typeUserSP = findViewById(R.id.TypeUserSP);
        registerBT = findViewById(R.id.registerBT);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeUserSP.setAdapter(adapter);

        registerBT.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String userName = userNameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String confirmPassword = confirmPasswordET.getText().toString().trim();
        String contact = contactET.getText().toString().trim();
        String userType = typeUserSP.getSelectedItem().toString();

        if (TextUtils.isEmpty(userName)) {
            userNameET.setError("Username is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailET.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.setError("Enter a valid email address");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordET.setError("Password is required");
            return;
        }

        // Regular expression to check if the password contains at least one uppercase letter, one number, and one symbol, and is at least 6 characters long
        String passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{6,}$";
        if (!password.matches(passwordPattern)) {
            passwordET.setError("Password must be at least 6 characters long, contain one uppercase letter, one number, and one symbol");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordET.setError("Passwords do not match");
            return;
        }

        // Use email for authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                if (verificationTask.isSuccessful()) {
                                    Toast.makeText(Register.this, "Verification email sent. Please check your email.", Toast.LENGTH_SHORT).show();
                                    saveUserToFirestore(user.getUid(), userName, contact, userType, email);
                                    mAuth.signOut();
                                    startActivity(new Intent(Register.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(Register.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void saveUserToFirestore(String userId, String userName, String contact, String userType, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("userName", userName);
        user.put("contact", contact);
        user.put("userType", userType);
        user.put("email", email);

        // Get the current time in milliseconds
        long lastActiveTimestamp = System.currentTimeMillis();
        user.put("lastActiveTimestamp", lastActiveTimestamp); // Add the timestamp

        String collectionName = userType.equals("Customer") ? "customers" : "managers";
        db.collection(collectionName).document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Register.this, "User Registered", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Register.this, "Error adding document", Toast.LENGTH_SHORT).show();
                });
    }
}
