package com.example.btl_androidnc.Gift;

import com.google.firebase.firestore.PropertyName;

public class GiftLocation {
    @PropertyName("name")
    private String name;
    @PropertyName("address")
    private String address;
    @PropertyName("phone")
    private String phone;
    @PropertyName("operatingHours")
    private String operatingHours;
    @PropertyName("status")
    private String status;
    @PropertyName("imageUrl")
    private String imageUrl;

    public GiftLocation() {
        // Firestore cần constructor rỗng
    }

    public GiftLocation(String name, String address, String phone, String operatingHours, String status, String imageUrl) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.operatingHours = operatingHours;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("address")
    public String getAddress() {
        return address;
    }

    @PropertyName("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @PropertyName("phone")
    public String getPhone() {
        return phone;
    }

    @PropertyName("phone")
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @PropertyName("operatingHours")
    public String getOperatingHours() {
        return operatingHours;
    }

    @PropertyName("operatingHours")
    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}