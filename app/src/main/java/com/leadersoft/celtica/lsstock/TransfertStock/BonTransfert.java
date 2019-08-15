package com.leadersoft.celtica.lsstock.TransfertStock;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.MyBD;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by celtica on 29/04/19.
 */

public class BonTransfert {
    public String id_bon,codebar_dep_src,nom_dep_src,codebar_dep_dest,nom_dep_dest,date_trans,code_emp;

    public BonTransfert(){

    }

    public BonTransfert(String id_bon, String codebar_dep_src, String nom_dep_src, String codebar_dep_dest, String nom_dep_dest,String date_trans) {
        this.id_bon = id_bon;
        this.codebar_dep_src = codebar_dep_src;
        this.nom_dep_src = nom_dep_src;
        this.codebar_dep_dest = codebar_dep_dest;
        this.nom_dep_dest = nom_dep_dest;
        this.date_trans=date_trans;
    }

    public BonTransfert(String id_bon, String codebar_dep_src, String codebar_dep_dest,String date_trans,String code_emp) {
        this.id_bon = id_bon;
        this.codebar_dep_src = codebar_dep_src;
        this.codebar_dep_dest = codebar_dep_dest;
        this.code_emp=code_emp;
        this.date_trans=date_trans;
    }

    public void validerTranfert(final String codebar_dep_dest){
        this.codebar_dep_dest=codebar_dep_dest;
        ArrayList<ProduitTransferé> produits=getProduitInBon();

        for (ProduitTransferé p : produits) {
            p.checkQtInDepotExiste(codebar_dep_src);
        }

        Accueil.BDsql.es.execute(new Runnable() {
            @Override
            public void run() {
                if (FaireTransfert.ExportErr.equals("")) {
                    Accueil.bd.write2("update bon_transfert set codebar_depot_dest=?,nom_depot_dest=?,date_transfert=strftime('%Y-%m-%d %H:%M','now','localtime') where id='" + id_bon + "'", new MyBD.SqlPrepState() {
                        @Override
                        public void putValue(SQLiteStatement stmt) {
                            stmt.bindString(1, codebar_dep_dest);
                            stmt.bindString(2, getDepotDestName());
                            stmt.execute();
                        }
                    });
                }
            }
        });


    }

    public String getDepotSrcName(){
        Cursor r=Accueil.bd.read2("select nom_dep from depot where codebar=?  ",new String[]{codebar_dep_src});
        if (r.moveToNext()){
            return r.getString(r.getColumnIndex("nom_dep"));
        }
        return "";
    }

    public String getDepotDestName(){
        Cursor r=Accueil.bd.read2("select nom_dep from depot where codebar=?  ",new String[]{codebar_dep_dest});
        if (r.moveToNext()){
            return r.getString(r.getColumnIndex("nom_dep"));
        }
        return "";
    }

    public ArrayList<ProduitTransferé> getProduitInBon(){
        ArrayList<ProduitTransferé> produits=new ArrayList<ProduitTransferé>();
        Cursor r=Accueil.bd.read("select * from produit_transferer where id_bon='"+id_bon+"'");
        while (r.moveToNext()){
            boolean isPackaging=false;
            if (r.getString(r.getColumnIndex("isPackaging")).equals("1"))
                isPackaging=true;
            produits.add(new ProduitTransferé(r.getString(r.getColumnIndex("codebar_pr")),r.getString(r.getColumnIndex("nom_pr")),r.getDouble(r.getColumnIndex("quantité")),isPackaging));
        }

        return produits;
    }

    public void changeState(String state){
        Accueil.bd.write("update bon_transfert set sync='1', etat='"+state+"'");
    }

    public void suppBon(){
        Accueil.bd.write("delete from  bon_transfert where  id='"+id_bon+"'");
    }

    public void exportéBon(String recordid){
        HashMap<Integer,String> datas=new HashMap<Integer,String> ();
        datas.put(1,Login.session.deviceId+"_"+id_bon);
        Accueil.BDsql.write2("insert into BonTransfertMobile(RECORDID,NUM_BON,CODEBARRE_DEPOT_SRC,CODEBARRE_DEPOT_DEST,DATE_BON,BLOCAGE) values('"+recordid+"',?,'"+codebar_dep_src+"','"+codebar_dep_dest+"',CAST("+date_trans+" as datetime ),'F')",datas,new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {
                Accueil.BDsql.transactErr=true;
                Synchronisation.ExportationErr=Synchronisation.ExportationErr+"-Erreur d insertion dans BonTransfertMobile: \n "+e.getMessage()+" \n";

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
