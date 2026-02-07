# AGENT 1 — Typewriter Opening Screen

## Role

You build the first 10 seconds of the app. This is the emotional contract. If this screen feels like a loading screen, the user deletes the app. If it feels like watching someone write on warm paper by candlelight, the user stays.

You have one composable canvas and a sequence of timed animations. No network. No database reads except one preference check. Pixel-perfect timing. That is your entire scope.

---

## What You Own

```
ui/typewriter/
├── TypewriterScreen.kt       # Screen composable + ViewModel hosting
├── TypewriterViewModel.kt    # State machine, first-launch check, timer orchestration
├── TypewriterCanvas.kt       # Canvas composable — character-level text rendering
└── CursorAnimation.kt        # Blinking cursor composable (extracted for reuse)
```

## What You Touch But Don't Own

- `data/dao/PreferenceDao` — read/write `first_launch_completed` key
- `navigation/Routes.kt` — navigate to `Routes.DesignStudio`
- `ui/theme/*` — use `MaterialTheme.colorScheme`, `CormorantGaramond` font family
- `MainActivity.kt` — you may need to set immersive mode flags here or in your screen

---

## The Quote

Hardcoded. Do not load from a database. Do not randomize.

```
"If you would not be forgotten,
as soon as you are dead and rotten,
either write things worth reading,
or do things worth writing"
```

Line breaks are semantic, not wrapping artifacts. Preserve them exactly.

Attribution: `Benjamin Franklin`
Call to action: `Now write yours.`

---

## Animation Timeline

This is a state machine, not a set of independent timers. Each state transition triggers the next.

```
State: IDLE
  T+0ms     Screen renders. Background: MaterialTheme.colorScheme.background (#F3EEE7 light).
            Nothing visible.

State: TYPING (enter at T+200ms)
  T+200ms   Cursor appears. First character renders.
            Each subsequent character: +40ms interval.
            Quote is 157 characters (including line breaks and punctuation).
            Total typing duration: 157 × 40ms = 6,280ms.
            End of typing: ~T+6,480ms.

  Font:     Cormorant Garamond, italic, 20sp, color Pencil (#585858)
  Cursor:   1dp wide vertical line, same color as text.
            Blinks: 530ms period. Step function (not ease). Alpha toggles 0↔1.
            Cursor position: immediately after last rendered character.
  Layout:   Center-aligned horizontally. Vertically centered in screen.
            Line height: 30sp (1.5× font size).

State: CURSOR_FADE (enter at ~T+6,480ms)
  Cursor stops blinking, holds visible 200ms, then fades out over 200ms.
  Duration: 400ms total.

State: PAUSE_1 (enter at ~T+6,880ms)
  Nothing happens. 600ms silence.

State: ATTRIBUTION (enter at ~T+7,480ms)
  "Benjamin Franklin" fades in.
  Font:      Roboto, 12sp, letter-spacing 0.7sp, color Pencil (#585858).
  Position:  Centered horizontally, 16dp below last line of quote.
  Animation: opacity 0→1, 500ms, ease-out.

State: PAUSE_2 (enter at ~T+7,980ms)
  Nothing happens. 800ms silence.

State: CALL_TO_ACTION (enter at ~T+8,780ms)
  "Now write yours." slides up.
  Font:      Cormorant Garamond, 24sp, REGULAR (not italic), color Ink (#313131).
  Position:  Centered horizontally, 32dp below attribution.
  Animation: translateY +24dp→0 + opacity 0→1, 600ms, ease-out.

State: CHEVRON (enter at ~T+9,380ms)
  Downward chevron appears below CTA.
  Size: 24dp. Color: Ink (#313131).
  Pulsing: alpha cycles 0.4→1.0→0.4, 2000ms period, infinite, ease-in-out.

State: READY (enter at ~T+9,380ms, simultaneous with CHEVRON)
  User can now interact. Swipe up or tap anywhere → navigate forward.
  Before READY state: taps and swipes are ignored (except Skip button).
```

---

## Skip Button

