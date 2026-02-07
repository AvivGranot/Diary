# AGENT 5 — Integration, Paywall & Polish

## Role

You are the closer. Agents 1–4 built the pieces. You wire them into a single coherent app. You own navigation flow (onboarding vs returning user), the bottom nav bar, the Settings screen, the subscription paywall, dark mode, export, empty states, and the splash screen.

When you're done, the app works end-to-end. A user installs it, sees the typewriter, customizes their diary, sets goals, writes an entry, searches for it, gets a notification, and — after 7 days — sees a paywall. That complete loop is your responsibility.

---

## What You Own

```
ui/settings/
├── SettingsScreen.kt              # Full settings screen
└── SettingsViewModel.kt

ui/paywall/
├── PaywallDialog.kt               # Subscription gate modal
├── BillingService.kt              # Google Play Billing wrapper
└── BillingViewModel.kt

ui/components/
├── BottomNavBar.kt                # Themed 4-tab bottom nav
└── EmptyState.kt                  # Reusable empty state composable

navigation/
├── NavGraph.kt                    # REWRITE: full wiring, nested nav
└── Routes.kt                      # UPDATE: add any missing routes
```

## What You Modify (owned by other agents)

- `ProactiveDiaryApp.kt` — add notification channels init, billing init
- `MainActivity.kt` — read intent extras for notification deep links
- `AndroidManifest.xml` — verify billing permission exists
- `app/build.gradle.kts` — verify billing dependency exists
- `ui/theme/Theme.kt` — ensure dark mode parameter is wired to preference
- `res/values/themes.xml` — splash screen style

## What You Read But Don't Modify

- Every screen from Agents 1–4
- All DAOs, entities, repositories

---

## NAVIGATION ARCHITECTURE

### Root Structure

```
NavHost(startDestination = determined at runtime)
├── "typewriter"       → TypewriterScreen
├── "design_studio"    → DesignStudioScreen (with ?edit=bool arg)
├── "onboarding_goals" → OnboardingGoalsScreen
├── "main"             → MainScreen (has its own nested NavHost)
│   ├── "write"        → WriteScreen (with ?entryId=string arg)
│   ├── "journal"      → JournalScreen
│   ├── "goals"        → GoalsScreen
│   └── "settings"     → SettingsScreen
└── "entry/{entryId}"  → EntryDetailScreen
```

### Start Destination Logic

```kotlin
@Composable
fun ProactiveDiaryNavHost() {
    val prefDao = // Hilt inject
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val onboardingCompleted = prefDao.get("onboarding_completed")?.value == "true"
        startDestination = if (onboardingCompleted) "main" else "typewriter"
    }

    if (startDestination == null) return // show nothing while loading (splash covers this)

    NavHost(navController = navController, startDestination = startDestination!!) {
        composable("typewriter") { TypewriterScreen(onNext = { nav to design_studio }) }
        composable("design_studio?edit={edit}") { DesignStudioScreen(onNext = { ... }) }
        composable("onboarding_goals") { OnboardingGoalsScreen(onDone = { nav to main }) }
        composable("main") { MainScreen(navController) }
        composable("entry/{entryId}") { EntryDetailScreen(...) }
    }
}
```

### MainScreen (nested nav + bottom bar)

```kotlin
@Composable
fun MainScreen(rootNavController: NavHostController) {
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(innerNavController) }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = "write",
            modifier = Modifier.padding(padding)
        ) {
            composable("write") { WriteScreen() }
            composable("journal") { JournalScreen(onEntryClick = { id ->
                rootNavController.navigate("entry/$id")
            }) }
            composable("goals") { GoalsScreen() }
            composable("settings") { SettingsScreen(onOpenDesignStudio = {
                rootNavController.navigate("design_studio?edit=true")
            }) }
        }
    }
}
```

---

## BOTTOM NAV BAR

4 tabs: Write, Journal, Goals, Settings.

```
┌──────────┬──────────┬──────────┬──────────┐
│  ✏ Write │ 📖 Journal│ 🎯 Goals │ ⚙ Settings│
└──────────┴──────────┴──────────┴──────────┘
```

