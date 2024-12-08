package com.example.smartinventory;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WarehouseProductListActivity extends AppCompatActivity {
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<InventoryItem> productList; // List to hold products
    private FirebaseFirestore db;
    private String trackingId; // Tracking ID passed from PackagesFragment
    private String username; // Username for fetching products
    private static final String TAG = "WarehouseProductListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse_product_list); // Make sure this matches your XML layout file name

        // Get the tracking ID and username from the intent
        trackingId = getIntent().getStringExtra("trackingId");
        username = getIntent().getStringExtra("username"); // Assuming you passed this from the previous activity

        productRecyclerView = findViewById(R.id.recyclerViewProducts);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        productRecyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();

        if (username != null && trackingId != null) {
            fetchItemsByTrackingId(username, trackingId);
            Log.e(TAG, "Username or trackingId is null");

        } else {
            Toast.makeText(this, "Invalid tracking ID or username", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Username or tracking ID is null");
        }
    }

    private void fetchItemsByTrackingId(String username, String trackingId) {
        productList.clear(); // Clear the list before fetching new data
        CollectionReference itemsRef = db.collection("users").document(username)
                .collection("inventory").document(trackingId).collection("items");

        itemsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    InventoryItem item = document.toObject(InventoryItem.class);
                    productList.add(item); // Add each item to the product list
                    Log.d(TAG, "Fetched Item: " + item.getProductName() + ", Quantity: " + item.getQuantity());
                }
                productAdapter.notifyDataSetChanged(); // Notify adapter of data change
            } else {
                Log.e(TAG, "Error fetching items", task.getException());
                Toast.makeText(this, "Error fetching items", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
