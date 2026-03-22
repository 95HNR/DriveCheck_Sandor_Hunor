package com.example.sofornyilvantarto.uj; // Ha a szerver oldalon vagy, a végére írd oda az .uj-at

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UtAdapter extends RecyclerView.Adapter<UtAdapter.UtViewHolder> {

    private List<Ut> utak = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Ut ut);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setUtak(List<Ut> ujUtak) {
        this.utak = new ArrayList<>();
        if (ujUtak != null) {
            for (Ut ut : ujUtak) {
                // Csak a nem sablon bejegyzéseket jelenítjük meg
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

        // 1. ID és Dátum
        String datum = (ut.getHonapEv() != null) ? ut.getHonapEv() : "-";
        holder.tvIdDatum.setText("#" + ut.getId() + " - " + datum);

        // 2. Sofőr és Autó (Típussal kiegészítve)
        String soforNev = (ut.getSofor() != null) ? ut.getSofor().getNev() : "Ismeretlen";
        String autoInfo = "Nincs autó";
        if (ut.getAuto() != null) {
            String tipus = ut.getAuto().getTipus() != null ? ut.getAuto().getTipus() : "Ismeretlen típus";
            String rendszam = ut.getAuto().getRendszam() != null ? ut.getAuto().getRendszam() : "???";
            autoInfo = tipus + " (" + rendszam + ")";
        }
        holder.tvSoforAuto.setText("Sofőr: " + soforNev + " | Autó: " + autoInfo);

        // 3. Útvonal és Távolság
        holder.tvUtvonalTav.setText("Úticél: " + ut.getIndulas() + " -> " + ut.getErkezes() + " | Táv: " + ut.getTavolsag() + " km");

        // 4. Fogyasztás és Átlag
        double atlag = (ut.getTavolsag() > 0) ? (ut.getFogyasztas() / ut.getTavolsag()) * 100 : 0;
        holder.tvFogyasztasAtlag.setText(String.format(Locale.US, "Fogyasztás: %.1f L | Átlag: %.2f L/100km",
                ut.getFogyasztas(), atlag));

        // 5. Költség
        holder.tvKoltseg.setText("Költség: " + ut.getKoltseg() + " EUR");

        // Státusz beállítása és színezése
        String status = (ut.getStatus() != null) ? ut.getStatus() : "BEERKEZO";
        switch (status) {
            case "JOVAHAGYOTT":
                holder.tvStatus.setText("JÓVÁHAGYVA");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Zöld
                break;
            case "ELUTASITOTT":
                holder.tvStatus.setText("ELUTASÍTVA");
                holder.tvStatus.setTextColor(Color.parseColor("#E53935")); // Piros
                break;
            default:
                holder.tvStatus.setText("ELBÍRÁLÁS ALATT");
                holder.tvStatus.setTextColor(holder.defaultStatusColor); // Téma szerinti alapértelmezett
                break;
        }
    }

    @Override
    public int getItemCount() {
        return utak.size();
    }

    static class UtViewHolder extends RecyclerView.ViewHolder {
        TextView tvIdDatum, tvStatus, tvSoforAuto, tvUtvonalTav, tvFogyasztasAtlag, tvKoltseg;
        int defaultStatusColor;

        public UtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIdDatum = itemView.findViewById(R.id.tv_item_id_datum);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
            tvSoforAuto = itemView.findViewById(R.id.tv_item_sofor_auto);
            tvUtvonalTav = itemView.findViewById(R.id.tv_item_utvonal_tav);
            tvFogyasztasAtlag = itemView.findViewById(R.id.tv_item_fogyasztas_atlag);
            tvKoltseg = itemView.findViewById(R.id.tv_item_koltseg);

            // Eredeti szövegszín mentése
            defaultStatusColor = tvStatus.getTextColors().getDefaultColor();
        }
    }
}