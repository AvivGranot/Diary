# Shared Contracts — Cross-Agent Type Surface

This document is the single source of truth for all types, keys, and interfaces shared across agents. If this document and an agent PDR disagree, this document wins.

---

## Preference Keys

The `preferences` table is the shared bus. These are the keys, their types, and who writes/reads them.

| Key | Value type | Written by | Read by | Default |
|-----|-----------|------------|---------|---------|
| `first_launch_completed` | `"true"` | Agent 1 | Agent 1 | absent (= first launch) |
| `diary_color` | string enum (see below) | Agent 2 | Agent 3, 5 | `"cream"` |
| `diary_form` | `"focused"\|"spacious"\|"compact"` | Agent 2 | Agent 3 | `"focused"` |
| `diary_texture` | `"paper"\|"parchment"\|"linen"\|"smooth"\|"dark"` | Agent 2 | Agent 3 | `"paper"` |
| `diary_canvas` | `"lined"\|"blank"\|"dotted"\|"grid"\|"numbered"` | Agent 2 | Agent 3 | `"lined"` |
| `diary_details` | JSON array of strings | Agent 2 | Agent 3 | `'["auto_save","word_count","date_header","daily_quote"]'` |
| `diary_mark_text` | string (max 20 chars) | Agent 2 | Agent 3 | `""` |
| `diary_mark_position` | `"header"\|"footer"` | Agent 2 | Agent 3 | `"header"` |
| `diary_mark_font` | `"serif"\|"sans"` | Agent 2 | Agent 3 | `"serif"` |
| `design_completed` | `"true"` | Agent 2 | Agent 5 | absent |
| `onboarding_completed` | `"true"` | Agent 4 | Agent 5 | absent |
| `dark_mode` | `"true"\|"false"` | Agent 5 | Agent 5 (Theme) | `"false"` |
| `font_size` | `"small"\|"medium"\|"large"` | Agent 5 | Agent 3 | `"medium"` |
| `trial_start_date` | epoch millis as string | Agent 5 | Agent 5 | absent (set on first launch) |

---

## Color Key → Hex Mapping

Used by Agent 2 (color chips) and Agent 3 (Write background).

| Key | Display Name | Hex | Text Color |
|-----|-------------|-----|------------|
| `cream` | Cream | `#F3EEE7` | dark (`#313131`) |
| `blush` | Blush | `#F0E0D6` | dark |
| `sage` | Sage | `#E4E8DF` | dark |
| `sky` | Sky | `#DDE4EC` | dark |
| `lavender` | Lavender | `#E4DEE8` | dark |
| `midnight` | Midnight | `#2C2C34` | light (`#E8E7E5`) |
| `charcoal` | Charcoal | `#30302E` | light |
| `wine_red` | Wine Red | `#8B3A3A` | light |
| `forest` | Forest | `#3A5A40` | light |
| `ocean` | Ocean | `#2C5F7C` | light |

---

## Mood Values

Stored in `EntryEntity.mood` as lowercase string. `null` means no mood set.

| Value | Display | Color (for circles) |
|-------|---------|---------------------|
| `"great"` | Great | `#5B8C5A` |
| `"good"` | Good | `#8FAD88` |
| `"neutral"` | Neutral | `#A0A09E` |
| `"bad"` | Bad | `#8B7B8B` |
| `"terrible"` | Terrible | `#8B5A5A` |

---

## Day-of-Week Encoding

In `reminder_days` and `GoalEntity.reminderDays` JSON arrays:

| Int | Day |
|-----|-----|
| 0 | Monday |
| 1 | Tuesday |
| 2 | Wednesday |
| 3 | Thursday |
| 4 | Friday |
| 5 | Saturday |
| 6 | Sunday |

---

## Date/Time Formats

