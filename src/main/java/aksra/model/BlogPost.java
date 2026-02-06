package aksra.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BlogPost {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private String id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private boolean published;

    public BlogPost() {}

    public BlogPost(String id, String title, String content, String author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.published = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean getPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public String getFormattedDate() {
        return createdAt != null ? createdAt.format(FMT) : "";
    }

    public String getExcerpt() {
        if (content == null) return "";
        return content.length() > 200 ? content.substring(0, 200) + "..." : content;
    }
}
