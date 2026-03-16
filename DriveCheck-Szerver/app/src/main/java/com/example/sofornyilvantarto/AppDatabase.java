package com.example.sofornyilvantarto.uj;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

// Verziószám növelése 2-ről 3-ra, és az Auto.class hozzáadása az entitásokhoz
@Database(entities = {Ut.class, Auto.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract UtDao utDao();
    public abstract AutoDao autoDao(); // ÚJ: Az AutoDao csatlakoztatása

    // Singleton minta az adatbázis eléréséhez
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "sofor_nyilvantarto_db")
                    // Itt adjuk hozzá mindkét migrációs tervet
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build();
        }
        return instance;
    }

    /**
     * Migrációs terv: Az 1-es verzióról a 2-esre
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE utak ADD COLUMN status TEXT DEFAULT 'BEERKEZO'");
        }
    };

    /**
     * Migrációs terv: A 2-es verzióról a 3-asra
     * Létrehozza az 'autok' táblát és feltölti az alapértelmezett autókkal.
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Autok tábla létrehozása
            database.execSQL("CREATE TABLE IF NOT EXISTS `autok` (`rendszam` TEXT NOT NULL, `tipus` TEXT, `statusz` TEXT, PRIMARY KEY(`rendszam`))");

            // Alapértelmezett tesztautók beszúrása, hogy ne legyen üres a lista
            database.execSQL("INSERT INTO `autok` (`rendszam`, `tipus`, `statusz`) VALUES ('ABC-123', 'Toyota Corolla', 'ELERHETO')");
            database.execSQL("INSERT INTO `autok` (`rendszam`, `tipus`, `statusz`) VALUES ('XYZ-789', 'Skoda Octavia', 'ELERHETO')");
            database.execSQL("INSERT INTO `autok` (`rendszam`, `tipus`, `statusz`) VALUES ('LMN-456', 'Volkswagen Golf', 'ELERHETO')");
            database.execSQL("INSERT INTO `autok` (`rendszam`, `tipus`, `statusz`) VALUES ('DEF-001', 'Ford Transit', 'FOGLALT')");
            database.execSQL("INSERT INTO `autok` (`rendszam`, `tipus`, `statusz`) VALUES ('GHI-002', 'Renault Master', 'FOGLALT')");
        }
    };
}