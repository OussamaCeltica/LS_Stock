package com.leadersoft.celtica.lsstock.Preparations;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.MyBD;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by celtica on 19/04/19.
 */

public class BonPreparation {
    public String code_bon,date_bon,Oid_bon,nom_clt;
    public ETAT etat;


    //Oid je l utilise que pour synchroniser ..

    public BonPreparation(String code_bon, String date_bon, String oid_bon) {
        this.code_bon = code_bon;
        this.date_bon = date_bon;
        Oid_bon = oid_bon;
    }

    public BonPreparation(String code_bon, String date_bon, String oid_bon,String nom_clt,ETAT e) {
        this.code_bon = code_bon;
        this.date_bon = date_bon;
        Oid_bon = oid_bon;
        this.nom_clt=nom_clt;
        etat=e;
    }



    public ArrayList<ProduitDansBon> getProduitAPreparer(){
        ArrayList<ProduitDansBon> produits=new ArrayList<ProduitDansBon>();
        Cursor r= Accueil.bd.read("select * from produit_preparer where code_bon='"+code_bon+"' ");
        while (r.moveToNext()){
            Log.e("ddd","Kayen");
            produits.add(new ProduitDansBon(r.getString(r.getColumnIndex("codebar_pr")),r.getString(r.getColumnIndex("nom_pr")),r.getString(r.getColumnIndex("codebar_depot")),r.getString(r.getColumnIndex("nom_depot")),r.getDouble(r.getColumnIndex("final_qt")),r.getDouble(r.getColumnIndex("current_qt"))));
        }

        return produits;
    }

    public ArrayList<ProduitDansBon> getProduitFromDepot(String code_depot){
        ArrayList<ProduitDansBon> produits=new ArrayList<ProduitDansBon>();
        Cursor r= Accueil.bd.read("select * from produit_preparer where code_bon='"+code_bon+"' and codebar_depot='"+code_depot+"' ");
        while (r.moveToNext()){
            Log.e("ddd","Kayen");
            produits.add(new ProduitDansBon(r.getString(r.getColumnIndex("codebar_pr")),r.getString(r.getColumnIndex("nom_pr")),r.getString(r.getColumnIndex("codebar_depot")),r.getString(r.getColumnIndex("nom_depot")),r.getDouble(r.getColumnIndex("final_qt")),r.getDouble(r.getColumnIndex("current_qt"))));
        }

        return produits;
    }

    public boolean estPréparé(){
        Cursor r=Accueil.bd.read("select * from produit_preparer where code_bon='"+code_bon+"' and current_qt != '0' limit 1 ");
        if (r.moveToNext()) return true;
        else return false;
    }

    public ArrayList<ProduitDansBon> getProduitFromDepotOrByName(String hint){
        hint="%"+hint+"%";
        ArrayList<ProduitDansBon> produits=new ArrayList<ProduitDansBon>();
        Cursor r= Accueil.bd.read2("select * from produit_preparer where nom_pr Like ? or nom_depot LIKE ? ",new String[]{hint,hint});
        while (r.moveToNext()){
            produits.add(new ProduitDansBon(r.getString(r.getColumnIndex("codebar_pr")),r.getString(r.getColumnIndex("nom_pr")),r.getString(r.getColumnIndex("codebar_depot")),r.getString(r.getColumnIndex("nom_depot")),r.getDouble(r.getColumnIndex("final_qt")),r.getDouble(r.getColumnIndex("current_qt"))));
        }

        return produits;
    }

    public void validerBon(){
        Accueil.bd.write("update bon_preparation set valider='1' where code_bon='"+code_bon+"'");
    }

    public void suppPrPreparer(){
        Accueil.bd.write("delete from produit_preparer where code_bon='"+code_bon+"'");
    }

    public void addToBD(){
        Accueil.bd.write2("replace into bon_preparation (Oid,code_bon,date_bon,nom_clt,sync,valider) values(?,?,'"+date_bon+"',?,'0','0')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                try {
                    stmt.bindString(1, Oid_bon + "");
                    stmt.bindString(2, code_bon + "");
                    stmt.bindString(3, nom_clt + "");
                    stmt.execute();
                }
                catch (SQLiteException e2){
                    Synchronisation.ImportationErr=Synchronisation.ImportationErr+"Erreur d insertion des bons de préparations: \n"+e2.getMessage()+" \n";
                }

            }
        });
    }

    public void exportéBon(){
        String formatExport="";

        ArrayList<ProduitDansBon> produits=getProduitAPreparer();

        for (ProduitDansBon pr: produits) {
            formatExport=formatExport+String.format( "%s / %s / %s / %2f ;",pr.codebar_dep,"",pr.codebar,pr.current_qt);

        }

        /*
        for (ProduitDansBon pr: produits) {
            formatExport=formatExport+pr.codebar_dep+";";
            int i=0;
            while (i != pr.current_qt){
                formatExport=formatExport+pr.codebar_pr+";";
                i++;
            }

        }*/

        HashMap<Integer, String> data=new HashMap<Integer, String>();
        data.put(1,formatExport);
        data.put(2,Oid_bon);
        Accueil.BDsql.write2("update InventoryPreparationNote set temporaryInputZone=? where Oid=?",data ,new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {
                Synchronisation.ExportationErr=Synchronisation.ExportationErr+"-Erreur d'exportations des Préparations: \n code_bon= "+code_bon+" | "+e.getMessage()+" \n";

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                Accueil.bd.write2("update bon_preparation set sync='1' where code_bon=?", new MyBD.SqlPrepState() {
                    @Override
                    public void putValue(SQLiteStatement stmt) {
                        stmt.bindString(1,code_bon+"");
                        stmt.execute();
                    }
                });
            }
        });
    }


}
