package com.example.smartinventory;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UserListActivity";
    private FirebaseFirestore db;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private ArrayList<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        String role = getIntent().getStringExtra("role");
        Log.d(TAG, "User role: " + role);


        if ("manager".equals(role)) {
            fetchCustomersOrderedByLastActive();
        } else if ("customer".equals(role)) {
            fetchManagersOrderedByLastActive();
        } else {
            Log.w(TAG, "Unknown role. Defaulting to fetch customers.");
            fetchCustomersOrderedByLastActive();
        }

    }



    private void fetchCustomersOrderedByLastActive() {
        Log.d(TAG, "Listening for real-time updates of customers ordered by last active timestamp.");
        CollectionReference customersRef = db.collection("customers");
        customersRef.orderBy("lastActiveTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for real-time updates", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        userList.clear();
                        Log.d(TAG, "Number of customers fetched: " + queryDocumentSnapshots.size());
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userName = document.getString("userName");
                            String email = document.getString("email");
                            User user = new User(document.getString("contact"), email, userName, document.getString("userType"));

                            // Check for unread messages
                            checkForUnreadMessages(user);

                            userList.add(user);
                        }

                        // Update the adapter with the new list
                        if (userAdapter == null) {
                            userAdapter = new UserAdapter(userList, UserListActivity.this);
                            userRecyclerView.setAdapter(userAdapter);
                        } else {
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }


    private void fetchManagersOrderedByLastActive() {
        Log.d(TAG, "Listening for real-time updates of managers ordered by last active timestamp.");
        CollectionReference managersRef = db.collection("managers");
        managersRef.orderBy("lastActiveTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for real-time updates", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        userList.clear();
                        Log.d(TAG, "Number of managers fetched: " + queryDocumentSnapshots.size());
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userName = document.getString("userName");
                            String email = document.getString("email");
                            User user = new User(document.getString("contact"), email, userName, document.getString("userType"));

                            // Check for unread messages
                            checkForUnreadMessages(user);

                            userList.add(user);
                        }

                        // Update the adapter with the new list
                        if (userAdapter == null) {
                            userAdapter = new UserAdapter(userList, UserListActivity.this);
                            userRecyclerView.setAdapter(userAdapter);
                        } else {
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }



    private String getCurrentUserEmail() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } else {
            return null;
        }
    }

    private void checkForUnreadMessages(User user) {
        // Construct the path to the specific chat between sender and receiver
        String senderMail = getCurrentUserEmail();
        String receiverMail = user.getEmail();
        Log.d(TAG, "Checking unread messages for chat between sender: " + senderMail + " and receiver: " + receiverMail);

        // Query the messages collection for unread messages (where "isRead" is false)
        db.collection("chats")
                .document(senderMail + "_" + receiverMail)
                .collection("messages")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean hasUnreadMessages = false;

                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Loop through all messages in the chat
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Boolean isRead = document.getBoolean("read");
                            Log.d(TAG, "Message ID: " + document.getId() + ", read: " + isRead);
                            if (isRead != null && !isRead) {
                                hasUnreadMessages = true;
                                break;
                            }
                        }
                    } else {
                        Log.d(TAG, "No messages found for chat between sender: " + senderMail + " and receiver: " + receiverMail);
                    }


                    // Set the unread status for the user
                    user.setHasUnreadMessages(hasUnreadMessages);

                    // Notify the adapter about the change for the specific user
                    int index = userList.indexOf(user);
                    if (index != -1) {
                        userAdapter.notifyItemChanged(index);
                    }

                    // Log for debugging purposes
                    Log.d("UnreadStatus", "User: " + user.getUserName() + ", Unread: " + hasUnreadMessages);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching unread messages", e));
    }



}
