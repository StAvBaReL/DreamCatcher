# Android Development Standards Guide
## Colman 2026 Android Course

This document outlines the development standards and conventions used across all repositories in this workspace.

---

## 📁 Project Structure

### Root-Level Organization
```
ProjectName/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/{company}/{projectname}/
│       │   └── res/
│       ├── androidTest/
│       └── test/
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew / gradlew.bat
```

### Package Structure (Layer-Based)
```
com.{company}.{projectname}/
├── base/                    # Application class, MainActivity, base components
├── model/                   # Data models, Firebase models, API integrations (e.g., Image generators)
├── view/                    # Fragments, Adapters, ViewHolders (UI components)
└── viewmodel/               # ViewModels handling business logic and UI state
```

---

## 🛠 Build Configuration

### Gradle Version Catalog (libs.versions.toml)
All dependencies should be managed via the version catalog:

```toml
[versions]
agp = "8.13.1"
kotlin = "2.0.21"
coreKtx = "1.17.0"
# ... more versions

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
# ... more libraries

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

### App-Level build.gradle.kts Standards
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Additional plugins as needed
}

android {
    namespace = "com.{company}.{projectname}"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.{company}.{projectname}"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true  // Always enable View Binding
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}
```

### SDK Version Standards
| Property | Minimum Value |
|----------|---------------|
| `compileSdk` | 35-36 |
| `targetSdk` | 34-36 |
| `minSdk` | 29-33 |
| `jvmTarget` | "11" |

---

## 🏗 Architecture Patterns

### MVVM Architecture
All features should follow the MVVM (Model-View-ViewModel) pattern:

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    View     │────▶│  ViewModel  │────▶│  Repository │
│ (Fragment)  │◀────│             │◀────│             │
└─────────────┘     └─────────────┘     └─────────────┘
                                              │
                          ┌───────────────────┼───────────────────┐
                          ▼                   ▼                   ▼
                    ┌──────────┐        ┌──────────┐        ┌──────────┐
                    │ Room DB  │        │ Firebase │        │  Remote  │
                    │ (Local)  │        │(Firestore)│       │  Storage │
                    └──────────┘        └──────────┘        └──────────┘
```

> **Smart Sync Architecture ("Single Source of Truth"):**
> The local database acts as the cache and UI source of truth, while the remote database is the master source. Use a **Delta Sync logic** tracking `lastUpdated` timestamps to only fetch modified records.

### Repository Pattern
```kotlin
class StudentsRepository private constructor() {

    private val firebaseModel = FirebaseModel()
    private val database: AppLocalDbRepository = AppLocalDB.db

    companion object {
        val shared = StudentsRepository()  // Singleton instance
    }

    fun getAllStudents(): LiveData<MutableList<Student>> {
        return database.studentDao.getAllStudents()
    }
}
```

---

## 📦 Data Layer

### Room Database Entity
```kotlin
@Entity
data class Student(
    @PrimaryKey
    val id: String,
    val name: String,
    var isPresent: Boolean,
    val avatarUrlString: String?,
    val lastUpdated: Long?
) {
    companion object {
        const val ID_KEY = "id"
        const val NAME_KEY = "name"
        // ... more keys
        
        fun fromJson(json: Map<String, Any?>): Student { /* ... */ }
    }
    
    val toJson: Map<String, Any?>
        get() = hashMapOf(/* ... */)
}
```

### DAO Interface
```kotlin
@Dao
interface StudentDao {
    @Query("SELECT * FROM Student")
    fun getAllStudents(): LiveData<MutableList<Student>>

    @Query("SELECT * FROM Student WHERE id = :id")
    fun getStudentById(id: String): Student

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStudents(vararg students: Student)

