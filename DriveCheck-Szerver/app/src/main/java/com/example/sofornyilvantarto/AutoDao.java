package com.example.sofornyilvantarto.uj;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AutoDao {

    // Új autó beszúrása (ha már létezik a rendszám, akkor felülírja a régit)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Auto auto);

    // Lekérdezi az összes autót a státusza alapján (ELERHETO vagy FOGLALT)
    @Query("SELECT * FROM autok WHERE statusz = :statusz ORDER BY rendszam ASC")
    List<Auto> getAutokByStatus(String statusz);

    // Lekérdezi a teljes autóparkot
    @Query("SELECT * FROM autok ORDER BY rendszam ASC")
    List<Auto> getAllAutok();

    // Törli az összes autót az adatbázisból (pl. teljes törlés/reset esetén)
    @Query("DELETE FROM autok")
    void deleteAll();
}