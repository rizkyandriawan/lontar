package lontar.pages.admin;

import candi.auth.core.annotation.Protected;
import candi.runtime.ActionResult;
import candi.runtime.Page;
import candi.runtime.Post;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import lombok.Getter;
import lontar.model.Role;
import lontar.model.SiteSettings;
import lontar.model.User;
import lontar.service.SiteSettingsService;
import lontar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;

@Protected(roles = {"owner", "admin"})
@Page(value = "/admin/settings", layout = "admin")
@Template("""
<div>
  <h1 class="text-2xl font-bold text-gray-900 mb-6">Settings</h1>

  {{ if error != null }}
    <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">{{ error }}</div>
  {{ end }}

  {{ if success != null }}
    <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">{{ success }}</div>
  {{ end }}

  <form method="POST" class="space-y-6">
    <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">

    <div class="bg-white rounded-lg border border-gray-200 p-6 space-y-6">
      <div>
        <label for="title" class="block text-sm font-medium text-gray-700 mb-1">Site Title</label>
        <input type="text" id="title" name="title" value="{{ settingsTitle }}" required
               class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
      </div>

      <div>
        <label for="description" class="block text-sm font-medium text-gray-700 mb-1">Site Description</label>
        <textarea id="description" name="description" rows="3"
                  class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition resize-y">{{ settingsDescription }}</textarea>
      </div>

      <div>
        <label for="postsPerPage" class="block text-sm font-medium text-gray-700 mb-1">Posts Per Page</label>
        <input type="number" id="postsPerPage" name="postsPerPage" value="{{ postsPerPage }}" min="1" max="50"
               class="w-32 px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
      </div>

      <div class="flex items-center gap-3">
        <input type="checkbox" id="allowComments" name="allowComments" value="true" {{ if allowComments }}checked{{ end }}
               class="w-4 h-4 text-gray-900 border-gray-300 rounded focus:ring-gray-900">
        <label for="allowComments" class="text-sm font-medium text-gray-700">Allow comments on posts</label>
      </div>
    </div>

    <button type="submit"
            class="px-5 py-2.5 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition">
      Save Settings
    </button>
  </form>
</div>
""")
@Getter
public class SettingsPage {

    @Autowired
    private SiteSettingsService settingsService;

    @Autowired
    private UserService userService;

    @Autowired
    private RequestContext ctx;

    private String error;
    private String success;
    private String csrfParameterName;
    private String csrfTokenValue;

    private String settingsTitle;
    private String settingsDescription;
    private int postsPerPage;
    private Boolean allowComments;

    public void init() {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != Role.OWNER && currentUser.getRole() != Role.ADMIN) {
            ctx.raw().setAttribute("candi.redirect", "/admin");
            return;
        }

        CsrfToken csrf = (CsrfToken) ctx.raw().getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            csrfParameterName = csrf.getParameterName();
            csrfTokenValue = csrf.getToken();
        }

        SiteSettings settings = settingsService.getSettings();
        settingsTitle = settings.getTitle();
        settingsDescription = settings.getDescription();
        postsPerPage = settings.getPostsPerPage();
        allowComments = settings.isAllowComments();
    }

    @Post
    public ActionResult save() {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != Role.OWNER && currentUser.getRole() != Role.ADMIN) {
            return ActionResult.redirect("/admin");
        }

        String title = ctx.form("title");
        String description = ctx.form("description");
        String postsPerPageStr = ctx.form("postsPerPage");
        String commentsToggle = ctx.form("allowComments");

        if (title == null || title.isBlank()) {
            error = "Site title is required.";
            return ActionResult.render();
        }

        SiteSettings settings = settingsService.getSettings();
        settings.setTitle(title.trim());
        settings.setDescription(description != null ? description.trim() : "");

        try {
            int ppp = Integer.parseInt(postsPerPageStr);
            settings.setPostsPerPage(Math.max(1, Math.min(50, ppp)));
        } catch (Exception ignored) {}

        settings.setAllowComments("true".equals(commentsToggle));
        settingsService.updateSettings(settings);

        success = "Settings saved.";
        settingsTitle = settings.getTitle();
        settingsDescription = settings.getDescription();
        postsPerPage = settings.getPostsPerPage();
        allowComments = settings.isAllowComments();

        return ActionResult.render();
    }
}
