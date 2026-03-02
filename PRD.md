# PRD: DreamCatcher
**Authors:** Stav Barel & Rom Matetzky
**Platform:** Android Native (Kotlin)
**Version:** 1.0 MVP
**Date:** March 2026

---

## 1. Executive Summary

DreamCatcher is an Android application that enables users to record their dreams as text descriptions and instantly transform them into AI-generated artwork. By connecting to an external image generation API, the app bridges the gap between imagination and visual reality, allowing users to build a personal "Dream Journal" and share their subconscious creativity with a community of dreamers.

The app combines cloud-based social features (Firebase Firestore, Firebase Auth, Firebase Storage) with robust offline support (Room local database), ensuring users can always access their own dream history regardless of connectivity. The social feed gives the experience a community dimension, where users discover and are inspired by each other's dreams.

The MVP goal is a fully functional Android app that allows a user to register, generate a dream image from a text description using an external AI API, post it to a shared social feed, and view their personal journal offline — all within a polished, lifecycle-aware MVVM architecture.

---

## 2. Mission

> **Empower people to visualize and share the stories their sleeping mind creates.**

### Core Principles

1. **Creativity First** — The dream creation flow must be fast, delightful, and friction-free.
2. **Offline Reliability** — Personal dream history is always accessible, even without internet.
3. **Community** — Dreams gain meaning when shared; the social feed is a first-class feature.
4. **Clean Architecture** — MVVM, Single Source of Truth (Room), and Delta Sync are non-negotiable patterns.
5. **Simplicity** — The MVP does one thing well: capture, visualize, and share dreams.

---

## 3. Target Users

### Primary Persona: The Creative Dreamer
- **Name:** Alex, 22, college student
- **Tech comfort:** Moderate — comfortable with social apps (Instagram, TikTok)
- **Needs:** A quick way to capture a vivid dream before forgetting it, see what it "looks like," and share it with others
- **Pain points:** Dreams fade fast in the morning; no tool currently bridges text memory → visual art → social sharing

### Secondary Persona: The Curious Explorer
- **Name:** Dana, 28, creative professional
- **Tech comfort:** High
- **Needs:** Browses the feed for visual inspiration, occasionally posts their own dreams
- **Pain points:** Wants a dedicated space for dream content, not lost in a general-purpose social app

---

## 4. MVP Scope

### Core Functionality
- ✅ User registration with email and password (Firebase Auth)
- ✅ User login and logout
- ✅ Basic profile creation: nickname and profile picture (camera + gallery)
- ✅ Text-to-image generation via external AI API (DALL·E 3 or equivalent)
- ✅ Post creation: generated image + dream description saved to Firestore
- ✅ Social feed displaying all users' posts (image + description + author)
- ✅ My Journal screen backed by Room local database (offline access)
- ✅ Delta Sync: Room as Single Source of Truth, synced from Firestore
- ✅ Splash / Onboarding screen
- ✅ Loading states and error feedback throughout the app

### Out of Scope (Future Phases)
- ❌ Like / comment on posts
- ❌ Dream interpretation via Text AI API
- ❌ Push notifications
- ❌ Search or filter dreams
- ❌ Follow / following system
- ❌ Geolocation tagging of dreams
- ❌ Dark mode / theming
- ❌ Web or iOS version

---

## 5. User Stories

| # | Story | Example |
|---|-------|---------|
| 1 | As a new user, I want to register with my email and password so that I can create a personal account. | Alex downloads the app, enters his email + password + nickname, and is taken to the feed. |
| 2 | As a returning user, I want to log in and be taken directly to the feed so that I can immediately see new dreams. | Dana opens the app, taps Login, and sees today's community posts. |
| 3 | As a user, I want to type a description of my dream and have the app generate an image so that I can visualize it. | Alex types "a neon cat riding a skateboard in space" and taps Visualize. An image appears within seconds. |
| 4 | As a user, I want to give my dream a title and post it to the community feed so that others can see it. | Alex adds the title "Space Kitty" and taps Post. The post appears at the top of the feed. |
| 5 | As a user browsing the feed, I want to see other users' dream images and descriptions so that I can explore the community's imagination. | Dana scrolls the vertical feed and sees colorful dream posts from multiple users. |
| 6 | As a user with no internet, I want to open My Journal and see all my past dreams so that I can access my content offline. | On the subway, Alex opens My Journal and sees "Space Kitty" with its image, loaded from Room. |
| 7 | As a user, I want to set or update my profile picture from my camera or gallery so that my profile feels personal. | Stav taps Edit Profile, picks a photo from her gallery, and her avatar updates across the app. |
| 8 | As a user, I want to see a loading indicator while the AI is generating my image so that I know the app is working. | After tapping Visualize, a spinner plays until the image is ready. |