    @Delete
    fun deleteStudent(student: Student)
}
```

### Database Singleton
```kotlin
object AppLocalDB {
    val db: AppLocalDbRepository by lazy {
        val context = MyApplication.appContext
            ?: throw IllegalStateException("Context is null")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "students.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
}
```

---

## 🎨 UI Layer

### View Binding
Always use View Binding instead of `findViewById`:

```kotlin
class StudentsListFragment : Fragment() {
    private var binding: FragmentStudentsListBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentsListBinding.inflate(layoutInflater, container, false)
        return binding?.root
    }
}
```

### RecyclerView Pattern
```kotlin
// Adapter
class StudentsAdapter(
    var students: MutableList<Student>?,
): RecyclerView.Adapter<StudentRowViewHolder>() {

    var listener: OnItemClickListener? = null
    
    override fun getItemCount(): Int = students?.size ?: 0
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentRowViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding = StudentRowLayoutBinding.inflate(inflator, parent, false)
        return StudentRowViewHolder(binding = binding, listener = listener)
    }

    override fun onBindViewHolder(holder: StudentRowViewHolder, position: Int) {
        students?.let { holder.bind(it[position], position) }
    }
}

// Click Listener Interface
interface OnItemClickListener {
    fun onStudentItemClick(student: Student)
}
```

### ViewModel
```kotlin
enum class LoadingState { IDLE, LOADING, SUCCESS, ERROR }

class StudentsListViewModel: ViewModel() {
    val loadingState = MutableLiveData(LoadingState.IDLE)
    val data: LiveData<MutableList<Student>> = StudentsRepository.shared.getAllStudents()

    fun refreshStudents() {
        loadingState.value = LoadingState.LOADING
        StudentsRepository.shared.refreshStudents {
            loadingState.value = LoadingState.SUCCESS
        }
    }
}
```

### Threading & Concurrency
> ❌ **Do not** spawn new threads manually.
> ✅ Use a centralized `ExecutorService` (e.g., `DreamCatcherApplication.executorService.execute`) and `Handler(Looper.getMainLooper()).post` for background tasks and UI updates:
> ```kotlin
> DreamCatcherApplication.executorService.execute {
>     val result = firebaseModel.someOperation()
>     Handler(Looper.getMainLooper()).post {
>         callback(result)
>     }
> }
> ```

### Media & Camera
> Use `ActivityResultContracts` (like `TakePicturePreview` or `GetContent`) instead of `startActivityForResult` for camera and gallery intents. Store launchers as fragment members.

---

## 🧭 Navigation

### Navigation Component with Safe Args
Use Navigation Component with Safe Args plugin for type-safe navigation:

```kotlin
// In Fragment
private fun navigateToPinkFragment(student: Student) {
    view?.let {
        val action = StudentsListFragmentDirections.actionStudentsListFragmentToBlueFragment(student.name)
        Navigation.findNavController(it).navigate(action)
    }
}
```

---

## 📱 Application Class

### Global Context Pattern
```kotlin
class DreamCatcherApplication: Application() {

