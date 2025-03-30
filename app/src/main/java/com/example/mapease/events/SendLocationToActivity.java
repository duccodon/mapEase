package com.example.mapease.events;

import com.google.android.gms.maps.model.LatLng;

public class SendLocationToActivity {
    private LatLng origin;
    private LatLng destination;
    private String originName;
    private String destinationName;

    public SendLocationToActivity(LatLng origin, LatLng destination, String originName, String destinationName) {
        this.origin = origin;
        this.destination = destination;
        this.originName = originName;
        this.destinationName = destinationName;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setOrigin(LatLng origin, String originName) {
        this.origin = origin;
        this.originName = originName;
    }

    public void setDestination(LatLng destination, String destinationName) {
        this.destination = destination;
        this.destinationName = destinationName;
    }

    public String getOriginString() {
        return origin.latitude + "," + origin.longitude;
    }

    public String getDestinationString() {
        return destination.latitude + "," + destination.longitude;
    }

    public String getOriginName() {
        return originName;
    }

    public String getDestinationName() {
        return destinationName;
    }
}
