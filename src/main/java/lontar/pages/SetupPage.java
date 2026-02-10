package lontar.pages;

import candi.auth.core.annotation.Public;
import candi.runtime.ActionResult;
import candi.runtime.Page;
import candi.runtime.Post;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import lontar.config.SetupRedirectFilter;
import lontar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;

@Page(value = "/setup", layout = "base")
@Public
@Template("""
<div class="max-w-md mx-auto mt-16 px-6">
  <div class="text-center mb-8">
    <h1 class="text-3xl font-bold font-serif text-gray-900">Welcome to Lontar</h1>
    <p class="mt-2 text-gray-500">Create your owner account to get started.</p>
  </div>

  {{ if error != null }}
    <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">{{ error }}</div>
  {{ end }}

  <form method="POST" class="space-y-5">
    <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
    <div>
      <label for="name" class="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
      <input type="text" id="name" name="name" required
             class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
    </div>
    <div>
      <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
      <input type="email" id="email" name="email" required
             class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
    </div>
    <div>
      <label for="password" class="block text-sm font-medium text-gray-700 mb-1">Password</label>
      <input type="password" id="password" name="password" required minlength="8"
             class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
    </div>
    <button type="submit"
            class="w-full py-2.5 px-4 bg-gray-900 text-white rounded-lg font-medium hover:bg-gray-800 transition">
      Create Account
    </button>
  </form>
</div>
""")
public class SetupPage {

    @Autowired
    private UserService userService;

    @Autowired
    private RequestContext ctx;

    @Autowired
    private SetupRedirectFilter setupFilter;

    private String error;
    private String csrfParameterName;
    private String csrfTokenValue;

    public void init() {
        if (userService.hasAnyUsers()) {
            ctx.raw().setAttribute("candi.redirect", "/");
            return;
        }

        CsrfToken csrf = (CsrfToken) ctx.raw().getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            csrfParameterName = csrf.getParameterName();
            csrfTokenValue = csrf.getToken();
        }
    }

    public String getError() { return error; }
    public String getCsrfParameterName() { return csrfParameterName; }
    public String getCsrfTokenValue() { return csrfTokenValue; }

    @Post
    public ActionResult create() {
        if (userService.hasAnyUsers()) {
            return ActionResult.redirect("/");
        }

        String name = ctx.form("name");
        String email = ctx.form("email");
        String password = ctx.form("password");

        if (name == null || name.isBlank() || email == null || email.isBlank()
                || password == null || password.length() < 8) {
            error = "All fields are required. Password must be at least 8 characters.";
            return ActionResult.render();
        }

        try {
            userService.createOwner(name, email, password);
            setupFilter.clearCache();
            return ActionResult.redirect("/login?setup=success");
        } catch (Exception e) {
            error = "Could not create account. Email may already be in use.";
            return ActionResult.render();
        }
    }
}