    companion object Globals {
        var appContext: Context? = null
        val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
```

Register in `AndroidManifest.xml`:
```xml
<application
    android:name=".base.DreamCatcherApplication"
    ...>
```

---

## 🔤 Type Aliases

Define common callback types in `Constants.kt`:
```kotlin
typealias StudentsCompletion = (List<Student>) -> Unit
typealias StudentCompletion = (Student) -> Unit
typealias Completion = () -> Unit
typealias StringCompletion = (String?) -> Unit
```

---

## 📄 AndroidManifest.xml

### Standard Configuration
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".base.MyApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.{AppName}">
        
        <activity
            android:name=".base.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 🎨 Resources

### Theme (Material 3)
```xml
<resources>
    <style name="Base.Theme.AppName" parent="Theme.Material3.DayNight">
        <!-- Customize theme here -->
    </style>

    <style name="Theme.AppName" parent="Base.Theme.AppName" />
</resources>
```

### Strings
- All user-visible text must be in `strings.xml`
- No hardcoded strings in code or layouts

### Colors
- Define all colors in `colors.xml`
- Use semantic color names

---

## 📝 Naming Conventions

### Files
| Type | Convention | Example |
|------|------------|---------|
| Activity | `{Name}Activity.kt` | `MainActivity.kt` |
| Fragment | `{Name}Fragment.kt` | `StudentsListFragment.kt` |
| ViewModel | `{Name}ViewModel.kt` | `StudentsListViewModel.kt` |
| Adapter | `{Name}Adapter.kt` | `StudentsAdapter.kt` |
| ViewHolder | `{Name}ViewHolder.kt` | `StudentRowViewHolder.kt` |
| Layout (Activity) | `activity_{name}.xml` | `activity_main.xml` |
| Layout (Fragment) | `fragment_{name}.xml` | `fragment_students_list.xml` |
| Layout (Item) | `{name}_row_layout.xml` | `student_row_layout.xml` |

### Code
| Type | Convention | Example |
|------|------------|---------|
| Classes | PascalCase | `StudentDao` |
| Functions | camelCase | `getAllStudents()` |
| Variables | camelCase | `studentList` |
| Constants | SCREAMING_SNAKE_CASE | `LAST_UPDATED_KEY` |
| Packages | lowercase | `com.company.projectname` |

---

## 🔧 Required Dependencies

### Core Dependencies
```kotlin
// AndroidX Core
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.appcompat)
implementation(libs.material)
implementation(libs.androidx.activity)
implementation(libs.androidx.constraintlayout)

// Navigation
implementation(libs.androidx.navigation.fragment.ktx)
implementation(libs.androidx.navigation.ui.ktx)

// Room Database
implementation(libs.androidx.room.runtime)
kapt(libs.androidx.room.compiler)
implementation(libs.androidx.room.ktx)

// RecyclerView
implementation(libs.androidx.recyclerview)
implementation(libs.androidx.fragment)
```

### Firebase Dependencies
```kotlin
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.firestore)
implementation(libs.firebase.auth)
implementation(libs.firebase.storage)
```

### Image Loading & Networking
```kotlin
implementation(libs.glide)
implementation(libs.okhttp)
```

### Cloud Storage (Images)
```kotlin
implementation(libs.cloudinary.android)
implementation(libs.cloudinary.android.download)
implementation(libs.cloudinary.android.preprocess)
```

---

## ✅ Checklist for New Features

- [ ] Create feature package under `features/`
- [ ] Create Fragment with View Binding
- [ ] Create ViewModel with LiveData
- [ ] Update Repository if needed
- [ ] Create Adapter and ViewHolder for lists
- [ ] Add layout XML files
- [ ] Add navigation actions
- [ ] Register Activity in AndroidManifest (if new Activity)
- [ ] Add strings to `strings.xml`
- [ ] Test on emulator/device

---

## 🔄 Activity Lifecycle

Always log lifecycle events during development:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d("ActivityLifecycle", "onCreate called")
}

override fun onStart() {
    super.onStart()
    Log.d("ActivityLifecycle", "onStart called")
}

override fun onResume() {
    super.onResume()
    Log.d("ActivityLifecycle", "onResume called")
}

override fun onPause() {
    super.onPause()
    Log.d("ActivityLifecycle", "onPause called")
}

override fun onStop() {
    super.onStop()
    Log.d("ActivityLifecycle", "onStop called")
}

override fun onDestroy() {
    super.onDestroy()
    Log.d("ActivityLifecycle", "onDestroy called")
}
```

---

## 📚 Additional Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Developers](https://developer.android.com/)
- [Material Design 3](https://m3.material.io/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Navigation Component](https://developer.android.com/guide/navigation)

---

If you miss some standards u can look into the rules directory

## Project wiki memory
This repo uses a compact AI-maintained wiki under `docs/wiki`.
After any meaningful code change, use `.skills/code-wiki/SKILL.md` to update the wiki.
A meaningful change includes new features, changed behavior, APIs, migrations, permissions, integrations, important refactors, durable bug-fix lessons, or removals.
Do not update the wiki for formatting-only, typo-only, or trivial cleanup changes.
