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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText userNameET, passwordET, confirmPasswordET, contactET;
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
        String password = passwordET.getText().toString().trim();
        String confirmPassword = confirmPasswordET.getText().toString().trim();
        String contact = contactET.getText().toString().trim();
        String userType = typeUserSP.getSelectedItem().toString();

        if (TextUtils.isEmpty(userName)) {
            userNameET.setError("Username is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(userName).matches()) {
            userNameET.setError("Enter a valid email address");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordET.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordET.setError("Password must be at least 6 characters long");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordET.setError("Passwords do not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(userName, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(userId, userName, contact, userType);
                    } else {
                        Toast.makeText(Register.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String userName, String contact, String userType) {
        Map<String, Object> user = new HashMap<>();
        user.put("userName", userName);
        user.put("contact", contact);
        user.put("userType", userType);

        String collectionName = "";
        if (userType.equals("Customer")) {
            collectionName = "customers";
        } else if (userType.equals("Manager")) {
            collectionName = "managers";
        }

        if (!collectionName.isEmpty()) {
            db.collection(collectionName).document(userId)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Register.this, "User Registered", Toast.LENGTH_SHORT).show();
                        // Navigate back to MainActivity
                        Intent intent = new Intent(Register.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Close the Register activity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Register.this, "Error adding document", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(Register.this, "Invalid user type", Toast.LENGTH_SHORT).show();
        }
    }
}
