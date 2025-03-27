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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
