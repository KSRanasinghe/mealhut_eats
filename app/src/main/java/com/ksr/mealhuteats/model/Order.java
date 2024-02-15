package com.ksr.mealhuteats.model;

public class Order {

    private String id;
    private String datetime;
    private int totalItems;
    private double netTotal;
    private String status;

    public Order() {
    }

    public Order(String id, String datetime, int totalItems, double netTotal, String status) {
        this.id = id;
        this.datetime = datetime;
        this.totalItems = totalItems;
        this.netTotal = netTotal;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
