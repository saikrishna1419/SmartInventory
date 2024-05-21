package com.example.smartinventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
        TextView warehosue = findViewById(R.id.ownerLoginTV);

        warehosue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WarehouseLogin.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the entered email and password
                String email = usernameET.getText().toString().trim();
                String password = passwordET.getText().toString().trim();

                // Check if email and password are not empty
                if (email.isEmpty() || password.isEmpty()) {
                    // Show a toast message to the user indicating that they need to enter both email and password
                    Toast.makeText(MainActivity.this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method early to prevent further execution
                }

                // Attempt to log in the user
                loginUser(email, password);
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
    }


    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Navigate to the default user interface or main activity after successful login
                                navigateToMainInterface();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to navigate to the main user interface after successful login
    private void navigateToMainInterface() {
        Intent intent = new Intent(MainActivity.this, UserInterface.class); // or another main activity
        startActivity(intent);
        finish();
    }
}
