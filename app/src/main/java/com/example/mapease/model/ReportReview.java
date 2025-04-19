package com.example.mapease.model;

public class ReportReview {
    private String Id;
    private String reporterId;
    private String reviewId;
    private String title;
    private String description;
    private String createdAt;
    private int state;

    public ReportReview() {
    }

    public ReportReview(String id, String reporterId, String reviewId, String title, String description, String createdAt) {
        Id = id;
        this.reporterId = reporterId;
        this.reviewId = reviewId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.state = 0;
    }

    public String getId() {
        return Id;
    }

    public String getReporterId() {
        return reporterId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public int getState(){ return state; }


    public void setId(String id) {
        Id = id;
    }
    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }
    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public void setState(int state) {this.state = state;}
}


