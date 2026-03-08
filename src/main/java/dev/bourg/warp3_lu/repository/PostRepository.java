package dev.bourg.warp3_lu.repository;

import dev.bourg.warp3_lu.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {

    Optional<Post> findBySlug(String slug);

    List<Post> findByStatus(Post.Status status);

    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY COALESCE(p.publishedAt, p.createdAt) DESC")
    List<Post> findByStatusOrderByPublishedAtDesc(Post.Status status);

    List<Post> findByAuthorId(Long authorId);

    boolean existsBySlug(String slug);

}