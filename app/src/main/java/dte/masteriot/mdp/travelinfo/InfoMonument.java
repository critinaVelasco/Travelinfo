
package dte.masteriot.mdp.travelinfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;

public class InfoMonument extends AppCompatActivity {

    TextView description;
    TextView url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infomonuments);

        description = findViewById(R.id.descriptionMonument);
        url = findViewById(R.id.url);

        description.setText("Since 1625, when it was acquired, its presence in the city has undergone constant changes of location and " +
                "various restorations.</strong></p><p>The history of this statue in Madrid is linked from its beginnings to that of the Puerta del Sol, " +
                "as it was commissioned in 1625 from the sculptor and dealer in classical statues, Ludovico Turqui, who brought it from Florence to form part of " +
                "the monumental fountain that would be installed in the Madrid square to embellish and dignify the city after the return of the Court to Madrid in 1606");
        String web = "https://www.esmadrid.com/en/tourist-information/mariblanca";
        SpannableString spannableString = new SpannableString(web);
        ClickableSpan clickableSpan = new ClickableSpan() {

            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(web));
                startActivity(intent);
            }
        };
        spannableString.setSpan(clickableSpan, 0, url.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        url.setText(spannableString);
        url.setMovementMethod(LinkMovementMethod.getInstance());

    }
}
