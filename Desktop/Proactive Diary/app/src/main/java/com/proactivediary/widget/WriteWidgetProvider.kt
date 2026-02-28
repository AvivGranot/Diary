package com.proactivediary.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.proactivediary.MainActivity
import com.proactivediary.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class WriteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_write)

        // Date — elegant, uppercase
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
        views.setTextViewText(R.id.widget_date, today.format(formatter).uppercase())

        // Daily wisdom quote from curated historical figures
        val wisdom = DailyWisdom.todayQuote()
        views.setTextViewText(R.id.widget_quote, "\u201C${wisdom.text}\u201D")
        views.setTextViewText(R.id.widget_author, "\u2014 ${wisdom.author}, ${wisdom.context}")

        // Streak — leave empty for now (widget can't easily access Room DB synchronously)
        views.setTextViewText(R.id.widget_streak, "")

        // Tap opens Write tab
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("destination", "write")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
