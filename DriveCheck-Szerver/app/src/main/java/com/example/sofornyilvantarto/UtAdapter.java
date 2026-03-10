package com.example.sofornyilvantarto.uj;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UtAdapter extends RecyclerView.Adapter<UtAdapter.UtViewHolder> {

    private List<Ut> utak = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(Ut ut);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setUtak(List<Ut> ujUtak) {
        this.utak = new ArrayList<>();
        if (ujUtak != null) {
            for (Ut ut : ujUtak) {
                if (ut != null && !"SABLON".equals(ut.getStatus())) {
                    this.utak.add(ut);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_ut, parent, false);
        return new UtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UtViewHolder holder, int position) {
        Ut ut = utak.get(position);
        if (ut == null) return;

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(ut);
        });

        // ADATOK BEÁLLÍTÁSA ÉS SZÍNEZÉSE (Fix fekete a láthatóságért)
        holder.tvSorszam.setText("ID: " + ut.getId());
        holder.tvSorszam.setTextColor(Color.BLACK);

        holder.tvHonapEv.setText(ut.getHonapEv() != null ? ut.getHonapEv() : "-");
        holder.tvHonapEv.setTextColor(Color.BLACK);

        String sofor = (ut.getSofor() != null) ? ut.getSofor().getNev() : "Ismeretlen";
        String auto = (ut.getAuto() != null) ? ut.getAuto().getRendszam() + " (" + ut.getAuto().getTipus() + ")" : "Nincs autó";
        holder.tvSoforAuto.setText("Sofőr: " + sofor + "\nAutó: " + auto);
        holder.tvSoforAuto.setTextColor(Color.BLACK);

        holder.tvUticel.setText("Útvonal: " + ut.getIndulas() + " -> " + ut.getErkezes());
        holder.tvUticel.setTextColor(Color.DKGRAY);

        holder.tvAdatok.setText(String.format(Locale.US, "Táv: %.1f km | Költség: %.1f EUR | Üzemanyag: %.1f L",
                ut.getTavolsag(), ut.getKoltseg(), ut.getFogyasztas()));
        holder.tvAdatok.setTextColor(Color.BLACK);

        if (ut.getTavolsag() > 0) {
            double atlag = (ut.getFogyasztas() / ut.getTavolsag()) * 100;
            holder.tvAtlag.setText(String.format(Locale.US, "Átlag: %.2f L/100km", atlag));
        } else {
            holder.tvAtlag.setText("Átlag: -");
        }
        holder.tvAtlag.setTextColor(Color.BLACK);

        // KÁRTYA SZÍNEZÉSE STATUSZ ALAPJÁN
        String status = (ut.getStatus() != null) ? ut.getStatus() : "BEERKEZO";
        switch (status) {
            case "JOVAHAGYOTT":
                holder.cardView.setCardBackgroundColor(Color.parseColor("#C8E6C9")); // Halványzöld
                break;
            case "ELUTASITOTT":
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFCDD2")); // Halványpiros
                break;
            default:
                holder.cardView.setCardBackgroundColor(Color.WHITE); // Alapértelmezett fehér
                break;
        }
    }

    @Override
    public int getItemCount() {
        return utak.size();
    }

    static class UtViewHolder extends RecyclerView.ViewHolder {
        TextView tvSorszam, tvHonapEv, tvSoforAuto, tvUticel, tvAdatok, tvAtlag;
        MaterialCardView cardView;

        public UtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSorszam = itemView.findViewById(R.id.tv_sorszam);
            tvHonapEv = itemView.findViewById(R.id.tv_honap_ev);
            tvSoforAuto = itemView.findViewById(R.id.tv_sofor_auto);
            tvUticel = itemView.findViewById(R.id.tv_uticel);
            tvAdatok = itemView.findViewById(R.id.tv_adatok);
            tvAtlag = itemView.findViewById(R.id.tv_atlagfogyasztas);

            if (itemView instanceof MaterialCardView) {
                cardView = (MaterialCardView) itemView;
            }
        }
    }
}
