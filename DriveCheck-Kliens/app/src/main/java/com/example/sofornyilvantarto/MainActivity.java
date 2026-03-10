package com.example.sofornyilvantarto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private TextInputEditText etServerIp;
    private TextView tvServerStatus;
    private final Handler statusHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService connectionExecutor = Executors.newSingleThreadExecutor();

    // GYORSABB ELLENŐRZÉS: 2000ms helyett 500ms
    private final int CHECK_INTERVAL = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etServerIp = findViewById(R.id.et_server_ip_main);
        tvServerStatus = findViewById(R.id.tv_server_status);

        findViewById(R.id.btn_uj_bejegyzes).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.sofornyilvantarto.UjBejegyzesActivity.class);
            intent.putExtra("SERVER_IP", etServerIp.getText().toString());
            startActivity(intent);
        });

        findViewById(R.id.btn_osszesites).setOnClickListener(v -> {
            Intent intent = new Intent(this, ListazasActivity.class);
            intent.putExtra("SERVER_IP", etServerIp.getText().toString());
            startActivity(intent);
        });

        findViewById(R.id.btn_elerheto_autok).setOnClickListener(v -> {
            Toast.makeText(this, "Hamarosan...", Toast.LENGTH_SHORT).show();
        });

        startStatusChecker();
    }

    private void startStatusChecker() {
        statusHandler.post(new Runnable() {
            @Override
            public void run() {
                checkConnection();
                statusHandler.postDelayed(this, CHECK_INTERVAL);
            }
        });
    }

    private void checkConnection() {
        final String fullIp = etServerIp.getText().toString().trim();
        if (fullIp.isEmpty()) return;

        connectionExecutor.execute(() -> {
            boolean isOnline = false;
            try {
                String ip = fullIp.contains(":") ? fullIp.split(":")[0] : fullIp;
                int port = fullIp.contains(":") ? Integer.parseInt(fullIp.split(":")[1]) : 8080;

                Socket socket = new Socket();
                // Rövid timeout (500ms), hogy ne torlódjanak a kérések
                socket.connect(new InetSocketAddress(ip, port), 500);
                socket.close();
                isOnline = true;
            } catch (Exception e) {
                isOnline = false;
            }
            final boolean finalIsOnline = isOnline;
            runOnUiThread(() -> updateUI(finalIsOnline));
        });
    }

    private void updateUI(boolean online) {
        tvServerStatus.setText(online ? "KAPCSOLÓDVA (ONLINE)" : "NEM ELÉRHETŐ (OFFLINE)");
        tvServerStatus.setTextColor(Color.parseColor(online ? "#2E7D32" : "#C62828"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statusHandler.removeCallbacksAndMessages(null);
    }
}