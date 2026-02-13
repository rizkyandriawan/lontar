package lontar.pages;

import candi.auth.core.annotation.Public;
import candi.runtime.Page;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import lombok.Getter;
import lontar.model.Post;
import lontar.model.Tag;
import lontar.repository.TagRepository;
import lontar.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Page(value = "/tag/{slug}", layout = "base")
@Public
@Template("""
<div class="max-w-3xl mx-auto px-6 pt-12 pb-8">
  {{ if tag == null }}
    <div class="text-center py-16">
      <h1 class="text-2xl font-bold font-serif text-gray-900 mb-4">Tag Not Found</h1>
      <a href="/" class="text-gray-900 underline">&larr; Back to home</a>
    </div>
  {{ else }}
    <header class="mb-12">
      <p class="text-sm font-medium text-gray-400 uppercase tracking-wider mb-2">Tag</p>
      <h1 class="text-3xl font-bold font-serif text-gray-900">{{ tag.name }}</h1>
      {{ if tag.description != null }}
        <p class="mt-2 text-gray-500">{{ tag.description }}</p>
      {{ end }}
    </header>

    {{ if posts.isEmpty() }}
      <div class="text-center py-16">
        <p class="text-gray-400 text-lg">No posts with this tag yet.</p>
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
          {{ if post.author != null }}
            <a href="/author/{{ post.author.id }}" class="text-gray-500 hover:text-gray-900 no-underline">{{ post.author.name }}</a>
            <span>&middot;</span>
          {{ end }}
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
          <a href="/tag/{{ tag.slug }}?page={{ prevPage }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">&larr; Newer</a>
        {{ end }}
        {{ if hasNextPage }}
          <a href="/tag/{{ tag.slug }}?page={{ nextPage }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">Older &rarr;</a>
        {{ end }}
      </nav>
    {{ end }}
  {{ end }}
</div>
""")
@Getter
public class TagPage {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private RequestContext ctx;

    private Tag tag;
    private List<Post> posts;
    private Boolean hasPrevPage;
    private Boolean hasNextPage;
    private int prevPage;
    private int nextPage;

    public void init() {
        String slug = ctx.path("slug");
        tag = tagRepository.findBySlug(slug).orElse(null);
        if (tag != null) {
            String pageParam = ctx.query("page");
            int currentPage = 0;
            if (pageParam != null) {
                try {
                    currentPage = Math.max(0, Integer.parseInt(pageParam) - 1);
                } catch (NumberFormatException ignored) {}
            }
            org.springframework.data.domain.Page<Post> page = postService.findByTagSlug(slug, PageRequest.of(currentPage, 10));
            posts = page.getContent();
            hasPrevPage = currentPage > 0;
            hasNextPage = currentPage < page.getTotalPages() - 1;
            prevPage = currentPage;
            nextPage = currentPage + 2;
        }
    }
}
