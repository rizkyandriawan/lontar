package lontar.pages.admin;

import candi.auth.core.annotation.Protected;
import candi.runtime.ActionResult;
import candi.runtime.Page;
import candi.runtime.Post;
import candi.runtime.RequestContext;
import candi.runtime.Template;
import lontar.model.Tag;
import lontar.model.User;
import lontar.service.PostService;
import lontar.service.TagService;
import lontar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Protected
@Page(value = "/admin/editor", layout = "admin")
@Template("""
<div x-data="editorApp()" x-init="initSlugWatcher()">
  <div class="flex items-center justify-between mb-6">
    <h1 class="text-2xl font-bold text-gray-900">New Post</h1>
    <div class="flex items-center gap-3">
      <!-- Import Markdown -->
      <label class="inline-flex items-center gap-2 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50 cursor-pointer transition">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
        Import .md
        <input type="file" accept=".md,.markdown" class="hidden" x-on:change="importMarkdown($event)">
      </label>
    </div>
  </div>

  {{ if error != null }}
    <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">{{ error }}</div>
  {{ end }}

  <form method="POST" enctype="multipart/form-data" class="space-y-6" id="editorForm">
    <input type="hidden" name="{{ csrfParameterName }}" value="{{ csrfTokenValue }}">
    <input type="hidden" name="action" id="formAction" value="save_draft">

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Main content area -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Title -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <input type="text" id="title" name="title" value="{{ postTitle }}" required placeholder="Post title..."
                 x-model="title" x-on:input="generateSlug()"
                 class="w-full text-2xl font-serif font-bold text-gray-900 placeholder-gray-300 border-0 outline-none focus:ring-0 p-0">
        </div>

        <!-- Content Editor -->
        <div class="bg-white rounded-lg border border-gray-200">
          <textarea id="content" name="content" class="hidden">{{ raw postContent }}</textarea>
          <div id="editor" class="min-h-[600px]"></div>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="space-y-6">
        <!-- Publish actions -->
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <h3 class="text-sm font-semibold text-gray-900 mb-4">Publish</h3>
          <div class="space-y-3">
            <button type="submit" onclick="document.getElementById('formAction').value='save_draft'"
                    class="w-full py-2.5 px-4 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition">
              Save Draft
            </button>
            <button type="submit" onclick="document.getElementById('formAction').value='publish'"
                    class="w-full py-2.5 px-4 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 transition">
              Publish
            </button>
          </div>
        </div>

        <!-- Slug -->
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <h3 class="text-sm font-semibold text-gray-900 mb-3">URL Slug</h3>
          <div class="flex items-center gap-2">
            <span class="text-xs text-gray-400 whitespace-nowrap">/post/</span>
            <input type="text" id="slug" name="slug" x-model="slug"
                   class="flex-1 px-3 py-1.5 border border-gray-200 rounded text-sm text-gray-600 focus:ring-1 focus:ring-gray-900 focus:border-transparent outline-none">
          </div>
          <button type="button" x-on:click="generateSlug()" class="text-xs text-blue-600 hover:text-blue-800 mt-2">Regenerate from title</button>
        </div>

        <!-- Cover Image -->
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <h3 class="text-sm font-semibold text-gray-900 mb-3">Cover Image</h3>
          <div x-show="coverImageUrl" class="mb-3 relative group">
            <img x-bind:src="coverImageUrl" class="w-full h-32 object-cover rounded-lg">
            <button type="button" x-on:click="coverImageUrl = ''" class="absolute top-2 right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs opacity-0 group-hover:opacity-100 transition">&times;</button>
          </div>
          <input type="hidden" name="coverImage" x-bind:value="coverImageUrl">
          <label class="flex items-center justify-center gap-2 px-4 py-3 border-2 border-dashed border-gray-200 rounded-lg cursor-pointer hover:border-gray-400 transition">
            <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
            <span class="text-sm text-gray-500" x-text="uploading ? 'Uploading...' : 'Upload image'"></span>
            <input type="file" accept="image/*" class="hidden" x-on:change="uploadCoverImage($event)" x-bind:disabled="uploading">
          </label>
          <div class="mt-2">
            <input type="text" placeholder="Or paste image URL..." x-model="coverImageUrl"
                   class="w-full px-3 py-1.5 border border-gray-200 rounded text-xs text-gray-500 focus:ring-1 focus:ring-gray-900 outline-none">
          </div>
        </div>

        <!-- Excerpt -->
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <h3 class="text-sm font-semibold text-gray-900 mb-3">Excerpt <span class="text-gray-400 font-normal">(optional)</span></h3>
          <textarea id="excerpt" name="excerpt" rows="3" placeholder="Auto-generated if blank..."
                    class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-1 focus:ring-gray-900 focus:border-transparent outline-none resize-y">{{ postExcerpt }}</textarea>
        </div>

        <!-- Tags -->
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <h3 class="text-sm font-semibold text-gray-900 mb-3">Tags</h3>
          <input type="text" id="tags" name="tags" value="{{ postTags }}" placeholder="javascript, web, tutorial..."
                 class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-1 focus:ring-gray-900 focus:border-transparent outline-none">
          <p class="text-xs text-gray-400 mt-1">Comma separated</p>
        </div>
      </div>
    </div>
  </form>
</div>

<!-- CKEditor 5 -->
<link rel="stylesheet" href="https://cdn.ckeditor.com/ckeditor5/43.3.1/ckeditor5.css">
<style>
  .ck-editor__editable { min-height: 600px !important; font-family: 'Lora', Georgia, serif; font-size: 1.1rem; line-height: 1.8; padding: 2rem !important; }
  .ck-editor__editable:focus { border-color: transparent !important; box-shadow: none !important; }
  .ck.ck-editor__top .ck-sticky-panel .ck-toolbar { border-radius: 0.5rem 0.5rem 0 0 !important; border-left: 0; border-right: 0; border-top: 0; }
  .ck.ck-editor__main>.ck-editor__editable { border-radius: 0 0 0.5rem 0.5rem !important; border-left: 0; border-right: 0; border-bottom: 0; }
  .ck.ck-editor { border: 0 !important; }
</style>
<script type="importmap">
{
  "imports": {
    "ckeditor5": "https://cdn.ckeditor.com/ckeditor5/43.3.1/ckeditor5.js",
    "ckeditor5/": "https://cdn.ckeditor.com/ckeditor5/43.3.1/"
  }
}
</script>
<script type="module">
  import { ClassicEditor, Essentials, Paragraph, Bold, Italic, Heading, Link, List, BlockQuote, Image, ImageInsertViaUrl, Table, MediaEmbed, CodeBlock, HorizontalLine, Indent, Underline, Strikethrough, SourceEditing, Markdown as CKMarkdown, Autoformat, TextTransformation, PasteFromOffice } from 'ckeditor5';

  let ckEditor;
  const editorEl = document.querySelector('#editor');
  if (editorEl) {
    ckEditor = await ClassicEditor.create(editorEl, {
      plugins: [ Essentials, Paragraph, Bold, Italic, Underline, Strikethrough, Heading, Link, List, BlockQuote, Image, ImageInsertViaUrl, Table, MediaEmbed, CodeBlock, HorizontalLine, Indent, SourceEditing, Autoformat, TextTransformation, PasteFromOffice ],
      toolbar: {
        items: [ 'heading', '|', 'bold', 'italic', 'underline', 'strikethrough', '|', 'link', 'bulletedList', 'numberedList', '|', 'blockQuote', 'codeBlock', 'insertTable', 'horizontalLine', '|', 'insertImage', 'mediaEmbed', '|', 'indent', 'outdent', '|', 'sourceEditing' ],
        shouldNotGroupWhenFull: false
      },
      heading: {
        options: [
          { model: 'paragraph', title: 'Paragraph', class: 'ck-heading_paragraph' },
          { model: 'heading2', view: 'h2', title: 'Heading 2', class: 'ck-heading_heading2' },
          { model: 'heading3', view: 'h3', title: 'Heading 3', class: 'ck-heading_heading3' },
          { model: 'heading4', view: 'h4', title: 'Heading 4', class: 'ck-heading_heading4' }
        ]
      },
      placeholder: 'Start writing your post...'
    });

    const existingContent = document.querySelector('#content').value;
    if (existingContent) {
      ckEditor.setData(existingContent);
    }

    document.querySelector('#editorForm').addEventListener('submit', () => {
      document.querySelector('#content').value = ckEditor.getData();
    });
  }

  // Alpine.js editor app
  window.editorApp = function() {
    return {
      title: document.querySelector('#title')?.value || '',
      slug: document.querySelector('#slug')?.value || '',
      coverImageUrl: document.querySelector('input[name="coverImage"]')?.value || '',
      uploading: false,
      slugManuallyEdited: false,

      initSlugWatcher() {
        const slugInput = document.querySelector('#slug');
        if (slugInput) {
          slugInput.addEventListener('input', () => { this.slugManuallyEdited = true; });
        }
      },

      generateSlug() {
        if (this.slugManuallyEdited && this.slug) return;
        this.slug = this.title.toLowerCase()
          .replace(/[^a-z0-9\\s-]/g, '')
          .replace(/\\s+/g, '-')
          .replace(/-+/g, '-')
          .replace(/^-|-$/g, '');
      },

      async uploadCoverImage(event) {
        const file = event.target.files[0];
        if (!file) return;
        this.uploading = true;
        try {
          const csrf = document.querySelector('input[name="{{ csrfParameterName }}"]');
          const formData = new FormData();
          formData.append('file', file);
          const res = await fetch('/api/upload/image', {
            method: 'POST',
            headers: csrf ? { 'X-CSRF-TOKEN': csrf.value } : {},
            body: formData
          });
          const data = await res.json();
          if (data.url) {
            this.coverImageUrl = data.url;
          } else {
            alert(data.error || 'Upload failed');
          }
        } catch (e) {
          alert('Upload failed: ' + e.message);
        } finally {
          this.uploading = false;
          event.target.value = '';
        }
      },

      async importMarkdown(event) {
        const file = event.target.files[0];
        if (!file) return;
        try {
          const csrf = document.querySelector('input[name="{{ csrfParameterName }}"]');
          const formData = new FormData();
          formData.append('file', file);
          const res = await fetch('/api/upload/markdown', {
            method: 'POST',
            headers: csrf ? { 'X-CSRF-TOKEN': csrf.value } : {},
            body: formData
          });
          const data = await res.json();
          if (data.content && ckEditor) {
            // Convert markdown to HTML (basic)
            let html = data.content;
            // Basic markdown to HTML conversion
            html = html.replace(/^### (.*$)/gm, '<h3>$1</h3>');
            html = html.replace(/^## (.*$)/gm, '<h2>$1</h2>');
            html = html.replace(/^# (.*$)/gm, '<h1>$1</h1>');
            html = html.replace(/\\*\\*(.+?)\\*\\*/g, '<strong>$1</strong>');
            html = html.replace(/\\*(.+?)\\*/g, '<em>$1</em>');
            html = html.replace(/`(.+?)`/g, '<code>$1</code>');
            html = html.replace(/!\\[([^\\]]*)\\]\\(([^)]+)\\)/g, '<img src="$2" alt="$1">');
            html = html.replace(/\\[([^\\]]*)\\]\\(([^)]+)\\)/g, '<a href="$2">$1</a>');
            html = html.replace(/^---$/gm, '<hr>');
            // Wrap bare lines in <p> tags
            html = html.split('\\n\\n').map(block => {
              block = block.trim();
              if (!block) return '';
              if (block.startsWith('<')) return block;
              return '<p>' + block.replace(/\\n/g, '<br>') + '</p>';
            }).join('\\n');
            ckEditor.setData(html);
            // Try to extract title from filename
            if (!this.title) {
              let fname = data.filename.replace(/\\.(md|markdown)$/i, '').replace(/[-_]/g, ' ');
              this.title = fname.charAt(0).toUpperCase() + fname.slice(1);
              this.generateSlug();
            }
          } else if (data.error) {
            alert(data.error);
          }
        } catch (e) {
          alert('Import failed: ' + e.message);
        } finally {
          event.target.value = '';
        }
      }
    };
  };
</script>
""")
public class EditorPage {

