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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
    private String chatPartnerEmail;
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Create the Notification Channel (for Android O and above)
        createNotificationChannel();

        // Initialize Firestore and UI components
        firestore = FirebaseFirestore.getInstance();
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, getCurrentUserEmail());

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        currentUserEmail = getCurrentUserEmail();
        chatPartnerEmail = getIntent().getStringExtra("selectedUserEmail");

        Log.d(TAG, "Current User Email: " + currentUserEmail);
        Log.d(TAG, "Chat Partner Email: " + chatPartnerEmail);

        if (currentUserEmail == null || chatPartnerEmail == null) {
            Toast.makeText(this, "Error: User emails are null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        loadChatMessages();
    }

    private String getCurrentUserEmail() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } else {
            return null;
        }
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

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        ChatMessage message = new ChatMessage(currentUserEmail, messageText, System.currentTimeMillis(), false);

        firestore.collection("chats")
                .document(currentUserEmail + "_" + chatPartnerEmail)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Message sent successfully.");
                    messageEditText.setText("");

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

    private void getChatPartnerToken(OnTokenReceivedCallback callback) {
        // Try to fetch from "managers" first
        firestore.collection("managers")
                .whereEqualTo("email", chatPartnerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String token = task.getResult().getDocuments().get(0).getString("fcmToken");
                        callback.onTokenReceived(token);
                    } else {
                        // If not found in managers, fallback to "customers"
                        firestore.collection("customers")
                                .whereEqualTo("email", chatPartnerEmail)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                        String token = task2.getResult().getDocuments().get(0).getString("fcmToken");
                                        callback.onTokenReceived(token);
                                    } else {
                                        Log.e(TAG, "Chat partner document not found in either collection.");
                                        callback.onTokenReceived(null);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error fetching chat partner token from customers: ", e);
                                    callback.onTokenReceived(null);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching chat partner token from managers: ", e);
                    callback.onTokenReceived(null);
                });
    }

    private void sendNotification(String accessToken, String chatPartnerToken, String messageText) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("message", new JSONObject()
                    .put("token", chatPartnerToken)
                    .put("notification", new JSONObject()
                            .put("title", "New Message from " + currentUserEmail)
                            .put("body", messageText)));

            sendFCMNotification(accessToken, payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendFCMNotification(String accessToken, JSONObject payload) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(FCM_SERVER_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Notification sending failed: ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Notification sent successfully.");
                } else {
                    Log.e(TAG, "Notification failed: " + response.message());
                }
            }
        });
    }

    private void showNotification(ChatMessage message) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("selectedUserEmail", chatPartnerEmail); // Pass necessary data
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Create a PendingIntent for the notification click
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Replace with your notification icon
                .setContentTitle("New Message from " + message.getSender())
                .setContentText(message.getMessageText())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent) // Set the content intent
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Make it public
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message.getMessageText())
                        .setBigContentTitle("New Message from " + message.getSender()));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private interface OnTokenReceivedCallback {
        void onTokenReceived(String token);
    }
}
