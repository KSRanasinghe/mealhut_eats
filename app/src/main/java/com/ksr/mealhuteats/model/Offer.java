package com.ksr.mealhuteats.model;

public class Offer {

    private String id;
    private String start;
    private String end;
   private String image;

    public Offer() {
    }

    public Offer(String start, String end, String image) {
        this.start = start;
        this.end = end;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
