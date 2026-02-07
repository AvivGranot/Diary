package com.proactivediary

import android.app.Application
import com.proactivediary.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ProactiveDiaryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createChannels(this)
    }
}
