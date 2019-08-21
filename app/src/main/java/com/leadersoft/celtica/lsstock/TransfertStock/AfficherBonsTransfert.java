package com.leadersoft.celtica.lsstock.TransfertStock;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.ArchiveMenuFrag;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.Inventaire.AfficherInventaires;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.R;

public class AfficherBonsTransfert extends AppCompatActivity {
    BonsTransfertAdapter mAdapter;
    Cursor r;
    ETAT affichage=ETAT.EN_COURS;
    int sync;
    EditText inputSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_bons_transfert);


        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

            inputSearch=(EditText)findViewById(R.id.afftrans_search);

            //region configuration de recyclerview et affichage ..
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_bonTransfert);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(AfficherBonsTransfert.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new BonsTransfertAdapter(AfficherBonsTransfert.this);

            mRecyclerView.setAdapter(mAdapter);

            if (getIntent().getExtras() == null){
                sync=0;

                if (!Login.session.mode.equals("admin"))
                    changeAffichage(ETAT.EN_COURS);

            }

            //region mode archive ..
            else {
                sync=1;
                affichage=ETAT.VALIDÉ;
                ((LinearLayout)findViewById(R.id.affTrans_divOptions)).setVisibility(View.GONE);

                //region menu des archives
                ((ImageView) findViewById(R.id.add_bonTransfert)).setVisibility(View.GONE);
                final ArchiveMenuFrag menu_butt=(ArchiveMenuFrag)getSupportFragmentManager().findFragmentById(R.id.archive);
                menu_butt.root.setVisibility(View.VISIBLE);
                //endregion

                r=Accueil.bd.read("select * from bon_transfert where sync='1' order by id desc");
            }
            //endregion

            afficherBons2(r);
            //endregion

            //region La recherche ..
            inputSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    r=Accueil.bd.read2("select * from bon_transfert where   "+(sync == 1 ? " sync='1'"  : ((affichage == ETAT.EN_COURS  )? "codebar_depot_dest is null  and sync='0'" : "codebar_depot_dest is not null  and ( sync='0' or (sync='1' and julianday('now') - julianday(date_transfert)<= 2) )")+"  and code_emp='"+Login.session.employe.code_emp+"'")+"  and nom_depot_src like ? order by id desc",new String[]{"%"+s.toString()+"%"} );
                    afficherBons2(r);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            //endregion

            //region changer l affichage
            ((LinearLayout)findViewById(R.id.affTrans_prepValid)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    ButtClick(v);
                    changeAffichage(ETAT.VALIDÉ);
                    afficherBons2(r);
                }
            });

            ((LinearLayout)findViewById(R.id.affTrans_prepCours)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    ButtClick(v);
                    changeAffichage(ETAT.EN_COURS);
                    afficherBons2(r);
                }
            });
            //endregion

            //region add new bonTransfert
            ((ImageView) findViewById(R.id.add_bonTransfert)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Login.session.mode.equals("admin")){
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.faire_transf_noEmp),Toast.LENGTH_SHORT).show();
                    }else {
                        Intent i=new Intent(AfficherBonsTransfert.this,FaireStockConfig.class);
                        i.putExtra("request","depot_src");
                        startActivity(i);
                    }

                }
            });

            //endregion

        }
    }


    public void afficherBons2(Cursor r){
        BonsTransfertAdapter.bons.clear();
        while (r.moveToNext()){
            BonsTransfertAdapter.bons.add(new BonTransfert(r.getString(r.getColumnIndex("id")),r.getString(r.getColumnIndex("codebar_depot_src")),r.getString(r.getColumnIndex("nom_depot_src")),r.getString(r.getColumnIndex("codebar_depot_dest"))+"",r.getString(r.getColumnIndex("nom_depot_dest"))+"",r.getString(r.getColumnIndex("date_transfert")),(r.getString(r.getColumnIndex("etat")).equals("en cours") == true ? ETAT.EN_COURS :(r.getString(r.getColumnIndex("etat")).equals("exporté") ? ETAT.EXPORTÉ : ETAT.VALIDÉ))));
        }
        mAdapter.notifyDataSetChanged();
    }


    public void changeAffichage(ETAT e){
        affichage=e;
        r=Accueil.bd.read("select * from bon_transfert where "+((affichage == ETAT.EN_COURS  )? "codebar_depot_dest is null and sync='0'" : "codebar_depot_dest is not null  and ( sync='0' or (sync='1' and julianday('now') - julianday(date_transfert)<= 2) )")+"  and code_emp='"+Login.session.employe.code_emp+"' order by id desc" );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BonsTransfertAdapter.bons.clear();
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
        Login.session.playAudioFromAsset(AfficherBonsTransfert.this,"klik.ogg");
    }
}
