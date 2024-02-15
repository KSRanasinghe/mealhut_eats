package com.ksr.mealhuteats.model;

public class Cart {
    private String itemId;
    private String itemName;
    private int qty;
    private double itemPrice;
    private String itemImage;

    public Cart() {
    }

    public Cart(String itemId, String itemName, int qty, double itemPrice, String itemImage) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.qty = qty;
        this.itemPrice = itemPrice;
        this.itemImage = itemImage;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }
}
