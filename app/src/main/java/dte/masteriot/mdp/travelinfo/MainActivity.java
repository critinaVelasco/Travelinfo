package dte.masteriot.mdp.travelinfo;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
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

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.os.SystemClock;

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

    // MQTT
    final String mqttServerUri = "tcp://192.168.56.1:1883";
    String publishMessage = "Hello World!";
    MqttAndroidClient mqttAndroidClient;
    MqttConnectOptions mqttConnectOptions;
    Long tsLong = System.currentTimeMillis()/1000;
    String clientId = "client" + tsLong.toString();

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

        /*
        clientId = clientId + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), mqttServerUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    addToHistory("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic("aaa");
                } else {
                    addToHistory("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                addToHistory("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        addToHistory("Connecting to " + mqttServerUri + "...");
        mqttConnect(mqttConnectOptions);
         */
        mqtttest();

    }

    private void mqtttest() {
        String serverUri = "tcp://192.168.56.1:1883";
        clientId = clientId + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    addToHistory("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic("test");
                } else {
                    addToHistory("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                addToHistory("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        //mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setCleanSession(true);

        addToHistory("Connecting to " + serverUri + "...");
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic("test");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to connect to: " + serverUri +
                            ". Cause: " + ((exception.getCause() == null)?
                            exception.toString() : exception.getCause()));
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            addToHistory(e.toString());
        }
    }
    private void mqttConnect(MqttConnectOptions mqttConnectOptions) {
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic("aaa");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to connect to: " + mqttServerUri +
                            ". Cause: " + ((exception.getCause() == null)?
                            exception.toString() : exception.getCause()));
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            addToHistory(e.toString());
        }
    }

    private void mqttDisconnect() {
        try {
            mqttAndroidClient.disconnect();
            addToHistory("MQTT client disconnected");
        } catch (MqttException e) {
        e.printStackTrace();
        addToHistory(e.toString());
    }
    }
    private void addToHistory(String mainText) {
        Log.d("MQTT", "LOG: " + mainText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    public void subscribeToTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    addToHistory("Subscribed to: " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to subscribe");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            addToHistory(e.toString());
        }

    }

    public void subscribeToAll() {
        for (int i=0; i< dataset.getSize(); i++) {
            subscribeToTopic(dataset.getItemAtPosition(i).getTopic());
        }

    }

    public void publishMessage(String topic) {
        MqttMessage message = new MqttMessage();
        message.setPayload(publishMessage.getBytes());
        message.setRetained(false);
        message.setQos(0);
        try {
            mqttAndroidClient.publish(topic, message);
            addToHistory("Message Published");
        } catch (Exception e) {
            e.printStackTrace();
            addToHistory(e.toString());
        }
        if (!mqttAndroidClient.isConnected()) {
            addToHistory("Client not connected!");
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
            //mqttDisconnect();
            //mqttConnect(mqttConnectOptions);
            recyclerView.getAdapter().notifyDataSetChanged();
            onItemActivatedListener.set_XML_text(xmlText);

        }
    };

}