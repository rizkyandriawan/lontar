package lontar.repository;

import lontar.model.Post;
import lontar.model.PostStatus;
import lontar.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Optional<Post> findBySlug(String slug);
    Page<Post> findByStatusOrderByPublishedAtDesc(PostStatus status, Pageable pageable);
    Page<Post> findByAuthorAndStatusOrderByPublishedAtDesc(User author, PostStatus status, Pageable pageable);

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);
    long countByStatus(PostStatus status);

    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.slug = :tagSlug AND p.status = :status ORDER BY p.publishedAt DESC")
    Page<Post> findByTagSlugAndStatus(@Param("tagSlug") String tagSlug, @Param("status") PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Post> search(@Param("q") String query, @Param("status") PostStatus status, Pageable pageable);
}
