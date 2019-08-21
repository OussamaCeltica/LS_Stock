package com.leadersoft.celtica.lsstock.EntréeSortie;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.ArchiveMenuFrag;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsstock.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsstock.Preparations.AfficherPreparations;
import com.leadersoft.celtica.lsstock.Preparations.BonPreparationAdapter;
import com.leadersoft.celtica.lsstock.Preparations.BonPreparationValidé;
import com.leadersoft.celtica.lsstock.R;

import java.util.ArrayList;

public class AfficherSortie extends AppCompatActivity {
    BonSortieAdapter mAdapter;
    int test1=0;
    int min;
    MySpinnerSearchable spinnerClt;
    Cursor r;
    EditText searchInp;
    int sync=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_sortie);

        if (savedInstanceState != null) {
            //region Revenir a au Login ..
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion

        }else {
            searchInp=(EditText)findViewById(R.id.affsortie_search);

            //region configuration recyclerview ..
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_sortie);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(AfficherSortie.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new BonSortieAdapter(AfficherSortie.this);

            mRecyclerView.setAdapter(mAdapter);
            //endregion

            //region mode normal ..
            if (getIntent().getExtras() == null){
                sync=0;
            }
            //endregion

            //region mode archive ..
            else {
                sync=1;
                //region menu des archives
                ((ImageView) findViewById(R.id.affSortie_addPrep)).setVisibility(View.GONE);
                final ArchiveMenuFrag menu_butt=(ArchiveMenuFrag)getSupportFragmentManager().findFragmentById(R.id.archive);
                menu_butt.root.setVisibility(View.VISIBLE);
                //endregion

            }
            //endregion

            //region la recherche ..
            searchInp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    r=Accueil.bd.read2("select * from bon_sortie where (sync='"+sync+"' "+(Login.session.mode.equals("admin")== true ? ")" : (sync == 1 ? ")" : "or ( sync='1' and  julianday('now') - julianday(date_bon)<= 2)) and code_emp='"+Login.session.employe.code_emp+"' "))+" and (code_bon LIKE ? or nom_clt LIKE ?)  order by code_bon desc",new String[]{"%"+s.toString()+"%","%"+s.toString()+"%"});

                    afficherBons(r);

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            //endregion

            //region select un client et faire sortie ..
            ((ImageView)findViewById(R.id.affSortie_addPrep)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Login.session.mode.equals("admin")){
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.faireEntre_noEmp),Toast.LENGTH_SHORT).show();

                    }else {
                        ArrayList<SpinnerItem> clts=new ArrayList<>();
                        Cursor r=Accueil.bd.read("select * from client");
                        while (r.moveToNext()){
                            clts.add(new SpinnerItem(r.getString(r.getColumnIndex("code_clt")),r.getString(r.getColumnIndex("nom_clt"))));
                        }
                        spinnerClt=new MySpinnerSearchable(AfficherSortie.this, clts, getResources().getString(R.string.faireSortie_clt), new MySpinnerSearchable.SpinnerConfig() {
                            @Override
                            public void onChooseItem(int pos, SpinnerItem item) {
                                Intent i=new Intent(AfficherSortie.this,FaireSortie.class);
                                i.putExtra("request","new_bon");
                                i.putExtra("code_clt",""+item.key);
                                i.putExtra("nom_clt",""+item.value);
                                startActivity(i);
                                spinnerClt.closeSpinner();

                            }
                        });

                        spinnerClt.openSpinner();
                    }

                }
            });
            //endregion

            r=Accueil.bd.read("select * from bon_sortie where (sync='"+sync+"' "+(Login.session.mode.equals("admin")== true ? ")" : (sync == 1 ? ")" : "or ( sync='1' and  julianday('now') - julianday(date_bon)<= 2)) and code_emp='"+Login.session.employe.code_emp+"'"))+" order by code_bon desc");

            afficherBons(r);


        }
    }

    private void afficherBons(Cursor r) {
        BonSortieAdapter.bons.clear();

        while (r.moveToNext()){
            BonSortieAdapter.bons.add(new BonSortie(r.getString(r.getColumnIndex("code_bon")),r.getString(r.getColumnIndex("date_bon")),r.getString(r.getColumnIndex("code_clt")),r.getString(r.getColumnIndex("nom_clt")), ((r.getString(r.getColumnIndex("sync")).equals("0") ? ETAT.VALIDÉ : (((r.getString(r.getColumnIndex("etat")).equals("exporté")))? ETAT.EXPORTÉ : ETAT.SUPPRIMÉ)))));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BonSortieAdapter.bons.clear();
    }
}
