# Lontar — Blog Engine Requirements

> Ghost/Medium-inspired blog engine built with Candi framework.
> Reference app for Candi + production-usable blog.

---

## 1. Tech Stack

| Layer | Tech |
|-------|------|
| Framework | Candi (pages, routing, SSR) |
| Backend | Spring Boot 3 + Spring Data JPA |
| DB | SQLite (swappable to PostgreSQL via config) |
| File storage | S3 / MinIO (interchangeable via config) |
| CSS | Tailwind CSS + `@tailwindcss/typography` |
| Fonts | Inter (UI) + Lora (article body) |
| Icons | Lucide (inline SVG) |
| Interactivity | Alpine.js |
| Rich Editor | CKEditor 5 (standalone build) |
| Code highlighting | Prism.js |
| Image zoom | medium-zoom |
| Date display | Day.js |
| Auth | Google OAuth 2.0 + Email/Password + invite system |

---

## 2. Data Model

### 2.1 User

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | PK |
| email | String | Unique |
| password | String | Hashed (BCrypt), nullable if Google-only |
| name | String | Display name |
| avatar | String | URL (from Google or uploaded) |
| bio | Text | Short author bio |
| role | Enum | `OWNER`, `ADMIN`, `WRITER` |
| authProvider | Enum | `LOCAL`, `GOOGLE`, `BOTH` |
| createdAt | Timestamp | |
| lastLoginAt | Timestamp | |

**Roles:**
- `OWNER` — Full access, can manage users, settings, invites
- `ADMIN` — Can manage all posts, users, settings
- `WRITER` — Can create/edit/delete own posts only

### 2.2 Post

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | PK |
| title | String | Required |
| slug | String | Unique, auto-generated from title, editable |
| content | Text | HTML from CKEditor |
| excerpt | Text | Auto-generated (first 200 chars stripped) or manual |
| coverImage | String | URL to S3/MinIO |
| status | Enum | `DRAFT`, `PUBLISHED` |
| author | User | FK |
| publishedAt | Timestamp | Set when status → PUBLISHED |
| createdAt | Timestamp | |
| updatedAt | Timestamp | |

### 2.3 Tag

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | PK |
| name | String | Display name (e.g. "Spring Boot") |
| slug | String | Unique, URL-safe (e.g. "spring-boot") |
| description | Text | Optional |

### 2.4 PostTag (join table)

| Field | Type |
|-------|------|
| postId | UUID FK |
| tagId | UUID FK |

### 2.5 Comment

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | PK |
| post | Post | FK |
| authorName | String | Display name (guest) |
| authorEmail | String | For gravatar (not displayed) |
| content | Text | Plain text or limited markdown |
| approved | Boolean | Default true (moderation optional) |
| createdAt | Timestamp | |

### 2.6 Invite

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | PK |
| email | String | Invited email |
| role | Enum | Role to assign on accept |
| token | String | Unique invite token |
| invitedBy | User | FK |
| accepted | Boolean | |
| createdAt | Timestamp | |
| expiresAt | Timestamp | 7 days default |

### 2.7 SiteSettings (singleton)

| Field | Type | Notes |
|-------|------|-------|
| id | Long | Always 1 |
| title | String | Blog title |
| description | String | Blog tagline |
| logo | String | URL |
| favicon | String | URL |
| postsPerPage | Integer | Default 10 |
| allowComments | Boolean | Default true |

---

## 3. Authentication & Authorization

### 3.1 First-time Setup (Owner)

Runs once when blog has zero users in the database.

1. Any route detects no users → redirect to `/setup`
2. Setup page: enter name, email, set password
3. First user is automatically assigned role `OWNER`, `authProvider=LOCAL`
4. Session created → redirect to `/admin`

### 3.2 Login Page

Login page (`/login`) shows two options:
- Email + password form
- "Sign in with Google" button

### 3.3 Email/Password Login

1. User enters email + password on login page
2. Backend: find user by email → BCrypt compare password
3. Success → create session, redirect to `/admin`
4. Failure → show error "Invalid email or password"
5. No public registration — account must be created via invite first

### 3.4 Google OAuth 2.0 Login

1. User clicks "Sign in with Google" on login page
2. Redirect to Google consent screen
3. Google redirects back to `/oauth/callback` with auth code
4. Backend exchanges code for tokens, gets user profile (email, name, avatar)
5. Lookup by email:
   - **User exists** → login, update lastLoginAt. If `authProvider=LOCAL` → upgrade to `BOTH` (link Google account)
   - **User doesn't exist, pending invite exists** → create user with invited role, `authProvider=GOOGLE`, consume invite
   - **User doesn't exist, no invite** → reject: "You need an invitation to join"

