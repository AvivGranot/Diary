# AGENT 2 — Design Studio Screen

## Role

You build the customization experience. The user designs how their diary looks and feels. This screen follows the Uncommon website's essay-list aesthetic: numbered sections, serif titles, italic subtitles, warm paper, zero decorative icons. The user scrolls through 6 sections, makes choices, and taps "Start Writing."

Everything the user selects here flows downstream. Agents 3 and 5 read your preferences to style the Write screen, Journal cards, and dark mode. Your data contract is the `preferences` table — get the keys right.

---

## What You Own

```
ui/designstudio/
├── DesignStudioScreen.kt          # Main screen + LazyColumn layout
├── DesignStudioViewModel.kt       # State management, preference persistence
├── DiaryPreview.kt                # Top preview composable (updates in real-time)
├── sections/
│   ├── SoulSection.kt             # 01 — Color scheme picker
│   ├── FormSection.kt             # 02 — Layout style
│   ├── TouchSection.kt            # 03 — Background texture
│   ├── CanvasSection.kt           # 04 — Page line style
│   ├── DetailsSection.kt          # 05 — Feature toggles
│   └── MarkSection.kt             # 06 — Personalization input
└── components/
    ├── SectionHeader.kt           # Reusable: number + title + subtitle + divider
    ├── ColorChip.kt               # Circle with selection state
    ├── DesignSummaryCard.kt       # Summary of all selections
    ├── StickyFooter.kt            # "Start Writing" button
    └── DesignNavBar.kt            # Fixed top bar
```

## What You Touch But Don't Own

- `data/dao/PreferenceDao` — save/load all design selections
- `data/entities/PreferenceEntity` — key-value writes
- `navigation/Routes.kt` — navigate to `Routes.OnboardingGoals`
- `ui/theme/*` — `MaterialTheme.colorScheme`, `CormorantGaramond`, `MaterialTheme.typography`
- `domain/model/DiaryTheme.kt` — `DiaryThemeConfig` data class (you populate it)

---

## Visual Language Rules

These rules apply to every pixel on this screen:

1. **No icons.** Text does all the work. The only graphical elements are color chips, texture previews, and the diary preview.
2. **No brand color.** The palette is monochromatic warm. The only color comes from the user's selection.
3. **Dividers:** 1px, `MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)`, 24dp vertical margin above and below.
4. **All motion:** opacity + translateY, 800ms, ease-out. This is the single animation curve for the entire screen.
5. **Section pattern:** Every section follows `SectionHeader` → content → divider. No exceptions.

---

## Layout Structure

```
┌─ DesignNavBar (fixed, 56dp) ─────────────────────┐
│  [≡]        U N C O M M O N        [♡]           │
├───────────────────────────────────────────────────┤
│                                                   │
│  ┌─ DiaryPreview (300dp) ───────────────────────┐ │
│  │   [diary mockup, updates with selections]    │ │
│  │   WINE RED · DOTTED · PAPER                  │ │
│  └──────────────────────────────────────────────┘ │
│                                                   │
│  ── divider ──                                    │
│                                                   │
│  01  Soul                                         │
│  "The color of your diary..."                     │
│  ○ ○ ● ○ ○ ○ ○ ○ ○ ○   (color chips)            │
│  Wine Red                                         │
│                                                   │
│  ── divider ──                                    │
│                                                   │
│  02  Form                                         │
│  ...                                              │
│  (continues for all 6 sections)                   │
│                                                   │
│  ┌─ DesignSummaryCard ─────────────────────────┐  │
│  │  WINE RED · FOCUSED · PAPER                 │  │
│  │  Dotted pages · Word count · Date header    │  │
│  │  Monogram: 'AG' — header, serif             │  │
│  └─────────────────────────────────────────────┘  │
│                                                   │
│  (80dp bottom padding for footer clearance)       │
├───────────────────────────────────────────────────┤
│  ┌─ StickyFooter (72dp) ───────────────────────┐  │
│  │  [       START WRITING       ]              │  │
│  └─────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────┘
```

---

## DesignNavBar (56dp, fixed top)

- Left: two horizontal lines (not three). Tap does nothing for MVP.
- Center: "UNCOMMON" — `CormorantGaramond`, 16sp, letter-spacing 3sp, uppercase, `MaterialTheme.colorScheme.onBackground`.
- Right: Heart outline icon, non-functional for MVP.
- Background: same as page background (no elevation, no shadow).

---

## DiaryPreview (Section A)

- Height: 300dp.
- Center: a styled rectangle representing the diary. Apply the selected color as fill, rounded corners 8dp, drop shadow `4dp blur, 2dp y-offset, Shadow color`.
- Show canvas lines (dots/lines/grid) on the preview surface as a miniature representation.
- Below: config label — Roboto 11sp, letter-spacing 1.5sp, uppercase, `Pencil` color.
  - Format: `"WINE RED · DOTTED · PAPER"` (updates reactively).
