package com.leadersoft.celtica.lsstock;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.EntréeSortie.AfficherEntree;
import com.leadersoft.celtica.lsstock.EntréeSortie.AfficherSortie;
import com.leadersoft.celtica.lsstock.Inventaire.AfficherInventaires;
import com.leadersoft.celtica.lsstock.Inventaire.FaireInventaireConfig;
import com.leadersoft.celtica.lsstock.Preparations.AfficherPreparations;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.TransfertStock.AfficherBonsTransfert;
import com.leadersoft.celtica.lsstock.TransfertStock.FaireStockConfig;
import com.leadersoft.celtica.lsstock.TransfertStock.ProduitTransferé;

import java.sql.SQLException;

public class Accueil extends AppCompatActivity {

    AlarmManager alarmMgr;
    PendingIntent alarmIntent;

    public static MyBD bd;
    public static SqlServerBD BDsql;

    DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            bd=new MyBD("ls_stock.db",Accueil.this);


            /*
            alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), AlertReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 111, intent, 0);

            if (!Login.session.mode.equals("admin")){
                alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis()+ 1000,
                        1000*60, alarmIntent);
            }else {
               alarmMgr.cancel(alarmIntent);

            }
            */


            //region check camera permission ..
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(Accueil.this, new String[]{Manifest.permission.CAMERA}, 8);

            }
            //endregion

            //region configuration drawer layout ..
            mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);

            ((ImageView)findViewById(R.id.drawer)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(Gravity.LEFT);

                }
            });


            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(
                    new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(MenuItem menuItem) {
                            // set item as selected to persist highlight
                            //menuItem.setChecked(true);

                            if(menuItem.getItemId()== R.id.menu_parametrage){
                                startActivity(new Intent(Accueil.this,Parametrage.class));

                            }
                            else if(menuItem.getItemId()== R.id.menu_sync) {
                                startActivity(new Intent(Accueil.this,Synchronisation.class));

                            }


                            // close drawer when item is tapped
                            mDrawerLayout.closeDrawers();

                            // Add code here to update the UI based on the item selected
                            // For example, swap UI fragments here

                            return true;
                        }
                    });

            TextView modeUser=(TextView) navigationView.getHeaderView(0).findViewById(R.id.acc_user);

            if (Login.session.mode.equals("admin")){
                modeUser.setText("Admin");
            }else {
                modeUser.setText(Login.session.employe.nom_emp+"");
            }


            //endregion

            //region afficher preparation
            ((LinearLayout)findViewById(R.id.affprep)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(final View view) {
                    ButtClick(view);
                    startActivity(new Intent(Accueil.this, AfficherPreparations.class));
                }
            });
            //endregion

            //region afficher transfert de stock
            ((LinearLayout)findViewById(R.id.transfert_stock)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View view) {
                    ButtClick(view);
                    startActivity(new Intent(Accueil.this,AfficherBonsTransfert.class));
                }
            });

            //endregion

            //region inventaire
            ((LinearLayout)findViewById(R.id.inventaire)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View view) {
                    ButtClick(view);
                    startActivity(new Intent(Accueil.this, AfficherInventaires.class));

                }
            });

            //endregion

            //region sortie stock ..
            ((LinearLayout)findViewById(R.id.sortieStock)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    ButtClick(v);
                    startActivity(new Intent(Accueil.this, AfficherSortie.class));
                }
            });

            //endregion

            //region entree stock ..
            ((LinearLayout)findViewById(R.id.entreestock)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    ButtClick(v);
                    startActivity(new Intent(Accueil.this, AfficherEntree.class));
                }
            });

            //endregion

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public  void ButtClick(final View view){

        view.setBackground(getResources().getDrawable(R.drawable.bg_butt_fonce));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setBackground(getResources().getDrawable(R.drawable.bg_butt));
            }
        },200);
        Login.session.playAudioFromAsset(Accueil.this,"klik.ogg");
    }
}
