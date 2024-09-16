package com.example.smartinventory;

public class ChatMessage {
    private String message;
    private String sender;
    private long timestamp;

    public ChatMessage() { } // Default constructor required for calls to DataSnapshot.getValue(ChatMessage.class)

    public ChatMessage(String message, String sender, long timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