- Cross-fade (150ms) when any selection changes.

---

## Section 01 — Soul (Color Scheme)

**SectionHeader:**
- Number: `"01"` — Roboto 11sp, letter-spacing 1.1sp, Pencil color.
- Title: `"Soul"` — Cormorant Garamond 24sp, regular, Ink color, letter-spacing 0.3sp.
- Subtitle: `"The color of your diary is the first thing you see every morning you reach for it"` — Cormorant Garamond 16sp, italic, Pencil color.

**Content: Horizontal LazyRow, 12dp spacing.**

10 color chips:

| Key | Display Name | Hex |
|-----|-------------|-----|
| `cream` | Cream | `#F3EEE7` |
| `blush` | Blush | `#F0E0D6` |
| `sage` | Sage | `#E4E8DF` |
| `sky` | Sky | `#DDE4EC` |
| `lavender` | Lavender | `#E4DEE8` |
| `midnight` | Midnight | `#2C2C34` |
| `charcoal` | Charcoal | `#30302E` |
| `wine_red` | Wine Red | `#8B3A3A` |
| `forest` | Forest | `#3A5A40` |
| `ocean` | Ocean | `#2C5F7C` |

**ColorChip.kt:**
- 56dp diameter circle.
- Fill: the color.
- Unselected: 1dp border, `Pencil.copy(alpha = 0.2f)`.
- Selected: 2dp border `Ink`, scale 1.0→1.12 over 200ms ease-out.
- Below selected chip: color name fades in (Cormorant Garamond 14sp, Ink, centered, 150ms fade).
- Haptic on selection: `HapticFeedbackType.TextHandleMove`.

---

## Section 02 — Form (Layout Style)

**Subtitle:** `"How your thoughts fill the page shapes how they fill your mind"`

Three vertical cards, full width minus 32dp padding, each 72dp tall, background Parchment.

| Key | Name | Description |
|-----|------|------------|
| `focused` | Focused | Single column, generous margins. For deep writing. |
| `spacious` | Spacious | Wide layout with breathing room. For long reflections. |
| `compact` | Compact | Tight layout, more words per screen. For quick captures. |

- Left: name (Cormorant Garamond 20sp, Ink).
- Right: description (Cormorant Garamond italic 13sp, Pencil).
- Selected: 3dp left border Ink, background `Ink.copy(alpha = 0.04f)`. Unselected cards dim to 50% opacity (200ms transition).

---

## Section 03 — Touch (Background Texture)

**Subtitle:** `"The surface beneath your words matters as much as the words themselves"`

Vertical list, each 96dp card height:

| Key | Name | Description |
|-----|------|------------|
| `paper` | Paper | Classic warm paper feel. The default. |
| `parchment` | Parchment | Aged, textured warmth. Like a real journal. |
| `linen` | Linen | Subtle woven texture. Soft and organic. |
| `smooth` | Smooth | Clean, no texture. Pure minimalism. |
| `dark` | Dark | Rich dark surface. For night writers. |

- Left: 72×72dp preview rectangle with subtle visual differentiation (color tint or pattern). Rounded 4dp.
- Right: name (Cormorant Garamond 18sp), description (Roboto 13sp, Pencil).
- Selected: same left-border treatment as Form.

---

## Section 04 — Canvas (Page Lines)

**Subtitle:** `"The lines on your page shape the thoughts you put on them"`

Horizontal pager, full width, 200dp height. Each page shows a visual miniature of the line style drawn on a card.

| Key | Name | Description |
|-----|------|------------|
| `lined` | Lined | Traditional guidance for flowing thoughts |
| `blank` | Blank | Complete freedom. No boundaries. |
| `dotted` | Dotted | Subtle guidance without rigidity |
| `grid` | Grid | Structure for the organized mind |
| `numbered` | Numbered | Every line counts. Literally. |

Below pager:
- Style name: Cormorant Garamond 18sp, Ink.
- Description: Cormorant Garamond italic 14sp, Pencil.
- 5 page indicator dots (5dp, 8dp spacing). Active: Ink. Inactive: `Pencil.copy(alpha = 0.3f)`.

---

## Section 05 — Details (Feature Toggles)

**Subtitle:** `"The small things are never small"`

Vertical list, each row 52dp:

| Key | Label | Description | Default |
|-----|-------|------------|---------|
| `auto_save` | Auto-save indicator | Show a subtle dot when saving | ON |
| `word_count` | Word count | Display words written at the bottom | ON |
| `mood_prompt` | Mood prompt | Ask how you're feeling before you write | OFF |
| `daily_quote` | Daily quote | Show an inspiring quote when you open your diary | ON |
| `date_header` | Date header | Display today's date above each entry | ON |

- Toggle switch: custom, matching palette. Track off: `Pencil.copy(alpha = 0.2f)`. Track on: Ink. Thumb: white.
- Label: Roboto 14sp, Ink.
- Description: Roboto 12sp, Pencil.

---

## Section 06 — Mark (Personalization)

