package com.example.sofornyilvantarto.uj;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class UjBejegyzesActivity extends AppCompatActivity {

    private TextInputEditText etSoforNev, etAutoRendszam, etAutoTipus, etIndulas, etErkezes, etTavolsag, etKoltseg, etFogyasztas, etDatum;
    private Button btnMentes;
    private AppDatabase db;
    private Long kivalasztottDatumMillis = MaterialDatePicker.todayInUtcMilliseconds();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uj_bejegyzes);

        // --- Felső menüsáv beállítása ---
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Új kérelem rögzítése");
        }

        db = AppDatabase.getInstance(this);

        etSoforNev = findViewById(R.id.et_sofor_nev);
        etAutoRendszam = findViewById(R.id.et_auto_rendszam);
        etAutoTipus = findViewById(R.id.et_auto_tipus);
        etDatum = findViewById(R.id.et_datum);
        etIndulas = findViewById(R.id.et_indulas);
        etErkezes = findViewById(R.id.et_erkezes);
        etTavolsag = findViewById(R.id.et_tavolsag);
        etKoltseg = findViewById(R.id.et_koltseg);
        etFogyasztas = findViewById(R.id.et_fogyasztas);
        btnMentes = findViewById(R.id.btn_mentes);

        updateDatumLabel();
        etDatum.setOnClickListener(v -> showDatePickerDialog());
        btnMentes.setOnClickListener(v -> mentes());
    }

    private void updateDatumLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        etDatum.setText(sdf.format(kivalasztottDatumMillis));
    }

    private void showDatePickerDialog() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Dátum kiválasztása");
        builder.setSelection(kivalasztottDatumMillis);
        final MaterialDatePicker<Long> materialDatePicker = builder.build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            kivalasztottDatumMillis = selection;
            updateDatumLabel();
        });
        materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void mentes() {
        String soforNev = etSoforNev.getText().toString().trim();
        String rendszam = etAutoRendszam.getText().toString().trim().toUpperCase();
        String tipus = etAutoTipus.getText().toString().trim();
        String honapEv = etDatum.getText().toString();
        String indulas = etIndulas.getText().toString().trim();
        String erkezes = etErkezes.getText().toString().trim();

        if (soforNev.isEmpty() || rendszam.isEmpty() || tipus.isEmpty()) {
            Toast.makeText(this, "Minden mezőt tölts ki!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double tavolsag = Double.parseDouble(etTavolsag.getText().toString());
            double koltseg = Double.parseDouble(etKoltseg.getText().toString());
            double fogyasztas = Double.parseDouble(etFogyasztas.getText().toString());

            Sofor sofor = new Sofor(soforNev);
            Auto auto = new Auto(rendszam, tipus);
            // Alapértelmezett státusz: BEERKEZO
            Ut ujUt = new Ut(sofor, auto, indulas, erkezes, tavolsag, koltseg, fogyasztas, honapEv, "BEERKEZO");

            db.utDao().insert(ujUt);
            Toast.makeText(this, "Kérelem elküldve!", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Hiba a mentés során!", Toast.LENGTH_SHORT).show();
        }
    }

    // Vissza nyíl kezelése
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}