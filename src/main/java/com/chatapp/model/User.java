package com.chatapp.model;

// this is our User class, a simple model to represent a user in our ChatApp
public class User {
    // this holds the unique ID of the user, like a special badge to identify them
    private int id;
    // this stores the user's username, their display name in the app
    private String username;
    // this keeps the user's profile picture as a byte array, for showing their photo
    private byte[] profilePicture;

    // this constructor sets up a new User with their ID, username, and profile picture
    public User(int id, String username, byte[] profilePicture) {
        // assign the provided ID to our user
        this.id = id;
        // set the user's username
        this.username = username;
        // store the profile picture, which can be null if they don't have one
        this.profilePicture = profilePicture;
    }

    // this method lets us get the user's ID whenever we need it
    public int getId() { return id; }
    // this method returns the user's username for display or other uses
    public String getUsername() { return username; }
    // this method gives us the profile picture as bytes, for showing in the UI
    public byte[] getProfilePicture() { return profilePicture; }
}