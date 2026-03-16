package com.example.sofornyilvantarto;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutokListaFragment extends Fragment {

    private String statusz;
    private String serverIp;

    private RecyclerView recyclerView;
    private TextView tvEmptyView;
    private AutoAdapter adapter;

    // Háttérszál a hálózati kommunikációhoz
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static AutokListaFragment newInstance(String statusz, String serverIp) {
        AutokListaFragment fragment = new AutokListaFragment();
        Bundle args = new Bundle();
        args.putString("STATUSZ", statusz); // "ELERHETO" vagy "FOGLALT"
        args.putString("SERVER_IP", serverIp);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statusz = getArguments().getString("STATUSZ");
            serverIp = getArguments().getString("SERVER_IP");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_autok_lista, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_autok);
        tvEmptyView = view.findViewById(R.id.tv_empty_view_autok);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AutoAdapter();
        recyclerView.setAdapter(adapter);

        adatokLetoltese();

        return view;
    }

    private void adatokLetoltese() {
        tvEmptyView.setText("Adatok betöltése...");
        tvEmptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        executorService.execute(() -> {
            List<Auto> letoltottAutok = new ArrayList<>();
            boolean hibaTortent = false;

            try {
                // IP és port szétválasztása
                String ip = serverIp.contains(":") ? serverIp.split(":")[0] : serverIp;
                int port = serverIp.contains(":") ? Integer.parseInt(serverIp.split(":")[1]) : 8080;

                // Nyers Socket kapcsolat felépítése, pontosan ahogy a többi Activity-ben is van
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 2000);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Parancs kiküldése: GET_AUTOK_ELERHETO vagy GET_AUTOK_FOGLALT
                String parancs = "GET_AUTOK_" + statusz;
                out.println(parancs);

                // Szerver válaszának (JSON szöveg) beolvasása
                String response = in.readLine();

                if (response != null && !response.isEmpty()) {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String rendszam = obj.optString("rendszam", "Ismeretlen rendszám");
                        String tipus = obj.optString("tipus", "Ismeretlen típus");
                        letoltottAutok.add(new Auto(rendszam, tipus));
                    }
                } else {
                    hibaTortent = true;
                }

                socket.close();

            } catch (Exception e) {
                Log.e("AutokListaFragment", "Hiba a letöltés során", e);
                hibaTortent = true;
            }

            final boolean finalHibaTortent = hibaTortent;

            // UI frissítése a fő szálon
            mainHandler.post(() -> {
                if (finalHibaTortent) {
                    tvEmptyView.setText("Hiba történt a szerverhez való csatlakozáskor.");
                } else if (letoltottAutok.isEmpty()) {
                    tvEmptyView.setText("Nincsenek " + (statusz.equals("ELERHETO") ? "elérhető" : "foglalt") + " autók.");
                } else {
                    tvEmptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setAutok(letoltottAutok);
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}