package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_options);

        TextView makePaymentTV = findViewById(R.id.MakePaymentTV);
        TextView paymentHistoryTV = findViewById(R.id.PaymentHistoryTV);

        makePaymentTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentOptionsActivity.this, PaymentPageActivity.class);
                startActivity(intent); // Navigate to PaymentPageActivity
            }
        });

        paymentHistoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentOptionsActivity.this, PaymentHistoryActivity.class); // Create PaymentHistoryActivity
                startActivity(intent); // Navigate to PaymentHistoryActivity
            }
        });
    }
}
