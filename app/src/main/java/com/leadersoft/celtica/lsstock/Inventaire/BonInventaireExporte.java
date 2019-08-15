package com.leadersoft.celtica.lsstock.Inventaire;

class BonInventaireExporte extends  BonInventaire {
    public BonInventaireExporte(String id_bon, String code_depot, String nom_depot, String date_bon, String code_emp) {
        super(id_bon, code_depot, nom_depot, date_bon, code_emp);
    }

    public BonInventaireExporte(String code_depot) {
        super(code_depot);
    }
}
