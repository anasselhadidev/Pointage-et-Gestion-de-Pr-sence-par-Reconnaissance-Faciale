package com.example.projets4.model;

public class Reclamation {
    private String idClient;
    private String objet;
    private String message;
    private String date;
    private String etat;

    public Reclamation() {}

    public Reclamation(String idClient, String objet, String message, String date, String etat) {
        this.idClient = idClient;
        this.objet = objet;
        this.message = message;
        this.date = date;
        this.etat = etat;
    }

    public String getIdClient() {
        return idClient;
    }

    public String getObjet() {
        return objet;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }
}
