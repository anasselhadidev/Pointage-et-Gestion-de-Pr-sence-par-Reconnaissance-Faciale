package com.example.projets4.model;

public class Etudiant {
    private String id;
    private String email;
    private String nom;
    private String prenom;
    private String filiere;
    private String statut; // "Présent", "Retard", "Absent"
    private String heurePointage;

    // Constructeur vide requis pour Firestore
    public Etudiant() {
    }

    public Etudiant(String email, String nom, String prenom) {
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.statut = "Absent"; // Valeur par défaut
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getFiliere() {
        return filiere;
    }

    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getHeurePointage() {
        return heurePointage;
    }

    public void setHeurePointage(String heurePointage) {
        this.heurePointage = heurePointage;
    }
}