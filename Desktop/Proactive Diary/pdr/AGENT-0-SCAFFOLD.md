# AGENT 0 — Project Scaffold & Infrastructure

## Role

You are the foundation layer. Every other agent lands on what you build. If your types are wrong, five agents produce incompatible code. If your build breaks, nobody ships. You own the contract surface between all agents.

Your job is not to build features. Your job is to make it impossible for feature agents to build the wrong thing.

---

## Outputs

When you are done, the following must be true:

1. `./gradlew assembleDebug` exits 0.
2. The app launches on an emulator and renders a placeholder screen.
3. Room creates all 5 tables + 1 FTS virtual table + 3 sync triggers on first run.
4. Hilt injects the database and all DAOs without crash.
5. Navigation resolves all 7 routes (placeholder composables).
6. The theme renders: `#F3EEE7` background, `#313131` text, Cormorant Garamond visible.

If any of these are false, you are not done.

---

## Project Location

```
C:\Users\user\Desktop\Proactive Diary
```

Package name: `com.proactivediary`

---

## File Manifest

Every file you must create. No more, no fewer.

```
Proactive Diary/
├── build.gradle.kts                          # project-level
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── CLAUDE.md
├── app/
│   ├── build.gradle.kts                      # app-level, all deps
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/proactivediary/
│       │   ├── ProactiveDiaryApp.kt          # @HiltAndroidApp
│       │   ├── MainActivity.kt               # @AndroidEntryPoint, splash, setContent
│       │   ├── di/
│       │   │   ├── AppModule.kt              # Hilt @Module — Gson, dispatchers
│       │   │   └── DatabaseModule.kt         # Hilt @Module — Room DB, DAOs
│       │   ├── data/
│       │   │   ├── db/
│       │   │   │   ├── AppDatabase.kt        # RoomDatabase, FTS callback
│       │   │   │   ├── entities/
│       │   │   │   │   ├── EntryEntity.kt
│       │   │   │   │   ├── GoalEntity.kt
│       │   │   │   │   ├── GoalCheckInEntity.kt
│       │   │   │   │   ├── WritingReminderEntity.kt
│       │   │   │   │   └── PreferenceEntity.kt
│       │   │   │   ├── dao/
│       │   │   │   │   ├── EntryDao.kt
│       │   │   │   │   ├── GoalDao.kt
│       │   │   │   │   ├── GoalCheckInDao.kt
│       │   │   │   │   ├── WritingReminderDao.kt
│       │   │   │   │   └── PreferenceDao.kt
│       │   │   │   └── converters/
│       │   │   │       └── Converters.kt     # JSON <-> List, Long <-> Date
│       │   │   └── repository/
│       │   │       ├── EntryRepository.kt    # interface
│       │   │       ├── GoalRepository.kt     # interface
│       │   │       └── ReminderRepository.kt # interface
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   │   ├── DiaryTheme.kt         # DiaryThemeConfig data class
│       │   │   │   ├── Mood.kt               # enum
│       │   │   │   └── GoalFrequency.kt      # enum
│       │   │   └── search/
│       │   │       └── SearchEngine.kt       # interface
│       │   ├── ui/
│       │   │   ├── theme/
│       │   │   │   ├── Color.kt
│       │   │   │   ├── Type.kt
│       │   │   │   ├── Theme.kt
│       │   │   │   └── Shape.kt
│       │   │   ├── typewriter/
│       │   │   │   └── TypewriterScreen.kt   # placeholder
│       │   │   ├── designstudio/
│       │   │   │   └── DesignStudioScreen.kt # placeholder
│       │   │   ├── onboarding/
│       │   │   │   └── OnboardingGoalsScreen.kt # placeholder
│       │   │   ├── write/
│       │   │   │   └── WriteScreen.kt        # placeholder
│       │   │   ├── journal/
│       │   │   │   └── JournalScreen.kt      # placeholder
│       │   │   ├── goals/
│       │   │   │   └── GoalsScreen.kt        # placeholder
│       │   │   ├── settings/
│       │   │   │   └── SettingsScreen.kt     # placeholder
│       │   │   └── components/
│       │   │       └── PlaceholderScreen.kt
│       │   └── navigation/
│       │       ├── NavGraph.kt
│       │       └── Routes.kt
│       └── res/
│           ├── font/
│           │   ├── cormorant_garamond_regular.ttf
│           │   └── cormorant_garamond_italic.ttf
│           ├── values/
│           │   ├── strings.xml
│           │   └── themes.xml
│           └── drawable/
│               └── ic_launcher_foreground.xml  # simple vector placeholder
```

