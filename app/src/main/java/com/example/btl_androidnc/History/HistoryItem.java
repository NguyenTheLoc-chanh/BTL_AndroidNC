package com.example.btl_androidnc.History;

public class HistoryItem {
    private String name;
    private String phone;
    private String status;
    private String method;
    private String date;
    private String time;

    public HistoryItem(String name, String phone, String status, String method, String date, String time) {
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.method = method;
        this.date = date;
        this.time = time;
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
}
