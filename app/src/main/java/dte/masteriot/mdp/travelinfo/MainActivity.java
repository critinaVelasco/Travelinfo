
package dte.masteriot.mdp.travelinfo;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity {

    TextView description;
    TextView url;
    String textDescription;
    ImageView imageView;
    LineChart chart;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infomonuments);

        description = findViewById(R.id.descriptionMonument);
        imageView = findViewById(R.id.imageMonument);
        url = findViewById(R.id.url);
        chart = (LineChart) findViewById(R.id.chart);

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
    }
}
