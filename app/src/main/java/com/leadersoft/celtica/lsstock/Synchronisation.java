package com.leadersoft.celtica.lsstock;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.EntréeSortie.BonEntree;
import com.leadersoft.celtica.lsstock.EntréeSortie.BonSortie;
import com.leadersoft.celtica.lsstock.EntréeSortie.ProduitEntree;
import com.leadersoft.celtica.lsstock.EntréeSortie.ProduitSortie;
import com.leadersoft.celtica.lsstock.Inventaire.BonInventaire;
import com.leadersoft.celtica.lsstock.Inventaire.ProduitInventaire;
import com.leadersoft.celtica.lsstock.Preparations.BonPreparation;
import com.leadersoft.celtica.lsstock.Preparations.BonPreparationValidé;
import com.leadersoft.celtica.lsstock.Preparations.ProduitDansBon;
import com.leadersoft.celtica.lsstock.TransfertStock.BonTransfert;
import com.leadersoft.celtica.lsstock.TransfertStock.ProduitTransferé;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Synchronisation extends AppCompatActivity {
    EditText ip,port,bd,user,mdp;
    ProgressDialog progess;
    public static int recordTansf=1;
    public static int recordTansfPr=1;

    public static int recordInvent=1;
    public static int recordInventPr=1;

    public static int recordSortie=1;
    public static int recordSortiePr=1;

    public static boolean isOnSync=false;


    private LinearLayout div_admin;

    public static String ImportationErr="";
    public static String ExportationErr="";

    public static boolean errSql=false;
    public static int recordEntree=1;
    public static int recordEntreePr=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronisation);

        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {

            progess=new ProgressDialog(this);

            ExportationErr = "";

            //region Conexion d admin ..
            div_admin = (LinearLayout) findViewById(R.id.sync_div_admin);

            final EditText nom_admin,mdp_admin;
            nom_admin = (EditText) findViewById(R.id.sync_admin_nom);
            mdp_admin = (EditText) findViewById(R.id.sync_admin_mdp);
            ((TextView) findViewById(R.id.sync_admin_conn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (nom_admin.getText().toString().equals("") || mdp_admin.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.remplissage_err), Toast.LENGTH_SHORT).show();
                    } else {
                        Cursor r = Accueil.bd.read("select * from admin where pseudo='" + nom_admin.getText().toString().replaceAll("'", "") + "'");
                        if (r.moveToNext()) {
                            if (r.getString(r.getColumnIndex("mdp")).equals(mdp_admin.getText().toString().replaceAll("'", ""))) {
                                div_admin.setVisibility(View.GONE);
                                ((LinearLayout)findViewById(R.id.DivSqlConnect)).setVisibility(View.VISIBLE);

                                //region connecter au sql server ..
                                ip=(EditText)findViewById(R.id.synch_ip);
                                port=(EditText)findViewById(R.id.synch_port);
                                user=(EditText)findViewById(R.id.synch__user);
                                bd=(EditText)findViewById(R.id.synch_bd);
                                mdp=(EditText)findViewById(R.id.synch_mdp);

                                Cursor r5= Accueil.bd.read("select * from sqlconnect");
                                while(r5.moveToNext()) {
                                    if(r5.getString(r5.getColumnIndex("ip")) != null) {
                                        ip.setText(r5.getString(r5.getColumnIndex("ip")));
                                        port.setText(r5.getString(r5.getColumnIndex("port")));
                                        user.setText(r5.getString(r5.getColumnIndex("user")));
                                        bd.setText(r5.getString(r5.getColumnIndex("bd_name")));
                                        mdp.setText(r5.getString(r5.getColumnIndex("mdp")));
                                    }
                                }

                                //region afficher le div de connexion SQL
                                try {
                                    Accueil.BDsql = new SqlServerBD(ip.getText().toString(),port.getText().toString(),bd.getText().toString(),user.getText().toString() ,mdp.getText().toString(),"net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                                        @Override
                                        public void echec() {
                                            Log.e("connnect", " Echoue");
                                            progess.dismiss();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_conect_err),Toast.LENGTH_SHORT).show();
                                                    ((TextView)findViewById(R.id.synch_Butt)).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            //region afficher le div de connexion SQL
                                                            try {
                                                                Accueil.BDsql = new SqlServerBD(ip.getText().toString(),port.getText().toString(),bd.getText().toString(),user.getText().toString() ,mdp.getText().toString(),"net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                                                                    @Override
                                                                    public void echec() {
                                                                        Log.e("connnect", " Echoue");
                                                                        progess.dismiss();
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_conect_err),Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });

                                                                    }

                                                                    @Override
                                                                    public void before() {
                                                                        progess.setTitle(getResources().getString(R.string.sync_conect));
                                                                        progess.setMessage(getResources().getString(R.string.sync_wait));
                                                                        progess.show();


                                                                    }

                                                                    @Override
                                                                    public void After() throws SQLException {
                                                                        Log.e("connnect", " Reussite");
                                                                        progess.dismiss();
                                                                        ((LinearLayout)findViewById(R.id.div_options)).setVisibility(View.VISIBLE);
                                                                        ((LinearLayout)findViewById(R.id.DivSqlConnect)).setVisibility(View.GONE);
                                                                        Accueil.bd.write("update sqlconnect set ip='"+ip.getText()+"',port='"+port.getText()+"',bd_name='"+bd.getText()+"',mdp='"+mdp.getText()+"',user='"+user.getText()+"' where id='1'");

                                                                    }
                                                                });
                                                            } catch (ClassNotFoundException e) {
                                                                e.printStackTrace();
                                                            } catch (IllegalAccessException e) {
                                                                e.printStackTrace();
                                                            } catch (InstantiationException e) {
                                                                e.printStackTrace();
                                                            } catch (SQLException e) {
                                                                e.printStackTrace();
                                                            }
                                                            //endregion
                                                        }
                                                    });

                                                }
                                            });




                                        }

                                        @Override
                                        public void before() {
                                            progess.setTitle(getResources().getString(R.string.sync_conect));
                                            progess.setMessage(getResources().getString(R.string.sync_wait));
                                            progess.show();


                                        }

                                        @Override
                                        public void After() throws SQLException {
                                            Log.e("connnect", " Reussite");
                                            progess.dismiss();
                                            ((LinearLayout)findViewById(R.id.div_options)).setVisibility(View.VISIBLE);
                                            ((LinearLayout)findViewById(R.id.DivSqlConnect)).setVisibility(View.GONE);
                                            Accueil.bd.write("update sqlconnect set ip='"+ip.getText()+"',port='"+port.getText()+"',bd_name='"+bd.getText()+"',mdp='"+mdp.getText()+"',user='"+user.getText()+"' where id='1'");

                                        }
                                    });
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                //endregion

                                //endregion

                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.param_mdp_err), Toast.LENGTH_SHORT).show();

                            }

                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.param_user_err), Toast.LENGTH_SHORT).show();

                        }

                    }

                }
            });
            //endregion

            //region connecter au sql server ..
            try {
                Accueil.BDsql = new SqlServerBD(Login.session.serveur.ip,Login.session.serveur.port,Login.session.serveur.bdName,Login.session.serveur.user,Login.session.serveur.mdp,"net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                    @Override
                    public void echec() {
                        Log.e("connnect", " Echoue");
                        progess.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_sql_err),Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void before() {
                        progess.setTitle(getResources().getString(R.string.sync_conect));
                        progess.setMessage(getResources().getString(R.string.sync_wait));
                        progess.show();


                    }

                    @Override
                    public void After() throws SQLException {
                        Log.e("connnect", " Reussite");
                        progess.dismiss();
                        ((LinearLayout)findViewById(R.id.div_options)).setVisibility(View.VISIBLE);
                        ((LinearLayout)findViewById(R.id.DivSqlConnect)).setVisibility(View.GONE);
                        // Accueil.bd.write("update sqlconnect set ip='"+ip.getText()+"',port='"+port.getText()+"',bd_name='"+bd.getText()+"',mdp='"+mdp.getText()+"',user='"+user.getText()+"' where id='1'");

                    }
                });
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //endregion

            //region importation ..
            ((TextView)findViewById(R.id.sync_import_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //region progressbar ..
                    Accueil.BDsql.es.execute(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progess.setTitle(getResources().getString(R.string.sync_conect));
                                    progess.setMessage(getResources().getString(R.string.sync_wait));
                                    progess.show();
                                }
                            });
                        }
                    });
                    //endregion

                    //region Importation mode admin ..
                    if (Login.session.mode.equals("admin")){

                        //region importation des employé ..
                        Accueil.BDsql.read("SELECT Employé.CODEBARRE\n" +
                                " ,FirstName\n" +
                                " ,LastName\n" +
                                " ,matricule_employé\n" +
                                "FROM Person \n" +
                                "INNER JOIN Employé  ON Person.Oid = Employé.Oid\n" +
                                "INNER JOIN Party ON Person.Oid = Party.Oid \n" +
                                "WHERE Employé.matricule_employé IS NOT NULL AND Party.GCRecord is null ", new SqlServerBD.doAfterBeforeGettingData() {
                            @Override
                            public void echec(final SQLException e) {
                                ImportationErr=ImportationErr+"-Erreur de récupération des employés: \n"+e.getMessage()+" \n";

                            }

                            @Override
                            public void before() {

                            }

                            @Override
                            public void After()  {

                                Employe.SqliteErr=false;
                                ResultSet r = Accueil.BDsql.r;
                                Accueil.bd.write("delete from employe  ");
                                try {
                                    while (r.next()) {
                                        Log.e("empp", r.getString("matricule_employé") + r.getString("FirstName"));

                                        Employe emp=new Employe(r.getString("matricule_employé"),r.getString("LastName") +" "+ r.getString("FirstName"),r.getString("CODEBARRE"),"");
                                        emp.addToBD();

                                    }
                                }catch(SQLException e2){
                                    ImportationErr = ImportationErr + "-Erreur de syntaxe SQLSERVEUR, table d employé: \n" + e2.getMessage() + "\n";

                                }
                            }
                        });
                        //endregion

                        //region importation des produit ..
                        Accueil.BDsql.read("select p.name , p.EAN13Code as codebar, ppIn.EAN13Code condEntree, ppIn.quantity qteIn, ppOut.EAN13Code condSortie, ppOut.quantity qteOut\n" +
                                "from Product p left join ProductPackaging ppIn on p.defaultPackagingInput = ppIn.Oid\n" +
                                "left join ProductPackaging ppOut on p.defaultPackagingOutput = ppOut.Oid\n" +
                                "where p.EAN13Code is not null and p.EAN13Code !='' and p.GCRecord is null ", new SqlServerBD.doAfterBeforeGettingData() {
                            @Override
                            public void echec(final SQLException e) {
                                ImportationErr=ImportationErr+"-Erreur de récupération des produits: \n"+e.getMessage()+" \n";
                            }

                            @Override
                            public void before() {
                                progess.setTitle(getResources().getString(R.string.sync_titre));
                                progess.setMessage(getResources().getString(R.string.sync_wait));
                                progess.show();
                            }

                            @Override
                            public void After() {
                                Produit.SqliteErr=false;
                                ResultSet r = Accueil.BDsql.r;
                                Accueil.bd.write("delete from produit ");


                                try {
                                    while (r.next()) {
                                        Log.e("prr", r.getString("codebar") + " / " + r.getString("name")+" / "+r.getDouble("qteOut"));

                                        Produit p=new Produit(r.getString("codebar")+"" ,r.getString("name"),r.getString("condSortie")+"",((r.getDouble("qteOut") == 0.0) ? 1 : r.getDouble("qteOut")));
                                        p.addToBD();

                                    }
                                }catch (SQLException e){

                                }
                            }
                        });

                        //endregion

                        //region importation des depot ..
                        Accueil.BDsql.read("SELECT  Oid , name , barCode  FROM  Warehouse  where GCRecord is null and isActif =1 and barCode is not null and barCode !=''  ", new SqlServerBD.doAfterBeforeGettingData() {
                            @Override
                            public void echec(SQLException e) {
                                ImportationErr=ImportationErr+"-Erreur d importation des dépôts: \n"+e.getMessage();
                            }

                            @Override
                            public void before() {

                            }

                            @Override
                            public void After() {
                                final ResultSet r=Accueil.BDsql.r;
                                Accueil.bd.write("delete from depot");
                                try {
                                    while (r.next()){
                                        Log.e("depp",""+r.getString("barcode"));

                                        Depot d=new Depot(r.getString("barcode"),r.getString("name"));
                                        d.addToBD();

                                    }

                                }catch (SQLException e){
                                    ImportationErr=ImportationErr+"-Erreur d importation des dépôts: \n"+e.getMessage();
                                }


                            }
                        });
                        //endregion

                        //region Importation des fournisseur ..
                        Accueil.BDsql.read("select code, name from Company where GCRecord is null and isSupplier = 1", new SqlServerBD.doAfterBeforeGettingData() {
                            @Override
                            public void echec(SQLException e) {
                                ImportationErr=ImportationErr+"Erreur d imporaton des fournisseurs: \n"+e.getMessage()+" \n";
                            }

                            @Override
                            public void before() {

                            }

                            @Override
                            public void After() {
                                final ResultSet r=Accueil.BDsql.r;
                                Accueil.bd.write("delete from fournisseur ");
                                //insertion des fourniseur ..
                                try{
                                    while(r.next()){
                                        Log.e("fff",r.getString("code")+" / "+r.getString("name"));
                                        Accueil.bd.write2("insert into fournisseur (code_fournis,nom_fournis) values(?,?)", new MyBD.SqlPrepState() {
                                            @Override
                                            public void putValue(SQLiteStatement stmt) {
                                                try {
                                                    stmt.bindString(1,r.getString("code")+"");
                                                    stmt.bindString(2,r.getString("name")+"");
                                                    stmt.execute();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                    ImportationErr=ImportationErr+"Erreur de syntaxe SQLSERVER(Récupération des fournisseurs): \n"+e.getMessage()+" \n";
                                                }catch (SQLiteException e){
                                                    try {
                                                        ImportationErr=ImportationErr+"Erreur d 'insertion  des fournisseurs (code fournisseur="+r.getString("code")+") \n"+e.getMessage()+"\n";
                                                    } catch (SQLException e1) {
                                                        e1.printStackTrace();
                                                    };
                                                }

                                            }
                                        });
                                    }
                                }catch (SQLException e){
                                    e.printStackTrace();
                                }

                            }
                        });
                        //endregion

                        //region importation des clients ..
                        Accueil.BDsql.read("select code, name from Company where GCRecord is null and isCustomer = 1 ", new SqlServerBD.doAfterBeforeGettingData() {
                            @Override
                            public void echec(SQLException e) {
                                ImportationErr=ImportationErr+"Erreur d imporaton des clinets: \n"+e.getMessage()+" \n";
                            }

                            @Override
                            public void before() {

                            }

                            @Override
                            public void After() {
                                final ResultSet r=Accueil.BDsql.r;
                                Accueil.bd.write("delete from client ");
                                //insertion des client ..
                                try{
                                    while(r.next()){
                                        Log.e("cclient",r.getString("code")+" / "+r.getString("name"));
                                        Accueil.bd.write2("insert into client (code_clt,nom_clt,codebar) values(?,?,'')", new MyBD.SqlPrepState() {
                                            @Override
                                            public void putValue(SQLiteStatement stmt) {
                                                try {
                                                    stmt.bindString(1,r.getString("code")+"");
                                                    stmt.bindString(2,r.getString("name")+"");
                                                    stmt.execute();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                    ImportationErr=ImportationErr+"Erreur de syntaxe SQLSERVER(Récupération des clients): \n"+e.getMessage()+" \n";
                                                }catch (SQLiteException e){
                                                    try {
                                                        ImportationErr=ImportationErr+"Erreur d 'insertion  des clients (code client="+r.getString("code")+") \n"+e.getMessage()+"\n";
                                                    } catch (SQLException e1) {
                                                        e1.printStackTrace();
                                                    };
                                                }

                                            }
                                        });
                                    }
                                }catch (SQLException e){
                                    e.printStackTrace();
                                }

                            }
                        });
                        //endregion

                    }
                    //endregion

                    //region importation mode employé
                    else {
                       //region récupération des bon de commande dun preparateur ..
                        Accueil.BDsql.read("SELECT\t[InventoryPreparationNote].Oid\n" +
                                "    \t\t, [Document].[date]\n" +
                                "    \t\t, [Document].[code]\n" +
                                "    \t\t, [InventoryPreparationNote].temporaryInputZone\n" +
                                "    \t\t, [Product].EAN13Code as product_barCode\n" +
                                "    \t\t, [Product].[name] as pr_name , [DocumentItem].quantityToBePrepared finalQuantity \n" +
                                "    \t\t, [Warehouse].barCode as warehouse_barCode\n" +
                                "    \t\t, [Warehouse].[name] as depot_name,Employé.matricule_employé \n" +
                                "\t\t\t, [Company].[name]\n" +
                                "    \n" +
                                "    FROM  [InventoryPreparationNote]\n" +
                                "    INNER JOIN Document\n" +
                                "    ON Document.Oid = [InventoryPreparationNote].Oid\n" +
                                "    \tAND [InventoryPreparationNote].isCanceled = 0\n" +
                                "    \tAND Document.isValidated = 0\n" +
                                "    \tAND [InventoryPreparationNote].[manualInput] = 1\n" +
                                "    \tAND [Document].GCRecord IS NULL\n" +
                                "    INNER JOIN Employé ON [InventoryPreparationNote].employee=Employé.Oid  INNER JOIN DocumentItem\n" +
                                "    ON\tDocument.Oid = DocumentItem.document\n" +
                                "    \tAND DocumentItem.GCRecord IS NULL\n" +
                                "    INNER JOIN Product \n" +
                                "    ON Product.Oid = DocumentItem.product\n" +
                                "    INNER JOIN Warehouse\n" +
                                "    ON Warehouse.Oid = DocumentItem.warehouse \n" +
                                "\t inner join Company on Document.contact = Company.Oid\n" +
                                "\t where matricule_employé='"+Login.session.employe.code_emp+"' and  not exists(select * from DocumentItem where GCRecord is null and document = [Document].Oid and quantity <> 0) order by code", new SqlServerBD.doAfterBeforeGettingData() {
                            @Override
                            public void echec(SQLException e) {
                                e.printStackTrace();
                                ImportationErr=ImportationErr+"-Erreur de récupération des bons de préparations: \n"+e.getMessage()+" \n";
                            }

                            @Override
                            public void before() {

                            }

                            @Override
                            public void After() {
                                Accueil.bd.write("delete from bon_preparation where valider='0'");
                                //il faut d abord supprimer les anciens prep ..
                                final ResultSet r=Accueil.BDsql.r;
                                String code_bon="";

                                try {
                                   while (r.next()){
                                      Log.e("prepp","code_prep"+r.getString("code")+" / "+r.getString("product_barCode")+" / code_depot: "+r.getString("warehouse_barCode")+" / client"+r.getString("name"));
                                      if(!code_bon.equals(r.getString("code"))){
                                          //nouveau bon ..
                                          code_bon=r.getString("code");

                                          BonPreparation bon=new BonPreparation(r.getString("code"),r.getString("date"),r.getString("Oid"),r.getString("name"),ETAT.EN_COURS);
                                          bon.addToBD();
                                      }

                                       ProduitDansBon pr=new ProduitDansBon(r.getString("product_barCode"),r.getString("pr_name"),r.getString("warehouse_barCode"),r.getString("depot_name"),r.getDouble("finalQuantity"),0);
                                       pr.addToBD(code_bon);
                                   }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    ImportationErr=ImportationErr+"-Erreur d'insertion  des bons de préparations: \n"+e.getMessage()+" \n";

                                }

                            }
                        });
                        //endregion
                    }
                    //endregion

                    afficherMsgErr();

                }
            });
            //endregion

            //region exportation ..
            ((TextView)findViewById(R.id.sync_export_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!isOnSync) {
                        isOnSync=true;

                        //region progressbar ..
                        Accueil.BDsql.es.execute(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progess.setTitle(getResources().getString(R.string.sync_conect));
                                        progess.setMessage(getResources().getString(R.string.sync_wait));
                                        progess.show();
                                    }
                                });
                            }
                        });
                        //endregion

                        exportéBonTransfert();

                        exportéPréparation();

                        exportéBonInventaire();

                        exportéBonEntrée();

                        exportéBonSortie();

                        afficherMsgErr();

                        //region supprimer l archive..
                        try{
                            Accueil.bd.write("delete  from bon_transfert where julianday('now') - julianday('date_transfert') > 60 and sync='1'  ");
                            Accueil.bd.write("delete  from bon_inventaire where julianday('now') - julianday('date_inventaire') > 60 and sync='1'  ");

                        }catch(SQLiteException e) {

                        }
                        //endregion
                    }
                    //endregion
                }
            });
            //endregion
        }
    }

    public static void exportéBonTransfert(){

        //region Exportations des transferts ..
        Accueil.BDsql.read("select top 1 RECORDID from BonTransfertMobile order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r=Accueil.BDsql.r;

                try {
                    if (r.next()){
                        recordTansf=Integer.parseInt(r.getString("RECORDID"))+1;
                        Log.e("recc"," BON1:  "+recordTansf);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });

        Accueil.BDsql.read("select top 1 RECORDID from LigneBonTransfertMobile order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r=Accueil.BDsql.r;

                try {
                    if (r.next()){
                        recordTansfPr=Integer.parseInt(r.getString("RECORDID"))+1;
                        Log.e("recc"," BON2:  "+recordTansfPr);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });

        Accueil.BDsql.es.execute(new Runnable() {
            @Override
            public void run() {
                Cursor r=Accueil.bd.read("select bt.* , cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_transfert)) as double) / (3600 * 24) as date_f from bon_transfert bt where sync='0' and date_transfert is not null");
                while (r.moveToNext()){

                    Accueil.BDsql.beginTRansact();
                    final BonTransfert bon=new BonTransfert(r.getString(r.getColumnIndex("id"))+"",r.getString(r.getColumnIndex("codebar_depot_src"))+"" ,r.getString(r.getColumnIndex("codebar_depot_dest"))+"",r.getDouble(r.getColumnIndex("date_f"))+"",r.getDouble(r.getColumnIndex("code_emp"))+"",ETAT.VALIDÉ);
                    bon.exportéBon(recordTansf+"");

                    ArrayList<ProduitTransferé> produits=bon.getProduitInBon();
                    int i=0;
                    while (i != produits.size()){
                        produits.get(i).exportéProduit(recordTansfPr+"",bon.id_bon);
                        recordTansfPr++;
                        i++;
                    }
                    Accueil.BDsql.commitTRansact();

                    Accueil.BDsql.es.execute(new Runnable() {
                        @Override
                        public void run() {
                        if (!Accueil.BDsql.transactErr){
                            bon.changeState("exporté");
                        }
                        }
                    });
                    recordTansf++;
                }
                recordTansfPr=1;
                recordTansf=1;
            }
        });
        //endregion
    }

    public static void exportéBonInventaire(){

        //region Exportations des inventaire ..
        Accueil.BDsql.read("select top 1 RECORDID from BonTransfertMobile order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r=Accueil.BDsql.r;

                try {
                    if (r.next()){
                        recordTansf=Integer.parseInt(r.getString("RECORDID"))+1;
                        Log.e("recc"," BON1:  "+recordTansf);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });

        Accueil.BDsql.read("select top 1 RECORDID from LigneBonTransfertMobile order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r=Accueil.BDsql.r;

                try {
                    if (r.next()){
                        recordTansfPr=Integer.parseInt(r.getString("RECORDID"))+1;
                        Log.e("recc"," BON2:  "+recordTansfPr);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });

        Accueil.BDsql.es.execute(new Runnable() {
            @Override
            public void run() {
                Cursor r=Accueil.bd.read("select bi.* , cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_inventaire)) as double) / (3600 * 24) as date_f from bon_inventaire bi where sync='0'");
                while (r.moveToNext()){

                    Accueil.BDsql.beginTRansact();
                    final BonInventaire bon=new BonInventaire(r.getString(r.getColumnIndex("id_bon"))+"",r.getString(r.getColumnIndex("code_depot"))+"" ,r.getString(r.getColumnIndex("nom_depot"))+"",r.getDouble(r.getColumnIndex("date_f"))+"",r.getDouble(r.getColumnIndex("code_emp"))+"");
                    bon.exportéBon(recordTansf+"");

                    ArrayList<ProduitInventaire> produits=bon.getProduitInBon();

                    for (ProduitInventaire p: produits){
                        p.exportéProduit(recordTansfPr+"",bon.id_bon);
                        recordTansfPr++;
                    }
                    Accueil.BDsql.commitTRansact();

                    Accueil.BDsql.es.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (!Accueil.BDsql.transactErr){
                                bon.changeState("exporté");
                            }
                        }
                    });
                    recordTansf++;
                }
            }
        });

        recordTansfPr=1;
        recordTansf=1;
        //endregion
    }

    public static void exportéBonEntrée(){
        Accueil.BDsql.read("select top 1 RECORDID from  bon1 order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r=Accueil.BDsql.r;

                try {
                    if (r.next()){
                        recordEntree=Integer.parseInt(r.getString("RECORDID"))+1;
                        Log.e("recc"," BonEntre:  "+recordEntree);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });

        Accueil.BDsql.read("select top 1 RECORDID from bon2 order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r=Accueil.BDsql.r;

                try {
                    if (r.next()){
                        recordEntreePr=Integer.parseInt(r.getString("RECORDID"))+1;
                        Log.e("recc"," BON2:  "+recordEntreePr);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });

        Accueil.BDsql.es.execute(new Runnable() {
            @Override
            public void run() {
                Cursor r=Accueil.bd.read("select be.* , cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_bon)) as double) / (3600 * 24) as date_f from bon_entree be where sync='0'");
                while (r.moveToNext()){

                    Accueil.BDsql.beginTRansact();
                    final BonEntree bon=new BonEntree(r.getString(r.getColumnIndex("code_bon"))+"",r.getString(r.getColumnIndex("date_f"))+"",r.getString(r.getColumnIndex("code_fournis")),r.getString(r.getColumnIndex("nom_fournis")),ETAT.VALIDÉ);
                    bon.exportéBon(recordEntree+"");

                    ArrayList<ProduitEntree> produits=bon.getProduitEntree();
                    int i=0;
                    while (i != produits.size()){
                        produits.get(i).exportéProduit(recordEntreePr+"",bon.id_bon);
                        recordEntreePr++;
                        i++;
                    }
                    Accueil.BDsql.commitTRansact();

                    Accueil.BDsql.es.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (!Accueil.BDsql.transactErr){
                                bon.changeState("exporté");
                            }
                        }
                    });
                    recordEntree++;
                }
                recordEntree=1;
                recordEntreePr=1;
            }
        });
    }

    public static void exportéBonSortie(){
        Accueil.BDsql.read("select top 1 RECORDID from  bon1 order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r=Accueil.BDsql.r;

                try {
                    if (r.next()){
                        recordSortie=Integer.parseInt(r.getString("RECORDID"))+1;
                        Log.e("recc"," BonSortie:  "+recordSortie);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });

        Accueil.BDsql.read("select top 1 RECORDID from bon2 order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r=Accueil.BDsql.r;

                try {
                    if (r.next()){
                        recordSortiePr=Integer.parseInt(r.getString("RECORDID"))+1;
                        Log.e("recc"," BON2:  "+recordSortiePr);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });

        Accueil.BDsql.es.execute(new Runnable() {
            @Override
            public void run() {
                Cursor r=Accueil.bd.read("select be.* , cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d 00:00:00',date_bon)) as double) / (3600 * 24) as date_f ,  strftime('%H:%M',date_bon) as heure from bon_sortie be where sync='0'");
                while (r.moveToNext()){

                    Accueil.BDsql.beginTRansact();
                    final BonSortie bon=new BonSortie(r.getString(r.getColumnIndex("code_bon"))+"",r.getString(r.getColumnIndex("date_f"))+"",r.getString(r.getColumnIndex("heure"))+"",r.getString(r.getColumnIndex("code_clt")),r.getString(r.getColumnIndex("nom_clt")),ETAT.VALIDÉ);
                    bon.exportéBon(recordSortie+"");

                    ArrayList<ProduitSortie> produits=bon.getProduitSortie();
                    int i=0;
                    while (i != produits.size()){
                        produits.get(i).exportéProduit(recordSortiePr+"",bon.id_bon);
                        recordSortiePr++;
                        i++;
                    }
                    Accueil.BDsql.commitTRansact();

                    Accueil.BDsql.es.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (!Accueil.BDsql.transactErr){
                                bon.changeState("exporté");
                            }
                        }
                    });
                    recordSortie++;
                }
                recordSortie=1;
                recordSortiePr=1;
            }
        });
    }

    public static void exportéPréparation(){
        //

        Cursor r=Accueil.bd.read("select * from bon_preparation where valider='1' and sync='0'");
        while (r.moveToNext()){
            BonPreparation bon=new BonPreparation(r.getString(r.getColumnIndex("code_bon")),r.getString(r.getColumnIndex("date_bon")),r.getString(r.getColumnIndex("Oid")));
            bon.exportéBon();
        }
    }

    public static void connecterSQL(AppCompatActivity c, final SqlServerBD.doAfterBeforeConnect d){
        //region connecter au sql server ..
        try {
            Accueil.BDsql = new SqlServerBD(Login.session.serveur.ip,Login.session.serveur.port,Login.session.serveur.bdName,Login.session.serveur.user,Login.session.serveur.mdp,"net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                @Override
                public void echec() {
                    d.echec();
                }

                @Override
                public void before() {
                    d.before();

                }

                @Override
                public void After() throws SQLException {
                    d.After();
                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //endregion
    }

    public static void connecterSQL(Context c, final SqlServerBD.doAfterBeforeConnect d){
        //region connecter au sql server ..
        try {
            Accueil.BDsql = new SqlServerBD(Login.session.serveur.ip,Login.session.serveur.port,Login.session.serveur.bdName,Login.session.serveur.user,Login.session.serveur.mdp,"net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                @Override
                public void echec() {
                    d.echec();
                }

                @Override
                public void before() {
                    d.before();

                }

                @Override
                public void After() throws SQLException {
                    d.After();
                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //endregion
    }


    public void afficherMsgErr(){
        Accueil.BDsql.es.execute(new Runnable() {
            @Override
            public void run() {
                Accueil.BDsql.es.execute(new Runnable() {
                    @Override
                    public void run() {
                        isOnSync=false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Log.e("errr"," MSG :"+ExportationErr);
                                progess.dismiss();
                                if(!ImportationErr.equals("") || !ExportationErr.equals("")){
                                    AlertDialog.Builder mb = new AlertDialog.Builder(Synchronisation.this); //c est l activity non le context ..

                                    View v = getLayoutInflater().inflate(R.layout.div_aff_msg_err, null);
                                    TextView msg = (TextView) v.findViewById(R.id.err_msg);
                                    Button ok = (Button) v.findViewById(R.id.ok);

                                    mb.setView(v);
                                    final AlertDialog add = mb.create();

                                    add.show();
                                    add.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                                    add.setCancelable(false); //désactiver le button de retour ..
                                    msg.setText(ImportationErr+" \n "+ExportationErr);

                                    ok.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            ImportationErr="";
                                            ExportationErr="";
                                            add.dismiss();
                                        }
                                    });

                                }else {
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_succ),Toast.LENGTH_SHORT).show();
                                }


                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
