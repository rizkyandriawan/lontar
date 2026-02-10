package lontar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "site_settings")
public class SiteSettings {

    @Id
    private Long id = 1L;

    private String title = "Lontar";

    private String description = "A modern blog engine";

    private String logo;

    private String favicon;

    private int postsPerPage = 10;

    private boolean allowComments = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getFavicon() { return favicon; }
    public void setFavicon(String favicon) { this.favicon = favicon; }

    public int getPostsPerPage() { return postsPerPage; }
    public void setPostsPerPage(int postsPerPage) { this.postsPerPage = postsPerPage; }

    public boolean isAllowComments() { return allowComments; }
    public void setAllowComments(boolean allowComments) { this.allowComments = allowComments; }
}
