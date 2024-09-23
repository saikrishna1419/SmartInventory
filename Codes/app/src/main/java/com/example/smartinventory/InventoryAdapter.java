package com.example.smartinventory;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
// InventoryAdapter.java

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<InventoryItem> inventoryList;
    private Context context;

    public InventoryAdapter(List<InventoryItem> inventoryList, Context context) {
        this.inventoryList = inventoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_inventory_item, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = inventoryList.get(position);
        holder.productNameTV.setText(item.getProductName());
        holder.quantityTV.setText(String.valueOf(item.getQuantity()));

        // Hide UPC TextView
        holder.upcTV.setVisibility(View.GONE);

        holder.requestButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, RequestDetailsActivity.class);
            intent.putExtra("productName", item.getProductName());
            intent.putExtra("quantity", item.getQuantity());
            intent.putExtra("upc", item.getUpc()); // Pass UPC to RequestDetailsActivity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTV;
        TextView quantityTV;
        TextView upcTV; // This will be hidden
        Button requestButton;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTV = itemView.findViewById(R.id.productNameTV);
            quantityTV = itemView.findViewById(R.id.quantityTV);
            upcTV = itemView.findViewById(R.id.upcTV);
            requestButton = itemView.findViewById(R.id.requestButton);
        }
    }
}
