# AGENT 4 — Notifications + Goals

## Role

You build the proactive engine — the part of the app that reaches out to the user. Goals with streak tracking. Writing reminders. Fallback notifications. The onboarding flow where users set their first goals.

This is a diary that pushes you to write, not one that waits passively. Your code runs even when the app is closed. It survives reboots. It checks whether the user wrote today and nudges them if they didn't. That reliability is non-negotiable.

---

## What You Own

```
ui/goals/
├── GoalsScreen.kt               # Main goals list (tab in bottom nav)
├── GoalsViewModel.kt            # Goals CRUD, check-in state, streak calc
├── GoalCard.kt                  # Single goal: progress bar, streak, check-in
└── AddGoalDialog.kt             # Modal for create/edit goal

ui/onboarding/
├── OnboardingGoalsScreen.kt     # Onboarding: set goals + writing reminders
└── OnboardingRemindersSection.kt # Writing reminder picker section

ui/components/
├── ReminderPicker.kt            # Time + days + fallback toggle
├── DaySelector.kt               # M T W T F S S toggle row
└── FrequencySelector.kt         # Daily / Weekly / Monthly

data/repository/
├── GoalRepositoryImpl.kt        # Implements GoalRepository
└── ReminderRepositoryImpl.kt    # Implements ReminderRepository

notifications/
├── NotificationService.kt       # Schedule / cancel / reschedule
├── NotificationChannels.kt      # Channel creation
├── AlarmReceiver.kt             # BroadcastReceiver for alarms
├── BootReceiver.kt              # Re-register alarms after reboot
└── FallbackChecker.kt           # "Did the user write today?" check
```

## What You Touch But Don't Own

- `data/dao/GoalDao`, `GoalCheckInDao`, `WritingReminderDao`, `PreferenceDao`, `EntryDao`
- All entity types from those DAOs
- `navigation/Routes.kt`
- `ui/theme/*`
- `AndroidManifest.xml` — receivers are already declared by Agent 0, but verify they exist

---

## ONBOARDING GOALS SCREEN

Users arrive here from Design Studio. Single scrollable screen, two numbered sections.

### Layout

```
┌─────────────────────────────────────────┐
│                                         │
│  01 — Goals                             │
│  "What do you want to track?"           │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │  Goal title:  [10k steps        ]   ││
│  │  Frequency:   [Daily ▼]             ││
│  │  Remind at:   [06:00 AM]            ││
│  │  Days:        M T W T F S S         ││
│  └─────────────────────────────────────┘│
│                                         │
│  + Add another goal   (max 3)           │
│                                         │
│  ── divider ──                          │
│                                         │
│  02 — Reminders                         │
│  "When should we remind you to write?"  │
│                                         │
│  [x] Morning reflection    06:00 AM    │
│  [x] Evening journal       08:00 PM    │
│                                         │
│  + Add custom time                      │
│                                         │
│  [x] Fallback reminder                 │
│      If you haven't written,            │
│      we'll nudge you 30 min later       │
│                                         │
│  ── divider ──                          │
│                                         │
│  [     DONE — LET'S WRITE      ]       │
│  [         Skip for now         ]       │
│                                         │
└─────────────────────────────────────────┘
```

### Typography

Same Uncommon aesthetic as Design Studio:
- Section number: Roboto 11sp, letter-spacing 1.1sp, Pencil.
- Section title: Cormorant Garamond 24sp, regular, Ink.
- Subtitle: Cormorant Garamond 16sp, italic, Pencil.
- Divider: 1px, `Pencil.copy(alpha = 0.15f)`, 24dp vertical margins.

### Goal Input Card

- Background: Parchment. Corner radius 8dp. Padding 16dp.
- Title: `TextField`, Roboto 14sp, underline style, placeholder `"What's your goal?"`.
- Frequency: `FrequencySelector` — 3 chips: Daily / Weekly / Monthly.
  - Chip: Parchment background, 1dp border `Pencil.copy(alpha = 0.2f)`, Roboto 12sp.
  - Selected: Ink border, `Ink.copy(alpha = 0.04f)` background.
- Time: tap opens Android system `TimePickerDialog`. Display as `"06:00 AM"`.
- Days: `DaySelector` — 7 circles.

### "Add another goal"

