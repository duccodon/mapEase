package com.example.mapease.model;

import java.util.List;

public class ReportProblem {

    private  String reportID;

    private  String userID;

    private double locationLatitude;

    private double locationLongitude;

    private String placeName;

    private List<String> issueTypes;

    private String extraComments;

    private String createAt;

    public ReportProblem() {}

    public ReportProblem(String placeName, List<String> issueTypes, String extraComments, String createAt,
                         String reportID, String userID, double locationLatitude, double locationLongitude) {
        this.placeName = placeName;
        this.issueTypes = issueTypes;
        this.extraComments = extraComments;
        this.createAt = createAt;
        this.reportID = reportID;
        this.userID = userID;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
    }

    public String getPlaceName() { return placeName; }
    public String getIssueTypes() { return String.join("-", issueTypes); }

    public String getExtraComments() { return extraComments; }
    public String getCreateAt() { return createAt; }

    public String getReportID() { return reportID; }

    public String getUserID() { return userID; }

    public double getLocationLatitude() { return locationLatitude; }

    public  double getLocationLongitude() {return  locationLongitude; }

}