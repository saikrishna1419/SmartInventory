package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class UserInterface extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        TextView inventoryTV = findViewById(R.id.InventoryTV);
        TextView liveChatTV = findViewById(R.id.livechatTV);
        TextView trackingTV = findViewById(R.id.TrackingTV);
        TextView paymentTV = findViewById(R.id.paymentTV);

        mAuth = FirebaseAuth.getInstance();

        inventoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current user's username
                String username = mAuth.getCurrentUser().getEmail(); // Assuming username is stored in email

                Intent intent = new Intent(UserInterface.this, Inventory.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        liveChatTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInterface.this, UserListActivity.class);
                intent.putExtra("role", "customer"); // Pass customer role
                startActivity(intent);
            }
        });

        trackingTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInterface.this, TrackingActivity.class);
                startActivity(intent);
            }
        });


        paymentTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInterface.this, PaymentOptionsActivity.class);
                startActivity(intent);
            }
        });
    }

}
