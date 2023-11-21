
package dte.masteriot.mdp.travelinfo;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    TextView description;
    TextView url;
    String textDescription;
    ImageView imageView;
    LineChart chart;
    String imageUrl;
    RadioGroup radioGroup;
    final String serverUri = "tcp://192.168.56.1:1883";
    final String subscriptionTopic = "concurrentTopic";
    final String publishTopic = "concurrentPublishTopic";
    MqttAndroidClient mqttAndroidClient;
    String clientId = "Client";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infomonuments);

        description = findViewById(R.id.descriptionMonument);
        imageView = findViewById(R.id.imageMonument);
        url = findViewById(R.id.url);
        chart = (LineChart) findViewById(R.id.chart);
        radioGroup = findViewById(R.id.radioGroup);

        clientId = clientId + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    Log.d("MQTT", "Reconnected to MQTT server");
                    // Because Clean Session is true, we need to re-subscribe
                }else{
                    Log.d("MQTT", "Connected to MQTT server for the first time");
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);


        // List of datasets:
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        List<Entry> ValuesConcurrent = new ArrayList<Entry>();

        textDescription = "Since 1625, when it was acquired, its presence in the city has undergone constant changes of location and " +
                "various restorations.</strong></p><p>The history of this statue in Madrid is linked from its beginnings to that of the Puerta del Sol, " +
                "as it was commissioned in 1625 from the sculptor and dealer in classical statues, Ludovico Turqui, who brought it from Florence to form part of " +
                "the monumental fountain that would be installed in the Madrid square to embellish and dignify the city after the return of the Court to Madrid in 1606";

        description.setText(Html.fromHtml(textDescription));

        imageUrl = "https://estaticos.esmadrid.com/cdn/farfuture/sXF1qdcQRXN4MbZBCILfvFIEBUk7Ba3pwNXZ_IjX52A/mtime:1693473801/sites/default/files/recursosturisticos/infoturistica/mariblanca.jpg";

        Glide.with(this)
                .load(imageUrl)
                .into(imageView);

        String web = "https://www.esmadrid.com/en/tourist-information/mariblanca";
        SpannableString spannableString = new SpannableString(web);
        ClickableSpan clickableSpan = new ClickableSpan() {

            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(web));
                startActivity(intent);
            }
        };
        spannableString.setSpan(clickableSpan, 0, web.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        url.setText(spannableString);
        url.setMovementMethod(LinkMovementMethod.getInstance());

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

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (mqttAndroidClient.isConnected()) {
                    RadioButton radioButton = findViewById(checkedId);
                    MqttMessage message = new MqttMessage();

                    // Obtener el texto del RadioButton seleccionado
                    String mensaje = radioButton.getText().toString();
                    message.setPayload(mensaje.getBytes());
                    message.setRetained(false);
                    message.setQos(1);
                    // Enviar el mensaje MQTT
                    try {
                        mqttAndroidClient.publish(publishTopic, message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    if (!mqttAndroidClient.isConnected()) {
                        Log.d("MQTT","Client not connected!");
                    }
                }else{
                    Log.d("MQTT","Client not connected!");
                }
            }
        });
    }
    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}