---

## Dependency Versions

Lock these. Do not deviate.

```kotlin
// build.gradle.kts (project)
id("com.android.application") version "8.2.2"
id("org.jetbrains.kotlin.android") version "1.9.22"
id("com.google.dagger.hilt.android") version "2.50"

// Compose compiler must match Kotlin 1.9.22:
composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
```

```kotlin
// build.gradle.kts (app) — full dependency list
platform("androidx.compose:compose-bom:2024.02.00")
"androidx.compose.ui:ui"
"androidx.compose.ui:ui-graphics"
"androidx.compose.ui:ui-tooling-preview"
"androidx.compose.material3:material3"
"androidx.compose.animation:animation"
"androidx.compose.material:material-icons-extended"
"androidx.activity:activity-compose:1.8.2"
"androidx.navigation:navigation-compose:2.7.7"
"androidx.lifecycle:lifecycle-runtime-compose:2.7.0"
"androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"
"androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
"androidx.room:room-runtime:2.6.1"
"androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"
"com.google.dagger:hilt-android:2.50"
kapt "com.google.dagger:hilt-android-compiler:2.50"
"androidx.hilt:hilt-navigation-compose:1.1.0"
"io.coil-kt:coil-compose:2.5.0"
"org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
"androidx.datastore:datastore-preferences:1.0.0"
"com.google.code.gson:gson:2.10.1"
"com.android.billingclient:billing-ktx:7.0.0"
"androidx.core:core-splashscreen:1.0.1"
"androidx.core:core-ktx:1.12.0"
```

compileSdk = 34, minSdk = 26, targetSdk = 34, jvmTarget = "17".

---

## Entity Definitions — The Shared Type Contract

These are the types that bind all agents. Get them exactly right.

### EntryEntity

```kotlin
@Entity(tableName = "entries", indices = [Index("created_at")])
data class EntryEntity(
    @PrimaryKey val id: String,                              // UUID string
    val title: String = "",
    val content: String,
    val mood: String? = null,                                // "great"|"good"|"neutral"|"bad"|"terrible"|null
    val tags: String = "[]",                                 // JSON array of strings: '["run","morning"]'
    @ColumnInfo(name = "word_count") val wordCount: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,    // epoch millis UTC
    @ColumnInfo(name = "updated_at") val updatedAt: Long     // epoch millis UTC
)
```

### GoalEntity

```kotlin
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,                              // UUID string
    val title: String,
    val description: String? = null,
    val frequency: String = "daily",                         // "daily"|"weekly"|"monthly"
    @ColumnInfo(name = "reminder_time") val reminderTime: String,  // "HH:mm" 24h format
    @ColumnInfo(name = "reminder_days") val reminderDays: String = "[0,1,2,3,4,5,6]", // JSON array, 0=Mon
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
```

### GoalCheckInEntity

```kotlin
@Entity(
    tableName = "goal_checkins",
    indices = [Index("goal_id"), Index("date")],
    foreignKeys = [ForeignKey(
        entity = GoalEntity::class,
        parentColumns = ["id"],
        childColumns = ["goal_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GoalCheckInEntity(
    @PrimaryKey val id: String,                              // UUID string
    @ColumnInfo(name = "goal_id") val goalId: String,
    val date: String,                                        // "yyyy-MM-dd" local date
    val completed: Boolean = false,
    val note: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
```

### WritingReminderEntity

```kotlin
@Entity(tableName = "writing_reminders")
data class WritingReminderEntity(
    @PrimaryKey val id: String,                              // UUID string
    val time: String,                                        // "HH:mm" 24h format
    val days: String = "[0,1,2,3,4,5,6]",                   // JSON array of ints, 0=Mon
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "fallback_enabled") val fallbackEnabled: Boolean = true,
    val label: String = "Write in your diary"
)
```

