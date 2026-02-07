package com.proactivediary.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    BootReceiverEntryPoint::class.java
                )
                entryPoint.notificationService().rescheduleAll()
            } catch (_: Exception) {
                // Silently fail — alarms will be rescheduled on next app launch
            } finally {
                pendingResult.finish()
            }
        }
    }
}
