package com.example.smartinventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView inventoryRecyclerView;
    private InventoryAdapter inventoryAdapter;
    private List<InventoryItem> inventoryList;
    private List<InventoryItem> allItemsList;
    private static final String TAG = "ProductsFragment";
    private boolean lowQuantityFound = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        db = FirebaseFirestore.getInstance();
        inventoryRecyclerView = view.findViewById(R.id.recyclerViewRV);
        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        inventoryList = new ArrayList<>();
        allItemsList = new ArrayList<>();
        inventoryAdapter = new InventoryAdapter(inventoryList, getContext());
        inventoryRecyclerView.setAdapter(inventoryAdapter);

        EditText searchET = view.findViewById(R.id.searchET);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fetchInventoryData();
        return view;
    }

    private void fetchInventoryData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "No authenticated user found");
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = user.getEmail();
        Log.d(TAG, "Authenticated user email: " + userEmail);

        db.collection("customers")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String username = task.getResult().getDocuments().get(0).getString("userName");
                        Log.d(TAG, "Fetched userName: " + username);
                        fetchInventoryByUserName(username);
                    } else {
                        Log.e(TAG, "User not found in customers collection");
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchInventoryByUserName(String username) {
        CollectionReference trackingIdRef = db.collection("users").document(username).collection("inventory");

        trackingIdRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot trackingIdDocument : task.getResult()) {
                    String trackingId = trackingIdDocument.getId();
                    fetchItemsByTrackingId(username, trackingId);
                }
            } else {
                Log.e(TAG, "Error fetching tracking IDs", task.getException());
                Toast.makeText(getContext(), "Error fetching tracking IDs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchItemsByTrackingId(String username, String trackingId) {
        CollectionReference itemsRef = db.collection("users").document(username)
                .collection("inventory").document(trackingId).collection("items");

        itemsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<InventoryItem> newItems = new ArrayList<>();
                lowQuantityFound = false; // Reset low quantity flag
                for (QueryDocumentSnapshot document : task.getResult()) {
                    InventoryItem item = document.toObject(InventoryItem.class);
                    newItems.add(item);
                    Log.d(TAG, "Fetched Item: " + item.getProductName() + ", Quantity: " + item.getQuantity());

                    // Check for low quantity
                    checkLowQuantity(item);
                }
                allItemsList.addAll(newItems);
                inventoryList.clear();
                inventoryList.addAll(allItemsList);
                inventoryAdapter.notifyDataSetChanged();

                if (lowQuantityFound) {
                    Toast.makeText(getContext(), "Some items have low quantity (less than 5)", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "Error fetching items", task.getException());
                Toast.makeText(getContext(), "Error fetching inventory", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLowQuantity(InventoryItem item) {
        String quantityStr = item.getQuantity();

        // Extract the numeric part of the quantity (up to any non-numeric character like '(')
        String numericQuantity = quantityStr.split("\\(")[0].trim();

        try {
            int quantity = Integer.parseInt(numericQuantity); // Parse the cleaned string as an integer
            if (quantity < 5) {
                lowQuantityFound = true;
            }
        } catch (NumberFormatException e) {
            // Log an error message or handle the case where parsing fails
            Log.e("ProductsFragment", "Invalid quantity format: " + quantityStr, e);
        }
    }


    private void filterItems(String query) {
        inventoryList.clear();
        if (query.isEmpty()) {
            inventoryList.addAll(allItemsList);
        } else {
            for (InventoryItem item : allItemsList) {
                String productName = item.getProductName().toLowerCase();
                String upc = item.getUpc().toLowerCase();

                if (productName.contains(query.toLowerCase()) || upc.contains(query.toLowerCase())) {
                    inventoryList.add(item);
                }
            }
        }
        inventoryAdapter.notifyDataSetChanged();
    }

    // Add this method to handle item clicks and navigate to DeductionDetails
    private void addProductToLayout(InventoryItem item) {
        View productView = LayoutInflater.from(getContext()).inflate(R.layout.product_item, null, false);
        productView.setOnClickListener(v -> navigateToDeductionDetails(item));
        inventoryRecyclerView.addView(productView); // Replace this with the appropriate layout in your adapter
    }

    // Navigate to DeductionDetails
    private void navigateToDeductionDetails(InventoryItem item) {
        Intent intent = new Intent(getContext(), DeductionDetails.class);
        intent.putExtra("productName", item.getProductName());
        intent.putExtra("deductedAmount", item.getDeductedAmount()); // Make sure this field exists in InventoryItem
        intent.putExtra("timestamp", item.getTimestamp()); // Make sure this field exists in InventoryItem
        startActivity(intent);
    }
}
