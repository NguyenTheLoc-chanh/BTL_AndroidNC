package com.example.btl_androidnc;

public class Collector {
    private String name;
    private String phone;
    private String address;
    private String birthYear;
    private String imageUrl;
    // Các thuộc tính hiện có
    private boolean isSelected = false;
    public Collector() {
        // Firestore cần constructor rỗng
    }

    public Collector(String name, String phone, String imageUrl, String address, String birthYear) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.birthYear = birthYear;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

