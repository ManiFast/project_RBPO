package com.example.lab1.controller;

import com.example.lab1.model.Post;
import com.example.lab1.repository.InMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final InMemoryRepository repository;

    @Autowired
    public PostController(InMemoryRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return repository.savePost(post);
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return repository.getAllPosts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable String id) {
        Optional<Post> post = repository.getPostById(id);
        return post.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable String id, @RequestBody Post post) {
        Optional<Post> updatedPost = repository.updatePost(id, post);
        return updatedPost.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        boolean deleted = repository.deletePost(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}