package lontar.layouts;

import candi.runtime.Layout;
import candi.runtime.Template;
import lontar.service.SiteSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@Layout
@Template("""
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>{{ siteTitle }}</title>
  {{ widget "cnd-seo" }}
  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    tailwind.config = {
      theme: {
        extend: {
          fontFamily: {
            sans: ['Inter', 'system-ui', 'sans-serif'],
            serif: ['Lora', 'Georgia', 'serif'],
          },
          typography: {
            DEFAULT: {
              css: {
                maxWidth: 'none',
              }
            }
          }
        }
      }
    }
  </script>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Lora:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap" rel="stylesheet">
  <script defer src="https://cdn.jsdelivr.net/npm/alpinejs@3.x.x/dist/cdn.min.js"></script>
  <style>
    [x-cloak] { display: none !important; }
  </style>
</head>
<body class="bg-white text-gray-900 font-sans antialiased">
  <nav class="border-b border-gray-100">
    <div class="max-w-5xl mx-auto px-6 py-4 flex items-center justify-between">
      <a href="/" class="text-xl font-bold text-gray-900 tracking-tight hover:text-gray-700 no-underline">{{ siteTitle }}</a>
      <div class="flex items-center gap-6 text-sm">
        <a href="/search" class="text-gray-500 hover:text-gray-900 no-underline">Search</a>
        <a href="/login" class="text-gray-500 hover:text-gray-900 no-underline">Sign in</a>
      </div>
    </div>
  </nav>
  <main class="min-h-[70vh]">
    {{ content }}
  </main>
  <footer class="border-t border-gray-100 mt-16">
    <div class="max-w-5xl mx-auto px-6 py-8 text-center text-sm text-gray-400">
      &copy; 2026 {{ siteTitle }} &mdash; Powered by Lontar
    </div>
  </footer>
</body>
</html>
""")
public class BaseLayout {

    @Autowired
    protected ApplicationContext _applicationContext;

    @Autowired
    private SiteSettingsService siteSettingsService;

    protected String siteTitle = "";

    @jakarta.annotation.PostConstruct
    public void initLayout() {
        siteTitle = siteSettingsService.getSettings().getTitle();
    }

    public String getSiteTitle() {
        return siteTitle;
    }
}
