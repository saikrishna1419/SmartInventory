package com.example.smartinventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class UserInterface extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        TextView inventoryTV = findViewById(R.id.InventoryTV);

        inventoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the registration page
                Intent intent = new Intent(UserInterface.this, Inventory.class);
                startActivity(intent);
            }
        });
    }
}