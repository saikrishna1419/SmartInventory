package com.example.smartinventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Added for logging
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText usernameET;
    private EditText passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Initialize EditText fields
        usernameET = findViewById(R.id.UsernameET);
        passwordET = findViewById(R.id.PasswordPW);

        Button loginButton = findViewById(R.id.button);
        TextView forgotPasswordTextView = findViewById(R.id.forgotPasswordTV);
        TextView newUserTextView = findViewById(R.id.NewUserTV);

        // Set OnClickListener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the entered email and password
                String email = usernameET.getText().toString().trim();
                String password = passwordET.getText().toString().trim();

                // Check if email and password are not empty
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method early to prevent further execution
                }

                // Attempt to log in the user
                loginUser(email, password);
            }
        });

        // Set OnClickListener for the "Forgot Password?" TextView
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for the "New User? SignUp" TextView
        newUserTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    // Retrieve user type
                                    getUserType(user.getUid());
                                } else {
                                    Toast.makeText(MainActivity.this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to retrieve user type from Firebase Firestore
    private void getUserType(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference customerDocRef = db.collection("customers").document(userId);
        DocumentReference managerDocRef = db.collection("managers").document(userId);

        // Log the document paths being checked
        Log.d("MainActivity", "Checking customer document: " + customerDocRef.getPath());
        Log.d("MainActivity", "Checking manager document: " + managerDocRef.getPath());

        // First, check the customers collection
        customerDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String userType = documentSnapshot.getString("userType");
                    if (userType != null) {
                        navigateToUserInterface(userType);
                        navigateToChatActivity(userType);
                        // Save FCM token to Firestore
                        saveFcmTokenToFirestore(userId, "customers"); // Pass "customers" to indicate user type
                    } else {
                        Toast.makeText(MainActivity.this, "User type not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // If not found in customers, check the managers collection
                    managerDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String userType = documentSnapshot.getString("userType");
                                if (userType != null) {
                                    navigateToUserInterface(userType);
                                    // Save FCM token to Firestore
                                    saveFcmTokenToFirestore(userId, "managers"); // Pass "managers" to indicate user type
                                } else {
                                    Toast.makeText(MainActivity.this, "User type not found.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Document does not exist.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to retrieve user type: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to retrieve user type: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFcmTokenToFirestore(String userId, String userType) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection(userType) // Use user type to determine collection
                                    .document(userId) // Use userId as document ID
                                    .update("fcmToken", token)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "FCM Token saved successfully!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MainActivity.this, "Failed to save FCM token: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to get FCM token: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to navigate to appropriate interface based on user type
    private void navigateToUserInterface(String userType) {
        if (userType.equals("Customer")) {
            Intent intent = new Intent(MainActivity.this, UserInterface.class);
            startActivity(intent);
        } else if (userType.equals("Manager")) {
            Intent intent = new Intent(MainActivity.this, warehouseInterface.class);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "Unknown user type.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToChatActivity(String userType) {
        Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
        chatIntent.putExtra("userType", userType);
        startActivity(chatIntent);
    }
}
