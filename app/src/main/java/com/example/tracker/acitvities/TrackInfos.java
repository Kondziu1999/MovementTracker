package com.example.tracker.acitvities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tracker.R;
import com.example.tracker.adapters.TrackInfoAdapter;
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
    //fetched tracks Ids
    public static List<String> trackIds;
    TrackInfoAdapter adapter;
    Context context;
    //static public items in order to avoid creating instances of this class
    public static List<String> tracksSelections;
    public static ActionMode actionMode;
    public static boolean ifActionMode=false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_infos);


        tracksSelections=new ArrayList<>(20);
        trackIds= new ArrayList<>(20);
        listView=(ListView)findViewById(R.id.trackLocations);
        dataService=FirebaseDataService.getInstance();
        context=getApplicationContext();

        getTracksList();
    }

//    private void getTrackIds(){
//        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,trackIds);
//        dataService.getLocationTracks(adapter,trackIds);
//
//        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String selectedItem=(String) parent.getItemAtPosition(position);
//
//                Toast.makeText(context,"selected track: "+selectedItem,Toast.LENGTH_LONG).show();
//
//                //show all track on separate map
//                //pass TrackId via intent's extra
//                Intent intent=new Intent(context,ShowTrackActivity.class);
//                intent.putExtra(getString(R.string.TRACK_ID),trackIds.get(position));
//                startActivity(intent);
//            }
//        });
//    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getTracksList(){
        TrackInfoAdapter adapter=new TrackInfoAdapter(this,trackIds);
        dataService.getLocationTracks(adapter,trackIds);

        listView.setAdapter(adapter);
        //to allow multiChoice (checkboxes)
        //mode listener below
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(modeListener);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem=(String) parent.getItemAtPosition(position);

                Toast.makeText(context,"selected track: "+selectedItem,Toast.LENGTH_LONG).show();

                //show all track on separate map
                //pass TrackId via intent's extra
                Intent intent=new Intent(context,ShowTrackActivity.class);
                intent.putExtra(getString(R.string.TRACK_ID),trackIds.get(position));
                startActivity(intent);
            }
        });

    }

    AbsListView.MultiChoiceModeListener modeListener=new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            //if selected track is on the list remove otherwise add to selection
//            if(tracksSelections.contains(trackIds.get(position))){
//                tracksSelections.remove(trackIds.get(position));
//            }
//            else{
//                tracksSelections.add(trackIds.get(position));
//            }
//            mode.setTitle(tracksSelections.size()+"  tracks selected...");
        }
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater=mode.getMenuInflater();
            inflater.inflate(R.menu.acions_menu,menu);
            actionMode=mode;
            ifActionMode=true;
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()){
                case(R.id.action_compare):
                    compareTracks();
                    return true;

                default:
                    return false;

            }
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            ifActionMode=false;
            actionMode=null;
            tracksSelections.clear();
        }
    };

    private void compareTracks(){
//        String[] selections=new String[tracksSelections.size()];
//        tracksSelections.toArray(selections);
        Bundle bundle=new Bundle();
        bundle.putStringArrayList(getString(R.string.MULTIPLE_TRACK_ID), (ArrayList<String>) tracksSelections);
        Intent intent=new Intent(context,ShowTrackActivity.class);
        intent.putExtras(bundle);

        startActivity(intent);
    }
}
