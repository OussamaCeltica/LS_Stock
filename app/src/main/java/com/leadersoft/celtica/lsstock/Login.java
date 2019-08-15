package com.leadersoft.celtica.lsstock;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsstock.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsstock.Preparations.ProduitDansBon;
import com.leadersoft.celtica.lsstock.TransfertStock.BonsTransfertAdapter;
import com.leadersoft.celtica.lsstock.TransfertStock.FaireStockConfig;
import com.leadersoft.celtica.lsstock.TransfertStock.FaireTransfert;
import com.leadersoft.celtica.lsstock.TransfertStock.ProduitTransferé;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class Login extends AppCompatActivity {

    TextView selectEmp,selectMode;
    MySpinnerSearchable spinner_employes;
    ArrayList<SpinnerItem> employes=new ArrayList<SpinnerItem>();
    private String code_emp="";
    public  static Session session;
    SpinnerDialog spinnerDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeLang(this,"fr");
        setContentView(R.layout.activity_login);

        Accueil.bd=new MyBD("ls_stock.db",Login.this);

        //region lancer le Updater ..
        try {
            UpdaterBD.update(Login.this);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //endregion

        selectEmp=(TextView)findViewById(R.id.login_emp);
        selectMode=(TextView)findViewById(R.id.login_mode);



        //Accueil.bd.write("update bon_transfert set sync='0',codebar_depot_dest=null   ");
        //Accueil.bd.write("insert into fournisseur (code_fournis,nom_fournis) values('f0001','Amine Jade')");
        //Accueil.bd.write("insert into employe (code_emp,nom_emp,codebar,Oid) values('EMP00001','Mohamed Rezoug','6130093010045','prrrrr')");
        //Accueil.bd.write("insert into depot (codebar,nom_dep) values('6130552001225','DEPOT0001')");
        //Accueil.bd.write("insert into depot (codebar,nom_dep) values('00001','DEPOT0003')");
        //Accueil.bd.write("insert into produit (codebar,nom_pr) values('6130552001225','Mauchoir Cotex')");
        //Accueil.bd.write("insert into produit (codebar,nom_pr) values('35521546563','Intermec Printer')");
        //Accueil.bd.write("insert into bon_sortie (Oid,code_bon,date_bon,sync) values('qsvsvsqscqs','BS/21235','2019-05-05','0')");
        //Accueil.bd.write("insert into bon_preparation (Oid,code_bon,date_bon,sync) values('qsvsvsqscqs','BP/30001','2019-05-05','0')");
        //Accueil.bd.write("insert into produit_preparer (code_bon,codebar_pr,nom_pr,codebar_depot,nom_depot,codebar_lot,final_qt,current_qt) values('BP/21235','6130552001225','Mauchoir Cotex','6130552001225','DEPOT0001','6130552001225','10','0')");
        //Accueil.bd.write("insert into produit_preparer (code_bon,codebar_pr,nom_pr,codebar_depot,nom_depot,codebar_lot,final_qt,current_qt) values('BP/30001','6130552001225','Mauchoir Cotex','6130552001225','DEPOT0001','6130552001225','10','0')");
        //Accueil.bd.write("insert into produit_preparer (code_bon,codebar_pr,nom_pr,codebar_depot,nom_depot,codebar_lot,final_qt,current_qt) values('BP/30001','35521546563','Intermec Printer','6130552001225','DEPOT0001','6130552001225','10','0')");


        Cursor r3=Accueil.bd.read("select * from produit limit 5");
        while (r3.moveToNext()){
            Log.e("ddd",r3.getString(r3.getColumnIndex("codebar"))+"");
        }




/*--------------------------------------------OFFICIEL--------------------------------------------------------*/


        //region test si le mobile has ID
        Cursor r2 = Accueil.bd.read("select device_name from  admin");
        if (r2.moveToNext()) {
            if(r2.getString(r2.getColumnIndex("device_name")) == null) {
                Intent intent = new Intent(getApplicationContext(), SetDeviceId.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        }
        //endregion

        //region test session existe ..
        r2=Accueil.bd.read("select * from session ");
        if (r2.moveToNext()){
            if (!r2.getString(r2.getColumnIndex("mode")).equals("admin")){
                session=new Session(new Employe((r2.getString(r2.getColumnIndex("code_emp")))));
            }else {
                session=new Session();
            }
             startActivity(new Intent(Login.this,Accueil.class));
             finish();
        }
        //endregion

        //region select mode
        ArrayList<String> items=new ArrayList<String>();
        items.add(getResources().getString(R.string.login_modeAdmin));
        items.add(getResources().getString(R.string.login_modeEmploye));

        spinnerDialog=new SpinnerDialog(Login.this,items,getResources().getString(R.string.login_mode));
        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String s, int i) {
                ((TextView)findViewById(R.id.login_valider)).setVisibility(View.VISIBLE);
                selectMode.setText(s);
                if (s.equals(getResources().getString(R.string.login_modeEmploye))){
                    selectEmp.setVisibility(View.VISIBLE);
                }else {
                    selectEmp.setVisibility(View.GONE);
                }
            }
        });
        selectMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinnerDialog.showSpinerDialog();
            }
        });

        //endregion

        //region select employé
        Cursor r=Accueil.bd.read("select * from employe");
        while (r.moveToNext()){
            employes.add(new SpinnerItem(r.getString(r.getColumnIndex("code_emp")),r.getString(r.getColumnIndex("nom_emp"))));
        }
        selectEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner_employes=new MySpinnerSearchable(Login.this, employes, "Select", new MySpinnerSearchable.SpinnerConfig() {
                    @Override
                    public void onChooseItem(int pos, SpinnerItem item) {
                        selectEmp.setText(item.value);
                        code_emp=item.key;
                        spinner_employes.closeSpinner();

                    }
                }, new MySpinnerSearchable.ButtonSpinnerOnClick() {
                    @Override
                    public void onClick() {
                        Session s=new Session();
                        s.openScannerCodeBarre(Login.this, new Session.OnScanListener() {
                            @Override
                            public void OnScan(String code,LinearLayout body) {
                                //check if codebar existe
                                checkCodeBarEmpExiste(code);
                            }
                        });

                    }
                });

                spinner_employes.openSpinner();

            }
        });
        //endregion

        //region valider connexion
        ((TextView)findViewById(R.id.login_valider)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectMode.getText().equals(getResources().getString(R.string.login_modeEmploye))) {
                    if (selectEmp.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_noEmpSelect), Toast.LENGTH_SHORT).show();
                    } else {
                        session = new Session(new Employe((code_emp)));
                        session.addToBD();
                        startActivity(new Intent(Login.this, Accueil.class));
                        finish();
                    }
                }else {
                    session = new Session();
                    session.addToBD();
                    startActivity(new Intent(Login.this, Accueil.class));
                    finish();
                }

            }
        });
        //endregion


    }


    public void checkCodeBarEmpExiste(String codebar){
        Cursor r=Accueil.bd.read("select * from employe where codebar='"+codebar+"' ");
        if (r.moveToNext()){
            code_emp=r.getString(r.getColumnIndex("code_emp"));
            selectEmp.setText(r.getString(r.getColumnIndex("nom_emp")));
            spinner_employes.closeSpinner();
        }else {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.codebar_noExist),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            checkCodeBarEmpExiste(data.getExtras().getString("code"));
        }
    }

    public static void changeLang(AppCompatActivity context, String lang) {
        Locale myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}
