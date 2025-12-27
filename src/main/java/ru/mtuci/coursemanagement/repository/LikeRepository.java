package ru.mtuci.coursemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.coursemanagement.model.Like;
import ru.mtuci.coursemanagement.model.Post;
import ru.mtuci.coursemanagement.model.User;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndPost(User user, Post post);
    int countByPost(Post post);
    boolean existsByUserAndPost(User user, Post post);
    List<Like> findByPost(Post post);
    void deleteByPost(Post post); // Добавьте этот метод
}