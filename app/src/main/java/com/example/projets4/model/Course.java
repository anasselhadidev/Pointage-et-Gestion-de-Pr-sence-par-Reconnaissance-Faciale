package com.example.projets4.model;

public class Course {
    private String title;
    private String schedule;
    private String room;
    private int progress;
    private String status;

    public Course(String title, String schedule, String room, int progress, String status) {
        this.title = title;
        this.schedule = schedule;
        this.room = room;
        this.progress = progress;
        this.status = status;
    }


    // Getters
    public String getTitle() { return title; }
    public String getSchedule() { return schedule; }
    public String getRoom() { return room; }
    public int getProgress() { return progress; }
    public String getStatus() { return status; }
}
