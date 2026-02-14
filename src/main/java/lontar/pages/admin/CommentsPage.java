package lontar.pages.admin;

import candi.auth.core.annotation.Protected;
import candi.runtime.ActionResult;
import candi.runtime.Page;
import candi.runtime.Post;
import candi.runtime.RequestContext;
import candi.runtime.RequestParam;
import candi.runtime.Template;
import lombok.Getter;
import lombok.Setter;
import lontar.model.Comment;
import lontar.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.List;
import java.util.UUID;

@Protected
@Page(value = "/admin/comments", layout = "admin")
@Template("""
<div>
  <h1 class="text-2xl font-bold text-gray-900 mb-6">Comments</h1>

  {{ if success != null }}
    <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">{{ success }}</div>
  {{ end }}

  <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
    {{ if comments.isEmpty() }}
      <div class="text-center py-12">
        <p class="text-gray-400">No comments yet.</p>
      </div>
    {{ else }}
      <div class="divide-y divide-gray-100">
        {{ for comment in comments }}
          <div class="p-6">
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-2">
                  <span class="font-medium text-sm text-gray-900">{{ comment.authorName }}</span>
                  {{ if comment.authorEmail != null }}
                    <span class="text-xs text-gray-400">{{ comment.authorEmail }}</span>
                  {{ end }}
                  <span class="text-xs text-gray-400">{{ comment.formattedDate }}</span>
                  {{ if comment.approved }}
                    <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-700">Approved</span>
                  {{ else }}
                    <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-700">Pending</span>
                  {{ end }}
                </div>
                <p class="text-sm text-gray-600 mb-2">{{ comment.content }}</p>
                <a href="/post/{{ comment.post.slug }}" class="text-xs text-gray-400 hover:text-gray-600 no-underline">on: {{ comment.post.title }}</a>
              </div>
              <div class="flex items-center gap-2 ml-4">
                {{ if comment.approved }}
                  <form method="POST" class="inline">
                    <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
                    <input type="hidden" name="action" value="unapprove">
                    <input type="hidden" name="commentId" value="{{ comment.id }}">
                    <button type="submit" class="text-xs px-3 py-1.5 bg-yellow-50 text-yellow-700 rounded-lg hover:bg-yellow-100 transition">Unapprove</button>
                  </form>
                {{ else }}
                  <form method="POST" class="inline">
                    <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
                    <input type="hidden" name="action" value="approve">
                    <input type="hidden" name="commentId" value="{{ comment.id }}">
                    <button type="submit" class="text-xs px-3 py-1.5 bg-green-50 text-green-700 rounded-lg hover:bg-green-100 transition">Approve</button>
                  </form>
                {{ end }}
                <form method="POST" class="inline" onsubmit="return confirm('Delete this comment?')">
                  <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="commentId" value="{{ comment.id }}">
                  <button type="submit" class="text-xs px-3 py-1.5 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition">Delete</button>
                </form>
              </div>
            </div>
          </div>
        {{ end }}
      </div>
    {{ end }}
  </div>

  {{ if hasNextPage || hasPrevPage }}
    <nav class="flex justify-center gap-2 mt-6">
      {{ if hasPrevPage }}
        <a href="/admin/comments?page={{ prevPage }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">&larr; Previous</a>
      {{ end }}
      {{ if hasNextPage }}
        <a href="/admin/comments?page={{ nextPage }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">Next &rarr;</a>
      {{ end }}
    </nav>
  {{ end }}
</div>
""")
@Getter
public class CommentsPage {

    @Autowired
    private CommentService commentService;

    @Autowired
    private RequestContext ctx;

    @Setter @RequestParam(name = "page", defaultValue = "1")
    private int pageParam;

    private List<Comment> comments;
    private String success;
    private Boolean hasPrevPage;
    private Boolean hasNextPage;
    private int prevPage;
    private int nextPage;
    private String csrfParameterName;
    private String csrfTokenValue;

    public void init() {
        CsrfToken csrf = (CsrfToken) ctx.raw().getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            csrfParameterName = csrf.getParameterName();
            csrfTokenValue = csrf.getToken();
        }

        int currentPage = Math.max(0, pageParam - 1);

        org.springframework.data.domain.Page<Comment> page = commentService.findAll(PageRequest.of(currentPage, 20));
        comments = page.getContent();
        hasPrevPage = currentPage > 0;
        hasNextPage = currentPage < page.getTotalPages() - 1;
        prevPage = currentPage;
        nextPage = currentPage + 2;
    }

    @Post
    public ActionResult handleAction() {
        String action = ctx.form("action");
        String commentId = ctx.form("commentId");

        if (commentId == null) return ActionResult.redirect("/admin/comments");

        try {
            UUID id = UUID.fromString(commentId);
            if ("approve".equals(action)) {
                commentService.approve(id);
            } else if ("unapprove".equals(action)) {
                commentService.unapprove(id);
            } else if ("delete".equals(action)) {
                commentService.delete(id);
            }
        } catch (Exception ignored) {}

        return ActionResult.redirect("/admin/comments");
    }
}
