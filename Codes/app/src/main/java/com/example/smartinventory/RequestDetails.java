package com.example.smartinventory;

public class RequestDetails {
    private String productName;
    private String upc;
    private int quantity;
    private String labelNumber;
    private String address;
    private String username;
    private String pincode;
    private String state;
    private String country;
    private String status;

    // No-argument constructor required for Firestore
    public RequestDetails() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RequestDetails(String productName, String upc, int quantity, String labelNumber,
                          String address, String username, String pincode, String state, String country, String status) {
        this.productName = productName;
        this.upc = upc;
        this.quantity = quantity;
        this.labelNumber = labelNumber;
        this.address = address;
        this.username = username;
        this.pincode = pincode;
        this.state = state;
        this.country = country;
        this.status = status;

    }

    // Getters and setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUpc() { return upc; }
    public void setUpc(String upc) { this.upc = upc; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getLabelNumber() { return labelNumber; }
    public void setLabelNumber(String labelNumber) { this.labelNumber = labelNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}

