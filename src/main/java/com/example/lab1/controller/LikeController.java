package com.example.lab1.controller;

import com.example.lab1.model.Like;
import com.example.lab1.repository.InMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/likes")
public class LikeController {
    private final InMemoryRepository repository;

    @Autowired
    public LikeController(InMemoryRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Like createLike(@RequestBody Like like) {
        return repository.saveLike(like);
    }

    @GetMapping("/post/{postId}")
    public List<Like> getLikesByPostId(@PathVariable String postId) {
        return repository.getLikesByPostId(postId);
    }

    @DeleteMapping("/user/{userId}/post/{postId}")
    public ResponseEntity<Void> deleteLike(@PathVariable String userId, @PathVariable String postId) {
        boolean deleted = repository.deleteLike(userId, postId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}