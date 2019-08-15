package com.leadersoft.celtica.lsstock.Inventaire;

import android.database.sqlite.SQLiteStatement;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.MyBD;
import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;

import java.sql.SQLException;

/**
 * Created by celtica on 30/04/19.
 */

public class ProduitInventaire extends Produit{

    public double qt;


    public ProduitInventaire(String codebar, String nom,double qt,boolean isPackaging) {
        super(codebar, nom);
        this.qt=qt;
        this.isPackaging=isPackaging;
    }

    public void addToBD(String id_bonTransfert){
        int packaging=0;
        if (isPackaging) packaging=1;
        Accueil.bd.write2("insert into produit_bon_inventaire(id_bon,codebar_pr,nom_pr,quantité,isPackaging) values('"+id_bonTransfert+"','"+codebar+"',?,'"+qt+"','"+packaging+"')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,nom);
                stmt.execute();
            }
        });
    }

    public void exportéProduit(String recordid,String id_bon){
        String QTE_PAR_CARTON="T";
        if (!isPackaging) QTE_PAR_CARTON="F";
        Accueil.BDsql.write("insert into LigneBonTransfertMobile(RECORDID,NUM_BON,CODEBARRE_PRODUIT,QTE,BLOCAGE,QTE_PAR_CARTON) values('"+recordid+"','"+id_bon+"','"+codebar+"','"+qt+"','F','"+QTE_PAR_CARTON+"')", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {
                Accueil.BDsql.transactErr=true;
                Synchronisation.ExportationErr=Synchronisation.ExportationErr+"-Erreur d insertion dans LigneBonTransfertMobile: \n "+e.getMessage();

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
