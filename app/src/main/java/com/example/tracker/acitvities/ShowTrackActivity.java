package com.example.tracker.acitvities;

import androidx.annotation.RequiresApi;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.example.tracker.R;
import com.example.tracker.database.FirebaseDataService;
import com.example.tracker.models.LatLanHolder;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class ShowTrackActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnMarkerClickListener

{
    //syf do modelowania
    private static final int POLYLINE_STROKE_WIDTH_PX = 3;
    private static final int PATTERN_DASH_LENGTH_PX = 2;
    private static final int PATTERN_GAP_LENGTH_PX = 2;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    private double LAST_CAPTURED_LONGITUDE=20.760;
    private double LAST_CAPTURED_LATITUDE= 50.840;
    private GoogleMap mMap;
    private List<LatLanHolder> locations=new LinkedList<>();
    private Polyline polyline;
    private String trackId;
    private FirebaseDataService dataService;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_track);
        //obtain track Id
        if(getIntent().hasExtra(getString(R.string.TRACK_ID))){
            trackId=getIntent().getStringExtra(getString(R.string.TRACK_ID));
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trackMap);
        mapFragment.getMapAsync(this);
        //obtain instance of dataService
        dataService=FirebaseDataService.getInstance();
        //setAppContext
        //TODO consider changing it
        context=getApplicationContext();
        getTrackLocations();
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
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LAST_CAPTURED_LATITUDE,LAST_CAPTURED_LONGITUDE),15));
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnMarkerClickListener(this);
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
            default:
                polyline.setStartCap(new RoundCap());
                break;
        }
        polyline.setEndCap(new RoundCap());
        polyline.setJointType(JointType.BEVEL);
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(Color.RED);
        polyline.setJointType(JointType.ROUND);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        if(mMap!=null){
            LAST_CAPTURED_LATITUDE=locations.get(locations.size()-1).getLat();
            LAST_CAPTURED_LONGITUDE=locations.get(locations.size()-1).getLan();
            onMapReady(mMap);
        }
    }

    private void getTrackLocations(){
        ObservableList<LatLanHolder> observableList=new ObservableArrayList<>();
        dataService.getLocationsForTrackId(trackId,observableList);
        observableList.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<LatLanHolder>>() {
            @Override
            public void onChanged(ObservableList<LatLanHolder> sender) {
            }
            @Override
            public void onItemRangeChanged(ObservableList<LatLanHolder> sender, int positionStart, int itemCount) {
            }
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemRangeInserted(ObservableList<LatLanHolder> sender, int positionStart, int itemCount) {
                for (int i=positionStart; i<sender.size(); i++){
                    locations.add(sender.get(i));
                }
                onResume();
            }
            @Override
            public void onItemRangeMoved(ObservableList<LatLanHolder> sender, int fromPosition, int toPosition, int itemCount) {
            }
            @Override
            public void onItemRangeRemoved(ObservableList<LatLanHolder> sender, int positionStart, int itemCount) {
            }
        });
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this,marker.getTitle(),Toast.LENGTH_SHORT).show();
        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
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

}