**Subtitle:** `"Leave your name where it matters"`

- Text input: underline-only (no box). 48dp height.
- Placeholder: `"Your initials, a word, a reminder..."` — Cormorant Garamond italic 14sp, `Pencil.copy(alpha = 0.5f)`.
- Input text: Cormorant Garamond 16sp, Ink.
- Underline: Pencil default, Ink on focus.
- Character counter: Roboto 11sp, Pencil, right-aligned — `"0 / 20"`.
- Max length: 20 characters.

**Position selector:** Two chips, tap to cycle — `"Header"` / `"Footer"`.
**Font style:** Two chips, tap to cycle — `"Serif"` / `"Sans"`.
Chip style: Parchment background, 1dp border `Pencil.copy(alpha = 0.2f)`, Roboto 12sp. Selected chip: Ink border, `Ink.copy(alpha = 0.04f)` fill.

---

## Summary Card

48dp whitespace above, then:

- Background: Parchment. Elevation 2dp. Corner radius 8dp. Padding 24dp.
- Line 1: `"WINE RED · FOCUSED · PAPER"` — Roboto 13sp, letter-spacing 0.5sp, Ink, uppercase.
- Line 2: `"Dotted pages · Word count · Date header"` — Roboto 13sp, Pencil.
- Line 3: `"Monogram: 'AG' — header, serif"` — Cormorant Garamond italic 13sp, Pencil. (Only if mark text is non-empty.)
- Updates in real-time as selections change.

---

## Sticky Footer (72dp)

- 1px top border (divider color).
- Background: same as page.
- Button: full width minus 32dp, 48dp height, background Ink, corner radius 4dp.
- Text: `"START WRITING"` — Roboto 14sp, letter-spacing 1sp, Parchment color, uppercase.
- Press: opacity dims to 0.85, 100ms. No Material ripple.
- On tap: save all selections → navigate to `Routes.OnboardingGoals`.

If opened from Settings (edit mode):
- Button text: `"SAVE CHANGES"`.
- On tap: save → navigate back (pop).

---

## Preference Keys Contract

These are the keys you write to `PreferenceDao`. Other agents read them.

```
diary_color         = "wine_red"                                          # one of the 10 keys
diary_form          = "focused"                                           # "focused"|"spacious"|"compact"
diary_texture       = "paper"                                             # "paper"|"parchment"|"linen"|"smooth"|"dark"
diary_canvas        = "dotted"                                            # "lined"|"blank"|"dotted"|"grid"|"numbered"
diary_details       = '["auto_save","word_count","date_header","daily_quote"]'  # JSON array of enabled keys
diary_mark_text     = "AG"                                                # string, max 20 chars
diary_mark_position = "header"                                            # "header"|"footer"
diary_mark_font     = "serif"                                             # "serif"|"sans"
design_completed    = "true"                                              # marker flag
```

Defaults (applied if user doesn't change anything):
- Color: `cream`, Form: `focused`, Texture: `paper`, Canvas: `lined`
- Details: `["auto_save", "word_count", "date_header", "daily_quote"]`
- Mark: empty string, header, serif.

---

## Scroll-Reveal Animations

Each `SectionHeader` + content block animates when first scrolled into the visible area:
- TranslateY 30dp→0 + opacity 0→1, 800ms, ease-out.
- Stagger: if multiple sections enter view simultaneously, offset by 150ms each.
- Use `LazyListState` scroll position to detect visibility.

---

## Edit Mode

When the user arrives from Settings (not onboarding), the screen must:
1. Pre-populate all selections from current preferences.
2. Change footer button to "SAVE CHANGES".
3. On save: overwrite preferences, pop back stack.

Detect edit mode via a navigation argument: `composable("design_studio?edit={edit}")` with default `false`.

---

## Acceptance Criteria

| # | Criterion | Verification |
|---|-----------|-------------|
| 1 | All 6 sections render with correct typography | Visual inspection |
| 2 | Color chips scroll horizontally, selection scales up | Tap different chips, observe animation |
| 3 | Form/Touch cards show left border on selection, others dim | Tap each option |
| 4 | Canvas pager swipes between 5 styles, dots update | Swipe through all |
| 5 | Detail toggles persist state in ViewModel | Toggle, scroll away, scroll back — state held |
| 6 | Mark input respects 20-char limit | Type 25 chars — only 20 accepted |
| 7 | Summary card updates in real-time | Change any selection — card reflects immediately |
| 8 | "Start Writing" writes all 8 preference keys | Query preferences table after tap |
| 9 | Diary preview cross-fades on selection change | Change color — preview updates smoothly |
| 10 | Scroll-reveal animations fire on each section | Scroll slowly — sections animate in |
| 11 | Haptic fires on color selection | Physical device test |
| 12 | Edit mode pre-populates and saves correctly | Open from Settings, change, save, re-open |
| 13 | Navigation: forward to OnboardingGoals, back pops | Test both directions |
