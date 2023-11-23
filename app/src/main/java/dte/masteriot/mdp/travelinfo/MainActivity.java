package dte.masteriot.mdp.travelinfo;


import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

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

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private static final String TAG = "DATASET";

    // App-specific dataset:
    //private ArrayList<String> monumentNameList = createTestMonumentList();
    String xmlText;
    private ArrayList<String> monumentNameList = createTestMonumentList();
    ExecutorService es;
    MyOnItemActivatedListener onItemActivatedListener;
    private Dataset dataset;
    private Button searchMonument;
    private EditText editText;

    private RecyclerView recyclerView;
    private SelectionTracker<Long> tracker;
    private static final String URL_XML_MONUMENTS = "https://www.zaragoza.es/sede/servicio/monumento.xml";

    private Context mContext;

    private SensorManager sensorManager;
    private Sensor lightSensor;
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
        searchMonument = findViewById(R.id.buttonSearch);
        editText = findViewById(R.id.plain_text_input);

        // Get the reference to the sensor manager and the sensor:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

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

        searchMonument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textForSearch = editText.getText().toString();
                if (textForSearch == null){
                    // change dataset
                    dataset.setNewData(monumentNameList);
                    recyclerView.getAdapter().notifyDataSetChanged();
                    onItemActivatedListener.set_XML_text(xmlText);

                } else {
                    // change dataset
                    dataset.setNewData(containsSubstring( monumentNameList, textForSearch ));
                    recyclerView.getAdapter().notifyDataSetChanged();
                    onItemActivatedListener.set_XML_text(xmlText);
                }


            }
        });
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
            float lightValue = sensorEvent.values[0];
            if (lightValue < 10) {
                // Ambiente muy oscuro, ajustar a un valor bajo
                setBrightness(0.1f);
            } else if (lightValue < 100) {
                // Ambiente con poca luz, ajustar a un valor moderado
                setBrightness(0.5f);
            } else {
                // Ambiente bien iluminado, ajustar a un valor alto
                setBrightness(1.0f);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void setBrightness(float brightness) {
        // Verificar si el ajuste automático de brillo está habilitado y desactivarlo si es necesario
        try {
            int mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        // Ajustar el brillo de la pantalla
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = brightness;
        getWindow().setAttributes(layoutParams);
    }

    // Función para verificar si un String coincide con alguna parte de los elementos en ArrayList
    public static ArrayList<String> containsSubstring(ArrayList<String> list, String word) {
        ArrayList<String> searchList = new ArrayList<>();
        for (String element : list) {
            if (element.contains(word)) {
                searchList.add(element);

            }
        }
        return searchList;

    }

}