package com.example.sofornyilvantarto.uj;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sofornyilvantarto.uj.R;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private boolean isServerRunning = false;
    private ServerSocket serverSocket;
    private final int PORT = 8080;
    private final ExecutorService serverExecutor = Executors.newFixedThreadPool(4);
    private final Gson gson = new Gson();

    private MaterialButton btnServerToggle;
    private TextView tvServerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_kerelmek).setOnClickListener(v ->
                startActivity(new Intent(this, KerelmekActivity.class)));
        findViewById(R.id.btn_osszesites).setOnClickListener(v ->
                startActivity(new Intent(this, ListazasActivity.class)));
        findViewById(R.id.btn_admin).setOnClickListener(v ->
                startActivity(new Intent(this, AdminActivity.class)));

        btnServerToggle = findViewById(R.id.btn_server_toggle);
        tvServerInfo = findViewById(R.id.tv_server_info);

        btnServerToggle.setOnClickListener(v -> {
            if (!isServerRunning) startServer();
            else stopServer();
        });
    }

    private void startServer() {
        isServerRunning = true;
        updateServerUI(true, "IP: " + getLocalIpAddress() + ":" + PORT);

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                AppDatabase db = AppDatabase.getInstance(this);

                while (isServerRunning) {
                    Socket clientSocket = serverSocket.accept();
                    serverExecutor.execute(() -> handleClient(clientSocket, db));
                }
            } catch (IOException e) {
                if (isServerRunning) runOnUiThread(this::stopServer);
            }
        }).start();
    }

    private void handleClient(Socket clientSocket, AppDatabase db) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String message = reader.readLine();
            if (message != null) {

                // 1. ADATBÁZISBÓL OLVASÁS A FIX LISTÁK HELYETT!
                if (message.equals("GET_AUTOK_ELERHETO")) {
                    List<Auto> elerhetoAutok = db.autoDao().getAutokByStatus("ELERHETO");
                    writer.println(gson.toJson(elerhetoAutok));
                }
                else if (message.equals("GET_AUTOK_FOGLALT")) {
                    List<Auto> foglaltAutok = db.autoDao().getAutokByStatus("FOGLALT");
                    writer.println(gson.toJson(foglaltAutok));
                }

                // 2. Régi lekérdezések (utak kezelése)
                else if (message.equals("GET_ALL")) {
                    List<Ut> utak = db.utDao().getAllSync();
                    writer.println(gson.toJson(utak));
                }
                else if (message.equals("GET_RESOURCES")) {
                    List<Ut> osszesUt = db.utDao().getAllSync();
                    writer.println(gson.toJson(osszesUt));
                }
                else {
                    try {
                        Ut ut = gson.fromJson(message, Ut.class);
                        if (ut != null) {
                            ut.setStatus("BEERKEZO");
                            db.utDao().insert(ut);
                            runOnUiThread(() -> Toast.makeText(this, "Új kérelem: " + ut.getSofor().getNev(), Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        // Nem megfelelő JSON esetén csendben maradunk
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    private void stopServer() {
        isServerRunning = false;
        new Thread(() -> {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        updateServerUI(false, "Szerver: Kikapcsolva");
    }

    private void updateServerUI(boolean online, String info) {
        runOnUiThread(() -> {
            btnServerToggle.setText(online ? "ONLINE" : "OFFLINE");
            btnServerToggle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(online ? "#2E7D32" : "#C62828")));
            tvServerInfo.setText(info);
        });
    }

    private String getLocalIpAddress() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            int ip = wm.getConnectionInfo().getIpAddress();
            return InetAddress.getByAddress(ByteBuffer.allocate(4).order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(ip).array()).getHostAddress();
        } catch (Exception e) { return "0.0.0.0"; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServer();
    }
}