| Field | Format | Example |
|-------|--------|---------|
| `EntryEntity.createdAt` | epoch millis (UTC) | `1707350400000` |
| `GoalCheckInEntity.date` | `"yyyy-MM-dd"` local | `"2026-02-08"` |
| `GoalEntity.reminderTime` | `"HH:mm"` 24h | `"06:00"` |
| `WritingReminderEntity.time` | `"HH:mm"` 24h | `"20:00"` |

---

## Navigation Routes

| Route | Arguments | Used by |
|-------|-----------|---------|
| `typewriter` | none | Agent 1 |
| `design_studio?edit={edit}` | `edit: Boolean = false` | Agent 2, Agent 5 |
| `onboarding_goals` | none | Agent 4 |
| `write?entryId={entryId}` | `entryId: String? = null` | Agent 3 |
| `journal` | none | Agent 3 |
| `goals` | none | Agent 4 |
| `settings` | none | Agent 5 |
| `entry/{entryId}` | `entryId: String` | Agent 3 |

---

## Notification Intent Extras

Sent via `PendingIntent` from `AlarmReceiver`:

| Extra Key | Type | Values |
|-----------|------|--------|
| `type` | String | `"writing"`, `"goal"`, `"fallback_check"` |
| `reminder_id` | String | UUID of the reminder/goal |
| `label` | String | Notification body text |
| `fallback_enabled` | Boolean | Whether to schedule fallback |
| `goal_title` | String | Goal name (for goal notifications) |

Sent to `MainActivity` for deep linking:

| Extra Key | Type | Values |
|-----------|------|--------|
| `destination` | String | `"write"`, `"goals"` |

---

## Notification Channel IDs

| ID | Name |
|----|------|
| `writing_reminders` | Writing Reminders |
| `goal_reminders` | Goal Reminders |

---

## Database Name

`"proactive_diary_db"` — used in `DatabaseModule.kt`. All agents that need raw DB access (e.g., `FallbackChecker`) use this name.

---

## Hilt Bindings

Agent 0 provides DAOs. Feature agents must bind their repository implementations:

| Interface | Implementation | Bound by |
|-----------|---------------|----------|
| `EntryRepository` | `EntryRepositoryImpl` | Agent 3 |
| `GoalRepository` | `GoalRepositoryImpl` | Agent 4 |
| `ReminderRepository` | `ReminderRepositoryImpl` | Agent 4 |
| `SearchEngine` | `SearchEngineImpl` | Agent 3 |

Each agent creates a Hilt `@Module` with `@Binds` for their implementations, or adds `@Provides` to existing modules.

---

## Design Token Quick Reference

### Colors (use via MaterialTheme.colorScheme)

| Token | Light | Dark | MaterialTheme mapping |
|-------|-------|------|-----------------------|
| Paper | `#F3EEE7` | `#1A1918` | `.background` |
| Ink | `#313131` | `#E8E7E5` | `.onBackground`, `.onSurface` |
| Pencil | `#585858` | `#A0A09E` | `.secondary` |
| Parchment | `#FAF9F5` | `#2A2928` | `.surface` |

### Typography (use via MaterialTheme.typography)

| Style | Font | Size | Weight | Use |
|-------|------|------|--------|-----|
| `headlineLarge` | Cormorant Garamond | 24sp | Regular | Section titles |
| `headlineMedium` | Cormorant Garamond | 20sp | Italic | Typewriter quote |
| `bodyLarge` | Roboto | 14sp | Regular | Body text |
| `labelSmall` | Roboto | 11sp | Regular | Section numbers, labels |
| `titleMedium` | Cormorant Garamond | 16sp | Regular | Nav logo |

### Common Patterns

- Divider: `Pencil.copy(alpha = 0.15f)`, 1px, 24dp vertical margin
- Card: Parchment background, 8dp radius
- Button: Ink background, Parchment text, Roboto 14sp, letter-spacing 1sp, 4dp radius
- Shadow: `Ink.copy(alpha = 0.08f)`, 4dp blur, 2dp y-offset
