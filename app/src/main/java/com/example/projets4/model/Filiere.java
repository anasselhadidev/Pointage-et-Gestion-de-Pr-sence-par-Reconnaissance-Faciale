package com.example.projets4.model;

public class Filiere {
    private String id;
    private String nom;
    private String description;

    public Filiere() {} // Needed for Firestore

    public Filiere(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

