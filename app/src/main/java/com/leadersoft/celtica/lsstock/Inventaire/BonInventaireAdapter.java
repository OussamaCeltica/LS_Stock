package com.leadersoft.celtica.lsstock.Inventaire;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.TransfertStock.BonTransfert;
import com.leadersoft.celtica.lsstock.TransfertStock.BonTransfertEnCours;
import com.leadersoft.celtica.lsstock.TransfertStock.BonsTransfertAdapter;
import com.leadersoft.celtica.lsstock.TransfertStock.FaireStockConfig;
import com.leadersoft.celtica.lsstock.TransfertStock.FaireTransfert;
import com.leadersoft.celtica.lsstock.TransfertStock.PanierAdapter;

import java.util.ArrayList;

/**
 * Created by celtica on 02/05/19.
 */

public class BonInventaireAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    AppCompatActivity c;
    public static ArrayList<BonInventaire> bons=new ArrayList<BonInventaire>();
    public static int itemSelected=-1;

    public BonInventaireAdapter(  AppCompatActivity c) {
        this.c = c;

    }

    public static class BonView extends RecyclerView.ViewHolder  {
        public TextView msg;
        public TextView nom_bon;
        public TextView depot;
        public TextView date;
        public LinearLayout body;
        public BonView(View v) {
            super(v);
            nom_bon=(TextView)v.findViewById(R.id.div_bonInvent_bon);
            msg=(TextView)v.findViewById(R.id.div_bonInvent_msg);
            depot=(TextView)v.findViewById(R.id.div_bonInvent_depot);
            date=(TextView)v.findViewById(R.id.div_bonInvent_date);
            body=(LinearLayout) v.findViewById(R.id.div_bonInvent_body);

        }
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_inventaire_exporte,parent,false);
        BonView vh = new  BonView(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {



            if (bons.get(position).etat == ETAT.EXPORTÉ) {
                ((BonView) holder).msg.setVisibility(View.VISIBLE);

            } else if (bons.get(position).etat == ETAT.SUPPRIMÉ){
                ((BonView) holder).msg.setVisibility(View.VISIBLE);
                ((BonView) holder).msg.setBackgroundColor(c.getResources().getColor(R.color.Red));
                ((BonView) holder).msg.setText(c.getResources().getString(R.string.etat_supprimé));
            }else {
                ((BonView) holder).msg.setVisibility(View.GONE);
            }
            ((BonView) holder).nom_bon.setText("" + bons.get(position).id_bon);
            ((BonView) holder).depot.setText("" + bons.get(position).nom_depot);
            ((BonView) holder).date.setText("" + bons.get(position).date_bon);

            ((BonView) holder).body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected = position;
                    PanierAdapterInventaire.produits = bons.get(position).getProduitInBon();
                    Intent i = new Intent(c, FaireInventaire.class);
                    i.putExtra("request", "bon_validé");
                    c.startActivity(i);
                }
            });

    }



    @Override
    public int getItemCount() {
        return bons.size();
    }
}
