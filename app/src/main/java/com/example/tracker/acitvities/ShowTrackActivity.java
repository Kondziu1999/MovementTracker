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
import com.example.tracker.services.DistanceCalculator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

public class ShowTrackActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnMarkerClickListener

{
    //syf do modelowania
    private static final int POLYLINE_STROKE_WIDTH_PX = 8;
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
    private List<String> TRACK_IDS=new ArrayList<>();
    private List<List<LatLanHolder>> TRACKS_TO_PLOT=new ArrayList<>();
    private Integer TRACKS_TO_PLOT_COUNT=0;
    //color to style polyline
    private int colorCount=0;
    private List<Marker> markers=new ArrayList<>(20);

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_track);

        //SINGLE TRACK obtain track Id
        if(getIntent().hasExtra(getString(R.string.TRACK_ID))){
            trackId=getIntent().getStringExtra(getString(R.string.TRACK_ID));
            TRACK_IDS.add(trackId);
        }
        //MULTIPLE TRACK
        if(getIntent().hasExtra(getString(R.string.MULTIPLE_TRACK_ID))){
            Bundle bundle=this.getIntent().getExtras();
            if(bundle!=null){
                TRACK_IDS=bundle.getStringArrayList(getString(R.string.MULTIPLE_TRACK_ID));
            }
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
        getMultipleTracksLocations();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //add points to polyline
        //TODO consider abstract it to another method for visibility
        TRACKS_TO_PLOT.forEach(track->{
            PolylineOptions polylineOptions=new PolylineOptions()
                    .clickable(true);
            track
                .forEach(location-> {
                    LatLng position=new LatLng(location.getLat(),location.getLan());
                    polylineOptions.add(position);
                    Marker marker=mMap.addMarker(
                            new MarkerOptions().position(position).title("extra point :) \n lat: "+position.latitude+" \n lon :"+position.longitude)
                    );
                    markers.add(marker);
                });
        polyline=googleMap.addPolyline(polylineOptions);
        polyline.setTag("A");
        stylePolyline(polyline);
        });
        //move camera to center of markers when map will initialize
        googleMap.setOnMapLoadedCallback(() -> googleMap.moveCamera(DistanceCalculator.setCenter(markers)));

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
        polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        polyline.setEndCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow),16));
        polyline.setJointType(JointType.BEVEL);
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(getRandomColor());

        polyline.setJointType(JointType.ROUND);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        if(mMap!=null){
            //get last plot
            List<LatLanHolder> holder=TRACKS_TO_PLOT.get(TRACKS_TO_PLOT.size()-1);
            if(holder.size()>0){
                LAST_CAPTURED_LATITUDE=holder.get(holder.size()-1).getLat();
                LAST_CAPTURED_LONGITUDE=holder.get(holder.size()-1).getLan();
            }
            mMap.clear();
            onMapReady(mMap);
        }
    }

    //if there is multiple TrackLocations to plot
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getMultipleTracksLocations(){
        //create List of list locations
        TRACK_IDS.forEach(trackId1 -> getTrackLocations(trackId1,TRACKS_TO_PLOT_COUNT));
    }
    //single Track plot
    private void getTrackLocations(String trackId,int PlotNr){
        List<LatLanHolder> locationsForTrack= new ArrayList<>();
        TRACKS_TO_PLOT.add(locationsForTrack);
        TRACKS_TO_PLOT_COUNT++;

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
                   // locations.add(sender.get(i));
                    locationsForTrack.add(sender.get(i));
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

    private int getRandomColor(){
        Integer [] xd={Color.RED,Color.BLACK,Color.YELLOW,Color.GREEN,
                Color.rgb(255,0,255),Color.BLUE,Color.rgb(0,255,255)};
        if(colorCount>xd.length-1) {
            colorCount = 0;
        }

        return xd[colorCount++];
    }

}
