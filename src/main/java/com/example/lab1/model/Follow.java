package com.example.lab1.model;

import java.util.UUID;

public class Follow {
    private String id;
    private String followerId; // Кто подписывается
    private String followedId; // На кого подписывается

    public Follow() {
        this.id = UUID.randomUUID().toString();
    }

    public Follow(String followerId, String followedId) {
        this.id = UUID.randomUUID().toString();
        this.followerId = followerId;
        this.followedId = followedId;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFollowerId() { return followerId; }
    public void setFollowerId(String followerId) { this.followerId = followerId; }
    public String getFollowedId() { return followedId; }
    public void setFollowedId(String followedId) { this.followedId = followedId; }
}