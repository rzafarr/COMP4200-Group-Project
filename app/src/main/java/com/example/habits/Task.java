package com.example.habits;

public class Task {
    private int id;
    private String name;
    private String deadline;
    private int status;


    public Task(int id, String name, String deadline, int status) {
        this.id = id;
        this.name = name;
        this.deadline = deadline;
        this.status = status;

    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDeadline() { return deadline; }
    public int getStatus() { return status; }

}
