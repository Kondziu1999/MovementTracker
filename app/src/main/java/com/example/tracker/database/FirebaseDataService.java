package com.example.tracker.database;

import android.os.Build;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.ObservableList;

import com.example.tracker.adapters.TrackInfoAdapter;
import com.example.tracker.models.LatLanHolder;
import com.example.tracker.models.TrackDetails;
import com.example.tracker.services.LocalizationService;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class FirebaseDataService {

    //NOTE service acts like a bean (singleton)
    private DatabaseReference databaseReference;
    private DatabaseReference infoReference;
    private long CURRENT_TRACK_ID=0;
    public boolean ifTrackIdRefreshed=false;

    /**
     * DB STRUCTURE
     *TRACKS---
     *       |
     *       TRACK ID---
     *                  \
     *                   Location(LatLanHolder)---
     *                                           |
     *                                             LATITUDE
     *                                             LONGITUDE
     * **/

    //TODO app is designated for single user so in order to handle more simultaneously
    // logged users there is need to add UserId to prevent overriding data between users

    private static class SingletonHelper{
        private static final FirebaseDataService INSTANCE=new FirebaseDataService();
    }
    public static FirebaseDataService getInstance(){return SingletonHelper.INSTANCE;}

    private FirebaseDataService() {
        //tracks is first of node in db
        this.databaseReference= FirebaseDatabase.getInstance().getReference().child("TRACKS");
        this.infoReference=FirebaseDatabase.getInstance().getReference().child("INFO");
        //set value event listener
        ValueEventListener listener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //set maxId to the last id of children
                    CURRENT_TRACK_ID=dataSnapshot.getChildrenCount();
                    ifTrackIdRefreshed=true;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        databaseReference.addValueEventListener(listener);
    }

    public void addLocationToCurrentTrack(long sampleNr,long trackId,LatLanHolder location){
        // +1 makes track id unique :)
        databaseReference.child(String.valueOf(trackId)).child(String.valueOf(sampleNr)).setValue(location);
    }

    //returns track id for particular track
    public long getTrackId(){
        ifTrackIdRefreshed=false;
        return CURRENT_TRACK_ID+1;
    }

    public void  getLocationsForTrackId(String trackID, ObservableList<LatLanHolder> list){

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("TRACKS").child(trackID);
        ChildEventListener childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                LatLanHolder holder=dataSnapshot.getValue(LatLanHolder.class);
                list.add(holder);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        reference.addChildEventListener(childEventListener);

    }
    public List<String> getLocationTracks(ArrayAdapter<String> adapter,List<String> tracks){

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference()
                .child("TRACKS");

        //list of track ids
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    String trackId=postSnapshot.getKey();
                    tracks.add(String.valueOf(trackId));
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        return tracks;
    }

    public Map<String,TrackDetails> getTrackDetails(TrackInfoAdapter adapter){
        Map<String,TrackDetails> detailsMap=new HashMap<>();

        DatabaseReference infoReference=FirebaseDatabase.getInstance().getReference().child("INFO");
        infoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    TrackDetails details=postSnapshot.getValue(TrackDetails.class);
                    //put details into hash map id of node is equal to track Id from data section
                    detailsMap.put(postSnapshot.getKey(),details);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return detailsMap;
    }
    public void setDistanceForTrack(String trackId,Double distance){
        infoReference
                .child(trackId)
                .child("distance").setValue(LocalizationService.round(distance,3));
    }
    public void setDateForTrack(String trackId, Date date){
        String pattern="MM-dd-yyyy";
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(pattern, Locale.UK);
        String formattedDate=simpleDateFormat.format(date);
        infoReference
                .child(trackId)
                .child("date").setValue(formattedDate);
    }
}
