package com.example.smartinventory;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String status;  // Current status of the request
    private String pdfUrl;  // PDF download URL
    private Timestamp timestamp;  // Timestamp for when the request is submitted
    private List<Map<String, Object>> statusHistory;  // List to hold status changes

    // Constructor
    public RequestItem(String productName, String upc, String quantity, String username,
                       String labelNumber, String address, String pincode, String state,
                       String country, String pdfUrl, String status, Timestamp timestamp) {
        this.productName = productName;
        this.upc = upc;
        this.quantity = quantity;
        this.username = username;
        this.labelNumber = labelNumber;
        this.address = address;
        this.pincode = pincode;
        this.state = state;
        this.country = country;
        this.pdfUrl = pdfUrl;
        this.status = status;
        this.timestamp = timestamp;
        this.statusHistory = new ArrayList<>();  // Initialize status history
        addStatusToHistory(status, timestamp);  // Add initial status to history
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

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public List<Map<String, Object>> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<Map<String, Object>> statusHistory) {
        this.statusHistory = statusHistory;
    }

    // Method to add status to history
    public void addStatusToHistory(String status, Timestamp timestamp) {
        Map<String, Object> statusEntry = new HashMap<>();
        statusEntry.put("status", status);
        statusEntry.put("timestamp", timestamp);
        this.statusHistory.add(statusEntry);
    }

    // Getters and setters for all fields
    // ...
}
