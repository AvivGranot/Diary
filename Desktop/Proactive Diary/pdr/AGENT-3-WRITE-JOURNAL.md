# AGENT 3 — Write Screen + Journal Screen + Search

## Role

You build the two screens where users spend 90% of their time. Write is the core — a distraction-free surface styled by the user's Design Studio choices. Journal is the archive — reverse-chronological entries with FTS5-powered full-text search.

If Write doesn't feel like opening a real diary, none of the other screens matter. If Journal search can't find an entry in under 200ms, the user stops trusting the app with their thoughts.

---

## What You Own

```
ui/write/
├── WriteScreen.kt              # Main writing composable
├── WriteViewModel.kt           # Auto-save, entry state, theme reading
├── WriteToolbar.kt             # Bottom toolbar: mood, tags, word count
└── MoodSelector.kt             # 5-mood horizontal picker

ui/journal/
├── JournalScreen.kt            # Entry list + search
├── JournalViewModel.kt         # Load entries, search, filtering
├── DiaryCard.kt                # Single entry card
├── SearchBar.kt                # Search input with mode detection
└── EntryDetailScreen.kt        # Full entry view (read-only + edit/delete)

data/repository/
└── EntryRepositoryImpl.kt      # Implements EntryRepository

domain/search/
└── SearchEngineImpl.kt         # FTS5 query builder + date parsing
```

## What You Touch But Don't Own

- `data/dao/EntryDao` + `PreferenceDao` — reads and writes
- `data/entities/EntryEntity`, `EntryWithSnippet` — Row types
- `domain/model/DiaryTheme.kt` — `DiaryThemeConfig` for reading theme preferences
- `domain/search/SearchEngine` — interface you implement
- `data/repository/EntryRepository` — interface you implement
- `navigation/Routes.kt` — `Routes.EntryDetail`
- `ui/theme/*` — all color and typography tokens

---

## Theme Integration Contract

You read the same preference keys Agent 2 writes. If they don't exist yet (user hasn't completed Design Studio), use `DiaryThemeConfig()` defaults.

```kotlin
// Read from PreferenceDao:
diary_color         → background color of Write surface
diary_form          → padding/margins of text area
diary_texture       → (visual hint on background — subtle, not blocking)
diary_canvas        → line style rendered behind text
diary_details       → JSON array — which features are enabled
diary_mark_text     → monogram in header/footer
diary_mark_position → "header" | "footer"
diary_mark_font     → "serif" | "sans"
```

**Color mapping** (diary_color → actual Color for Write background):

| Key | Color |
|-----|-------|
| `cream` | `Color(0xFFF3EEE7)` |
| `blush` | `Color(0xFFF0E0D6)` |
| `sage` | `Color(0xFFE4E8DF)` |
| `sky` | `Color(0xFFDDE4EC)` |
| `lavender` | `Color(0xFFE4DEE8)` |
| `midnight` | `Color(0xFF2C2C34)` |
| `charcoal` | `Color(0xFF30302E)` |
| `wine_red` | `Color(0xFF8B3A3A)` |
| `forest` | `Color(0xFF3A5A40)` |
| `ocean` | `Color(0xFF2C5F7C)` |

For dark colors (midnight, charcoal, wine_red, forest, ocean), text color should be light (`Color(0xFFE8E7E5)`). For light colors, text stays Ink (`Color(0xFF313131)`).

**Form mapping:**
- `focused`: horizontal padding 32dp.
- `spacious`: horizontal padding 24dp, line-height 2.0×.
- `compact`: horizontal padding 16dp, line-height 1.5×.

---

## WRITE SCREEN

### Layout

```
┌─────────────────────────────────────────┐
│ [monogram if set, position per pref]    │
│                                         │
│  Saturday, February 8, 2026             │  ← if date_header enabled
│                                         │
│  [tap to add title]                     │  ← collapsed by default
│                                         │
│  [                                      │
│   Writing area fills available space.   │
│   Canvas lines/dots/grid behind text.   │
│   Background = diary_color.             │
│                                         │
│  ]                                      │
│                                         │
├─────────────────────────────────────────┤
│  ● ● ○ ○ ○   #tags   |   247 words     │  ← toolbar, above keyboard
└─────────────────────────────────────────┘
```

