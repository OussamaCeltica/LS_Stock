package com.leadersoft.celtica.lsstock.Preparations;

import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
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
import com.leadersoft.celtica.lsstock.Inventaire.FaireInventaire;
import com.leadersoft.celtica.lsstock.Inventaire.PanierAdapterInventaire;
import com.leadersoft.celtica.lsstock.Inventaire.ProduitInventaire;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.REQUEST_SCANNER;
import com.leadersoft.celtica.lsstock.Session;

public class FairePreparation extends AppCompatActivity {

    ImageView addPr;
    PanierPreparationAdapter mAdapter;
    Depot depot;
    public static FairePreparation me;
    public  static boolean withIncr=false;
    CheckBox cartonCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_preparation);

        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

            me=this;
            cartonCheck=(CheckBox)findViewById(R.id.fairePrep_cartonCheck);
            String request=getIntent().getExtras().getString("request");

             depot=new Depot(getIntent().getExtras().getString("code_depot"));
            ((TextView)findViewById(R.id.faire_prep_depot)).setText(Html.fromHtml("<span><font color='#f9a327'>"+getResources().getString(R.string.div_prep_depot)+" </font>"+depot.getNomDepot()+"</span>"));

            //region configuration recyclerview
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.faire_prep_panier);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(FairePreparation.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new PanierPreparationAdapter(FairePreparation.this);

            mRecyclerView.setAdapter(mAdapter);

            PanierPreparationAdapter.produits=BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).getProduitFromDepot(depot.code_dep);

            mAdapter.notifyDataSetChanged();
            //endregion

            //region add produit to panier ..
            ((ImageView)findViewById(R.id.faire_prep_addPr)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CodeBarScanner.requestScanner= REQUEST_SCANNER.PREPARATION;
                    Login.session.openScannerCodeBarreIncr(FairePreparation.this, new Session.OnScanListenerIncr() {
                        @Override
                        public void OnScanIncrNotChecked(String code, LinearLayout body, AlertDialog ad) {
                            if (checkPrExiste(code)){
                                ad.dismiss();
                                affDivSetQt(code);
                            }else {
                                Login.session.changeColorOnScan(body,getResources().getColor(R.color.Red),getResources().getColor(R.color.White));

                            }
                        }

                        @Override
                        public void OnScan(String code, LinearLayout body) {
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

            //region valider la MAJ des produits dans le bons ..
            ((TextView)findViewById(R.id.faire_prep_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).suppPrPreparer();
                    for (ProduitDansBon pr:PanierPreparationAdapter.produits){
                        pr.updatePreparation(BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).code_bon);
                    }
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_ok),Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), Accueil.class);
                    Intent i = new Intent(getApplicationContext(), AfficherPreparations.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivities(new Intent[]{intent,i});

                }
            });
            //endregion

        }
    }

    public boolean checkPrExiste(String code) {
        Produit p = new Produit(code);
        if (p.existe()) {
            if (p.isPackaging) cartonCheck.setChecked(true);

            if (cartonCheck.isChecked()) {
                if (p.qt_carton == 0) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.carton_noQt), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            double qt = 0;
            if (withIncr)
                qt = 1;//pour l affichage de quantity: si avec incr j ajout 1 par default ..
            Login.session.playAudioFromAsset(FairePreparation.this, "barcode_succ.mp3");
            //Toast.makeText(getApplicationContext(),"Produit: "+p.nom+" \n Quantité: "+(mAdapter.getProduitQt(p)+qt),Toast.LENGTH_SHORT).show();

            return true;
        }else {
            Login.session.playAudioFromAsset(FairePreparation.this, "barcode_err.wav");
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.codebar_noExist), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void affDivSetQt(final String code){

        AlertDialog.Builder mb = new AlertDialog.Builder(FairePreparation.this); //c est l activity non le context ..

        View v= getLayoutInflater().inflate(R.layout.div_qt_pr,null);
        TextView valider=(TextView) v.findViewById(R.id.valider_pr_panier);
        TextView nomPr=(TextView) v.findViewById(R.id.panier_qt_nom_pr);
        final EditText qt=(EditText)v.findViewById(R.id.panier_qt);

        mb.setView(v);
        final AlertDialog ad=mb.create();
        ad.show();
        ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..

        qt.requestFocus();


        Login.session.opneClavier(FairePreparation.this);

        qt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                if( !qt.getText().toString().equals("")){
                    Login.session.closeClavier(FairePreparation.this,qt);
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
                Login.session.closeClavier(FairePreparation.this,qt);
                addPrToPanier(code,Double.parseDouble(qt.getText().toString()));
                ad.dismiss();

            }
            }
        });


    }

    public void addPrToPanier(String code,double qt){
        Produit p=new Produit(code);
        p.existe();
        mAdapter.addPrToPanier(new ProduitDansBon(p.codebar, p.nom,depot.code_dep+"",depot.nom_dep+"",0.0,((cartonCheck.isChecked()==true) ? qt * p.qt_carton : qt)));
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
}
