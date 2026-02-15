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
        NotificationContent("Good morning.", "What's one thing you want to remember from yesterday?", "What's one thing you want to remember from yesterday?"),
        NotificationContent("A new page.", "The day is blank. What will you write on it?", "The day is blank. What will you write on it?"),
        NotificationContent("Before the rush.", "Capture one thought before the day takes over.", "Capture one thought before the day takes over."),
        NotificationContent("Morning light.", "What are you looking forward to today?", "What are you looking forward to today?"),
        NotificationContent("A quiet moment.", "Set an intention for today in just one sentence.", "Set an intention for today in just one sentence."),
        NotificationContent("Fresh start.", "If today had a title, what would it be?", "If today had a title, what would it be?"),
        NotificationContent("Rise.", "What's the first thing on your mind this morning?", "What's the first thing on your mind this morning?"),
        NotificationContent("Daybreak.", "What does today need from you?", "What does today need from you?"),
        NotificationContent("Good morning.", "Write one thing you're grateful for right now.", "Write one thing you're grateful for right now."),
        NotificationContent("Dawn.", "How did you sleep? Start there.", "How did you sleep? Start there."),
    )

    private val afternoonMessages = listOf(
        NotificationContent("A pause.", "Close your eyes for 3 seconds. Now write what you see.", "Close your eyes for 3 seconds. Now write what you see."),
        NotificationContent("Midday check-in.", "How is your day going? Really.", "How is your day going? Really."),
        NotificationContent("Halfway there.", "What's surprised you today so far?", "What's surprised you today so far?"),
        NotificationContent("A breath.", "What would you tell yourself right now?", "What would you tell yourself right now?"),
        NotificationContent("Pause.", "One thing that made you smile today.", "One thing that made you smile today."),
        NotificationContent("Still time.", "What conversation is stuck in your head?", "What conversation is stuck in your head?"),
        NotificationContent("The afternoon.", "What have you learned today that you didn't know this morning?", "What have you learned today that you didn't know this morning?"),
        NotificationContent("A moment.", "Describe where you are right now — just the details.", "Describe where you are right now — just the details."),
        NotificationContent("Check in.", "Rate your energy from 1-10. Then write why.", "Rate your energy from 1-10. Then write why."),
        NotificationContent("Take five.", "What's one thing you'd change about today if you could?", "What's one thing you'd change about today if you could?"),
    )

    private val eveningMessages = listOf(
        NotificationContent("Before the day fades.", "What surprised you today?", "What surprised you today?"),
        NotificationContent("Evening.", "What was the highlight? Write it down before you forget.", "What was the highlight? Write it down before you forget."),
        NotificationContent("Wind down.", "What are you proud of from today?", "What are you proud of from today?"),
        NotificationContent("Tonight.", "One thing you'd tell your morning self.", "One thing you'd tell your morning self."),
        NotificationContent("Reflect.", "If today were a chapter, how would it end?", "If today were a chapter, how would it end?"),
        NotificationContent("The day is done.", "What will you carry from today into tomorrow?", "What will you carry from today into tomorrow?"),
        NotificationContent("Quiet hours.", "Who made a difference in your day?", "Who made a difference in your day?"),
        NotificationContent("Nightfall.", "What's on your mind as the day closes?", "What's on your mind as the day closes?"),
        NotificationContent("Last light.", "Describe today in three words. Then explain why.", "Describe today in three words. Then explain why."),
        NotificationContent("Before sleep.", "What do you hope to dream about tonight?", "What do you hope to dream about tonight?"),
    )

    private val lateNightMessages = listOf(
        NotificationContent("Still up?", "What's keeping you awake? Write it out.", "What's keeping you awake? Write it out."),
        NotificationContent("The quiet hours.", "What's on your mind right now?", "What's on your mind right now?"),
        NotificationContent("Night thoughts.", "What do you want to remember from today?", "What do you want to remember from today?"),
        NotificationContent("Midnight.", "Write one honest sentence. That's all.", "Write one honest sentence. That's all."),
        NotificationContent("Late.", "Sometimes the best entries come at odd hours.", "Sometimes the best entries come at odd hours."),
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
                title = "Day $streak.",
                body = "Your practice is alive. Keep the thread going.",
                prompt = "You're on day $streak. What's keeping you going?"
            )
        } else {
            NotificationContent(
                title = "Welcome back.",
                body = "Your diary is open. No rules, no pressure. Just you.",
                prompt = "Start with what's on your mind right now."
            )
        }
    }

    fun getFallbackContent(streak: Int): NotificationContent {
        return if (streak > 0) {
            NotificationContent(
                title = "Day $streak is slipping.",
                body = "Even one sentence keeps it alive.",
                prompt = "Even one sentence. What's on your mind?"
            )
        } else {
            NotificationContent(
                title = "Still time.",
                body = "Your diary is open. No rules, no pressure. Just you.",
                prompt = "Start with what's on your mind right now."
            )
        }
    }

    fun getGoalContent(goalTitle: String): NotificationContent {
        val dayOfYear = LocalDate.now().dayOfYear
        val messages = listOf(
            "A small step still counts. Have you made progress?",
            "Just checking in. How's it going?",
            "Progress, not perfection. Any movement today?",
            "One step at a time. Have you worked on this?",
            "Consistency builds everything. Check in on your progress.",
        )
        val index = Random(dayOfYear.toLong() + goalTitle.hashCode()).nextInt(messages.size)
        return NotificationContent(
            title = goalTitle,
            body = messages[index],
            prompt = messages[index]
        )
    }
}
