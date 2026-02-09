package com.proactivediary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.navigation.ProactiveDiaryNavHost
import com.proactivediary.playstore.InAppUpdateService
import com.proactivediary.ui.theme.ProactiveDiaryTheme

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
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

    private var sessionStartMs = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sessionStartMs = System.currentTimeMillis()

        val deepLinkDestination = intent?.getStringExtra("destination")

        analyticsService.logAppOpened()
        inAppUpdateService.checkForUpdate(this)
        if (deepLinkDestination != null) {
            analyticsService.logNotificationTapped(deepLinkDestination)
        }

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
        }

        setContent {
            ProactiveDiaryTheme {
                ProactiveDiaryNavHost(deepLinkDestination = deepLinkDestination)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (sessionStartMs > 0) {
            val durationSec = (System.currentTimeMillis() - sessionStartMs) / 1000
            analyticsService.logSessionEnd(durationSec, screensViewed = 0)
        }
    }
}
