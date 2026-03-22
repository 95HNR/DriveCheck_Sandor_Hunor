package com.example.sofornyilvantarto.uj;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AutoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Auto auto);

    @Query("SELECT * FROM autok WHERE statusz = :statusz ORDER BY rendszam ASC")
    List<Auto> getAutokByStatus(String statusz);

    @Query("SELECT * FROM autok ORDER BY rendszam ASC")
    List<Auto> getAllAutok();

    @Query("DELETE FROM autok")
    void deleteAll();

    // ÚJ METÓDUS: Célzott törlés rendszám alapján az "autok" táblából
    @Query("DELETE FROM autok WHERE rendszam = :rendszam")
    void deleteByRendszam(String rendszam);
}