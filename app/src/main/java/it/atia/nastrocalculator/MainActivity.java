package it.atia.nastrocalculator;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends Activity {
    private EditText motorRpm, reduction, diameterMm, beltLengthMm, targetSpeed;
    private TextView output;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(buildUi());
    }

    private View buildUi() {
        int pad = dp(18);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(244,247,250));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);

        TextView brand = text("ATIA", 34, Color.rgb(0,75,135), true);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand);
        TextView title = text("Nastro Calculator", 21, Color.rgb(0,47,87), true);
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        TextView sub = text("Calcolo trasmissione e velocità del nastro", 14, Color.DKGRAY, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, dp(18));
        root.addView(sub);

        motorRpm = field(root, "Giri motore (rpm)", "1400");
        reduction = field(root, "Rapporto di riduzione (es. 30 = 1:30)", "30");
        diameterMm = field(root, "Diametro rullo (mm)", "80");
        beltLengthMm = field(root, "Lunghezza totale nastro (mm)", "1000");
        targetSpeed = field(root, "Velocità desiderata (m/min) — opzionale", "");

        Button calc = button("CALCOLA PARAMETRI");
        calc.setOnClickListener(v -> calculate());
        root.addView(calc);

        Button ratio = button("CALCOLA RAPPORTO NECESSARIO");
        ratio.setOnClickListener(v -> calculateRatio());
        root.addView(ratio);

        output = text("Inserisci i dati e premi CALCOLA.", 16, Color.rgb(0,47,87), false);
        output.setBackgroundColor(Color.WHITE);
        output.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams outParams = new LinearLayout.LayoutParams(-1, -2);
        outParams.setMargins(0, dp(12), 0, dp(20));
        root.addView(output, outParams);

        TextView creditsTitle = text("CREDITI", 13, Color.rgb(0,75,135), true);
        creditsTitle.setGravity(Gravity.CENTER);
        root.addView(creditsTitle);
        TextView creditsLogo = text("ATIA", 28, Color.rgb(0,75,135), true);
        creditsLogo.setGravity(Gravity.CENTER);
        creditsLogo.setPadding(0, dp(4), 0, 0);
        root.addView(creditsLogo);
        TextView credits = text("ATIA Food Device\nwww.atiafooddevice.com", 14, Color.DKGRAY, false);
        credits.setGravity(Gravity.CENTER);
        credits.setPadding(0, dp(2), 0, dp(24));
        credits.setOnClickListener(v -> {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.atiafooddevice.com"));
            startActivity(browser);
        });
        root.addView(credits);
        return scroll;
    }

    private EditText field(LinearLayout root, String label, String initial) {
        TextView l = text(label, 14, Color.rgb(0,47,87), true);
        root.addView(l);
        EditText e = new EditText(this);
        e.setText(initial);
        e.setTextSize(18);
        e.setSingleLine(true);
        e.setSelectAllOnFocus(true);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52));
        p.setMargins(0, dp(3), 0, dp(11));
        root.addView(e, p);
        return e;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackgroundColor(Color.rgb(0,75,135));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52));
        p.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(p);
        return b;
    }

    private void calculate() {
        try {
            double rpm = value(motorRpm), red = value(reduction), d = value(diameterMm), len = value(beltLengthMm);
            requirePositive(rpm, red, d, len);
            double rollerRpm = rpm / red;
            double circumferenceM = Math.PI * d / 1000.0;
            double mMin = circumferenceM * rollerRpm;
            double mSec = mMin / 60.0;
            double cycleSec = (len / 1000.0) / mSec;
            output.setText(String.format(Locale.ITALY,
                "RISULTATI\n\nGiri rullo: %.2f rpm\nCirconferenza rullo: %.1f mm\nVelocità nastro: %.3f m/s\nVelocità nastro: %.2f m/min\nTempo percorrenza nastro: %.2f s\nTempo per metro: %.2f s",
                rollerRpm, Math.PI*d, mSec, mMin, cycleSec, 1.0/mSec));
        } catch (Exception e) { error(); }
    }

    private void calculateRatio() {
        try {
            double rpm = value(motorRpm), d = value(diameterMm), wanted = value(targetSpeed);
            requirePositive(rpm, d, wanted);
            double rollerNeeded = wanted / (Math.PI * d / 1000.0);
            double ratioNeeded = rpm / rollerNeeded;
            reduction.setText(String.format(Locale.US, "%.3f", ratioNeeded));
            output.setText(String.format(Locale.ITALY,
                "RAPPORTO NECESSARIO\n\nRapporto teorico: 1 : %.3f\nGiri rullo richiesti: %.2f rpm\nVelocità impostata: %.2f m/min\n\nIl rapporto è stato copiato nel relativo campo.",
                ratioNeeded, rollerNeeded, wanted));
        } catch (Exception e) { error(); }
    }

    private double value(EditText e) { return Double.parseDouble(e.getText().toString().trim().replace(',', '.')); }
    private void requirePositive(double... values) { for (double v : values) if (v <= 0) throw new IllegalArgumentException(); }
    private void error() { Toast.makeText(this, "Controlla i valori inseriti: devono essere numeri maggiori di zero.", Toast.LENGTH_LONG).show(); }
    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
