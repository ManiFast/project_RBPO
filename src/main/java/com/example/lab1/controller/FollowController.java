package com.example.lab1.controller;

import com.example.lab1.model.Follow;
import com.example.lab1.model.Post;
import com.example.lab1.repository.InMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follows")
public class FollowController {
    private final InMemoryRepository repository;

    @Autowired
    public FollowController(InMemoryRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Follow createFollow(@RequestBody Follow follow) {
        return repository.saveFollow(follow);
    }

    @GetMapping("/follower/{followerId}")
    public List<Follow> getFollowsByFollowerId(@PathVariable String followerId) {
        return repository.getFollowsByFollowerId(followerId);
    }

    @GetMapping("/feed/{userId}")
    public List<Post> getFeed(@PathVariable String userId) {
        return repository.getFeedForUser(userId);
    }

    @DeleteMapping("/follower/{followerId}/followed/{followedId}")
    public ResponseEntity<Void> deleteFollow(@PathVariable String followerId, @PathVariable String followedId) {
        boolean deleted = repository.deleteFollow(followerId, followedId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}