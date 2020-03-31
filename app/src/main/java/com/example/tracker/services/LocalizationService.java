package com.example.tracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.tracker.database.FirebaseDataService;
import com.example.tracker.models.LatLanHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocalizationService extends Service {
    //TODO change it do handle db
    private List<LatLanHolder> locations= new LinkedList<>();
    private FirebaseDataService database;
    private long TRACK_ID=0;
    private  long sampleCount=0;

    public LocalizationService() {
        this.database=FirebaseDataService.getInstance();
        //if id has not been refreshed wait
    }


    private Handler handler;
    private LocationTrack locationTrack;

    int delay=10000;
    private static final double VALID_DISTANCE_BETWEEN_SAMPLES=0.007; //7m
    private static double TOTAL_DISTANCE=0;
    private double LAST_DISTANCE=0;

    private final IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public LocalizationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocalizationService.this;
        }
    }

    public List<LatLanHolder> getLocations(){
        return this.locations;
    }
    public double getTotalDistance(){
        return round(TOTAL_DISTANCE,3);
    }
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        locations.clear();
        locationTrack.stopListener();
        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        handler=new Handler();
        locations=new ArrayList<>(100);
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> getLocation());
            }
        };
        //Starts after 20 sec and will repeat on every 20 sec of time interval.
        Timer timer=new Timer();
        timer.schedule(doAsynchronousTask, delay,delay);  // 20 sec timer
    }

    public void getLocation() {
        //create LocationTrack obj to get info
        //TODO check if LocationTrack can be singleton class
        locationTrack=new LocationTrack(this);
        if(locationTrack.canGetLocation()){
            //current values
            double lanCurr=locationTrack.getLongitude();
            double latCurr=locationTrack.getLatitude();
            //last values
            double lanLast=0.0;
            double latLast=0.0;
            if(locations.size()>0){
                lanLast=locations.get(locations.size()-1).getLan();
                latLast=locations.get(locations.size()-1).getLat();
            }
            if(validLocalization(latCurr,lanCurr,latLast,lanLast)){
                LatLanHolder currentLocalization=new LatLanHolder(latCurr,lanCurr);
                locations.add(currentLocalization);
                //increase sample nr and add to db
                ++sampleCount;
                initTrackId();
                database.addLocationToCurrentTrack(sampleCount,TRACK_ID,currentLocalization);
                TOTAL_DISTANCE+=LAST_DISTANCE;
            }
            //if there is only one point set distance to 0
            if(locations.size()==1){
                TOTAL_DISTANCE=0;
            }
            System.out.println("\n"+"super       "+latCurr+" "+lanCurr);
        }
    }

    private void initTrackId() {
        if(TRACK_ID==0){
            this.TRACK_ID=database.getTrackId();
        }
    }
    private boolean validLocalization(double latCurr, double lanCurr, double latLast, double lanLast){
        double distance= DistanceCalculator.distance(latCurr,lanCurr,latLast,lanLast,"K");
        if(distance > VALID_DISTANCE_BETWEEN_SAMPLES){
            LAST_DISTANCE=distance;
            return true;
        }
        return false;
    }
}
