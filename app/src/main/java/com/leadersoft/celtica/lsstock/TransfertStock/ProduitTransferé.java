package com.leadersoft.celtica.lsstock.TransfertStock;

import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.Depot;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.MyBD;
import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by celtica on 28/04/19.
 */

public class ProduitTransferé extends Produit {
    public double qt;



    public ProduitTransferé(String codebar, String nom,double qt,boolean isPackaging) {
        super(codebar, nom);
        this.qt=qt;
        this.isPackaging=isPackaging;
    }

    public void addToBD(String id_bonTransfert){
        int packaging=0;
        if (isPackaging) packaging=1;
        Accueil.bd.write2("insert into produit_transferer(id_bon,codebar_pr,nom_pr,quantité,isPackaging) values('"+id_bonTransfert+"','"+codebar+"',?,'"+qt+"','"+packaging+"')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,nom);
                stmt.execute();
            }
        });
    }

    public void checkQtInDepotExiste(final String code_dep){
        Accueil.BDsql.read("select p.EAN13Code code_barre_produit, p.id code_produit, p.name nom_produit, \n" +
                "w.barCode code_barre_entrepot, w.id code_entrepot, w.name nom_entrepot, \n" +
                "wpl.stock qtStock, pp.EAN13Code code_bare_emballage, pp.quantity quantité_par_enballage, pck.designation nom_emballage\n" +
                "from WarehouseProductLine wpl \n" +
                "inner join Product p on wpl.product = p.Oid\n" +
                "inner join Warehouse w on wpl.warehouse = w.Oid\n" +
                "left join ProductPackaging pp on p.defaultPackagingOutput = pp.Oid\n" +
                "left join Packaging pck on pp.packaging = pck.Oid\n" +
                "\n" +
                "where w.barCode='"+code_dep+"' and p.EAN13Code ='"+codebar+"'  order by code_produit", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {
                Log.e("err",e.getMessage());
                FaireTransfert.ExportErr=FaireTransfert.ExportErr+"-Erreur de récupération des infos de produit:("+codebar+") depuis le dépot: ("+code_dep+") \n \n";
            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r=Accueil.BDsql.r;
                try {
                    if (r.next()){
                        Log.e("ppp","PRODUIT: "+r.getString("code_barre_produit")+" / DÉPOT: "+r.getString("code_barre_entrepot")+" / QTÉ: "+r.getDouble("qtStock")+"/ PACKAGING_DEFAULT_QT:"+r.getDouble("quantité_par_enballage"));

                        if (isPackaging && r.getString("quantité_par_enballage") != null)qt=qt*r.getDouble("quantité_par_enballage");
                        if (r.getDouble("qtStock")<Double.parseDouble(qt+"")){
                            FaireTransfert.ExportErr=FaireTransfert.ExportErr+"<span>-Erreur de transfert de produit: <font color='red'> ("+r.getString("nom_produit")+" / codebarre:"+r.getString("code_barre_produit")+" ) </font> car la quantité est invalide , MAX quantité est = "+r.getDouble("qtStock")+"</span> \n";
                        }
                    }else {
                        Depot d=new Depot(code_dep);
                        FaireTransfert.ExportErr=FaireTransfert.ExportErr+"<span>-Erreur de transfert de produit: <font color='red'> ("+nom+" / codebarre:"+codebar+" ) </font> car ce produit n 'existe pas dans le dépot: ("+d.getNomDepot()+" / codebarre: "+code_dep+")</span> <br>";
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    FaireTransfert.ExportErr=" \n "+FaireTransfert.ExportErr+" "+e.getMessage();
                }

            }
        });
    }

    public void exportéProduit(String recordid,String id_bon){
        String QTE_PAR_CARTON="T";
        if (!isPackaging) QTE_PAR_CARTON="F";

        HashMap<Integer,String> datas=new HashMap<Integer,String> ();
        datas.put(1,Login.session.deviceId+"_"+id_bon);
        Accueil.BDsql.write2("insert into LigneBonTransfertMobile(RECORDID,NUM_BON,CODEBARRE_PRODUIT,QTE,BLOCAGE,QTE_PAR_CARTON) values('"+recordid+"',?,'"+codebar+"','"+qt+"','F','"+QTE_PAR_CARTON+"')",datas,new SqlServerBD.doAfterBeforeGettingData() {
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
