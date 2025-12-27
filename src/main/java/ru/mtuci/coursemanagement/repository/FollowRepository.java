package ru.mtuci.coursemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.coursemanagement.model.Follow;
import ru.mtuci.coursemanagement.model.User;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    List<Follow> findByFollower(User follower); // Новый метод (подписки пользователя)
    List<Follow> findByFollowing(User following); // Новый метод (подписчики пользователя)
    boolean existsByFollowerAndFollowing(User follower, User following);
}