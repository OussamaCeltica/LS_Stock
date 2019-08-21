package com.leadersoft.celtica.lsstock.Preparations;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.ArchiveMenuFrag;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.TransfertStock.AfficherBonsTransfert;

public class AfficherPreparations extends AppCompatActivity {

    BonPreparationAdapter mAdapter;
    int test1=0;
    int min;
    TypeBon type_bon=TypeBon.EN_COURS;
     EditText searchInp;
      LinearLayout div_options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_preparations);


        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

            searchInp=(EditText)findViewById(R.id.affprep_search);
            div_options=((LinearLayout)findViewById(R.id.affprep_divOptions));



            //region configuration recyclerview ..
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_prep);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(AfficherPreparations.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new BonPreparationAdapter(AfficherPreparations.this);

            mRecyclerView.setAdapter(mAdapter);
            //endregion

            //region la Recherche ..
            searchInp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    Cursor r;
                    if (type_bon==TypeBon.EN_COURS){
                        if(s.toString().equals("")){
                            r=Accueil.bd.read("select * from bon_preparation where valider='0' order by date_bon desc");
                        }else {
                            r=Accueil.bd.read2("select * from bon_preparation where valider='0' and code_bon LIKE ? order by date_bon desc",new String[]{"%"+s.toString()+"%"});
                        }
                        afficherBonPrep(r);
                    }else {
                        if(s.toString().equals("")){
                            r=Accueil.bd.read("select * from bon_preparation where valider='1' order by date_bon desc");
                        }else {
                            r=Accueil.bd.read2("select * from bon_preparation where valider='1' and code_bon LIKE ? order by date_bon desc",new String[]{"%"+s.toString()+"%"});
                        }
                        afficherBonValider(r);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            //endregion

            //region séléctionner le type de preparation qui sera afficher  ..

            //region prep validé
            ((LinearLayout)findViewById(R.id.affprep_prepValid)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View view) {
                    ButtClick(view);
                    type_bon=TypeBon.VALIDÉ;
                    setTypeBonTitre(getResources().getString(R.string.prep_titre_valide));
                    Cursor r=Accueil.bd.read("select * from bon_preparation where valider='1' and sync='0' order by date_bon desc");
                    afficherBonValider(r);
                }
            });
            //endregion

            //region prep en cours
            ((LinearLayout)findViewById(R.id.affprep_prepCours)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View view) {
                    ButtClick(view);
                    type_bon=TypeBon.EN_COURS;
                    setTypeBonTitre(getResources().getString(R.string.prep_titre_cours));
                    Cursor r=Accueil.bd.read("select * from bon_preparation where valider='0' order by date_bon desc");
                    afficherBonPrep(r);
                }
            });
            //endregion

            //endregion


            if (getIntent().getExtras() == null) {

                //region detecter l ouvrage de clavier ..
                final FrameLayout body=((FrameLayout)findViewById(R.id.affprep_root));//le view root de layout ..

                body.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect measureRect = new Rect(); //you should cache this, onGlobalLayout can get called often
                        body.getWindowVisibleDisplayFrame(measureRect);
                        // measureRect.bottom is the position above soft keypad
                        int keypadHeight = body.getRootView().getHeight() - measureRect.bottom;

                        Log.e("Width Keybo",""+keypadHeight);
                        if(test1==0){
                            test1=1;
                            min=keypadHeight;
                        }

                        if (keypadHeight > min) {
                            // keyboard is opened
                            div_options.setVisibility(View.GONE);

                        } else {
                            //Keyboard is close ..
                            div_options.setVisibility(View.VISIBLE);

                        }
                    }
                });

                //endregion

                setTypeBonTitre(getResources().getString(R.string.prep_titre_cours));
                Cursor r = Accueil.bd.read("select * from bon_preparation where valider='0' order by date_bon desc ");
                afficherBonPrep(r);
            }else {

                //region menu des archives

                final ArchiveMenuFrag menu_butt=(ArchiveMenuFrag)getSupportFragmentManager().findFragmentById(R.id.archive);
                menu_butt.root.setVisibility(View.VISIBLE);
                //endregion

                setTypeBonTitre("ARCHIVE");
                div_options.setVisibility(View.GONE);
                ((ImageView)findViewById(R.id.affprep_addPrep)).setVisibility(View.GONE);
                searchInp.setInputType(InputType.TYPE_NULL);
                searchInp.setBackgroundColor(getResources().getColor(R.color.AppColor));

                Cursor r = Accueil.bd.read("select * from bon_preparation where sync='1' order by date_bon desc ");
                BonPreparationAdapter.bons.clear();
                while (r.moveToNext()){
                    BonPreparationAdapter.bons.add(new BonPreparation(r.getString(r.getColumnIndex("code_bon")),r.getString(r.getColumnIndex("date_bon")),r.getString(r.getColumnIndex("Oid")),r.getString(r.getColumnIndex("nom_clt")), ETAT.EXPORTÉ));
                }
                mAdapter.notifyDataSetChanged();
            }

        }


    }

    public void afficherBonPrep(Cursor r){
        BonPreparationAdapter.bons.clear();
        while (r.moveToNext()){
            BonPreparationAdapter.bons.add(new BonPreparation(r.getString(r.getColumnIndex("code_bon")),r.getString(r.getColumnIndex("date_bon")),r.getString(r.getColumnIndex("Oid")),r.getString(r.getColumnIndex("nom_clt")), ETAT.EN_COURS));
        }
        mAdapter.notifyDataSetChanged();

    }

    public void afficherBonValider(Cursor r2){
        BonPreparationAdapter.bons.clear();
        while (r2.moveToNext()){
            BonPreparationAdapter.bons.add(new BonPreparation(r2.getString(r2.getColumnIndex("code_bon")),r2.getString(r2.getColumnIndex("date_bon")),r2.getString(r2.getColumnIndex("Oid")),r2.getString(r2.getColumnIndex("nom_clt")),ETAT.VALIDÉ));
        }
        mAdapter.notifyDataSetChanged();
    }

    public void setTypeBonTitre(String type){
        ((TextView)findViewById(R.id.affprep_titre)).setText(Html.fromHtml("<span><font color='black'>"+getResources().getString(R.string.prep_titre)+"</font> "+type+"</span>"));
    }



    public enum TypeBon{
        VALIDÉ,EN_COURS
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public  void ButtClick(final View view){

        view.setBackground(getResources().getDrawable(R.drawable.bg_butt_fonce));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setBackground(getResources().getDrawable(R.drawable.butt_back_degrade));
            }
        },200);
        Login.session.playAudioFromAsset(AfficherPreparations.this,"klik.ogg");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BonPreparationAdapter.bons.clear();
    }
}
