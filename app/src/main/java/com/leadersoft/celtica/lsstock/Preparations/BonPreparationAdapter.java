package com.leadersoft.celtica.lsstock.Preparations;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.R;

import java.util.ArrayList;

/**
 * Created by celtica on 19/04/19.
 */

public class BonPreparationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    AppCompatActivity c;
    public static ArrayList<BonPreparation> bons=new ArrayList<BonPreparation>();
    public static int itemSelected;

    public BonPreparationAdapter(  AppCompatActivity c) {
        this.c = c;

    }

    public static class BonView extends RecyclerView.ViewHolder  {
        public TextView code_bon;
        public TextView date_bon;
        public TextView nom_clt;
        public TextView msg;
        public LinearLayout prep_butt;
        public LinearLayout body;
        public BonView(View v) {
            super(v);
            code_bon=(TextView)v.findViewById(R.id.div_prep_codeBon);
            date_bon=(TextView)v.findViewById(R.id.div_prep_dateBon);
            nom_clt=(TextView)v.findViewById(R.id.div_prep_clt);
            msg=(TextView)v.findViewById(R.id.div_prep_msg);
            prep_butt=(LinearLayout) v.findViewById(R.id.div_prep_buttPrep);
            body=(LinearLayout) v.findViewById(R.id.div_bon_body);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_bon_prep,parent,false);

        BonView vh = new BonView(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        ((BonView)holder).code_bon.setText(bons.get(position).code_bon);
        ((BonView)holder).date_bon.setText(bons.get(position).date_bon);
        ((BonView)holder).nom_clt.setText(bons.get(position).nom_clt);


        if(bons.get(position).etat == ETAT.VALIDÉ){
            ((BonView)holder).msg.setVisibility(View.VISIBLE);
            ((BonView)holder).prep_butt.setVisibility(View.GONE);
            ((BonView)holder).body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    //ProduitPreparerAdapter.produits=bons.get(position).getProduitAPreparer();
                    Intent i=new Intent(new Intent(c,AfficherProduitPreparer.class));
                    i.putExtra("request","validé");
                    c.startActivity(i);

                }
            });
        }else if (bons.get(position).etat == ETAT.EN_COURS){

            ((BonView)holder).body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    //ProduitPreparerAdapter.produits=bons.get(position).getProduitAPreparer();
                    Intent i=new Intent(new Intent(c,AfficherProduitPreparer.class));
                    i.putExtra("request","new_bon");
                    c.startActivity(i);

                }
            });
            ((BonView)holder).msg.setVisibility(View.GONE);
            ((BonView)holder).prep_butt.setVisibility(View.VISIBLE);
            ((BonView)holder).prep_butt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    c.startActivity(new Intent(c,FairePreparationConfig.class));
                }
            });
        }else {
            ((BonView)holder).msg.setVisibility(View.VISIBLE);
            ((BonView)holder).msg.setText(c.getResources().getString(R.string.etat_exporté));
            ((BonView)holder).prep_butt.setVisibility(View.GONE);
            ((BonView)holder).body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    //ProduitPreparerAdapter.produits=bons.get(position).getProduitAPreparer();
                    Intent i=new Intent(new Intent(c,AfficherProduitPreparer.class));
                    i.putExtra("request","archive");
                    c.startActivity(i);

                }
            });
        }




    }

    @Override
    public int getItemCount() {
        return bons.size();
    }
}
