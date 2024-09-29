package com.example.smartinventory;

public class RequestItem {
    private String productName;
    private String upc;
    private String quantity;
    private String username;
    private String labelNumber;
    private String address;
    private String pincode;
    private String state;
    private String country;
    private String status; // New field for status

    public RequestItem(String productName, String upc, String quantity, String username,
                       String labelNumber, String address, String pincode, String state,
                       String country, String status) {
        this.productName = productName;
        this.upc = upc;
        this.quantity = quantity;
        this.username = username;
        this.labelNumber = labelNumber;
        this.address = address;
        this.pincode = pincode;
        this.state = state;
        this.country = country;
        this.status = status; // Initialize status
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLabelNumber() {
        return labelNumber;
    }

    public void setLabelNumber(String labelNumber) {
        this.labelNumber = labelNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
