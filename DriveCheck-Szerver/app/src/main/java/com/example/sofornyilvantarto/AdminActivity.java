package com.example.sofornyilvantarto.uj;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// Erőforrás import a helyes csomagból
import com.example.sofornyilvantarto.uj.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminActivity extends AppCompatActivity {

    private static final String TORLESI_KULCS = "admin";
    private AppDatabase db;
    private LinearLayout adminGombokLayout;

    private Button btnTorlesIntervallum, btnTesztAdatok, btnOsszesTorlese;
    private Button btnAddSofor, btnAddAuto, btnDeleteSofor, btnDeleteAuto;

    private ProgressBar progressBarAdmin;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private static final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Adminisztrációs műveletek");
        }

        db = AppDatabase.getInstance(this);

        // UI elemek inicializálása
        adminGombokLayout = findViewById(R.id.admin_gombok_layout);
        progressBarAdmin = findViewById(R.id.progressBarAdmin);

        btnTorlesIntervallum = findViewById(R.id.btn_torles_intervallum);
        btnTesztAdatok = findViewById(R.id.btn_teszt_adatok);
        btnOsszesTorlese = findViewById(R.id.btn_osszes_torlese);

        btnAddSofor = findViewById(R.id.btn_add_sofor);
        btnAddAuto = findViewById(R.id.btn_add_auto);
        btnDeleteSofor = findViewById(R.id.btn_delete_sofor);
        btnDeleteAuto = findViewById(R.id.btn_delete_auto);

        // Eseménykezelők beállítása
        btnTorlesIntervallum.setOnClickListener(v -> showDeleteBySequenceIntervalDialog());
        btnTesztAdatok.setOnClickListener(v -> tesztAdatokHozzaadasa());
        btnOsszesTorlese.setOnClickListener(v -> osszesBejegyzesTorlese());

        btnAddSofor.setOnClickListener(v -> showAddSoforDialog());
        btnAddAuto.setOnClickListener(v -> showAddAutoDialog());
        btnDeleteSofor.setOnClickListener(v -> showOnlySoforDeleteDialog());
        btnDeleteAuto.setOnClickListener(v -> showOnlyAutoDeleteDialog());

        jelszoBekerese(); // Biztonsági zár indításkor
    }

    // --- ÚJ SOFŐR HOZZÁADÁSA ---
    private void showAddSoforDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Új sofőr regisztrálása");
        final EditText input = new EditText(this);
        input.setHint("Sofőr neve");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        FrameLayout container = new FrameLayout(this);
        int padding = (int)(20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, 10, padding, 10);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Mentés", (dialog, which) -> {
            String nev = input.getText().toString().trim();
            if (!nev.isEmpty()) mentesSablonkent(new Sofor(nev), null);
        });
        builder.setNegativeButton("Mégse", null);
        builder.show();
    }

    // --- ÚJ AUTÓ HOZZÁADÁSA ---
    private void showAddAutoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Új autó regisztrálása");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int)(20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, 20, padding, 10);

        final EditText etTipus = new EditText(this);
        etTipus.setHint("Autó típusa");
        layout.addView(etTipus);

        final EditText etRendszam = new EditText(this);
        etRendszam.setHint("Rendszám (pl. ABC-123)");
        etRendszam.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        layout.addView(etRendszam);

        builder.setView(layout);
        builder.setPositiveButton("Mentés", (dialog, which) -> {
            String tipus = etTipus.getText().toString().trim();
            String rendszam = etRendszam.getText().toString().trim().toUpperCase();
            if (!tipus.isEmpty() && !rendszam.isEmpty()) mentesSablonkent(null, new Auto(rendszam, tipus));
        });
        builder.setNegativeButton("Mégse", null);
        builder.show();
    }

    // --- CSAK SOFŐRÖK TÖRLÉSE ---
    private void showOnlySoforDeleteDialog() {
        databaseExecutor.execute(() -> {
            List<Ut> osszesUt = db.utDao().getAllUtak();
            Set<String> soforok = new HashSet<>();
            for (Ut u : osszesUt) {
                if (u.getSofor() != null) soforok.add(u.getSofor().getNev());
            }
            final List<String> lista = new ArrayList<>(soforok);
            runOnUiThread(() -> {
                if (lista.isEmpty()) { Toast.makeText(this, "Nincs sofőr!", Toast.LENGTH_SHORT).show(); return; }
                new AlertDialog.Builder(this)
                        .setTitle("Válaszd ki a törlendő sofőrt")
                        .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista), (dialog, which) -> {
                            executeTargetedDelete(lista.get(which), true);
                        }).show();
            });
        });
    }

    // --- CSAK AUTÓK TÖRLÉSE ---
    private void showOnlyAutoDeleteDialog() {
        databaseExecutor.execute(() -> {
            List<Ut> osszesUt = db.utDao().getAllUtak();
            Set<String> autok = new HashSet<>();
            for (Ut u : osszesUt) {
                if (u.getAuto() != null) autok.add(u.getAuto().getTipus() + " [" + u.getAuto().getRendszam() + "]");
            }
            final List<String> lista = new ArrayList<>(autok);
            runOnUiThread(() -> {
                if (lista.isEmpty()) { Toast.makeText(this, "Nincs autó!", Toast.LENGTH_SHORT).show(); return; }
                new AlertDialog.Builder(this)
                        .setTitle("Válaszd ki a törlendő autót")
                        .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista), (dialog, which) -> {
                            executeTargetedDelete(lista.get(which), false);
                        }).show();
            });
        });
    }

    private void executeTargetedDelete(String keresettString, boolean isSofor) {
        showProgress(true);
        databaseExecutor.execute(() -> {
            List<Ut> utak = db.utDao().getAllUtak();
            int szamlalo = 0;
            for (Ut u : utak) {
                boolean match = false;
                if (isSofor && u.getSofor() != null && u.getSofor().getNev().equals(keresettString)) match = true;
                if (!isSofor && u.getAuto() != null && keresettString.contains(u.getAuto().getRendszam())) match = true;
                if (match) { db.utDao().delete(u); szamlalo++; }
            }
            final int finalSzamlalo = szamlalo;
            runOnUiThread(() -> {
                showProgress(false);
                Toast.makeText(this, "Törölve: " + finalSzamlalo + " bejegyzés.", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void mentesSablonkent(Sofor s, Auto a) {
        databaseExecutor.execute(() -> {
            db.utDao().insert(new Ut(s, a, "-", "-", 0, 0, 0, "1900-01-01", "SABLON"));
            runOnUiThread(() -> Toast.makeText(this, "Sikeres mentés!", Toast.LENGTH_SHORT).show());
        });
    }

    // --- SORSZÁM ALAPJÁN TÖRLÉS ---
    private void executeDeleteBySequenceInterval(final int sTol, final int sIg) {
        showProgress(true);
        databaseExecutor.execute(() -> {
            List<Ut> mindenUt = db.utDao().getAllUtak();
            List<Ut> lathatoUtak = new ArrayList<>();

            // Csak a nem-SABLON bejegyzéseket vesszük alapul
            for (Ut u : mindenUt) {
                if (u != null && !"SABLON".equals(u.getStatus())) {
                    lathatoUtak.add(u);
                }
            }

            int toroltCount = 0;
            if (sTol >= 1 && sTol <= lathatoUtak.size()) {
                int idTol = lathatoUtak.get(sTol - 1).getId();
                int vegIndex = Math.min(sIg, lathatoUtak.size());
                int idIg = lathatoUtak.get(vegIndex - 1).getId();

                toroltCount = db.utDao().deleteByIdInterval(idTol, idIg);
            }

            final int finalTorolt = toroltCount;
            runOnUiThread(() -> {
                showProgress(false);
                Toast.makeText(this, finalTorolt + " bejegyzés törölve.", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // --- SEGÉDFUNKCIÓK (Jelszó, Teszt, Progress) ---
    private void jelszoBekerese() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Admin kulcs");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        b.setView(input);
        b.setPositiveButton("OK", (d, w) -> {
            if (input.getText().toString().equals(TORLESI_KULCS)) adminGombokLayout.setVisibility(View.VISIBLE);
            else { Toast.makeText(this, "Hibás!", Toast.LENGTH_SHORT).show(); finish(); }
        });
        b.setNegativeButton("Mégse", (d, w) -> finish());
        b.setCancelable(false); b.show();
    }

    private void tesztAdatokHozzaadasa() {
        showProgress(true);
        databaseExecutor.execute(() -> {
            String[] n = {"Nagy Janos", "Kiss Maria", "Kovacs Peter"};
            String[] t = {"Suzuki Swift", "Opel Astra", "Toyota Corolla"};
            for (int i = 0; i < 100; i++) {
                String rNap = String.format(Locale.US, "%02d", random.nextInt(28) + 1);
                String datum = "2026-03-" + rNap;
                db.utDao().insert(new Ut(new Sofor(n[random.nextInt(n.length)]),
                        new Auto("ABC-"+(100+i), t[random.nextInt(t.length)]),
                        "Hely A", "Hely B", 10.0 + random.nextInt(50), 5.0, 6.5, datum, "BEERKEZO"));
            }
            runOnUiThread(() -> { showProgress(false); Toast.makeText(this, "100 adat kész.", Toast.LENGTH_SHORT).show(); });
        });
    }

    // JAVÍTÁS: Adatbázis reset és ID nullázás
    private void osszesBejegyzesTorlese() {
        new AlertDialog.Builder(this).setTitle("MINDEN TÖRLÉSE").setMessage("Biztosan törölsz mindent? A sorszámozás is 1-ről fog indulni!")
                .setPositiveButton("Igen", (d, w) -> {
                    showProgress(true);
                    databaseExecutor.execute(() -> {
                        // 1. Tábla ürítése
                        db.utDao().deleteAll();
                        // 2. Számláló nullázása
                        db.utDao().resetSequence();
                        runOnUiThread(() -> {
                            showProgress(false);
                            Toast.makeText(this, "Adatbázis ürítve, sorszámozás resetelve.", Toast.LENGTH_SHORT).show();
                        });
                    });
                }).setNegativeButton("Mégse", null).show();
    }

    private void showProgress(boolean show) {
        progressBarAdmin.setVisibility(show ? View.VISIBLE : View.GONE);
        adminGombokLayout.setEnabled(!show);
    }

    private void showDeleteBySequenceIntervalDialog() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        final EditText etT = new EditText(this); etT.setHint("Tól (sorszám)"); etT.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText etI = new EditText(this); etI.setHint("Ig (sorszám)"); etI.setInputType(InputType.TYPE_CLASS_NUMBER);
        l.addView(etT); l.addView(etI);
        int p = (int)(20 * getResources().getDisplayMetrics().density); l.setPadding(p, p, p, p);
        new AlertDialog.Builder(this).setTitle("Sorszám alapú törlés").setView(l).setPositiveButton("Törlés", (d, w) -> {
            try { executeDeleteBySequenceInterval(Integer.parseInt(etT.getText().toString()), Integer.parseInt(etI.getText().toString())); }
            catch (Exception e) { Toast.makeText(this, "Hiba!", Toast.LENGTH_SHORT).show(); }
        }).setNegativeButton("Mégse", null).show();
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}