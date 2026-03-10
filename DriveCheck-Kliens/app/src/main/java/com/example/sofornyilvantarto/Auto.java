package com.example.sofornyilvantarto;

public class Auto {
    private String rendszam;
    private String tipus;

    public Auto(String rendszam, String tipus) {
        this.rendszam = rendszam;
        this.tipus = tipus;
    }

    // Ures konstruktor a Room-nak
    public Auto() { }

    public String getRendszam() {
        return rendszam;
    }

    public void setRendszam(String rendszam) {
        this.rendszam = rendszam;
    }

    public String getTipus() {
        return tipus;
    }

    public void setTipus(String tipus) {
        this.tipus = tipus;
    }

    @Override
    public String toString() {
        return "Auto: " + tipus + " (Rendszam: " + rendszam + ")";
    }
}