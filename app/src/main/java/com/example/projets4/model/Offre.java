package com.example.projets4.model;

public class Offre {
    private String id;  // Add a unique ID for each offer
    private String titre;
    private String description;
    private float superficie;
    private int pieces;
    private int sdb;
    private float loyer;
    private String agentid;
    private String agentemail;

    // Constructor to initialize an offer

    public Offre(){
    }

    public Offre(String id, String titre, String description, float superficie, int pieces, int sdb, float loyer) {
        this.id = id;  // Set the offer's unique ID
        this.titre = titre;
        this.description = description;
        this.superficie = superficie;
        this.pieces = pieces;
        this.sdb = sdb;
        this.loyer = loyer;
    }

    // Getters and setters for each field
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getSuperficie() {
        return superficie;
    }

    public void setSuperficie(float superficie) {
        this.superficie = superficie;
    }

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
    }

    public int getSdb() {
        return sdb;
    }

    public void setSdb(int sdb) {
        this.sdb = sdb;
    }

    public float getLoyer() {
        return loyer;
    }

    public void setLoyer(float loyer) {
        this.loyer = loyer;
    }

    public void setAgentId(String agentId) {
        this.agentid = agentId;
    }
    public String getAgentId() {
        return this.agentid;
    }

    public void setAgentEmail(String agentEmail) {
        this.agentemail = agentEmail;
    }
    public String getAgentEmail() {
        return this.agentemail;
    }
}
