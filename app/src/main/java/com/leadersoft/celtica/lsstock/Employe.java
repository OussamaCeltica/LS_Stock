package com.leadersoft.celtica.lsstock;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by celtica on 19/04/19.
 */

public class Employe {
   public String code_emp,nom_emp,codebar,Oid;

    public static boolean SqliteErr=false;

    public Employe(String code_emp, String nom_emp,String codebar,String Oid) {
        this.code_emp = code_emp;
        this.nom_emp = nom_emp;
        this.codebar=codebar;
        this.Oid=Oid;
    }

    public Employe(String code_emp, String nom_emp) {
        this.code_emp = code_emp;
        this.nom_emp = nom_emp;
    }

    public Employe(String code_emp) {
        this.code_emp = code_emp;
        this.nom_emp = getEmpName();
    }

    public String getEmpName(){
        Cursor r=Accueil.bd.read("select * from employe where code_emp='"+code_emp+"'");
        if (r.moveToNext()){
            return r.getString(r.getColumnIndex("nom_emp"));
        }
        return "";
    }

    public void addToBD(){

        try {
            Accueil.bd.write2("insert into employe (code_emp,nom_emp,codebar,Oid) values('" + code_emp + "',?,'" + codebar + "','"+Oid+"')", new MyBD.SqlPrepState() {
                @Override
                public void putValue(SQLiteStatement stmt) {
                    stmt.bindString(1,nom_emp+"");
                    stmt.execute();
                }
            });

        } catch (android.database.SQLException e) {
              Synchronisation.ImportationErr = Synchronisation.ImportationErr + "-Erreur d'insertion d'empolyé qui a le code:"+code_emp+" \n" + e.getMessage() + "\n";

        }
    }
}
