package com.example.tracker;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMarkerClickListener
{
    private static final int COLOR_RED_ARGB=0xff6550;
    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int POLYLINE_STROKE_WIDTH_PX = 3;
    private static final int PATTERN_DASH_LENGTH_PX = 2;
    private static final int PATTERN_GAP_LENGTH_PX = 2;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final double VALID_DISTANCE_BETWEEN_SAMPLES=0.007; //7m
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    private GoogleMap mMap;
    private static boolean ifLocationToAdd=false;
    private Polyline polyline;
    private LocationTrack locationTrack;

    private Handler handler;
    int delay=10000;
    double LAST_CAPTURED_LONGITUDE=20.760;
    double LAST_CAPTURED_LATITUDE= 50.840;
    private List<LatLanHolder> locations= new LinkedList<>();
    private static double TOTAL_DISTANCE=0;
    private TextView distanceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //locationTrack= new LocationTrack(this);
        distanceView=findViewById(R.id.distanceView);

        locations.add(new LatLanHolder(LAST_CAPTURED_LATITUDE,LAST_CAPTURED_LONGITUDE));
        locations.add(new LatLanHolder(LAST_CAPTURED_LATITUDE+0.008,LAST_CAPTURED_LONGITUDE+0.004));

        handler=new Handler();
        //run fetching user localization every 10 sec
        handler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void run() {
                    getLocation();
                    handler.postDelayed(this, delay);
                }
            }, delay);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        PolylineOptions polylineOptions=new PolylineOptions()
                .clickable(true);
        //add points to polyline
        locations
                .forEach(location-> {
                    LatLng position=new LatLng(location.getLat(),location.getLan());
                    polylineOptions.add(position);
                    mMap.addMarker(
                            new MarkerOptions().position(position).title("extra point :) \n lat: "+position.latitude+" \n lon :"+position.longitude)
                    );
                });

        polyline=googleMap.addPolyline(polylineOptions);
        polyline.setTag("A");
        // Style the polyline.
        stylePolyline(polyline);
        //move camera to last position of user
        //TODO change setting camera view to capture whole points area
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LAST_CAPTURED_LATITUDE, LAST_CAPTURED_LONGITUDE),20));
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnMarkerClickListener(this);

    }

    //refresh map
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        if(ifLocationToAdd && mMap!=null){
            mMap.clear();
            onMapReady(mMap);
            refreshDistance();
            ifLocationToAdd=false;
        }
    }

    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }
        switch (type) {
            // If no type is given, allow the API to use the default.
            case "A":
                // Use a custom bitmap as the cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                break;
            case "B":
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                break;
        }

        polyline.setEndCap(new RoundCap());
        polyline.setJointType(JointType.BEVEL);
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(Color.RED);
        polyline.setJointType(JointType.ROUND);
    }
    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }
        Toast.makeText(this, "Route type " + polyline.getTag().toString(),
                Toast.LENGTH_SHORT).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getLocation() {
        //create LocationTrack obj to get info
        //TODO check if LocationTrack can be singleton class
        locationTrack=new LocationTrack(this);
        if(locationTrack.canGetLocation()){
            //valid results
            //current values
            double lanCurr=locationTrack.getLongitude();
            double latCurr=locationTrack.getLatitude();
            double lanLast=locations.get(locations.size()-1).lan;
            double latLast=locations.get(locations.size()-1).lat;
            //if change of distance is grater than 10m add point
            double distance=DistanceCalculator.distance(latCurr,lanCurr,latLast,lanLast,"K");
            if(distance>VALID_DISTANCE_BETWEEN_SAMPLES){
                locations.add(new LatLanHolder(latCurr,lanCurr));
                ifLocationToAdd=true;
                TOTAL_DISTANCE+=distance;
                LAST_CAPTURED_LATITUDE=latCurr;
                LAST_CAPTURED_LONGITUDE=lanLast;
                onResume();
            }

            Toast.makeText(this,"lat : "+locationTrack.getLatitude()+" \n lan: "+locationTrack.getLongitude(),Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getPosition(View view) {
        getLocation();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this,marker.getTitle(),Toast.LENGTH_SHORT).show();
        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }
    private void refreshDistance(){
        String distanceSting=String.valueOf(TOTAL_DISTANCE);
        distanceView.setText(distanceSting.substring(0,5));
    }
}