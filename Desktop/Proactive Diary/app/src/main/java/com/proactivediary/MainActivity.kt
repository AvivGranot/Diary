package com.proactivediary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.analytics.ExperimentService
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.navigation.ProactiveDiaryNavHost
import com.proactivediary.playstore.InAppUpdateService
import com.proactivediary.ui.theme.ProactiveDiaryTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
    lateinit var experimentService: ExperimentService

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deepLinkDestination = intent?.getStringExtra("destination")

        analyticsService.logAppOpened()
        inAppUpdateService.checkForUpdate(this)
        lifecycleScope.launch { experimentService.initialize() }
        if (deepLinkDestination != null) {
            analyticsService.logNotificationTapped(deepLinkDestination)
        }

        setContent {
            ProactiveDiaryTheme(preferenceDao = preferenceDao, experimentService = experimentService) {
                ProactiveDiaryNavHost(deepLinkDestination = deepLinkDestination)
            }
        }
    }
}
