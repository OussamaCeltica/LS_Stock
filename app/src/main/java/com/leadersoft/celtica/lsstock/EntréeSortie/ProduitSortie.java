package com.leadersoft.celtica.lsstock.EntréeSortie;

import android.database.sqlite.SQLiteStatement;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.MyBD;
import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;

import java.sql.SQLException;

public class ProduitSortie extends Produit {
    public String code_dep,nom_dep;
    public double qt;
    public ETAT etat;
    public ProduitSortie(String codebar, String nom) {
        super(codebar, nom);
    }

    public ProduitSortie(String codebar, String nom, String code_dep, String nom_dep, double qt) {
        super(codebar, nom);
        this.code_dep = code_dep;
        this.nom_dep = nom_dep;
        this.qt = qt;

    }

    public ProduitSortie(String codebar, String nom, String code_dep, String nom_dep, double qt,ETAT etat) {
        super(codebar, nom);
        this.code_dep = code_dep;
        this.nom_dep = nom_dep;
        this.qt = qt;
        this.etat=etat;
    }

    public ProduitSortie(String codebar, String nom, String code_dep, String nom_dep, double qt,ETAT etat,boolean isPack) {
        super(codebar, nom);
        this.code_dep = code_dep;
        this.nom_dep = nom_dep;
        this.qt = qt;
        this.etat=etat;
        super.isPackaging=isPack;
    }

    public ProduitSortie(String codebar, String nom, String code_dep, String nom_dep) {
        super(codebar, nom);
        this.code_dep = code_dep;
        this.nom_dep = nom_dep;

    }

    public void addToBD(String id_bon){
        Accueil.bd.write2("insert into produit_sortie(code_bon,codebar_pr,nom_pr,codebar_depot,nom_depot,qt,isPackaging) values('"+id_bon+"','"+codebar+"',?,'"+code_dep+"',?,'"+qt+"','"+((isPackaging == true) ? 1 : 0)+"')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,""+nom);
                stmt.bindString(2,""+nom_dep);
                stmt.execute();

            }
        });
    }

    public void exportéProduit(String id_record, String id_bon) {
        Accueil.BDsql.write("insert into bon2 (RECORDID,NUM_BON,CODE_BARRE,QTE,CODE_DEPOT,BLOCAGE,QTE_PAR_CARTON) values('"+id_record+"','"+ Login.session.deviceId +"_s_"+id_bon+"','"+codebar+"','"+qt+"','"+code_dep+"','F','"+((isPackaging==true) ? 'T' : 'F')+"')", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {
                Accueil.BDsql.transactErr=true;
                Synchronisation.ExportationErr=Synchronisation.ExportationErr+"Erreur d'insertion dans bon2 (entree stock) :\n"+e.getMessage()+"\n";
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
