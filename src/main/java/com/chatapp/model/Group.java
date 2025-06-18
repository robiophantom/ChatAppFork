package com.chatapp.model;

// this is our Group class, a simple model to represent a chat group in our ChatApp
public class Group {
    // this holds the unique ID of the group, like a special tag to identify it
    private int id;
    // this stores the group's name, what users see when they select it
    private String name;
    // this keeps track of the user ID who created the group
    private int createdBy;

    // this constructor sets up a new Group with its ID, name, and creator's ID
    public Group(int id, String name, int createdBy) {
        // assign the provided ID to our group
        this.id = id;
        // set the group's name
        this.name = name;
        // record who created the group
        this.createdBy = createdBy;
    }

    // this method lets us get the group's ID whenever we need it
    public int getId() { return id; }
    // this method returns the group's name for display or other uses
    public String getName() { return name; }
    // this method gives us the ID of the user who created the group
    public int getCreatedBy() { return createdBy; }
}