- Text button: Cormorant Garamond italic 14sp, Ink.
- Maximum 3 goals during onboarding.
- Adding shows another goal card below.

### Writing Reminders Section

- Two pre-suggested rows, each with checkbox + label + time:
  - `[x] Morning reflection    06:00 AM`
  - `[x] Evening journal       08:00 PM`
- Checkbox: custom style. Unchecked: 1dp Pencil border. Checked: Ink fill + white checkmark.
- Tap the time to change it via `TimePickerDialog`.
- `"+ Add custom time"`: adds another reminder row (max 5 total).
- Each reminder has a label (editable) and time.

### Fallback Toggle

- Switch with description below.
- On: when a writing reminder fires and the user hasn't written today, fire a second notification 30 minutes later.
- Label: Roboto 14sp, Ink. `"Fallback reminder"`.
- Description: Roboto 12sp, Pencil. `"If you haven't written, we'll nudge you 30 min later"`.

### Done Button

- Same style as Design Studio footer: full width minus 32dp, 48dp height, Ink background, Parchment text.
- `"DONE — LET'S WRITE"` — Roboto 14sp, letter-spacing 1sp, uppercase.
- On tap:
  1. Save all goals to `goals` table.
  2. Save all writing reminders to `writing_reminders` table.
  3. Schedule all notifications via `NotificationService`.
  4. Write `PreferenceEntity("onboarding_completed", "true")`.
  5. Navigate to main app (Write tab).

### Skip

- `"Skip for now"` — Roboto 12sp, Pencil, centered below Done button.
- On tap: write `onboarding_completed = true`, navigate to main app. No goals or reminders saved.

---

## GOALS SCREEN (Main App Tab)

### Layout

```
┌─────────────────────────────────────────┐
│  Your Goals                              │
│                                         │
│  ┌─ GoalCard ──────────────────────────┐│
│  │  10k Steps                          ││
│  │  Daily · 6:00 AM                    ││
│  │  ████████████░░░░░░░░  71%          ││
│  │  🔥 5 day streak                    ││
│  │                     [Check In]      ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─ GoalCard ──────────────────────────┐│
│  │  ...                                ││
│  └─────────────────────────────────────┘│
│                                         │
│  + Add new goal                          │
│                                         │
└─────────────────────────────────────────┘
```

### Header

`"Your Goals"` — Cormorant Garamond 24sp, Ink. Top padding 16dp.

### GoalCard

- Background: Parchment. Corner radius 8dp. Padding 16dp. Bottom margin 12dp.
- Title: Cormorant Garamond 20sp, Ink.
- Subtitle: Roboto 13sp, Pencil. Format: `"Daily · 6:00 AM"`.
- Progress bar: 6dp height, 3dp corner radius.
  - Track: `Pencil.copy(alpha = 0.15f)`.
  - Fill: Ink.
  - Percentage text: Roboto 13sp, Ink, right of bar.
- Streak: Roboto 13sp, Pencil. Format: `"🔥 5 day streak"` (or `"No streak"` if 0).
- Check In button: right-aligned. Outlined: 1dp Ink border, Roboto 12sp, Ink text.
  - If already checked in today: filled Ink background, white text, checkmark icon. Disabled.
  - On tap: create `GoalCheckInEntity` for today with `completed = true`.

### Progress Calculation

```
Daily:   (check-ins this calendar month with completed=true) / (days elapsed this month) × 100
Weekly:  (weeks this month with ≥1 check-in) / (weeks elapsed this month) × 100
Monthly: (months this year with ≥1 check-in) / (months elapsed this year) × 100
```

### Streak Calculation

```kotlin
fun calculateStreak(checkIns: List<GoalCheckInEntity>): Int {
    val completedDates = checkIns
        .filter { it.completed }
        .map { LocalDate.parse(it.date) }
        .toSortedSet(compareByDescending { it })
        .toList()

    if (completedDates.isEmpty()) return 0

    val today = LocalDate.now()
    // Streak must include today or yesterday
    if (completedDates.first() != today && completedDates.first() != today.minusDays(1)) return 0

    var streak = 1
    for (i in 0 until completedDates.size - 1) {
        if (completedDates[i].minusDays(1) == completedDates[i + 1]) {
            streak++
        } else break
    }
    return streak
}
```

