package com.leadersoft.celtica.lsstock.Preparations;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.leadersoft.celtica.lsstock.Accueil;
import com.leadersoft.celtica.lsstock.MyBD;
import com.leadersoft.celtica.lsstock.SqlServerBD;
import com.leadersoft.celtica.lsstock.Synchronisation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by celtica on 16/05/19.
 */

public class BonPreparationValidé extends BonPreparation {
    public BonPreparationValidé(String code_bon, String date_bon, String oid_bon) {
        super(code_bon, date_bon, oid_bon);
    }


}
