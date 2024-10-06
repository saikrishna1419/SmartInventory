package com.example.smartinventory;

public class ChatMessage {
    private String sender;
    private String messageText;
    private long timestamp;
    private boolean isRead;

    // Empty constructor needed for Firebase deserialization
    public ChatMessage() {}

    public ChatMessage(String sender, String messageText, long timestamp, boolean isRead) {
        this.sender = sender;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
