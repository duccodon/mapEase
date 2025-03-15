package com.example.mapease.Remote;

import com.google.android.gms.maps.model.LatLng;

public class RouteRequest {
    private OriginDestination origin;
    private OriginDestination destination;


    public RouteRequest(LatLng origin, LatLng destination) {
        this.origin = new OriginDestination(origin);
        this.destination = new OriginDestination(destination);

    }
    private static class OriginDestination {
        private Location location;

        public OriginDestination(LatLng latLng) {
            this.location = new Location(latLng);
        }
    }

    private static class Location {
        private LatLng latLng;

        public Location(LatLng latLng) {
            this.latLng = latLng;
        }
    }
}
