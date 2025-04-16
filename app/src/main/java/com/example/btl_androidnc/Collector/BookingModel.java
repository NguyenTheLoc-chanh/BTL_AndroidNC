package com.example.btl_androidnc.Collector;

import java.util.List;
import java.util.Map;

public class BookingModel {
    private String id;
    private String name;
    private String phone;
    private String status;
    private String method;
    private String date;
    private String timeSlot;
    private String Image;
    private Map<String, Integer> scrapData;
    private List<String> scrapTypes;

    private String userId;
    private String userName;
    private String userPhone;
    private String userAddress;

    public BookingModel() {}

    public BookingModel(String id,String name, String phone, String userID, String userName, String userPhone, String userAddress, String status, String method, String date, String time, String Image,Map<String, Integer> scrapData,List<String> scrapTypes) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.method = method;
        this.date = date;
        this.timeSlot = time;
        this.Image = Image;
        this.scrapData = scrapData;
        this.scrapTypes = scrapTypes;
        this.userId = userID;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress =userAddress;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public String getMethod() {
        return method;
    }

    public String getDate() {
        return date;
    }
    public String getTimeSlot() {
        return timeSlot;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getImage() {
        return Image;
    }
    public Map<String, Integer> getScrapData() {
        return scrapData;
    }
    public List<String> getScrapTypes() {
        return scrapTypes;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
    public String getUserId() {
        return userId;
    }
}
