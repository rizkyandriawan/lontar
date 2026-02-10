package lontar.pages.admin;

import candi.auth.core.annotation.Protected;
import candi.runtime.Page;
import candi.runtime.Template;
import lontar.model.PostStatus;
import lontar.service.CommentService;
import lontar.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;

@Protected
@Page(value = "/admin", layout = "admin")
@Template("""
<div>
  <h1 class="text-2xl font-bold text-gray-900 mb-8">Dashboard</h1>

  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <div class="text-sm font-medium text-gray-500">Total Posts</div>
      <div class="text-3xl font-bold text-gray-900 mt-2">{{ totalPosts }}</div>
    </div>
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <div class="text-sm font-medium text-gray-500">Published</div>
      <div class="text-3xl font-bold text-green-600 mt-2">{{ publishedPosts }}</div>
    </div>
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <div class="text-sm font-medium text-gray-500">Drafts</div>
      <div class="text-3xl font-bold text-yellow-600 mt-2">{{ draftPosts }}</div>
    </div>
    <div class="bg-white rounded-lg border border-gray-200 p-6">
      <div class="text-sm font-medium text-gray-500">Comments</div>
      <div class="text-3xl font-bold text-blue-600 mt-2">{{ totalComments }}</div>
    </div>
  </div>

  <div class="flex gap-4">
    <a href="/admin/editor" class="inline-flex items-center gap-2 px-4 py-2 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 no-underline">
      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
      New Post
    </a>
    <a href="/admin/posts" class="inline-flex items-center gap-2 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50 no-underline">
      View All Posts
    </a>
  </div>
</div>
""")
public class DashboardPage {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    private long totalPosts;
    private long publishedPosts;
    private long draftPosts;
    private long totalComments;

    public long getTotalPosts() { return totalPosts; }
    public long getPublishedPosts() { return publishedPosts; }
    public long getDraftPosts() { return draftPosts; }
    public long getTotalComments() { return totalComments; }

    public void init() {
        totalPosts = postService.countAll();
        publishedPosts = postService.countByStatus(PostStatus.PUBLISHED);
        draftPosts = postService.countByStatus(PostStatus.DRAFT);
        totalComments = commentService.countAll();
    }
}
