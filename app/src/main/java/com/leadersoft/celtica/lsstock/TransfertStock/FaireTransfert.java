package com.leadersoft.celtica.lsstock.TransfertStock;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.leadersoft.celtica.lsstock.Inventaire.FaireInventaire;
import com.leadersoft.celtica.lsstock.Inventaire.PanierAdapterInventaire;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.Preparations.AfficherProduitPreparer;
import com.leadersoft.celtica.lsstock.Preparations.ProduitPreparerAdapter;
import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.REQUEST_SCANNER;
import com.leadersoft.celtica.lsstock.Session;

public class FaireTransfert extends AppCompatActivity {
    PanierAdapter mAdapter;
    public static FaireTransfert me;
    private CheckBox packging;
    public  static boolean withIncr=false;
    public static String ExportErr="";
     String request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_transfert);

        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

            me=this;

            packging = (CheckBox) findViewById(R.id.faire_inventaire_isPackaging);

            //region configuration de recyclerview ..
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_panier);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(FaireTransfert.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new PanierAdapter(FaireTransfert.this);

            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            //endregion

            request=getIntent().getExtras().getString("request");
            if(request.equals("bon_validé")){
                ((ImageView)findViewById(R.id.add_pr_toPanier)).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.faire_transfert_valider)).setVisibility(View.GONE);
                packging.setVisibility(View.GONE);
            }else if(request.equals("new_bon")) {
                BonsTransfertAdapter.itemSelected = -1;
            }

            //region ajouter produit au panier
            ((ImageView)findViewById(R.id.add_pr_toPanier)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    CodeBarScanner.requestScanner= REQUEST_SCANNER.TRANSFERT;
                    Login.session.openScannerCodeBarreIncr(FaireTransfert.this, new Session.OnScanListenerIncr() {

                        @Override
                        public void OnScanIncrNotChecked(final String code,LinearLayout body,AlertDialog ad) {
                            withIncr=false;
                            Log.e("ttt",withIncr+" / OnNot");
                            if (checkPrExiste(code)){
                                Login.session.changeColorOnScan(body,getResources().getColor(R.color.Green),getResources().getColor(R.color.White));
                                ad.dismiss();
                                affDivSetQt(code);
                            }else {
                                Login.session.changeColorOnScan(body,getResources().getColor(R.color.Red),getResources().getColor(R.color.White));

                            }
                        }

                        @Override
                        public void OnScan(String code,LinearLayout body) {

                            withIncr=true;
                            Log.e("ttt",withIncr+"/ On");
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

            //region valider le bon de transfert en cours ..
            ((TextView)findViewById(R.id.faire_transfert_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (PanierAdapter.produits.size() == 0){
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.faire_transf_panierVide), Toast.LENGTH_SHORT).show();
                    }else {
                        if(request.equals("new_bon")) {
                            BonTransfertEnCours bon = new BonTransfertEnCours(getIntent().getExtras().getString("code_depot"));
                            bon.addToBD();
                            int i = 0;
                            while (i != PanierAdapter.produits.size()) {
                                PanierAdapter.produits.get(i).addToBD(bon.id_bon);
                                i++;
                            }

                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_ok), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            Intent i2 = new Intent(getApplicationContext(), AfficherBonsTransfert.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivities(new Intent[]{intent,i2});
                        }else {
                            BonTransfertEnCours bon = new BonTransfertEnCours(BonsTransfertAdapter.bons.get(BonsTransfertAdapter.itemSelected).id_bon,getIntent().getExtras().getString("code_depot"));
                            bon.suppPrTransferer();
                            int i = 0;
                            while (i != PanierAdapter.produits.size()) {
                                PanierAdapter.produits.get(i).addToBD(bon.id_bon);
                                i++;
                            }
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_ok), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                }
            });
            //endregion
        }
    }

    public boolean checkPrExiste(String code){

        Produit p=new Produit(code);
        if (p.existe()) {
            if (p.isPackaging){
                packging.setChecked(true);
            }
            p.isPackaging=false;
            double qt=0;
            if (packging.isChecked()) p.isPackaging=true;
            if (withIncr)qt=1;//pour l affichage de quantity: si avec incr j ajout 1 par default ..
            Login.session.playAudioFromAsset(FaireTransfert.this,"barcode_succ.wav");
            Toast.makeText(getApplicationContext(),"Produit: "+p.nom+" \n Quantité: "+(mAdapter.getProduitQt(p)+qt),Toast.LENGTH_SHORT).show();
            return true;
        }else{
            Login.session.playAudioFromAsset(FaireTransfert.this,"barcode_err.wav");
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.codebar_noExist),Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void affDivSetQt(final String code){

        AlertDialog.Builder mb = new AlertDialog.Builder(FaireTransfert.this); //c est l activity non le context ..
        View v= getLayoutInflater().inflate(R.layout.div_qt_pr,null);
        TextView valider=(TextView) v.findViewById(R.id.valider_pr_panier);
        TextView nomPr=(TextView) v.findViewById(R.id.panier_qt_nom_pr);
        final EditText qt=(EditText)v.findViewById(R.id.panier_qt);
        mb.setView(v);
        final AlertDialog ad=mb.create();
        ad.show();
        ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..

        qt.requestFocus();

        Login.session.opneClavier(FaireTransfert.this);

        qt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if( !qt.getText().toString().equals("")){
                        Login.session.closeClavier(FaireTransfert.this,qt);
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
                    Login.session.closeClavier(FaireTransfert.this,qt);
                    addPrToPanier(code,Double.parseDouble(qt.getText().toString()));
                    ad.dismiss();

                }
            }
        });


    }

    public void addPrToPanier(String code,double qt){

        Produit p=new Produit(code);
        p.existe();
            mAdapter.addPrToPanier(new ProduitTransferé(p.codebar, p.nom, qt,packging.isChecked()));
            mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            final String code=data.getExtras().getString("code");
            withIncr=false;
            if (checkPrExiste(code)){
                affDivSetQt(code);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(PanierAdapter.produits.size() != 0 && !request.equals("bon_validé")  ){
            AlertDialog.Builder mb = new AlertDialog.Builder(FaireTransfert.this); //c est l activity non le context ..

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
        PanierAdapter.produits.clear();
    }
}