### 3.5 Invite System

**Sending invites (Owner/Admin):**
1. Go to `/admin/team` → click "Invite"
2. Enter email + select role (Writer / Admin)
3. Backend creates Invite record (token, 7-day expiry)
4. Invite link generated: `/invite/{token}`
5. Link displayed for copy (email notification optional for MVP)

**Accepting invites:**
1. Invitee opens `/invite/{token}`
2. Backend validates: token exists, not expired, not yet accepted
3. Page shows two options:
   - **Option A:** Fill name + set password → user created with `authProvider=LOCAL`
   - **Option B:** Click "Sign in with Google" → OAuth flow → user created with `authProvider=GOOGLE`
4. Invite marked `accepted=true`
5. User logged in → redirect to `/admin`

### 3.6 Forgot / Reset Password

1. Login page → click "Forgot password?" → `/forgot-password`
2. Enter email → submit
3. Backend:
   - If user exists with password (LOCAL/BOTH) → generate reset token, send email with `/reset-password/{token}` link
   - If user is Google-only → show message "This account uses Google sign-in"
   - If email not found → same success message (prevent enumeration)
4. User opens reset link → enter new password → submit
5. Password updated → redirect to `/login`

### 3.7 Link Auth Method (Profile)

Users can add a second login method from profile settings:
- Google-only user → set password → `authProvider` becomes `BOTH`
- Password-only user → link Google account → `authProvider` becomes `BOTH`
- User with `BOTH` can use either method to log in

### 3.8 Team Management

Owner/Admin actions at `/admin/team`:
- **Change role** — Owner can change any role. Admin can only manage Writers.
- **Remove user** — Revoke access. User's posts remain (attributed to original author).
- **Revoke invite** — Cancel pending invite before it's accepted.

### 3.9 Role Permissions

| Action | Owner | Admin | Writer |
|--------|-------|-------|--------|
| Create/edit/delete own posts | yes | yes | yes |
| Edit/delete any post | yes | yes | no |
| Manage comments | yes | yes | no |
| Manage tags | yes | yes | no |
| Invite users | yes | yes | no |
| Change user roles | yes | Writers only | no |
| Remove users | yes | Writers only | no |
| Site settings | yes | yes | no |
| Transfer ownership | yes | no | no |

### 3.10 Session Management

- Spring Security session-based auth
- Remember me cookie (30 days)
- Session stored server-side (default Spring session)
- Password: BCrypt hashing, minimum 8 characters

---

## 4. Features

### 4.1 Public Blog (Reader-facing)

#### Homepage (`/`)
- Hero section with blog title + description
- Post list: cover image, title, excerpt, author avatar + name, date, tags
- Pagination (configurable posts per page)
- Clean, Ghost/Medium aesthetic

#### Post Page (`/post/{slug}`)
- Full article with typography styling (`prose` class)
- Cover image (full-width, medium-zoom on click)
- Author card (avatar, name, bio)
- Published date (relative via Day.js, absolute on hover)
- Tags as clickable chips
- Code blocks highlighted via Prism.js
- Comments section at bottom
- Share buttons (copy link, Twitter/X)
- Related posts (same tags)

#### Tag Page (`/tag/{slug}`)
- Tag name + description
- Filtered post list (same layout as homepage)
- Pagination

#### Author Page (`/author/{slug}`)
- Author profile (avatar, name, bio)
- Posts by this author
- Pagination

#### Search (`/search?q=...`)
- Full-text search across post title + content
- Search results with highlighted matches
- Debounced search input (Alpine.js)

#### RSS Feed (`/rss`)
- Standard RSS 2.0 XML
- Includes latest N published posts
- Title, description, link, pubDate, content

### 4.2 Admin Panel (Writer-facing)

All admin routes under `/admin/*`, require authentication.

#### Dashboard (`/admin`)
- Quick stats: total posts, published, drafts, comments
- Recent activity
- Quick actions: new post, view blog

#### Post Editor (`/admin/editor` / `/admin/editor/{id}`)
- **Ghost-style distraction-free editor**
- CKEditor 5 with:
  - Headings (H2, H3, H4)
  - Bold, italic, strikethrough
  - Links
  - Ordered/unordered lists
  - Blockquote
  - Code block (with language selector)
  - Image upload (drag & drop, paste, browse → S3/MinIO)
  - Embed (YouTube, Twitter)
