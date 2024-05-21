package com.example.smartinventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class Inventory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InventoryAdaptor adapter;
    private List<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        items = new ArrayList<>();
        items.add("IPhone");
        items.add("Laptops");
        items.add("Toys");
        items.add("Books");
        items.add("PlayStation");

        adapter = new InventoryAdaptor(items);
        recyclerView.setAdapter(adapter);

    }
}