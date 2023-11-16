package dte.masteriot.mdp.travelinfo;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DATASET";

    // App-specific dataset:
    //private ArrayList<String> monumentNameList = createTestMonumentList();
    String xmlText;
    private ArrayList<String> monumentNameList = createTestMonumentList();
    ExecutorService es;
    MyOnItemActivatedListener onItemActivatedListener;
    private Dataset dataset;

    private RecyclerView recyclerView;
    private SelectionTracker<Long> tracker;
    private static final String URL_XML_MONUMENTS = "https://www.zaragoza.es/sede/servicio/monumento.xml";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setupToolbar();

        // Create an executor for the background tasks:
        es = Executors.newSingleThreadExecutor();

        // Execute the loading task in background:
        Log.d("Initial Parse", "Going to launch background thread...");

        LoadURLContent loadURLContents = new LoadURLContent(handler_initialXMLparce, URL_XML_MONUMENTS);
        es.execute(loadURLContents);

        // Prepare the RecyclerView:
        recyclerView = findViewById(R.id.recyclerView);
        dataset = new Dataset(monumentNameList);
        MyAdapter recyclerViewAdapter = new MyAdapter(dataset);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Choose the layout manager to be set.
        // some options for the layout manager:  GridLayoutManager, LinearLayoutManager, StaggeredGridLayoutManager
        // by default, a linear layout is chosen:
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Selection tracker (to allow for selection of items):
        onItemActivatedListener = new MyOnItemActivatedListener(this, dataset);
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                recyclerView,
                new MyItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED, recyclerView),
//                new StableIdKeyProvider(recyclerView), // This caused the app to crash on long clicks
                new MyItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(onItemActivatedListener)
                .build();
        recyclerViewAdapter.setSelectionTracker(tracker);

        if (savedInstanceState != null) {
            // Restore state related to selections previously made
            tracker.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        tracker.onSaveInstanceState(outState); // Save state about selections.
    }

    private ArrayList createTestMonumentList() {
        ArrayList<String> monumentNamesList = new ArrayList<>();
        monumentNamesList.add("Loading monument list...");
        return monumentNamesList;
    }

    Handler handler_initialXMLparce = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d("Initial Parse", "Entered fun: handler_initialXMLparce.handleMessage");
            // message received from background thread: load complete (or failure)
            ArrayList<String> monumentNamesList_aux;
            String string_msg;

            super.handleMessage(msg);
            monumentNamesList_aux = msg.getData().getStringArrayList("monuments");
            if (monumentNamesList_aux != null){
                monumentNameList = monumentNamesList_aux;
                for (int i = 0; i<monumentNameList.size(); i++){
                    Log.d("Initial Parse", monumentNameList.get(i));
                }
            }

            if((string_msg = msg.getData().getString("text")) != null) {
                xmlText = string_msg;
                Log.d("Initial Parse", xmlText);
            }
            // change dataset
            dataset.setNewData(monumentNameList);
            recyclerView.getAdapter().notifyDataSetChanged();
            onItemActivatedListener.set_XML_text(xmlText);

        }
    };

}