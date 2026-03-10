package com.example.sofornyilvantarto.uj;
public class Sofor {
    private String nev;

    public Sofor(String nev) {
        this.nev = nev;
    }

    // Üres konstruktorra szüksége van a Room-nak
    // (a JSON feldolgozóval ellentétben)
    public Sofor() { }

    public String getNev() {
        return nev;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    @Override
    public String toString() {
        return "Sofor: " + nev;
    }
}