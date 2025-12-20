package ru.mtuci.coursemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.model.*;
import ru.mtuci.coursemanagement.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/social/operations")
@RequiredArgsConstructor
@Transactional
public class SocialBusinessController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;

    // 1️⃣ Лента подписок (Post + Follow + User)
    @GetMapping("/feed/{userId}")
    public ResponseEntity<?> getFeed(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        List<Follow> subscriptions = followRepository.findByFollower(userOpt.get());
        List<Long> followingIds = subscriptions.stream()
                .map(f -> f.getFollowing().getId())
                .toList();

        List<Post> feedPosts = postRepository.findAll().stream()
                .filter(p -> followingIds.contains(p.getAuthor().getId()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("feedPosts", feedPosts);
        return ResponseEntity.ok(response);
    }

    // 2️⃣ Аналитика поста (Post + Like + Comment)
    @GetMapping("/post/{postId}/analytics")
    public ResponseEntity<?> getPostAnalytics(@PathVariable Long postId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) return ResponseEntity.notFound().build();

        Post post = postOpt.get();
        List<Like> likes = likeRepository.findByPost(post);
        List<Comment> comments = commentRepository.findByPostId(postId);

        Map<String, Object> response = new HashMap<>();
        response.put("postId", postId);
        response.put("author", post.getAuthor().getUsername());
        response.put("likesCount", likes.size());
        response.put("commentsCount", comments.size());
        response.put("likes", likes);
        response.put("comments", comments);

        return ResponseEntity.ok(response);
    }

    // 3️⃣ Кого подписать? (Follow + User) - рекомендации
    @GetMapping("/user/{userId}/recommendations")
    public ResponseEntity<?> getFollowRecommendations(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        // Находим, на кого подписан пользователь
        List<Follow> userSubscriptions = followRepository.findByFollower(userOpt.get());
        Set<Long> userFollowingIds = userSubscriptions.stream()
                .map(f -> f.getFollowing().getId())
                .collect(Collectors.toSet());

        // Находим пользователей, на которых подписаны те, на кого подписан наш пользователь
        Set<User> recommendations = new HashSet<>();
        for (Follow sub : userSubscriptions) {
            List<Follow> theirSubscriptions = followRepository.findByFollower(sub.getFollowing());
            for (Follow theirSub : theirSubscriptions) {
                User recommendedUser = theirSub.getFollowing();
                // Не рекомендовать самого себя и тех, на кого уже подписан
                if (!recommendedUser.getId().equals(userId) && !userFollowingIds.contains(recommendedUser.getId())) {
                    recommendations.add(recommendedUser);
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("recommendations", recommendations.stream().map(u -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", u.getId());
            userMap.put("username", u.getUsername());
            return userMap;
        }).toList());

        return ResponseEntity.ok(response);
    }

    // 4️⃣ Поиск по контенту (Post + Comment)
    @GetMapping("/search/content")
    public ResponseEntity<?> searchContent(@RequestParam String keyword) {
        List<Post> posts = postRepository.findAll().stream()
                .filter(p -> p.getContent().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        List<Comment> comments = commentRepository.findAll().stream()
                .filter(c -> c.getContent().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("keyword", keyword);
        response.put("postsFound", posts.size());
        response.put("commentsFound", comments.size());
        response.put("posts", posts);
        response.put("comments", comments);

        return ResponseEntity.ok(response);
    }

    // 5️⃣ Деактивация пользователя (ВСЕ сущности)
    @DeleteMapping("/user/{userId}/deactivate")
    @Transactional
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();

        // Удаляем лайки пользователя
        List<Like> userLikes = likeRepository.findAll().stream()
                .filter(l -> l.getUser().getId().equals(userId))
                .toList();
        likeRepository.deleteAll(userLikes);

        // Удаляем комментарии пользователя
        List<Comment> userComments = commentRepository.findAll().stream()
                .filter(c -> c.getAuthor().getId().equals(userId))
                .toList();
        commentRepository.deleteAll(userComments);

        // Удаляем посты пользователя (лайки и комментарии к ним удалятся каскадом)
        List<Post> userPosts = postRepository.findByAuthor(user);
        postRepository.deleteAll(userPosts);

        // Удаляем подписки пользователя
        List<Follow> userFollows = followRepository.findByFollower(user);
        followRepository.deleteAll(userFollows);

        // Удаляем подписчиков пользователя
        List<Follow> userFollowers = followRepository.findByFollowing(user);
        followRepository.deleteAll(userFollowers);

        // Наконец, удаляем самого пользователя
        userRepository.delete(user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User deactivated and all related data deleted");
        response.put("userId", userId);

        return ResponseEntity.ok(response);
    }
}