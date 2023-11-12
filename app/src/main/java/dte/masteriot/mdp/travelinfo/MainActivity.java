package dte.masteriot.mdp.travelinfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    //private static final String URL_XML_MONUMENTS = "https://www.esmadrid.com/opendata/turismo_v1_en.xml";
    private static final String URL_XML_MONUMENTS = "https://www.zaragoza.es/sede/servicio/monumento.xml";
    String xmlText;
    private ArrayList<String> monumentNameList;
    ExecutorService es;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an executor for the background tasks:
        es = Executors.newSingleThreadExecutor();

        // Execute the loading task in background:
        LoadURLContent loadURLContents = new LoadURLContent(handler_initialXMLparce, URL_XML_MONUMENTS);
        es.execute(loadURLContents);

    }

        // Define the handler that will receive the messages from the background thread PARCEXML
        // :
    Handler handler_initialXMLparce = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            // message received from background thread: load complete (or failure)
            ArrayList<String> monumentNamesList_aux;
            String string_msg;

            super.handleMessage(msg);
            monumentNamesList_aux = msg.getData().getStringArrayList("monuments");
            if (monumentNamesList_aux != null){
                monumentNameList = monumentNamesList_aux;
                for (int i = 0; i<monumentNameList.size(); i++){
                    Log.d("Test", monumentNameList.get(i));
                }
            }

            if((string_msg = msg.getData().getString("text")) != null) {
                xmlText = string_msg;
                Log.d("Test", xmlText);
            }

        }
    };

}