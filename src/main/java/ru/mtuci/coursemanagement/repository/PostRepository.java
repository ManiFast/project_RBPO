package ru.mtuci.coursemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.coursemanagement.model.Post;
import ru.mtuci.coursemanagement.model.User;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthor(User author);
    List<Post> findByAuthorId(Long authorId);
}