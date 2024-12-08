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

public class ProductListActivity extends AppCompatActivity {
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<InventoryItem> productList; // List to hold products
    private FirebaseFirestore db;
    private String trackingId; // Tracking ID passed from PackagesFragment
    private static final String TAG = "ProductListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list); // Make sure this matches your XML layout file name

        // Get the tracking ID from the intent
        trackingId = getIntent().getStringExtra("trackingId");

        productRecyclerView = findViewById(R.id.recyclerViewProducts);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        productRecyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();
        fetchProductData();
    }

    private void fetchProductData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "No authenticated user found");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = user.getEmail();
        db.collection("customers")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String username = task.getResult().getDocuments().get(0).getString("userName");
                        fetchItemsByTrackingId(username, trackingId);
                    } else {
                        Log.e(TAG, "User not found in customers collection");
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
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
