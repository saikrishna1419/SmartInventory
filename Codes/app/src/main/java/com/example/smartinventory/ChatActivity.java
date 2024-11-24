package com.example.smartinventory;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private static final String FCM_SERVER_URL = "https://fcm.googleapis.com/v1/projects/smartinventory-da577/messages:send";
    private static final String CHANNEL_ID = "chat_notifications"; // Unique channel ID

    private String currentUserEmail;
    private String currentUserUsername;
    private String chatPartnerEmail;
    private String chatPartnerUsername;
    private RecyclerView chatRecyclerView;
    private RecyclerView recyclerView;
    private List<ChatMessage> messageList;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private FirebaseFirestore firestore;

    private List<User> userList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // Your custom back press logic
                    markMessagesAsRead("currentUserEmail", "chatPartnerEmail");

                    // Finish the activity after custom handling
                    finish();
                }
            });



    // Create the Notification Channel (for Android O and above)
        createNotificationChannel();

        // Initialize Firestore and UI components
        firestore = FirebaseFirestore.getInstance();
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, getCurrentUserEmail());

        // Initialize userList
        userList = new ArrayList<>(); // Initialize as an empty list
        userAdapter = new UserAdapter((ArrayList<User>) userList, this); // Pass the userList to UserAdapter

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        currentUserEmail = getCurrentUserEmail();
        chatPartnerEmail = getIntent().getStringExtra("selectedUserEmail");
        chatPartnerUsername = getIntent().getStringExtra("selectedUser");
        markMessagesAsRead(currentUserEmail,chatPartnerEmail);

        Log.d(TAG, "Current User Email: " + currentUserEmail);
        Log.d(TAG, "Chat Partner Email: " + chatPartnerEmail);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserEmail);
        chatRecyclerView.setAdapter(chatAdapter);

        // Fetch messages and update the RecyclerView
        fetchMessages();

        if (currentUserEmail == null || chatPartnerEmail == null) {
            Toast.makeText(this, "Error: User emails are null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        sendButton.setOnClickListener(v -> sendMessage());

        loadChatMessages();
    }

    private void loadChatMessages() {
        firestore.collection("chats")
                .document(currentUserEmail + "_" + chatPartnerEmail)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Error loading messages: ", error);
                            return;
                        }

                        if (value != null) {
                            for (DocumentChange dc : value.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    ChatMessage message = dc.getDocument().toObject(ChatMessage.class);
                                    chatMessages.add(message);  // Add message to the list

                                    // Check if the user is not currently in the chat activity
                                    if (!isCurrentUserInChatActivity()) {
                                        showNotification(message); // Show notification for new message
                                    }
                                }
                            }

                            chatAdapter.notifyDataSetChanged();
                            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
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

    private void moveUserToTop(String username) {
        Log.d(TAG, "Moving user to top in Firestore: " + username);

        // Update the user document in the "managers" collection
        firestore.collection("managers")
                .whereEqualTo("userName", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            document.getReference().update("lastActive", System.currentTimeMillis()) // Update lastActive timestamp
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User moved to top in managers: " + username))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error moving user to top in managers: ", e));
                        }
                    } else {
                        // If not found in managers, check in the "customers" collection
                        firestore.collection("customers")
                                .whereEqualTo("userName", username)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                    if (!queryDocumentSnapshots2.isEmpty()) {
                                        for (DocumentSnapshot document : queryDocumentSnapshots2.getDocuments()) {
                                            document.getReference().update("lastActive", System.currentTimeMillis()) // Update lastActive timestamp
                                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User moved to top in customers: " + username))
                                                    .addOnFailureListener(e -> Log.e(TAG, "Error moving user to top in customers: ", e));
                                        }
                                    } else {
                                        Log.d(TAG, "User not found in either collection: " + username);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving customers collection: ", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving managers collection: ", e));
    }

    private void fetchCurrentUserUsername() {
        String userEmail = getCurrentUserEmail();
        if (userEmail != null) {
            // Clear existing user data if necessary
            userList.clear(); // Assuming you want to clear the list before fetching

            // Create a Task for fetching from both collections
            Task<Void> fetchManagers = firestore.collection("managers")
                    .get()
                    .continueWith(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (userEmail.equals(document.getString("email"))) {
                                    currentUserUsername = document.getString("userName");
                                    Log.d(TAG, "Current User Username from Managers: " + currentUserUsername);
                                    Log.d(TAG, "Moving user to top after fetching from managers");
                                    moveUserToTop(currentUserUsername);
                                    userList.add(document.toObject(User.class)); // Assuming User class has the same structure
                                    // Add logging here to check if moveUserToTop is called

                                    return null; // Exit early if found
                                }
                            }
                        } else {
                            Log.e(TAG, "Error fetching managers: ", task.getException());
                        }
                        return null;
                    });

            Task<Void> fetchCustomers = firestore.collection("customers")
                    .get()
                    .continueWith(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (userEmail.equals(document.getString("email"))) {
                                    currentUserUsername = document.getString("userName");
                                    Log.d(TAG, "Current User Username from Customers: " + currentUserUsername);
                                    userList.add(document.toObject(User.class)); // Assuming User class has the same structure
                                    // Add logging here to check if moveUserToTop is called
                                    Log.d(TAG, "Moving user to top after fetching from customers");
                                    moveUserToTop(currentUserUsername);
                                    return null; // Exit early if found
                                }
                            }
                        } else {
                            Log.e(TAG, "Error fetching customers: ", task.getException());
                        }
                        return null;
                    });


            // Wait for both fetch tasks to complete
            Tasks.whenAllComplete(fetchManagers, fetchCustomers)
                    .addOnCompleteListener(task -> {
                        // Optional: Handle any final logic after both fetches are complete
                        if (currentUserUsername == null) {
                            Log.d(TAG, "Current user not found in either collection: " + userEmail);
                        }
                    });
        }
    }



    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        ChatMessage message = new ChatMessage(currentUserEmail, messageText, System.currentTimeMillis(), false);

        // Send the message to the chat document
        firestore.collection("chats")
                .document(currentUserEmail + "_" + chatPartnerEmail)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Message sent successfully.");
                    messageEditText.setText("");

                    // Update user's position in Firestore after sending the message
                    updateUserPosition(currentUserEmail);

                    // Send a notification to the chat partner asynchronously
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        try {
                            String accessToken = getAccessToken();
                            getChatPartnerToken(token -> {
                                if (token != null) {
                                    sendNotification(accessToken, token, messageText);
                                } else {
                                    Log.e(TAG, "Chat partner token is null.");
                                }
                            });
                        } catch (IOException e) {
                            Log.e(TAG, "Error getting access token: ", e);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message: ", e);
                    Toast.makeText(ChatActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                });

        // Also save the message to the reverse chat document
        firestore.collection("chats")
                .document(chatPartnerEmail + "_" + currentUserEmail)
                .collection("messages")
                .add(message);
    }

    private void updateUserPosition(String userEmail) {
        Log.d(TAG, "Updating user position for email: " + userEmail);

        // First check in the managers collection
        firestore.collection("managers")
                .orderBy("lastActiveTimestamp", Query.Direction.DESCENDING) // Order by lastActiveTimestamp if needed
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Successfully retrieved managers collection.");
                        boolean userFound = false; // Flag to check if user is found

                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Checking document: " + document.getId() + ", Email: " + document.getString("email"));
                            if (userEmail.equals(document.getString("email"))) {
                                currentUserUsername = document.getString("userName");
                                Log.d(TAG, "User found in managers: " + currentUserUsername);
                                userFound = true; // Set flag to true
                                // Update the last active timestamp
                                document.getReference().update("lastActiveTimestamp", System.currentTimeMillis())
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User position updated successfully in managers."))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error updating user position in managers: ", e));
                                break; // Exit loop once found
                            }
                        }

                        if (!userFound) {
                            Log.d(TAG, "User not found in managers, proceeding to check customers.");
                            updateCustomerPosition(userEmail); // Call to check customers if not found
                        }
                    } else {
                        Log.e(TAG, "Error retrieving managers collection: ", task.getException());
                    }
                });
    }

    private void updateCustomerPosition(String userEmail) {
        Log.d(TAG, "Checking customers collection for email: " + userEmail);

        firestore.collection("customers")
                .orderBy("lastActiveTimestamp", Query.Direction.DESCENDING) // Order by lastActiveTimestamp if needed
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Successfully retrieved customers collection.");
                        boolean userFound = false; // Flag to check if user is found

                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Checking document: " + document.getId() + ", Email: " + document.getString("email"));
                            if (userEmail.equals(document.getString("email"))) {
                                currentUserUsername = document.getString("userName");
                                Log.d(TAG, "User found in customers: " + currentUserUsername);
                                userFound = true; // Set flag to true
                                // Update the last active timestamp
                                document.getReference().update("lastActiveTimestamp", System.currentTimeMillis())
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User position updated successfully in customers."))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error updating user position in customers: ", e));
                                break; // Exit loop once found
                            }
                        }

                        if (!userFound) {
                            Log.d(TAG, "User not found in customers.");
                        }
                    } else {
                        Log.e(TAG, "Error retrieving customers collection: ", task.getException());
                    }
                });
    }


    // Fetch the messages from Firestore and update the adapter
    private void fetchMessages() {
        firestore.collection("chats")
                .document(currentUserEmail + "_" + chatPartnerEmail)
                .collection("messages")
                .orderBy("timestamp")  // Sort by timestamp
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching messages", error);
                        return;
                    }

                    if (querySnapshot != null) {
                        messageList.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            ChatMessage chatMessage = document.toObject(ChatMessage.class);
                            messageList.add(chatMessage);
                        }
                        chatAdapter.updateMessages(messageList);  // Update adapter with the new message list
                    }
                });
    }

    public void markMessageAsRead(String senderEmail, String receiverEmail, String messageId) {
        // Reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update the "read" field to true for the specific message
        db.collection("chats")
                .document(senderEmail + "_" + receiverEmail)  // Path to the chat between the sender and receiver
                .collection("messages")
                .document(messageId)  // Reference to the specific message by its ID
                .update("read", true)  // Set the "read" field to true
                .addOnSuccessListener(aVoid -> {
                    // Successfully marked the message as read
                    Log.d(TAG, "Message " + messageId + " marked as read.");
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the update
                    Log.e(TAG, "Error marking message " + messageId + " as read", e);
                });
    }

    private void markMessagesAsRead(String senderEmail, String receiverEmail) {
        firestore.collection("chats")
                .document(senderEmail + "_" + receiverEmail)
                .collection("messages")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String messageId = document.getId();
                        markMessageAsRead(senderEmail, receiverEmail, messageId);
                    }

                    // After marking as read, refresh the message list to update the RecyclerView
                    fetchMessages(); // Refresh the messages list to show updated read status
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error marking messages as read: ", e));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Mark all messages as read when the activity is paused (user leaves the chat)
        markMessagesAsRead(currentUserEmail, chatPartnerEmail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the RecyclerView so that unread bubbles are updated based on the "read" field
        fetchMessages();
    }

    /*
    @Override
    public void onBackPressed() {

            // For Android versions below API level 31, use the old method
            markMessagesAsRead("currentUserEmail", "chatPartnerEmail");

            // Finish the activity
            super.onBackPressed();

    }
*/


    private boolean isCurrentUserInChatActivity() {
        // Check if the user is currently viewing the chat activity
        return currentUserEmail.equals(getCurrentUserEmail()) && chatPartnerEmail.equals(getIntent().getStringExtra("selectedUserEmail"));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Chat Notifications";
            String description = "Notifications for chat messages";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String getAccessToken() throws IOException {
        InputStream inputStream = getApplicationContext().getAssets().open("smartinventory-da577-firebase-adminsdk-kkiwb-7ea1cf49f6.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));

        AccessToken token = credentials.refreshAccessToken();
        return token.getTokenValue();
    }

    private void getChatPartnerToken(OnTokenReceived callback) {
        Log.d(TAG, "Attempting to fetch token for username: " + chatPartnerUsername);

        // Check in the managers collection first
        firestore.collection("managers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if any documents were retrieved
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Loop through the documents
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String username = document.getString("userName");
                            if (username != null && username.equals(chatPartnerUsername)) {
                                // Username matches, retrieve the token
                                String token = document.getString("fcmToken");
                                if (token != null) {
                                    Log.d(TAG, "Chat partner token retrieved successfully: " + token);
                                    callback.onTokenReceived(token);
                                    return; // Exit after finding the token
                                } else {
                                    Log.e(TAG, "Token field is missing for the user document in managers.");
                                }
                            }
                        }
                    }

                    // If not found in managers, check in the customers collection
                    firestore.collection("customers")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                if (!queryDocumentSnapshots2.isEmpty()) {
                                    // Loop through the documents
                                    for (DocumentSnapshot document : queryDocumentSnapshots2.getDocuments()) {
                                        String username = document.getString("userName");
                                        if (username != null && username.equals(chatPartnerUsername)) {
                                            // Username matches, retrieve the token
                                            String token = document.getString("fcmToken");
                                            if (token != null) {
                                                Log.d(TAG, "Chat partner token retrieved successfully: " + token);
                                                callback.onTokenReceived(token);
                                                return; // Exit after finding the token
                                            } else {
                                                Log.e(TAG, "Token field is missing for the user document in customers.");
                                            }
                                        }
                                    }
                                }
                                // If token not found in both collections
                                Log.e(TAG, "Chat partner document does not exist for username: " + chatPartnerUsername);
                                callback.onTokenReceived(null);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error retrieving from customers collection: ", e);
                                callback.onTokenReceived(null);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving from managers collection: ", e);
                    callback.onTokenReceived(null);
                });
    }





    private void sendNotification(String accessToken, String token, String messageText) {
        // Construct notification payload
        JSONObject payload = new JSONObject();
        try {
            JSONObject message = new JSONObject();
            message.put("token", token);
            JSONObject notification = new JSONObject();
            notification.put("title", "New message from " + currentUserUsername); // Add current username
            notification.put("body", messageText);
            message.put("notification", notification);
            payload.put("message", message);
        } catch (JSONException e) {
            Log.e(TAG, "Error constructing JSON payload: ", e);
        }

        // Create a request to send the notification
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(FCM_SERVER_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Notification sending failed: ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Notification response failed: " + response.code() + " " + response.message());
                } else {
                    Log.d(TAG, "Notification sent successfully.");
                }
            }
        });
    }

    private void showNotification(ChatMessage message) {
        // Create a notification to alert the user
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Replace with your notification icon
                .setContentTitle("New message from " + message.getSender())
                .setContentText(message.getMessageText())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("selectedUserEmail", chatPartnerEmail);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    interface OnTokenReceived {
        void onTokenReceived(String token);
    }
}
