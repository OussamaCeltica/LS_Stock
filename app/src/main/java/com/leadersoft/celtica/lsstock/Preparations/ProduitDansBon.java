package com.leadersoft.celtica.lsstock.Preparations;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.MyBD;
import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.Synchronisation;

import java.sql.SQLException;

/**
 * Created by celtica on 20/04/19.
 */

public class ProduitDansBon extends Produit {
    public String codebar_dep,nom_dep;
    public double final_qt,current_qt;

    public ProduitDansBon(String codebar_pr, String nom_pr, String codebar_dep, String nom_dep, double final_qt, double current_qt) {
        super(codebar_pr,nom_pr);
        this.codebar_dep = codebar_dep;
        this.nom_dep = nom_dep;
        this.final_qt = final_qt;
        this.current_qt = current_qt;
    }

    public ProduitDansBon(String codebar_pr, String nom_pr, String codebar_dep, String nom_dep, double current_qt) {
        super(codebar_pr,nom_pr);
        this.codebar_dep = codebar_dep;
        this.nom_dep = nom_dep;
        this.current_qt = current_qt;
    }

    public void updatePreparation(String code_bon){
        Cursor r= Accueil.bd.read("select * from produit_preparer where code_bon='"+code_bon+"' and codebar_pr='"+codebar+"' and codebar_depot='"+codebar_dep+"' ");
        if(r.moveToNext()){
            //update qt
            Accueil.bd.write("update produit_preparer set current_qt="+current_qt+" where code_bon='"+code_bon+"' and codebar_pr='"+codebar+"' and codebar_depot='"+codebar_dep+"' ");
        }else {
            //add it to bd ..
           Accueil.bd.write2("insert into produit_preparer(code_bon,codebar_pr,nom_pr,codebar_depot,nom_depot,codebar_lot,final_qt,current_qt) values('"+code_bon+"','"+codebar+"',?,'"+codebar_dep+"',?,'','0','"+current_qt+"')", new MyBD.SqlPrepState() {
               @Override
               public void putValue(SQLiteStatement stmt) {
                   stmt.bindString(1,nom+"");
                   stmt.bindString(2,nom_dep+"");
                   stmt.execute();
               }
           });
        }
    }

    public double getCartonQt (){
        Produit p=new Produit(codebar);
        p.existe();
        return p.qt_carton;
    }

    public void addToBD(final String code_bon){
        Accueil.bd.write2("insert into produit_preparer (code_bon,codebar_pr,nom_pr,codebar_depot,nom_depot,codebar_lot,final_qt,current_qt) values(?,'"+codebar+"',?,'"+codebar_dep+"',?,'','"+final_qt+"','0')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {

                try {
                    stmt.bindString(1, code_bon + "");
                    stmt.bindString(2, nom + "");
                    stmt.bindString(3, nom_dep + "");

                    stmt.execute();
                }catch (SQLiteException e2){
                    Synchronisation.ImportationErr=Synchronisation.ImportationErr+"Erreur d insertion des produits a préparer: \n"+e2.getMessage()+" \n";
                }

            }
        });
    }
}
