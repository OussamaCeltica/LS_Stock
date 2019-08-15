package com.leadersoft.celtica.lsstock;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.leadersoft.celtica.lsstock.EntréeSortie.AfficherEntree;
import com.leadersoft.celtica.lsstock.EntréeSortie.AfficherSortie;
import com.leadersoft.celtica.lsstock.Inventaire.AfficherInventaires;
import com.leadersoft.celtica.lsstock.Preparations.AfficherPreparations;
import com.leadersoft.celtica.lsstock.TransfertStock.AfficherBonsTransfert;



public class ArchiveMenuFrag extends Fragment {

    AppCompatActivity c;
    public View root;


    public ArchiveMenuFrag() {
        // Required empty public constructor
    }


    public static ArchiveMenuFrag newInstance() {
        ArchiveMenuFrag fragment = new ArchiveMenuFrag();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        c=(AppCompatActivity)getActivity();
        View v=inflater.inflate(R.layout.fragment_archive_menu, container, false);
        root=v.findViewById(R.id.frag_root);
        //region menu des archives
        final ImageView menu_butt=((ImageView)v.findViewById(R.id.archive));
        menu_butt.setVisibility(View.VISIBLE);
        menu_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(c,menu_butt);

                popup.getMenu().add(getResources().getString(R.string.archive_inventaire));
                popup.getMenu().add(getResources().getString(R.string.archive_transfert));
                popup.getMenu().add(getResources().getString(R.string.archive_préparation));
                popup.getMenu().add(getResources().getString(R.string.archive_entree));
                popup.getMenu().add(getResources().getString(R.string.archive_sortie));



                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getTitle().toString().equals(getResources().getString(R.string.archive_inventaire))){

                            Intent i=new Intent(c, AfficherInventaires.class);
                            i.putExtra("request","archive");
                            startActivity(i);
                            c.finish();

                        }else if (item.getTitle().toString().equals(getResources().getString(R.string.archive_transfert))){
                            Intent i=new Intent(c, AfficherBonsTransfert.class);
                            i.putExtra("request","archive");
                            startActivity(i);
                            c.finish();
                        } else if(item.getTitle().toString().equals(getResources().getString(R.string.archive_entree))){
                            Intent i=new Intent(c, AfficherEntree.class);
                            i.putExtra("request","archive");
                            startActivity(i);
                            c.finish();

                        }else if (item.getTitle().toString().equals(getResources().getString(R.string.archive_sortie))){
                            Intent i=new Intent(c, AfficherSortie.class);
                            i.putExtra("request","archive");
                            startActivity(i);
                            c.finish();
                        }else if (item.getTitle().toString().equals(getResources().getString(R.string.archive_préparation))){
                            Intent i=new Intent(c, AfficherPreparations.class);
                            i.putExtra("request","archive");
                            startActivity(i);
                            c.finish();
                        }
                        return true;
                    }
                });

                popup.show();//showing popup menu
            }

        });
        //endregion
        return v;
    }

}
