package com.example.sofornyilvantarto;

import androidx.room.Dao;
import androidx.room.Delete; // Új import a törléshez
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface UtDao {

    @Insert
    void insert(Ut ut);

    // --- EZ AZ ÚJ METÓDUS: Egy adott bejegyzés törlése ---
    @Delete
    void delete(Ut ut);

    // ID szerint növekvő sorrend (a lista sorszámozásához kell)
    @Query("SELECT * FROM utak ORDER BY id ASC")
    List<Ut> getAllUtak();

    @Query("DELETE FROM utak")
    void deleteAll();

    // Szűrés ID szerint növekvő sorrendben
    @Query("SELECT * FROM utak WHERE " +
            "(:honapEv = '' OR honapEv = :honapEv) AND " +
            "(:nev = '' OR sofor_nev LIKE '%' || :nev || '%') AND " +
            "(:rendszam = '' OR auto_rendszam = :rendszam) " +
            "ORDER BY id ASC")
    List<Ut> szures(String honapEv, String nev, String rendszam);

    // Törlés ID tartomány alapján (ezt használja az AdminActivity)
    @Query("DELETE FROM utak WHERE id >= :idTol AND id <= :idIg")
    int deleteByIdInterval(int idTol, int idIg);
}