### Date Header

- Format: `"Saturday, February 8, 2026"` (use `DateTimeFormatter` with `EEEE, MMMM d, yyyy`).
- Font: Cormorant Garamond 16sp, regular, Pencil color.
- Only rendered if `diary_details` contains `"date_header"`.

### Title Field

- Default: collapsed, shows `"tap to add title"` in Cormorant Garamond italic 20sp, `Pencil.copy(alpha = 0.5f)`.
- On tap: expand to editable `TextField`, Cormorant Garamond 20sp regular, Ink.
- Optional — user can write without ever tapping it.

### Writing Area

- `BasicTextField` filling remaining space (above toolbar, below title).
- Font: Roboto, 16sp default (adjustable by font_size preference: 14/16/18sp).
- Text color: Ink (or light variant for dark diary_color).
- Line height: 1.7× (or per Form choice).
- Horizontal padding: per Form choice (16/24/32dp).
- Use `imePadding()` so toolbar stays above keyboard.
- Content scrolls so cursor is always visible.

### Canvas Lines (behind text)

Render on a `Canvas` layer behind the `BasicTextField`:

| Canvas | Rendering |
|--------|-----------|
| `lined` | Horizontal rules at every `lineHeight` interval. Color: `Pencil.copy(alpha = 0.1f)`. Width: 0.5dp. |
| `dotted` | Dots at `lineHeight` vertical intervals and every 24dp horizontal. 2dp radius. Color: `Pencil.copy(alpha = 0.15f)`. |
| `grid` | Both horizontal and vertical rules at `lineHeight` and 24dp intervals. Color: `Pencil.copy(alpha = 0.08f)`. Width: 0.5dp. |
| `numbered` | Line numbers in left margin at every `lineHeight`. Roboto 10sp, `Pencil.copy(alpha = 0.3f)`. Numbers start at 1. |
| `blank` | Nothing. |

### Auto-Save

```kotlin
// In WriteViewModel — debounce 5 seconds
viewModelScope.launch {
    snapshotFlow { content.value }
        .debounce(5_000L)
        .collect { text ->
            if (text.isNotBlank()) saveEntry()
        }
}
```

Save logic:
- Compute `wordCount = text.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size`.
- If `currentEntryId == null` → `repository.insert(newEntry)`, capture the ID.
- Else → `repository.update(existingEntry)`.
- `createdAt` is set once on insert, never changed.
- `updatedAt` = `System.currentTimeMillis()` on every save.

### Auto-Save Indicator

If `diary_details` contains `"auto_save"`:
- Show a 6dp circle, top-right corner, `Pencil.copy(alpha = 0.4f)`.
- On save: single pulse animation (scale 1→1.3→1, 300ms).

### Toolbar (48dp, above keyboard)

- Background: same as writing surface, with 1px top divider.
- Left: `MoodSelector` — 5 colored circles in a row, 28dp diameter, 8dp spacing.
  - Colors: Great=`#5B8C5A`, Good=`#8FAD88`, Neutral=`#A0A09E`, Bad=`#8B7B8B`, Terrible=`#8B5A5A`.
  - Unselected: color fill at 40% opacity, no border.
  - Selected: full opacity + 2dp Ink border + scale 1.15 (200ms).
  - Tap toggles (tap selected to deselect). Only one active.
- Center: `"#"` text button. On tap: show dialog with `TextField` for comma-separated tags.
  - Tags stored as JSON array: `["run", "morning"]`.
- Right: word count — Roboto 12sp, Pencil. Updates in real-time.
  - Only visible if `diary_details` contains `"word_count"`.

### Same-Day Entry Deduplication

When Write opens with no arguments:
1. Query `EntryDao.getByDateRange(startOfToday, endOfToday)`.
2. If entries exist for today → load the most recent one for editing.
3. If no entry exists → create new.

This prevents duplicate entries for the same day.

### Entry Editing