---

## 6. Core Architecture & Patterns

### Architecture: MVVM + Repository

```
UI Layer (Fragments + ViewBinding)
        ↕  observe LiveData / call functions
ViewModel Layer (business logic, no Android imports)
        ↕  calls repository methods
Repository Layer (Single Source of Truth)
        ↕                    ↕
Room (Local DB)       Firebase (Remote DB / Auth / Storage)
```

### Single Source of Truth — Delta Sync

All data flows **through Room**. The UI never reads directly from Firestore.

```
UI ◄── observes ── Room ◄── syncs ── Firestore
```

- Room is the cache and offline store.
- Firestore is the master remote source.
- Sync only fetches records where `lastUpdated > localLastSync` (delta fetch).
- `lastUpdated: Long` is required on every Room `@Entity` and Firestore document.

### Threading

- A centralized `ExecutorService` (4 threads) is initialized in the `Application` class.
- All database and network operations run on the executor.
- Results are posted back to the UI via `Handler(Looper.getMainLooper())`.
- **No** manual `Thread { }` blocks anywhere in the codebase.

### Directory Structure

```
app/src/main/java/com/dreamcatcher/
├── Model/
│   ├── DreamPost.kt            # @Entity + Firestore data class
│   ├── User.kt                 # User profile data class
│   ├── FirebaseModel.kt        # All Firebase operations
│   ├── DreamCatcherModel.kt    # Singleton repository (Single Source of Truth)
│   └── dao/
│       └── DreamPostDao.kt     # Room DAO
├── ViewModel/
│   ├── FeedViewModel.kt
│   ├── CreateDreamViewModel.kt
│   ├── JournalViewModel.kt
│   └── ProfileViewModel.kt
├── View/
│   ├── MainActivity.kt         # Single Activity host
│   ├── SplashFragment.kt
│   ├── LoginFragment.kt
│   ├── RegisterFragment.kt
│   ├── FeedFragment.kt
│   ├── CreateDreamFragment.kt
│   ├── JournalFragment.kt
│   └── ProfileFragment.kt
├── Utils/
│   ├── LocalSyncManager.kt     # SharedPreferences for delta sync timestamp
│   └── ImageUploadHelper.kt    # Firebase Storage bitmap upload
└── AppDatabase.kt              # Room database definition
```

---

## 7. Feature Specifications

### 7.1 Authentication (Login / Register / Logout)

- Firebase Authentication with email + password.
- On first launch: Register screen (nickname, email, password, optional profile photo).
- On subsequent launches: Login screen with "Remember me" behavior (Firebase persists session).
- Logout clears the local session; user returns to Login screen.
- Basic validation: non-empty fields, valid email format, password ≥ 6 characters.

### 7.2 User Profile

- Profile data stored in Firestore under `users/{uid}`.
- Fields: `uid`, `nickname`, `profilePictureUrl`, `createdAt`.
- Profile picture set via:
  - Camera: `ActivityResultContracts.TakePicturePreview()` — never `startActivityForResult`.
  - Gallery: `ActivityResultContracts.GetContent()` with `"image/*"` MIME type.
- Bitmap uploaded to Firebase Storage at `profile_images/{uid}.jpg`.
- Download URL stored in Firestore user document.

### 7.3 AI Image Generation

- User inputs a free-text dream description (max ~500 characters).
- App calls an external image generation API (DALL·E 3 recommended, or equivalent).
- Request is made off the main thread via `ExecutorService`.
- A `LoadingState.LOADING` LiveData event shows a spinner during the API call.
- On success: generated image is displayed in a preview before posting.
- On failure: user-facing error message with retry option.
- The generated image URL (or bitmap) is stored in Firebase Storage before posting.

### 7.4 Post Creation

- After image preview, user can add an optional title.
- Tapping "Post" uploads the image to Firebase Storage and saves the post document to Firestore.
- Firestore document structure:

```json
{
  "postId": "auto-generated",
  "authorUid": "abc123",
  "authorNickname": "Alex",
  "authorProfilePicUrl": "https://...",
  "title": "Space Kitty",
  "description": "a neon cat riding a skateboard in space",
  "imageUrl": "https://...",
  "createdAt": 1700000000000,
  "lastUpdated": 1700000000000
}
```

- After successful post, app navigates to the Social Feed.

### 7.5 Social Feed

- Vertical scrolling `RecyclerView` with one card per post.
- Each card shows: dream image, title, description (truncated), author nickname + avatar, timestamp.
- Feed is populated by observing a `LiveData<List<DreamPost>>` from the ViewModel.
- Data source is Room (synced from Firestore via delta sync).
- SwipeRefreshLayout triggers a manual sync.
- `LoadingState` LiveData drives the refresh spinner.

