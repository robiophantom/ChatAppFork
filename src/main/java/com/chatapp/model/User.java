package com.chatapp.model;

public class User {
    private int id;
    private String username;
    private byte[] profilePicture;

    public User(int id, String username, byte[] profilePicture) {
        this.id = id;
        this.username = username;
        this.profilePicture = profilePicture;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public byte[] getProfilePicture() { return profilePicture; }
}