Navigate to Write with `Routes.Write.route + "?entryId={id}"`:
- Load entry from `EntryDao.getById(id)`.
- Pre-fill title, content, mood, tags.
- Auto-save updates the existing entry.

---

## JOURNAL SCREEN

### Layout

```
┌─────────────────────────────────────────┐
│  🔍 Search your diary...                │  ← pinned search bar
├─────────────────────────────────────────┤
│                                         │
│  Saturday, February 8, 2026             │  ← date group header
│  ┌─────────────────────────────────────┐│
│  │  Entry title or first line...       ││
│  │  Content preview (max 3 lines)...   ││
│  │  ● #run #morning         142 words  ││
│  └─────────────────────────────────────┘│
│                                         │
│  Friday, February 7, 2026              │
│  ┌─────────────────────────────────────┐│
│  │  Another entry...                   ││
│  └─────────────────────────────────────┘│
│                                         │
└─────────────────────────────────────────┘
```

### SearchBar

- Pinned to top, 48dp height.
- Background: Parchment.
- Placeholder: `"Search your diary..."` — Roboto 14sp, italic, `Pencil.copy(alpha = 0.5f)`.
- Leading icon: magnifying glass, Pencil color.
- Trailing: "X" clear button, only when text is present.
- On text change (debounced 300ms): trigger search.

### Entry List

- Source: `EntryRepository.getAllEntries()` (Flow, reactive).
- Group by date. Date header: Cormorant Garamond 14sp, Pencil.
- Reverse chronological order.

### DiaryCard

- Background: Parchment. Corner radius 8dp. Padding 16dp. Bottom margin 8dp.
- Title (or first line of content if no title): Cormorant Garamond 18sp, Ink. Single line, ellipsis.
- Content preview: Roboto 14sp, Pencil. Max 3 lines, ellipsis.
- Bottom row: mood circle (14dp, filled with mood color) + tags as text (`#run #morning`, Roboto 12sp, Pencil) + word count (right-aligned, Roboto 12sp, Pencil).
- On tap: navigate to `Routes.EntryDetail.create(entry.id)`.

### Search Results

When search is active, replace the entry list with search results:

- **Content search** (default): use `EntryDao.search(ftsQuery)` which returns `EntryWithSnippet`.
  - Display `snippet` field instead of content preview.
  - Render `<b>` tags as bold spans using `AnnotatedString`.
- **Date search**: detected by `SearchEngine.isDateQuery()`.
  - Use `SearchEngine.parseDateRange()` → `EntryRepository.searchByDateRange()`.

### EntryDetailScreen

- Full-screen read view. Background: diary color (from preferences).
- Top bar: back arrow (left), edit pencil icon (right), overflow menu (right).
- Content renders with same styling as Write screen (fonts, line height, canvas lines).
- Date header at top: Cormorant Garamond 16sp, Pencil.
- Title: Cormorant Garamond 20sp, Ink.
- Content: Roboto 16sp, Ink.
- Mood + tags + word count at bottom.
- Edit button → navigate to Write with `entryId` argument.
- Overflow → "Delete" with confirmation dialog: `"Delete this entry? This cannot be undone."` → Two buttons: Cancel / Delete.

### Empty States

- No entries: `"Your diary is empty."` / `"Start writing to see your entries here."` — centered, Cormorant Garamond 20sp + italic 16sp.
- No search results: `"Nothing found for '${query}'"` — same styling.

---

## EntryRepositoryImpl

```kotlin
class EntryRepositoryImpl @Inject constructor(
    private val entryDao: EntryDao
) : EntryRepository {

    override fun getAllEntries(): Flow<List<EntryEntity>> =
        entryDao.getAllOrderByCreatedAtDesc()

    override fun getEntryById(id: String): Flow<EntryEntity?> =
        entryDao.getById(id)

    override fun getTodayEntry(): Flow<List<EntryEntity>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + 86_400_000L - 1
        return entryDao.getByDateRange(startOfDay, endOfDay)
    }

    override suspend fun insert(entry: EntryEntity) = entryDao.insert(entry)
    override suspend fun update(entry: EntryEntity) = entryDao.update(entry)
    override suspend fun delete(entryId: String) = entryDao.deleteById(entryId)
    override suspend fun searchContent(ftsQuery: String) = entryDao.search(ftsQuery)
    override fun searchByDateRange(start: Long, end: Long) = entryDao.getByDateRange(start, end)
}
```

