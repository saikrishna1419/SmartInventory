package com.example.smartinventory;

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

public class WarehouseProductAdapter extends RecyclerView.Adapter<WarehouseProductAdapter.InventoryViewHolder> {

    private List<InventoryItem> inventoryList;
    private Context context;

    public WarehouseProductAdapter(List<InventoryItem> inventoryList, Context context) {
        this.inventoryList = inventoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.warehouse_inventory_item, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = inventoryList.get(position);
        holder.productNameTV.setText(item.getProductName());
        holder.quantityTV.setText(String.valueOf(item.getQuantity()));
        holder.upcTV.setText(item.getUpc());

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DeductionDetails.class);
            intent.putExtra("productName", item.getProductName());
            intent.putExtra("deductedAmount", item.getQuantity()); // Assuming quantity is the deducted amount
            intent.putExtra("timestamp", item.getDate()); // Assuming date is the timestamp
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
        TextView upcTV;


        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTV = itemView.findViewById(R.id.productNameTV);
            quantityTV = itemView.findViewById(R.id.quantityTV);
            upcTV = itemView.findViewById(R.id.upcTV);

        }
    }
}
