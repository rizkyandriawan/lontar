package lontar.repository;

import lontar.model.Comment;
import lontar.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    List<Comment> findByPostAndApprovedTrueOrderByCreatedAtDesc(Post post);
}