Bind in Hilt via `@Binds` or `@Provides` in a new `RepositoryModule.kt` (or add to existing module).

---

## SearchEngineImpl

```kotlin
class SearchEngineImpl @Inject constructor() : SearchEngine {

    private val stopWords = setOf(
        "the", "a", "an", "is", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would",
        "could", "should", "may", "might", "shall", "can", "to", "of",
        "in", "for", "on", "with", "at", "by", "from", "as", "into",
        "about", "like", "through", "after", "over", "between", "out",
        "against", "during", "without", "before", "under", "around",
        "among", "i", "me", "my", "we", "our", "you", "your", "he",
        "she", "it", "they", "them", "this", "that", "these", "those",
        "and", "but", "or", "not", "no", "so", "if", "when", "what",
        "which", "who", "how", "all", "each", "every", "both", "few",
        "more", "most", "some", "any", "just", "very", "really",
        "today", "went", "got", "also", "then", "than"
    )

    private val monthNames = listOf(
        "january", "february", "march", "april", "may", "june",
        "july", "august", "september", "october", "november", "december"
    )

    override fun isDateQuery(query: String): Boolean {
        val q = query.lowercase().trim()
        val patterns = listOf(
            Regex("\\d{4}-\\d{2}-\\d{2}"),
            Regex("\\d{1,2}/\\d{1,2}/\\d{2,4}"),
            Regex("(${monthNames.joinToString("|")})"),
            Regex("(yesterday|last\\s+(week|month|year))")
        )
        return patterns.any { it.containsMatchIn(q) }
    }

    override fun buildFtsQuery(userInput: String): String {
        val words = userInput.lowercase().trim().split("\\s+".toRegex())
        val filtered = words.filter { it !in stopWords && it.length > 1 }
        if (filtered.isEmpty()) return userInput.trim() + "*"
        return filtered.joinToString(" AND ") { "$it*" }
    }

    override fun parseDateRange(query: String): Pair<Long, Long>? {
        // Parse "July 12", "2025-07-12", "yesterday", "last week", month names
        // Return (startEpochMillis, endEpochMillis) or null if unparseable
        // Implementation uses Calendar / LocalDate
    }
}
```

---

## Write Route Definition

Update `Routes.kt` (or coordinate with Agent 0):

```kotlin
object Write : Routes("write?entryId={entryId}") {
    fun create(entryId: String? = null) =
        if (entryId != null) "write?entryId=$entryId" else "write"
}
```

---

## Acceptance Criteria

| # | Criterion | Verification |
|---|-----------|-------------|
| 1 | Write renders with diary theme (color, canvas lines) | Set Wine Red + Dotted in prefs, open Write |
| 2 | Typing auto-saves after 5s inactivity | Type, wait 6s, query entries table |
| 3 | Word count updates in real-time | Type words, count increments |
| 4 | Mood persists with entry | Select mood, save, re-open — mood still selected |
| 5 | Tags save as JSON | Add tags, query entry — tags field is valid JSON array |
| 6 | Same-day dedup works | Open Write twice same day — same entry ID both times |
| 7 | Journal lists entries reverse-chrono | Create 3 entries on different days — correct order |
| 8 | Content search returns highlighted results | Write "running in the park", search "running" — bold highlight |
| 9 | Date search returns correct entries | Write entry, search by that date — found |
| 10 | Tap journal card → detail view | Tap → full entry visible with all metadata |
| 11 | Edit from detail → Write with data loaded | Tap edit → Write has title, content, mood pre-filled |
| 12 | Delete works with confirmation | Delete → confirm → entry gone from journal |
| 13 | Empty states render | Fresh install, open Journal — empty state visible |
| 14 | FTS stays in sync after update | Edit entry content, search new text — found |
| 15 | FTS stays in sync after delete | Delete entry, search old text — not found |
