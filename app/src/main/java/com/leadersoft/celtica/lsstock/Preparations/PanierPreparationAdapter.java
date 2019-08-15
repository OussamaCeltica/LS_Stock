package com.leadersoft.celtica.lsstock.Preparations;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.R;

import java.util.ArrayList;

/**
 * Created by celtica on 12/05/19.
 */

public class PanierPreparationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    AppCompatActivity c;
    public static ArrayList<ProduitDansBon> produits=new ArrayList<ProduitDansBon>();
    public static int itemSelected;

    public PanierPreparationAdapter(  AppCompatActivity c) {
        this.c = c;

    }

    public static class PrView extends RecyclerView.ViewHolder  {
        public TextView nom_pr;
        public TextView current_qt;
        public TextView final_qt;
        public TextView current_qtCarton;
        public TextView final_qtCarton;
        public LinearLayout body;
        public PrView(View v) {
            super(v);
            nom_pr=(TextView)v.findViewById(R.id.div_panier_prep_pr);
            current_qt=(TextView)v.findViewById(R.id.div_panier_prep_qtCurrent);
            final_qt=(TextView)v.findViewById(R.id.div_panier_prep_qtFinal);
            current_qtCarton=(TextView)v.findViewById(R.id.div_panier_prep_qtCurrentCarton);
            final_qtCarton=(TextView)v.findViewById(R.id.div_panier_prep_qtFinalCarton);
            body=(LinearLayout)v.findViewById(R.id.div_panier_prep_body);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_panier_prep,parent,false);

        PrView vh = new PrView(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((PrView)holder).nom_pr.setText(produits.get(position).nom+"");
        ((PrView)holder).current_qt.setText(Login.session.formatQt(produits.get(position).current_qt)+"");
        ((PrView)holder).final_qt.setText(Login.session.formatQt(produits.get(position).final_qt)+"");

        ((PrView)holder).current_qtCarton.setText(((produits.get(position).qt_carton == 0) ? "/" : Login.session.formatQt(produits.get(position).current_qt/produits.get(position).qt_carton))+"");
        ((PrView)holder).final_qtCarton.setText(((produits.get(position).qt_carton == 0) ? "/" : Login.session.formatQt(produits.get(position).final_qt/produits.get(position).qt_carton))+"");

        //region div de modification ..
        ((PrView)holder).body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mb = new AlertDialog.Builder(c); //c est l activity non le context ..

                View v = c.getLayoutInflater().inflate(R.layout.supp_pr_panier, null);
                TextView valider = (TextView) v.findViewById(R.id.panier_modif_valider);
                TextView suppButt = (TextView) v.findViewById(R.id.panier_supp_pr_oui);
                final CheckBox cartonCheck=(CheckBox)v.findViewById(R.id.cartonCheck);
                final EditText qt = (EditText) v.findViewById(R.id.panier_pr_modif_qt);

                mb.setView(v);
                final AlertDialog ad = mb.create();
                ad.show();
                ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..

                cartonCheck.setVisibility(View.VISIBLE);
                qt.setText(Login.session.formatQt(produits.get(position).current_qt)+ "");

                valider.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (cartonCheck.isChecked() && produits.get(position).qt_carton == 0){
                            Toast.makeText(c.getApplicationContext(), c.getResources().getString(R.string.carton_noQt), Toast.LENGTH_SHORT).show();

                        }else {
                            produits.get(position).current_qt =  ((cartonCheck.isChecked()==true)?Double.parseDouble(qt.getText().toString()) *  produits.get(position).getCartonQt() :  Double.parseDouble(qt.getText().toString()));
                            notifyDataSetChanged();
                            ad.dismiss();
                        }


                    }
                });

                suppButt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (produits.get(position).final_qt == 0){
                            produits.remove(position);
                        }else {
                            produits.get(position).current_qt=0;
                        }

                        notifyDataSetChanged();
                        ad.dismiss();
                    }
                });
            }
        });
        //endregion

        //region testé les Qt et changé Background Item ..
        if(produits.get(position).current_qt == produits.get(position).final_qt){
            ((PrView)holder).body.setBackgroundColor(c.getResources().getColor(R.color.Green));
        }else if(produits.get(position).current_qt>produits.get(position).final_qt) {
            if(produits.get(position).final_qt==0)
                ((PrView)holder).body.setBackgroundColor(c.getResources().getColor(R.color.Orange));
            else
                ((PrView)holder).body.setBackgroundColor(c.getResources().getColor(R.color.Bleu));

        }else {
            ((PrView)holder).body.setBackgroundColor(c.getResources().getColor(R.color.Red));
        }
        //endregion

    }

    public void addPrToPanier(ProduitDansBon pr){
        int i=0;
        boolean existe=false;
        while (i != produits.size() && !existe) {
            if(pr.codebar.equals(produits.get(i).codebar)){
                existe=true;
                produits.get(i).current_qt=produits.get(i).current_qt+pr.current_qt;
            }
            i++;
        }

        if (!existe){
            produits.add(pr);
        }

        notifyDataSetChanged();
    }

    public double getProduitQt(Produit p){
        int i=0;
        boolean existe=false;
        while (i != produits.size() && !existe){
            if(produits.get(i).codebar.equals(p.codebar)){
                existe=true;
            }else {
                i++;
            }

        }

        if (existe){
            return produits.get(i).current_qt;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return produits.size();
    }
}
