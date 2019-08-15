package com.leadersoft.celtica.lsstock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.leadersoft.celtica.lsstock.Preparations.NotificationAlert;

import java.sql.ResultSet;
import java.sql.SQLException;

class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        /*
        try {
            if(Accueil.bd == null)  Accueil.bd=new MyBD("ls_stock.db",context);

            ServeurInfos s=new ServeurInfos();
            Accueil.BDsql=new SqlServerBD(s.ip, s.port, s.bdName, s.user, s.mdp, "net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                @Override
                public void echec() {
                    Log.e("ttt","ERR CNCT ...");
                }

                @Override
                public void before() {

                }

                @Override
                public void After() throws SQLException {
                    //region récupération des bon de commande dun preparateur ..
                    Accueil.BDsql.read("SELECT\t[InventoryPreparationNote].Oid\n" +
                            "    \t\t, [Document].[date]\n" +
                            "    \t\t, [Document].[code]\n" +
                            "    \t\t, [InventoryPreparationNote].temporaryInputZone\n" +
                            "    \t\t, [Product].EAN13Code as product_barCode\n" +
                            "    \t\t, [Product].[name] as pr_name , [DocumentItem].quantityToBePrepared finalQuantity \n" +
                            "    \t\t, [Warehouse].barCode as warehouse_barCode\n" +
                            "    \t\t, [Warehouse].[name] as depot_name,Employé.matricule_employé \n" +
                            "\t\t\t, [Company].[name]\n" +
                            "    \n" +
                            "    FROM  [InventoryPreparationNote]\n" +
                            "    INNER JOIN Document\n" +
                            "    ON Document.Oid = [InventoryPreparationNote].Oid\n" +
                            "    \tAND [InventoryPreparationNote].isCanceled = 0\n" +
                            "    \tAND Document.isValidated = 0\n" +
                            "    \tAND [InventoryPreparationNote].[manualInput] = 1\n" +
                            "    \tAND [Document].GCRecord IS NULL\n" +
                            "    INNER JOIN Employé ON [InventoryPreparationNote].employee=Employé.Oid  INNER JOIN DocumentItem\n" +
                            "    ON\tDocument.Oid = DocumentItem.document\n" +
                            "    \tAND DocumentItem.GCRecord IS NULL\n" +
                            "    INNER JOIN Product \n" +
                            "    ON Product.Oid = DocumentItem.product\n" +
                            "    INNER JOIN Warehouse\n" +
                            "    ON Warehouse.Oid = DocumentItem.warehouse \n" +
                            "\t inner join Company on Document.contact = Company.Oid\n" +
                            "\t where matricule_employé='"+Login.session.employe.code_emp+"' and  not exists(select * from DocumentItem where GCRecord is null and document = [Document].Oid and quantity <> 0)", new SqlServerBD.doAfterBeforeGettingData() {
                        @Override
                        public void echec(SQLException e) {
                            e.printStackTrace();
                            Log.e("ttt","errrrrrrr");
                        }

                        @Override
                        public void before() {

                        }

                        @Override
                        public void After() {

                            //il faut d abord supprimer les anciens prep ..
                            final ResultSet r=Accueil.BDsql.r;

                            try {
                                if (r.next()){
                                    Log.e("ttt","KAYEN HMD ....");
                                    context.startActivity(new Intent(context, NotificationAlert.class));
                                }else {
                                    Log.e("ttt","MAKACH ....");
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }


                        }
                    });
                    //endregion
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
        */
    }
}
