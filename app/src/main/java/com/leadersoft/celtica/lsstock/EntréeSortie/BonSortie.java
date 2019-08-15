package com.leadersoft.celtica.lsstock.EntréeSortie;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.Bon;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.MyBD;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class BonSortie extends Bon {


    public String code_clt,nom_clt;
    private String heure;

    public BonSortie(){
        id_bon=getLastExportId()+"";
    }

    public BonSortie(String id_bon, String date_bon, String code_clt,String nom_clt,ETAT etat) {
        this.id_bon = id_bon;
        this.date_bon = date_bon;
        this.etat = etat;
        this.code_clt=code_clt;
        this.nom_clt=nom_clt;
    }

    //pour l exportation ..
    public BonSortie(String id_bon, String date_bon,String heure, String code_clt,String nom_clt,ETAT etat) {
        this.id_bon = id_bon;
        this.date_bon = date_bon;
        this.etat = etat;
        this.code_clt=code_clt;
        this.nom_clt=nom_clt;
        this.heure=heure;
    }

    public BonSortie(String code_clt,String nom_clt) {
        this.code_clt = code_clt;
        this.nom_clt = nom_clt;
        id_bon=getLastExportId()+"";
    }



    public ArrayList<ProduitSortie> getProduitSortie(){
        ArrayList<ProduitSortie> produits=new ArrayList<>();
        Cursor r= Accueil.bd.read("select * from produit_sortie where code_bon='"+id_bon+"'");
        while (r.moveToNext()){
            produits.add(new ProduitSortie(r.getString(r.getColumnIndex("codebar_pr")),r.getString(r.getColumnIndex("nom_pr")),r.getString(r.getColumnIndex("codebar_depot")),r.getString(r.getColumnIndex("nom_depot")),r.getDouble(r.getColumnIndex("qt")),ETAT.VALIDÉ,r.getString(r.getColumnIndex("isPackaging")).equals("1")));
        }
        return produits;
    }

    public int getLastExportId(){
        int i=1;
        Cursor r=Accueil.bd.read("select id_sortie from bon_last_id");
        if(r.moveToNext()) i=r.getInt(r.getColumnIndex("id_sortie"))+1;
        id_bon=i+"";

        return i;

    }

    public void updateLastId(){
        Accueil.bd.write("update bon_last_id set id_sortie='"+id_bon+"'");
    }

    public void addToBD(){
        Accueil.bd.write2("insert into bon_sortie (code_bon,date_bon,code_emp,code_clt,nom_clt,etat,sync) values('"+id_bon+"',strftime('%Y-%m-%d %H:%M','now','localtime'),?,?,?,'validé','0')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1, Login.session.employe.code_emp+"");
                stmt.bindString(2, code_clt+"");
                stmt.bindString(3, nom_clt+"");
                stmt.execute();
            }
        });
        updateLastId();

    }

    public void changeState(String etat){
        Accueil.bd.write("update bon_sortie set sync='1', etat='"+etat+"'");
    }

    public void exportéBon(String id_record){
        HashMap<Integer,String> datas=new HashMap<>();
        datas.put(1,code_clt);
        datas.put(2,Login.session.employe.code_emp);
        Accueil.BDsql.write2("insert into bon1 (RECORDID,NUM_BON,BLOCAGE,CODE_CLIENT,CODE_VENDEUR,DATE_BON,HEURE,TYPE_BON) values('"+id_record+"','"+Login.session.deviceId+"_s_"+id_bon+"','F',?,?,CAST( "+date_bon +" as datetime),'"+heure+"','SORTIE-STOCK')",datas, new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {
                Accueil.BDsql.transactErr=true;
                Synchronisation.ExportationErr=Synchronisation.ExportationErr+"Erreur d'insertion des bons de sortie dans bon1:  \n"+e.getMessage()+"\n";
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
