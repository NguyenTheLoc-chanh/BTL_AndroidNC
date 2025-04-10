package com.example.btl_androidnc;

public class GiftLocation {
    private String name;
    private String phone;
    private String operatingHours;
    private String status;
    private String imageUrl;

    public GiftLocation() {
        // Firestore cần constructor rỗng
    }

    public GiftLocation(String name, String phone, String operatingHours, String status, String imageUrl) {
        this.name = name;
        this.phone = phone;
        this.operatingHours = operatingHours;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public String getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}