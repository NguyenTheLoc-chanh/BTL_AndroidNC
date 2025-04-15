package com.example.btl_androidnc.Collector;

public class Collector {
    private String id;
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

    public Collector(String id,String name, String phone, String imageUrl, String address, String birthYear) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.birthYear = birthYear;
        this.imageUrl = imageUrl;
        this.id = id;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {  // 👈 Thêm setter cho ID
        this.id = id;
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

