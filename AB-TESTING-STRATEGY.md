# A/B Testing Strategy — Proactive Diary

**Author perspective:** Jack Dorsey
**Date:** February 2026

---

## Philosophy

Every feature in this app should earn its place. We don't add complexity — we subtract confusion. A/B testing isn't about finding what "works." It's about finding what's *true*. What does the person actually need in the moment they open this diary?

The metric that matters most is not revenue. It's whether someone wrote today who wouldn't have otherwise. Revenue follows truth.

---

## Principles

1. **Test one thing.** Never bundle changes. One variable, one hypothesis, one answer.
2. **Small cohorts, real signal.** 5% of users per variant is enough. Wait for statistical significance — don't peek.
3. **Measure behavior, not opinion.** What people do > what people say.
4. **Ship the loser fast.** When a variant loses, kill it the same day. No committees.
5. **Respect the craft.** This is a diary. Intimacy matters. Never test anything that makes the writing experience feel like a product.

---

## Infrastructure Requirements

### Firebase Remote Config + A/B Testing
- Already using Firebase Analytics (events are well-instrumented)
- Add `firebase-config` dependency to `build.gradle.kts`
- Create an `ExperimentService` in `/analytics/` that wraps Remote Config
- Expose experiment variants as `StateFlow` so Compose UI reacts in real-time
- Log experiment assignment as a user property (`experiment_{name}: variant_id`)

### Guardrail Metrics (monitor on ALL tests)
These must never regress, regardless of what we're testing:

| Metric | Source | Threshold |
|--------|--------|-----------|
| Day-1 retention | `app_opened` events | Must not drop > 2% |
| Entries per writer per week | `entry_saved` events | Must not drop > 5% |
| Crash-free rate | Firebase Crashlytics | Must stay > 99.5% |
| Avg words per entry | `entry_saved.word_count` | Must not drop > 10% |

If any guardrail breaks, auto-kill the experiment.

---

## The Experiments

### Experiment 1: First Words

**What we're testing:** Does the first screen matter, or does it get in the way?

| | Control | Variant A | Variant B |
|---|---|---|---|
| **Flow** | Typewriter → Design Studio → Goals → Write | Typewriter → Write (skip customization) | Straight to Write (skip everything) |

**Primary metric:** First entry written within 24 hours of install
**Secondary metric:** Day-7 retention, entries written in first 7 days

**Hypothesis:** The typewriter screen creates emotional resonance but the Design Studio adds friction before the person has committed. Variant A will win — keep the brand moment, remove the decision fatigue.

**Duration:** 14 days, minimum 1,000 users per variant.

---

### Experiment 2: The Blank Page vs. The Prompt

**What we're testing:** Should we show a writing prompt by default, or let the page be empty?

| | Control | Variant |
|---|---|---|
| **Write screen** | Daily prompt visible in placeholder text | Empty page, prompt accessible via tap on a subtle icon |

**Primary metric:** Entries saved per user per week
**Secondary metric:** Average word count, mood completion rate

**Hypothesis:** The blank page is more honest. A diary is not a homework assignment. But some people freeze in front of emptiness. The data will tell us which population is larger.

**Duration:** 21 days, minimum 800 users per variant.

---

### Experiment 3: When to Ask for Money

**What we're testing:** Is 10 entries the right engagement gate for the paywall?

| | Control | Variant A | Variant B |
|---|---|---|---|
| **Paywall trigger** | After 10 entries | After 7 entries | After 14 entries |

**Primary metric:** Trial start rate (users who begin the 7-day trial)
**Secondary metric:** Paid conversion rate at day 30, revenue per user at day 60

**Hypothesis:** 10 is arbitrary. 7 may be too early — the person hasn't built the habit yet. 14 may be too late — they've gotten full value without paying. We need to find the inflection point where the person has enough investment to value the product but still wants more.

**Duration:** 30 days (need to observe full trial + conversion cycle), minimum 500 users per variant.

---

### Experiment 4: The Streak

**What we're testing:** Do streak celebrations help or hurt?

| | Control | Variant |
|---|---|---|
| **Streak behavior** | Celebration overlay at milestones | No celebration. Streak count visible quietly in Journal insights only |

**Primary metric:** Writing consistency (% of days with an entry, measured over 28 days)
**Secondary metric:** Day-28 retention

**Hypothesis:** Streaks create anxiety. Missing a day feels like failure. A quiet streak — visible but not celebrated — may produce more sustainable writing behavior. The celebration puts the app at the center. The quiet version puts the writer at the center.

