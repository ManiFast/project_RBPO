package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // ========== ВЕБ-СТРАНИЦА ==========

    @GetMapping("/social")
    public String socialPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        try {
            List<Post> posts = postRepository.findAll();
            if (posts == null) {
                posts = new ArrayList<>();
            }

            Map<Long, List<Comment>> commentsByPost = new HashMap<>();
            for (Post post : posts) {
                List<Comment> comments = commentRepository.findByPostId(post.getId());
                if (comments == null) {
                    comments = new ArrayList<>();
                }
                commentsByPost.put(post.getId(), comments);
            }

            // Добавляем ID текущего пользователя для форм
            String username = (String) session.getAttribute("username");
            Optional<User> currentUser = userRepository.findByUsername(username);
            if (currentUser.isPresent()) {
                model.addAttribute("currentUserId", currentUser.get().getId());
            }

            model.addAttribute("posts", posts);
            model.addAttribute("commentsByPost", commentsByPost);

            return "social";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("posts", new ArrayList<Post>());
            model.addAttribute("commentsByPost", new HashMap<Long, List<Comment>>());
            return "social";
        }
    }

    // ========== API ДЛЯ POSTMAN ==========

    // ===== POSTS =====

    @PostMapping("/api/social/posts")
    @ResponseBody
    public ResponseEntity<?> createPost(@RequestParam String content,
                                        HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = (String) session.getAttribute("username");
            Optional<User> author = userRepository.findByUsername(username);

            if (author.isEmpty()) {
                return ResponseEntity.badRequest().body("Author not found");
            }

            Post post = new Post();
            post.setContent(content);
            post.setAuthor(author.get());
            post.setCreatedAt(LocalDateTime.now());
            postRepository.save(post);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Post created successfully");
            response.put("postId", post.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/api/social/posts")
    @ResponseBody
    public ResponseEntity<?> getAllPosts() {
        try {
            List<Post> posts = postRepository.findAll();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/api/social/posts/{postId}")
    @ResponseBody
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        try {
            Optional<Post> post = postRepository.findById(postId);
            if (post.isPresent()) {
                return ResponseEntity.ok(post.get());
            } else {
                return ResponseEntity.status(404).body("Post not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/api/social/posts/{postId}")
    @ResponseBody
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestParam String content,
                                        HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = (String) session.getAttribute("username");
            Optional<User> currentUser = userRepository.findByUsername(username);
            Optional<Post> postOpt = postRepository.findById(postId);

            if (currentUser.isPresent() && postOpt.isPresent()) {
                Post post = postOpt.get();
                if (post.getAuthor().getId().equals(currentUser.get().getId())) {
                    post.setContent(content);
                    postRepository.save(post);
                    return ResponseEntity.ok("Post updated successfully");
                } else {
                    return ResponseEntity.status(403).body("Forbidden: You are not the author of this post");
                }
            }
            return ResponseEntity.status(404).body("Post or user not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/social/posts/{postId}")
    @ResponseBody
    public ResponseEntity<?> deletePost(@PathVariable Long postId,
                                        HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = (String) session.getAttribute("username");
            Optional<User> currentUser = userRepository.findByUsername(username);
            Optional<Post> postOpt = postRepository.findById(postId);

            if (currentUser.isPresent() && postOpt.isPresent()) {
                Post post = postOpt.get();
                if (post.getAuthor().getId().equals(currentUser.get().getId())) {
                    // Сначала удаляем лайки и комментарии
                    likeRepository.deleteByPost(post);
                    commentRepository.deleteByPost(post);
                    // Затем удаляем пост
                    postRepository.delete(post);
                    return ResponseEntity.ok("Post deleted successfully");
                } else {
                    return ResponseEntity.status(403).body("Forbidden: You are not the author of this post");
                }
            }
            return ResponseEntity.status(404).body("Post or user not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ===== COMMENTS =====

    @PostMapping("/api/social/comments")
    @ResponseBody
    public ResponseEntity<?> createComment(@RequestParam String content,
                                           @RequestParam Long postId,
                                           HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = (String) session.getAttribute("username");
            Optional<User> user = userRepository.findByUsername(username);
            Optional<Post> post = postRepository.findById(postId);

            if (user.isPresent() && post.isPresent()) {
                Comment comment = new Comment();
                comment.setContent(content);
                comment.setAuthor(user.get());
                comment.setPost(post.get());
                comment.setCreatedAt(LocalDateTime.now());
                commentRepository.save(comment);

                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Comment created successfully");
                response.put("commentId", comment.getId());
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(404).body("User or post not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/api/social/comments")
    @ResponseBody
    public ResponseEntity<?> getAllComments() {
        try {
            List<Comment> comments = commentRepository.findAll();
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/api/social/posts/{postId}/comments")
    @ResponseBody
    public ResponseEntity<?> getPostComments(@PathVariable Long postId) {
        try {
            List<Comment> comments = commentRepository.findByPostId(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ===== LIKES =====

    @PostMapping("/api/social/posts/{postId}/like")
    @ResponseBody
    public ResponseEntity<?> likePost(@PathVariable Long postId,
                                      HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = (String) session.getAttribute("username");
            Optional<User> user = userRepository.findByUsername(username);
            Optional<Post> post = postRepository.findById(postId);

            if (user.isPresent() && post.isPresent()) {
                if (likeRepository.existsByUserAndPost(user.get(), post.get())) {
                    return ResponseEntity.badRequest().body("Already liked");
                }

                Like like = new Like();
                like.setPost(post.get());
                like.setUser(user.get());
                like.setCreatedAt(LocalDateTime.now());
                likeRepository.save(like);

                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Post liked");
                response.put("likeId", like.getId());
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(404).body("User or post not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/api/social/posts/{postId}/likes")
    @ResponseBody
    public ResponseEntity<?> getPostLikes(@PathVariable Long postId) {
        try {
            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isPresent()) {
                List<Like> likes = likeRepository.findByPost(postOpt.get());
                int likeCount = likes.size();

                Map<String, Object> response = new HashMap<>();
                response.put("postId", postId);
                response.put("totalLikes", likeCount);
                response.put("likes", likes);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(404).body("Post not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/social/posts/{postId}/like")
    @ResponseBody
    public ResponseEntity<?> deleteLike(@PathVariable Long postId,
                                        HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = (String) session.getAttribute("username");
            Optional<User> user = userRepository.findByUsername(username);
            Optional<Post> post = postRepository.findById(postId);

            if (user.isPresent() && post.isPresent()) {
                Optional<Like> likeOpt = likeRepository.findByUserAndPost(user.get(), post.get());
                if (likeOpt.isPresent()) {
                    likeRepository.delete(likeOpt.get());
                    return ResponseEntity.ok("Like removed");
                } else {
                    return ResponseEntity.status(404).body("Like not found");
                }
            }
            return ResponseEntity.status(404).body("User or post not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ===== FOLLOWS =====

    @PostMapping("/api/social/users/{userId}/follow")
    @ResponseBody
    public ResponseEntity<?> followUser(@PathVariable Long userId,
                                        HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = (String) session.getAttribute("username");
            Optional<User> follower = userRepository.findByUsername(username);
            Optional<User> userToFollow = userRepository.findById(userId);

            if (follower.isPresent() && userToFollow.isPresent()) {
                if (followRepository.existsByFollowerAndFollowing(follower.get(), userToFollow.get())) {
                    return ResponseEntity.badRequest().body("Already following");
                }

                Follow follow = new Follow();
                follow.setFollower(follower.get());
                follow.setFollowing(userToFollow.get());
                follow.setCreatedAt(LocalDateTime.now());
                followRepository.save(follow);

                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "User followed");
                response.put("followId", follow.getId());
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/api/social/users/{userId}/followers")
    @ResponseBody
    public ResponseEntity<?> getUserFollowers(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
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
            }
            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/api/social/users/{userId}/following")
    @ResponseBody
    public ResponseEntity<?> getUserFollowing(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
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
            }
            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/social/users/{userId}/follow")
    @ResponseBody
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId,
                                          HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = (String) session.getAttribute("username");
            Optional<User> followerOpt = userRepository.findByUsername(username);
            Optional<User> userToUnfollowOpt = userRepository.findById(userId);

            if (followerOpt.isPresent() && userToUnfollowOpt.isPresent()) {
                Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(
                        followerOpt.get(), userToUnfollowOpt.get());

                if (followOpt.isPresent()) {
                    followRepository.delete(followOpt.get());
                    return ResponseEntity.ok("Unfollowed successfully");
                } else {
                    return ResponseEntity.status(404).body("Not following this user");
                }
            }
            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ===== Обновление комментария =====
    @PutMapping("/api/social/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                           @RequestParam String content,
                                           HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = (String) session.getAttribute("username");
            Optional<User> currentUser = userRepository.findByUsername(username);
            Optional<Comment> commentOpt = commentRepository.findById(commentId);

            if (currentUser.isPresent() && commentOpt.isPresent()) {
                Comment comment = commentOpt.get();
                // Проверяем, что пользователь - автор комментария
                if (comment.getAuthor().getId().equals(currentUser.get().getId())) {
                    comment.setContent(content);
                    commentRepository.save(comment);

                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Comment updated successfully");
                    response.put("commentId", comment.getId());
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(403).body("Forbidden: You are not the author of this comment");
                }
            }
            return ResponseEntity.status(404).body("Comment or user not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ===== Удаление комментария =====
    @DeleteMapping("/api/social/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId,
                                           HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String username = (String) session.getAttribute("username");
            Optional<User> currentUser = userRepository.findByUsername(username);
            Optional<Comment> commentOpt = commentRepository.findById(commentId);

            if (currentUser.isPresent() && commentOpt.isPresent()) {
                Comment comment = commentOpt.get();
                // Проверяем, что пользователь - автор комментария
                if (comment.getAuthor().getId().equals(currentUser.get().getId())) {
                    commentRepository.delete(comment);

                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Comment deleted successfully");
                    response.put("commentId", commentId);
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(403).body("Forbidden: You are not the author of this comment");
                }
            }
            return ResponseEntity.status(404).body("Comment or user not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}