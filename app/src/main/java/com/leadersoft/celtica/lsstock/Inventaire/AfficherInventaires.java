package com.leadersoft.celtica.lsstock.Inventaire;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.ArchiveMenuFrag;
import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.TransfertStock.AfficherBonsTransfert;
import com.leadersoft.celtica.lsstock.TransfertStock.BonTransfert;
import com.leadersoft.celtica.lsstock.TransfertStock.BonTransfertEnCours;
import com.leadersoft.celtica.lsstock.TransfertStock.BonsTransfertAdapter;

public class AfficherInventaires extends AppCompatActivity {
    BonInventaireAdapter mAdapter;
    Cursor r;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_inventaires);
        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_bonInventaire);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(AfficherInventaires.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new BonInventaireAdapter(AfficherInventaires.this);

            mRecyclerView.setAdapter(mAdapter);

            if (getIntent().getExtras() == null){
                r=Accueil.bd.read("select * from bon_inventaire where sync='0' order by id_bon desc");
            }else {

                //region menu des archives
                ((ImageView) findViewById(R.id.add_bonInventaire)).setVisibility(View.GONE);
                final ArchiveMenuFrag menu_butt=(ArchiveMenuFrag)getSupportFragmentManager().findFragmentById(R.id.archive);
                menu_butt.root.setVisibility(View.VISIBLE);
                //endregion

                r=Accueil.bd.read("select * from bon_inventaire where sync='1' order by id_bon desc");
            }

            while (r.moveToNext()){
                BonInventaireAdapter.bons.add(new BonInventaire(r.getString(r.getColumnIndex("id_bon")),r.getString(r.getColumnIndex("code_depot")),r.getString(r.getColumnIndex("nom_depot")),r.getString(r.getColumnIndex("date_inventaire")),r.getString(r.getColumnIndex("code_emp")),((r.getString(r.getColumnIndex("sync")).equals("0"))== true)? ETAT.VALIDÉ : ((r.getString(r.getColumnIndex("etat")).equals("exporté"))==true) ? ETAT.EXPORTÉ : ETAT.SUPPRIMÉ ));
            }

            mAdapter.notifyDataSetChanged();

            //region add inventaire ..
            ((ImageView)findViewById(R.id.add_bonInventaire)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if (Login.session.mode.equals("admin")){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.faire_transf_noEmp),Toast.LENGTH_SHORT).show();
                }else {
                    startActivity(new Intent(AfficherInventaires.this,FaireInventaireConfig.class));
                }

                }
            });
            //endregion
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BonInventaireAdapter.bons.clear();
    }
}
