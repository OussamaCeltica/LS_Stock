package com.leadersoft.celtica.lsstock.EntréeSortie;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.R;

import java.util.ArrayList;

public class PanierSortieAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    AppCompatActivity c;
    public static ArrayList<ProduitSortie> produits=new ArrayList<>();
    public static int itemSelected;

    public PanierSortieAdapter(  AppCompatActivity c) {
        this.c = c;

    }

    public static class PrView extends RecyclerView.ViewHolder  {
        public TextView nom_pr;
        public TextView nom_dep;
        public TextView type;
        public TextView qt;

        public LinearLayout body;
        public PrView(View v) {
            super(v);
            nom_pr=(TextView)v.findViewById(R.id.divpanier_es_pr);
            nom_dep=(TextView)v.findViewById(R.id.divpanier_es_dep);
            qt=(TextView)v.findViewById(R.id.divpanier_es_qt);
            type=(TextView)v.findViewById(R.id.divpanier_es_type);
            body=(LinearLayout) v.findViewById(R.id.dives_body);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.divpanier_entresortie,parent,false);

        PrView vh = new   PrView(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((PrView)holder).nom_dep.setText(produits.get(position).nom_dep+"");
        ((PrView)holder).nom_pr.setText(produits.get(position).nom+"");
        ((PrView)holder).qt.setText(produits.get(position).qt+"");

        if (produits.get(position).isPackaging) ((PrView)holder).type.setText("conditionnée");
        else ((PrView)holder).type.setText("unité");

        if(produits.get(position).etat== ETAT.EN_COURS){
            ((PrView)holder).body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View vv) {
                    AlertDialog.Builder mb = new AlertDialog.Builder(c); //c est l activity non le context ..

                    View v= c.getLayoutInflater().inflate(R.layout.supp_pr_panier,null);
                    TextView supp=(TextView) v.findViewById(R.id.panier_supp_pr_oui);
                    TextView valider=(TextView) v.findViewById(R.id.panier_modif_valider);
                    final EditText qt=(EditText)v.findViewById(R.id.panier_pr_modif_qt);
                    qt.setText(produits.get(position).qt+"");


                    mb.setView(v);
                    final AlertDialog ad=mb.create();
                    ad.show();
                    ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..

                    valider.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!qt.getText().toString().equals("")){
                                produits.get(position).qt=Double.parseDouble(qt.getText().toString());
                                notifyItemChanged(position);
                                ad.dismiss();

                            }
                        }
                    });

                    supp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            produits.remove(position);
                            notifyItemRemoved(position);
                            ad.dismiss();
                        }
                    });


                }
            });
        }

    }

    public void addToPanier(ProduitSortie p){
        boolean added=false;
        int i=0;
        while (i<produits.size() && !added){
            ProduitSortie pr=produits.get(i);
            if (pr.code_dep.equals(p.code_dep) && pr.codebar.equals(p.codebar) && pr.isPackaging==p.isPackaging){
                pr.qt=pr.qt+p.qt;
                added=true;
            }
            i++;
        }

        if (!added) produits.add(p);

        notifyDataSetChanged();
    }



    @Override
    public int getItemCount() {
        return produits.size();
    }
}
