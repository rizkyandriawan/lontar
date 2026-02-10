package lontar.service;

import lontar.model.Comment;
import lontar.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Page<Comment> findAll(Pageable pageable) {
        return commentRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Optional<Comment> findById(UUID id) {
        return commentRepository.findById(id);
    }

    public void approve(UUID id) {
        commentRepository.findById(id).ifPresent(comment -> {
            comment.setApproved(true);
            commentRepository.save(comment);
        });
    }

    public void unapprove(UUID id) {
        commentRepository.findById(id).ifPresent(comment -> {
            comment.setApproved(false);
            commentRepository.save(comment);
        });
    }

    public void delete(UUID id) {
        commentRepository.deleteById(id);
    }

    public long countAll() {
        return commentRepository.count();
    }
}
