package com.proactivediary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.data.repository.GoalRepository
import com.proactivediary.data.repository.StreakRepository
import com.proactivediary.navigation.ProactiveDiaryNavHost
import com.proactivediary.playstore.InAppUpdateService
import com.proactivediary.ui.theme.ProactiveDiaryTheme

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceDao: PreferenceDao

    @Inject
    lateinit var analyticsService: AnalyticsService

    @Inject
    lateinit var inAppUpdateService: InAppUpdateService

    @Inject
    lateinit var entryRepository: EntryRepository

    @Inject
    lateinit var goalRepository: GoalRepository

    @Inject
    lateinit var streakRepository: StreakRepository

    private var sessionStartMs = 0L
    private val _deepLinkDestination = MutableStateFlow<String?>(null)
    private val _deepLinkPrompt = MutableStateFlow<String?>(null)
    private val _deepLinkGoalId = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sessionStartMs = System.currentTimeMillis()

        handleDeepLink(intent)

        analyticsService.logAppOpened()
        inAppUpdateService.checkForUpdate(this)

        // Log enriched session start with engagement context
        lifecycleScope.launch {
            val (daysSinceInstall, totalEntries) = withContext(Dispatchers.IO) {
                val installDate = preferenceDao.getSync("trial_start_date")?.value?.toLongOrNull()
                    ?: System.currentTimeMillis()
                val days = ((System.currentTimeMillis() - installDate) / 86_400_000).toInt()
                val entries = entryRepository.getTotalEntryCount()
                Pair(days, entries)
            }
            analyticsService.logSessionStart(daysSinceInstall, totalEntries)

            // Log retention milestones (day 1, 3, 7, 14, 30)
            if (daysSinceInstall in listOf(1, 3, 7, 14, 30, 60, 90)) {
                val totalWords = withContext(Dispatchers.IO) { entryRepository.getTotalWordCountSync() }
                analyticsService.logRetentionDay(daysSinceInstall, totalEntries, totalWords)
            }

            // Crashlytics custom keys
            val cachedPlan = withContext(Dispatchers.IO) {
                preferenceDao.getSync("billing_cached_plan")?.value ?: "trial"
            }
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("plan_type", cachedPlan)
                setCustomKey("total_entries", totalEntries)
                setCustomKey("days_since_install", daysSinceInstall)
            }

            // User properties for segmentation
            val (streak, goalCount, onboardingDone, designDone) = withContext(Dispatchers.IO) {
                val s = streakRepository.calculateWritingStreak()
                val g = goalRepository.getActiveGoalCount()
                val ob = preferenceDao.getSync("onboarding_completed")?.value == "true"
                val dc = preferenceDao.getSync("design_completed")?.value == "true"
                data class Props(val streak: Int, val goals: Int, val onboarding: Boolean, val design: Boolean)
                Props(s, g, ob, dc)
            }
            analyticsService.setUserProperties(
                planType = cachedPlan,
                totalEntries = totalEntries,
                daysSinceInstall = daysSinceInstall,
                writingStreak = streak,
                onboardingCompleted = onboardingDone,
                hasGoals = goalCount > 0,
                designCustomized = designDone
            )
        }

        setContent {
            val deepLink by _deepLinkDestination.collectAsState()
            val deepLinkPrompt by _deepLinkPrompt.collectAsState()
            val deepLinkGoalId by _deepLinkGoalId.collectAsState()
            ProactiveDiaryTheme {
                ProactiveDiaryNavHost(
                    analyticsService = analyticsService,
                    deepLinkDestination = deepLink,
                    deepLinkPrompt = deepLinkPrompt,
                    deepLinkGoalId = deepLinkGoalId,
                    onDeepLinkConsumed = {
                        _deepLinkDestination.value = null
                        _deepLinkPrompt.value = null
                        _deepLinkGoalId.value = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val destination = intent?.getStringExtra("destination")
        if (destination != null) {
            analyticsService.logNotificationTapped(destination)
            analyticsService.logAppOpenedWithSource("notification")
            _deepLinkDestination.value = destination
            _deepLinkPrompt.value = intent.getStringExtra("notification_prompt")
            _deepLinkGoalId.value = intent.getStringExtra("notification_goal_id")
        }
    }

    override fun onStart() {
        super.onStart()
        sessionStartMs = System.currentTimeMillis()
        // Track app open source (launcher if no deep link pending)
        if (_deepLinkDestination.value == null) {
            analyticsService.logAppOpenedWithSource("launcher")
        }
    }

    override fun onStop() {
        super.onStop()
        if (sessionStartMs > 0) {
            val durationSec = (System.currentTimeMillis() - sessionStartMs) / 1000
            analyticsService.logSessionEnd(durationSec, screensViewed = 0)
            sessionStartMs = 0L
        }
    }
}