### Long Press on GoalCard

Show bottom sheet or popup menu:
- **Edit** → opens `AddGoalDialog` pre-filled with goal data.
- **Delete** → confirmation dialog: `"Delete this goal and all check-ins?"` → Cancel / Delete.
  - On delete: remove goal, cancel its scheduled reminders.

### Add New Goal

- `"+ Add new goal"` — Cormorant Garamond italic 14sp, Ink.
- Opens `AddGoalDialog`.

### AddGoalDialog

- Modal dialog with same fields as onboarding goal input (title, frequency, time, days).
- Save: insert goal + schedule notification.
- Cancel: dismiss.
- If editing: pre-fill fields, update on save.

### Empty State

No goals: `"No goals yet"` / `"Set a goal to start tracking your progress"` — centered, same empty state styling.

---

## NOTIFICATION SYSTEM

### Channels (NotificationChannels.kt)

Create on app startup (idempotent):

```kotlin
object NotificationChannels {
    const val WRITING = "writing_reminders"
    const val GOALS = "goal_reminders"

    fun create(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(NotificationChannel(
            WRITING, "Writing Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Reminders to write in your diary" })

        manager.createNotificationChannel(NotificationChannel(
            GOALS, "Goal Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Reminders to check in on your goals" })
    }
}
```

### NotificationService

```kotlin
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderDao: WritingReminderDao,
    private val goalDao: GoalDao,
    private val entryDao: EntryDao
) {
    fun scheduleWritingReminder(reminder: WritingReminderEntity)
    fun scheduleGoalReminder(goal: GoalEntity)
    fun scheduleFallbackCheck(reminderTime: String)   // 30min after reminder
    fun cancelReminder(reminderId: String)
    fun cancelGoalReminder(goalId: String)
    suspend fun rescheduleAll()                        // after boot
}
```

**Scheduling pattern:**

For each active day in the reminder's `days` JSON array:
1. Calculate next occurrence of that day-of-week + time.
2. Use `AlarmManager.setExactAndAllowWhileIdle()` for each.
3. `PendingIntent` extras: `type` ("writing" | "goal" | "fallback_check"), `reminder_id`, `label`, `fallback_enabled`, `goal_title`.
4. Request codes: hash of `"${reminderId}_${dayOfWeek}"` to avoid collisions.

After each alarm fires, the receiver must schedule the next occurrence (7 days later) — `setExactAndAllowWhileIdle` is one-shot.

### AlarmReceiver

```kotlin
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: return

        when (type) {
            "writing" -> {
                showNotification(
                    context = context,
                    title = "Time to write",
                    body = intent.getStringExtra("label") ?: "Open your diary",
                    channel = NotificationChannels.WRITING,
                    deepLink = "proactivediary://write"
                )
                // Reschedule for next week (same day, same time)
                rescheduleNextWeek(context, intent)
                // Schedule fallback if enabled
                if (intent.getBooleanExtra("fallback_enabled", false)) {
                    scheduleFallbackIn30Min(context, intent)
                }
            }
            "goal" -> {
                showNotification(
                    context = context,
                    title = "Goal: ${intent.getStringExtra("goal_title") ?: "Check in"}",
                    body = "Time to check in!",
                    channel = NotificationChannels.GOALS,
                    deepLink = "proactivediary://goals"
                )
                rescheduleNextWeek(context, intent)
            }
            "fallback_check" -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val hasWrittenToday = FallbackChecker.hasWrittenToday(context)
                        if (!hasWrittenToday) {
                            showNotification(
                                context = context,
                                title = "You haven't written yet today",
                                body = "Even a few words count. Open your diary?",
                                channel = NotificationChannels.WRITING,
                                deepLink = "proactivediary://write"
                            )
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
```

### FallbackChecker

```kotlin
object FallbackChecker {
    suspend fun hasWrittenToday(context: Context): Boolean {
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "proactive_diary_db").build()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + 86_400_000L - 1
        val entries = db.entryDao().getByDateRangeSync(startOfDay, endOfDay)
        db.close()
        return entries.isNotEmpty()
    }
}
```

**Note:** You'll need to add a synchronous date-range query to `EntryDao`:
```kotlin
@Query("SELECT * FROM entries WHERE created_at BETWEEN :start AND :end")
fun getByDateRangeSync(start: Long, end: Long): List<EntryEntity>
```

