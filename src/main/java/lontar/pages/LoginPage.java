package lontar.pages;

import candi.auth.core.CandiAuthService;
import candi.auth.core.annotation.Public;
import candi.runtime.ActionResult;
import candi.runtime.Page;
import candi.runtime.Post;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;

@Page(value = "/login", layout = "base")
@Public
@Template("""
<div class="max-w-md mx-auto mt-16 px-6">
  <div class="text-center mb-8">
    <h1 class="text-3xl font-bold font-serif text-gray-900">Sign in</h1>
    <p class="mt-2 text-gray-500">Welcome back to your blog.</p>
  </div>

  {{ if success != null }}
    <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">{{ success }}</div>
  {{ end }}

  {{ if error != null }}
    <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">{{ error }}</div>
  {{ end }}

  <form method="POST" action="/login" class="space-y-5">
    <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
    <div>
      <label for="username" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
      <input type="email" id="username" name="username" required
             class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
    </div>
    <div>
      <label for="password" class="block text-sm font-medium text-gray-700 mb-1">Password</label>
      <input type="password" id="password" name="password" required
             class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent outline-none transition">
    </div>
    <button type="submit"
            class="w-full py-2.5 px-4 bg-gray-900 text-white rounded-lg font-medium hover:bg-gray-800 transition">
      Sign in
    </button>
  </form>

  <div class="mt-6">
    <div class="relative">
      <div class="absolute inset-0 flex items-center"><div class="w-full border-t border-gray-200"></div></div>
      <div class="relative flex justify-center text-sm"><span class="bg-white px-4 text-gray-400">or</span></div>
    </div>
    <a href="/oauth2/authorization/google"
       class="mt-4 flex items-center justify-center gap-3 w-full py-2.5 px-4 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 transition no-underline">
      <svg class="w-5 h-5" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
      Sign in with Google
    </a>
  </div>
</div>
""")
public class LoginPage {

    @Autowired
    private RequestContext ctx;

    @Autowired
    private CandiAuthService authService;

    private String error;
    private String success;
    private String csrfParameterName;
    private String csrfTokenValue;

    public String getError() { return error; }
    public String getSuccess() { return success; }
    public String getCsrfParameterName() { return csrfParameterName; }
    public String getCsrfTokenValue() { return csrfTokenValue; }

    public void init() {
        if (authService.isAuthenticated()) {
            ctx.raw().setAttribute("candi.redirect", "/admin");
            return;
        }

        CsrfToken csrf = (CsrfToken) ctx.raw().getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            csrfParameterName = csrf.getParameterName();
            csrfTokenValue = csrf.getToken();
        }

        String setupParam = ctx.query("setup");
        if ("success".equals(setupParam)) {
            success = "Account created! Sign in to continue.";
        }

        String errorParam = ctx.query("error");
        if (errorParam != null) {
            error = "oauth".equals(errorParam)
                    ? "Social login failed. You may not have an active invitation."
                    : "Invalid email or password.";
        }
    }

    @Post
    public ActionResult login() {
        String username = ctx.form("username");
        String password = ctx.form("password");

        try {
            authService.login(username, password);
            return ActionResult.redirect("/admin");
        } catch (Exception e) {
            error = "Invalid email or password.";
            return ActionResult.render();
        }
    }
}
