package dte.masteriot.mdp.travelinfo;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.speech.tts.TextToSpeech;

//import com.google.cloud.translate.Translate;
//import com.google.cloud.translate.TranslateOptions;
//import com.google.cloud.translate.Translation;

public class InfoMonument extends AppCompatActivity implements TextToSpeech.OnInitListener, SensorEventListener {
    private static final String TAG = "DATASET";
    XmlPullParserFactory parserFactory;
    String xmlText;
    TextView appbar;
    TextView description;
    TextView url;
    String textDescription;
    ImageView imageView;
    LineChart chart;
    String imageUrl;
    TextView address;
    String textAddress;
    private TextToSpeech textToSpeech ;
    private Button speakButton;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    String textWeb;
    //String apiKey = getString(R.string.google_cloud_translate_api_key);
    //Translate translate = TranslateOptions.newBuilder()
    //        .setApiKey(apiKey)
    //        .build()
    //       .getService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infomonuments);
        appbar = findViewById(R.id.appbar);
        description = findViewById(R.id.descriptionMonument);
        address = findViewById(R.id.address);
        imageView = findViewById(R.id.imageMonument);
        url = findViewById(R.id.url);
        chart = (LineChart) findViewById(R.id.chart);
        //setupToolbar();
        textToSpeech = new TextToSpeech(this, this);
        speakButton = findViewById(R.id.textToSpeechButton);
        // Get intent, action and type
        Intent intent = getIntent();
        String action = intent.getAction();

        // Get the reference to the sensor manager and the sensor:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llama al método para leer el texto
                speakText(String.valueOf(Html.fromHtml(textDescription)));
          }
        });

        if (Intent.ACTION_SEND.equals(action)) {
            xmlText = intent.getStringExtra("XML_TEXT");
            String monument = intent.getStringExtra("MONUMENT");

            if (monument != null && xmlText != null) {
                appbar.setText(monument);
                get_info_monuments(xmlText, monument);
            }
        }

    }

    @Override
    protected void onDestroy() {
        // Libera los recursos de TextToSpeech cuando la actividad se destruye
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
}



    public ArrayList get_info_monuments (String string_XML, String monument_to_find) {

        ArrayList info_monuments = new ArrayList<>(); // This string will contain the loaded contents of a text resource

        try {

            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = new ByteArrayInputStream(string_XML.getBytes());

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            do_chart();

            int eventType = parser.getEventType(); // current event state of the parser
            boolean monument_found = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String elementName = null;
                elementName = parser.getName(); // name of the current element


                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("title".equals(elementName)) {
                            String monument = parser.nextText(); // if next element is TEXT then element content is returned
                            if (monument.equals(monument_to_find)){
                                monument_found = true;
                            }
                            else {
                                monument_found = false;
                            }

                        }
                        else if ("description".equals(elementName)){
                            if (monument_found){
                                textDescription = parser.nextText();
                                //Translation translation = translate.translate(textDescription, Translate.TranslateOption.targetLanguage("en"));
                                //textDescription = translation.getTranslatedText();
                                description.setText(Html.fromHtml(textDescription));
                            }
                        }
                        else if ("address".equals(elementName)){
                            if (monument_found){
                                textAddress = parser.nextText();
                                address.setText(Html.fromHtml(textAddress));
                            }
                        }
                        else if ("image".equals(elementName)){
                            if (monument_found){
                                imageUrl = parser.nextText();
                                Glide.with(this).load(imageUrl).into(imageView);
                            }
                        }
                        else if ("uri".equals(elementName)){
                            if (monument_found){
                                textWeb = parser.nextText();
                                SpannableString spannableString = new SpannableString(textWeb);
                                ClickableSpan clickableSpan = new ClickableSpan() {

                                    @Override
                                    public void onClick(@NonNull View view) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(textWeb));
                                        startActivity(intent);
                                    }
                                };
                                spannableString.setSpan(clickableSpan, 0, textWeb.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                url.setText(spannableString);
                                url.setMovementMethod(LinkMovementMethod.getInstance());
                            }
                        }
                }
                eventType = parser.next(); // Get next parsing event
            }

        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return info_monuments;

    }

    private void do_chart () {

        // List of datasets:
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        List<Entry> ValuesConcurrent = new ArrayList<Entry>();


        ValuesConcurrent.add(new Entry((float) 16.00, (float) 2.00));
        ValuesConcurrent.add(new Entry((float) 16.05, (float) 5.00));
        ValuesConcurrent.add(new Entry((float) 16.10, (float) 3.00));

        // add entries to datasets:
        LineDataSet dataSetSine = new LineDataSet(ValuesConcurrent, "concurrent");
        // configure datasets colors:
        dataSetSine.setColor(Color.RED);
        dataSetSine.setCircleColor(Color.RED);
        // add datasets to the list of datasets:
        dataSets.add(dataSetSine);
        // create line data:
        LineData lineDataSineAndCosine = new LineData(dataSets);
        // set data to chart:
        chart.setData(lineDataSineAndCosine);
        // configure chart:
        chart.getDescription().setEnabled(false);
        chart.animateX(3000); // animation to draw chart
        chart.invalidate(); // refresh

    }

    private void speakText(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            //int langResult = textToSpeech.setLanguage(Locale.ENGLISH);
            int langResult = textToSpeech.setLanguage(new Locale("es", "ES"));

            if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle language not supported
            } else {
                speakButton.setEnabled(true);
            }
        } else {
            // Handle initialization failure
        }
    }
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

}

