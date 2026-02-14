package lontar.pages;

import candi.auth.core.annotation.Public;
import candi.runtime.Page;
import candi.runtime.RequestParam;
import candi.runtime.Template;
import candi.web.seo.Seo;
import lombok.Getter;
import lombok.Setter;
import lontar.model.Post;
import lontar.service.PostService;
import lontar.service.SiteSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@Page(value = "/", layout = "base")
@Public
@Seo(title = "Home")
@Template("""
<div class="max-w-3xl mx-auto px-6 pt-12 pb-8">
  <header class="mb-12">
    <h1 class="text-4xl font-bold font-serif text-gray-900">{{ siteDescription }}</h1>
  </header>

  {{ if posts.isEmpty() }}
    <div class="text-center py-16">
      <p class="text-gray-400 text-lg">No posts published yet.</p>
    </div>
  {{ end }}

  {{ for post in posts }}
    <article class="mb-10 pb-10 border-b border-gray-100 last:border-0">
      {{ if post.coverImage != null }}
        <a href="/post/{{ post.slug }}" class="block mb-4">
          <img src="{{ post.coverImage }}" alt="{{ post.title }}"
               class="w-full h-56 object-cover rounded-lg">
        </a>
      {{ end }}
      <h2 class="text-2xl font-bold font-serif mb-2">
        <a href="/post/{{ post.slug }}" class="text-gray-900 hover:text-gray-600 no-underline">{{ post.title }}</a>
      </h2>
      <div class="flex items-center gap-3 text-sm text-gray-400 mb-3">
        {{ if post.author != null }}
          <a href="/author/{{ post.author.id }}" class="text-gray-500 hover:text-gray-900 no-underline">{{ post.author.name }}</a>
          <span>&middot;</span>
        {{ end }}
        <time>{{ post.formattedDate }}</time>
      </div>
      {{ if post.excerpt != null }}
        <p class="text-gray-600 leading-relaxed">{{ post.excerpt }}</p>
      {{ end }}
      {{ if post.tags.isEmpty() == false }}
        <div class="flex flex-wrap gap-2 mt-3">
          {{ for tag in post.tags }}
            <a href="/tag/{{ tag.slug }}" class="text-xs px-2.5 py-1 bg-gray-100 text-gray-600 rounded-full hover:bg-gray-200 no-underline">{{ tag.name }}</a>
          {{ end }}
        </div>
      {{ end }}
    </article>
  {{ end }}

  {{ if hasNextPage || hasPrevPage }}
    <nav class="flex justify-center gap-2 mt-8">
      {{ if hasPrevPage }}
        <a href="/?page={{ prevPage }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">&larr; Newer</a>
      {{ end }}
      {{ if hasNextPage }}
        <a href="/?page={{ nextPage }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">Older &rarr;</a>
      {{ end }}
    </nav>
  {{ end }}
</div>
""")
@Getter
public class IndexPage {

    @Autowired
    private PostService postService;

    @Autowired
    private SiteSettingsService settingsService;

    @Setter @RequestParam(name = "page", defaultValue = "1")
    private int pageParam;

    private java.util.List<Post> posts;
    private Boolean hasPrevPage;
    private Boolean hasNextPage;
    private int prevPage;
    private int nextPage;
    private String siteDescription;

    public void init() {
        siteDescription = settingsService.getSettings().getDescription();
        int currentPage = Math.max(0, pageParam - 1);
        int perPage = settingsService.getSettings().getPostsPerPage();
        org.springframework.data.domain.Page<Post> page = postService.findPublished(PageRequest.of(currentPage, perPage));
        posts = page.getContent();
        hasPrevPage = currentPage > 0;
        hasNextPage = currentPage < page.getTotalPages() - 1;
        prevPage = currentPage;
        nextPage = currentPage + 2;
    }
}
