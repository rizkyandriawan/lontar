package lontar.pages.admin;

import candi.auth.core.annotation.Protected;
import candi.runtime.ActionResult;
import candi.runtime.Page;
import candi.runtime.Post;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import lombok.Getter;
import lontar.model.Invite;
import lontar.model.Role;
import lontar.model.User;
import lontar.service.InviteService;
import lontar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.List;
import java.util.UUID;

@Protected(roles = {"owner", "admin"})
@Page(value = "/admin/team", layout = "admin")
@Template("""
<div>
  <h1 class="text-2xl font-bold text-gray-900 mb-6">Team</h1>

  {{ if error != null }}
    <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">{{ error }}</div>
  {{ end }}

  {{ if success != null }}
    <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">{{ success }}</div>
  {{ end }}

  {{ if isOwner }}
  <!-- Invite Form -->
  <div class="bg-white rounded-lg border border-gray-200 p-6 mb-6">
    <h2 class="text-sm font-medium text-gray-900 mb-4">Invite Team Member</h2>
    <form method="POST" class="flex items-end gap-4">
      <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
      <input type="hidden" name="action" value="invite">
      <div class="flex-1">
        <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
        <input type="email" id="email" name="email" required
               class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition text-sm">
      </div>
      <div>
        <label for="role" class="block text-sm font-medium text-gray-700 mb-1">Role</label>
        <select id="role" name="role"
                class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition text-sm">
          <option value="WRITER">Writer</option>
          <option value="ADMIN">Admin</option>
        </select>
      </div>
      <button type="submit" class="px-5 py-2 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition whitespace-nowrap">
        Send Invite
      </button>
    </form>
  </div>

  <!-- Pending Invites -->
  {{ if pendingInvites.isEmpty() == false }}
    <div class="bg-white rounded-lg border border-gray-200 overflow-hidden mb-6">
      <div class="px-6 py-3 border-b border-gray-100">
        <h2 class="text-sm font-medium text-gray-900">Pending Invites</h2>
      </div>
      <table class="w-full">
        <tbody class="divide-y divide-gray-50">
          {{ for invite in pendingInvites }}
            <tr class="hover:bg-gray-50">
              <td class="px-6 py-4 text-sm text-gray-900">{{ invite.email }}</td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ invite.role }}</td>
              <td class="px-6 py-4 text-right">
                <form method="POST" class="inline">
                  <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
                  <input type="hidden" name="action" value="revoke">
                  <input type="hidden" name="inviteId" value="{{ invite.id }}">
                  <button type="submit" class="text-sm text-red-500 hover:text-red-700">Revoke</button>
                </form>
              </td>
            </tr>
          {{ end }}
        </tbody>
      </table>
    </div>
  {{ end }}
  {{ end }}

  <!-- Team Members -->
  <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
    <div class="px-6 py-3 border-b border-gray-100">
      <h2 class="text-sm font-medium text-gray-900">Members</h2>
    </div>
    <table class="w-full">
      <thead>
        <tr class="border-b border-gray-100">
          <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
          <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
          <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Role</th>
          {{ if isOwner }}
            <th class="text-right px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
          {{ end }}
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-50">
        {{ for member in members }}
          <tr class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm font-medium text-gray-900">{{ member.name }}</td>
            <td class="px-6 py-4 text-sm text-gray-500">{{ member.email }}</td>
            <td class="px-6 py-4">{{ raw member.roleLabel }}</td>
            {{ if isOwner }}
              <td class="px-6 py-4 text-right">
                {{ if member.isOwner == false }}
                  <form method="POST" class="inline-flex items-center gap-2">
                    <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
                    <input type="hidden" name="action" value="changeRole">
                    <input type="hidden" name="userId" value="{{ member.uuid }}">
                    <select name="newRole" class="text-xs px-2 py-1 border border-gray-200 rounded">
                      <option value="WRITER" {{ if member.isWriter }}selected{{ end }}>Writer</option>
                      <option value="ADMIN" {{ if member.isAdmin }}selected{{ end }}>Admin</option>
                    </select>
                    <button type="submit" class="text-xs text-gray-500 hover:text-gray-900">Save</button>
                  </form>
                  <form method="POST" class="inline ml-3" onsubmit="return confirm('Remove this team member?')">
                    <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
                    <input type="hidden" name="action" value="remove">
                    <input type="hidden" name="userId" value="{{ member.uuid }}">
                    <button type="submit" class="text-xs text-red-500 hover:text-red-700">Remove</button>
                  </form>
                {{ end }}
              </td>
            {{ end }}
          </tr>
        {{ end }}
      </tbody>
    </table>
  </div>
</div>
""")
@Getter
public class TeamPage {

    @Autowired
    private UserService userService;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private RequestContext ctx;

    private List<User> members;
    private List<Invite> pendingInvites;
    private Boolean isOwner;
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

        User currentUser = userService.getCurrentUser();
        isOwner = currentUser.getRole() == Role.OWNER;

        // Only Owner and Admin can access team page
        if (currentUser.getRole() != Role.OWNER && currentUser.getRole() != Role.ADMIN) {
            ctx.raw().setAttribute("candi.redirect", "/admin");
            return;
        }

        members = userService.findAll();
        pendingInvites = isOwner ? inviteService.findPending() : List.of();
    }

    @Post
    public ActionResult handleAction() {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != Role.OWNER) {
            return ActionResult.redirect("/admin/team");
        }

        String action = ctx.form("action");

        if ("invite".equals(action)) {
            String email = ctx.form("email");
            String role = ctx.form("role");
            if (email == null || email.isBlank()) {
                error = "Email is required.";
                return ActionResult.render();
            }
            if (userService.findByEmail(email).isPresent()) {
                error = "A user with this email already exists.";
                return ActionResult.render();
            }
            Role inviteRole = "ADMIN".equals(role) ? Role.ADMIN : Role.WRITER;
            Invite invite = inviteService.create(email.trim(), inviteRole, currentUser);
            success = "Invite sent! Token: " + invite.getToken();
            members = userService.findAll();
            pendingInvites = inviteService.findPending();
            return ActionResult.render();
        }

        if ("revoke".equals(action)) {
            String inviteId = ctx.form("inviteId");
            if (inviteId != null) {
                try { inviteService.revoke(UUID.fromString(inviteId)); } catch (Exception ignored) {}
            }
            return ActionResult.redirect("/admin/team");
        }

        if ("changeRole".equals(action)) {
            String userId = ctx.form("userId");
            String newRole = ctx.form("newRole");
            if (userId != null && newRole != null) {
                try {
                    Role role = Role.valueOf(newRole);
                    if (role != Role.OWNER) {
                        userService.changeRole(UUID.fromString(userId), role);
                    }
                } catch (Exception ignored) {}
            }
            return ActionResult.redirect("/admin/team");
        }

        if ("remove".equals(action)) {
            String userId = ctx.form("userId");
            if (userId != null) {
                try {
                    UUID uid = UUID.fromString(userId);
                    // Don't allow removing self
                    if (!uid.equals(currentUser.getUuid())) {
                        userService.deleteUser(uid);
                    }
                } catch (Exception ignored) {}
            }
            return ActionResult.redirect("/admin/team");
        }

        return ActionResult.redirect("/admin/team");
    }
}