- Sidebar panel (toggle):
  - Title
  - Slug (auto-generated, editable)
  - Excerpt (auto or manual)
  - Cover image upload
  - Tags (autocomplete, create new inline)
  - Author (for Admin/Owner: can reassign)
  - Status toggle: Draft / Published
  - Publish date (defaults to now)
- Auto-save draft: Alpine.js submits form to editor page POST every 30s (background fetch, no page reload), Candi action saves and returns current state
- Publish / Update / Unpublish buttons (form submit → redirect)

#### Post List (`/admin/posts`)
- Table/list view of all posts (Admin/Owner: all, Writer: own)
- Columns: title, status, author, date, tags
- Filter by: status (all/draft/published), author, tag
- Sort by: date, title
- Bulk actions: delete, publish, unpublish (Admin/Owner only)

#### Comments (`/admin/comments`)
- List all comments across posts
- Approve / delete actions
- Filter by post

#### Tags (`/admin/tags`)
- List all tags with post count
- Create / edit / delete tags
- Merge tags (Admin/Owner)

#### Team (`/admin/team`)
- List all users with role
- Invite new user (email + role)
- Change user role (Owner only)
- Revoke access / remove user
- Pending invites list

#### Settings (`/admin/settings`)
- Blog title, description, logo, favicon
- Posts per page
- Enable/disable comments
- S3/MinIO configuration (bucket, endpoint, credentials)

---

## 5. File Storage (S3/MinIO)

### 5.1 Interface

```
FileStorageService
  ├── upload(file, path) → URL
  ├── delete(path) → void
  └── getUrl(path) → String
```

### 5.2 Implementations

- `S3FileStorageService` — AWS S3
- `MinioFileStorageService` — MinIO (S3-compatible)

Switchable via `application.properties`:
```properties
lontar.storage.type=minio  # or s3
lontar.storage.endpoint=http://localhost:9000
lontar.storage.bucket=lontar
lontar.storage.access-key=...
lontar.storage.secret-key=...
```

### 5.3 Upload Flow

1. User drops/selects image in CKEditor or cover image field
2. CKEditor/Alpine.js sends multipart POST to `/admin/upload`
3. Backend validates (type: jpg/png/gif/webp, size limit 10MB)
4. Stores to S3/MinIO under `images/{year}/{month}/{uuid}.{ext}`
5. Returns JSON `{ "url": "https://..." }`
6. CKEditor inserts `<img>` with returned URL

> Note: This is the only non-SSR endpoint — required because CKEditor's
> upload adapter needs an async JSON response. All other operations are
> full SSR via Candi page actions.

---

## 6. Routes Overview

### Public (Candi pages)

| Route | Method | Page | Description |
|-------|--------|------|-------------|
| `/` | GET | IndexPage | Homepage, post list (paginated) |
| `/post/{slug}` | GET | PostPage | Single post + comments |
| `/post/{slug}` | POST | PostPage | Submit comment |
| `/tag/{slug}` | GET | TagPage | Posts filtered by tag |
| `/author/{slug}` | GET | AuthorPage | Posts filtered by author |
| `/search` | GET | SearchPage | Search results (`?q=...`) |
| `/rss` | GET | — | RSS 2.0 feed (XML) |
| `/sitemap.xml` | GET | — | Sitemap |
| `/setup` | GET | SetupPage | First-time owner setup |
| `/setup` | POST | SetupPage | Create owner account |
| `/login` | GET | LoginPage | Login form (email/password + Google) |
| `/login` | POST | LoginPage | Email/password auth |
| `/logout` | GET | — | Destroy session, redirect to `/` |
| `/oauth/callback` | GET | — | Google OAuth callback |
| `/invite/{token}` | GET | InvitePage | Accept invite form |
| `/invite/{token}` | POST | InvitePage | Submit invite acceptance |
| `/forgot-password` | GET | ForgotPasswordPage | Request password reset form |
| `/forgot-password` | POST | ForgotPasswordPage | Send reset email |
| `/reset-password/{token}` | GET | ResetPasswordPage | New password form |
| `/reset-password/{token}` | POST | ResetPasswordPage | Submit new password |

### Admin (Candi pages, authenticated)

