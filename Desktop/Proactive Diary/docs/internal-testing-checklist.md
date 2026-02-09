# Proactive Diary - Internal Testing Track Checklist

## Prerequisites

### 1. Google Play Console Account
- [ ] Developer account created ($25 one-time fee)
- [ ] Account identity verified
- [ ] App created in Play Console (com.proactivediary)

### 2. Signing Configuration
- [ ] Release keystore created and stored securely
- [ ] `keystore.properties` file configured with:
  ```
  storeFile=../release.keystore
  storePassword=<password>
  keyAlias=<alias>
  keyPassword=<password>
  ```
- [ ] **IMPORTANT:** Back up keystore file — losing it means you can never update the app

### 3. Firebase Configuration
- [ ] `google-services.json` matches your Firebase project
- [ ] Firebase Analytics enabled in console
- [ ] Firebase Crashlytics enabled in console
- [ ] Firebase Auth enabled (Google + Email/Password providers)

### 4. Google Play Billing
- [ ] In-app products created in Play Console:
  - `pd_monthly` - Monthly subscription
  - `pd_annual` - Annual subscription
- [ ] Products activated and pricing set for all target countries
- [ ] License testers added (your email + team emails)

---

## Build Release APK/Bundle

```bash
# Set environment
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
export ANDROID_HOME="/c/Users/user/AppData/Local/Android/Sdk"

# Build release bundle (preferred for Play Store)
cd "C:/Users/user/Desktop/Proactive Diary"
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab

# Or build release APK (for direct distribution)
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

---

## Internal Testing Track Setup

### Step 1: Create Internal Testing Release
1. Go to Play Console > Proactive Diary > Testing > Internal testing
2. Click "Create new release"
3. Upload `app-release.aab` from `app/build/outputs/bundle/release/`
4. Set release name: "1.0.0 (1) - Internal"
5. Add release notes: "Initial internal testing release"
6. Review and roll out

### Step 2: Add Testers
1. Go to Internal testing > Testers
2. Create a new email list (e.g., "Internal Testers")
3. Add tester emails (must be Google accounts)
4. Save and copy the opt-in URL
5. Share opt-in URL with testers

### Step 3: Tester Flow
1. Tester opens opt-in URL on their Android device
2. Accepts the testing invitation
3. Downloads app from Play Store (may take a few minutes to appear)
4. Tests all flows: onboarding, writing, goals, billing, settings

---

## Play Store Listing Setup

### Required Information
- [ ] **App name:** Proactive Diary
- [ ] **Short description:** (see docs/play-store-listing.md)
- [ ] **Full description:** (see docs/play-store-listing.md)
- [ ] **Category:** Productivity
- [ ] **Contact email:** support@proactivediary.com
- [ ] **Privacy policy URL:** https://proactivediary.com/privacy-policy

### Required Assets
- [ ] **App icon:** 512 x 512 PNG (high-res, no transparency)
- [ ] **Feature graphic:** 1024 x 500 PNG/JPG
- [ ] **Phone screenshots:** 4-8 screenshots (min 1080 x 1920)
- [ ] **7-inch tablet screenshots:** 1-8 (optional but recommended)
- [ ] **10-inch tablet screenshots:** 1-8 (optional but recommended)

### Content Rating
- [ ] Complete IARC content rating questionnaire
- [ ] Expected rating: Everyone

### Target Audience
- [ ] App does NOT target children under 13
- [ ] Select "18 and over" or "Everyone" as target age group

### Data Safety
- [ ] Fill out Data Safety section:
  - App collects: Email (optional, for sign-in), crash logs, analytics
  - App does NOT collect: diary content, location, contacts, files
  - Data is NOT shared with third parties
  - Data can be deleted by user (Settings > Delete All Data)
  - Data is encrypted in transit (HTTPS)

---

## Pre-Launch Testing Checklist

### Core Flows
- [ ] Fresh install → Typewriter → Design Studio → Goals → Write
- [ ] Write an entry with title, content, mood, tags
- [ ] Auto-save triggers after 5 seconds
- [ ] Navigate to Journal → entry appears
- [ ] Search for entry by content
- [ ] Search for entry by date
- [ ] View entry detail → edit → save
- [ ] Set a goal → check in → streak increments
- [ ] Set writing reminder → notification fires
- [ ] Export entries as JSON
- [ ] Export entries as Text
- [ ] Delete all data → app resets to typewriter

### Billing Flows
- [ ] Free trial: write 10 entries, paywall appears on 11th
- [ ] Paywall dialog shows correct prices
- [ ] Monthly purchase completes successfully
- [ ] Annual purchase completes successfully
- [ ] Restore purchases works
- [ ] App works offline (cached billing state)

### Edge Cases
- [ ] Kill app during write → auto-save preserved
- [ ] Rotate device → state preserved
- [ ] Dark mode toggle → all screens render correctly
- [ ] Large font size → no text clipping
- [ ] Empty states (no entries, no goals, no search results)
- [ ] Device reboot → alarms reschedule

### Performance
- [ ] App launches in < 2 seconds
- [ ] Write screen is responsive (no typing lag)
- [ ] Journal scrolls smoothly
- [ ] Memory usage stays under 150MB

---

## Version Checklist

Current: `versionCode = 1`, `versionName = "1.0.0"`

For each subsequent release:
- Increment `versionCode` by 1
- Update `versionName` following semver (1.0.1, 1.1.0, etc.)
- File: `app/build.gradle.kts` lines 27-28