**Duration:** 28 days, minimum 600 users per variant.

---

### Experiment 5: Reminder Tone

**What we're testing:** What notification copy drives writing without creating guilt?

| | Control | Variant A | Variant B |
|---|---|---|---|
| **Notification text** | "Time to write in your diary" | "Your diary is here when you're ready" | No text — just the app icon badge (silent notification) |

**Primary metric:** Notification → entry written within 30 minutes
**Secondary metric:** Notification opt-out rate over 14 days

**Hypothesis:** Most reminder notifications are ignored because they feel like obligations. Variant A reframes the reminder as availability, not demand. Variant B tests whether the nudge can be almost invisible and still work. If B wins, we've learned something profound about restraint.

**Duration:** 14 days, minimum 1,000 users per variant.

---

### Experiment 6: Dark Mode Default

**What we're testing:** Should we default to system theme or always start light?

| | Control | Variant |
|---|---|---|
| **Initial theme** | Always light (paper aesthetic) | Follow system setting |

**Primary metric:** Day-7 retention
**Secondary metric:** Theme toggle rate in settings, entries in first 7 days

**Hypothesis:** The paper aesthetic is our brand identity. But someone who uses dark mode system-wide will be visually jarred by a bright screen, especially when writing at night — which is when most diary entries happen. Following the system is more respectful.

**Duration:** 14 days, minimum 1,000 users per variant.

---

### Experiment 7: Pricing Anchoring

**What we're testing:** Does showing the lifetime option change monthly/annual conversion?

| | Control | Variant |
|---|---|---|
| **Paywall layout** | Monthly, Annual, Lifetime — all visible | Monthly and Annual only. Lifetime appears after first renewal |

**Primary metric:** Revenue per user at day 90
**Secondary metric:** Plan distribution (monthly vs annual), churn at month 2

**Hypothesis:** The lifetime option cannibalizes annual subscriptions. People who would have paid annually instead wait and deliberate, reducing overall conversion. Hiding lifetime initially and offering it as a loyalty reward may increase total revenue while rewarding committed users.

**Duration:** 60 days (need full billing cycle data), minimum 500 users per variant.

---

## Prioritization & Sequencing

Run experiments in this order. Each one informs the next.

| Priority | Experiment | Why first |
|----------|-----------|-----------|
| **1** | First Words (onboarding) | Affects every new user from install. Highest leverage. |
| **2** | Blank Page vs. Prompt | Directly impacts core action (writing). |
| **3** | Reminder Tone | Drives returning users — the majority of DAU. |
| **4** | Streak Celebrations | Tests our assumptions about gamification. |
| **5** | Paywall Timing | Monetization — only optimize after activation is strong. |
| **6** | Pricing Anchoring | Fine-tuning revenue after paywall timing is settled. |
| **7** | Dark Mode Default | Low risk, easy win. Can run in parallel with 5 or 6. |

---

## Rules of Engagement

1. **No experiment runs longer than 60 days.** If we don't have signal by then, the difference doesn't matter. Ship either variant and move on.
2. **Never test on fewer than 500 users per variant.** Below that, we're reading noise.
3. **Every experiment has an owner.** One person decides ship/kill based on the data. No design-by-committee.
4. **Document every result** — wins AND losses — in a shared experiment log. The losses teach us more.
5. **Never run more than 2 experiments simultaneously** unless they affect completely independent surfaces. Interaction effects are real.
6. **The writing experience is sacred.** No experiment may alter the core text editor, font rendering, or auto-save behavior. Those are invariants.

---

## What We're Not Testing

Some things are decisions, not experiments:

- **The warm paper aesthetic.** That's brand identity, not a variable.
- **Serif typography.** Cormorant Garamond is a deliberate choice for intimacy. We don't A/B test taste.
- **Privacy model.** Entries stay on-device with optional cloud sync. Not negotiable.
- **Ad-free experience.** Ads in a diary would be a violation of trust. Never.

---

## Success Definition

After completing experiments 1–5, we should be able to answer:

1. What is the minimum onboarding that maximizes first-entry rate?
2. Does structured prompting help or hinder authentic writing?
3. What is the optimal engagement depth before introducing payment?
4. Do gamification elements (streaks, celebrations) create lasting habits or fragile ones?
5. What tone of communication respects the user's autonomy while maintaining engagement?

These five answers define the product. Everything else is optimization.

---

*"The best diary is the one you actually write in. Our job is to get out of the way."*
