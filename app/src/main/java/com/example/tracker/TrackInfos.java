package com.example.tracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tracker.database.FirebaseDataService;
import com.example.tracker.models.LatLanHolder;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TrackInfos extends AppCompatActivity {
    ListView listView;
    FirebaseDataService dataService;
    List<String> trackIds;
    Context context;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_infos);

        trackIds= new ArrayList<>(20);
        listView=(ListView)findViewById(R.id.trackLocations);
        dataService=FirebaseDataService.getInstance();
        context=getApplicationContext();

        getTrackIds();
    }

//    @RequiresApi(api = Build.VERSION_CODES.N)
//    private void getInfos()  {
//
//        StringBuilder builder=new StringBuilder();
//
//        ArrayList<LatLanHolder> locations=new ArrayList<>();
//
//        ArrayAdapter<LatLanHolder> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,locations);
//        dataService.getLocationForTrackSynchronized(1,locations,adapter);
//
//        listView.setAdapter(adapter);
//        builder.append("lokalizacje : \n");
//        locations.forEach(location -> {
//            builder.append(location.getLat())
//                    .append("      ")
//                    .append(location.getLan())
//                    .append("\n");
//        });
//
//
//    }
    private void getTrackIds(){
        StringBuilder builder= new StringBuilder();


        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,trackIds);
        dataService.getLocationTracks(adapter,trackIds);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem=(String) parent.getItemAtPosition(position);

                Toast.makeText(context,"selected track: "+selectedItem,Toast.LENGTH_LONG).show();

                //Intent intent=new Intent(context,)
            }
        });

    }

}
