package com.example.mapease.model;

import java.util.List;

public class Review {
    private String userID;
    private String locationID;
    private float rating;
    private String createAt;
    private String content;
    private List<String> imageUrls;
    private Integer likes;


    public Review(String userID, String content, String locationID, float rating, String createAt, Integer likes) {
        this.userID = userID;
        this.content = content;
        this.locationID = locationID;
        this.rating = rating;
        this.createAt = createAt;
        this.likes = likes;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getUserID() {
        return userID;
    }

    public String getContent() {
        return content;
    }

    public String getLocationID() {
        return locationID;
    }

    public float getRating() {
        return rating;
    }

    public String getCreateAt() {
        return createAt;
    }

    public Integer getLikes() {
        return likes;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
