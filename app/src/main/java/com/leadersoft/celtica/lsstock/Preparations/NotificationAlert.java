package com.leadersoft.celtica.lsstock.Preparations;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.leadersoft.celtica.lsstock.Login;
import com.leadersoft.celtica.lsstock.R;

public class NotificationAlert extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_alert);
        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {

        }
    }
}
