package com.proactivediary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.navigation.ProactiveDiaryNavHost
import com.proactivediary.playstore.InAppUpdateService
import com.proactivediary.ui.theme.ProactiveDiaryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceDao: PreferenceDao

    @Inject
    lateinit var analyticsService: AnalyticsService

    @Inject
    lateinit var inAppUpdateService: InAppUpdateService

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deepLinkDestination = intent?.getStringExtra("destination")

        analyticsService.logAppOpened()
        inAppUpdateService.checkForUpdate(this)
        if (deepLinkDestination != null) {
            analyticsService.logNotificationTapped(deepLinkDestination)
        }

        setContent {
            ProactiveDiaryTheme(preferenceDao = preferenceDao) {
                ProactiveDiaryNavHost(deepLinkDestination = deepLinkDestination)
            }
        }
    }
}
