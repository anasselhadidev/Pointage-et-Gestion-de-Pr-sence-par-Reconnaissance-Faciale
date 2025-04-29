package com.example.projets4.model;

public class Justificatif {
    private String id;
    private String etudiantEmail;
    private String dateAbsence;
    private String dateSoumission;
    private String motif;
    private String commentaire;
    private String documentUrl;
    private String status; // "En attente", "Accepté", "Refusé"

    // Constructeur vide requis pour Firestore
    public Justificatif() {}

    public Justificatif(String etudiantEmail, String dateAbsence, String dateSoumission,
                        String motif, String commentaire, String documentUrl, String status) {
        this.etudiantEmail = etudiantEmail;
        this.dateAbsence = dateAbsence;
        this.dateSoumission = dateSoumission;
        this.motif = motif;
        this.commentaire = commentaire;
        this.documentUrl = documentUrl;
        this.status = status;
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

    public String getDateAbsence() {
        return dateAbsence;
    }

    public void setDateAbsence(String dateAbsence) {
        this.dateAbsence = dateAbsence;
    }

    public String getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(String dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}