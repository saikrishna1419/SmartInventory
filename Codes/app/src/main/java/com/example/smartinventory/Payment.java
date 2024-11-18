package com.example.smartinventory;

public class Payment {
    private String username;
    private String trackingId;
    private String last4Digits; // Last 4 digits of the card number
    private double amount;
    private String date; // Payment date/time
    private int numberOfDays; // Number of days for storage

    public Payment() {
    }
    // Constructor
    public Payment(String username, String trackingId, String last4Digits, double amount, String date, int numberOfDays) {
        this.username = username;
        this.trackingId = trackingId;
        this.last4Digits = last4Digits; // Store the last 4 digits
        this.amount = amount;
        this.date = date;
        this.numberOfDays = numberOfDays;
    }
    public Payment(String username, String trackingId, double amount, String date, int numberOfDays) {
        this.username = username;
        this.trackingId = trackingId;
        this.amount = amount;
        this.date = date;
        this.numberOfDays = numberOfDays;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getLast4Digits() {
        return last4Digits;
    }

    public void setLast4Digits(String last4Digits) {
        this.last4Digits = last4Digits;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }
}
