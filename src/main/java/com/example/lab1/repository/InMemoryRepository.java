package com.example.lab1.repository;

import com.example.lab1.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryRepository {
    private final List<User> users = new ArrayList<>();
    private final List<Post> posts = new ArrayList<>();
    private final List<Comment> comments = new ArrayList<>();
    private final List<Like> likes = new ArrayList<>();
    private final List<Follow> follows = new ArrayList<>();

    // User
    public User saveUser(User user) {
        users.add(user);
        return user;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public Optional<User> getUserById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public Optional<User> updateUser(String id, User updatedUser) {
        Optional<User> user = getUserById(id);
        user.ifPresent(u -> {
            u.setUsername(updatedUser.getUsername());
            u.setEmail(updatedUser.getEmail());
        });
        return user;
    }

    public boolean deleteUser(String id) {
        return users.removeIf(u -> u.getId().equals(id));
    }

    // Post
    public Post savePost(Post post) {
        posts.add(post);
        return post;
    }

    public List<Post> getAllPosts() {
        return new ArrayList<>(posts);
    }

    public Optional<Post> getPostById(String id) {
        return posts.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Optional<Post> updatePost(String id, Post updatedPost) {
        Optional<Post> post = getPostById(id);
        post.ifPresent(p -> p.setContent(updatedPost.getContent()));
        return post;
    }

    public boolean deletePost(String id) {
        return posts.removeIf(p -> p.getId().equals(id));
    }

    // Comment
    public Comment saveComment(Comment comment) {
        comments.add(comment);
        return comment;
    }

    public List<Comment> getCommentsByPostId(String postId) {
        return comments.stream().filter(c -> c.getPostId().equals(postId)).collect(Collectors.toList());
    }

    public Optional<Comment> getCommentById(String id) {
        return comments.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public Optional<Comment> updateComment(String id, Comment updatedComment) {
        Optional<Comment> comment = getCommentById(id);
        comment.ifPresent(c -> c.setContent(updatedComment.getContent()));
        return comment;
    }

    public boolean deleteComment(String id) {
        return comments.removeIf(c -> c.getId().equals(id));
    }

    // Like
    public Like saveLike(Like like) {
        if (likes.stream().noneMatch(l -> l.getUserId().equals(like.getUserId()) && l.getPostId().equals(like.getPostId()))) {
            likes.add(like);
        }
        return like;
    }

    public List<Like> getLikesByPostId(String postId) {
        return likes.stream().filter(l -> l.getPostId().equals(postId)).collect(Collectors.toList());
    }

    public boolean deleteLike(String userId, String postId) {
        return likes.removeIf(l -> l.getUserId().equals(userId) && l.getPostId().equals(postId));
    }

    // Follow
    public Follow saveFollow(Follow follow) {
        follows.add(follow);
        return follow;
    }

    public List<Follow> getFollowsByFollowerId(String followerId) {
        return follows.stream().filter(f -> f.getFollowerId().equals(followerId)).collect(Collectors.toList());
    }

    public boolean deleteFollow(String followerId, String followedId) {
        return follows.removeIf(f -> f.getFollowerId().equals(followerId) && f.getFollowedId().equals(followedId));
    }

    // Лента постов для подписчиков
    public List<Post> getFeedForUser(String userId) {
        List<String> followedIds = getFollowsByFollowerId(userId).stream()
                .map(Follow::getFollowedId)
                .collect(Collectors.toList());
        return posts.stream()
                .filter(p -> followedIds.contains(p.getUserId()))
                .collect(Collectors.toList());
    }
}