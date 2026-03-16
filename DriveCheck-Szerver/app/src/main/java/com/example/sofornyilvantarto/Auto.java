package com.example.sofornyilvantarto.uj;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "autok")
public class Auto {

    @PrimaryKey
    @NonNull
    private String rendszam;
    private String tipus;
    private String statusz; // ÚJ MEZŐ: "ELERHETO" vagy "FOGLALT"

    public Auto(@NonNull String rendszam, String tipus, String statusz) {
        this.rendszam = rendszam;
        this.tipus = tipus;
        this.statusz = statusz;
    }

    // Üres konstruktor a Room-nak
    public Auto() {
        this.rendszam = "";
    }

    @NonNull
    public String getRendszam() {
        return rendszam;
    }

    public void setRendszam(@NonNull String rendszam) {
        this.rendszam = rendszam;
    }

    public String getTipus() {
        return tipus;
    }

    public void setTipus(String tipus) {
        this.tipus = tipus;
    }

    public String getStatusz() {
        return statusz;
    }

    public void setStatusz(String statusz) {
        this.statusz = statusz;
    }

    @Override
    public String toString() {
        return "Auto: " + tipus + " (Rendszam: " + rendszam + ")";
    }
}