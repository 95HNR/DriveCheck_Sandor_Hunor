package com.example.sofornyilvantarto.uj;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface UtDao {

    @Insert
    void insert(Ut ut);

    @Update
    void update(Ut ut);

    @Delete
    void delete(Ut ut);

    @Query("SELECT * FROM utak ORDER BY id ASC")
    List<Ut> getAllUtak();

    @Query("SELECT * FROM utak WHERE status = :statusz ORDER BY id DESC")
    List<Ut> getUtakByStatus(String statusz);

    @Query("DELETE FROM utak")
    void deleteAll();

    // --- EZ A RÉSZ HIÁNYZOTT: Ez nullázza le a sorszámlálót az adatbázisban ---
    @Query("DELETE FROM sqlite_sequence WHERE name = 'utak'")
    void resetSequence();

    @Query("DELETE FROM utak WHERE id BETWEEN :idTol AND :idIg")
    int deleteByIdInterval(int idTol, int idIg);

    @Query("SELECT * FROM utak WHERE " +
            "(:datum = '' OR honapEv LIKE :datum || '%') AND " +
            "(:sofor = '' OR sofor_nev LIKE '%' || :sofor || '%') AND " +
            "(:rendszam = '' OR auto_rendszam LIKE '%' || :rendszam || '%') " +
            "ORDER BY id DESC")
    List<Ut> szures(String datum, String sofor, String rendszam);

    @Query("SELECT * FROM utak")
    List<Ut> getAllSync();
}