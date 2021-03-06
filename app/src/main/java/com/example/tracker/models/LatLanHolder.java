package com.example.tracker.models;

import androidx.annotation.NonNull;

public class LatLanHolder {

    double lat;
    double lan;
    public LatLanHolder() {
    }

    public LatLanHolder(double lat, double lan) {
        this.lat = lat;
        this.lan = lan;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLan() {
        return lan;
    }

    public void setLan(double lan) {
        this.lan = lan;
    }

    @NonNull
    @Override
    public String toString() {
        return "Latitude"+lat+"    Longitude"+lan;
    }
}
