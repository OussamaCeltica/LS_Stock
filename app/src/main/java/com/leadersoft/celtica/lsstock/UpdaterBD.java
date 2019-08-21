package com.leadersoft.celtica.lsstock;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by celtica on 02/05/19.
 */

public class UpdaterBD {
    public static void update(AppCompatActivity c) throws PackageManager.NameNotFoundException {
        PackageManager manager = c.getPackageManager();
        PackageInfo info = manager.getPackageInfo(c.getPackageName(), PackageManager.GET_ACTIVITIES);

        Cursor r=Accueil.bd.read("select code_version from admin");
        if (r.moveToNext()){
            if (r.getString(r.getColumnIndex("code_version")) == null){
                Accueil.bd.write("update admin set code_version='"+info.versionCode+"'");
            }else {
                Log.e("ccc",""+r.getString(r.getColumnIndex("code_version")));
                int code=r.getInt(r.getColumnIndex("code_version"));

                    while (code != info.versionCode) {
                        switch (code) {

                            case 1: {
                                try {
                                    Accueil.bd.write("Alter table bon_preparation add valider  VARCHAR (2)");
                                    Accueil.bd.write("update bon_preparation set valider='0'  ");
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            }break;
                            case 8: {
                                try {
                                    Accueil.bd.write("Alter table produit add defaultCarton_codebar  VARCHAR (30)");

                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }break;
                            case 12:{
                                try {
                                    Accueil.bd.write("CREATE TABLE fournisseur (\n" +
                                            "    code_fournis VARCHAR (30)  PRIMARY KEY,\n" +
                                            "    nom_fournis  VARCHAR (100) \n" +
                                            ")");

                                    Accueil.bd.write("CREATE TABLE client (\n" +
                                            "    code_clt VARCHAR (30),\n" +
                                            "    nom_clt  VARCHAR (100),\n" +
                                            "    codebar  VARCHAR (30) \n" +
                                            ")");

                                    Accueil.bd.write("Alter table produit add qt_carton double");

                                    Accueil.bd.write("Alter table bon_last_id add id_entree varchar(30)");

                                    Accueil.bd.write("Alter table bon_last_id add id_sortie varchar(30)");

                                    Accueil.bd.write("update bon_last_id set id_entree='0'");

                                    Accueil.bd.write("update bon_last_id set id_sortie='0'");

                                    Accueil.bd.write("CREATE TABLE bon_sortie (\n" +
                                            "    code_bon VARCHAR (100),\n" +
                                            "    date_bon VARCHAR (30),\n" +
                                            "    code_clt VARCHAR (30),\n" +
                                            "    nom_clt  VARCHAR (100),\n" +
                                            "    code_emp VARCHAR (100),\n" +
                                            "    etat     VARCHAR (30),\n" +
                                            "    sync     VARCHAR (3),\n" +
                                            "    PRIMARY KEY (\n" +
                                            "        code_bon\n" +
                                            "    )\n" +
                                            ")");

                                    Accueil.bd.write("CREATE TABLE produit_sortie (\n" +
                                            "    code_bon      VARCHAR (100) REFERENCES bon_sortie (code_bon) ON DELETE CASCADE\n" +
                                            "                                                                 ON UPDATE CASCADE,\n" +
                                            "    codebar_pr    VARCHAR (50),\n" +
                                            "    nom_pr        VARCHAR (100),\n" +
                                            "    codebar_depot VARCHAR (50),\n" +
                                            "    nom_depot     VARCHAR (100),\n" +
                                            "    codebar_lot   VARCHAR (50),\n" +
                                            "    qt            DOUBLE,\n" +
                                            "    isPackaging   VARCHAR (3) \n" +
                                            ")");

                                    Accueil.bd.write("CREATE TABLE bon_entree (\n" +
                                            "    code_bon     VARCHAR (100),\n" +
                                            "    date_bon     VARCHAR (30),\n" +
                                            "    code_emp     VARCHAR (100),\n" +
                                            "    code_fournis VARCHAR (30),\n" +
                                            "    nom_fournis  VARCHAR (100),\n" +
                                            "    etat         VARCHAR (30),\n" +
                                            "    sync         VARCHAR (3),\n" +
                                            "    PRIMARY KEY (\n" +
                                            "        code_bon\n" +
                                            "    )\n" +
                                            ")");

                                    Accueil.bd.write("CREATE TABLE produit_entree (\n" +
                                            "    code_bon      VARCHAR (100) REFERENCES bon_entree (code_bon) ON DELETE CASCADE\n" +
                                            "                                                                 ON UPDATE CASCADE,\n" +
                                            "    codebar_pr    VARCHAR (50),\n" +
                                            "    nom_pr        VARCHAR (100),\n" +
                                            "    codebar_depot VARCHAR (50),\n" +
                                            "    nom_depot     VARCHAR (100),\n" +
                                            "    codebar_lot   VARCHAR (50),\n" +
                                            "    qt            DOUBLE,\n" +
                                            "    isPackaging   VARCHAR (3) \n" +
                                            ")");




                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }break;
                            case 18:{
                                try {
                                    Accueil.bd.write("alter table bon_preparation add nom_clt varchar (100)");
                                    Accueil.bd.write("update bon_preparation set nom_clt='' ");

                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }break;
                            case 21:{

                                Log.e("ddd"," NI DKHELT");
                                try {

                                    Accueil.bd.write("CREATE TABLE sqlitestudio_temp_table AS SELECT *\n" +
                                            "                                          FROM bon_entree;");
                                    Accueil.bd.write("DROP TABLE bon_entree;");

                                    Accueil.bd.write("CREATE TABLE bon_entree (\n" +
                                            "    code_bon     INTEGER,\n" +
                                            "    date_bon     VARCHAR (30),\n" +
                                            "    code_emp     VARCHAR (100),\n" +
                                            "    code_fournis VARCHAR (30),\n" +
                                            "    nom_fournis  VARCHAR (100),\n" +
                                            "    etat         VARCHAR (30),\n" +
                                            "    sync         VARCHAR (3),\n" +
                                            "    PRIMARY KEY (\n" +
                                            "        code_bon\n" +
                                            "    )\n" +
                                            ");");

                                    Accueil.bd.write("INSERT INTO bon_entree (\n" +
                                            "                           code_bon,\n" +
                                            "                           date_bon,\n" +
                                            "                           code_emp,\n" +
                                            "                           code_fournis,\n" +
                                            "                           nom_fournis,\n" +
                                            "                           etat,\n" +
                                            "                           sync\n" +
                                            "                       )\n" +
                                            "                       SELECT code_bon,\n" +
                                            "                              date_bon,\n" +
                                            "                              code_emp,\n" +
                                            "                              code_fournis,\n" +
                                            "                              nom_fournis,\n" +
                                            "                              etat,\n" +
                                            "                              sync\n" +
                                            "                         FROM sqlitestudio_temp_table;");

                                    Accueil.bd.write("DROP TABLE sqlitestudio_temp_table;");

                                    /*------------------- Table 2 -------------------*/

                                    Accueil.bd.write("CREATE TABLE sqlitestudio_temp_table AS SELECT *\n" +
                                            "                                          FROM bon_inventaire;");
                                    Accueil.bd.write("DROP TABLE bon_inventaire;");

                                    Accueil.bd.write("CREATE TABLE bon_inventaire (\n" +
                                            "    id_bon          INTEGER       NOT NULL\n" +
                                            "                                  PRIMARY KEY,\n" +
                                            "    code_emp        VARCHAR (100),\n" +
                                            "    code_depot      VARCHAR (100),\n" +
                                            "    nom_depot       VARCHAR (200),\n" +
                                            "    date_inventaire DATETIME,\n" +
                                            "    sync            VARCHAR (2),\n" +
                                            "    etat            VARCHAR (30) \n" +
                                            ");");

                                    Accueil.bd.write("INSERT INTO bon_inventaire (\n" +
                                            "                               id_bon,\n" +
                                            "                               code_emp,\n" +
                                            "                               code_depot,\n" +
                                            "                               nom_depot,\n" +
                                            "                               date_inventaire,\n" +
                                            "                               sync,\n" +
                                            "                               etat\n" +
                                            "                           )\n" +
                                            "                           SELECT id_bon,\n" +
                                            "                                  code_emp,\n" +
                                            "                                  code_depot,\n" +
                                            "                                  nom_depot,\n" +
                                            "                                  date_inventaire,\n" +
                                            "                                  sync,\n" +
                                            "                                  etat\n" +
                                            "                             FROM sqlitestudio_temp_table;\n");

                                    Accueil.bd.write("DROP TABLE sqlitestudio_temp_table;");


                                    /*------------------- Table 2 -------------------*/

                                    Accueil.bd.write("CREATE TABLE sqlitestudio_temp_table AS SELECT *\n" +
                                            "                                          FROM bon_sortie;\n");
                                    Accueil.bd.write("DROP TABLE bon_sortie;");

                                    Accueil.bd.write("CREATE TABLE bon_sortie (\n" +
                                            "    code_bon INTEGER,\n" +
                                            "    date_bon VARCHAR (30),\n" +
                                            "    code_clt VARCHAR (30),\n" +
                                            "    nom_clt  VARCHAR (100),\n" +
                                            "    code_emp VARCHAR (100),\n" +
                                            "    etat     VARCHAR (30),\n" +
                                            "    sync     VARCHAR (3),\n" +
                                            "    PRIMARY KEY (\n" +
                                            "        code_bon\n" +
                                            "    )\n" +
                                            ");");

                                    Accueil.bd.write("INSERT INTO bon_sortie (\n" +
                                            "                           code_bon,\n" +
                                            "                           date_bon,\n" +
                                            "                           code_clt,\n" +
                                            "                           nom_clt,\n" +
                                            "                           code_emp,\n" +
                                            "                           etat,\n" +
                                            "                           sync\n" +
                                            "                       )\n" +
                                            "                       SELECT code_bon,\n" +
                                            "                              date_bon,\n" +
                                            "                              code_clt,\n" +
                                            "                              nom_clt,\n" +
                                            "                              code_emp,\n" +
                                            "                              etat,\n" +
                                            "                              sync\n" +
                                            "                         FROM sqlitestudio_temp_table;");

                                    Accueil.bd.write("DROP TABLE sqlitestudio_temp_table;");


                                    /*------------------- Table 2 -------------------*/

                                    Accueil.bd.write("CREATE TABLE sqlitestudio_temp_table AS SELECT *\n" +
                                            "                                          FROM bon_transfert;\n");
                                    Accueil.bd.write("DROP TABLE bon_transfert;");

                                    Accueil.bd.write("CREATE TABLE bon_transfert (\n" +
                                            "    id                 INTEGER       NOT NULL,\n" +
                                            "    code_emp           VARCHAR (100),\n" +
                                            "    codebar_depot_src  VARCHAR (100),\n" +
                                            "    nom_depot_src      VARCHAR (100),\n" +
                                            "    codebar_depot_dest VARCHAR (100),\n" +
                                            "    nom_depot_dest     VARCHAR (100),\n" +
                                            "    date_transfert     DATETIME,\n" +
                                            "    sync               VARCHAR (2),\n" +
                                            "    etat               VARCHAR (30),\n" +
                                            "    PRIMARY KEY (\n" +
                                            "        id\n" +
                                            "    )\n" +
                                            ");");

                                    Accueil.bd.write("INSERT INTO bon_transfert (\n" +
                                            "                              id,\n" +
                                            "                              code_emp,\n" +
                                            "                              codebar_depot_src,\n" +
                                            "                              nom_depot_src,\n" +
                                            "                              codebar_depot_dest,\n" +
                                            "                              nom_depot_dest,\n" +
                                            "                              date_transfert,\n" +
                                            "                              sync,\n" +
                                            "                              etat\n" +
                                            "                          )\n" +
                                            "                          SELECT id,\n" +
                                            "                                 code_emp,\n" +
                                            "                                 codebar_depot_src,\n" +
                                            "                                 nom_depot_src,\n" +
                                            "                                 codebar_depot_dest,\n" +
                                            "                                 nom_depot_dest,\n" +
                                            "                                 date_transfert,\n" +
                                            "                                 sync,\n" +
                                            "                                 etat\n" +
                                            "                            FROM sqlitestudio_temp_table;");

                                    Accueil.bd.write("DROP TABLE sqlitestudio_temp_table;");

                                }catch (Exception e){
                                    e.printStackTrace();

                                }
                            }break;
                        }
                        code++;
                    }
                    Accueil.bd.write("update admin set code_version='"+info.versionCode+"'  ");
            }
        }

    }
}
