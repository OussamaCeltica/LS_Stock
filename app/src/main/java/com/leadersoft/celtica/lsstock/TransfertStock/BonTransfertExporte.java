package com.leadersoft.celtica.lsstock.TransfertStock;

/**
 * Created by celtica on 07/05/19.
 */

public class BonTransfertExporte extends BonTransfert {
    public BonTransfertExporte(String id_bon, String codebar_dep_src, String nom_dep_src, String codebar_dep_dest, String nom_dep_dest, String date_trans) {
        super(id_bon, codebar_dep_src, nom_dep_src, codebar_dep_dest, nom_dep_dest, date_trans);
    }
}
