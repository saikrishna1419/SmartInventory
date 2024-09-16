package com.example.smartinventory;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Inventory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private List<InventoryItem> itemList;
    private EditText searchET;
    private FirebaseFirestore db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        recyclerView = findViewById(R.id.recyclerViewRV);
        searchET = findViewById(R.id.searchET);
        db = FirebaseFirestore.getInstance();

        // Assume username is passed via Intent or saved in SharedPreferences
        username = getIntent().getStringExtra("USERNAME");

        itemList = new ArrayList<>();
        adapter = new InventoryAdapter(itemList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchInventoryItems();

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchInventoryItems() {
        db.collection("users").document(username).collection("inventory")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        itemList.clear();
                        // Iterate over trackingId documents
                        for (QueryDocumentSnapshot trackingDoc : task.getResult()) {
                            String trackingId = trackingDoc.getId();
                            // Fetch items in the trackingId document
                            db.collection("users").document(username)
                                    .collection("inventory").document(trackingId)
                                    .collection("items")
                                    .get()
                                    .addOnCompleteListener(itemsTask -> {
                                        if (itemsTask.isSuccessful()) {
                                            // Iterate over items in the items collection
                                            for (QueryDocumentSnapshot itemDoc : itemsTask.getResult()) {
                                                InventoryItem item = itemDoc.toObject(InventoryItem.class);
                                                itemList.add(item);
                                            }
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(Inventory.this, "Error fetching items for trackingId " + trackingId, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Inventory.this, "Error fetching inventory", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