| Route | Method | Page | Description |
|-------|--------|------|-------------|
| `/admin` | GET | DashboardPage | Admin dashboard |
| `/admin/editor` | GET | EditorPage | New post form |
| `/admin/editor` | POST | EditorPage | Create post (save/publish) |
| `/admin/editor/{id}` | GET | EditorPage | Edit post form |
| `/admin/editor/{id}` | POST | EditorPage | Update post |
| `/admin/editor/{id}` | DELETE | EditorPage | Delete post |
| `/admin/posts` | GET | PostListPage | List posts (filter/sort/paginate) |
| `/admin/posts/bulk` | POST | PostListPage | Bulk publish/unpublish/delete |
| `/admin/comments` | GET | CommentsPage | List comments |
| `/admin/comments/{id}/approve` | POST | CommentsPage | Approve comment |
| `/admin/comments/{id}` | DELETE | CommentsPage | Delete comment |
| `/admin/tags` | GET | TagsPage | List tags |
| `/admin/tags` | POST | TagsPage | Create tag |
| `/admin/tags/{id}` | POST | TagsPage | Update tag |
| `/admin/tags/{id}` | DELETE | TagsPage | Delete tag |
| `/admin/team` | GET | TeamPage | List users + invites |
| `/admin/team/invite` | POST | TeamPage | Send invite |
| `/admin/team/invite/{id}` | DELETE | TeamPage | Revoke invite |
| `/admin/team/{id}/role` | POST | TeamPage | Change user role |
| `/admin/team/{id}` | DELETE | TeamPage | Remove user |
| `/admin/settings` | GET | SettingsPage | Blog settings form |
| `/admin/settings` | POST | SettingsPage | Update settings |
| `/admin/profile` | GET | ProfilePage | Edit profile form |
| `/admin/profile` | POST | ProfilePage | Update profile / link auth |

### Upload Handler (minimal, for CKEditor)

| Route | Method | Description |
|-------|--------|-------------|
| `/admin/upload` | POST | CKEditor image upload (multipart → S3/MinIO, returns JSON `{ url }`) |

---

## 7. SEO

- `<title>` — dynamic per page (post title, tag name, etc.)
- `<meta name="description">` — post excerpt or blog description
- Open Graph tags (`og:title`, `og:description`, `og:image`, `og:url`, `og:type`)
- Twitter Card tags (`twitter:card`, `twitter:title`, `twitter:description`, `twitter:image`)
- Canonical URL (`<link rel="canonical">`)
- Structured data (JSON-LD: BlogPosting schema)
- Sitemap.xml (`/sitemap.xml`)
- robots.txt

---

## 8. Non-Functional Requirements

| Concern | Requirement |
|---------|-------------|
| Performance | Pages load < 200ms server-side render |
| Mobile | Fully responsive (Tailwind breakpoints) |
| Dark mode | System-preference + manual toggle, persisted |
| Accessibility | Semantic HTML, ARIA where needed, keyboard nav |
| Security | CSRF protection, XSS prevention (Candi auto-escapes), rate limiting on comments/auth |
| Image optimization | Resize on upload (thumbnail, medium, original) |
| Database | SQLite default, PostgreSQL via config swap |
| Deployment | Single JAR, Docker-ready |

---

## 9. Implementation Phases

### Phase 1 — Foundation
- [ ] Data model (JPA entities)
- [ ] Spring Security + email/password auth + Google OAuth
- [ ] First-time setup flow (owner creation)
- [ ] File storage service (S3/MinIO)
- [ ] Tailwind CSS setup + base layout
- [ ] Basic public pages: index, post

### Phase 2 — Admin Core
- [ ] Admin layout + dashboard
- [ ] Post editor with CKEditor 5
- [ ] Post list + CRUD
- [ ] Image upload integration
- [ ] Auto-save drafts

### Phase 3 — Content Features
- [ ] Tags system
- [ ] Search (full-text)
- [ ] Comments
- [ ] Pagination
- [ ] Author pages

### Phase 4 — Team & Settings
- [ ] Invite system (send + accept)
- [ ] Team management (roles, remove)
- [ ] Site settings
- [ ] Role-based access control
- [ ] Profile page (edit bio, avatar, link auth methods)
- [ ] Forgot / reset password flow

### Phase 5 — Polish
- [ ] SEO (meta tags, Open Graph, JSON-LD, sitemap)
- [ ] RSS feed
- [ ] Dark mode
- [ ] Image optimization (resize on upload)
- [ ] Mobile responsiveness fine-tuning
- [ ] Error pages (404, 403, 500)
