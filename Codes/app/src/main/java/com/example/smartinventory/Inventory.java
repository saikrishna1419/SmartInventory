package com.example.smartinventory;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Inventory extends AppCompatActivity {
    private static final String TAG = "InventoryActivity";
    private FirebaseFirestore db;
    private RecyclerView inventoryRecyclerView;
    private InventoryAdapter inventoryAdapter;
    private List<InventoryItem> inventoryList;
    private List<InventoryItem> allItemsList;  // List to store all items for resetting search

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Initialize Firestore
        initializeFirestore();

        // Initialize RecyclerView
        inventoryRecyclerView = findViewById(R.id.recyclerViewRV);
        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        inventoryList = new ArrayList<>();
        allItemsList = new ArrayList<>();
        inventoryAdapter = new InventoryAdapter(inventoryList, this);
        inventoryRecyclerView.setAdapter(inventoryAdapter);

        // Initialize search EditText
        EditText searchET = findViewById(R.id.searchET);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        // Fetch inventory data
        fetchInventoryData();
    }

    private void initializeFirestore() {
        try {
            db = FirebaseFirestore.getInstance();
            Log.d(TAG, "Firestore initialized successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firestore", e);
        }
    }

    private void fetchInventoryData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "No authenticated user found");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = user.getEmail();
        Log.d(TAG, "Fetching items for user: " + username);

        // Hard-code the tracking ID
        String trackingId = "T1234";
        Log.d(TAG, "Fetching items for tracking ID: " + trackingId);

        // Construct path
        CollectionReference itemsRef = db.collection("users").document(username)
                .collection("inventory").document(trackingId).collection("items");

        itemsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allItemsList.clear();  // Clear previous data if any
                    inventoryList.clear();  // Clear current data displayed
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        InventoryItem item = document.toObject(InventoryItem.class);
                        allItemsList.add(item);  // Add item to all items list
                        inventoryList.add(item);  // Add item to display list
                        Log.d(TAG, "Fetched Item: " + item.getProductName() + ", Quantity: " + item.getQuantity());
                    }
                    inventoryAdapter.notifyDataSetChanged();  // Refresh RecyclerView
                    Log.d(TAG, "Items fetch successful for tracking ID: " + trackingId);
                } else {
                    Log.e(TAG, "Error fetching items for tracking ID: " + trackingId, task.getException());
                    Toast.makeText(Inventory.this, "Error fetching items for tracking ID: " + trackingId, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterItems(String query) {
        inventoryList.clear();
        if (query.isEmpty()) {
            // Show all items if search query is empty
            inventoryList.addAll(allItemsList);
        } else {
            // Filter items based on the query
            for (InventoryItem item : allItemsList) {
                if (item.getProductName().toLowerCase().contains(query.toLowerCase())) {
                    inventoryList.add(item);
                }
            }
        }
        inventoryAdapter.notifyDataSetChanged();  // Refresh RecyclerView
    }
}