### PreferenceEntity

```kotlin
@Entity(tableName = "preferences")
data class PreferenceEntity(
    @PrimaryKey val key: String,
    val value: String
)
```

---

## DAO Contracts

Each DAO must provide at minimum these operations. Use `Flow` for all queries that other screens observe reactively. Use `suspend` for writes.

### EntryDao

```kotlin
@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY created_at DESC")
    fun getAllOrderByCreatedAtDesc(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE id = :id")
    fun getById(id: String): Flow<EntryEntity?>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getByIdSync(id: String): EntryEntity?

    @Query("SELECT * FROM entries WHERE created_at BETWEEN :start AND :end ORDER BY created_at DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries ORDER BY created_at DESC")
    suspend fun getAllSync(): List<EntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM entries")
    suspend fun deleteAll()

    // FTS search — returns entry rows with highlighted snippets
    @Query("""
        SELECT e.*, highlight(entries_fts, 1, '<b>', '</b>') AS snippet
        FROM entries_fts
        JOIN entries e ON entries_fts.docid = e.rowid
        WHERE entries_fts MATCH :query
        ORDER BY rank
        LIMIT 50
    """)
    suspend fun search(query: String): List<EntryWithSnippet>
}
```

You must also define:

```kotlin
data class EntryWithSnippet(
    val id: String,
    val title: String,
    val content: String,
    val mood: String?,
    val tags: String,
    @ColumnInfo(name = "word_count") val wordCount: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    val snippet: String?
)
```

### GoalDao

```kotlin
@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE is_active = 1 ORDER BY created_at DESC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY created_at DESC")
    fun getAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun getById(id: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getByIdSync(id: String): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM goals")
    suspend fun deleteAll()
}
```

### GoalCheckInDao

```kotlin
@Dao
interface GoalCheckInDao {
    @Query("SELECT * FROM goal_checkins WHERE goal_id = :goalId ORDER BY date DESC")
    fun getByGoalId(goalId: String): Flow<List<GoalCheckInEntity>>

    @Query("SELECT * FROM goal_checkins WHERE goal_id = :goalId AND date = :date LIMIT 1")
    suspend fun getByGoalAndDate(goalId: String, date: String): GoalCheckInEntity?

    @Query("SELECT * FROM goal_checkins WHERE date = :date")
    suspend fun getByDate(date: String): List<GoalCheckInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: GoalCheckInEntity)

    @Update
    suspend fun update(checkIn: GoalCheckInEntity)

    @Query("DELETE FROM goal_checkins WHERE goal_id = :goalId")
    suspend fun deleteByGoalId(goalId: String)

    @Query("DELETE FROM goal_checkins")
    suspend fun deleteAll()
}
```

### WritingReminderDao

```kotlin
@Dao
interface WritingReminderDao {
    @Query("SELECT * FROM writing_reminders WHERE is_active = 1")
    fun getActive(): Flow<List<WritingReminderEntity>>

    @Query("SELECT * FROM writing_reminders")
    fun getAll(): Flow<List<WritingReminderEntity>>

    @Query("SELECT * FROM writing_reminders")
    suspend fun getAllSync(): List<WritingReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: WritingReminderEntity)

    @Update
    suspend fun update(reminder: WritingReminderEntity)

    @Query("DELETE FROM writing_reminders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM writing_reminders")
    suspend fun deleteAll()
}
```

### PreferenceDao

```kotlin
@Dao
interface PreferenceDao {
    @Query("SELECT * FROM preferences WHERE `key` = :key")
    suspend fun get(key: String): PreferenceEntity?

    @Query("SELECT * FROM preferences WHERE `key` = :key")
    fun observe(key: String): Flow<PreferenceEntity?>

    @Query("SELECT * FROM preferences")
    fun getAll(): Flow<List<PreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(pref: PreferenceEntity)

    @Query("DELETE FROM preferences WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM preferences")
    suspend fun deleteAll()
}
```

---

## FTS Virtual Table

In `AppDatabase.kt`, use a `RoomDatabase.Callback` to create the FTS table and sync triggers after the database is created:

