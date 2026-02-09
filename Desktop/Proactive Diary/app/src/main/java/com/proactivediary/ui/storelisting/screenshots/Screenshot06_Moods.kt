package com.proactivediary.ui.storelisting.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.Mood
import com.proactivediary.ui.journal.InsightsCard
import com.proactivediary.ui.journal.JournalInsights
import com.proactivediary.ui.journal.MoodDataPoint
import com.proactivediary.ui.journal.MoodTrendChart
import com.proactivediary.ui.storelisting.ScreenshotFrame
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors
import com.proactivediary.ui.theme.ProactiveDiaryTheme
import java.time.LocalDate

@Composable
fun Screenshot06Moods() {
    ScreenshotFrame(headline = "Understand your patterns") {
        MoodsContent()
    }
}

@Composable
private fun MoodsContent() {
    val today = LocalDate.now()

    // Mock 14-day mood data trending upward
    val moodData = listOf(
        Mood.BAD, Mood.NEUTRAL, Mood.BAD, Mood.NEUTRAL, Mood.GOOD,
        Mood.NEUTRAL, Mood.GOOD, Mood.NEUTRAL, Mood.GOOD, Mood.GOOD,
        Mood.GREAT, Mood.GOOD, Mood.GREAT, Mood.GREAT
    ).mapIndexed { index, mood ->
        MoodDataPoint(
            date = today.minusDays((13 - index).toLong()),
            mood = mood
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiaryColors.Paper)
    ) {
        Spacer(Modifier.height(12.dp))

        // Mood trend chart
        MoodTrendChart(dataPoints = moodData)

        Spacer(Modifier.height(4.dp))

        // Insights card
        InsightsCard(
            insights = JournalInsights(
                totalEntries = 47,
                totalWords = 12340,
                averageWordsPerEntry = 262,
                mostCommonMood = "good",
                currentStreak = 30,
                mostActiveDay = "Tuesdays"
            )
        )

        Spacer(Modifier.height(12.dp))

        // Mood legend
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Mood.entries.forEach { mood ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(mood.color)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = mood.label,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = DiaryColors.Pencil
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun Screenshot06MoodsPreview() {
    ProactiveDiaryTheme {
        Screenshot06Moods()
    }
}
