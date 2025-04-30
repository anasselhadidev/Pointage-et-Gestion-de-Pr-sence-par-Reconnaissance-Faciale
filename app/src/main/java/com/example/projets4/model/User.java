package com.example.projets4.model;

public class User {
    private String email;
    private String nom;
    private String prenom;
    private String role;
    private String telephone;
    private String ville;
    private String pays;
    private boolean isActive = true;

    // Constructeur vide requis pour Firestore
    public User() {
    }

    public User(String email, String nom, String prenom, String role, String telephone) {
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.telephone = telephone;
    }

    // Getters et Setters
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}