- Height: 56dp.
- Background: `MaterialTheme.colorScheme.surface` (Parchment light / ParchmentDark dark). 1px top border (divider).
- Labels: Roboto 11sp.
- Selected: `MaterialTheme.colorScheme.onSurface` (Ink).
- Unselected: `MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)` (Pencil 50%).
- Icons: Material icons — `Edit` (Write), `MenuBook` (Journal), `TrackChanges` (Goals), `Settings` (Settings).
- No Material ripple. Instant color swap on tap.
- Write icon: 1dp larger than others (24dp vs 23dp) — it's the primary action.
- Reselecting current tab: no-op (don't re-navigate).

---

## SETTINGS SCREEN

### Layout

```
┌─────────────────────────────────────────┐
│  Settings                                │
│                                         │
│  APPEARANCE                             │
│  ┌─────────────────────────────────────┐│
│  │  Diary Design      Wine Red · ...   ││  → Design Studio (edit mode)
│  │  Dark Mode         [toggle]         ││
│  │  Font Size         Medium ▼         ││
│  └─────────────────────────────────────┘│
│                                         │
│  NOTIFICATIONS                          │
│  ┌─────────────────────────────────────┐│
│  │  Writing Reminders    2 active      ││  → reminder management
│  │  Goal Reminders       3 active      ││  → Goals tab
│  └─────────────────────────────────────┘│
│                                         │
│  SUBSCRIPTION                           │
│  ┌─────────────────────────────────────┐│
│  │  Plan: Free Trial (3 days left)     ││
│  │  [     Upgrade to Pro     ]         ││
│  │  Restore Purchases                  ││
│  └─────────────────────────────────────┘│
│                                         │
│  DATA                                   │
│  ┌─────────────────────────────────────┐│
│  │  Export Diary         JSON / Text   ││
│  │  Delete All Data      ⚠             ││
│  └─────────────────────────────────────┘│
│                                         │
│  ABOUT                                  │
│  ┌─────────────────────────────────────┐│
│  │  Privacy Policy                     ││
│  │  Version 1.0.0                      ││
│  └─────────────────────────────────────┘│
└─────────────────────────────────────────┘
```

### Typography

- Screen title: Cormorant Garamond 24sp, Ink.
- Section headers: Roboto 11sp, letter-spacing 1.5sp, uppercase, Pencil.
- Row labels: Roboto 14sp, Ink.
- Row values/descriptions: Roboto 13sp, Pencil.
- Cards: Parchment background, 8dp radius, 1px internal dividers.

### Diary Design Row

Shows current config summary: `"Wine Red · Focused · Paper"`.
On tap: navigate to `design_studio?edit=true`. Design Studio reads existing prefs and shows "Save Changes".

### Dark Mode Toggle

- Switch: same custom style as Design Studio toggles.
- On toggle: write `PreferenceEntity("dark_mode", "true"/"false")`.
- The `ProactiveDiaryTheme` composable must observe this preference and switch color scheme immediately.
- All screens must use `MaterialTheme.colorScheme.*` tokens (never raw hex) so the switch propagates everywhere.

### Font Size

- Dropdown or bottom sheet: Small (14sp) / Medium (16sp) / Large (18sp).
- Save: `PreferenceEntity("font_size", "small"/"medium"/"large")`.
- Affects Write screen body text size.

### Writing Reminders Row

- Shows count of active reminders.
- On tap: open a management screen or dialog listing all reminders with edit/delete/add.

### Goal Reminders Row

- Shows count of active goals.
- On tap: switch bottom nav to Goals tab.

### Subscription Section

- Display current plan status from `BillingViewModel.getSubscriptionState()`.
- Trial: `"Free Trial (X days left)"`.
- Active: `"Monthly plan"` or `"Annual plan"`.
- Expired: `"Trial expired"`.
- "Upgrade to Pro" button: same Ink-background style. Opens `PaywallDialog`.
- "Restore Purchases": text button, Roboto 12sp, Pencil. Calls `BillingService.restorePurchases()`.

### Export Diary

Two options: JSON / Plain Text.
Show a bottom sheet or dialog with both options.

**JSON export:**
```json
[
  {
    "id": "...",
    "title": "...",
    "content": "...",
    "mood": "good",
    "tags": ["run", "morning"],
    "wordCount": 142,
    "createdAt": "2026-02-08T10:30:00Z",
    "updatedAt": "2026-02-08T10:35:00Z"
  }
]
```

**Text export:**
```
Saturday, February 8, 2026
My Morning Run

Today I went for a long run in the park...

Mood: Good | Words: 142 | Tags: run, morning

---

Friday, February 7, 2026
...
```

Save to device Downloads folder using `MediaStore` API (API 29+) or `SAF` intent. Show `Snackbar`: `"Diary exported to Downloads"`.

### Delete All Data

1. First dialog: `"This will permanently delete all diary entries, goals, and settings. This cannot be undone."`
   - Buttons: Cancel / Continue.
2. Second dialog: `"Type DELETE to confirm"` — text input.
   - Only enable "Delete" button when input exactly matches `"DELETE"`.
3. On confirm:
   - Call `deleteAll()` on all DAOs (entries, goals, goal_checkins, writing_reminders, preferences).
   - Cancel all scheduled alarms.
   - Navigate to `typewriter` and clear entire back stack.

### Privacy Policy

Static screen or dialog with text:
```
All your data stays on this device. We never collect, transmit, or analyze your diary entries.
Your writing is yours alone.

The only network connection this app makes is to Google Play for subscription management.
No diary content ever leaves your device.
```

### Version

`"Version 1.0.0"` — hardcoded, Roboto 13sp, Pencil.

---

## PAYWALL

### Trial Logic

On first app launch ever, write `PreferenceEntity("trial_start_date", System.currentTimeMillis().toString())`.

```kotlin
data class SubscriptionState(
    val isActive: Boolean,        // can the user write?
    val plan: Plan,
    val trialDaysLeft: Int        // 0 if not in trial
)

enum class Plan { TRIAL, MONTHLY, ANNUAL, EXPIRED }

fun getSubscriptionState(): SubscriptionState {
    val trialStart = prefDao.getSync("trial_start_date")?.value?.toLongOrNull()

    if (trialStart == null) {
        // First launch — set trial
        prefDao.setSync(PreferenceEntity("trial_start_date", now().toString()))
        return SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 7)
    }

    val daysSinceStart = TimeUnit.MILLISECONDS.toDays(now() - trialStart).toInt()
    val trialDaysLeft = (7 - daysSinceStart).coerceAtLeast(0)

    if (trialDaysLeft > 0) {
        return SubscriptionState(true, Plan.TRIAL, trialDaysLeft)
    }

    // Check Play Billing
    if (billingService.hasActiveSubscription()) {
        val plan = if (billingService.isAnnual()) Plan.ANNUAL else Plan.MONTHLY
        return SubscriptionState(true, plan, 0)
    }

    return SubscriptionState(false, Plan.EXPIRED, 0)
}
```

### Gate Points

These are the ONLY places the paywall blocks:
1. Tapping Write tab when trial expired and no subscription.
2. Creating a new entry.
3. Editing an existing entry (from EntryDetail).

**NEVER gate:**
- Reading entries.
- Browsing journal.
- Searching.
- Viewing goals.
- Checking in on goals.
- Settings.

### PaywallDialog Layout

```
┌─────────────────────────────────────────┐
│                                         │
│  Your free trial has ended              │  ← Cormorant Garamond 24sp, Ink
│                                         │
│  "Either write things worth reading,    │  ← CG italic 16sp, Pencil
│   or do things worth writing"           │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │  Monthly                 $2/month   ││  ← Parchment card, 12dp padding
│  └─────────────────────────────────────┘│
│  ┌─────────────────────────────────────┐│
│  │  Annual       Save 17%  $20/year   ││
│  │  ★ BEST VALUE                       ││
│  └─────────────────────────────────────┘│
│                                         │
│  Restore Purchases                      │  ← Roboto 12sp, Pencil
│                                         │
│  Your data stays on your device.        │  ← Roboto 12sp, Pencil, italic
│  We never see your diary.               │
│                                         │
└─────────────────────────────────────────┘
```

- Plan card labels: Roboto 14sp, Ink.
- Price: Roboto 16sp, Ink, right-aligned.
- "Save 17%" chip: Roboto 11sp, Ink on `Ink.copy(alpha = 0.1f)` background, 4dp radius.
- "BEST VALUE": Roboto 10sp, letter-spacing 1sp, Pencil.
- On plan tap → `billingService.launchPurchaseFlow(activity, sku)`.
- On successful purchase → dismiss dialog, user can write.

### BillingService

```kotlin
class BillingService(private val context: Context) {
    private lateinit var billingClient: BillingClient

    companion object {
        const val MONTHLY_SKU = "pd_monthly"
        const val ANNUAL_SKU = "pd_annual"
    }

    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(::onPurchaseUpdated)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(...)
    }

    fun launchPurchaseFlow(activity: Activity, sku: String) { ... }
    fun hasActiveSubscription(): Boolean { ... }
    fun isAnnual(): Boolean { ... }
    fun restorePurchases() { ... }
    fun destroy() { billingClient.endConnection() }
}
```

**Testing note:** Real purchases require Google Play Console configuration. For development, use the billing library's test product IDs or mock the service.

---

## DARK MODE WIRING

`ProactiveDiaryTheme` must be the root composable and observe the dark mode preference:

```kotlin
@Composable
fun ProactiveDiaryTheme(content: @Composable () -> Unit) {
    val prefDao = // inject or pass
    val darkModePref by prefDao.observe("dark_mode").collectAsState(initial = null)
    val isDark = darkModePref?.value == "true"

    val colorScheme = if (isDark) {
        darkColorScheme(
            background = DiaryColors.PaperDark,
            onBackground = DiaryColors.InkDark,
            surface = DiaryColors.ParchmentDark,
            onSurface = DiaryColors.InkDark,
            secondary = DiaryColors.PencilDark
        )
    } else {
        lightColorScheme(
            background = DiaryColors.Paper,
            onBackground = DiaryColors.Ink,
            surface = DiaryColors.Parchment,
            onSurface = DiaryColors.Ink,
            secondary = DiaryColors.Pencil
        )
    }

    MaterialTheme(colorScheme = colorScheme, typography = DiaryTypography, content = content)
}
```

**Critical:** Every screen must use `MaterialTheme.colorScheme.*` tokens. If any screen hardcodes `Color(0xFF313131)` instead of `MaterialTheme.colorScheme.onBackground`, dark mode breaks for that screen. Audit all Agent 1–4 files.

---

## EMPTY STATE COMPOSABLE

```kotlin
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = TextStyle(
            fontFamily = CormorantGaramond, fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        ))
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = TextStyle(
            fontFamily = CormorantGaramond, fontStyle = FontStyle.Italic,
            fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary
        ), textAlign = TextAlign.Center)
    }
}
```

Ensure Agents 3 and 4 use this (or equivalent). If they rolled their own, keep theirs and skip this — no duplication.

---

## SPLASH SCREEN

`res/values/themes.xml`:
```xml
<style name="Theme.ProactiveDiary.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">#F3EEE7</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="postSplashScreenTheme">@style/Theme.ProactiveDiary</item>
</style>
```

`MainActivity.onCreate`: call `installSplashScreen()` before `super.onCreate()`.

---

## APP INITIALIZATION

Update `ProactiveDiaryApp.kt`:
```kotlin
@HiltAndroidApp
class ProactiveDiaryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
    }
}
```

Update `MainActivity.kt`:
- Read `intent.getStringExtra("destination")` for notification deep links.
- Pass to NavHost so it navigates to the correct tab on launch.

---

## NOTIFICATION DEEP LINK HANDLING

When a notification is tapped, `MainActivity` receives an intent with `"destination"` extra:
- `"write"` → navigate to Write tab
- `"goals"` → navigate to Goals tab

```kotlin
// In MainActivity, after setContent:
val destination = intent?.getStringExtra("destination")
if (destination != null) {
    LaunchedEffect(destination) {
        when (destination) {
            "write" -> innerNavController.navigate("write")
            "goals" -> innerNavController.navigate("goals")
        }
    }
}
```

---

## END-TO-END SMOKE TEST

This is the test you must pass before declaring done:

```
 1. Fresh install → Typewriter screen (quote animates)
 2. "Now write yours." → swipe up → Design Studio
 3. Pick Wine Red, Dotted pages, add initials "AG" → "Start Writing"
 4. Set goal "10k steps" daily 6:00 AM → Set writing reminder 8:00 PM → Done
 5. Main app opens on Write tab (Wine Red background, dotted lines)
 6. Write: "Today I went for a long run in the park" → auto-saves
 7. Navigate to Journal → entry appears with preview
 8. Search "run" → entry found with "run" highlighted
 9. Navigate to Goals → "10k steps" visible → Check In
10. Settings → Dark Mode on → all screens dark
11. Settings → Dark Mode off → back to light
12. Kill app → reopen → goes straight to Write tab (no onboarding)
13. After 7 days (simulate by changing trial_start_date) → tap Write → PaywallDialog
14. Reading journal still works (no paywall for reads)
```

---

## Acceptance Criteria

| # | Criterion | Verification |
|---|-----------|-------------|
| 1 | Fresh install → Typewriter → Design Studio → Goals → Write | Full onboarding flow |
| 2 | Returning user → straight to Write tab | Kill app, reopen |
| 3 | Bottom nav: all 4 tabs work, selection state visible | Tap each tab |
| 4 | Settings: all rows render, taps work | Visual + functional test |
| 5 | Settings: Design Studio opens in edit mode | Selections pre-populated |
| 6 | Settings: dark mode toggles all screens | Toggle, navigate through app |
| 7 | Settings: font size changes Write text | Set Large, check Write |
| 8 | Settings: export JSON produces valid file | Export, open file |
| 9 | Settings: export text produces readable file | Export, open file |
| 10 | Settings: delete all → two confirmations → wipe → typewriter | Full flow |
| 11 | Trial: starts on first launch, counts down | Check preference |
| 12 | Paywall: blocks writing after 7 days | Simulate expired trial |
| 13 | Paywall: reading/browsing remains free | Browse journal when expired |
| 14 | Paywall: purchase flow launches | Tap plan card |
| 15 | Paywall: restore purchases works | Call restore |
| 16 | Dark mode: all screens correct in both modes | Navigate everywhere in dark |
| 17 | Empty states shown appropriately | Fresh install journal, goals |
| 18 | Notification deep links: open correct screen | Tap notification |
| 19 | Splash screen renders with warm paper | Cold launch |
| 20 | Full 14-step smoke test passes | Manual walkthrough |
