package com.example.tracker.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import com.example.tracker.R;
import com.example.tracker.acitvities.TrackInfos;
import com.example.tracker.models.LatLanHolder;
import com.example.tracker.models.TrackDetails;

import java.util.ArrayList;
import java.util.List;

public class TrackInfoAdapter extends ArrayAdapter<String> {
    private List<String> tracks;
    private List<Boolean> ifTrackSelected;
    private Context context;
    private static TrackDetails defaultDetails=new TrackDetails("no date specified",0.0);

    @RequiresApi(api = Build.VERSION_CODES.N)
    public TrackInfoAdapter(@NonNull Context context, List<String> tracks) {
        super(context, R.layout.choose_track_layout,tracks);
        this.context=context;
        this.tracks = tracks;
        ifTrackSelected=new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //if there is any data approaching from firebase initialize check to false
        if(ifTrackSelected.size()!=tracks.size()){
            for(int i=ifTrackSelected.size(); i<tracks.size(); i++){
                ifTrackSelected.add(false);
            }
        }
        View row=convertView;
        if(row==null){
            row=LayoutInflater.from(context).inflate(R.layout.choose_track_layout,parent,false);
        }

        //initialize text
        //TODO add to text info about date and maybe city ?
        TextView track=row.findViewById(R.id.track_item);
        CheckBox checkBox=row.findViewById(R.id.track_checkbox);
        if(TrackInfos.ifActionMode){
            checkBox.setVisibility(View.VISIBLE);
        }else {
            checkBox.setVisibility(View.INVISIBLE);
        }


        //track.setText(tracks.get(position));
        track.setText(prepareText(tracks.get(position)));
        checkBox.setChecked(ifTrackSelected.get(position));
        checkBox.setTag(position);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos= (int) checkBox.getTag();
                if(ifTrackSelected.get(pos)){
                    //uncheck it
                    ifTrackSelected.set(pos,false);
                    //remove from selected
                    TrackInfos.tracksSelections.remove(tracks.get(pos));
                    TrackInfos.actionMode.setTitle(TrackInfos.tracksSelections.size()+ "tracks selected...");
                }
                else {
                    //check it
                    ifTrackSelected.set(pos,true);
                    //add to selected
                    TrackInfos.tracksSelections.add(tracks.get(pos));
                    TrackInfos.actionMode.setTitle(TrackInfos.tracksSelections.size()+ "tracks selected...");
                }
            }
        });

        return row;
    }

    public List<Boolean> getIfTrackSelected() {
        return ifTrackSelected;
    }

    public void setIfTrackSelected(List<Boolean> ifTrackSelected) {
        this.ifTrackSelected = ifTrackSelected;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private String prepareText(String trackId){
        StringBuilder builder=new StringBuilder();
        builder.append("ID:  ")
                .append(trackId);
        if(TrackInfos.trackDetailsMap!=null){
            TrackDetails details=TrackInfos.trackDetailsMap.getOrDefault(trackId,defaultDetails);
            builder.append("    ")
                    .append(details.getDate())
                    .append("    ")
                    .append(details.getDistance())
                    .append(" km");
        }

        return builder.toString();
    }
}
