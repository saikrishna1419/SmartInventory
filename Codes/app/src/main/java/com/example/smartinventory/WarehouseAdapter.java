package com.example.smartinventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WarehouseAdapter extends RecyclerView.Adapter<WarehouseAdapter.WarehouseViewHolder> {

    private List<WarehouseItem> warehouseItems;

    public WarehouseAdapter(List<WarehouseItem> warehouseItems) {
        this.warehouseItems = warehouseItems;
    }

    @NonNull
    @Override
    public WarehouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new WarehouseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WarehouseViewHolder holder, int position) {
        // Get the current WarehouseItem
        WarehouseItem item = warehouseItems.get(position);

        // Set the item name and quantity
        holder.itemNameTextView.setText(item.getProductName());
        holder.quantityTextView.setText("Quantity: " + item.getQuantity());
    }

    @Override
    public int getItemCount() {
        return warehouseItems.size();
    }

    // ViewHolder class
    static class WarehouseViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView quantityTextView;

        WarehouseViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind the views using correct IDs from item_layout.xml
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView); // Correct ID for item name
            quantityTextView = itemView.findViewById(R.id.quantityTextView); // Correct ID for quantity
        }
    }
}