```kotlin
override fun onCreate(db: SupportSQLiteDatabase) {
    super.onCreate(db)

    db.execSQL("""
        CREATE VIRTUAL TABLE IF NOT EXISTS entries_fts
        USING fts4(title, content, tags, content='entries')
    """)

    db.execSQL("""
        CREATE TRIGGER IF NOT EXISTS entries_ai AFTER INSERT ON entries BEGIN
            INSERT INTO entries_fts(docid, title, content, tags)
            VALUES (new.rowid, new.title, new.content, new.tags);
        END
    """)

    db.execSQL("""
        CREATE TRIGGER IF NOT EXISTS entries_ad AFTER DELETE ON entries BEGIN
            INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
            VALUES('delete', old.rowid, old.title, old.content, old.tags);
        END
    """)

    db.execSQL("""
        CREATE TRIGGER IF NOT EXISTS entries_au AFTER UPDATE ON entries BEGIN
            INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
            VALUES('delete', old.rowid, old.title, old.content, old.tags);
            INSERT INTO entries_fts(docid, title, content, tags)
            VALUES (new.rowid, new.title, new.content, new.tags);
        END
    """)
}
```

---

## Color System

```kotlin
object DiaryColors {
    // Light
    val Paper = Color(0xFFF3EEE7)
    val Ink = Color(0xFF313131)
    val Pencil = Color(0xFF585858)
    val Parchment = Color(0xFFFAF9F5)
    val Divider = Color(0xFF585858).copy(alpha = 0.15f)
    val Shadow = Color(0xFF313131).copy(alpha = 0.08f)

    // Dark
    val PaperDark = Color(0xFF1A1918)
    val InkDark = Color(0xFFE8E7E5)
    val PencilDark = Color(0xFFA0A09E)
    val ParchmentDark = Color(0xFF2A2928)
}
```

`Theme.kt` must expose a `ProactiveDiaryTheme` composable that accepts a `darkMode: Boolean` parameter and maps these tokens to `MaterialTheme.colorScheme`. All downstream agents use `MaterialTheme.colorScheme.background`, `.onBackground`, `.surface`, `.onSurface`, `.secondary` — never raw hex.

---

## Typography System

```kotlin
val CormorantGaramond = FontFamily(
    Font(R.font.cormorant_garamond_regular, FontWeight.Normal),
    Font(R.font.cormorant_garamond_italic, FontWeight.Normal, FontStyle.Italic)
)
```

Map to `Typography`:
- `headlineLarge` → Cormorant Garamond 24sp regular, letter-spacing 0.3sp, line-height 31.2sp — section titles
- `headlineMedium` → Cormorant Garamond 20sp italic, line-height 30sp — typewriter quote
- `bodyLarge` → Roboto 14sp, line-height 22.4sp — body text
- `labelSmall` → Roboto 11sp, letter-spacing 1.1sp — section numbers
- `titleMedium` → Cormorant Garamond 16sp, letter-spacing 3sp — nav logo

---

## Navigation

### Routes.kt

```kotlin
sealed class Routes(val route: String) {
    object Typewriter : Routes("typewriter")
    object DesignStudio : Routes("design_studio")
    object OnboardingGoals : Routes("onboarding_goals")
    object Write : Routes("write")
    object Journal : Routes("journal")
    object Goals : Routes("goals")
    object Settings : Routes("settings")
    object EntryDetail : Routes("entry/{entryId}") {
        fun create(entryId: String) = "entry/$entryId"
    }
}
```

### NavGraph.kt

`NavHost` with `startDestination` = `typewriter`. All routes resolve to placeholder composables. The start-destination logic (first launch vs returning user) will be wired by Agent 5 — you just set `typewriter` as default.

---

## Domain Models

### Mood.kt

```kotlin
enum class Mood(val label: String) {
    GREAT("Great"),
    GOOD("Good"),
    NEUTRAL("Neutral"),
    BAD("Bad"),
    TERRIBLE("Terrible")
}
```

### GoalFrequency.kt

```kotlin
enum class GoalFrequency(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}
```

### DiaryTheme.kt

