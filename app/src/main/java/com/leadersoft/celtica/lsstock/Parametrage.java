package com.leadersoft.celtica.lsstock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsstock.EntréeSortie.FaireEntre;
import com.leadersoft.celtica.lsstock.Inventaire.AfficherInventaires;
import com.leadersoft.celtica.lsstock.TransfertStock.AfficherBonsTransfert;

import java.sql.SQLException;

public class Parametrage extends AppCompatActivity {
    private EditText nom,mdp;
    private LinearLayout div_admin;
    private ScrollView div_param;
    private LinearLayout div_sql;
    ProgressDialog progress;

    EditText ip,port,bd,user,mdp2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametrage);
        if (savedInstanceState != null) {
            //region Revenir a au Login ..
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion

        }else {

            progress = new ProgressDialog(Parametrage.this);

            //region Conexion d admin ..
            div_admin = (LinearLayout) findViewById(R.id.param_div_admin);
            div_param = (ScrollView) findViewById(R.id.param_div_param);


            nom = (EditText) findViewById(R.id.param_admin_nom);
            mdp = (EditText) findViewById(R.id.param_admin_mdp);
            ((TextView) findViewById(R.id.param_admin_conn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (nom.getText().toString().equals("") || mdp.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.remplissage_err), Toast.LENGTH_SHORT).show();
                    } else {
                        if (Login.session.pseudo_admin.equals(nom.getText().toString())) {
                            if (Login.session.mdp_admin.equals(mdp.getText().toString())) {
                                div_admin.setVisibility(View.GONE);
                                div_param.setVisibility(View.VISIBLE);

                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.param_mdp_err), Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.param_user_err), Toast.LENGTH_SHORT).show();

                        }

                    }

                }
            });
            //endregion

            //region afficher archive ..
            ((LinearLayout)findViewById(R.id.param_archive)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(Parametrage.this, AfficherBonsTransfert.class);
                    i.putExtra("request","archive");
                    startActivity(i);
                }
            });

            //endregion

            //region change  type device ..
            ((LinearLayout)findViewById(R.id.param_type_device)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder mb = new AlertDialog.Builder(Parametrage.this); //c est l activity non le context ..

                    View v= getLayoutInflater().inflate(R.layout.change_type_device,null);
                    RadioGroup check=(RadioGroup) v.findViewById(R.id.check);
                    RadioButton sans=(RadioButton)v.findViewById(R.id.type_sans);
                    RadioButton avec=(RadioButton)v.findViewById(R.id.type_avec);
                    mb.setView(v);
                    final AlertDialog ad=mb.create();
                    ad.show();
                    ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                    //ad.setCancelable(false); //désactiver le button de retour ..

                    if(Login.session.type.equals(getResources().getString(R.string.config_type_sansscanner))){
                        sans.setChecked(true);
                    }else {
                        avec.setChecked(true);
                    }


                    check.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup ch, int i) {
                            if(ch.getCheckedRadioButtonId() == R.id.type_avec){
                                Login.session.changerTypeDevice(getResources().getString(R.string.config_type_scanner));
                            }else {
                                Login.session.changerTypeDevice(getResources().getString(R.string.config_type_sansscanner));
                            }
                            ad.dismiss();
                        }
                    });


                }
            });

            //endregion

            //region changer le deviceId ..
            ((LinearLayout) findViewById(R.id.param_change_deviceId)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder mb = new AlertDialog.Builder(Parametrage.this); //c est l activity non le context ..
                    View v = getLayoutInflater().inflate(R.layout.div_msg_inp, null);
                    TextView valider = (TextView) v.findViewById(R.id.div_msgInp_add);
                    TextView msg = (TextView) v.findViewById(R.id.div_msgInp_msg);
                    final EditText deviceId = (EditText) v.findViewById(R.id.div_msgInp_value);

                    mb.setView(v);
                    final AlertDialog ad = mb.create();
                    ad.show();

                    msg.setText(getResources().getString(R.string.param_deviecId_titre)+"");
                    deviceId.setText(Login.session.deviceId+"");

                    //region valider le changement ..
                    valider.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (deviceId.getText().toString().equals("")){
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.remplissage_err),Toast.LENGTH_SHORT).show();

                            }else {
                                Login.session.changeDeviceId(deviceId.getText().toString());
                                ad.dismiss();
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_ok),Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    //endregion

                }
            });
            //endregion

            //region configure sqlserver
            div_sql=(LinearLayout)findViewById(R.id.DivSqlConnect);
            ((LinearLayout)findViewById(R.id.param_sql_config)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    div_param.setVisibility(View.GONE);
                    div_sql.setVisibility(View.VISIBLE);

                    ip=(EditText)findViewById(R.id.synch_ip);
                    port=(EditText)findViewById(R.id.synch_port);
                    user=(EditText)findViewById(R.id.synch__user);
                    bd=(EditText)findViewById(R.id.synch_bd);
                    mdp2=(EditText)findViewById(R.id.synch_mdp);

                    Cursor r5= Accueil.bd.read("select * from sqlconnect");

                    if(Login.session.serveur.ip != null) {
                        ip.setText(Login.session.serveur.ip);
                        port.setText(Login.session.serveur.port);
                        user.setText(Login.session.serveur.user);
                        bd.setText(Login.session.serveur.bdName);
                        mdp2.setText(Login.session.serveur.mdp);
                    }
                }
            });
            //endregion

            //region connecter sql server
            ((TextView)findViewById(R.id.synch_Butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Accueil.BDsql=new SqlServerBD(ip.getText().toString(), port.getText().toString(), bd.getText().toString(), user.getText().toString(), mdp2.getText().toString(), "net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                            @Override
                            public void echec() {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_conect_err),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void before() {
                                progress.setTitle("Connexion");
                                progress.setMessage("attendez SVP...");
                                progress.show();
                            }

                            @Override
                            public void After() throws SQLException {
                                progress.dismiss();
                                Login.session.serveur.majInfos(ip.getText().toString(), port.getText().toString(), bd.getText().toString(), user.getText().toString(), mdp2.getText().toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_conect_succ),Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        });
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                }
            });

            //endregion

            //region formater la bd ..
            ((LinearLayout)findViewById(R.id.param_formatBD)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View vv) {
                    AlertDialog.Builder mb = new AlertDialog.Builder(Parametrage.this); //c est l activity non le context ..

                    View v= getLayoutInflater().inflate(R.layout.confirm_box,null);
                    TextView oui=(TextView) v.findViewById(R.id.confirm_oui);
                    TextView non=(TextView) v.findViewById(R.id.confirm_non);
                    TextView msg=(TextView) v.findViewById(R.id.confirm_msg);

                    msg.setText(getResources().getString(R.string.param_bd_format_confirm));

                    mb.setView(v);
                    final AlertDialog ad=mb.create();
                    ad.show();
                    ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                    ad.setCancelable(false); //désactiver le button de retour ..

                    non.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ad.dismiss();
                        }
                    });

                    oui.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Login.session.formaterBD();
                            ad.dismiss();
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.param_bd_format_succ),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            //endregion

            //region fermer la session actuel de préparateur ..
            ((LinearLayout) findViewById(R.id.param_deconect)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Login.session.deconect();
                    Login.session.libererDepot();

                     AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent intent = new Intent(getApplicationContext(), AlertReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 111, intent, 0);
                    alarmMgr.cancel(alarmIntent);

                    Intent i = new Intent(getApplicationContext(), Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            });

            //endregion
        }
    }

    @Override
    public void onBackPressed() {
        if(div_sql.getVisibility()== View.VISIBLE) {
            div_sql.setVisibility(View.GONE);
            div_param.setVisibility(View.VISIBLE);
        }
        else super.onBackPressed();
    }
}
