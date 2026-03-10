package com.example.sofornyilvantarto;

import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class AdminActivity extends AppCompatActivity {

    private static final String TORLESI_KULCS = "admin"; //
    private AppDatabase db;
    private LinearLayout adminGombokLayout;
    private Button btnTorlesIntervallum, btnTesztAdatok, btnOsszesTorlese;
    private ProgressBar progressBarAdmin;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private static final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Vissza gomb bekapcsolása a fejlécben
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this); //

        // UI elemek inicializálása
        adminGombokLayout = findViewById(R.id.admin_gombok_layout);
        btnTorlesIntervallum = findViewById(R.id.btn_torles_intervallum);
        btnTesztAdatok = findViewById(R.id.btn_teszt_adatok);
        btnOsszesTorlese = findViewById(R.id.btn_osszes_torlese);
        progressBarAdmin = findViewById(R.id.progressBarAdmin);

        btnTorlesIntervallum.setOnClickListener(v -> showDeleteBySequenceIntervalDialog());
        btnTesztAdatok.setOnClickListener(v -> tesztAdatokHozzaadasa());
        btnOsszesTorlese.setOnClickListener(v -> osszesBejegyzesTorlese());

        jelszoBekerese(); // Belépéskor jelszót kér
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgress(boolean show) {
        progressBarAdmin.setVisibility(show ? View.VISIBLE : View.GONE);
        btnTorlesIntervallum.setEnabled(!show);
        btnTesztAdatok.setEnabled(!show);
        btnOsszesTorlese.setEnabled(!show);
    }

    private void jelszoBekerese() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adminisztrátori kulcs");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            if (input.getText().toString().equals(TORLESI_KULCS)) {
                adminGombokLayout.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Sikeres azonosítás!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Hibás kulcs!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.setCancelable(false).show();
    }

    private void tesztAdatokHozzaadasa() {
        showProgress(true);
        databaseExecutor.execute(() -> {
            String[] nevek = {"Nagy János", "Kiss Mária", "Kovács Péter", "Tóth Éva"};
            String[] tipusok = {"Suzuki Swift", "Opel Astra", "Ford Focus", "Toyota Yaris"};
            String[] helyek = {"Budapest", "Debrecen", "Szeged", "Miskolc", "Pécs"};

            for (int i = 0; i < 100; i++) {
                Sofor sofor = new Sofor(nevek[random.nextInt(nevek.length)]); //
                Auto auto = new Auto("TEST-" + (100 + i), tipusok[random.nextInt(tipusok.length)]); //

                String ind = helyek[random.nextInt(helyek.length)];
                String erk = helyek[random.nextInt(helyek.length)];
                String datum = "2026-02";

                Ut ujUt = new Ut(sofor, auto, ind, erk,
                        random.nextInt(300) + 10.5,
                        random.nextInt(150) + 5.0,
                        random.nextInt(20) + 2.0,
                        datum, "teszt"); //

                db.utDao().insert(ujUt); //
            }

            runOnUiThread(() -> {
                showProgress(false);
                Toast.makeText(this, "100 teszt adat hozzáadva!", Toast.LENGTH_LONG).show();
            });
        });
    }

    private void showDeleteBySequenceIntervalDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText inTol = new EditText(this); inTol.setHint("Ettől (sorszám)");
        final EditText inIg = new EditText(this); inIg.setHint("Eddig (sorszám)");
        layout.addView(inTol); layout.addView(inIg);

        new AlertDialog.Builder(this).setTitle("Törlés sorszám alapján").setView(layout)
                .setPositiveButton("Törlés", (d, w) -> {
                    try {
                        executeDeleteBySequenceInterval(Integer.parseInt(inTol.getText().toString()),
                                Integer.parseInt(inIg.getText().toString()));
                    } catch (Exception e) { Toast.makeText(this, "Hibás számok!", Toast.LENGTH_SHORT).show(); }
                }).show();
    }

    private void executeDeleteBySequenceInterval(int tol, int ig) {
        showProgress(true);
        databaseExecutor.execute(() -> {
            List<Ut> lista = db.utDao().getAllUtak();
            if (lista.size() >= tol && lista.size() >= ig && ig >= tol) {
                int idTol = lista.get(tol - 1).getId();
                int idIg = lista.get(ig - 1).getId();
                db.utDao().deleteByIdInterval(idTol, idIg); //
            }
            runOnUiThread(() -> {
                showProgress(false);
                Toast.makeText(this, "Törlés kész!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void osszesBejegyzesTorlese() {
        new AlertDialog.Builder(this).setTitle("FIGYELEM!").setMessage("MINDENT törölsz?")
                .setPositiveButton("Igen", (d, w) -> {
                    databaseExecutor.execute(() -> {
                        db.utDao().deleteAll(); //
                        runOnUiThread(() -> Toast.makeText(this, "Adatbázis ürítve!", Toast.LENGTH_SHORT).show());
                    });
                }).show();
    }
}