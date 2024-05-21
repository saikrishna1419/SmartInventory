package com.example.smartinventory;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
                checkUserAuthentication();
            }
        });
    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is authenticated, navigate to the warehouse interface
            navigateToWarehouseInterface();
        } else {
            // User is not authenticated, show error message or redirect to login page
            showLoginRequiredMessage();
        }
    }

    private void navigateToWarehouseInterface() {
        Intent intent = new Intent(WarehouseLogin.this, warehouseInterface.class);
        startActivity(intent);
        finish();
    }

    private void showLoginRequiredMessage() {
        Toast.makeText(WarehouseLogin.this, "You need to login first.", Toast.LENGTH_SHORT).show();
    }
}
