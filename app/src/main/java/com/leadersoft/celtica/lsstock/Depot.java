package com.leadersoft.celtica.lsstock;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import java.sql.SQLException;

/**
 * Created by celtica on 21/04/19.
 */

public class Depot {
    public String code_dep,nom_dep;

    public Depot(String code_dep, String nom_dep) {
        this.code_dep = code_dep;
        this.nom_dep = nom_dep;
    }

    public Depot(String code_dep) {
        this.code_dep = code_dep;
        this.nom_dep=getNomDepot();
    }


    public String getNomDepot() {
        Cursor r=Accueil.bd.read2("select * from depot where codebar=?",new String[]{code_dep});
        if (r.moveToNext()){
            return r.getString(r.getColumnIndex("nom_dep"));
        }

        return "";

    }

    public boolean existe(){
        Cursor r=Accueil.bd.read2("select * from depot where codebar=?",new String[]{code_dep});
        if (r.moveToNext()){
            nom_dep=r.getString(r.getColumnIndex("nom_dep"));
            return true;
        }

        return false;
    }

    public void addToBD(){
        Accueil.bd.write2("insert into depot (codebar,nom_dep) values('"+code_dep+"',?)", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                try{
                    stmt.bindString(1,nom_dep+"");
                    stmt.execute();
                }catch (android.database.SQLException e){
                    Synchronisation.ImportationErr=Synchronisation.ImportationErr+"-Erreur d insertion des dépôts: \n"+e.getMessage();

                }
            }
        });
    }
}
