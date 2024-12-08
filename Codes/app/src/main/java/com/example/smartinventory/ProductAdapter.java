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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<InventoryItem> productList;
    private Context context;

    public ProductAdapter(List<InventoryItem> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_product layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        InventoryItem item = productList.get(position);
        holder.productNameTV.setText(item.getProductName());
        holder.upcTV.setText("UPC: " + item.getUpc());
        holder.quantityTV.setText("Qty: " + item.getQuantity());

        // Set the click listener for the request button
        holder.requestButton.setOnClickListener(v -> {
            // Create an Intent to navigate to RequestDetailsActivity
            Intent intent = new Intent(context, RequestDetailsActivity.class);
            // Pass product details to the new activity
            intent.putExtra("productName", item.getProductName());
            intent.putExtra("quantity", item.getQuantity());
            intent.putExtra("upc", item.getUpc());
            // Start the activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTV;
        TextView upcTV;
        TextView quantityTV;
        Button requestButton;

        ProductViewHolder(View itemView) {
            super(itemView);
            productNameTV = itemView.findViewById(R.id.productNameTV);
            upcTV = itemView.findViewById(R.id.upcTV);
            quantityTV = itemView.findViewById(R.id.quantityTV);
            requestButton = itemView.findViewById(R.id.requestButton);
        }
    }
}
