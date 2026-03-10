package com.example.sofornyilvantarto; // Figyelj a package névre a saját projektjedben!

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "utak")
public class Ut {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @Embedded(prefix = "sofor_")
    private Sofor sofor;

    @Embedded(prefix = "auto_")
    private Auto auto;

    private String indulas;
    private String erkezes;
    private double tavolsag;
    private double koltseg;
    private double fogyasztas;
    private String honapEv;
    private String status;

    // Üres konstruktor a Room-nak és a Gson-nak
    public Ut() {}

    // Teljes konstruktor a Kliens oldali beküldéshez
    public Ut(Sofor sofor, Auto auto, String indulas, String erkezes, double tavolsag, double koltseg, double fogyasztas, String honapEv, String status) {
        this.sofor = sofor;
        this.auto = auto;
        this.indulas = indulas;
        this.erkezes = erkezes;
        this.tavolsag = tavolsag;
        this.koltseg = koltseg;
        this.fogyasztas = fogyasztas;
        this.honapEv = honapEv;
        this.status = status;
    }

    // Getterek és Setterek (MINDET hagyd benne!)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Sofor getSofor() { return sofor; }
    public void setSofor(Sofor sofor) { this.sofor = sofor; }
    public Auto getAuto() { return auto; }
    public void setAuto(Auto auto) { this.auto = auto; }
    public String getIndulas() { return indulas; }
    public void setIndulas(String indulas) { this.indulas = indulas; }
    public String getErkezes() { return erkezes; }
    public void setErkezes(String erkezes) { this.erkezes = erkezes; }
    public double getTavolsag() { return tavolsag; }
    public void setTavolsag(double tavolsag) { this.tavolsag = tavolsag; }
    public double getKoltseg() { return koltseg; }
    public void setKoltseg(double koltseg) { this.koltseg = koltseg; }
    public double getFogyasztas() { return fogyasztas; }
    public void setFogyasztas(double fogyasztas) { this.fogyasztas = fogyasztas; }
    public String getHonapEv() { return honapEv; }
    public void setHonapEv(String honapEv) { this.honapEv = honapEv; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}