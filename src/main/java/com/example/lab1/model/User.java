package com.example.lab1.model;

import java.util.UUID;

public class User {
    private String id;
    private String username;
    private String email;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String username, String email) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}