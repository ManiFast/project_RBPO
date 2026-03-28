package ru.mtuci.coursemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.model.*;
import ru.mtuci.coursemanagement.repository.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class SocialController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @GetMapping("/social")
    public String socialPage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        List<Post> posts = postRepository.findAll();
        Map<Long, List<Comment>> commentsByPost = new HashMap<>();
        for (Post post : posts) {
            commentsByPost.put(post.getId(), commentRepository.findByPostId(post.getId()));
        }

        userRepository.findByUsername(authentication.getName())
                .ifPresent(user -> model.addAttribute("currentUserId", user.getId()));

        model.addAttribute("posts", posts);
        model.addAttribute("commentsByPost", commentsByPost);
        return "social";
    }

    @PostMapping("/api/social/posts")
    @ResponseBody
    public ResponseEntity<?> createPost(@RequestParam String content, Authentication authentication) {
        try {
            User author = getCurrentUser(authentication);
            Post post = new Post();
            post.setContent(content);
            post.setAuthor(author);
            post.setCreatedAt(LocalDateTime.now());
            postRepository.save(post);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("message", "Пост создан");
            response.put("postId", post.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @GetMapping("/api/social/posts")
    @ResponseBody
    public ResponseEntity<?> getAllPosts() {
        try {
            return ResponseEntity.ok(postRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @GetMapping("/api/social/posts/{postId}")
    @ResponseBody
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        return postRepository.findById(postId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Пост не найден"));
    }

    @PutMapping("/api/social/posts/{postId}")
    @ResponseBody
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestParam String content,
                                        Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Optional<Post> postOpt = postRepository.findById(postId);

            if (postOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Пост не найден");
            }

            Post post = postOpt.get();
            if (!post.getAuthor().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body("Это не ваш пост");
            }

            post.setContent(content);
            postRepository.save(post);
            return ResponseEntity.ok("Пост обновлен");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @DeleteMapping("/api/social/posts/{postId}")
    @ResponseBody
    public ResponseEntity<?> deletePost(@PathVariable Long postId, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Optional<Post> postOpt = postRepository.findById(postId);

            if (postOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Пост не найден");
            }

            Post post = postOpt.get();
            if (!post.getAuthor().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body("Это не ваш пост");
            }

            likeRepository.deleteByPost(post);
            commentRepository.deleteByPost(post);
            postRepository.delete(post);
            return ResponseEntity.ok("Пост удален");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @PostMapping("/api/social/comments")
    @ResponseBody
    public ResponseEntity<?> createComment(@RequestParam String content,
                                           @RequestParam Long postId,
                                           Authentication authentication) {
        try {
            User author = getCurrentUser(authentication);
            Optional<Post> post = postRepository.findById(postId);

            if (post.isEmpty()) {
                return ResponseEntity.status(404).body("Пост не найден");
            }

            Comment comment = new Comment();
            comment.setContent(content);
            comment.setAuthor(author);
            comment.setPost(post.get());
            comment.setCreatedAt(LocalDateTime.now());
            commentRepository.save(comment);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("message", "Комментарий создан");
            response.put("commentId", comment.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @GetMapping("/api/social/comments")
    @ResponseBody
    public ResponseEntity<?> getAllComments() {
        try {
            return ResponseEntity.ok(commentRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @GetMapping("/api/social/posts/{postId}/comments")
    @ResponseBody
    public ResponseEntity<?> getPostComments(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(commentRepository.findByPostId(postId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @PutMapping("/api/social/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                           @RequestParam String content,
                                           Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Optional<Comment> commentOpt = commentRepository.findById(commentId);

            if (commentOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Комментарий не найден");
            }

            Comment comment = commentOpt.get();
            if (!comment.getAuthor().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body("Это не ваш комментарий");
            }

            comment.setContent(content);
            commentRepository.save(comment);
            return ResponseEntity.ok("Комментарий обновлен");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @DeleteMapping("/api/social/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId,
                                           Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Optional<Comment> commentOpt = commentRepository.findById(commentId);

            if (commentOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Комментарий не найден");
            }

            Comment comment = commentOpt.get();
            if (!comment.getAuthor().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body("Это не ваш комментарий");
            }

            commentRepository.delete(comment);
            return ResponseEntity.ok("Комментарий удален");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @PostMapping("/api/social/posts/{postId}/like")
    @ResponseBody
    public ResponseEntity<?> likePost(@PathVariable Long postId, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Optional<Post> postOpt = postRepository.findById(postId);

            if (postOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Пост не найден");
            }

            Post post = postOpt.get();
            if (likeRepository.existsByUserAndPost(currentUser, post)) {
                return ResponseEntity.badRequest().body("Уже есть лайк");
            }

            Like like = new Like();
            like.setUser(currentUser);
            like.setPost(post);
            like.setCreatedAt(LocalDateTime.now());
            likeRepository.save(like);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("message", "Лайк поставлен");
            response.put("likeId", like.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @GetMapping("/api/social/posts/{postId}/likes")
    @ResponseBody
    public ResponseEntity<?> getPostLikes(@PathVariable Long postId) {
        try {
            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Пост не найден");
            }

            List<Like> likes = likeRepository.findByPost(postOpt.get());
            Map<String, Object> response = new HashMap<>();
            response.put("postId", postId);
            response.put("totalLikes", likes.size());
            response.put("likes", likes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @DeleteMapping("/api/social/posts/{postId}/like")
    @ResponseBody
    public ResponseEntity<?> deleteLike(@PathVariable Long postId, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Optional<Post> postOpt = postRepository.findById(postId);

            if (postOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Пост не найден");
            }

            Optional<Like> likeOpt = likeRepository.findByUserAndPost(currentUser, postOpt.get());
            if (likeOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Лайк не найден");
            }

            likeRepository.delete(likeOpt.get());
            return ResponseEntity.ok("Лайк удален");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @PostMapping("/api/social/users/{userId}/follow")
    @ResponseBody
    public ResponseEntity<?> followUser(@PathVariable Long userId, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Optional<User> targetUser = userRepository.findById(userId);

            if (targetUser.isEmpty()) {
                return ResponseEntity.status(404).body("Пользователь не найден");
            }

            if (currentUser.getId().equals(userId)) {
                return ResponseEntity.badRequest().body("Нельзя подписаться на себя");
            }

            if (followRepository.existsByFollowerAndFollowing(currentUser, targetUser.get())) {
                return ResponseEntity.badRequest().body("Уже подписаны");
            }

            Follow follow = new Follow();
            follow.setFollower(currentUser);
            follow.setFollowing(targetUser.get());
            follow.setCreatedAt(LocalDateTime.now());
            followRepository.save(follow);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("message", "Подписка создана");
            response.put("followId", follow.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @GetMapping("/api/social/users/{userId}/followers")
    @ResponseBody
    public ResponseEntity<?> getUserFollowers(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Пользователь не найден");
            }

            List<Follow> followers = followRepository.findByFollowing(userOpt.get());
            List<Map<String, Object>> followerUsers = new ArrayList<>();

            for (Follow follow : followers) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", follow.getFollower().getId());
                userMap.put("username", follow.getFollower().getUsername());
                followerUsers.add(userMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalFollowers", followerUsers.size());
            response.put("followers", followerUsers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @GetMapping("/api/social/users/{userId}/following")
    @ResponseBody
    public ResponseEntity<?> getUserFollowing(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Пользователь не найден");
            }

            List<Follow> following = followRepository.findByFollower(userOpt.get());
            List<Map<String, Object>> followingUsers = new ArrayList<>();

            for (Follow follow : following) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", follow.getFollowing().getId());
                userMap.put("username", follow.getFollowing().getUsername());
                followingUsers.add(userMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalFollowing", followingUsers.size());
            response.put("following", followingUsers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    @DeleteMapping("/api/social/users/{userId}/follow")
    @ResponseBody
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Optional<User> targetUser = userRepository.findById(userId);

            if (targetUser.isEmpty()) {
                return ResponseEntity.status(404).body("Пользователь не найден");
            }

            Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(currentUser, targetUser.get());
            if (followOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Подписка не найдена");
            }

            followRepository.delete(followOpt.get());
            return ResponseEntity.ok("Подписка удалена");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка");
        }
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("No auth");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
