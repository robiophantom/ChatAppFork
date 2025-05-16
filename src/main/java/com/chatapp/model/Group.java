package com.chatapp.model;

public class Group {
    private int id;
    private String name;
    private int createdBy;

    public Group(int id, String name, int createdBy) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCreatedBy() { return createdBy; }
}