# Implemented So Far

---

## Task 1: External REST API Integration (Dream Generation)

**Goal:** Build the Dream Creation Screen UI. Integrate an external text-to-image REST API, handle loading states, and display the generated dream image.

### Status: ✅ Done

### What needs to be done:
- Build `CreateDreamFragment` UI (text input + Visualize button + image preview)
- Integrate external text-to-image API (e.g. DALL·E 3 or Pollinations)
- Send network request off the main thread via `ExecutorService`
- Handle `LoadingState` (LOADING / IDLE / ERROR) with a spinner
- Display the generated image on success
- Show user-facing error with retry option on failure

---

## Task 2: Remote Database Setup (Firebase Firestore)

**Goal:** Configure Firebase Firestore, define the `DreamPost` document structure, and implement read/write operations.

### Status: ✅ Done

### What was implemented:

#### Build Configuration
- `gradle/libs.versions.toml` — added Firebase BOM, google-services plugin, Kotlin, Navigation, Glide, OkHttp, Lifecycle versions and library aliases
- `build.gradle.kts` (root) — applied `kotlin-android` and `google-services` plugins
- `app/build.gradle.kts` — applied all plugins, enabled `viewBinding`, added `kotlinOptions`, added Firebase BOM + Firestore dependencies

#### Application Class
- `base/DreamCatcherApplication.kt` — Application class with global `appContext` and a 4-thread `ExecutorService`
- `AndroidManifest.xml` — registered `DreamCatcherApplication`, added `INTERNET` permission

#### Data Model
- `model/DreamPost.kt` — data class representing a post in both Room (future) and Firestore
  - Fields: `postId`, `authorUid`, `authorNickname`, `authorProfilePicUrl`, `title`, `description`, `imageUrl`, `createdAt`, `lastUpdated`
  - `companion object` with Firestore field key constants and `fromJson()` factory
  - `toJson` property for Firestore writes

#### Firebase Layer
- `model/FirebaseModel.kt` — Firestore operations:
  - `addPost(post, callback)` — writes to `posts/{postId}`
  - `getAllPosts(since, callback)` — delta sync query (`lastUpdated > since`)
  - `getPostsByUser(uid, callback)` — filters posts by `authorUid`

#### Repository
- `model/DreamCatcherModel.kt` — singleton repository wiring `FirebaseModel` with `ExecutorService` threading and main-thread result posting via `Handler(Looper.getMainLooper())`

#### Firebase Console
- Project created: `dreamcatcher-b45a4`
- Firestore enabled in Native mode
- Security rules applied: authenticated read for all, write restricted to document owner
- `google-services.json` placed in `app/`
