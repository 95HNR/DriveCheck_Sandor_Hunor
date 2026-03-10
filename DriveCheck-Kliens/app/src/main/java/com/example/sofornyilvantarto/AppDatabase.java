package com.example.sofornyilvantarto;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Ez köti össze az entitásokat (Ut) és a DAO-t (UtDao).
// A version = 1 azt jelenti, hogy ez az adatbázis-séma 1-es verziója.
@Database(entities = {Ut.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Ez a függvény adja majd vissza a DAO-t, amin keresztül parancsokat adunk ki.
    public abstract UtDao utDao();

    // Ezt a részt "Singleton" mintának hívják.
    // Biztosítja, hogy az egész alkalmazásban csak EGY adatbázis-kapcsolat
    // létezzen egyszerre, elkerülve a hibákat és a memóriapazarlást.
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "sofor_adatbazis")
                            // FONTOS: Ez egy egyszerűsítés a te konzolos
                            // programodhoz hasonló működésért. Éles alkalmazásban
                            // ezt nem szabad használni, mert a fő szálat blokkolhatja!
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}