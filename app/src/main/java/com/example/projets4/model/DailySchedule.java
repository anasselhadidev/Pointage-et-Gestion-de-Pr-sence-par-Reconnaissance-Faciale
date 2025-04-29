package com.example.projets4.model;

public class DailySchedule {
    private String dayName;
    private String morningSubject;
    private String eveningSubject;

    public DailySchedule(String dayName, String morningSubject, String eveningSubject) {
        this.dayName = dayName;
        this.morningSubject = morningSubject;
        this.eveningSubject = eveningSubject;
    }

    public String getDayName() {
        return dayName;
    }

    public String getMorningSubject() {
        return morningSubject;
    }

    public String getEveningSubject() {
        return eveningSubject;
    }
}