```kotlin
data class DiaryThemeConfig(
    val colorScheme: String = "cream",
    val form: String = "focused",
    val texture: String = "paper",
    val canvas: String = "lined",
    val features: List<String> = listOf("auto_save", "word_count", "date_header", "daily_quote"),
    val markText: String = "",
    val markPosition: String = "header",
    val markFont: String = "serif"
)
```

---

## Repository Interfaces

Shell interfaces only. Agents 3 and 4 provide the implementations.

```kotlin
interface EntryRepository {
    fun getAllEntries(): Flow<List<EntryEntity>>
    fun getEntryById(id: String): Flow<EntryEntity?>
    fun getTodayEntry(): Flow<List<EntryEntity>>
    suspend fun insert(entry: EntryEntity)
    suspend fun update(entry: EntryEntity)
    suspend fun delete(entryId: String)
    suspend fun searchContent(ftsQuery: String): List<EntryWithSnippet>
    fun searchByDateRange(start: Long, end: Long): Flow<List<EntryEntity>>
}

interface GoalRepository {
    fun getActiveGoals(): Flow<List<GoalEntity>>
    fun getGoalById(id: String): Flow<GoalEntity?>
    fun getCheckInsForGoal(goalId: String): Flow<List<GoalCheckInEntity>>
    suspend fun insertGoal(goal: GoalEntity)
    suspend fun updateGoal(goal: GoalEntity)
    suspend fun deleteGoal(goalId: String)
    suspend fun checkIn(checkIn: GoalCheckInEntity)
    suspend fun getCheckInForToday(goalId: String, date: String): GoalCheckInEntity?
}

interface ReminderRepository {
    fun getActiveReminders(): Flow<List<WritingReminderEntity>>
    fun getAllReminders(): Flow<List<WritingReminderEntity>>
    suspend fun insertReminder(reminder: WritingReminderEntity)
    suspend fun updateReminder(reminder: WritingReminderEntity)
    suspend fun deleteReminder(id: String)
}
```

---

## SearchEngine Interface

```kotlin
interface SearchEngine {
    fun isDateQuery(query: String): Boolean
    fun buildFtsQuery(userInput: String): String
    fun parseDateRange(query: String): Pair<Long, Long>?
}
```

---

## Hilt Modules

### DatabaseModule.kt

Provides `AppDatabase` singleton, all 5 DAOs as `@Provides` functions.

### AppModule.kt

Provides `Gson` singleton, `@IoDispatcher` CoroutineDispatcher.

---

## Placeholder Composables

Every screen placeholder follows this pattern:

```kotlin
@Composable
fun XxxScreen() {
    PlaceholderScreen(title = "Screen Name")
}
```

Where `PlaceholderScreen`:

```kotlin
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineLarge)
    }
}
```

---

## Font Files

Download Cormorant Garamond Regular and Italic from Google Fonts. Place as:
- `res/font/cormorant_garamond_regular.ttf`
- `res/font/cormorant_garamond_italic.ttf`

Filenames must be lowercase, underscores only.

---

## CLAUDE.md

Write a project charter file at the repo root containing:
- App name, package name
- Tech stack with version numbers
- Architecture overview (MVVM: ViewModel + Repository + Room)
- Design system reference (color tokens, typography)
- Entity schema summary
- Agent division (0–5) with one-line summaries
- Build command: `./gradlew assembleDebug`
- Privacy note: all data local, no network except Play Billing

---

## Acceptance Criteria

| # | Criterion | How to verify |
|---|-----------|--------------|
| 1 | Build succeeds | `./gradlew assembleDebug` exits 0 |
| 2 | App launches | Emulator shows placeholder |
| 3 | Room tables created | Query `sqlite_master` — 5 tables + entries_fts |
| 4 | FTS triggers exist | Query `sqlite_master` — entries_ai, entries_ad, entries_au |
| 5 | All 7 routes navigate | Deep link to each route, placeholder renders |
| 6 | Theme renders | Paper background visible, Cormorant Garamond renders |
| 7 | Hilt injection works | App doesn't crash on launch |
| 8 | DAOs work | Insert + query round-trips for each entity |
| 9 | TypeConverters | JSON string <-> List<String>, Long <-> epoch millis |
