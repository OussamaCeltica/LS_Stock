package com.leadersoft.celtica.lsstock.Inventaire;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.MyBD;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;
import com.leadersoft.celtica.lsstock.TransfertStock.ProduitTransferé;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by celtica on 30/04/19.
 */

public class BonInventaire {
    public String id_bon,code_depot,nom_depot,date_bon,code_emp;
    public ETAT etat;

    public BonInventaire(String id_bon, String code_depot, String nom_depot, String date_bon, String code_emp) {
        this.id_bon = id_bon;
        this.code_depot = code_depot;
        this.nom_depot = nom_depot;
        this.date_bon = date_bon;
        this.code_emp = code_emp;
    }

    public BonInventaire(String id_bon, String code_depot, String nom_depot, String date_bon, String code_emp,ETAT e) {
        this.id_bon = id_bon;
        this.code_depot = code_depot;
        this.nom_depot = nom_depot;
        this.date_bon = date_bon;
        this.code_emp = code_emp;
        etat=e;
    }

    public BonInventaire(String code_depot) {
        this.code_depot=code_depot;
        int id=1;
        Cursor r= Accueil.bd.read("select id_inventaire from bon_last_id  order by id_inventaire desc limit 1");
        if(r.moveToNext()){
            id=r.getInt(r.getColumnIndex("id_inventaire"))+1;
        }
        id_bon=id+"";
    }

    public String getDepotName(){
        Cursor r=Accueil.bd.read2("select nom_dep from depot where codebar=?  ",new String[]{code_depot});
        if (r.moveToNext()){
            return r.getString(r.getColumnIndex("nom_dep"));
        }
        return "";
    }

    public ArrayList<ProduitInventaire> getProduitInBon(){
        ArrayList<ProduitInventaire> produits=new ArrayList<ProduitInventaire>();
        Cursor r=Accueil.bd.read("select * from produit_bon_inventaire where id_bon='"+id_bon+"'");
        while (r.moveToNext()){
            boolean isPackaging=false;
            if (r.getString(r.getColumnIndex("isPackaging")).equals("1"))
                isPackaging=true;
            produits.add(new ProduitInventaire(r.getString(r.getColumnIndex("codebar_pr")),r.getString(r.getColumnIndex("nom_pr")),r.getDouble(r.getColumnIndex("quantité")),isPackaging));
        }

        return produits;
    }
    public void addToBD(){
        Accueil.bd.write2("insert into bon_inventaire(id_bon,code_emp,code_depot,nom_depot,date_inventaire,sync,etat ) values('"+id_bon+"','"+ Login.session.employe.code_emp+"',?,?,strftime('%Y-%m-%d %H:%M','now','localtime'),'0','en cours')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,code_depot);
                stmt.bindString(2,getDepotName());
                stmt.execute();
            }
        });
        updateLastBonId();

    }

    public void updateLastBonId(){
        Accueil.bd.write("update bon_last_id set id_inventaire='"+id_bon+"'  ");
    }

    public void changeState(String state){
        Accueil.bd.write("update bon_inventaire set sync='1', etat='"+state+"'");
    }

    public void exportéBon(String recordid){

        HashMap<Integer,String> datas=new HashMap<Integer,String> ();
        datas.put(1,Login.session.deviceId+"_"+id_bon);
        Accueil.BDsql.write2("insert into BonTransfertMobile(RECORDID,NUM_BON,CODEBARRE_DEPOT_SRC,DATE_BON,BLOCAGE) values('"+recordid+"',?,'"+code_depot+"',CAST("+date_bon+" as datetime ),'F')", datas,new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {
                Accueil.BDsql.transactErr=true;
                Synchronisation.ExportationErr=Synchronisation.ExportationErr+"-Erreur d insertion dans BonInventaireMobile: \n "+e.getMessage()+" \n";

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {


            }
        });

    }
}
