package lontar.service;

import lontar.model.Post;
import lontar.model.PostStatus;
import lontar.model.User;
import lontar.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Page<Post> findPublished(Pageable pageable) {
        return postRepository.findByStatusOrderByPublishedAtDesc(PostStatus.PUBLISHED, pageable);
    }

    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<Post> findAllByAuthor(User author, Pageable pageable) {
        return postRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
    }

    public long countByStatus(PostStatus status) {
        return postRepository.countByStatus(status);
    }

    public long countAll() {
        return postRepository.count();
    }

    public Optional<Post> findBySlug(String slug) {
        return postRepository.findBySlug(slug);
    }

    public Optional<Post> findById(UUID id) {
        return postRepository.findById(id);
    }

    public Page<Post> findByTagSlug(String tagSlug, Pageable pageable) {
        return postRepository.findByTagSlugAndStatus(tagSlug, PostStatus.PUBLISHED, pageable);
    }

    public Page<Post> findByAuthor(User author, Pageable pageable) {
        return postRepository.findByAuthorAndStatusOrderByPublishedAtDesc(author, PostStatus.PUBLISHED, pageable);
    }

    public Page<Post> search(String query, Pageable pageable) {
        return postRepository.search(query, PostStatus.PUBLISHED, pageable);
    }

    public Post create(String title, String content, User author) {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setTitle(title);
        post.setSlug(generateSlug(title));
        post.setContent(content);
        post.setExcerpt(generateExcerpt(content));
        post.setStatus(PostStatus.DRAFT);
        post.setAuthor(author);
        return postRepository.save(post);
    }

    public Post update(Post post) {
        if (post.getContent() != null) {
            post.setExcerpt(generateExcerpt(post.getContent()));
        }
        return postRepository.save(post);
    }

    public void delete(UUID id) {
        postRepository.deleteById(id);
    }

    public Post publish(Post post) {
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public Post unpublish(Post post) {
        post.setStatus(PostStatus.DRAFT);
        post.setPublishedAt(null);
        return postRepository.save(post);
    }

    private String generateSlug(String title) {
        String base = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String slug = base;
        int counter = 2;
        while (postRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + counter;
            counter++;
        }
        return slug;
    }

    private String generateExcerpt(String content) {
        if (content == null) return "";
        String stripped = content.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
        return stripped.length() > 200 ? stripped.substring(0, 200) + "..." : stripped;
    }
}
