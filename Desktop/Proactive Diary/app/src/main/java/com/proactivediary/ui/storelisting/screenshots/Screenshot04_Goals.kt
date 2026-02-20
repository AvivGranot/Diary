package com.proactivediary.ui.storelisting.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.GoalFrequency
import com.proactivediary.ui.goals.GoalCard
import com.proactivediary.ui.goals.GoalUiState
import com.proactivediary.ui.storelisting.ScreenshotFrame
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.ProactiveDiaryTheme

@Composable
fun Screenshot04Goals() {
    ScreenshotFrame(headline = "Build habits that matter") {
        GoalsContent()
    }
}

@Composable
private fun GoalsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Your Goals",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 24.sp,
                color = Color(0xFFFFFFFF)
            )
        )

        Spacer(Modifier.height(4.dp))

        GoalCard(
            goal = GoalUiState(
                id = "mock1",
                title = "Write 500 words daily",
                frequency = GoalFrequency.DAILY,
                reminderTime = "08:00",
                reminderDays = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                progressPercent = 85,
                streak = 12,
                checkedInToday = true
            ),
            onCheckIn = {},
            onEdit = {},
            onDelete = {},
            onLongPress = {}
        )

        GoalCard(
            goal = GoalUiState(
                id = "mock2",
                title = "Gratitude journaling",
                frequency = GoalFrequency.DAILY,
                reminderTime = "21:00",
                reminderDays = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                progressPercent = 60,
                streak = 5,
                checkedInToday = false
            ),
            onCheckIn = {},
            onEdit = {},
            onDelete = {},
            onLongPress = {}
        )
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun Screenshot04GoalsPreview() {
    ProactiveDiaryTheme {
        Screenshot04Goals()
    }
}
