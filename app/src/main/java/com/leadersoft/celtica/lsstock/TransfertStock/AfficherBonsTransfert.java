package com.leadersoft.celtica.lsstock.TransfertStock;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.ArchiveMenuFrag;
import com.leadersoft.celtica.lsstock.Inventaire.AfficherInventaires;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.R;

public class AfficherBonsTransfert extends AppCompatActivity {
    BonsTransfertAdapter mAdapter;
    Cursor r;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_bons_transfert);


        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

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
                r=Accueil.bd.read("select * from bon_transfert where codebar_depot_dest is not null and sync='0' "+(Login.session.mode.equals("admin")== true ? "" : " and code_emp='"+Login.session.employe.code_emp+"'")+" order by id desc");
            }else {

                //region menu des archives
                ((ImageView) findViewById(R.id.add_bonTransfert)).setVisibility(View.GONE);
                final ArchiveMenuFrag menu_butt=(ArchiveMenuFrag)getSupportFragmentManager().findFragmentById(R.id.archive);
                menu_butt.root.setVisibility(View.VISIBLE);
                //endregion

                r=Accueil.bd.read("select * from bon_transfert where sync='1' order by id desc");
            }

            afficherBon(r);
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

    public void afficherBon(Cursor r){
        BonsTransfertAdapter.bons.clear();
        mAdapter.notifyDataSetChanged();

        //region request normal
        if (getIntent().getExtras() == null){
            if(!Login.session.mode.equals("admin")) {
                Cursor r2 = Accueil.bd.read("select * from bon_transfert where codebar_depot_dest is null and code_emp='"+Login.session.employe.code_emp+"' order by id desc");
                while (r2.moveToNext()) {
                    BonsTransfertAdapter.bons.add(new BonTransfertEnCours(r2.getString(r2.getColumnIndex("id")), r2.getString(r2.getColumnIndex("codebar_depot_src")), r2.getString(r2.getColumnIndex("nom_depot_src"))));
                }
            }
            while (r.moveToNext()){
                BonsTransfertAdapter.bons.add(new BonTransfert(r.getString(r.getColumnIndex("id")),r.getString(r.getColumnIndex("codebar_depot_src")),r.getString(r.getColumnIndex("nom_depot_src")),r.getString(r.getColumnIndex("codebar_depot_dest")),r.getString(r.getColumnIndex("nom_depot_dest")),r.getString(r.getColumnIndex("date_transfert"))));
            }
        }
        //endregion

        //region request Archive ..
        else {
            while (r.moveToNext()){
                Log.e("bbb","kayen");
                if (r.getString(r.getColumnIndex("etat")).equals("exporté")){

                    BonsTransfertAdapter.bons.add(new BonTransfertExporte(r.getString(r.getColumnIndex("id")),r.getString(r.getColumnIndex("codebar_depot_src")),r.getString(r.getColumnIndex("nom_depot_src")),r.getString(r.getColumnIndex("codebar_depot_dest")),r.getString(r.getColumnIndex("nom_depot_dest")),r.getString(r.getColumnIndex("date_transfert"))));
                }
            }
        }
        //endregion

        mAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BonsTransfertAdapter.bons.clear();
    }
}
