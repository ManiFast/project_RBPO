package com.example.lab1.model;

import java.util.UUID;

public class Like {
    private String id;
    private String userId;
    private String postId;

    public Like() {
        this.id = UUID.randomUUID().toString();
    }

    public Like(String userId, String postId) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.postId = postId;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
}