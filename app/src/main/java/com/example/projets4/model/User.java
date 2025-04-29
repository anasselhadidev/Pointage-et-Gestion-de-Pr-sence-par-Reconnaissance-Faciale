package com.example.projets4.model;

public class User {
    private String email;
    private String role;
    private String nom;

    private String pays;
    private String ville;
    private String prenom;
    private String telephone;

    public User() {}

    public User(String email, String role, String nom, String prenom, String telephone) {
        this.email = email;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
    }

    // Getters
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getTelephone() { return telephone; }

    public String getPays() { return pays; }

    public String getVille() { return ville; }
}
