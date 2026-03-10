package com.example.sofornyilvantarto.uj;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

// Verziószám növelése 1-ről 2-re
@Database(entities = {Ut.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract UtDao utDao();

    // Singleton minta az adatbázis eléréséhez
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "sofor_nyilvantarto_db")
                    // Itt adjuk hozzá a migrációs tervet
                    .addMigrations(MIGRATION_1_2)
                    // Ha nem akarsz migrációval bajlódni és nem baj ha törlődik az adat:
                    // .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    /**
     * Migrációs terv: Az 1-es verzióról a 2-esre váltáskor
     * hozzáadjuk a 'status' oszlopot az 'utak' táblához.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // SQL parancs az új oszlop hozzáadásához, alapértelmezett értékkel
            database.execSQL("ALTER TABLE utak ADD COLUMN status TEXT DEFAULT 'BEERKEZO'");
        }
    };
}