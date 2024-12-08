package com.example.smartinventory;

public class WarehouseItem {
    private String productName;
    private String quantity;
    private String upc;

    // Default constructor required for Firestore
    public WarehouseItem() {}

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

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }
}
