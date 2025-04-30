package com.example.projets4.model;

public class Rapport {
    private String id;
    private String titre;
    private String professeurEmail;
    private String dateGeneration;
    private String dateDebut;
    private String dateFin;
    private String typeRapport;
    private int nombreSeances;
    private int nombreEtudiants;
    private int nombrePresents;
    private int nombreRetards;
    private int nombreAbsents;
    private double tauxPresence;
    private double tauxRetard;
    private double tauxAbsence;

    // Constructeur vide requis pour Firestore
    public Rapport() {
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getProfesseurEmail() {
        return professeurEmail;
    }

    public void setProfesseurEmail(String professeurEmail) {
        this.professeurEmail = professeurEmail;
    }

    public String getDateGeneration() {
        return dateGeneration;
    }

    public void setDateGeneration(String dateGeneration) {
        this.dateGeneration = dateGeneration;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public String getTypeRapport() {
        return typeRapport;
    }

    public void setTypeRapport(String typeRapport) {
        this.typeRapport = typeRapport;
    }

    public int getNombreSeances() {
        return nombreSeances;
    }

    public void setNombreSeances(int nombreSeances) {
        this.nombreSeances = nombreSeances;
    }

    public int getNombreEtudiants() {
        return nombreEtudiants;
    }

    public void setNombreEtudiants(int nombreEtudiants) {
        this.nombreEtudiants = nombreEtudiants;
    }

    public int getNombrePresents() {
        return nombrePresents;
    }

    public void setNombrePresents(int nombrePresents) {
        this.nombrePresents = nombrePresents;
    }

    public int getNombreRetards() {
        return nombreRetards;
    }

    public void setNombreRetards(int nombreRetards) {
        this.nombreRetards = nombreRetards;
    }

    public int getNombreAbsents() {
        return nombreAbsents;
    }

    public void setNombreAbsents(int nombreAbsents) {
        this.nombreAbsents = nombreAbsents;
    }

    public double getTauxPresence() {
        return tauxPresence;
    }

    public void setTauxPresence(double tauxPresence) {
        this.tauxPresence = tauxPresence;
    }

    public double getTauxRetard() {
        return tauxRetard;
    }

    public void setTauxRetard(double tauxRetard) {
        this.tauxRetard = tauxRetard;
    }

    public double getTauxAbsence() {
        return tauxAbsence;
    }

    public void setTauxAbsence(double tauxAbsence) {
        this.tauxAbsence = tauxAbsence;
    }
}