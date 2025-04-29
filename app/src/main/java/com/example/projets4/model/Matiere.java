package com.example.projets4.model;

public class Matiere {
    private String nom;
    private String id;

    public Matiere() {}
    public Matiere(String nom) { this.nom = nom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}