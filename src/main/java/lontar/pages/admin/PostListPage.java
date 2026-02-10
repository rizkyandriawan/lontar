package lontar.pages.admin;

import candi.auth.core.annotation.Protected;
import candi.runtime.Page;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import lontar.model.Role;
import lontar.model.User;
import lontar.service.PostService;
import lontar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Protected
@Page(value = "/admin/posts", layout = "admin")
@Template("""
<div>
  <div class="flex items-center justify-between mb-6">
    <h1 class="text-2xl font-bold text-gray-900">Posts</h1>
    <a href="/admin/editor" class="inline-flex items-center gap-2 px-4 py-2 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 no-underline">
      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
      New Post
    </a>
  </div>

  <!-- Status filter -->
  <div class="flex gap-2 mb-6">
    <a href="/admin/posts" class="px-3 py-1.5 text-sm rounded-lg no-underline {{ if filterAll }}bg-gray-900 text-white{{ end }}{{ if filterAll == false }}bg-white text-gray-600 border border-gray-200 hover:bg-gray-50{{ end }}">All</a>
    <a href="/admin/posts?status=published" class="px-3 py-1.5 text-sm rounded-lg no-underline {{ if filterPublished }}bg-gray-900 text-white{{ end }}{{ if filterPublished == false }}bg-white text-gray-600 border border-gray-200 hover:bg-gray-50{{ end }}">Published</a>
    <a href="/admin/posts?status=draft" class="px-3 py-1.5 text-sm rounded-lg no-underline {{ if filterDraft }}bg-gray-900 text-white{{ end }}{{ if filterDraft == false }}bg-white text-gray-600 border border-gray-200 hover:bg-gray-50{{ end }}">Drafts</a>
  </div>

  {{ if success != null }}
    <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">{{ success }}</div>
  {{ end }}

  <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
    {{ if posts.isEmpty() }}
      <div class="text-center py-12">
        <p class="text-gray-400">No posts found.</p>
      </div>
    {{ end }}
    {{ if posts.isEmpty() == false }}
      <table class="w-full">
        <thead>
          <tr class="border-b border-gray-100">
            <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Title</th>
            <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Author</th>
            <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
            <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
            <th class="text-right px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          {{ for post in posts }}
            <tr class="hover:bg-gray-50">
              <td class="px-6 py-4">
                <a href="/admin/editor/{{ post.id }}" class="text-sm font-medium text-gray-900 hover:text-gray-600 no-underline">{{ post.title }}</a>
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ post.author.name }}</td>
              <td class="px-6 py-4">{{ raw post.statusLabel }}</td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ post.formattedDate }}</td>
              <td class="px-6 py-4 text-right">
                <a href="/admin/editor/{{ post.id }}" class="text-sm text-gray-500 hover:text-gray-900 no-underline">Edit</a>
              </td>
            </tr>
          {{ end }}
        </tbody>
      </table>
    {{ end }}
  </div>

  {{ if hasNextPage || hasPrevPage }}
    <nav class="flex justify-center gap-2 mt-6">
      {{ if hasPrevPage }}
        <a href="/admin/posts?page={{ prevPage }}&status={{ statusFilter }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">&larr; Previous</a>
      {{ end }}
      {{ if hasNextPage }}
        <a href="/admin/posts?page={{ nextPage }}&status={{ statusFilter }}" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 no-underline text-gray-700">Next &rarr;</a>
      {{ end }}
    </nav>
  {{ end }}
</div>
""")
public class PostListPage {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private RequestContext ctx;

    private List<lontar.model.Post> posts;
    private String statusFilter = "all";
    private Boolean filterAll = true;
    private Boolean filterPublished = false;
    private Boolean filterDraft = false;
    private Boolean hasPrevPage;
    private Boolean hasNextPage;
    private int prevPage;
    private int nextPage;
    private String success;

    public List<lontar.model.Post> getPosts() { return posts; }
    public String getStatusFilter() { return statusFilter; }
    public Boolean getFilterAll() { return filterAll; }
    public Boolean getFilterPublished() { return filterPublished; }
    public Boolean getFilterDraft() { return filterDraft; }
    public Boolean getHasPrevPage() { return hasPrevPage; }
    public Boolean getHasNextPage() { return hasNextPage; }
    public int getPrevPage() { return prevPage; }
    public int getNextPage() { return nextPage; }
    public String getSuccess() { return success; }

    public void init() {
        User currentUser = userService.getCurrentUser();

        String status = ctx.query("status");
        if ("published".equals(status)) {
            statusFilter = "published";
            filterAll = false;
            filterPublished = true;
        } else if ("draft".equals(status)) {
            statusFilter = "draft";
            filterAll = false;
            filterDraft = true;
        }

        String pageParam = ctx.query("page");
        int currentPage = 0;
        if (pageParam != null) {
            try { currentPage = Math.max(0, Integer.parseInt(pageParam) - 1); } catch (NumberFormatException ignored) {}
        }

        if ("deleted".equals(ctx.query("msg"))) {
            success = "Post deleted successfully.";
        }

        org.springframework.data.domain.Page<lontar.model.Post> page;
        boolean isAdminOrOwner = currentUser.getRole() == Role.OWNER || currentUser.getRole() == Role.ADMIN;

        if (isAdminOrOwner) {
            page = postService.findAll(PageRequest.of(currentPage, 20));
        } else {
            page = postService.findAllByAuthor(currentUser, PageRequest.of(currentPage, 20));
        }

        if (!"all".equals(statusFilter)) {
            var targetStatus = "published".equals(statusFilter)
                    ? lontar.model.PostStatus.PUBLISHED : lontar.model.PostStatus.DRAFT;
            posts = page.getContent().stream()
                    .filter(p -> p.getStatus() == targetStatus)
                    .toList();
        } else {
            posts = page.getContent();
        }

        hasPrevPage = currentPage > 0;
        hasNextPage = currentPage < page.getTotalPages() - 1;
        prevPage = currentPage;
        nextPage = currentPage + 2;
    }
}
