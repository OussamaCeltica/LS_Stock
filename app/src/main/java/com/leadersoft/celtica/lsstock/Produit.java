package com.leadersoft.celtica.lsstock;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by celtica on 28/04/19.
 */

public class Produit {
    public String codebar,nom,carton_code;
    public boolean isPackaging;
    public double qt_carton;
    public static boolean SqliteErr=false;

    public Produit(String codebar, String nom) {
        this.codebar = codebar;
        this.nom = nom;
        existe();
    }

    public Produit(String codebar, String nom,String carton_code) {
        this.codebar = codebar;
        this.nom = nom;
        this.carton_code=carton_code;
    }

    public Produit(String codebar, String nom,String carton_code,double qt_carton) {
        this.codebar = codebar;
        this.nom = nom;
        this.carton_code=carton_code;
        this.qt_carton=qt_carton;
    }

    public Produit(String codebar) {
        this.codebar = codebar;

    }

    public boolean existe(){
        Cursor r=Accueil.bd.read2("select * from produit where codebar=? or defaultCarton_codebar=?",new String[]{codebar+"",codebar+""});
        if(r.moveToNext()){
            this.nom=r.getString(r.getColumnIndex("nom_pr"));
            this.qt_carton=r.getDouble(r.getColumnIndex("qt_carton"));
            if(r.getString(r.getColumnIndex("defaultCarton_codebar")).equals(codebar)){
                isPackaging=true;
                codebar=r.getString(r.getColumnIndex("codebar"));
            }else {
                isPackaging=false;
            }
            return true;
        }
        return false;
    }

    public void addToBD(){
        //Log.e("prr", r.getString("codebar") + " / " + r.getString("name"));
        try {
            Accueil.bd.write2("insert into produit(codebar,nom_pr,defaultCarton_codebar,qt_carton) values('" + codebar + "',?,'"+carton_code+"','"+qt_carton+"')", new MyBD.SqlPrepState() {
                @Override
                public void putValue(SQLiteStatement stmt) {
                    stmt.bindString(1,nom+"");
                    stmt.execute();
                }
            });
        } catch (android.database.SQLException e) {
            if (!SqliteErr) {
                SqliteErr = true;
                Synchronisation.ImportationErr = Synchronisation.ImportationErr + "-Erreur d'insertion des produits: \n code produit:" + codebar + " \n " + e.getMessage() + "\n";

            } else {
                Synchronisation.ImportationErr = Synchronisation.ImportationErr + "code produit:" + codebar + "\n" + e.getMessage() + " \n";
            }
        }
    }
}