- Appears at T+1,500ms (absolute, regardless of animation state).
- Position: top-right, 16dp inset from both edges.
- Text: "Skip" — Roboto, 12sp, color Pencil (#585858).
- Minimum tap target: 48×48dp.
- Fade-in: opacity 0→1, 300ms, ease-out.
- On tap: cancel all running animations, navigate immediately to `Routes.DesignStudio`.

---

## Subsequent Launches

Check `PreferenceDao.get("first_launch_completed")`:

- If value exists and equals `"true"`:
  - Render the full quote instantly (no animation). All elements visible.
  - Hold for 1,000ms.
  - Auto-navigate to `Routes.DesignStudio`.
  - OR: tap anywhere to skip immediately.

- If value is null:
  - Run full animation.
  - On forward navigation (any trigger), write `PreferenceEntity("first_launch_completed", "true")`.

---

## Canvas Implementation

Use `Canvas` + `TextMeasurer` for character-level rendering control:

```kotlin
@Composable
fun TypewriterCanvas(
    text: String,
    visibleCharCount: Int,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxWidth()) {
        val visibleText = text.substring(0, visibleCharCount.coerceAtMost(text.length))
        val textLayoutResult = textMeasurer.measure(
            text = AnnotatedString(visibleText),
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
                color = Color(0xFF585858),
                lineHeight = 30.sp
            ),
            constraints = Constraints(maxWidth = size.width.toInt())
        )
        drawText(textLayoutResult)
    }
}
```

The parent composable (`TypewriterScreen`) controls `visibleCharCount` via a `LaunchedEffect` that increments every 40ms.

---

## Immersive Mode

Hide status bar and navigation bar for the duration of this screen:

```kotlin
val window = (LocalContext.current as Activity).window
val controller = WindowInsetsControllerCompat(window, window.decorView)

DisposableEffect(Unit) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    controller.hide(WindowInsetsCompat.Type.systemBars())
    controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    onDispose {
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
}
```

Restore system bars when leaving this screen.

---

## State Machine Type

```kotlin
enum class TypewriterState {
    IDLE,
    TYPING,
    CURSOR_FADE,
    PAUSE_1,
    ATTRIBUTION,
    PAUSE_2,
    CALL_TO_ACTION,
    READY,
    NAVIGATING       // terminal — prevents double navigation
}
```

ViewModel exposes `StateFlow<TypewriterState>` and `StateFlow<Int>` for visible character count. The screen composable observes both.

---

## Navigation

- Forward: `navController.navigate(Routes.DesignStudio.route) { popUpTo(Routes.Typewriter.route) { inclusive = true } }`
- Pop the typewriter off the back stack. Users should not be able to navigate back to it.
- Transition: screen fade-out 400ms, ease-in-out.

---

## Performance Budget

- 60 FPS during typing on a Pixel 4a.
- Canvas recomposes every 40ms during TYPING state. Keep the composable allocation-free in the draw path.
- Total screen memory: <10MB.
- No image loading. No network. Text and canvas only.

---

## Acceptance Criteria

| # | Criterion | Verification |
|---|-----------|-------------|
| 1 | Characters appear at 40ms intervals | Count frames: 25 chars/second |
| 2 | Cursor blinks at 530ms | Stopwatch: ~1.9 blinks/sec |
| 3 | Cursor uses step function, not ease | Visual: instant on/off, no fade |
| 4 | Attribution fades in 600ms after last char | Timing matches ±100ms |
| 5 | "Now write yours." slides up 800ms after attribution | Timing matches ±100ms |
| 6 | Skip button appears at 1.5s | Visible before quote finishes |
| 7 | Skip navigates immediately | No animation delay on skip |
| 8 | Swipe/tap navigates after READY state | Cannot navigate during TYPING |
| 9 | First-launch flag persists | Kill app, reopen → instant render |
| 10 | Subsequent launch: 1s hold, auto-advance | Stopwatch confirms |
| 11 | Immersive mode | No status bar, no nav bar |
| 12 | System bars restore on exit | Design Studio has normal bars |
| 13 | Cormorant Garamond italic renders | Visual check — not Roboto fallback |
| 14 | Typewriter popped from back stack | Back button from Design Studio does NOT return here |
