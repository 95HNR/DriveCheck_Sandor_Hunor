package com.example.sofornyilvantarto;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UjBejegyzesActivity extends AppCompatActivity {

    private Spinner spinnerSofor, spinnerAutoTipus;
    private MaterialAutoCompleteTextView etIndulas, etErkezes;
    private TextInputEditText etAutoRendszam, etTavolsag, etKoltseg, etFogyasztas, etDatum;
    private Button btnMentes;

    private String serverIp;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();
    private List<Ut> serverAdatLista = new ArrayList<>();

    private Long kivalasztottDatumMillis = MaterialDatePicker.todayInUtcMilliseconds();
    private double latStart = 0, lonStart = 0, latEnd = 0, lonEnd = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uj_bejegyzes);

        serverIp = getIntent().getStringExtra("SERVER_IP");

        spinnerSofor = findViewById(R.id.spinner_sofor);
        spinnerAutoTipus = findViewById(R.id.spinner_auto_tipus);
        etAutoRendszam = findViewById(R.id.et_auto_rendszam);
        etDatum = findViewById(R.id.et_datum);
        etIndulas = findViewById(R.id.et_indulas);
        etErkezes = findViewById(R.id.et_erkezes);
        etTavolsag = findViewById(R.id.et_tavolsag);
        etFogyasztas = findViewById(R.id.et_fogyasztas);
        etKoltseg = findViewById(R.id.et_koltseg);
        btnMentes = findViewById(R.id.btn_mentes);

        // 1. MAI DÁTUM AUTOMATIKUS KITÖLTÉSE (Most már yyyy-MM-dd formátumban)
        updateDatumLabel();

        PlaceAutoSuggestAdapter placeAdapter = new PlaceAutoSuggestAdapter(this, android.R.layout.simple_list_item_1);
        etIndulas.setAdapter(placeAdapter);
        etErkezes.setAdapter(placeAdapter);

        etIndulas.setOnItemClickListener((parent, view, position, id) -> {
            OsmPlace place = (OsmPlace) parent.getItemAtPosition(position);
            latStart = Double.parseDouble(place.lat);
            lonStart = Double.parseDouble(place.lon);
            frissitTavolsag();
        });

        etErkezes.setOnItemClickListener((parent, view, position, id) -> {
            OsmPlace place = (OsmPlace) parent.getItemAtPosition(position);
            latEnd = Double.parseDouble(place.lat);
            lonEnd = Double.parseDouble(place.lon);
            frissitTavolsag();
        });

        spinnerAutoTipus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String valasztottTipus = parent.getItemAtPosition(position).toString();
                for (Ut ut : serverAdatLista) {
                    if (ut.getAuto() != null && ut.getAuto().getTipus().equals(valasztottTipus)) {
                        etAutoRendszam.setText(ut.getAuto().getRendszam());
                        break;
                    }
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        etFogyasztas.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                try {
                    double l = Double.parseDouble(s.toString());
                    etKoltseg.setText(String.format(Locale.US, "%.2f", l * 1.65));
                } catch (Exception e) { etKoltseg.setText("0.00"); }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        etDatum.setOnClickListener(v -> showDatePicker());
        betoltSzerverAdatok();
        btnMentes.setOnClickListener(v -> bekuldes());
    }

    private void frissitTavolsag() {
        if (latStart != 0 && latEnd != 0) {
            double earthRadius = 6371;
            double dLat = Math.toRadians(latEnd - latStart);
            double dLon = Math.toRadians(lonEnd - lonStart);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(latStart)) * Math.cos(Math.toRadians(latEnd)) *
                            Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double dist = earthRadius * c;
            etTavolsag.setText(String.format(Locale.US, "%.2f", dist));
        }
    }

    // JAVÍTVA: A formátum most már yyyy-MM-dd (év-hónap-nap)
    private void updateDatumLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        etDatum.setText(sdf.format(kivalasztottDatumMillis));
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().setSelection(kivalasztottDatumMillis).build();
        picker.addOnPositiveButtonClickListener(selection -> {
            kivalasztottDatumMillis = selection;
            updateDatumLabel();
        });
        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void betoltSzerverAdatok() {
        if (serverIp == null) return;
        final String tisztaIp = serverIp.contains(":") ? serverIp.split(":")[0] : serverIp;
        executor.execute(() -> {
            try (Socket socket = new Socket(tisztaIp, 8080);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println("GET_RESOURCES");
                String json = in.readLine();
                if (json != null) {
                    serverAdatLista = gson.fromJson(json, new TypeToken<List<Ut>>(){}.getType());
                    Set<String> soforok = new HashSet<>();
                    Set<String> tipusok = new HashSet<>();
                    for (Ut u : serverAdatLista) {
                        if (u.getSofor() != null) soforok.add(u.getSofor().getNev());
                        if (u.getAuto() != null) tipusok.add(u.getAuto().getTipus());
                    }
                    new Handler(Looper.getMainLooper()).post(() -> {
                        spinnerSofor.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(soforok)));
                        spinnerAutoTipus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(tipusok)));
                    });
                }
            } catch (Exception e) { Log.e("UjBejegyzes", "Szerver hiba", e); }
        });
    }

    private void bekuldes() {
        try {
            if (serverIp == null) {
                Toast.makeText(this, "Nincs szerver IP megadva!", Toast.LENGTH_SHORT).show();
                return;
            }

            String soforNev = spinnerSofor.getSelectedItem().toString();
            String autoTipus = spinnerAutoTipus.getSelectedItem().toString();
            String rendszam = etAutoRendszam.getText().toString();
            String indulas = etIndulas.getText().toString();
            String erkezes = etErkezes.getText().toString();

            // JAVÍTVA: Beküldéskor is a teljes dátumot használjuk (yyyy-MM-dd)
            String datumTeljes = etDatum.getText().toString();

            double tav = Double.parseDouble(etTavolsag.getText().toString());
            double fogy = Double.parseDouble(etFogyasztas.getText().toString());
            double koltseg = Double.parseDouble(etKoltseg.getText().toString());

            Ut ujUt = new Ut(new Sofor(soforNev), new Auto(rendszam, autoTipus),
                    indulas, erkezes, tav, koltseg, fogy, datumTeljes, "BEERKEZO");

            final String tisztaIp = serverIp.contains(":") ? serverIp.split(":")[0] : serverIp;

            executor.execute(() -> {
                try (Socket socket = new Socket(tisztaIp, 8080);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    out.println(gson.toJson(ujUt));

                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(this, "Kérelem sikeresen elküldve!", Toast.LENGTH_LONG).show();
                        finish();
                    });

                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(this, "Hiba: Nem sikerült kapcsolódni a szerverhez!", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Kérlek tölts ki minden mezőt megfelelően!", Toast.LENGTH_SHORT).show();
        }
    }
}