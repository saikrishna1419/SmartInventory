package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class warehouseInterface extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse_interface);

        TextView checkInTV = findViewById(R.id.checkInTV);
        TextView liveChatTV = findViewById(R.id.livechatTV);
        TextView requestTV = findViewById(R.id.requestTV);

        requestTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(warehouseInterface.this, Request.class);
                startActivity(intent);
            }
        });

        checkInTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(warehouseInterface.this, checkIn.class);
                startActivity(intent);
            }
        });

        liveChatTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(warehouseInterface.this, ChatActivity.class);
                intent.putExtra("role", "manager");
                startActivity(intent);
            }
        });
    }
}
