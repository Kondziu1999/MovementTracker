package com.example.tracker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.tracker.acitvities.TrackInfos;
import com.example.tracker.models.LatLanHolder;
import com.example.tracker.services.DistanceCalculator;
import com.example.tracker.services.LocalizationService;
import com.example.tracker.services.LocationTrack;
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
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnMarkerClickListener
{

    private static final int POLYLINE_STROKE_WIDTH_PX = 7;
    private static final int PATTERN_DASH_LENGTH_PX = 2;
    private static final int PATTERN_GAP_LENGTH_PX = 2;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    private boolean mapToCenter=true;

    private GoogleMap mMap;
    private Polyline polyline;

    int DELAY=10000;
    double LAST_CAPTURED_LONGITUDE=20.760;
    double LAST_CAPTURED_LATITUDE= 50.840;
    private List<LatLanHolder> locations= new LinkedList<>();
    private static double TOTAL_DISTANCE=0;
    private TextView distanceView;
    private LocalizationService mLocalizationService;
    private boolean mBound;
    private Handler refreshHandler;
    private List<Marker> markers=new ArrayList<>(20);

    public static Activity activity;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle toggle;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        activity=this;
        //navigation drawer (side menu)
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle=new ActionBarDrawerToggle(this,mDrawer,toolbar,R.string.OPEN,R.string.CLOSE);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        mDrawer.addDrawerListener(toggle);

        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        nvDrawer.inflateHeaderView(R.layout.nav_header);
        // Setup drawer view
        setNavigationViewListener(nvDrawer);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        distanceView=findViewById(R.id.distanceView);
        //start Localization service and bind it
        Intent intent= new Intent(this,LocalizationService.class);
       // startService(intent);
        startForegroundService(intent);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);

        setRefreshLocalizationTimeout(DELAY);

    }

    private void setNavigationViewListener(NavigationView nv){
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.show_tracks_icon:
                        startTrackInfos();
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });
    }

    private void setRefreshLocalizationTimeout(long timeout){
        refreshHandler=new Handler();
        refreshHandler.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                onResume();
                refreshHandler.postDelayed(this, 2000);
            }
        }, timeout);
    }
    //connection to service
    private ServiceConnection connection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalizationService.LocalBinder binder= (LocalizationService.LocalBinder) service;
            mLocalizationService=binder.getService();
            mBound=true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound=false;
        }
    };

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
                    Marker marker=mMap.addMarker(
                            new MarkerOptions().position(position).title("extra point :) \n lat: "+position.latitude+" \n lon :"+position.longitude)
                    );
                    markers.add(marker);
                });
        polyline=googleMap.addPolyline(polylineOptions);
        polyline.setTag("A");
        // Style the polyline.
        stylePolyline(polyline);
        //move camera to center of markers when map will initialize
        //first time center and then when user click center button
        if(mapToCenter){
            mapToCenter=false;
            googleMap.setOnMapLoadedCallback(() ->googleMap.moveCamera(DistanceCalculator.setCenter(markers)));
        }


        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnMarkerClickListener(this);
    }

    //refresh map
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        //TODO check if comparing size of list can be replace by Observable<?>
        if(mLocalizationService!=null && !mLocalizationService.getLocations().isEmpty()){
            //if something changed
            if(mLocalizationService.getLocations().size()>=locations.size()){
                locations=mLocalizationService.getLocations();
                LAST_CAPTURED_LATITUDE=locations.get(locations.size()-1).getLat();
                LAST_CAPTURED_LONGITUDE=locations.get(locations.size()-1).getLan();
                mMap.clear();
                onMapReady(mMap);
                refreshDistance();
            }
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
                polyline.setEndCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow),16));
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
    public void getPosition(View view) {
//        List<LatLanHolder> locationsInternal=mLocalizationService.getLocations();
//        LatLanHolder holder=locationsInternal.get(locationsInternal.size()-1);
//        locations=mLocalizationService.getLocations();
        Toast.makeText(this,LAST_CAPTURED_LATITUDE+" "+LAST_CAPTURED_LONGITUDE,Toast.LENGTH_LONG ).show();

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
        //String distanceSting=String.valueOf(TOTAL_DISTANCE);
        //distanceView.setText(distanceSting.substring(0,5));
        distanceView.setText(String.valueOf(mLocalizationService.getTotalDistance()));
    }

    public void goToDetails(View view) {
        Intent intent=new Intent(this, TrackInfos.class);
        startActivity(intent);
    }

    private void startTrackInfos(){
        Intent intent=new Intent(this, TrackInfos.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //collapse side bar if back pressed
        if(this.mDrawer.isDrawerOpen(GravityCompat.START)){
            this.mDrawer.closeDrawer(GravityCompat.START);
        }
        //if there is no side bar minimize app
        else {
            this.moveTaskToBack(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void centerMap(View view) {
        mapToCenter=true;
        onResume();
    }
}
