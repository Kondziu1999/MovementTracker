package com.example.tracker.services;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.tracker.models.LatLanHolder;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class DistanceCalculator
{
    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static CameraUpdate setCenter(List<Marker> markers){
        if(markers.size()==0){
            return CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0),20);
        }
        LatLngBounds.Builder builder=new LatLngBounds.Builder();

        markers.forEach(marker -> builder.include(marker.getPosition()));
        int padding=4;
        LatLngBounds bounds=builder.build();
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngBounds(bounds,padding);
        return cameraUpdate;
    }
}