package com.example.smartinventory;

public class User {
    private String contact;
    private String email;
    private String userName;
    private String userType;
    private long lastMessageTimestamp; // New field for sorting by recent messages
    private boolean hasUnreadMessages;

    // No-argument constructor required for Firestore
    public User() {
    }

    public User(String contact, String email, String userName, String userType) {
        this.contact = contact;
        this.email = email;
        this.userName = userName;
        this.userType = userType;
        this.lastMessageTimestamp = 0; // Initialize to 0 or any default value if needed
        this.hasUnreadMessages = false;
    }

    // Getters
    public String getContact() {
        return contact;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserType() {
        return userType;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    // Setters
    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    public void setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }
}
