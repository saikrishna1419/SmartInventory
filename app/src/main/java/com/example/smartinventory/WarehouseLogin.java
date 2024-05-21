package com.example.smartinventory;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class WarehouseLogin extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_login);

        mAuth = FirebaseAuth.getInstance();

        Button loginButton = findViewById(R.id.button);

        // Set OnClickListener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ensure user is authenticated
                if (mAuth.getCurrentUser() != null) {
                    // Retrieve user type from Firestore
                    FirebaseFirestore.getInstance().collection("users")
                            .document(mAuth.getCurrentUser().getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String userType = documentSnapshot.getString("userType");
                                    if (userType != null && userType.equals("manager")) {
                                        // User is a manager, navigate to the warehouse interface
                                        Intent intent = new Intent(WarehouseLogin.this, warehouseInterface.class);
                                        startActivity(intent);
                                    } else {
                                        // User is not a manager, show error message
                                        Toast.makeText(WarehouseLogin.this, "You are not authorized to access this page.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Failed to retrieve user details
                                Toast.makeText(WarehouseLogin.this, "Failed to retrieve user details.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // User is not authenticated, show error message or redirect to login page
                    Toast.makeText(WarehouseLogin.this, "You need to login first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
