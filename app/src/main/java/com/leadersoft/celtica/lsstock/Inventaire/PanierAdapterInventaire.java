package com.leadersoft.celtica.lsstock.Inventaire;

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

import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.TransfertStock.BonTransfertEnCours;
import com.leadersoft.celtica.lsstock.TransfertStock.BonsTransfertAdapter;
import com.leadersoft.celtica.lsstock.TransfertStock.ProduitTransferé;

import java.util.ArrayList;

/**
 * Created by celtica on 28/04/19.
 */

public class PanierAdapterInventaire extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    AppCompatActivity c;
    public static ArrayList<ProduitInventaire> produits=new ArrayList<ProduitInventaire>();
    public static int itemSelected;

    public PanierAdapterInventaire(AppCompatActivity c) {
        this.c = c;

    }

    public static class ItemView extends RecyclerView.ViewHolder  {
        public TextView nom_pr;
        public TextView qt;
        public TextView packaging;
        public LinearLayout body;
        public ItemView(View v) {
            super(v);
            nom_pr=(TextView)v.findViewById(R.id.div_pr_panier_pr);
            qt=(TextView)v.findViewById(R.id.div_pr_panier_qt);
            packaging=(TextView)v.findViewById(R.id.div_pr_panier_packaging);
            body=(LinearLayout) v.findViewById(R.id.div_pr_panier_body);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_pr_panier,parent,false);

        ItemView vh = new ItemView(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((ItemView)holder).nom_pr.setText(produits.get(position).nom);
        ((ItemView)holder).qt.setText(Login.session.formatQt(produits.get(position).qt)+"");

        if(produits.get(position).isPackaging){
            ((ItemView)holder).packaging.setText(c.getResources().getString(R.string.faire_transf_qt_packaging));
        }else {
            ((ItemView)holder).packaging.setText(c.getResources().getString(R.string.faire_transf_qt_unité));
        }


        ((ItemView)holder).body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemSelected=position;

                if (BonInventaireAdapter.itemSelected == -1 ) {
                    AlertDialog.Builder mb = new AlertDialog.Builder(c); //c est l activity non le context ..

                    View v = c.getLayoutInflater().inflate(R.layout.supp_pr_panier, null);
                    TextView valider = (TextView) v.findViewById(R.id.panier_modif_valider);
                    TextView suppButt = (TextView) v.findViewById(R.id.panier_supp_pr_oui);
                    final EditText qt = (EditText) v.findViewById(R.id.panier_pr_modif_qt);

                    mb.setView(v);
                    final AlertDialog ad = mb.create();
                    ad.show();
                    ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..

                    qt.setText(Login.session.formatQt(produits.get(position).qt)+ "");

                    valider.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            produits.get(position).qt = Double.parseDouble(qt.getText().toString());
                            notifyDataSetChanged();
                            ad.dismiss();
                        }
                    });

                    suppButt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            produits.remove(position);
                            notifyDataSetChanged();
                            ad.dismiss();
                        }
                    });
                }




            }
        });




    }

    public double getProduitQt(Produit p){
        int i=0;
        boolean existe=false;
        while (i != produits.size() && !existe){
            if(produits.get(i).codebar.equals(p.codebar)  && produits.get(i).isPackaging== p.isPackaging ){
                existe=true;
            }else {
                i++;
            }

        }

        if (existe){
            return produits.get(i).qt;
        }
        return 0;
    }

    public void addPrToPanier(ProduitInventaire p){
        int i=0;
        boolean existe=false;
        while (i != produits.size() && !existe){
            if(produits.get(i).codebar.equals(p.codebar) && produits.get(i).isPackaging ==p.isPackaging){
                existe=true;
            }else {
                i++;
            }

        }

        if (existe){
            produits.get(i).qt+=p.qt;
        }else {
            produits.add(p);
        }
    }

    @Override
    public int getItemCount() {
        return produits.size();
    }
}
