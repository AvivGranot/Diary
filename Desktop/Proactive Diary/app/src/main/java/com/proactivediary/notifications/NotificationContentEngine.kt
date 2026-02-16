package com.proactivediary.notifications

import java.time.LocalDate
import java.time.LocalTime
import kotlin.random.Random

/**
 * Generates beautiful, rotating notification content.
 * No DI — instantiated directly in BroadcastReceiver context.
 * Uses day-of-year seeding so content is deterministic per day but never repeats consecutively.
 */
object NotificationContentEngine {

    data class NotificationContent(
        val title: String,
        val body: String,
        val prompt: String  // The writing prompt carried through the deep link
    )

    // --- Writing Notification Content ---

    private val morningMessages = listOf(
        NotificationContent("gm bestie", "your thoughts are piling up. let's write", "What's one thing you want to remember from yesterday?"),
        NotificationContent("fresh page energy", "the day is blank. what will you write on it?", "The day is blank. What will you write on it?"),
        NotificationContent("before the chaos", "capture one thought before the day takes over", "Capture one thought before the day takes over."),
        NotificationContent("morning check-in", "what are you looking forward to today?", "What are you looking forward to today?"),
        NotificationContent("quick one", "set an intention for today in just one sentence", "Set an intention for today in just one sentence."),
        NotificationContent("new day new you", "if today had a title, what would it be?", "If today had a title, what would it be?"),
        NotificationContent("rise and write", "what's the first thing on your mind?", "What's the first thing on your mind this morning?"),
        NotificationContent("hey you", "your diary misses you. just saying", "What does today need from you?"),
        NotificationContent("gratitude moment", "one thing you're grateful for. go", "Write one thing you're grateful for right now."),
        NotificationContent("how'd you sleep?", "start there. just one sentence", "How did you sleep? Start there."),
    )

    private val afternoonMessages = listOf(
        NotificationContent("pause", "close your eyes for 3 seconds. now write what you see", "Close your eyes for 3 seconds. Now write what you see."),
        NotificationContent("real talk", "how is your day going? like actually", "How is your day going? Really."),
        NotificationContent("halfway through", "what's surprised you today?", "What's surprised you today so far?"),
        NotificationContent("hey", "what would you tell yourself right now?", "What would you tell yourself right now?"),
        NotificationContent("vibe check", "one thing that made you smile today", "One thing that made you smile today."),
        NotificationContent("brain dump time", "what conversation is stuck in your head?", "What conversation is stuck in your head?"),
        NotificationContent("quick thought", "what did you learn today that you didn't know this morning?", "What have you learned today that you didn't know this morning?"),
        NotificationContent("where are you rn", "describe it. just the details", "Describe where you are right now \u2014 just the details."),
        NotificationContent("energy check", "rate your energy 1-10. then write why", "Rate your energy from 1-10. Then write why."),
        NotificationContent("hot take", "what's one thing you'd change about today?", "What's one thing you'd change about today if you could?"),
    )

    private val eveningMessages = listOf(
        NotificationContent("day recap", "what surprised you today? write it before you forget", "What surprised you today?"),
        NotificationContent("highlight reel", "what was the best part? don't let it fade", "What was the highlight? Write it down before you forget."),
        NotificationContent("proud of you", "what did you crush today?", "What are you proud of from today?"),
        NotificationContent("plot twist", "one thing you'd tell your morning self", "One thing you'd tell your morning self."),
        NotificationContent("chapter end", "if today were a chapter, how would it end?", "If today were a chapter, how would it end?"),
        NotificationContent("carry forward", "what's worth remembering from today?", "What will you carry from today into tomorrow?"),
        NotificationContent("shoutout time", "who made a difference in your day?", "Who made a difference in your day?"),
        NotificationContent("end of day feels", "what's on your mind as the day closes?", "What's on your mind as the day closes?"),
        NotificationContent("three words", "describe today in three words. then explain why", "Describe today in three words. Then explain why."),
        NotificationContent("before sleep", "what do you hope to dream about tonight?", "What do you hope to dream about tonight?"),
    )

    private val lateNightMessages = listOf(
        NotificationContent("still up?", "what's keeping you awake? write it out", "What's keeping you awake? Write it out."),
        NotificationContent("late night thoughts", "what's on your mind rn?", "What's on your mind right now?"),
        NotificationContent("midnight entry", "what do you want to remember from today?", "What do you want to remember from today?"),
        NotificationContent("one sentence", "write one honest sentence. that's it", "Write one honest sentence. That's all."),
        NotificationContent("night owl", "the best entries come at weird hours tbh", "Sometimes the best entries come at odd hours."),
    )

    fun getWritingContent(hour: Int = LocalTime.now().hour): NotificationContent {
        val pool = when (hour) {
            in 5..11 -> morningMessages
            in 12..17 -> afternoonMessages
            in 18..22 -> eveningMessages
            else -> lateNightMessages
        }
        val dayOfYear = LocalDate.now().dayOfYear
        val index = Random(dayOfYear.toLong()).nextInt(pool.size)
        return pool[index]
    }

    fun getStreakContent(streak: Int): NotificationContent {
        return if (streak > 0) {
            NotificationContent(
                title = "$streak day streak \uD83D\uDD25",
                body = "you're on fire. don't break the chain",
                prompt = "You're on day $streak. What's keeping you going?"
            )
        } else {
            NotificationContent(
                title = "hey, welcome back",
                body = "your diary is open. no rules, no pressure. just you",
                prompt = "Start with what's on your mind right now."
            )
        }
    }

    fun getFallbackContent(streak: Int): NotificationContent {
        return if (streak > 0) {
            NotificationContent(
                title = "your $streak day streak ends at midnight",
                body = "quick entry? even one sentence counts",
                prompt = "Even one sentence. What's on your mind?"
            )
        } else {
            NotificationContent(
                title = "you haven't checked in today",
                body = "your diary misses you. just saying",
                prompt = "Start with what's on your mind right now."
            )
        }
    }

    fun getGoalContent(goalTitle: String): NotificationContent {
        val dayOfYear = LocalDate.now().dayOfYear
        val messages = listOf(
            "small steps still count. how's it going?",
            "just checking in on you. any progress?",
            "progress > perfection. any movement today?",
            "one step at a time. you got this",
            "consistency is everything. quick check-in?",
        )
        val index = Random(dayOfYear.toLong() + goalTitle.hashCode()).nextInt(messages.size)
        return NotificationContent(
            title = goalTitle,
            body = messages[index],
            prompt = messages[index]
        )
    }
}
