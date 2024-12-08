package com.example.smartinventory;

public class InventoryItem {
    private String trackId;
    private String upc;
    private String productName;
    private String quantity;
    private String date;
    private String note;
    private String deductedAmount; // New field for deducted amount
    private String timestamp; // New field for timestamp

    public InventoryItem() {}  // Default constructor required for Firestore serialization

    public InventoryItem(String trackId, String upc, String productName, String quantity, String date, String note, String deductedAmount, String timestamp) {
        this.trackId = trackId;
        this.upc = upc;
        this.productName = productName;
        this.quantity = quantity;
        this.date = date;
        this.note = note;
        this.deductedAmount = deductedAmount; // Initialize deducted amount
        this.timestamp = timestamp; // Initialize timestamp
    }

    public InventoryItem(String trackId, String upc, String productName, String quantity, String date, String note) {
        this.trackId = trackId;
        this.upc = upc;
        this.productName = productName;
        this.quantity = quantity;
        this.date = date;
        this.note = note;
    }

    // Getters and setters
    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // Getters and setters for new fields
    public String getDeductedAmount() {
        return deductedAmount;
    }

    public void setDeductedAmount(String deductedAmount) {
        this.deductedAmount = deductedAmount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
