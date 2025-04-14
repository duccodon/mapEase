package com.example.mapease.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Map;

public class favoriteLocation {
    private String favoriteId;
    private String userID;
    private String locationID;

    private double locationLatitude;
    private double locationLongitude;

    private String locationName;
    private String locationAddress;
    private String locationNotes;
    private String locationType;
    private String imageUrls;

    public favoriteLocation(String favoriteId, String userID, String locationID, LatLng lng, String locationName, String locationAddress, String locationNotes, String locationType, String createAt, String imageUrls) {
        this.favoriteId = favoriteId;
        this.userID = userID;
        this.locationID = locationID;
       this.locationLatitude = lng.latitude;
        this.locationLongitude = lng.longitude;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationNotes = locationNotes;
        this.locationType = locationType;

        this.imageUrls = imageUrls;
    }

    public favoriteLocation() {
    }

    public String getFavoriteId() {
        return favoriteId;
    }

    public String getUserID() {
        return userID;
    }

    public String getLocationID() {
        return locationID;
    }

    public double getLocationLatitude() {
        return locationLatitude;
    }

    public double getLocationLongitude() {
        return locationLongitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public String getLocationNotes() {
        return locationNotes;
    }

    public String getLocationType() {
        return locationType;
    }



    public String getImageUrls() {
        return imageUrls;
    }

    public void setFavoriteId(String favoriteId) {
        this.favoriteId = favoriteId;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public void setLocationLatitude(double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public void setLocationLongitude(double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public void setLocationNotes(String locationNotes) {
        this.locationNotes = locationNotes;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }


    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    @Override
    public String toString() {
        return "favoriteLocation{" +
                "favoriteId='" + favoriteId + '\'' +
                ", userID='" + userID + '\'' +
                ", locationID='" + locationID + '\'' +
                ", locationLatitude=" + locationLatitude + '\'' +
                ", locationLongitude=" + locationLongitude + '\'' +
                ", locationName='" + locationName + '\'' +
                ", locationAddress='" + locationAddress + '\'' +
                ", locationNotes='" + locationNotes + '\'' +
                ", locationType='" + locationType + '\'' +
                ", imageUrls=" + imageUrls +
                '}';
    }

}