### 7.6 My Journal (Offline)

- Displays only the **current user's** posts, read directly from Room.
- No network call needed — fully offline.
- Same card layout as the feed.
- Posts are written to Room at creation time, so they are available immediately offline.

### 7.7 Splash / Onboarding

- Displays app logo/name briefly.
- Checks Firebase Auth state:
  - If logged in → navigate to Feed.
  - If not logged in → navigate to Login.

---

## 8. Technology Stack

| Layer | Technology | Notes |
|-------|-----------|-------|
| Language | Kotlin | Primary language |
| Min SDK | API 24 (Android 7.0) | Covers ~95% of active devices |
| Target SDK | API 35 | Latest stable |
| UI | XML Layouts + ViewBinding | No Jetpack Compose for MVP |
| Navigation | Jetpack Navigation Component | Single Activity with NavGraph |
| Architecture | MVVM + Repository | Per university requirement |
| Local DB | Room 2.x | Offline journal + delta sync cache |
| Remote DB | Firebase Firestore | Cloud data store |
| Auth | Firebase Authentication | Email/password |
| Storage | Firebase Storage | Profile pics + dream images |
| Threading | Java ExecutorService (4 threads) | Centralized in Application class |
| LiveData | AndroidX LiveData | UI state management |
| Image Loading | Glide or Picasso | Load remote image URLs into ImageViews |
| AI Image API | DALL·E 3 (OpenAI) or equivalent | HTTP call via OkHttp or Retrofit |
| HTTP Client | Retrofit 2 + OkHttp | API communication |
| Camera/Gallery | ActivityResultContracts | `TakePicturePreview` + `GetContent` |

### Gradle Dependencies (key)

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.x"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-storage-ktx")

// Room
implementation("androidx.room:room-runtime:2.6.x")
kapt("androidx.room:room-compiler:2.6.x")

// Retrofit
implementation("com.squareup.retrofit2:retrofit:2.x")
implementation("com.squareup.retrofit2:converter-gson:2.x")

// Glide
implementation("com.github.bumptech.glide:glide:4.x")

