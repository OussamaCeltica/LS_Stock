package com.leadersoft.celtica.lsstock.Inventaire;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.CodeBarScanner;
import com.leadersoft.celtica.lsstock.EntréeSortie.FaireEntre;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.REQUEST_SCANNER;
import com.leadersoft.celtica.lsstock.Session;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;
import com.leadersoft.celtica.lsstock.TransfertStock.BonTransfertEnCours;
import com.leadersoft.celtica.lsstock.TransfertStock.BonsTransfertAdapter;
import com.leadersoft.celtica.lsstock.TransfertStock.FaireTransfert;
import com.leadersoft.celtica.lsstock.TransfertStock.PanierAdapter;
import com.leadersoft.celtica.lsstock.TransfertStock.ProduitTransferé;

import java.sql.SQLException;

public class FaireInventaire extends AppCompatActivity {

    PanierAdapterInventaire mAdapter;
    CheckBox packging;
    ProgressDialog progress;
    public static FaireInventaire me;
    public  static boolean withIncr=false;
    String request;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_inventaire);

        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

            me=this;
            packging = (CheckBox) findViewById(R.id.faire_inventaire_isPackaging);
            progress=new ProgressDialog(this);


            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_panier);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(FaireInventaire.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new PanierAdapterInventaire(FaireInventaire.this);

            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

            request=getIntent().getExtras().getString("request");
            if(request.equals("bon_validé")){
                ((ImageView)findViewById(R.id.add_pr_toPanier)).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.faire_inventaire_valider)).setVisibility(View.GONE);
                packging.setVisibility(View.GONE);
            }else if(request.equals("new_bon")) {
                BonInventaireAdapter.itemSelected = -1;
            }

            //region ajouter produit au panier
            ((ImageView) findViewById(R.id.add_pr_toPanier)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CodeBarScanner.requestScanner= REQUEST_SCANNER.INVENTAIRE;
                    Login.session.openScannerCodeBarreIncr(FaireInventaire.this, new Session.OnScanListenerIncr() {
                        @Override
                        public void OnScanIncrNotChecked(final String code, final LinearLayout body, AlertDialog ad) {
                            withIncr=false;
                            if (checkPrExiste(code)){
                                ad.dismiss();
                                affDivSetQt(code);
                            }else {
                                Login.session.changeColorOnScan(body,getResources().getColor(R.color.Red),getResources().getColor(R.color.White));

                            }
                        }

                        @Override
                        public void OnScan(String code, final LinearLayout body) {
                            withIncr=true;
                            if (checkPrExiste(code)) {
                                Login.session.changeColorOnScan(body,getResources().getColor(R.color.Green),getResources().getColor(R.color.White));
                                addPrToPanier(code,1);
                            }else {
                                Login.session.changeColorOnScan(body,getResources().getColor(R.color.Red),getResources().getColor(R.color.White));
                            }
                        }
                    });
                }
            });
            //endregion

            //region valider le bon ..
            ((TextView) findViewById(R.id.faire_inventaire_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (PanierAdapterInventaire.produits.size() == 0){
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.faire_transf_panierVide), Toast.LENGTH_SHORT).show();
                    }else {
                        BonInventaire bon = new BonInventaire(getIntent().getExtras().getString("code_depot"));
                        bon.addToBD();
                        int i = 0;
                        while (i != PanierAdapterInventaire.produits.size()) {
                            PanierAdapterInventaire.produits.get(i).addToBD(bon.id_bon);
                            i++;
                        }

                        Synchronisation.connecterSQL(FaireInventaire.this, new SqlServerBD.doAfterBeforeConnect() {
                            @Override
                            public void echec() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_ok), Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        Intent i=new Intent(getApplicationContext(),AfficherInventaires.class);
                                        startActivities(new Intent[]{intent,i});
                                    }
                                });
                            }

                            @Override
                            public void before() {

                            }

                            @Override
                            public void After() throws SQLException {
                                if (!Synchronisation.isOnSync) {
                                    Synchronisation.isOnSync = true;
                                    Synchronisation.exportéBonInventaire();
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
                                                            AlertDialog.Builder mb = new AlertDialog.Builder(FaireInventaire.this); //c est l activity non le context ..

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

                }
            });
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

    public boolean checkPrExiste(String code){
        Produit p=new Produit(code);
        if (p.existe()) {
            p.isPackaging=false;
            double qt=0;
            if (packging.isChecked()) p.isPackaging=true;
            if (withIncr)qt=1;//pour l affichage de quantity: si avec incr j ajout 1 par default ..
            Login.session.playAudioFromAsset(FaireInventaire.this,"barcode_succ.mp3");
            Toast.makeText(getApplicationContext(),"Produit: "+p.nom+" \n Quantité: "+(mAdapter.getProduitQt(p)+qt),Toast.LENGTH_SHORT).show();
            return true;
        }else{
            Login.session.playAudioFromAsset(FaireInventaire.this,"barcode_err.wav");
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.codebar_noExist),Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void affDivSetQt(final String code){

        AlertDialog.Builder mb = new AlertDialog.Builder(FaireInventaire.this); //c est l activity non le context ..

        View v= getLayoutInflater().inflate(R.layout.div_qt_pr,null);
        TextView valider=(TextView) v.findViewById(R.id.valider_pr_panier);
        TextView nomPr=(TextView) v.findViewById(R.id.panier_qt_nom_pr);
        final EditText qt=(EditText)v.findViewById(R.id.panier_qt);

        mb.setView(v);
        final AlertDialog ad=mb.create();
        ad.show();
        ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..

        qt.requestFocus();


        Login.session.opneClavier(FaireInventaire.this);

        qt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if( !qt.getText().toString().equals("")){
                        Login.session.closeClavier(FaireInventaire.this,qt);
                        addPrToPanier(code,Double.parseDouble(qt.getText().toString()));
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
                    Login.session.closeClavier(FaireInventaire.this,qt);
                    addPrToPanier(code,Double.parseDouble(qt.getText().toString()));
                    ad.dismiss();

                }
            }
        });


    }

    public void addPrToPanier(String code,double qt){

        Produit p=new Produit(code);
        p.existe();
        mAdapter.addPrToPanier(new ProduitInventaire(code, p.nom, qt,packging.isChecked()));
        mAdapter.notifyDataSetChanged();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            final String code=data.getExtras().getString("code");
            if (checkPrExiste(code)){
                affDivSetQt(code);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(PanierAdapterInventaire.produits.size() != 0 && !request.equals("bon_validé") ){
            AlertDialog.Builder mb = new AlertDialog.Builder(FaireInventaire.this); //c est l activity non le context ..

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



        }else
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PanierAdapterInventaire.produits.clear();
    }
}
