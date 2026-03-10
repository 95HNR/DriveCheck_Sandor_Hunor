package com.example.sofornyilvantarto;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UtAdapter extends RecyclerView.Adapter<UtAdapter.UtViewHolder> {

    private List<Ut> utak = new ArrayList<>();

    public void setUtak(List<Ut> ujUtak) {
        this.utak = new ArrayList<>();
        if (ujUtak != null) {
            for (Ut ut : ujUtak) {
                // Csak a valódi utakat adjuk hozzá, a "SABLON"-okat nem
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
        try {
            Ut ut = utak.get(position);
            if (ut == null) return;

            // 1. Dátum és ID megjelenítése
            String datum = (ut.getHonapEv() != null) ? ut.getHonapEv() : "Nincs dátum";
            holder.tvIdDatum.setText("#" + ut.getId() + " - " + datum);

            // 2. Sofőr és Autó (Null-safe kezelés)
            String soforNev = "Ismeretlen";
            if (ut.getSofor() != null && ut.getSofor().getNev() != null) {
                soforNev = ut.getSofor().getNev();
            }

            String autoInfo = "Ismeretlen autó";
            if (ut.getAuto() != null) {
                String rendszam = (ut.getAuto().getRendszam() != null) ? ut.getAuto().getRendszam() : "???";
                String tipus = (ut.getAuto().getTipus() != null) ? ut.getAuto().getTipus() : "Ismeretlen típus";
                autoInfo = rendszam + " (" + tipus + ")";
            }
            holder.tvSoforAuto.setText("Sofőr: " + soforNev + " | Autó: " + autoInfo);

            // 3. Útvonal
            String indulas = (ut.getIndulas() != null) ? ut.getIndulas() : "-";
            String erkezes = (ut.getErkezes() != null) ? ut.getErkezes() : "-";
            holder.tvUtvonal.setText("Útvonal: " + indulas + " -> " + erkezes);

            // 4. Statisztika
            holder.tvAdatok.setText("Táv: " + ut.getTavolsag() + " km | Költség: " + ut.getKoltseg() + " EUR | Üzemanyag: " + ut.getFogyasztas() + " L");

            // 5. Állapot és Színezés (Biztonságos CardView kezelés)
            String status = (ut.getStatus() != null) ? ut.getStatus() : "BEERKEZO";

            if (holder.cardView != null) {
                switch (status) {
                    case "JOVAHAGYOTT":
                        holder.cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                        holder.tvStatus.setText("ÁLLAPOT: JÓVÁHAGYVA");
                        holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                        break;
                    case "ELUTASITOTT":
                        holder.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                        holder.tvStatus.setText("ÁLLAPOT: ELUTASÍTVA");
                        holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
                        break;
                    default:
                        holder.cardView.setCardBackgroundColor(Color.WHITE);
                        holder.tvStatus.setText("ÁLLAPOT: BEÉRKEZETT");
                        holder.tvStatus.setTextColor(Color.DKGRAY);
                        break;
                }
            }
        } catch (Exception e) {
            // Ha bármi hiba történne egy elem kirajzolásakor, az app nem áll le
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return utak.size();
    }

    static class UtViewHolder extends RecyclerView.ViewHolder {
        TextView tvIdDatum, tvSoforAuto, tvUtvonal, tvAdatok, tvStatus;
        CardView cardView; // Általános CardView-ra cserélve a stabilitásért

        public UtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIdDatum = itemView.findViewById(R.id.tv_item_id_datum);
            tvSoforAuto = itemView.findViewById(R.id.tv_item_sofor_auto);
            tvUtvonal = itemView.findViewById(R.id.tv_item_utvonal);
            tvAdatok = itemView.findViewById(R.id.tv_item_tav_koltseg);
            tvStatus = itemView.findViewById(R.id.tv_item_status);

            // Biztonságos bekötés: ha az XML-ben nem MaterialCardView van, akkor is működik
            if (itemView instanceof CardView) {
                cardView = (CardView) itemView;
            }
        }
    }
}