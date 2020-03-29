package com.example.tracker.database;

import androidx.annotation.NonNull;

import com.example.tracker.models.LatLanHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseDataService {

    private DatabaseReference databaseReference;
    private long CURRENT_TRACK_ID=0;

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
    public FirebaseDataService() {
        //tracks is first of node in db
        this.databaseReference= FirebaseDatabase.getInstance().getReference().child("TRACKS");
        //set value event listener
        this.databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //set maxId to the last id of children
                    CURRENT_TRACK_ID=dataSnapshot.getChildrenCount();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void addLocationToCurrentTrack(long sampleNr,long trackId,LatLanHolder location){
        // +1 makes track id unique :)
        databaseReference.child(String.valueOf(trackId)).child(String.valueOf(sampleNr)).setValue(location);
    }

    //returns track id for particular track
    public long getTrackId(){
        return CURRENT_TRACK_ID+1;
    }

}
