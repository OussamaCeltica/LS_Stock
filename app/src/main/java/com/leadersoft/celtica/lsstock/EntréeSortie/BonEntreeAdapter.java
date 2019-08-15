package com.leadersoft.celtica.lsstock.EntréeSortie;

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

public class BonEntreeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    AppCompatActivity c;
    public static ArrayList<BonEntree> bons=new ArrayList<>();
    public static int itemSelected;

    public BonEntreeAdapter(  AppCompatActivity c) {
        this.c = c;

    }

    public static class BonView extends RecyclerView.ViewHolder  {
        public TextView code_bon;
        public TextView nom_fournis;
        public TextView date_bon;
        public TextView msg;
        public LinearLayout prep_butt;
        public LinearLayout body;
        public BonView(View v) {
            super(v);
            code_bon=(TextView)v.findViewById(R.id.div_entre_codeBon);
            nom_fournis=(TextView)v.findViewById(R.id.div_entre_fournis);
            date_bon=(TextView)v.findViewById(R.id.div_entre_dateBon);
            msg=(TextView)v.findViewById(R.id.div_entre_msg);
            prep_butt=(LinearLayout) v.findViewById(R.id.div_prep_buttPrep);
            body=(LinearLayout) v.findViewById(R.id.div_bon_body);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_bon_entre,parent,false);

         BonView vh = new BonView(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        (( BonView)holder).code_bon.setText(bons.get(position).id_bon);
        (( BonView)holder).nom_fournis.setText(bons.get(position).nom_fournis);
        (( BonView)holder).date_bon.setText(bons.get(position).date_bon);
        (( BonView)holder).prep_butt.setVisibility(View.GONE);


        if(bons.get(position).etat != ETAT.EN_COURS ){

            ((BonView)holder).msg.setVisibility(View.VISIBLE);
            if (bons.get(position).etat == ETAT.EXPORTÉ){
                ((BonView)holder).msg.setText(c.getResources().getString(R.string.etat_exporté));
                ((BonView)holder).msg.setBackgroundColor(c.getResources().getColor(R.color.Green));
            }else if (bons.get(position).etat == ETAT.VALIDÉ){
                ((BonView)holder).msg.setText(c.getResources().getString(R.string.etat_validé));
                ((BonView)holder).msg.setBackgroundColor(c.getResources().getColor(R.color.Green));
            }else {
                ((BonView)holder).msg.setText(c.getResources().getString(R.string.etat_supprimé));
                ((BonView)holder).msg.setBackgroundColor(c.getResources().getColor(R.color.Red));
            }
            ((BonView)holder).prep_butt.setVisibility(View.GONE);
            ((BonView)holder).body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    PanierEntreAdapter.produits=bons.get(position).getProduitEntree();
                    Intent i=new Intent(c,FaireEntre.class);
                    i.putExtra("request","validé");
                    c.startActivity(i);
                }
            });
        }else {

            (( BonView)holder).body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    //ProduitPreparerAdapter.produits=bons.get(position).getProduitAPreparer();


                }
            });

            (( BonView)holder).prep_butt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    //c.startActivity(new Intent(c,FairePreparationConfig.class));
                }
            });
        }




    }

    @Override
    public int getItemCount() {
        return bons.size();
    }
}