If Agent 0's DAO doesn't have this, add it.

### BootReceiver

```kotlin
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.IO).launch {
            // Get NotificationService via Hilt EntryPoint or manual construction
            // Call rescheduleAll()
        }
    }
}
```

`rescheduleAll()`:
1. Query all active writing reminders.
2. Query all active goals with reminder times.
3. Schedule each one fresh.

### Notification Tap Behavior

Each notification's `PendingIntent` should launch `MainActivity` with an intent extra:
- `"destination" = "write"` for writing reminders
- `"destination" = "goals"` for goal reminders

`MainActivity` reads this extra and passes it to the NavHost to navigate on launch.

### Permission Handling

On Android 13+ (API 33), request `POST_NOTIFICATIONS` before scheduling:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
}
```

Request this in `OnboardingGoalsScreen` when user taps "Done". If denied, save goals/reminders but show a `Snackbar`: `"Notifications disabled. You can enable them in Settings."`.

---

## Repository Implementations

### GoalRepositoryImpl

```kotlin
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val checkInDao: GoalCheckInDao
) : GoalRepository {
    override fun getActiveGoals() = goalDao.getActiveGoals()
    override fun getGoalById(id: String) = goalDao.getById(id)
    override fun getCheckInsForGoal(goalId: String) = checkInDao.getByGoalId(goalId)
    override suspend fun insertGoal(goal: GoalEntity) = goalDao.insert(goal)
    override suspend fun updateGoal(goal: GoalEntity) = goalDao.update(goal)
    override suspend fun deleteGoal(goalId: String) {
        goalDao.deleteById(goalId)
        // Foreign key CASCADE handles check-in deletion
    }
    override suspend fun checkIn(checkIn: GoalCheckInEntity) = checkInDao.insert(checkIn)
    override suspend fun getCheckInForToday(goalId: String, date: String) =
        checkInDao.getByGoalAndDate(goalId, date)
}
```

### ReminderRepositoryImpl

```kotlin
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: WritingReminderDao
) : ReminderRepository {
    override fun getActiveReminders() = reminderDao.getActive()
    override fun getAllReminders() = reminderDao.getAll()
    override suspend fun insertReminder(reminder: WritingReminderEntity) = reminderDao.insert(reminder)
    override suspend fun updateReminder(reminder: WritingReminderEntity) = reminderDao.update(reminder)
    override suspend fun deleteReminder(id: String) = reminderDao.deleteById(id)
}
```

Bind both via Hilt `@Binds` or `@Provides`.

---

## Acceptance Criteria

| # | Criterion | Verification |
|---|-----------|-------------|
| 1 | Onboarding: set 1-3 goals with all fields | Fill in, tap Done, query goals table |
| 2 | Onboarding: set writing reminders (preset + custom) | Enable reminders, query writing_reminders table |
| 3 | Onboarding: fallback toggle persists | Toggle on, query the reminder entity's fallbackEnabled |
| 4 | Onboarding: "Done" saves + schedules + navigates | Everything persists, app opens on Write tab |
| 5 | Onboarding: "Skip" works without crash | Navigates to Write, no goals saved |
| 6 | Goals screen: lists all goals with progress + streak | Create goals, check in, observe UI |
| 7 | Goals screen: check-in button creates record, disables | Tap Check In, tap again — disabled |
| 8 | Goals screen: add/edit/delete works | Full CRUD cycle |
| 9 | Streak: consecutive days counted correctly | Check in 3 days in a row — streak = 3 |
| 10 | Progress: percentage calculated per frequency | Daily: 5/28 = 18% in February |
| 11 | Writing notification fires at set time | Set alarm for 1 min from now, wait |
| 12 | Goal notification fires at set time | Same test |
| 13 | Fallback fires 30 min later if no entry today | Set alarm, don't write, wait 30 min |
| 14 | Notification tap opens correct screen | Tap writing notif → Write, goal notif → Goals |
| 15 | Boot: alarms survive reboot | Schedule, reboot emulator, notifications still fire |
| 16 | POST_NOTIFICATIONS requested on API 33+ | Test on API 33 emulator |
| 17 | Permission denial: graceful degradation | Deny permission — no crash, toast shown |