// Navigation
implementation("androidx.navigation:navigation-fragment-ktx:2.x")
implementation("androidx.navigation:navigation-ui-ktx:2.x")
```

---

## 9. Security & Configuration

### Authentication
- Firebase Auth manages all tokens; the app never stores raw passwords.
- Auth state is persisted by Firebase SDK across app restarts.
- All Firestore and Storage access rules require authenticated users (`request.auth != null`).

### Firestore Security Rules (minimum)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /posts/{postId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == resource.data.authorUid;
    }
    match /users/{uid} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

### API Key Management
- AI API key stored in `local.properties` (not committed to git).
- Accessed via `BuildConfig` field injected at build time.
- `local.properties` and `google-services.json` are in `.gitignore`.

### Out-of-Scope Security
- ❌ End-to-end encryption of dream content
- ❌ Rate limiting on AI API calls
- ❌ Content moderation of generated images

---

## 10. Data Models

### DreamPost (Room Entity + Firestore Document)

```kotlin
@Entity(tableName = "dream_posts")
data class DreamPost(
    @PrimaryKey val postId: String,
    val authorUid: String,
    val authorNickname: String,
    val authorProfilePicUrl: String?,
    val title: String,
    val description: String,
    val imageUrl: String,
    val createdAt: Long,
    var lastUpdated: Long
)
```

### User

```kotlin
data class User(
    val uid: String = "",
    val nickname: String = "",
    val profilePictureUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

---

## 11. Success Criteria

### MVP is successful when:

- ✅ A new user can register with email, password, and nickname
- ✅ A logged-in user can type a dream description and receive an AI-generated image
- ✅ A user can post a dream (title + description + image) to the shared feed
- ✅ The social feed displays posts from all users with image, title, description, and author
- ✅ My Journal shows the user's own dreams when offline (no internet connection)
- ✅ Delta sync correctly fetches only new/updated posts since last sync
- ✅ Profile picture can be set from both camera and gallery
- ✅ Loading states are shown during AI generation and data sync
- ✅ App does not crash on rotation, backgrounding, or loss of connectivity

### Quality Indicators
- No ANR (Application Not Responding) — all network/DB work off main thread
- Images load smoothly via Glide with placeholder/error states
- Room schema exported and versioned
- No Firebase API keys or secrets in source control

---

## 12. Implementation Phases

### Phase 1 — Foundation (Days 1–3)
**Goal:** Project skeleton, auth, and navigation working end-to-end.

- ✅ Set up Android project with MVVM structure and Navigation Component
- ✅ Configure Firebase (Auth, Firestore, Storage) and `google-services.json`
- ✅ Implement Splash → Login → Register flow with Firebase Auth
- ✅ Create `AppDatabase` with Room, export schema
- ✅ Create `Application` class with `ExecutorService`
- ✅ Implement basic User profile (nickname stored in Firestore on register)

**Validation:** User can register, log in, and log out. Firebase console shows new user records.

---

### Phase 2 — Core Feature: Dream Creation (Days 4–8)
**Goal:** The primary value proposition works — type a dream, get an image, post it.

- ✅ Integrate AI image generation API (Retrofit + API key via BuildConfig)
- ✅ Build `CreateDreamFragment`: text input → Visualize button → loading → image preview
- ✅ Implement Firebase Storage upload for generated images
- ✅ Implement post creation: save to Firestore + insert to Room
- ✅ `LoadingState` LiveData drives spinner during API call
- ✅ Error handling with user-visible messages

**Validation:** End-to-end dream creation posts to Firestore; post visible in Firebase console.

---

### Phase 3 — Feed & Journal (Days 9–12)
**Goal:** Social feed and offline journal both work correctly.

- ✅ Build `FeedFragment` with `RecyclerView` + card layout (image, title, description, author)
- ✅ Implement delta sync: `LocalSyncManager` + Firestore `lastUpdated` query
- ✅ Room LiveData drives feed; SwipeRefreshLayout triggers sync
- ✅ Build `JournalFragment`: filter Room posts by current user UID
- ✅ Offline test: disable network, confirm My Journal still loads
- ✅ Glide integration for image loading with placeholder

**Validation:** Feed shows posts from multiple accounts. Journal works with airplane mode on.

---

### Phase 4 — Profile, Polish & Testing (Days 13–14)
**Goal:** Profile picture flow complete; app is stable and presentable.

- ✅ `ProfileFragment`: display nickname, avatar, user's post count
- ✅ Camera capture via `ActivityResultContracts.TakePicturePreview()`
- ✅ Gallery selection via `ActivityResultContracts.GetContent()`
- ✅ Upload profile picture to Firebase Storage, update Firestore user document
- ✅ Firestore security rules reviewed and applied
- ✅ End-to-end walkthrough of all user stories
- ✅ Fix rotation/lifecycle edge cases
- ✅ Code review and cleanup

**Validation:** Full "Morning After" use case flow completes without errors.

---

## 13. Future Considerations

### Social Interaction (Post-MVP)
- Like button per post (Firestore `likes` subcollection or counter field)
- Comments thread per post
- Notification when someone likes your dream

### Dream Interpretation
- Second AI call using a Text API (e.g., Claude, GPT-4) to generate a psychological interpretation of the dream description
- Display interpretation as expandable text below the post

### Enhanced Feed
- Filter by date, popularity, or tags
- Search dreams by keyword
- Infinite scroll pagination (Firestore cursors)

### Profile Enhancements
- Edit nickname post-registration
- View another user's profile and their public posts
- Dream streak counter (consecutive days posting)

---

## 14. Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| AI API latency / cost overrun | Medium | High | Set a request timeout (30s); show error with retry. Use a free-tier API (Stability AI) as fallback if DALL·E costs are prohibitive. |
| Firebase quota exhaustion during demo/grading | Low | High | Keep image sizes compressed (JPEG, quality 80). Limit storage to generated + profile images only. |
| Delta sync produces stale feed | Medium | Medium | Always write `lastUpdated = System.currentTimeMillis()` at creation; test sync with multiple accounts. |
| Room migration failure after schema change | Low | Medium | Export Room schema from day 1. Increment version and write a `Migration` object for any schema change. |
| Two-week timeline pressure leading to shortcuts | High | Medium | Implement features in strict phase order. Scope creep is deferred to Future Considerations — not added mid-sprint. |

---

## 15. Appendix

### Key Dependencies Reference
- [Firebase Android SDK](https://firebase.google.com/docs/android/setup)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Jetpack Navigation](https://developer.android.com/guide/navigation)
- [ActivityResultContracts](https://developer.android.com/training/basics/intents/result)
- [Retrofit](https://square.github.io/retrofit/)
- [Glide](https://bumptech.github.io/glide/)
- [OpenAI Images API (DALL·E 3)](https://platform.openai.com/docs/api-reference/images)

### University Standards Applied
This PRD is aligned with the project's `ADVANCED_STANDARDS.md`, which mandates:
- Delta Sync pattern with `lastUpdated` timestamps
- `ExecutorService` for all background work (no raw `Thread {}`)
- `ActivityResultContracts` for camera/gallery (no deprecated `startActivityForResult`)
- Firebase Storage upload pattern via `ByteArrayOutputStream`
- Room schema export enabled in `build.gradle.kts`
- Lifecycle-aware LiveData with `onActive()` / `onInactive()` for Firebase listeners
- `LoadingState` enum for global loading state management
