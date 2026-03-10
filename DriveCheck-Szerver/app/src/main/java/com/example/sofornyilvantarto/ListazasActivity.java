package com.example.sofornyilvantarto.uj;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListazasActivity extends AppCompatActivity {

    private UtAdapter adapter;
    private AppDatabase db;
    private EditText etFilterDatum, etFilterSofor, etFilterRendszam;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Osztályszintű változók a lenyitható menühöz
    private LinearLayout filterHeader, filterContainer;
    private ImageView ivToggleArrow;
    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listazas);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Összesítés és szűrés");
        }

        db = AppDatabase.getInstance(this);

        // Alapvető nézetek bekötése
        etFilterDatum = findViewById(R.id.et_filter_datum);
        etFilterSofor = findViewById(R.id.et_filter_sofor);
        etFilterRendszam = findViewById(R.id.et_filter_rendszam);
        Button btnSzures = findViewById(R.id.btn_szures);
        RecyclerView recyclerView = findViewById(R.id.recyclerview_utak);

        // Lenyitható menü elemeinek bekötése
        filterHeader = findViewById(R.id.filter_header);
        filterContainer = findViewById(R.id.filter_container);
        ivToggleArrow = findViewById(R.id.iv_toggle_arrow);

        // Biztonsági ellenőrzés a fejléc kattintáshoz
        if (filterHeader != null) {
            filterHeader.setOnClickListener(v -> toggleFilterMenu());
        }

        // Naptár választó beállítása biztonságosan
        if (etFilterDatum != null) {
            etFilterDatum.setFocusable(false);
            etFilterDatum.setOnClickListener(v -> showDatePicker());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UtAdapter();
        recyclerView.setAdapter(adapter);

        if (btnSzures != null) {
            btnSzures.setOnClickListener(v -> {
                vegrehajtSzures();
                if (isExpanded) toggleFilterMenu();
            });
        }

        // Az első betöltés indítása
        vegrehajtSzures();
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Válassz napot a szűréshez")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (etFilterDatum != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                etFilterDatum.setText(sdf.format(new Date(selection)));
            }
        });

        datePicker.show(getSupportFragmentManager(), "FILTER_DATE_PICKER");
    }

    private void toggleFilterMenu() {
        if (filterContainer == null || ivToggleArrow == null) return;

        if (isExpanded) {
            filterContainer.setVisibility(View.GONE);
            ivToggleArrow.setRotation(0);
        } else {
            filterContainer.setVisibility(View.VISIBLE);
            ivToggleArrow.setRotation(180);
        }
        isExpanded = !isExpanded;
    }

    private void vegrehajtSzures() {
        // JAVÍTÁS: Null-ellenőrzés, hogy ne omoljon össze indításkor, ha hiányzik egy nézet
        if (etFilterDatum == null || etFilterSofor == null || etFilterRendszam == null) {
            executor.execute(() -> {
                List<Ut> eredmeny = db.utDao().getAllUtak();
                runOnUiThread(() -> { if (adapter != null) adapter.setUtak(eredmeny); });
            });
            return;
        }

        String datum = etFilterDatum.getText().toString().trim();
        String sofor = etFilterSofor.getText().toString().trim();
        String rendszam = etFilterRendszam.getText().toString().trim();

        executor.execute(() -> {
            try {
                // Az UtDao LIKE lekérdezése támogatja a napokat is
                List<Ut> eredmeny = db.utDao().szures(datum, sofor, rendszam);
                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.setUtak(eredmeny);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Hiba a lekérdezés során!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
