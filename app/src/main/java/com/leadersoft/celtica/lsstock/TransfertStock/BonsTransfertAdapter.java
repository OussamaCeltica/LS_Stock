package com.leadersoft.celtica.lsstock.TransfertStock;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.leadersoft.celtica.lsstock.ETAT;
import com.leadersoft.celtica.lsstock.R;

import java.util.ArrayList;

/**
 * Created by celtica on 29/04/19.
 */

public class BonsTransfertAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    AppCompatActivity c;
    public static ArrayList<BonTransfert> bons=new ArrayList<BonTransfert>();
    public static int itemSelected=-1;

    public BonsTransfertAdapter(  AppCompatActivity c) {
        this.c = c;

    }

    public static class BonView extends RecyclerView.ViewHolder  {
        public TextView msg;
        public TextView nom_bon;
        public TextView depotSrc;
        public TextView depotDest;
        public LinearLayout transButt;
        public LinearLayout divDepotDest;
        public LinearLayout body;
        public BonView(View v) {
            super(v);
            nom_bon=(TextView)v.findViewById(R.id.div_bonTransfert_bon);
            msg=(TextView)v.findViewById(R.id.div_bonTransfert_msg);
            transButt=(LinearLayout) v.findViewById(R.id.div_bonTransfert_butt);
            depotSrc=(TextView)v.findViewById(R.id.div_bonTransfert_depotSrc);
            depotDest=(TextView)v.findViewById(R.id.div_bonTransfert_depotDest);
            divDepotDest=(LinearLayout) v.findViewById(R.id.divBonTrans_divDepDest);
            body=(LinearLayout) v.findViewById(R.id.div_bonTransfert_body);

        }
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_bon_transfert_exporte,parent,false);

            BonView vh = new BonView(v);
            return vh;


    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        ((BonView)holder).nom_bon.setText(""+bons.get(position).id_bon);
        ((BonView)holder).depotSrc.setText(""+bons.get(position).nom_dep_src);

        if(bons.get(position).etat == ETAT.EN_COURS){
            ((BonView)holder).msg.setText(c.getResources().getString(R.string.etat_cours));
            ((BonView)holder).msg.setBackgroundColor(c.getResources().getColor(R.color.Red));
            ((BonView)holder).divDepotDest.setVisibility(View.GONE);
            ((BonView)holder).transButt.setVisibility(View.VISIBLE);

            //region afficher produits ..
            ((BonView)holder).body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    PanierAdapter.produits=bons.get(position).getProduitInBon();
                    Intent i=new Intent(c,FaireTransfert.class);
                    i.putExtra("request","bon_non_validé");
                    c.startActivity(i);
                }
            });
            //endregion

            //region supprimer un bon en cours ..
            ((BonView)holder).body.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    PopupMenu popup = new PopupMenu(c,((BonView)holder).nom_bon);

                    //popup.getMenu().add("Archive Préparation");
                    popup.getMenu().add("Supprimer");


                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().toString().equals("Supprimer")){
                                bons.get(position).suppBon();
                                bons.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, getItemCount() - position);
                            }

                            return true;
                        }
                    });

                    popup.show();//showing popup menu
                    return true;
                }
            });
            //endregion

            //region valider le bon ..
            ((BonView)holder).transButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    Intent i=new Intent(c,FaireStockConfig.class);
                    i.putExtra("request","depot_dest");
                    c.startActivity(i);
                }
            });
            //endregion

        }else {
            ((BonView)holder).msg.setBackgroundColor(c.getResources().getColor(R.color.Green));
            ((BonView)holder).divDepotDest.setVisibility(View.VISIBLE);
            ((BonView)holder).depotDest.setText(""+bons.get(position).nom_dep_dest);
            ((BonView)holder).body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    PanierAdapter.produits=bons.get(position).getProduitInBon();
                    Intent i=new Intent(c,FaireTransfert.class);
                    i.putExtra("request","bon_validé");
                    c.startActivity(i);
                }
            });

            if (bons.get(position).etat == ETAT.EXPORTÉ){
                ((BonView)holder).msg.setText(c.getResources().getString(R.string.etat_exporté));
            }else {
                ((BonView)holder).msg.setText(c.getResources().getString(R.string.etat_validé));
            }
        }





    }



    @Override
    public int getItemCount() {
        return bons.size();
    }
}
