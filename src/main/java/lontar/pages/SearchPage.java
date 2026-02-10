package lontar.pages;

import candi.auth.core.annotation.Public;
import candi.runtime.Page;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import candi.web.seo.Seo;
import lontar.model.Post;
import lontar.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Page(value = "/search", layout = "base")
@Public
@Seo(noindex = true)
@Template("""
<div class="max-w-3xl mx-auto px-6 pt-12 pb-8">
  <header class="mb-8">
    <h1 class="text-3xl font-bold font-serif text-gray-900 mb-4">Search</h1>
    <form method="GET" action="/search" class="flex gap-3">
      <input type="text" name="q" value="{{ query }}" placeholder="Search posts..."
             class="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
      <button type="submit" class="px-6 py-2.5 bg-gray-900 text-white rounded-lg font-medium text-sm hover:bg-gray-800 transition">
        Search
      </button>
    </form>
  </header>

  {{ if query != null }}
    {{ if posts.isEmpty() }}
      <div class="text-center py-12">
        <p class="text-gray-400 text-lg">No results found for "{{ query }}".</p>
      </div>
    {{ end }}

    {{ for post in posts }}
      <article class="mb-8 pb-8 border-b border-gray-100 last:border-0">
        <h2 class="text-xl font-bold font-serif mb-1">
          <a href="/post/{{ post.slug }}" class="text-gray-900 hover:text-gray-600 no-underline">{{ post.title }}</a>
        </h2>
        <div class="flex items-center gap-3 text-sm text-gray-400 mb-2">
          {{ if post.author != null }}
            <span>{{ post.author.name }}</span>
            <span>&middot;</span>
          {{ end }}
          <time>{{ post.formattedDate }}</time>
        </div>
        {{ if post.excerpt != null }}
          <p class="text-gray-600 text-sm leading-relaxed">{{ post.excerpt }}</p>
        {{ end }}
      </article>
    {{ end }}

    {{ if hasNextPage || hasPrevPage }}
      <nav class="flex justify-center gap-2 mt-8">
        {{ if hasPrevPage }}
          <a href="/search?q={{ query }}&page={{ prevPage }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">&larr; Prev</a>
        {{ end }}
        {{ if hasNextPage }}
          <a href="/search?q={{ query }}&page={{ nextPage }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">Next &rarr;</a>
        {{ end }}
      </nav>
    {{ end }}
  {{ end }}
</div>
""")
public class SearchPage {

    @Autowired
    private PostService postService;

    @Autowired
    private RequestContext ctx;

    private String query;
    private List<Post> posts;
    private Boolean hasPrevPage;
    private Boolean hasNextPage;
    private int prevPage;
    private int nextPage;

    public String getQuery() { return query; }
    public List<Post> getPosts() { return posts; }
    public Boolean getHasPrevPage() { return hasPrevPage; }
    public Boolean getHasNextPage() { return hasNextPage; }
    public int getPrevPage() { return prevPage; }
    public int getNextPage() { return nextPage; }

    public void init() {
        query = ctx.query("q");
        if (query != null && !query.isBlank()) {
            String pageParam = ctx.query("page");
            int currentPage = 0;
            if (pageParam != null) {
                try {
                    currentPage = Math.max(0, Integer.parseInt(pageParam) - 1);
                } catch (NumberFormatException ignored) {}
            }
            org.springframework.data.domain.Page<Post> page = postService.search(query.trim(), PageRequest.of(currentPage, 10));
            posts = page.getContent();
            hasPrevPage = currentPage > 0;
            hasNextPage = currentPage < page.getTotalPages() - 1;
            prevPage = currentPage;
            nextPage = currentPage + 2;
        } else {
            posts = List.of();
        }
    }
}
