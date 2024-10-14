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
    private boolean lowQuantityFound; // Flag to track low quantity items

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

        // Fetch user's email to get the associated username
        String userEmail = user.getEmail();
        Log.d(TAG, "Authenticated user email: " + userEmail);

        // Fetch the userName based on the email from the "customers" collection
        db.collection("customers")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Assuming there's only one match for the user's email
                        String username = task.getResult().getDocuments().get(0).getString("userName");
                        Log.d(TAG, "Fetched userName: " + username);

                        // Now fetch inventory based on the username
                        fetchInventoryByUserName(username);
                    } else {
                        Log.e(TAG, "User not found in customers collection");
                        Toast.makeText(Inventory.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchInventoryByUserName(String username) {
        // Fetch the tracking IDs for the specified user
        CollectionReference trackingIdRef = db.collection("users").document(username).collection("inventory");

        trackingIdRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot trackingIdDocument : task.getResult()) {
                    String trackingId = trackingIdDocument.getId(); // Get the tracking ID
                    fetchItemsByTrackingId(username, trackingId); // Fetch items for the tracking ID
                }
            } else {
                Log.e(TAG, "Error fetching tracking IDs", task.getException());
                Toast.makeText(Inventory.this, "Error fetching tracking IDs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchItemsByTrackingId(String username, String trackingId) {
        // Construct path to the items using the dynamic tracking ID
        CollectionReference itemsRef = db.collection("users").document(username)
                .collection("inventory").document(trackingId).collection("items");

        itemsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Instead of clearing the lists, just add items to them
                for (QueryDocumentSnapshot document : task.getResult()) {
                    InventoryItem item = document.toObject(InventoryItem.class);
                    allItemsList.add(item);
                    inventoryList.add(item);
                    Log.d(TAG, "Fetched Item: " + item.getProductName() + ", Quantity: " + item.getQuantity());

                    // Check for low quantity
                    checkLowQuantity(item);
                }
                inventoryAdapter.notifyDataSetChanged(); // Refresh RecyclerView after all items are added

                // Show notification if low quantity found
                if (lowQuantityFound) {
                    Toast.makeText(Inventory.this, "Some items have low quantity (less than 5)", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "Error fetching items", task.getException());
                Toast.makeText(Inventory.this, "Error fetching inventory", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLowQuantity(InventoryItem item) {
        try {
            String quantityStr = item.getQuantity();
            String[] parts = quantityStr.split("\\(");
            int quantity = Integer.parseInt(parts[0].trim());

            if (quantity < 5) {
                lowQuantityFound = true;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing quantity for item: " + item.getProductName(), e);
            Toast.makeText(Inventory.this, "Invalid quantity for item: " + item.getProductName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterItems(String query) {
        inventoryList.clear();
        if (query.isEmpty()) {
            // Show all items if search query is empty
            inventoryList.addAll(allItemsList);
        } else {
            // Filter items based on the query for both product name and UPC
            for (InventoryItem item : allItemsList) {
                String productName = item.getProductName().toLowerCase();
                String upc = item.getUpc().toLowerCase(); // Assuming getUpc() returns UPC as a String

                if (productName.contains(query.toLowerCase()) || upc.contains(query.toLowerCase())) {
                    inventoryList.add(item);
                }
            }
        }
        inventoryAdapter.notifyDataSetChanged();  // Refresh RecyclerView
    }
}
