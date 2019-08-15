package com.leadersoft.celtica.lsstock.EntréeSortie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.CodeBarScanner;
import com.leadersoft.celtica.lsstock.Depot;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.Preparations.BonPreparationAdapter;
import com.leadersoft.celtica.lsstock.Preparations.FairePreparation;
import com.leadersoft.celtica.lsstock.Preparations.PanierPreparationAdapter;
import com.leadersoft.celtica.lsstock.Preparations.ProduitDansBon;
import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.REQUEST_SCANNER;
import com.leadersoft.celtica.lsstock.Session;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;

import java.sql.SQLException;

public class FaireSortie extends AppCompatActivity {

    public PanierSortieAdapter mAdapter;
    LinearLayout root,divAddPr;
    TextView depotInp,prInp;
    EditText qt;
    boolean prDemandeScan=false;
    public ProduitSortie pre=new ProduitSortie("","");
    String request;
    public CheckBox checkCarton;
    ProgressDialog progress;
    public boolean withIncr=false;
    public static FaireSortie me;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_sortie);

        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            me=this;
            progress=new ProgressDialog(this);
            pre.etat= ETAT.EN_COURS;
            root=(LinearLayout)findViewById(R.id.faireSortie_root);
            divAddPr=(LinearLayout)findViewById(R.id.faireSortie_divAddPr);
            depotInp=(TextView)findViewById(R.id.faireSortie_dep);
            prInp=(TextView)findViewById(R.id.faireSortie_pr);
            qt=(EditText) findViewById(R.id.faireSortie_qt);
            checkCarton=(CheckBox)findViewById(R.id.addPr_checkCarton);

            //region configuration recyclerview ..
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.faireSortie_panier);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(FaireSortie.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new PanierSortieAdapter(FaireSortie.this);

            mRecyclerView.setAdapter(mAdapter);
            //endregion

            //region check type request ..
            request=getIntent().getExtras().getString("request");
            if (request.equals("validé")){
                ((ImageView)findViewById(R.id.faireSortie_addPr)).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.faireSortie_valider)).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.faireSortie_dep)).setVisibility(View.GONE);
                checkCarton.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            }else {

                //region select produit --DEPRECATED-- ..
                ((TextView)findViewById(R.id.faireSortie_pr)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prDemandeScan=true;
                        if (pre.code_dep.equals("")){
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.faireEntre_noDepot),Toast.LENGTH_SHORT).show();
                        }else {
                            Login.session.openScannerCodeBarre(FaireSortie.this, new Session.OnScanListener() {
                                @Override
                                public void OnScan(String code, LinearLayout div_scanner) {
                                    if(checkProduitExiste(code))
                                        affDivSetQt(code);

                                }
                            });
                        }
                    }
                });
                //endregion

                //region select depot ..
                ((TextView)findViewById(R.id.faireSortie_dep)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prDemandeScan=false;
                        Login.session.openScannerCodeBarre(FaireSortie.this, new Session.OnScanListener() {
                            @Override
                            public void OnScan(String code, LinearLayout div_scanner) {
                                checkDepotExiste(code);

                            }
                        });
                    }
                });
                //endregion

                //region ajouter un produit ...
                ((ImageView)findViewById(R.id.faireSortie_addPr)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CodeBarScanner.requestScanner= REQUEST_SCANNER.SORTIE_STOCK;
                        prDemandeScan=true;

                        if (pre.code_dep == null){
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.faireEntre_noDepot),Toast.LENGTH_SHORT).show();
                        }else {
                            Login.session.openScannerCodeBarreIncr(FaireSortie.this, new Session.OnScanListenerIncr() {
                                @Override
                                public void OnScanIncrNotChecked(String code, LinearLayout div_scanner, AlertDialog ad) {
                                    withIncr=false;
                                    ad.dismiss();
                                    if(checkProduitExiste(code)) {
                                        Login.session.changeColorOnScan(div_scanner,getResources().getColor(R.color.Green),getResources().getColor(R.color.White));
                                        affDivSetQt(code);
                                    }else {
                                        Login.session.changeColorOnScan(div_scanner,getResources().getColor(R.color.Red),getResources().getColor(R.color.White));
                                    }
                                }

                                @Override
                                public void OnScan(String code, LinearLayout body) {
                                    withIncr=true;

                                    if(checkProduitExiste(code)) {
                                        Login.session.changeColorOnScan(body,getResources().getColor(R.color.Green),getResources().getColor(R.color.White));
                                        addProduit(code,1);
                                    }else {
                                        Login.session.changeColorOnScan(body,getResources().getColor(R.color.Red),getResources().getColor(R.color.White));
                                    }

                                }
                            });
                        }




                        /*
                        root.setVisibility(View.GONE);
                        divAddPr.setVisibility(View.VISIBLE);

                        ((TextView)findViewById(R.id.faireEntre_validerPr)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (qt.getText().toString().equals("") || depotInp.getText().toString().equals("") || prInp.getText().toString().equals("")){
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.remplissage_err),Toast.LENGTH_SHORT).show();
                                }else {
                                    pre.qt=Double.parseDouble(qt.getText().toString());
                                    if (checkCarton.isChecked()) pre.isPackaging=true;
                                    else pre.isPackaging=false;
                                    mAdapter.addToPanier(pre);
                                    qt.setText("");
                                    prInp.setText("");
                                    divAddPr.setVisibility(View.GONE);
                                    root.setVisibility(View.VISIBLE);
                                    pre=new ProduitEntree("","",pre.code_dep+"",pre.nom_dep+"");
                                    pre.etat= ETAT.EN_COURS;

                                }

                            }

                        });
                        */
                    }
                });
                //endregion

                //region valider le bon ..
                ((TextView)findViewById(R.id.faireSortie_valider)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (PanierSortieAdapter.produits.size()==0){
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.faire_transf_panierVide),Toast.LENGTH_SHORT).show();
                        }else {
                            final BonSortie bon=new BonSortie(getIntent().getExtras().getString("code_clt"),getIntent().getExtras().getString("nom_clt"));
                            try {
                                bon.addToBD();

                                for (ProduitSortie pr:PanierSortieAdapter.produits){
                                    pr.addToBD(bon.id_bon);
                                }
                                ((TextView)findViewById(R.id.faireSortie_valider)).setVisibility(View.GONE);
                                ((ImageView)findViewById(R.id.faireSortie_addPr)).setVisibility(View.GONE);

                                Synchronisation.connecterSQL(FaireSortie.this, new SqlServerBD.doAfterBeforeConnect() {
                                    @Override
                                    public void echec() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.faireEntre_conect_err),Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                Intent i=new Intent(getApplicationContext(), AfficherSortie.class);
                                                startActivities(new Intent[]{intent,i});
                                            }
                                        });
                                    }

                                    @Override
                                    public void before() {
                                        progress.setTitle("Connexion");
                                        progress.setMessage("attendez SVP ...");
                                        progress.show();
                                    }

                                    @Override
                                    public void After() throws SQLException {

                                        if (!Synchronisation.isOnSync) {
                                            Synchronisation.isOnSync = true;
                                            Synchronisation.exportéBonSortie();
                                        }

                                        //region afficher msg d err
                                        Accueil.BDsql.es.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                Accueil.BDsql.es.execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Synchronisation.isOnSync = false;
                                                        if (!Synchronisation.ExportationErr.equals("") ) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    progress.dismiss();
                                                                    AlertDialog.Builder mb = new AlertDialog.Builder(FaireSortie.this); //c est l activity non le context ..

                                                                    View v = getLayoutInflater().inflate(R.layout.div_aff_msg_err, null);
                                                                    TextView msg = (TextView) v.findViewById(R.id.err_msg);
                                                                    final TextView ok = (TextView) v.findViewById(R.id.ok);

                                                                    msg.setText(Synchronisation.ExportationErr);
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

                                    }
                                });
                            }
                            catch (SQLiteException e){
                                Toast.makeText(getApplicationContext(),e.getMessage()+"",Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });
                //endregion
            }
            //endregion


        }

    }

    public void revenirAccueil(){
        //region revenir au Accueil ..
        Intent intent = new Intent(getApplicationContext(), Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //endregion
    }

    public void addProduit(String code,double qt){
        pre.qt=qt;
        if (checkCarton.isChecked()) pre.isPackaging=true;
        else pre.isPackaging=false;
        mAdapter.addToPanier(pre);
        pre=new ProduitSortie("","",pre.code_dep+"",pre.nom_dep+"");
        pre.etat= ETAT.EN_COURS;
    }

    public boolean checkProduitExiste(String code){

        Produit p=new Produit(code);
        if(p.existe()){
            Login.session.playAudioFromAsset(me,"barcode_succ.wav");
            if (p.isPackaging) checkCarton.setChecked(true);
            prInp.setText(p.nom);
            pre.nom=p.nom;
            pre.codebar=code;

            return true;

        }else {
            Login.session.playAudioFromAsset(me,"barcode_err.wav");
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.codebar_noExist),Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    public void checkDepotExiste(String code){
        Depot d=new Depot(code);
        if(d.existe()){
            Login.session.playAudioFromAsset(me,"barcode_succ.wav");
            depotInp.setText(d.nom_dep);
            pre.nom_dep=d.nom_dep;
            pre.code_dep=code;
        }else {
            Login.session.playAudioFromAsset(me,"barcode_err.wav");
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.codebar_noExist),Toast.LENGTH_SHORT).show();
        }


    }

    public void affDivSetQt(final String code){

        AlertDialog.Builder mb = new AlertDialog.Builder(FaireSortie.this); //c est l activity non le context ..
        View v= getLayoutInflater().inflate(R.layout.div_qt_pr,null);
        TextView valider=(TextView) v.findViewById(R.id.valider_pr_panier);
        TextView nomPr=(TextView) v.findViewById(R.id.panier_qt_nom_pr);
        final EditText qt=(EditText)v.findViewById(R.id.panier_qt);
        mb.setView(v);
        final AlertDialog ad=mb.create();
        ad.show();
        ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..

        qt.requestFocus();

        Login.session.opneClavier(FaireSortie.this);

        qt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if( !qt.getText().toString().equals("")){
                        Login.session.closeClavier(FaireSortie.this,qt);
                        addProduit(code,Double.parseDouble(qt.getText().toString()));
                        ad.dismiss();
                    }
                }
                return false;
            }
        });

        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !qt.getText().toString().equals("")){
                    Login.session.closeClavier(FaireSortie.this,qt);
                    addProduit(code,Double.parseDouble(qt.getText().toString()));
                    ad.dismiss();

                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (prDemandeScan) {
                if(checkProduitExiste(data.getExtras().getString("code")))
                affDivSetQt(data.getExtras().getString("code"));
            }
            else
                checkDepotExiste(data.getExtras().getString("code"));
        }

    }

    @Override
    public void onBackPressed() {

        if(request.equals("new_bon")){
            if (PanierSortieAdapter.produits.isEmpty()){
                super.onBackPressed();
            }else {
                AlertDialog.Builder mb = new AlertDialog.Builder(FaireSortie.this); //c est l activity non le context ..

                View v= getLayoutInflater().inflate(R.layout.confirm_box,null);
                TextView oui=(TextView) v.findViewById(R.id.confirm_oui);
                TextView non=(TextView) v.findViewById(R.id.confirm_non);
                TextView msg=(TextView) v.findViewById(R.id.confirm_msg);

                msg.setText(getResources().getString(R.string.panier_noVide_quiter));

                mb.setView(v);
                final AlertDialog ad=mb.create();
                ad.show();
                ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                ad.setCancelable(false); //désactiver le button de retour ..

                non.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ad.dismiss();
                    }
                });

                oui.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

            }
        }else {
            super.onBackPressed();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PanierSortieAdapter.produits.clear();
    }
}
