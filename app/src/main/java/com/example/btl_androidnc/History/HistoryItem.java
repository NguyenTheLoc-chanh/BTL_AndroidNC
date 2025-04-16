package com.example.btl_androidnc.History;

import java.util.List;
import java.util.Map;

public class HistoryItem {
    private String id;
    private String name;
    private String phone;
    private String status;
    private String method;
    private String date;
    private String time;
    private String Image;
    private Map<String, Integer> scrapData;
    private List<String> scrapTypes;

    public HistoryItem() {}

    public HistoryItem(String id,String name, String phone, String status, String method, String date, String time, String Image,Map<String, Integer> scrapData,List<String> scrapTypes) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.method = method;
        this.date = date;
        this.time = time;
        this.Image = Image;
        this.scrapData = scrapData;
        this.scrapTypes = scrapTypes;
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
    public String getTime() {
        return time;
    }
    public String getId() {
        return id;
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
}
