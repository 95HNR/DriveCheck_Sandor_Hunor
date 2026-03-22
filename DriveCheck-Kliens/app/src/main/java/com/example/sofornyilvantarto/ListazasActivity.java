package com.example.sofornyilvantarto;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections; // SZÜKSÉGES IMPORT a sorrend megfordításához
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ListazasActivity extends AppCompatActivity {

    private TextInputEditText etFilterDatum, etFilterSofor, etFilterRendszam;
    private RecyclerView recyclerView;
    private com.example.sofornyilvantarto.UtAdapter adapter;

    private String serverIp;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    private LinearLayout filterContainer;
    private ImageView ivToggleArrow;
    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listazas);

        serverIp = getIntent().getStringExtra("SERVER_IP");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Benyújtott kérelmek");
        }

        // Nézetek bekötése
        etFilterDatum = findViewById(R.id.et_filter_datum);
        etFilterSofor = findViewById(R.id.et_filter_sofor);
        etFilterRendszam = findViewById(R.id.et_filter_rendszam);
        recyclerView = findViewById(R.id.recyclerview_utak);

        LinearLayout filterHeader = findViewById(R.id.filter_header);
        filterContainer = findViewById(R.id.filter_container);
        ivToggleArrow = findViewById(R.id.iv_toggle_arrow);

        // Naptár választó a szűréshez
        if (etFilterDatum != null) {
            etFilterDatum.setFocusable(false);
            etFilterDatum.setOnClickListener(v -> showDatePicker());
        }

        if (filterHeader != null) {
            filterHeader.setOnClickListener(v -> toggleFilterMenu());
        }

        adapter = new UtAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button btnSzures = findViewById(R.id.btn_szures);
        if (btnSzures != null) {
            btnSzures.setOnClickListener(v -> {
                szuresInditasa();
                toggleFilterMenu();
            });
        }

        // Első betöltés
        szuresInditasa();
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Válassz dátumot")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            etFilterDatum.setText(sdf.format(new Date(selection)));
        });

        datePicker.show(getSupportFragmentManager(), "FILTER_DATE_PICKER");
    }

    private void toggleFilterMenu() {
        if (filterContainer == null || ivToggleArrow == null) return;
        if (isExpanded) {
            filterContainer.setVisibility(View.GONE);
            ivToggleArrow.animate().rotation(0).setDuration(200).start();
        } else {
            filterContainer.setVisibility(View.VISIBLE);
            ivToggleArrow.animate().rotation(180).setDuration(200).start();
        }
        isExpanded = !isExpanded;
    }

    private void szuresInditasa() {
        if (serverIp == null || serverIp.isEmpty()) return;
        final String tisztaIp = serverIp.contains(":") ? serverIp.split(":")[0] : serverIp;

        executor.execute(() -> {
            try (Socket socket = new Socket(tisztaIp, 8080);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println("GET_ALL");
                String jsonValasz = in.readLine();

                if (jsonValasz != null && !jsonValasz.isEmpty()) {
                    Type listType = new TypeToken<ArrayList<Ut>>(){}.getType();
                    List<Ut> osszesUt = gson.fromJson(jsonValasz, listType);

                    // JAVÍTÁS: A lista megfordítása, hogy a legújabb (utolsó) ID legyen legfelül
                    Collections.reverse(osszesUt);

                    // Szűrés alkalmazása a már megfordított listán
                    List<Ut> szurtLista = alkalmazSzures(osszesUt);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        // DIAGNOSZTIKA: Megmondja, hány adat jött a szerverről
                       // Toast.makeText(this, "Adatok: " + szurtLista.size(), Toast.LENGTH_SHORT).show();
                        adapter.setUtak(szurtLista);
                        adapter.notifyDataSetChanged();
                    });
                } else {
                    //runOnUiThread(() -> Toast.makeText(this, "A szerver nem küldött adatot!", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                //runOnUiThread(() -> Toast.makeText(this, "Szerver hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private List<Ut> alkalmazSzures(List<Ut> lista) {
        if (lista == null) return new ArrayList<>();

        String fDatum = etFilterDatum.getText().toString().trim();
        String fSofor = etFilterSofor.getText().toString().toLowerCase().trim();
        String fRendszam = etFilterRendszam.getText().toString().toLowerCase().trim();

        // Ha minden mező üres, adjuk vissza az eredeti listát (gyorsabb és biztosabb)
        if (fDatum.isEmpty() && fSofor.isEmpty() && fRendszam.isEmpty()) return lista;

        List<Ut> eredmeny = new ArrayList<>();
        for (Ut ut : lista) {
            if (ut == null) continue;
            boolean ok = true;

            // Dátum szűrés
            if (!fDatum.isEmpty()) {
                if (ut.getHonapEv() == null || !ut.getHonapEv().contains(fDatum)) ok = false;
            }

            // Sofőr szűrés
            if (ok && !fSofor.isEmpty()) {
                if (ut.getSofor() == null || ut.getSofor().getNev() == null ||
                        !ut.getSofor().getNev().toLowerCase().contains(fSofor)) ok = false;
            }

            // Rendszám szűrés
            if (ok && !fRendszam.isEmpty()) {
                if (ut.getAuto() == null || ut.getAuto().getRendszam() == null ||
                        !ut.getAuto().getRendszam().toLowerCase().contains(fRendszam)) ok = false;
            }

            if (ok) eredmeny.add(ut);
        }
        return eredmeny;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}