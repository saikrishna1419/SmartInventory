package com.example.smartinventory;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class Inventory extends AppCompatActivity implements InventoryAdaptor.OnRequestButtonClickListener {

    private RecyclerView recyclerView;
    private InventoryAdaptor adapter;
    private List<Item> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewRV);

        // Initialize item list
        itemList = new ArrayList<>();
        itemList.add(new Item("IPhone", 10));
        itemList.add(new Item("Laptop", 5));
        itemList.add(new Item("Toys", 20));

        // Initialize adapter and set it to RecyclerView
        adapter = new InventoryAdaptor(itemList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestButtonClick(int position) {
        // Handle button click for the item at the given position
    }
}
