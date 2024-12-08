package com.example.smartinventory;

public class PackageItem {
    private String trackingId;

    public PackageItem() {}

    public PackageItem(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getTrackingId() {
        return trackingId;
    }
}
