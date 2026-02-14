package lontar.pages;

import candi.auth.core.annotation.Public;
import candi.runtime.ActionResult;
import candi.runtime.Page;
import candi.runtime.PathVariable;
import candi.runtime.RequestContext;
import candi.runtime.RequestParam;
import candi.runtime.Template;
import candi.web.seo.Seo;
import candi.web.seo.SeoField;
import candi.web.seo.SeoRole;
import lombok.Getter;
import lombok.Setter;
import lontar.model.Comment;
import lontar.model.Post;
import lontar.repository.CommentRepository;
import lontar.service.PostService;
import lontar.service.SiteSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.List;

@Page(value = "/post/{slug}", layout = "base")
@Public
@Seo(type = "article")
@Template("""
{{ if post == null }}
  <div class="max-w-3xl mx-auto px-6 pt-16 text-center">
    <h1 class="text-2xl font-bold font-serif text-gray-900 mb-4">Post Not Found</h1>
    <p class="text-gray-500 mb-6">The post you're looking for doesn't exist.</p>
    <a href="/" class="text-gray-900 underline">&larr; Back to home</a>
  </div>
{{ else }}
  <article class="max-w-3xl mx-auto px-6 pt-12">
    {{ if post.coverImage != null }}
      <img src="{{ post.coverImage }}" alt="{{ post.title }}"
           class="w-full h-72 object-cover rounded-lg mb-8">
    {{ end }}

    <header class="mb-8">
      <h1 class="text-4xl font-bold font-serif text-gray-900 leading-tight mb-4">{{ post.title }}</h1>
      <div class="flex items-center gap-4">
        {{ if post.author != null }}
          <a href="/author/{{ post.author.id }}" class="flex items-center gap-3 no-underline">
            {{ if post.author.avatar != null }}
              <img src="{{ post.author.avatar }}" alt="{{ post.author.name }}" class="w-10 h-10 rounded-full">
            {{ end }}
            <div>
              <div class="text-sm font-medium text-gray-900">{{ post.author.name }}</div>
              <div class="text-sm text-gray-400">{{ post.formattedDate }}</div>
            </div>
          </a>
        {{ end }}
      </div>
      {{ if post.tags.isEmpty() == false }}
        <div class="flex flex-wrap gap-2 mt-4">
          {{ for tag in post.tags }}
            <a href="/tag/{{ tag.slug }}" class="text-xs px-2.5 py-1 bg-gray-100 text-gray-600 rounded-full hover:bg-gray-200 no-underline">{{ tag.name }}</a>
          {{ end }}
        </div>
      {{ end }}
    </header>

    <div class="prose prose-lg prose-gray max-w-none font-serif
                prose-headings:font-sans prose-headings:font-bold
                prose-a:text-gray-900 prose-img:rounded-lg">
      {{ raw post.content }}
    </div>
  </article>

  {{ if allowComments }}
    <section class="max-w-3xl mx-auto px-6 mt-16 pt-8 border-t border-gray-100">
      <h2 class="text-xl font-bold font-serif mb-6">Comments ({{ comments.size() }})</h2>

      {{ if commentSuccess }}
        <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">Your comment has been posted.</div>
      {{ end }}

      <form method="POST" class="mb-10 space-y-4">
        <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label for="authorName" class="block text-sm font-medium text-gray-700 mb-1">Name</label>
            <input type="text" id="authorName" name="authorName" required
                   class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
          </div>
          <div>
            <label for="authorEmail" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input type="email" id="authorEmail" name="authorEmail"
                   class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
          </div>
        </div>
        <div>
          <label for="commentContent" class="block text-sm font-medium text-gray-700 mb-1">Comment</label>
          <textarea id="commentContent" name="commentContent" rows="4" required
                    class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition resize-y"></textarea>
        </div>
        <button type="submit" class="px-6 py-2.5 bg-gray-900 text-white rounded-lg font-medium text-sm hover:bg-gray-800 transition">
          Post Comment
        </button>
      </form>

      {{ for comment in comments }}
        <div class="mb-6 pb-6 border-b border-gray-50 last:border-0">
          <div class="flex items-center gap-2 mb-2">
            <span class="font-medium text-sm text-gray-900">{{ comment.authorName }}</span>
            <span class="text-gray-300">&middot;</span>
            <span class="text-xs text-gray-400">{{ comment.formattedDate }}</span>
          </div>
          <p class="text-gray-600 text-sm leading-relaxed">{{ comment.content }}</p>
        </div>
      {{ end }}
    </section>
  {{ end }}
{{ end }}
""")
@Getter
public class PostPage {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private SiteSettingsService settingsService;

    @Autowired
    private RequestContext ctx;

    @Setter @PathVariable
    private String slug;

    @Setter @RequestParam(name = "commented", required = false)
    private String commented;

    private Post post;
    private List<Comment> comments;
    private Boolean allowComments;
    private Boolean commentSuccess;
    private String csrfParameterName;
    private String csrfTokenValue;

    @SeoField(SeoRole.TITLE)
    private String title;

    @SeoField(SeoRole.DESCRIPTION)
    private String excerpt;

    @SeoField(SeoRole.IMAGE)
    private String coverImage;

    public void init() {
        CsrfToken csrf = (CsrfToken) ctx.raw().getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            csrfParameterName = csrf.getParameterName();
            csrfTokenValue = csrf.getToken();
        }

        post = postService.findBySlug(slug).orElse(null);
        if (post != null) {
            title = post.getTitle();
            excerpt = post.getExcerpt();
            coverImage = post.getCoverImage();
            allowComments = settingsService.getSettings().isAllowComments();
            comments = commentRepository.findByPostAndApprovedTrueOrderByCreatedAtDesc(post);
        }

        if ("true".equals(commented)) {
            commentSuccess = true;
        }
    }

    @candi.runtime.Post
    public ActionResult addComment() {
        if (post == null) return ActionResult.redirect("/");

        String authorName = ctx.form("authorName");
        String content = ctx.form("commentContent");

        if (authorName == null || authorName.isBlank() || content == null || content.isBlank()) {
            return ActionResult.render();
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthorName(authorName.trim());
        comment.setAuthorEmail(ctx.form("authorEmail"));
        comment.setContent(content.trim());
        commentRepository.save(comment);

        return ActionResult.redirect("/post/" + post.getSlug() + "?commented=true");
    }
}
