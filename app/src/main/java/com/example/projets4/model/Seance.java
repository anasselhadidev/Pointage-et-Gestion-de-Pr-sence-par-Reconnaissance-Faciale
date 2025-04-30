package com.example.projets4.model;

public class Seance {
    private String id;
    private String coursId;
    private String titre;
    private String professeurEmail;
    private String date;
    private String heureDebut;
    private String heureFin;
    private String salle;
    private boolean commencee;
    private boolean terminee;
    private String heureDebutEffective;
    private String heureFinEffective;
    private int nombreEtudiants;
    private int nombrePresents;
    private int nombreRetards;
    private int nombreAbsents;

    // Constructeur vide requis pour Firestore
    public Seance() {
    }

    public Seance(String coursId, String titre, String professeurEmail, String date, String heureDebut, String heureFin, String salle) {
        this.coursId = coursId;
        this.titre = titre;
        this.professeurEmail = professeurEmail;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.salle = salle;
        this.commencee = false;
        this.terminee = false;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoursId() {
        return coursId;
    }

    public void setCoursId(String coursId) {
        this.coursId = coursId;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public boolean isCommencee() {
        return commencee;
    }

    public void setCommencee(boolean commencee) {
        this.commencee = commencee;
    }

    public boolean isTerminee() {
        return terminee;
    }

    public void setTerminee(boolean terminee) {
        this.terminee = terminee;
    }

    public String getHeureDebutEffective() {
        return heureDebutEffective;
    }

    public void setHeureDebutEffective(String heureDebutEffective) {
        this.heureDebutEffective = heureDebutEffective;
    }

    public String getHeureFinEffective() {
        return heureFinEffective;
    }

    public void setHeureFinEffective(String heureFinEffective) {
        this.heureFinEffective = heureFinEffective;
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
}