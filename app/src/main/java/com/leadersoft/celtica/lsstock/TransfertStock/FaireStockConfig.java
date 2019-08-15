package com.leadersoft.celtica.lsstock.TransfertStock;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsstock.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.Session;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class FaireStockConfig extends AppCompatActivity {

    String code_depot="";
    ProgressDialog progress;
    int recordTansf=1;
    int recordTansfPr=1;
    public static boolean isValider=false;//pour eviter la validation 2 fois lors de cnx t9ila ..

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_stock_config);

        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

            progress=new ProgressDialog(this);

            final String request=getIntent().getExtras().getString("request");

            if(request.equals("depot_dest")){
                ((TextView)findViewById(R.id.stock_config_depSrc)).setHint(getResources().getString(R.string.faire_prep_depotDest));
            }

            //region select depot source ..
            ((TextView)findViewById(R.id.stock_config_depSrc)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Login.session.openScannerCodeBarre(FaireStockConfig.this, new Session.OnScanListener() {
                        @Override
                        public void OnScan(String code,LinearLayout body) {
                            checkDepotExiste(code);
                        }
                    });

                }
            });
            //endregion

            //region valider depot  et faire transfert ..
            ((TextView)findViewById(R.id.stock_config_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (code_depot.equals("")){
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.faire_prep_depot),Toast.LENGTH_SHORT).show();
                    }else {
                        if (request.equals("depot_dest")) {
                            if(!isValider) {
                                if(BonsTransfertAdapter.bons.get(BonsTransfertAdapter.itemSelected).codebar_dep_src.equals(code_depot)){
                                    Toast.makeText(getApplicationContext(),"Choisissez un dépôt de déstination différent de dépôt source.",Toast.LENGTH_LONG).show();
                                }else {
                                    isValider=true;

                                    //region exporté transfert ..
                                    try {
                                        Accueil.BDsql = new SqlServerBD(Login.session.serveur.ip, Login.session.serveur.port, Login.session.serveur.bdName, Login.session.serveur.user, Login.session.serveur.mdp, "net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {

                                            @Override
                                            public void echec() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.faire_transf_conect_err), Toast.LENGTH_LONG).show();
                                                        progress.dismiss();
                                                        isValider = false;
                                                        //revenirAccueil();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void before() {
                                                progress = new ProgressDialog(FaireStockConfig.this); // activité non context ..

                                                progress.setTitle("Connexion");
                                                progress.setMessage("attendez SVP ...");
                                                progress.show();
                                            }

                                            @Override
                                            public void After() throws SQLException {
                                                //region exportation des transfert ..

                                                BonsTransfertAdapter.bons.get(BonsTransfertAdapter.itemSelected).validerTranfert(code_depot);

                                                if (!Synchronisation.isOnSync) {
                                                    Synchronisation.isOnSync = true;
                                                    Synchronisation.exportéBonTransfert();
                                                }

                                                //region afficher msg d err
                                                Accueil.BDsql.es.execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Accueil.BDsql.es.execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Synchronisation.isOnSync = false;
                                                                isValider = false;
                                                                if (!Synchronisation.ExportationErr.equals("") || !FaireTransfert.ExportErr.equals("")) {
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            progress.dismiss();
                                                                            AlertDialog.Builder mb = new AlertDialog.Builder(FaireStockConfig.this); //c est l activity non le context ..

                                                                            View v = getLayoutInflater().inflate(R.layout.div_aff_msg_err, null);
                                                                            TextView msg = (TextView) v.findViewById(R.id.err_msg);
                                                                            final TextView ok = (TextView) v.findViewById(R.id.ok);
                                                                            String msgErr = "<div>";

                                                                            if (!FaireTransfert.ExportErr.equals("")) {
                                                                                msgErr = msgErr + "Erreur de valider le transfert de stock de bon num: " + BonsTransfertAdapter.bons.get(BonsTransfertAdapter.itemSelected).id_bon + ": <br><br> " + FaireTransfert.ExportErr;
                                                                            }

                                                                            if (!Synchronisation.ExportationErr.equals("")) {
                                                                                msgErr = msgErr + "###### EXPORTATION DES TRANSFERT ##### <br><br> " + Synchronisation.ExportationErr + "";
                                                                            }
                                                                            msgErr = msgErr + "</div>";

                                                                            msg.setText(Html.fromHtml(msgErr));
                                                                            Synchronisation.ExportationErr = "";
                                                                            FaireTransfert.ExportErr = "";

                                                                            mb.setView(v);
                                                                            final AlertDialog ad = mb.create();
                                                                            ad.show();
                                                                            ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                                                                            ad.setCancelable(false); //désactiver le button de retour ..

                                                                            ok.setOnClickListener(new View.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(View view) {
                                                                                    revenirAccueil();
                                                                                }
                                                                            });

                                                                        }
                                                                    });
                                                                } else {
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_ok), Toast.LENGTH_SHORT).show();
                                                                            revenirAccueil();
                                                                        }
                                                                    });
                                                                }


                                                            }
                                                        });
                                                    }
                                                });
                                                //endregion

                                                //endregion

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
                            }else {
                                progress.show();
                            }

                        } else {
                            Intent i = new Intent(FaireStockConfig.this, FaireTransfert.class);
                            i.putExtra("code_depot", code_depot);
                            i.putExtra("request", "new_bon");
                            startActivity(i);
                        }
                    }
                }
            });
            //endregion

        }
    }

    public void checkDepotExiste(String codebar_dep){
        Cursor r=Accueil.bd.read("select * from depot where codebar='"+codebar_dep+"'");
        if (r.moveToNext()){
            code_depot=codebar_dep;
            ((TextView)findViewById(R.id.stock_config_depSrc)).setText(r.getString(r.getColumnIndex("nom_dep")));
        }else {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.config_transfert_noCodebar),Toast.LENGTH_SHORT).show();
            code_depot="";
            ((TextView)findViewById(R.id.stock_config_depSrc)).setText("");
        }

    }

    public void revenirAccueil(){
        //region revenir au Accueil ..
        Intent intent = new Intent(getApplicationContext(), Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //endregion
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            checkDepotExiste(data.getExtras().getString("code"));
        }
    }
}
