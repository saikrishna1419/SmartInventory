package com.example.smartinventory;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailET, passwordET, confirmPasswordET;
    private Button resetButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        confirmPasswordET = findViewById(R.id.confirmPasswordET);
        resetButton = findViewById(R.id.resetButton);
        mAuth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = emailET.getText().toString().trim();
        String newPassword = passwordET.getText().toString().trim();
        String confirmPassword = confirmPasswordET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter your email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(getApplicationContext(), "Enter new password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.equals(newPassword, confirmPassword)) {
            Toast.makeText(getApplicationContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.getCurrentUser().updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Finish activity after successful password reset
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ResetPassword", "Failed to update password", task.getException());
                    }
                });
    }
}
