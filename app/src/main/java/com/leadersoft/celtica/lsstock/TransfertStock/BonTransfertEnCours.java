package com.leadersoft.celtica.lsstock.TransfertStock;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.MyBD;

/**
 * Created by celtica on 29/04/19.
 */

public class BonTransfertEnCours extends BonTransfert {

    public BonTransfertEnCours(String id_bon, String codebar_dep_src,String nom_dep_src) {
        this.id_bon = id_bon;
        this.codebar_dep_src = codebar_dep_src;
        this.nom_dep_src=nom_dep_src;
        this.etat= ETAT.EN_COURS;
    }
    public BonTransfertEnCours(String codebar_dep_src){
        this.etat= ETAT.EN_COURS;
        this.codebar_dep_src=codebar_dep_src;
        int id=1;
        Cursor r= Accueil.bd.read("select id_transfert from bon_last_id  order by id_transfert desc limit 1");
        if(r.moveToNext()){
            id=r.getInt(r.getColumnIndex("id_transfert"))+1;
        }
        id_bon=id+"";
    }

    public BonTransfertEnCours(String id_bon,String depot_src){
        this.etat= ETAT.EN_COURS;
        this.id_bon=id_bon;
    }


    public void addToBD(){
        Accueil.bd.write2("insert into bon_transfert(id,code_emp,codebar_depot_src,nom_depot_src,sync,etat ) values('"+id_bon+"','"+ Login.session.employe.code_emp+"','"+codebar_dep_src+"',?,'0','en cours')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,getDepotSrcName());
                stmt.execute();
            }
        });
        updateLastBonId();

    }

    public void updateLastBonId(){
        Accueil.bd.write("update bon_last_id set id_transfert='"+id_bon+"'  ");
    }

    public void suppPrTransferer(){
        Accueil.bd.write("delete from produit_transferer where id_bon='"+id_bon+"'");
    }
}
