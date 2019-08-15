package com.leadersoft.celtica.lsstock.Preparations;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadersoft.celtica.lsstock.Produit;
import com.leadersoft.celtica.lsstock.R;

import java.util.ArrayList;

/**
 * Created by celtica on 20/04/19.
 */

public class ProduitPreparerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    AppCompatActivity c;
    public static ArrayList<ProduitDansBon> produits=new ArrayList<ProduitDansBon>();
    public static int itemSelected;

    public ProduitPreparerAdapter(  AppCompatActivity c) {
        this.c = c;

    }

    public static class PrView extends RecyclerView.ViewHolder  {
        public TextView nom_pr;
        public TextView nom_dep;
        public TextView current_qt;
        public TextView final_qt;
        public TextView current_qtCarton;
        public TextView final_qtCarton;
        public LinearLayout body;
        public PrView(View v) {
            super(v);
            nom_pr=(TextView)v.findViewById(R.id.div_prep_produit);
            nom_dep=(TextView)v.findViewById(R.id.div_prep_depot);
            current_qt=(TextView)v.findViewById(R.id.div_prep_currentQt);
            final_qt=(TextView)v.findViewById(R.id.div_prep_finalQt);
            current_qtCarton=(TextView)v.findViewById(R.id.div_prep_currentQtCarton);
            final_qtCarton=(TextView)v.findViewById(R.id.div_prep_finalQtCarton);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_pr_in_bon,parent,false);

        PrView vh = new PrView(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((PrView)holder).nom_pr.setText(produits.get(position).nom);
        ((PrView)holder).nom_dep.setText(produits.get(position).nom_dep);
        ((PrView)holder).current_qt.setText(produits.get(position).current_qt+"");
        ((PrView)holder).current_qt.setBackground(c.getResources().getDrawable(R.drawable.bg_prep_qt));
        ((PrView)holder).final_qt.setText(produits.get(position).final_qt+"");

        ((PrView)holder).current_qtCarton.setText(produits.get(position).current_qt/produits.get(position).getCartonQt()+"");
        ((PrView)holder).final_qtCarton.setText(produits.get(position).final_qt/produits.get(position).getCartonQt()+"");

        Drawable mDrawable = c.getResources().getDrawable(R.drawable.bg_prep_qt);

        if(produits.get(position).current_qt == produits.get(position).final_qt){
            mDrawable.setColorFilter(c.getResources().getColor(R.color.Green), PorterDuff.Mode.SRC_OVER);
            ((PrView)holder).current_qt.setBackground(mDrawable);
        }else if(produits.get(position).current_qt>produits.get(position).final_qt) {
            if(produits.get(position).final_qt==0) {
                mDrawable.setColorFilter(c.getResources().getColor(R.color.Orange), PorterDuff.Mode.SRC_OVER);
                ((PrView)holder).current_qt.setBackground(mDrawable);
            }
            else {
                mDrawable.setColorFilter(c.getResources().getColor(R.color.Bleu), PorterDuff.Mode.SRC_OVER);
                ((PrView)holder).current_qt.setBackground(mDrawable);
            }

        }else {
            mDrawable.setColorFilter(c.getResources().getColor(R.color.Red), PorterDuff.Mode.SRC_OVER);
            ((PrView)holder).current_qt.setBackground(mDrawable);
        }

    }



    @Override
    public int getItemCount() {
        return produits.size();
    }
}
