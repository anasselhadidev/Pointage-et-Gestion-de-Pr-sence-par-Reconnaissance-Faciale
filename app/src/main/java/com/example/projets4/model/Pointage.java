package com.example.projets4.model;

public class Pointage {
    private String id;
    private String etudiantEmail;
    private String cours;
    private String date;
    private String typePointage; // "Entrée" ou "Sortie"
    private String terminal;
    private String statut; // "Présent", "Retard", "Absent"

    // Constructeur vide requis pour Firestore
    public Pointage() {}

    public Pointage(String etudiantEmail, String cours, String date,
                    String typePointage, String terminal, String statut) {
        this.etudiantEmail = etudiantEmail;
        this.cours = cours;
        this.date = date;
        this.typePointage = typePointage;
        this.terminal = terminal;
        this.statut = statut;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEtudiantEmail() {
        return etudiantEmail;
    }

    public void setEtudiantEmail(String etudiantEmail) {
        this.etudiantEmail = etudiantEmail;
    }

    public String getCours() {
        return cours;
    }

    public void setCours(String cours) {
        this.cours = cours;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTypePointage() {
        return typePointage;
    }

    public void setTypePointage(String typePointage) {
        this.typePointage = typePointage;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}