package com.proactivediary

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.data.repository.GoalRepository
import com.proactivediary.data.repository.StreakRepository
import com.proactivediary.navigation.ProactiveDiaryNavHost
import com.proactivediary.playstore.InAppUpdateService
import com.proactivediary.ui.lock.LockScreen
import com.proactivediary.ui.lock.LockViewModel
import com.proactivediary.ui.theme.ProactiveDiaryTheme

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

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

    private val lockViewModel: LockViewModel by viewModels()

    private var sessionStartMs = 0L
    private val _deepLinkDestination = MutableStateFlow<String?>(null)
    private val _deepLinkPrompt = MutableStateFlow<String?>(null)
    private val _deepLinkGoalId = MutableStateFlow<String?>(null)
    private val _deepLinkEntryId = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sessionStartMs = System.currentTimeMillis()

        handleDeepLink(intent)

        // Feature 1: Apply FLAG_SECURE when "Hide in App Switcher" is enabled
        lifecycleScope.launch {
            preferenceDao.observe("hide_in_switcher")
                .map { it?.value == "true" }
                .collectLatest { hideContent ->
                    if (hideContent) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
        }

        // Wire lock overlay: show LockScreen when app resumes with lock enabled
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                lockViewModel.onAppResumed()
            }
        })

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
            val deepLinkEntryId by _deepLinkEntryId.collectAsState()
            val isLocked by lockViewModel.isLocked.collectAsState()

            // Observe theme preferences reactively (auto-recomposes on change)
            val themePref by preferenceDao.observe("theme_mode")
                .collectAsState(initial = null)
            val accentPref by preferenceDao.observe("accent_color")
                .collectAsState(initial = null)

            val isDarkTheme = themePref?.value == "dark" // default light
            val accentKey = accentPref?.value ?: "blue"

            ProactiveDiaryTheme(
                darkTheme = isDarkTheme,
                accentColorKey = accentKey
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ProactiveDiaryNavHost(
                        analyticsService = analyticsService,
                        deepLinkDestination = deepLink,
                        deepLinkPrompt = deepLinkPrompt,
                        deepLinkGoalId = deepLinkGoalId,
                        deepLinkEntryId = deepLinkEntryId,
                        onDeepLinkConsumed = {
                            _deepLinkDestination.value = null
                            _deepLinkPrompt.value = null
                            _deepLinkGoalId.value = null
                            _deepLinkEntryId.value = null
                        }
                    )

                    // Lock overlay on top of everything
                    if (isLocked) {
                        LockScreen(onUnlocked = { lockViewModel.unlock() })
                    }
                }
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
            _deepLinkEntryId.value = intent.getStringExtra("capsule_entry_id")
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
