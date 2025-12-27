package ru.mtuci.coursemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.coursemanagement.model.Comment;
import ru.mtuci.coursemanagement.model.Post;
import java.util.Optional;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);
    List<Comment> findByPostId(Long postId);
    void deleteByPost(Post post);
//    Optional<Comment> findById(Long id); // иджетот import java.util.Optional;
}