package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsernameListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<String> usernameList = new ArrayList<>();
    private UsernameAdapter usernameAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_list);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewUsernames);
        usernameAdapter = new UsernameAdapter(usernameList, this::onUsernameClick);
        recyclerView.setAdapter(usernameAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch the usernames
        fetchUsernames();
    }

    private void fetchUsernames() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String username = document.getId();  // Usernames are document IDs
                    usernameList.add(username);
                }
                usernameAdapter.notifyDataSetChanged();
            } else {
                Log.e("UsernameListActivity", "Error fetching users", task.getException());
                Toast.makeText(this, "Error fetching users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onUsernameClick(String username) {
        // Start WarehouseInventoryActivity and pass the selected username
        Intent intent = new Intent(UsernameListActivity.this, WarehouseInventoryActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);

    }
}
