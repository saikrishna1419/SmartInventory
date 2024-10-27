package com.example.smartinventory;

public class Payment {
    private String username;
    private String trackingId;
    private double amount;
    private String date; // Check-in date
    private int numberOfDays; // Number of days for storage

    public Payment(String username, String trackingId, double amount, String date, int numberOfDays) {
        this.username = username;
        this.trackingId = trackingId;
        this.amount = amount;
        this.date = date;
        this.numberOfDays = numberOfDays;
    }

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

