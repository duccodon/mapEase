package com.example.mapease.model;

import java.util.List;

public class ReportProblem {

    private  String reportID;

    private  String userID;

    private String locationID;

    private double locationLatitude;

    private double locationLongitude;

    private String placeName;

    private String placeAddress;

    private List<String> issueTypes;

    private String extraComments;

    private String createAt;

    public ReportProblem() {}

    public ReportProblem(String placeName, String placeAddress, List<String> issueTypes, String extraComments, String createAt,
                         String reportID, String userID, String locationID, double locationLatitude, double locationLongitude) {
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.issueTypes = issueTypes;
        this.extraComments = extraComments;
        this.createAt = createAt;
        this.reportID = reportID;
        this.userID = userID;
        this.locationID = locationID;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
    }

    public String getPlaceName() { return placeName; }
    public String getPlaceAddress() { return placeAddress; }
    public List<String> getIssueTypes() { return issueTypes; }
    public String getExtraComments() { return extraComments; }
    public String getCreateAt() { return createAt; }

    public String getReportID() { return reportID; }

    public String getUserID() { return userID; }

    public String getLocationID() { return locationID; }

    public double getLocationLatitude() { return locationLatitude; }

    public  double getLocationLongitude() {return  locationLongitude; }

}
