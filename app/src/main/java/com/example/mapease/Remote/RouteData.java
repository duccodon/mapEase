package com.example.mapease.Remote;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class RouteData {
    private String duration;
    private String distance;
    private List<Step> steps;

    private List<LatLng> polylineList;

    public RouteData(String duration, String distance, List<Step> steps, List<LatLng> polylineList) {
        this.duration = duration;
        this.distance = distance;
        this.steps = steps;
        this.polylineList = polylineList;
    }

    public String getDuration() { return duration; }
    public String getDistance() { return distance; }
    public List<Step> getSteps() { return steps; }
    public List<LatLng> getPolylineList(){return polylineList;}
}
