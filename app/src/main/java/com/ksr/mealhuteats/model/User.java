package com.ksr.mealhuteats.model;

public class User {
    private String uuid;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String image;

    public User() {
    }

    public User(String fullName, String email, String phone, String address, String image) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.image = image;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


}
