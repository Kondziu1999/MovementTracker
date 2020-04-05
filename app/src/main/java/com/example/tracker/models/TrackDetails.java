package com.example.tracker.models;

public class TrackDetails {

    private String date;
    private Double distance;

    public TrackDetails() {
    }

    public TrackDetails(String date, Double distance) {
        this.date = date;
        this.distance = distance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
