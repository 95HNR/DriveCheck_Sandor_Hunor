package com.example.sofornyilvantarto;

import androidx.annotation.NonNull;

public class OsmPlace {
    public String displayName; // A hely teljes neve (pl. Budapest, Magyarország)
    public String lat;         // Szélességi koordináta
    public String lon;         // Hosszúsági koordináta

    public OsmPlace(String displayName, String lat, String lon) {
        this.displayName = displayName;
        this.lat = lat;
        this.lon = lon;
    }

    // Ez a metódus határozza meg, mi jelenjen meg a legördülő listában gépeléskor
    @NonNull
    @Override
    public String toString() {
        return displayName;
    }
}