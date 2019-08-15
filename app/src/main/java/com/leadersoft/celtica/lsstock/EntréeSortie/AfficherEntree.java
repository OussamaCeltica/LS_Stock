package com.leadersoft.celtica.lsstock.EntréeSortie;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.ArchiveMenuFrag;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsstock.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsstock.Preparations.AfficherPreparations;
import com.leadersoft.celtica.lsstock.Preparations.BonPreparationAdapter;
import com.leadersoft.celtica.lsstock.R;

import java.util.ArrayList;

public class AfficherEntree extends AppCompatActivity {
    BonEntreeAdapter mAdapter;
    int test1=0;
    int min;
    AfficherPreparations.TypeBon type_bon= AfficherPreparations.TypeBon.EN_COURS;
    EditText searchInp;
    MySpinnerSearchable spinnerFournis;
    int sync =0;
    Cursor r;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_entree);
        if (savedInstanceState != null) {
            //region Revenir a au Login ..
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion

        }else {

            searchInp=(EditText)findViewById(R.id.affentre_search);

            //region configuration recyclerview ..
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_entre);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(AfficherEntree.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new BonEntreeAdapter(AfficherEntree.this);

            mRecyclerView.setAdapter(mAdapter);

            if (getIntent().getExtras() == null){
                sync=0;

            }else {
                sync=1;

                //region menu des archives
                ((ImageView) findViewById(R.id.affentre_addPrep)).setVisibility(View.GONE);
                final ArchiveMenuFrag menu_butt=(ArchiveMenuFrag)getSupportFragmentManager().findFragmentById(R.id.archive);
                menu_butt.root.setVisibility(View.VISIBLE);
                //endregion

            }

            r=Accueil.bd.read("select * from bon_entree where sync='"+sync+"' "+(Login.session.mode.equals("admin")== true ? "" : (sync == 1 ? "" : "and code_emp='"+Login.session.employe.code_emp+"'"))+" order by code_bon desc");

            afficherBons(r);
            //endregion

            //region la recherche
            searchInp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (s.toString().equals("")){
                        r=Accueil.bd.read("select * from bon_entree where sync='"+sync+"' "+(Login.session.mode.equals("admin")== true ? "" : (sync == 1 ? "" : "and code_emp='"+Login.session.employe.code_emp+"'"))+" order by code_bon desc");
                    }else {
                        r=Accueil.bd.read2("select * from bon_entree where sync='"+sync+"' "+(Login.session.mode.equals("admin")== true ? "" : (sync == 1 ? "" : "and code_emp='"+Login.session.employe.code_emp+"'"))+" and (code_bon LIKE ? or nom_fournis LIKE ?)  order by code_bon desc",new String[]{"%"+s.toString()+"%","%"+s.toString()+"%"});
                        Log.e("sss","select * from bon_entree where sync='"+sync+"' "+(Login.session.mode.equals("admin")== true ? "" : (sync == 1 ? "" : "and code_emp='"+Login.session.employe.code_emp+"'"))+" and (code_bon LIKE ? or nom_fournis LIKE ?)  order by code_bon desc");
                    }

                    afficherBons(r);

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            //endregion

            //region select un fournisseur et faire entree ..
            ((ImageView)findViewById(R.id.affentre_addPrep)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Login.session.mode.equals("admin")){
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.faireEntre_noEmp),Toast.LENGTH_SHORT).show();

                    }else {
                        ArrayList<SpinnerItem> fournis=new ArrayList<>();
                        Cursor r=Accueil.bd.read("select * from fournisseur");
                        while (r.moveToNext()){
                            fournis.add(new SpinnerItem(r.getString(r.getColumnIndex("code_fournis")),r.getString(r.getColumnIndex("nom_fournis"))));
                        }
                        spinnerFournis=new MySpinnerSearchable(AfficherEntree.this, fournis, getResources().getString(R.string.faireEntre_fournisse), new MySpinnerSearchable.SpinnerConfig() {
                            @Override
                            public void onChooseItem(int pos, SpinnerItem item) {
                                Intent i=new Intent(AfficherEntree.this,FaireEntre.class);
                                i.putExtra("request","new_bon");
                                i.putExtra("code_fournis",""+item.key);
                                i.putExtra("nom_fournis",""+item.value);
                                startActivity(i);
                                spinnerFournis.closeSpinner();

                            }
                        });

                        spinnerFournis.openSpinner();
                    }

                }
            });
            //endregion
        }
    }

    public void afficherBons(Cursor r){
        BonEntreeAdapter.bons.clear();
        while (r.moveToNext()){
            BonEntreeAdapter.bons.add(new BonEntree(r.getString(r.getColumnIndex("code_bon")),r.getString(r.getColumnIndex("date_bon")),r.getString(r.getColumnIndex("code_fournis")),r.getString(r.getColumnIndex("nom_fournis")), ((r.getString(r.getColumnIndex("sync")).equals("0") ? ETAT.VALIDÉ : (((r.getString(r.getColumnIndex("etat")).equals("exporté")))? ETAT.EXPORTÉ : ETAT.SUPPRIMÉ)))));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BonEntreeAdapter.bons.clear();
    }
}
