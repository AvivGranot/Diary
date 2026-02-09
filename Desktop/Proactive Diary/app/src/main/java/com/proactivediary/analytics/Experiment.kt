package com.proactivediary.analytics

/**
 * All A/B test experiments in Proactive Diary.
 *
 * Each experiment defines:
 * - [key]: Firebase Remote Config parameter key
 * - [variants]: Possible variant values (first is always control)
 * - [defaultVariant]: Fallback when Remote Config hasn't fetched yet
 */
enum class Experiment(
    val key: String,
    val variants: List<String>,
    val defaultVariant: String
) {
    /**
     * Experiment 1: First Words — onboarding flow length.
     * - control: Typewriter → Design Studio → Goals → Write (full flow)
     * - skip_design: Typewriter → Write (skip customization)
     * - straight_to_write: Straight to Write (skip everything)
     */
    FIRST_WORDS(
        key = "exp_first_words",
        variants = listOf("control", "skip_design", "straight_to_write"),
        defaultVariant = "control"
    ),

    /**
     * Experiment 2: Blank Page vs Prompt.
     * - control: Daily prompt visible as placeholder text
     * - blank_page: Empty page, prompt hidden behind subtle icon
     */
    BLANK_PAGE_VS_PROMPT(
        key = "exp_blank_page",
        variants = listOf("control", "blank_page"),
        defaultVariant = "control"
    ),

    /**
     * Experiment 3: Paywall Timing — when to show the engagement gate.
     * - control: After 10 entries
     * - early: After 7 entries
     * - late: After 14 entries
     */
    PAYWALL_TIMING(
        key = "exp_paywall_timing",
        variants = listOf("control", "early", "late"),
        defaultVariant = "control"
    ),

    /**
     * Experiment 4: Streak Celebrations.
     * - control: Full celebration overlay at milestones
     * - quiet: No celebration; streak count only in Journal insights
     */
    STREAK_CELEBRATION(
        key = "exp_streak_celebration",
        variants = listOf("control", "quiet"),
        defaultVariant = "control"
    ),

    /**
     * Experiment 5: Reminder Tone — notification copy.
     * - control: "Time to write in your diary"
     * - gentle: "Your diary is here when you're ready"
     * - silent: No text — just the app icon badge (silent notification)
     */
    REMINDER_TONE(
        key = "exp_reminder_tone",
        variants = listOf("control", "gentle", "silent"),
        defaultVariant = "control"
    ),

    /**
     * Experiment 6: Dark Mode Default.
     * - control: Always start light (paper aesthetic)
     * - system: Follow system setting
     */
    DARK_MODE_DEFAULT(
        key = "exp_dark_mode_default",
        variants = listOf("control", "system"),
        defaultVariant = "control"
    ),

    /**
     * Experiment 7: Pricing Anchoring — lifetime visibility.
     * - control: Monthly, Annual, Lifetime all visible
     * - hide_lifetime: Monthly and Annual only; Lifetime after first renewal
     */
    PRICING_ANCHORING(
        key = "exp_pricing_anchoring",
        variants = listOf("control", "hide_lifetime"),
        defaultVariant = "control"
    );
}
