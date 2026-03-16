package com.example.sofornyilvantarto;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AutoAdapter extends RecyclerView.Adapter<AutoAdapter.AutoViewHolder> {

    private List<Auto> autoLista = new ArrayList<>();

    // Frissíti a listát az új adatokkal
    public void setAutok(List<Auto> autok) {
        this.autoLista = autok;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Csatoljuk az előbb létrehozott kártya dizájnt (list_item_auto.xml)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_auto, parent, false);
        return new AutoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AutoViewHolder holder, int position) {
        Auto aktualisAuto = autoLista.get(position);

        // Beállítjuk a szövegeket a modelled alapján
        holder.tvTipus.setText(aktualisAuto.getTipus());
        holder.tvRendszam.setText(aktualisAuto.getRendszam());
    }

    @Override
    public int getItemCount() {
        return autoLista.size();
    }

    // A kártyán lévő elemek (TextView-k) megkeresése
    static class AutoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTipus;
        private TextView tvRendszam;

        public AutoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipus = itemView.findViewById(R.id.tv_auto_tipus);
            tvRendszam = itemView.findViewById(R.id.tv_auto_rendszam);
        }
    }
}