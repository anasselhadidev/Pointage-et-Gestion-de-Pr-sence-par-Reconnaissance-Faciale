package com.example.projets4.model;

public class Terminal {
    private String id;
    private String name;
    private String location;
    private String type;
    private boolean online;
    private int batteryLevel;
    private boolean inMaintenance;
    private String lastSeen;
    private String firmwareVersion;

    // Constructeur vide requis pour Firestore
    public Terminal() {
    }

    public Terminal(String id, String name, String location, String type, boolean online, int batteryLevel) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.type = type;
        this.online = online;
        this.batteryLevel = batteryLevel;
        this.inMaintenance = false;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public boolean isInMaintenance() {
        return inMaintenance;
    }

    public void setInMaintenance(boolean inMaintenance) {
        this.inMaintenance = inMaintenance;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
}