package lontar.pages.admin;

import candi.auth.core.annotation.Protected;
import candi.runtime.ActionResult;
import candi.runtime.Page;
import candi.runtime.Post;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import lontar.model.User;
import lontar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;

@Protected
@Page(value = "/admin/profile", layout = "admin")
@Template("""
<div>
  <h1 class="text-2xl font-bold text-gray-900 mb-6">Profile</h1>

  {{ if error != null }}
    <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">{{ error }}</div>
  {{ end }}

  {{ if success != null }}
    <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">{{ success }}</div>
  {{ end }}

  <!-- Profile Info -->
  <form method="POST" class="space-y-6 mb-10">
    <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
    <input type="hidden" name="action" value="updateProfile">

    <div class="bg-white rounded-lg border border-gray-200 p-6 space-y-6">
      <h2 class="text-sm font-medium text-gray-900">Profile Information</h2>
      <div>
        <label for="name" class="block text-sm font-medium text-gray-700 mb-1">Name</label>
        <input type="text" id="name" name="name" value="{{ userName }}" required
               class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
      </div>
      <div>
        <label for="bio" class="block text-sm font-medium text-gray-700 mb-1">Bio</label>
        <textarea id="bio" name="bio" rows="3"
                  class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition resize-y">{{ userBio }}</textarea>
      </div>
    </div>

    <button type="submit" class="px-5 py-2.5 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition">
      Update Profile
    </button>
  </form>

  <!-- Change Password -->
  <form method="POST" class="space-y-6">
    <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
    <input type="hidden" name="action" value="changePassword">

    <div class="bg-white rounded-lg border border-gray-200 p-6 space-y-6">
      <h2 class="text-sm font-medium text-gray-900">Change Password</h2>
      <div>
        <label for="currentPassword" class="block text-sm font-medium text-gray-700 mb-1">Current Password</label>
        <input type="password" id="currentPassword" name="currentPassword" required
               class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
      </div>
      <div>
        <label for="newPassword" class="block text-sm font-medium text-gray-700 mb-1">New Password</label>
        <input type="password" id="newPassword" name="newPassword" required minlength="8"
               class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
      </div>
      <div>
        <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-1">Confirm New Password</label>
        <input type="password" id="confirmPassword" name="confirmPassword" required minlength="8"
               class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
      </div>
    </div>

    <button type="submit" class="px-5 py-2.5 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition">
      Change Password
    </button>
  </form>
</div>
""")
public class ProfilePage {

    @Autowired
    private UserService userService;

    @Autowired
    private RequestContext ctx;

    private String error;
    private String success;
    private String csrfParameterName;
    private String csrfTokenValue;
    private String userName = "";
    private String userBio = "";

    public String getError() { return error; }
    public String getSuccess() { return success; }
    public String getCsrfParameterName() { return csrfParameterName; }
    public String getCsrfTokenValue() { return csrfTokenValue; }
    public String getUserName() { return userName; }
    public String getUserBio() { return userBio; }

    public void init() {
        CsrfToken csrf = (CsrfToken) ctx.raw().getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            csrfParameterName = csrf.getParameterName();
            csrfTokenValue = csrf.getToken();
        }

        User currentUser = userService.getCurrentUser();
        userName = currentUser.getName();
        userBio = currentUser.getBio() != null ? currentUser.getBio() : "";
    }

    @Post
    public ActionResult handleAction() {
        User currentUser = userService.getCurrentUser();
        String action = ctx.form("action");

        if ("updateProfile".equals(action)) {
            String name = ctx.form("name");
            String bio = ctx.form("bio");

            if (name == null || name.isBlank()) {
                error = "Name is required.";
                return ActionResult.render();
            }

            currentUser.setName(name.trim());
            currentUser.setBio(bio != null && !bio.isBlank() ? bio.trim() : null);
            userService.updateProfile(currentUser);

            userName = currentUser.getName();
            userBio = currentUser.getBio() != null ? currentUser.getBio() : "";
            success = "Profile updated.";
            return ActionResult.render();
        }

        if ("changePassword".equals(action)) {
            String currentPassword = ctx.form("currentPassword");
            String newPassword = ctx.form("newPassword");
            String confirmPassword = ctx.form("confirmPassword");

            if (currentPassword == null || newPassword == null || confirmPassword == null) {
                error = "All password fields are required.";
                return ActionResult.render();
            }

            if (newPassword.length() < 8) {
                error = "New password must be at least 8 characters.";
                return ActionResult.render();
            }

            if (!newPassword.equals(confirmPassword)) {
                error = "New passwords do not match.";
                return ActionResult.render();
            }

            if (!userService.checkPassword(currentUser, currentPassword)) {
                error = "Current password is incorrect.";
                return ActionResult.render();
            }

            userService.changePassword(currentUser, newPassword);
            success = "Password changed successfully.";
            return ActionResult.render();
        }

        return ActionResult.render();
    }
}