    @Autowired
    private PostService postService;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @Autowired
    private RequestContext ctx;

    private String error;
    private String csrfParameterName;
    private String csrfTokenValue;

    private String postTitle = "";
    private String postSlug = "";
    private String postContent = "";
    private String postExcerpt = "";
    private String postTags = "";
    private String postCoverImage = "";

    public String getError() { return error; }
    public String getCsrfParameterName() { return csrfParameterName; }
    public String getCsrfTokenValue() { return csrfTokenValue; }
    public String getPostTitle() { return postTitle; }
    public String getPostSlug() { return postSlug; }
    public String getPostContent() { return postContent; }
    public String getPostExcerpt() { return postExcerpt; }
    public String getPostTags() { return postTags; }
    public String getPostCoverImage() { return postCoverImage; }

    public void init() {
        CsrfToken csrf = (CsrfToken) ctx.raw().getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            csrfParameterName = csrf.getParameterName();
            csrfTokenValue = csrf.getToken();
        }
    }

    @Post
    public ActionResult save() {
        User currentUser = userService.getCurrentUser();
        String action = ctx.form("action");
        String title = ctx.form("title");
        String content = ctx.form("content");
        String slug = ctx.form("slug");
        String excerpt = ctx.form("excerpt");
        String tags = ctx.form("tags");
        String coverImage = ctx.form("coverImage");

        if (title == null || title.isBlank()) {
            error = "Title is required.";
            return ActionResult.render();
        }

        Set<Tag> tagSet = new HashSet<>();
        if (tags != null && !tags.isBlank()) {
            tagSet = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(tagService::findOrCreate)
                    .collect(Collectors.toSet());
        }

        lontar.model.Post newPost = postService.create(title, content, currentUser);
        if (slug != null && !slug.isBlank()) {
            newPost.setSlug(slug);
        }
        if (excerpt != null && !excerpt.isBlank()) {
            newPost.setExcerpt(excerpt);
        }
        newPost.setTags(tagSet);
        newPost.setCoverImage(coverImage != null && !coverImage.isBlank() ? coverImage : null);

        if ("publish".equals(action)) {
            postService.publish(newPost);
        }
        postService.update(newPost);

        return ActionResult.redirect("/admin/editor/" + newPost.getId());
    }
}
