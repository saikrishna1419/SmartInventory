package com.example.smartinventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button loginButton = findViewById(R.id.button);
        TextView newUserTextView = findViewById(R.id.NewUserTV);
        TextView ownerLoginTextView = findViewById(R.id.ownerLoginTV);

        // Set OnClickListener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the user login page
                Intent intent = new Intent(MainActivity.this, UserInterface.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for the "New User? SignUp" TextView
        newUserTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the registration page
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for the "Owner Login" TextView
        ownerLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the owner login page
                Intent intent = new Intent(MainActivity.this, WarehouseLogin.class);
                startActivity(intent);
            }
        });




    }
}