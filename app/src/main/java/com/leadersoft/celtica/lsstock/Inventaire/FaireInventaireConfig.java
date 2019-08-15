package com.leadersoft.celtica.lsstock.Inventaire;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.R;
import com.leadersoft.celtica.lsstock.Session;
import com.leadersoft.celtica.lsstock.TransfertStock.FaireStockConfig;

public class FaireInventaireConfig extends AppCompatActivity {
    String code_depot="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_inventaire_config);

        if (savedInstanceState != null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

            //region select entrepot ..
            ((TextView)findViewById(R.id.invent_config_depot)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Login.session.openScannerCodeBarre(FaireInventaireConfig.this, new Session.OnScanListener() {
                        @Override
                        public void OnScan(String code,LinearLayout body) {
                            checkDepotExiste(code);
                        }
                    });
                }
            });
            //endregion

            //region valider le depot et faire inventaire
            ((TextView)findViewById(R.id.invent_config_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(code_depot.equals("")){
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.faire_prep_depot),Toast.LENGTH_SHORT).show();
                    }else {
                        Intent i = new Intent(FaireInventaireConfig.this,FaireInventaire.class);
                        i.putExtra("code_depot", code_depot);
                        i.putExtra("request", "new_bon");
                        startActivity(i);
                    }
                }
            });

            //endregion
        }
    }

    public void checkDepotExiste(String codebar_dep){
        Cursor r= Accueil.bd.read("select * from depot where codebar='"+codebar_dep+"'");
        if (r.moveToNext()){
            code_depot=codebar_dep;
            ((TextView)findViewById(R.id.invent_config_depot)).setText(r.getString(r.getColumnIndex("nom_dep")));
        }else {
            code_depot="";
            ((TextView)findViewById(R.id.invent_config_depot)).setText("");
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.config_transfert_noCodebar),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            checkDepotExiste(data.getExtras().getString("code"));
        }
    }
}
