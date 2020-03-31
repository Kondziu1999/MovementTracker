//package com.example.tracker.acitvities;
//
//import androidx.fragment.app.FragmentActivity;
//
//import android.graphics.Color;
//import android.os.Bundle;
//
//import com.example.tracker.R;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.JointType;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.Polyline;
//import com.google.android.gms.maps.model.PolylineOptions;
//import com.google.android.gms.maps.model.RoundCap;
//
//public class ShowTrackActivity extends FragmentActivity implements OnMapReadyCallback {
//
//    private GoogleMap mMap;
//    private static final int POLYLINE_STROKE_WIDTH_PX = 3;
//    private static final int PATTERN_DASH_LENGTH_PX = 2;
//    private static final int PATTERN_GAP_LENGTH_PX = 2;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_show_track);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//    }
//
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        PolylineOptions polylineOptions=new PolylineOptions()
//                .clickable(true);
//        //add points to polyline
//        locations
//                .forEach(location-> {
//                    LatLng position=new LatLng(location.getLat(),location.getLan());
//                    polylineOptions.add(position);
//                    mMap.addMarker(
//                            new MarkerOptions().position(position).title("extra point :) \n lat: "+position.latitude+" \n lon :"+position.longitude)
//                    );
//                });
//        polyline=googleMap.addPolyline(polylineOptions);
//        polyline.setTag("A");
//        // Style the polyline.
//        stylePolyline(polyline);
//        //move camera to last position of user
//        //TODO change setting camera view to capture whole points area
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LAST_CAPTURED_LATITUDE, LAST_CAPTURED_LONGITUDE),20));
//        googleMap.setOnPolylineClickListener(this);
//        googleMap.setOnMarkerClickListener(this);
//    }
//    private void stylePolyline(Polyline polyline) {
//        String type = "";
//        // Get the data object stored with the polyline.
//        if (polyline.getTag() != null) {
//            type = polyline.getTag().toString();
//        }
//        switch (type) {
//            // If no type is given, allow the API to use the default.
//            case "A":
//                // Use a custom bitmap as the cap at the start of the line.
//                polyline.setStartCap(new RoundCap());
//                break;
//            case "B":
//                // Use a round cap at the start of the line.
//                polyline.setStartCap(new RoundCap());
//                break;
//        }
//
//        polyline.setEndCap(new RoundCap());
//        polyline.setJointType(JointType.BEVEL);
//        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
//        polyline.setColor(Color.RED);
//        polyline.setJointType(JointType.ROUND);
//    }
//}
