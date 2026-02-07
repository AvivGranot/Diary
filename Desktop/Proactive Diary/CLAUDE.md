# Proactive Diary

## App Overview
- **Name:** Proactive Diary
- **Package:** com.proactivediary
- **Type:** Native Android app - proactive self-diary (notes + reminders + goals)
- **Privacy:** All data local. No network calls except Google Play Billing.

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Navigation:** Compose Navigation
- **DI:** Hilt
- **Database:** Room + SQLite FTS4 (full-text search)
- **State:** ViewModel + StateFlow
- **Notifications:** AlarmManager + NotificationCompat
- **Billing:** Google Play Billing Library v7
- **Image Loading:** Coil
- **Fonts:** Cormorant Garamond (bundled) + Roboto (system)

## Architecture
- MVVM: ViewModel + Repository + Room
- Single Activity (Compose Navigation)
- Hilt dependency injection throughout

## Design System (Uncommon Aesthetic)
- Warm paper backgrounds, serif headings, minimal chrome
- **Colors:** Paper #F3EEE7, Ink #313131, Pencil #585858, Parchment #FAF9F5
- **Dark:** Paper #1A1918, Ink #E8E7E5, Pencil #A0A09E, Parchment #2A2928
- **Typography:** Cormorant Garamond for headings/titles, Roboto for body/labels
- **Dividers:** rgba(88,88,88, 0.15)

## Screen Flow
1. Typewriter Opening (first launch) → 2. Design Studio → 3. Goals Onboarding
4. Write (main tab) | 5. Journal | 6. Goals | 7. Settings

## Build Commands
```
./gradlew assembleDebug
./gradlew installDebug
```

## Project Structure
```
app/src/main/java/com/proactivediary/
  di/             - Hilt modules
  data/db/        - Room database, entities, DAOs
  data/repository - Repository implementations
  domain/model/   - Domain models (Mood, GoalFrequency, DiaryTheme)
  domain/search/  - FTS search engine
  ui/theme/       - Color, Typography, Theme
  ui/typewriter/  - Typewriter opening screen
  ui/designstudio/- Diary customization
  ui/onboarding/  - Goals onboarding
  ui/write/       - Diary writing
  ui/journal/     - Entry browsing + search
  ui/goals/       - Goal tracking
  ui/settings/    - App settings
  ui/paywall/     - Subscription gate
  ui/components/  - Shared composables
  navigation/     - NavGraph, Routes
  notifications/  - AlarmReceiver, BootReceiver
```
