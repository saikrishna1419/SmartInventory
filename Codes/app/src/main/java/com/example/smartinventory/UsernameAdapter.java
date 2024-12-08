package com.example.smartinventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsernameAdapter extends RecyclerView.Adapter<UsernameAdapter.ViewHolder> {

    private List<String> usernames;
    private OnUsernameClickListener listener;

    public interface OnUsernameClickListener {
        void onUsernameClick(String username);
    }

    public UsernameAdapter(List<String> usernames, OnUsernameClickListener listener) {
        this.usernames = usernames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.username_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String username = usernames.get(position);
        holder.usernameTextView.setText(username);
        holder.itemView.setOnClickListener(v -> listener.onUsernameClick(username));
    }

    @Override
    public int getItemCount() {
        return usernames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
        }
    }
}
