package com.proactivediary.ui.storelisting.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.journal.CalendarHeatmap
import com.proactivediary.ui.journal.DiaryCard
import com.proactivediary.ui.journal.DiaryCardData
import com.proactivediary.ui.storelisting.ScreenshotFrame
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.ProactiveDiaryTheme
import java.time.LocalDate

@Composable
fun Screenshot03Journal() {
    ScreenshotFrame(headline = "Revisit your story") {
        JournalContent()
    }
}

@Composable
private fun JournalContent() {
    val today = LocalDate.now()

    // Mock ~60% fill heatmap data
    val writingDays = buildSet {
        for (i in 0..83) {
            val date = today.minusDays(i.toLong())
            // ~60% fill — skip some days
            if (i % 5 != 2 && i % 7 != 4) {
                add(date)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(12.dp))

        // On This Day card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "One year ago, you wrote:",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "\u201CThe sunset reminded me of that summer in Lisbon, how we sat on the rooftop and watched the light change.\u201D",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Calendar heatmap
        CalendarHeatmap(writingDays = writingDays)

        Spacer(Modifier.height(8.dp))

        // Diary cards
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DiaryCard(
                data = DiaryCardData(
                    id = "mock1",
                    title = "Evening walk thoughts",
                    content = "The air was cool and the neighborhood was quiet. I noticed the magnolia tree on Elm Street has started blooming early this year.",
                    tags = listOf("nature", "evening"),
                    wordCount = 156,
                    createdAt = System.currentTimeMillis() - 86400000
                ),
                onClick = {}
            )

            DiaryCard(
                data = DiaryCardData(
                    id = "mock2",
                    title = "Monday morning clarity",
                    content = "Started the week with a clear head. Wrote down three things I want to accomplish this week and felt a sense of purpose I haven\u2019t had in a while.",
                    tags = listOf("goals", "morning"),
                    wordCount = 234,
                    createdAt = System.currentTimeMillis() - 172800000
                ),
                onClick = {}
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun Screenshot03JournalPreview() {
    ProactiveDiaryTheme {
        Screenshot03Journal()
    }
}
