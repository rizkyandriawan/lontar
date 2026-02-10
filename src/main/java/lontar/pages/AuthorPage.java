package lontar.pages;

import candi.auth.core.annotation.Public;
import candi.runtime.Page;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import lontar.model.Post;
import lontar.model.User;
import lontar.service.PostService;
import lontar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

@Page(value = "/author/{id}", layout = "base")
@Public
@Template("""
<div class="max-w-3xl mx-auto px-6 pt-12 pb-8">
  {{ if author == null }}
    <div class="text-center py-16">
      <h1 class="text-2xl font-bold font-serif text-gray-900 mb-4">Author Not Found</h1>
      <a href="/" class="text-gray-900 underline">&larr; Back to home</a>
    </div>
  {{ end }}

  {{ if author != null }}
    <header class="mb-12 flex items-center gap-5">
      {{ if author.avatar != null }}
        <img src="{{ author.avatar }}" alt="{{ author.name }}" class="w-20 h-20 rounded-full">
      {{ end }}
      <div>
        <h1 class="text-3xl font-bold font-serif text-gray-900">{{ author.name }}</h1>
        {{ if author.bio != null }}
          <p class="mt-1 text-gray-500">{{ author.bio }}</p>
        {{ end }}
      </div>
    </header>

    {{ if posts.isEmpty() }}
      <div class="text-center py-16">
        <p class="text-gray-400 text-lg">No posts by this author yet.</p>
      </div>
    {{ end }}

    {{ for post in posts }}
      <article class="mb-10 pb-10 border-b border-gray-100 last:border-0">
        {{ if post.coverImage != null }}
          <a href="/post/{{ post.slug }}" class="block mb-4">
            <img src="{{ post.coverImage }}" alt="{{ post.title }}" class="w-full h-56 object-cover rounded-lg">
          </a>
        {{ end }}
        <h2 class="text-2xl font-bold font-serif mb-2">
          <a href="/post/{{ post.slug }}" class="text-gray-900 hover:text-gray-600 no-underline">{{ post.title }}</a>
        </h2>
        <div class="flex items-center gap-3 text-sm text-gray-400 mb-3">
          <time>{{ post.formattedDate }}</time>
        </div>
        {{ if post.excerpt != null }}
          <p class="text-gray-600 leading-relaxed">{{ post.excerpt }}</p>
        {{ end }}
      </article>
    {{ end }}

    {{ if hasNextPage || hasPrevPage }}
      <nav class="flex justify-center gap-2 mt-8">
        {{ if hasPrevPage }}
          <a href="/author/{{ author.id }}?page={{ prevPage }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">&larr; Newer</a>
        {{ end }}
        {{ if hasNextPage }}
          <a href="/author/{{ author.id }}?page={{ nextPage }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">Older &rarr;</a>
        {{ end }}
      </nav>
    {{ end }}
  {{ end }}
</div>
""")
public class AuthorPage {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private RequestContext ctx;

    private User author;
    private List<Post> posts;
    private Boolean hasPrevPage;
    private Boolean hasNextPage;
    private int prevPage;
    private int nextPage;

    public User getAuthor() { return author; }
    public List<Post> getPosts() { return posts; }
    public Boolean getHasPrevPage() { return hasPrevPage; }
    public Boolean getHasNextPage() { return hasNextPage; }
    public int getPrevPage() { return prevPage; }
    public int getNextPage() { return nextPage; }

    public void init() {
        String idStr = ctx.path("id");
        try {
            UUID id = UUID.fromString(idStr);
            author = userService.findById(id).orElse(null);
        } catch (IllegalArgumentException e) {
            author = null;
        }
        if (author != null) {
            String pageParam = ctx.query("page");
            int currentPage = 0;
            if (pageParam != null) {
                try {
                    currentPage = Math.max(0, Integer.parseInt(pageParam) - 1);
                } catch (NumberFormatException ignored) {}
            }
            org.springframework.data.domain.Page<Post> page = postService.findByAuthor(author, PageRequest.of(currentPage, 10));
            posts = page.getContent();
            hasPrevPage = currentPage > 0;
            hasNextPage = currentPage < page.getTotalPages() - 1;
            prevPage = currentPage; // 1-based for display
            nextPage = currentPage + 2; // 1-based for display
        }
    }
}
