package com.leadersoft.celtica.lsstock;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import com.google.zxing.Result;
import com.leadersoft.celtica.lsstock.EntréeSortie.FaireEntre;
import com.leadersoft.celtica.lsstock.EntréeSortie.FaireSortie;
import com.leadersoft.celtica.lsstock.EntréeSortie.ProduitEntree;
import com.leadersoft.celtica.lsstock.Inventaire.FaireInventaire;
import com.leadersoft.celtica.lsstock.Preparations.FairePreparation;
import com.leadersoft.celtica.lsstock.TransfertStock.FaireTransfert;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CodeBarScanner extends AppCompatActivity {


    private ZXingScannerView mScannerView;
    private FrameLayout camera_body;
    private CheckBox check_incr;
    public static REQUEST_SCANNER requestScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            //region Revenir a au Deviceconfig ..
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        } else {

            //region check camera permission ..
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(CodeBarScanner.this, new String[]{Manifest.permission.CAMERA}, 8);

                Log.e("ccc","non ");

            }
            //endregion

            else {

                //region afficher le layout de scanner Incre
                if(getIntent().getExtras() != null) {
                    setContentView(R.layout.activity_codebar_scanner);
                    mScannerView=(ZXingScannerView)findViewById(R.id.camera_scanner);
                    camera_body=(FrameLayout)findViewById(R.id.camera_body);
                    check_incr=(CheckBox)findViewById(R.id.camera_scanner_check_incr);

                }
                //endregion

                //region aficher le layout d un seul scan ..
                else {

                    mScannerView = new ZXingScannerView(this); // Programmatically initialize the scanner view
                    setContentView(mScannerView);
                }
                //endregion

                mScannerView.startCamera();

                //region on scann result ..
                mScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
                    @Override
                    public void handleResult(Result result) {
                        // Log.e("code"," "+result.getText());
                        mScannerView.resumeCameraPreview(this);

                        //test si scann avec incrementation
                        if(getIntent().getExtras() != null){

                            //region faire  transfert ..
                            if(requestScanner == REQUEST_SCANNER.TRANSFERT) {
                                if (!check_incr.isChecked()){
                                    FaireTransfert.withIncr=false;
                                    Intent i = new Intent();
                                    i.putExtra("code", "" + result.getText());
                                    setResult(RESULT_OK, i);
                                    finish();
                                }else {
                                    FaireTransfert.withIncr=true;
                                    if(FaireTransfert.me.checkPrExiste(result.getText())) {
                                        Login.session.changeColorOnScan(camera_body,getResources().getColor(R.color.Green),getResources().getColor(R.color.White));
                                        FaireTransfert.me.addPrToPanier(result.getText(), 1);
                                    }else {
                                        Login.session.changeColorOnScan(camera_body,getResources().getColor(R.color.Red),getResources().getColor(R.color.White));
                                    }
                                }

                            }
                            //endregion

                            //region faire  inventaire ..
                            else if(requestScanner == REQUEST_SCANNER.INVENTAIRE) {
                                if (!check_incr.isChecked()){
                                    FaireInventaire.withIncr=false;
                                    Intent i = new Intent();
                                    i.putExtra("code", "" + result.getText());
                                    setResult(RESULT_OK, i);
                                    finish();
                                }else {
                                    FaireInventaire.withIncr=true;
                                    if (FaireInventaire.me.checkPrExiste(result.getText())) {
                                        Login.session.changeColorOnScan(camera_body, getResources().getColor(R.color.Green), getResources().getColor(R.color.White));
                                        FaireInventaire.me.addPrToPanier(result.getText(), 1);
                                    } else {
                                        Login.session.changeColorOnScan(camera_body, getResources().getColor(R.color.Red), getResources().getColor(R.color.White));
                                    }
                                }
                            }
                            //endregion

                            //region faire  Preparation ..
                            else if (requestScanner == REQUEST_SCANNER.PREPARATION){
                                if (!check_incr.isChecked()){
                                    FairePreparation.withIncr=false;
                                    Intent i = new Intent();
                                    i.putExtra("code", "" + result.getText());
                                    setResult(RESULT_OK, i);
                                    finish();
                                }else {
                                    FairePreparation.withIncr=true;
                                    if (FairePreparation.me.checkPrExiste(result.getText())) {
                                        Login.session.changeColorOnScan(camera_body, getResources().getColor(R.color.Green), getResources().getColor(R.color.White));
                                        FairePreparation.me.addPrToPanier(result.getText(), 1);
                                    } else {
                                        Login.session.changeColorOnScan(camera_body, getResources().getColor(R.color.Red), getResources().getColor(R.color.White));
                                    }
                                }
                            }
                            //endregion

                            //region faire entree ..
                            else if (requestScanner == REQUEST_SCANNER.ENTREE_STOCK){
                                if (!check_incr.isChecked()){
                                    FaireEntre.me.withIncr=false;
                                    Intent i = new Intent();
                                    i.putExtra("code", "" + result.getText());
                                    setResult(RESULT_OK, i);
                                    finish();
                                }else {
                                    FaireEntre.me.withIncr=true;
                                    FaireEntre.me.checkProduitExiste(result.getText());
                                    Login.session.changeColorOnScan(camera_body,getResources().getColor(R.color.Green),getResources().getColor(R.color.White));
                                    FaireEntre.me.addProduit(result.getText(),1);
                                }
                            }
                            //endregion

                            //region faire sortie ..
                            else {
                                if (!check_incr.isChecked()){
                                    FaireSortie.me.withIncr=false;
                                    Intent i = new Intent();
                                    i.putExtra("code", "" + result.getText());
                                    setResult(RESULT_OK, i);
                                    finish();
                                }else {
                                    FaireSortie.me.withIncr=true;
                                    if(FaireSortie.me.checkProduitExiste(result.getText())){
                                        Login.session.changeColorOnScan(camera_body,getResources().getColor(R.color.Green),getResources().getColor(R.color.White));
                                        FaireSortie.me.addProduit(result.getText(),1);
                                    }else {
                                        Login.session.changeColorOnScan(camera_body,getResources().getColor(R.color.Red),getResources().getColor(R.color.White));
                                    }

                                }

                            }
                            //endregion

                        }else {
                            Intent i = new Intent();
                            i.putExtra("code", "" + result.getText());
                            setResult(RESULT_OK, i);
                            finish();
                        }


                    }
                });
                //endregion

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera(); // Stop camera on pause
    }


    }
