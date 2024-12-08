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

public class WarehousePackagesAdapter extends RecyclerView.Adapter<WarehousePackagesAdapter.PackageViewHolder> {

    private List<String> packageList;
    private Context context;
    private String username;

    public WarehousePackagesAdapter(List<String> packageList,String username, Context context) {
        this.packageList = packageList;
        this.username = username;
        this.context = context;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        String trackingId = packageList.get(position);
        holder.packageIdTV.setText(trackingId);

        holder.openButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, WarehouseProductListActivity.class);
            intent.putExtra("trackingId", trackingId);
            intent.putExtra("username", username);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

    public static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView packageIdTV;
        Button openButton;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            packageIdTV = itemView.findViewById(R.id.packageIdTV);
            openButton = itemView.findViewById(R.id.openButton);
        }
    }

}
