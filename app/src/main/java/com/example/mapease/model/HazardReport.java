package com.example.mapease.model;

public class HazardReport {
    private String hazardType;
    private String description;
    private double latitude;
    private double longitude;
    private String createdAt;
    private String reporterId;

    public HazardReport() {}

    public HazardReport(String hazardType, String description, double latitude, double longitude, String createdAt, String reporterId) {
        this.hazardType = hazardType;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
        this.reporterId = reporterId;
    }

    // Getters and setters
    public String getHazardType() { return hazardType; }
    public void setHazardType(String hazardType) { this.hazardType = hazardType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }
}
