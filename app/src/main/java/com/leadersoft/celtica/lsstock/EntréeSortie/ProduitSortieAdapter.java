package com.leadersoft.celtica.lsstock.EntréeSortie;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadersoft.celtica.lsstock.Preparations.ProduitDansBon;
import com.leadersoft.celtica.lsstock.Preparations.ProduitPreparerAdapter;
import com.leadersoft.celtica.lsstock.R;

import java.util.ArrayList;

public class ProduitSortieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    AppCompatActivity c;
    public static ArrayList<ProduitSortie> produits=new ArrayList<>();
    public static int itemSelected;

    public ProduitSortieAdapter(  AppCompatActivity c) {
        this.c = c;

    }

    public static class PrView extends RecyclerView.ViewHolder  {
        public TextView nom_pr;
        public TextView nom_dep;
        public TextView current_qt;
        public TextView final_qt;
        public LinearLayout body;
        public PrView(View v) {
            super(v);
            nom_pr=(TextView)v.findViewById(R.id.div_prep_produit);
            nom_dep=(TextView)v.findViewById(R.id.div_prep_depot);
            current_qt=(TextView)v.findViewById(R.id.div_prep_currentQt);
            final_qt=(TextView)v.findViewById(R.id.div_prep_finalQt);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_pr_in_bon,parent,false);

        ProduitPreparerAdapter.PrView vh = new ProduitPreparerAdapter.PrView(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

    }



    @Override
    public int getItemCount() {
        return produits.size();
    }
}
