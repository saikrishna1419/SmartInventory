package com.example.smartinventory;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private ArrayList<User> userList;
    private Context context;

    public UserAdapter(ArrayList<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the list
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        // Set the username
        holder.userNameTV.setText(user.getUserName());



        // Show the unread message bubble if the user has unread messages
        if (user.hasUnreadMessages()) {
            holder.unreadIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.unreadIndicator.setVisibility(View.GONE);
        }

        // Set up chat button to open ChatActivity when clicked
        holder.chatButton.setOnClickListener(v -> {
            if (context != null) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("selectedUser", user.getUserName());
                intent.putExtra("selectedUserEmail", user.getEmail());
                Log.d("UserAdapter", "Selected User Email: " + user.getEmail());
                context.startActivity(intent);
            } else {
                Log.e("UserAdapter", "Context is null. Cannot start ChatActivity.");
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // ViewHolder class to hold references to the views in each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTV;
        Button chatButton;
        ImageView unreadIndicator;

        public ViewHolder(View itemView) {
            super(itemView);
            userNameTV = itemView.findViewById(R.id.userNameTV);
            chatButton = itemView.findViewById(R.id.chatButton);
            unreadIndicator = itemView.findViewById(R.id.unreadBubble);  // Ensure this is added to your layout
        }
    }
}
