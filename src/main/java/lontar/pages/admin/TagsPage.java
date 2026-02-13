package lontar.pages.admin;

import candi.auth.core.CandiAuthService;
import candi.auth.core.annotation.Protected;
import candi.runtime.ActionResult;
import candi.runtime.Page;
import candi.runtime.Post;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import lombok.Getter;
import lontar.model.Role;
import lontar.model.Tag;
import lontar.model.User;
import lontar.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.List;
import java.util.UUID;

@Protected
@Page(value = "/admin/tags", layout = "admin")
@Template("""
<div>
  <h1 class="text-2xl font-bold text-gray-900 mb-6">Tags</h1>

  {{ if error != null }}
    <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">{{ error }}</div>
  {{ end }}

  {{ if success != null }}
    <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">{{ success }}</div>
  {{ end }}

  <!-- Create Tag Form -->
  <div class="bg-white rounded-lg border border-gray-200 p-6 mb-6">
    <h2 class="text-sm font-medium text-gray-900 mb-4">Add New Tag</h2>
    <form method="POST" class="flex items-end gap-4">
      <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
      <input type="hidden" name="action" value="create">
      <div class="flex-1">
        <label for="name" class="block text-sm font-medium text-gray-700 mb-1">Name</label>
        <input type="text" id="name" name="name" required
               class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition text-sm">
      </div>
      <div class="flex-1">
        <label for="description" class="block text-sm font-medium text-gray-700 mb-1">Description</label>
        <input type="text" id="description" name="description"
               class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition text-sm">
      </div>
      <button type="submit" class="px-5 py-2 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition whitespace-nowrap">
        Add Tag
      </button>
    </form>
  </div>

  <!-- Tags List -->
  <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
    {{ if tags.isEmpty() }}
      <div class="text-center py-12">
        <p class="text-gray-400">No tags yet.</p>
      </div>
    {{ else }}
      <table class="w-full">
        <thead>
          <tr class="border-b border-gray-100">
            <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
            <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Slug</th>
            <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
            <th class="text-right px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          {{ for tag in tags }}
            <tr class="hover:bg-gray-50" x-data="{ editing: false }">
              <td class="px-6 py-4 text-sm font-medium text-gray-900">
                <span x-show="!editing">{{ tag.name }}</span>
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ tag.slug }}</td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ tag.description }}</td>
              <td class="px-6 py-4 text-right">
                <form method="POST" class="inline" onsubmit="return confirm('Delete this tag?')">
                  <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="tagId" value="{{ tag.id }}">
                  <button type="submit" class="text-sm text-red-500 hover:text-red-700">Delete</button>
                </form>
              </td>
            </tr>
          {{ end }}
        </tbody>
      </table>
    {{ end }}
  </div>
</div>
""")
@Getter
public class TagsPage {

    @Autowired
    private TagService tagService;

    @Autowired
    private RequestContext ctx;

    private List<Tag> tags;
    private String error;
    private String success;
    private String csrfParameterName;
    private String csrfTokenValue;

    public void init() {
        CsrfToken csrf = (CsrfToken) ctx.raw().getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            csrfParameterName = csrf.getParameterName();
            csrfTokenValue = csrf.getToken();
        }

        tags = tagService.findAll();
    }

    @Post
    public ActionResult handleAction() {
        String action = ctx.form("action");

        if ("create".equals(action)) {
            String name = ctx.form("name");
            String description = ctx.form("description");
            if (name == null || name.isBlank()) {
                error = "Tag name is required.";
                return ActionResult.render();
            }
            if (tagService.findByName(name.trim()).isPresent()) {
                error = "A tag with this name already exists.";
                return ActionResult.render();
            }
            tagService.create(name.trim(), description != null && !description.isBlank() ? description.trim() : null);
            return ActionResult.redirect("/admin/tags");
        }

        if ("delete".equals(action)) {
            String tagId = ctx.form("tagId");
            if (tagId != null) {
                try {
                    tagService.delete(UUID.fromString(tagId));
                } catch (Exception ignored) {}
            }
            return ActionResult.redirect("/admin/tags");
        }

        return ActionResult.render();
    }
}
