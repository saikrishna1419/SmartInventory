package com.example.smartinventory;
public class InventoryItem {
    private String trackId;
    private String upc;
    private String productName;
    private int quantity;
    private String date;
    private String note;

    public InventoryItem() {}  // Default constructor required for Firestore serialization

    public InventoryItem(String trackId, String upc, String productName, int quantity, String date, String note) {
        this.trackId = trackId;
        this.upc = upc;
        this.productName = productName;
        this.quantity = quantity;
        this.date = date;
        this.note = note;
    }

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
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
}
