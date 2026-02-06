package aksra.service;

import aksra.model.BlogPost;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BlogService {

    private final Map<String, BlogPost> posts = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public BlogService() {
        // Seed with sample posts
        create("Welcome to Aksra",
                "Aksra is a simple blog engine built with the Candi framework. " +
                "Candi is an HTML-first web framework on Spring Boot that compiles " +
                ".jhtml templates into Java bytecode for maximum performance.",
                "Admin");

        create("Getting Started with Candi",
                "Candi uses a unique file format: Java class on top, template block on the bottom. " +
                "Annotations like @Page, @Layout, and @Widget determine what type of component you're building. " +
                "The template section uses Go-style {{ }} expressions for dynamic content.",
                "Admin");

        create("Template Syntax Guide",
                "Candi templates support conditionals ({{ if }}), loops ({{ for item in items }}), " +
                "property access ({{ post.title }}), null-safe navigation ({{ post?.title }}), " +
                "widgets ({{ widget \"name\" }}), and includes ({{ include \"partial\" }}). " +
                "All output is HTML-escaped by default. Use {{ raw expr }} for unescaped output.",
                "Admin");
    }

    public List<BlogPost> findAll() {
        return posts.values().stream()
                .sorted(Comparator.comparing(BlogPost::getCreatedAt).reversed())
                .toList();
    }

    public BlogPost findById(String id) {
        return posts.get(id);
    }

    public BlogPost create(String title, String content, String author) {
        String id = String.valueOf(idCounter.incrementAndGet());
        BlogPost post = new BlogPost(id, title, content, author);
        posts.put(id, post);
        return post;
    }

    public void update(String id, String title, String content) {
        BlogPost post = posts.get(id);
        if (post != null) {
            post.setTitle(title);
            post.setContent(content);
        }
    }

    public void delete(String id) {
        posts.remove(id);
    }

    public int count() {
        return posts.size();
    }
}
