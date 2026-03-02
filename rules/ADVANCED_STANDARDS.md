# 📘 Advanced Implementation & Feature Standards

> **This document supplements the core standards with specific implementation patterns for synchronization, threading, media capture, and geolocation.**

---

## 📑 Table of Contents

| Section | Description |
|---------|-------------|
| [🔄 Data Synchronization](#-data-synchronization-strategy) | Smart Sync Architecture & Delta Patterns |
| [🧵 Threading & Concurrency](#-threading--concurrency) | Executor Service & Main Thread Handling |
| [📸 Media & Camera](#-media--camera-integration) | Activity Result Contracts & Firebase Storage |
| [📍 GeoLocation](#-geolocation--geohashing) | GeoHashing for Firestore Queries |
| [⚡ LiveData Patterns](#-advanced-livedata-patterns) | Lifecycle-Aware Data & Loading States |
| [🗄️ Room Database](#️-room-database---schema-management) | Schema Export & Migration |

---

## 🔄 Data Synchronization Strategy

### "Smart Sync" Architecture

To minimize network usage and latency, all data repositories must implement the **Delta Sync** pattern using a **"Single Source of Truth"** approach:

```
┌─────────┐      observes      ┌────────────────┐      syncs      ┌─────────────────┐
│   UI    │ ◄───────────────── │ Local Database │ ◄─────────────► │ Remote Database │
└─────────┘                    └────────────────┘                 └─────────────────┘
```

| Component | Role |
|-----------|------|
| **Local Database** | Acts as the cache and offline storage |
| **Remote Database** | Acts as the master data source |
| **Sync Logic** | Only fetch records modified since the last sync |

---

### Timestamp Tracking

> ⚠️ All Entities and Firebase records **must** include a `lastUpdated` timestamp.

#### Entity Requirement

```kotlin
@Entity
data class Student(
    // ... other fields
    var lastUpdated: Long? = null
)
```

#### Shared Preferences for Sync Tracking

Store the timestamp of the last successful sync locally:

```kotlin
// Utils/LocalSyncManager.kt
private const val LAST_UPDATED_KEY = "local_last_updated"

fun getLastUpdated(): Long {
    return sharedPreferences.getLong(LAST_UPDATED_KEY, 0)
}

fun setLastUpdated(time: Long) {
    sharedPreferences.edit().putLong(LAST_UPDATED_KEY, time).apply()
}
```

---

### Delta Fetch Logic

Query Firebase for records where `lastUpdated > localLastUpdated`:

```kotlin
// Repository
fun refreshStudents() {
    val lastLocalUpdate = localSyncManager.getLastUpdated()
    
    firebaseModel.getStudentsSince(lastLocalUpdate) { newStudents ->
        executor.execute {
            // 1. Update Local DB
            studentDao.insertAll(*newStudents.toTypedArray())
            
            // 2. Update Timestamp
            val newTimestamp = System.currentTimeMillis()
            localSyncManager.setLastUpdated(newTimestamp)
        }
    }
}
```

---

## 🧵 Threading & Concurrency

### Executor Service Pattern

> ❌ **Do not** spawn new threads manually.  
> ✅ Use a centralized `ExecutorService` initialized in the Application class.

#### Application Class Setup

```kotlin
class MyApplication : Application() {
    val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    // ...
}
```

#### Main Thread Handler

Use `Handler(Looper.getMainLooper())` to safely pass results from background threads back to the UI.

#### Usage Pattern

```kotlin
fun performBackgroundTask(callback: (ResultType) -> Unit) {
    MyApplication.executorService.execute {
        // 1. Background work
        val result = databaseOperation()
        
        // 2. Post to UI Thread
        Handler(Looper.getMainLooper()).post {
            callback(result)
        }
    }
}
```

---

## 📸 Media & Camera Integration

### Activity Result Contracts

> ⚠️ Use `ActivityResultContracts` for all camera and gallery interactions.  
> ❌ **Do not** use deprecated `startActivityForResult`.

#### Fragment Member Declaration

Declare launchers as member variables to survive configuration changes:

```kotlin
// 1. Camera Capture
private val cameraLauncher = registerForActivityResult(
    ActivityResultContracts.TakePicturePreview()
) { bitmap ->
    bitmap?.let { 
        // Handle captured bitmap
        binding.imageView.setImageBitmap(it) 
    }
}

// 2. Gallery Selection
private val galleryLauncher = registerForActivityResult(
    ActivityResultContracts.GetContent()
) { uri ->
    uri?.let { 
        // Handle selected image URI
        binding.imageView.setImageURI(it) 
    }
}
```

#### Triggering Actions

```kotlin
fun onCaptureClicked() {
    cameraLauncher.launch(null)
}

fun onGalleryClicked() {
    galleryLauncher.launch("image/*")
}
```

---

### Firebase Storage Upload

Standard pattern for uploading bitmaps:

```kotlin
fun uploadImage(bitmap: Bitmap, name: String, callback: (String?) -> Unit) {
    val storageRef = FirebaseStorage.getInstance()
        .reference
        .child("images/$name.jpg")
    
    val baos = ByteArrayOutputStream()
    
    // Compress logic
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()
    
    val uploadTask = storageRef.putBytes(data)
    
    uploadTask
        .addOnFailureListener {
            callback(null)
        }
        .addOnSuccessListener { 
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }
        }
}
```

---

## 📍 GeoLocation & GeoHashing

### GeoHashing for Firestore

> 💡 Since Firestore does not natively support geospatial querying (radius search), use **GeoHashing**.

| Step | Description |
|------|-------------|
| **Encoding** | Convert Latitude/Longitude into a geohash string |
| **Structure** | Store the geohash alongside coordinates |
| **Indexing** | Create a composite index in Firestore on the geohash field |

#### Data Structure

```json
{
  "name": "Restaurant A",
  "coordinates": {
    "latitude": 32.109,
    "longitude": 34.855,
    "geohash": "sv8w" 
  }
}
```

---

### Query Pattern

To find items nearby, calculate the target geohash prefix and query a range:

```kotlin
// Query logic
val centerHash = GeoHash.encode(userLat, userLng, precision = 6)

firestore.collection("locations")
    .orderBy("geohash")
    .startAt(centerHash)
    .endAt(centerHash + "\uf8ff")
    .get()
    .addOnSuccessListener { /* ... */ }
```

> 📌 **Note:** Always filter results by calculated distance **client-side** after fetching to handle rectangular grid edge cases.

---

## ⚡ Advanced LiveData Patterns

### Lifecycle-Aware Data Loading

To manage resources (like Firebase listeners) automatically, extend `MutableLiveData` and override lifecycle methods. This ensures listeners are only active when the UI is visible.

```kotlin
class StudentListLiveData : MutableLiveData<List<Student>>() {
    
    // Called when the first observer activates (onStart/onResume)
    override fun onActive() {
        super.onActive()
        firebaseModel.startListening { data ->
            value = data
        }
    }

    // Called when the last observer leaves (onStop/onDestroy)
    override fun onInactive() {
        super.onInactive()
        firebaseModel.stopListening()
    }
}
```

---

### Global Loading State (Event Bus)

Use an Enum-based LiveData in the Model layer to manage loading spinners across Fragments:

```kotlin
enum class LoadingState {
    LOADING, 
    NOT_LOADING
}

object Model {
    val loadingState = MutableLiveData<LoadingState>()
}
```

#### Swipe-to-Refresh Integration

```kotlin
binding.swipeRefresh.setOnRefreshListener {
    viewModel.refreshData()
}

// Observer to hide spinner
Model.loadingState.observe(viewLifecycleOwner) { state ->
    binding.swipeRefresh.isRefreshing = (state == LoadingState.LOADING)
}
```

---

## 🗄️ Room Database - Schema Management

### Schema Export

Enable schema export to track database version history and debug migration issues.

#### build.gradle.kts

```kotlin
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas"
                )
            }
        }
    }
}
```

> 📁 This generates JSON files in a `/schemas` folder representing your database structure for every version.
