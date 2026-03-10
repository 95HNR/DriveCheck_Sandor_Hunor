package com.example.sofornyilvantarto.uj;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KerelmekListaFragment extends Fragment {

    private String statuszSzuro;
    private UtAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static KerelmekListaFragment newInstance(String statusz) {
        KerelmekListaFragment fragment = new KerelmekListaFragment();
        Bundle args = new Bundle();
        args.putString("STATUSZ", statusz);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statuszSzuro = getArguments().getString("STATUSZ");
        }
        db = AppDatabase.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kerelmek_lista, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_kerelmek_lista);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new UtAdapter();
        recyclerView.setAdapter(adapter);

        // Kattintás kezelése
        adapter.setOnItemClickListener(this::mutatKezeloDialogus);

        frissitLista();
        return view;
    }

    private void frissitLista() {
        executor.execute(() -> {
            List<Ut> szurtUtak = db.utDao().getUtakByStatus(statuszSzuro);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.setUtak(szurtUtak));
            }
        });
    }

    private void mutatKezeloDialogus(Ut ut) {
        // Csak a beérkező kérelmeket lehessen módosítani (opcionális, de logikus)
        if (!"BEERKEZO".equals(ut.getStatus())) {
            Toast.makeText(getContext(), "Ez a kérelem már el lett bírálva.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Kérelem elbírálása");
        builder.setMessage(ut.getSofor().getNev() + " kérelme:\n" + ut.getIndulas() + " -> " + ut.getErkezes());

        builder.setPositiveButton("JÓVÁHAGYÁS", (dialog, which) -> frissitStatusz(ut, "JOVAHAGYOTT"));
        builder.setNegativeButton("ELUTASÍTÁS", (dialog, which) -> frissitStatusz(ut, "ELUTASITOTT"));
        builder.setNeutralButton("MÉGSE", null);

        builder.show();
    }

    private void frissitStatusz(Ut ut, String ujStatusz) {
        executor.execute(() -> {
            ut.setStatus(ujStatusz);
            db.utDao().update(ut); // Feltételezve, hogy van update metódus a DAO-ban

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Kérelem " + (ujStatusz.equals("JOVAHAGYOTT") ? "jóváhagyva" : "elutasítva"), Toast.LENGTH_SHORT).show();
                    frissitLista(); // Lista újratöltése
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        frissitLista(); // Visszalépéskor vagy fülváltáskor frissüljön
    }
}