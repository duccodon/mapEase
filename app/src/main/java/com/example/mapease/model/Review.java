package com.example.mapease.model;

import java.util.List;
import java.util.Map;

public class Review {
    private String reviewId;
    private String userID;
    private String locationID;
    private float rating;
    private String createAt;
    private String content;
    private List<String> imageUrls;
    private Map<String, Boolean> likes;


    public Review(String reviewId, String userID, String locationID, String content, float rating, String createAt,
                   List<String> imageUrls, Map<String, Boolean> likes) {
        this.reviewId = reviewId;
        this.userID = userID;
        this.locationID = locationID;
        this.rating = rating;
        this.createAt = createAt;
        this.content = content;
        this.imageUrls = imageUrls;
        this.likes = likes;
    }

    public Review(){
    }

    public String getReviewId() {
        return reviewId;
    }

    public String getUserID() {
        return userID;
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

    public String getContent() {
        return content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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

    public void setContent(String content) {
        this.content = content;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }
}
