package com.leadersoft.celtica.lsstock.Preparations;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;
import com.leadersoft.celtica.lsstock.TransfertStock.BonsTransfertAdapter;
import com.leadersoft.celtica.lsstock.TransfertStock.FaireStockConfig;
import com.leadersoft.celtica.lsstock.TransfertStock.FaireTransfert;

import java.sql.SQLException;

public class AfficherProduitPreparer extends AppCompatActivity {
    ProduitPreparerAdapter mAdapter;
    LinearLayout validerBonButt;
    boolean isValider=false;
    int test1=0;
    int min;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_produit_preparer);

        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

            progress=new ProgressDialog(this);

            validerBonButt=(LinearLayout)findViewById(R.id.affprep_validerBon);
            EditText searchInp=(EditText)findViewById(R.id.affprep_saerch);
            ((TextView)findViewById(R.id.affPrep_numBon)).setText(Html.fromHtml("<span><font color='black'>"+getResources().getString(R.string.bon_titre)+"</font> "+BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).code_bon+"<span>"));

            if(getIntent().getExtras().get("request").equals("validé")){
                validerBonButt.setVisibility(View.GONE);
            }



            //region config recyclervew ..
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_pr_prep);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(AfficherProduitPreparer.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new ProduitPreparerAdapter(AfficherProduitPreparer.this);

            mRecyclerView.setAdapter(mAdapter);
            afficherPrPrep("");
            //endregion

            //region la recherche ..
            searchInp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    afficherPrPrep(s.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            //endregion

            //region faire preparation ..
            ((ImageView)findViewById(R.id.affprep_addPrep)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProduitPreparerAdapter.produits=BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).getProduitAPreparer();
                    Intent i=new Intent(AfficherProduitPreparer.this,FairePreparationConfig.class);
                    i.putExtra("request","new_bon");
                    startActivity(i);
                }
            });
            //endregion

            //region valider préparation ..
            validerBonButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).estPréparé()){
                       Toast.makeText(getApplicationContext(),getResources().getString(R.string.aff_prep_noPrep),Toast.LENGTH_SHORT).show();
                    }else {
                        BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).validerBon();
                        ((LinearLayout)findViewById(R.id.affprep_validerBon)).setVisibility(View.GONE);
                        isValider=true;

                        //region exporté préparation ..
                        try {
                            Accueil.BDsql = new SqlServerBD(Login.session.serveur.ip, Login.session.serveur.port, Login.session.serveur.bdName, Login.session.serveur.user, Login.session.serveur.mdp, "net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {

                                @Override
                                public void echec() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.faire_prep_conect_err), Toast.LENGTH_LONG).show();
                                            progress.dismiss();
                                            revenirAccueil();
                                        }
                                    });

                                }

                                @Override
                                public void before() {
                                    progress = new ProgressDialog(AfficherProduitPreparer.this); // activité non context ..

                                    progress.setTitle("Connexion");
                                    progress.setMessage("attendez SVP ...");
                                    progress.show();
                                }

                                @Override
                                public void After() throws SQLException {
                                    //region exportation des productions ..

                                    if (!Synchronisation.isOnSync) {
                                        Synchronisation.isOnSync = true;
                                        Synchronisation.exportéPréparation();
                                    }

                                    //region afficher msg d err
                                    Accueil.BDsql.es.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            Accueil.BDsql.es.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Synchronisation.isOnSync = false;

                                                    if (!Synchronisation.ExportationErr.equals("")) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                progress.dismiss();
                                                                AlertDialog.Builder mb = new AlertDialog.Builder(AfficherProduitPreparer.this); //c est l activity non le context ..

                                                                View v = getLayoutInflater().inflate(R.layout.div_aff_msg_err, null);
                                                                TextView msg = (TextView) v.findViewById(R.id.err_msg);
                                                                final TextView ok = (TextView) v.findViewById(R.id.ok);


                                                                msg.setText(Synchronisation.ExportationErr+"");
                                                                Synchronisation.ExportationErr = "";

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



                }
            });
            //endregion

            if(getIntent().getExtras().getString("request").equals("archive")){
                ((ImageView)findViewById(R.id.affprep_addPrep)).setVisibility(View.GONE);
                validerBonButt.setVisibility(View.GONE);
                searchInp.setInputType(InputType.TYPE_NULL);
                searchInp.setBackgroundColor(getResources().getColor(R.color.AppColor));
            }else {

                //region detecter l ouvrage de clavier ..
                final LinearLayout body=((LinearLayout)findViewById(R.id.affprep_root));//le view root de layout ..

                body.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect measureRect = new Rect(); //you should cache this, onGlobalLayout can get called often
                        body.getWindowVisibleDisplayFrame(measureRect);
                        // measureRect.bottom is the position above soft keypad
                        int keypadHeight = body.getRootView().getHeight() - measureRect.bottom;


                        if(test1==0){
                            test1=1;
                            min=keypadHeight;
                        }

                        if (keypadHeight > min) {
                            // keyboard is opened
                            if(!getIntent().getExtras().get("request").equals("validé")){
                                validerBonButt.setVisibility(View.GONE);

                            }

                        } else {
                            if(!getIntent().getExtras().get("request").equals("validé")){
                                validerBonButt.setVisibility(View.VISIBLE);

                            }

                        }
                    }
                });

                //endregion
            }

        }


    }

    public void afficherPrPrep(String hint){
        ProduitPreparerAdapter.produits.clear();
        if (hint.toString().equals("")){
            ProduitPreparerAdapter.produits=BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).getProduitAPreparer();
        }else {
            ProduitPreparerAdapter.produits=BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).getProduitFromDepotOrByName(hint);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if(isValider){
            revenirAccueil();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProduitPreparerAdapter.produits.clear();
    }

    public void revenirAccueil(){
        //region revenir au Accueil ..
        Intent intent = new Intent(getApplicationContext(), Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //endregion
    }
}
