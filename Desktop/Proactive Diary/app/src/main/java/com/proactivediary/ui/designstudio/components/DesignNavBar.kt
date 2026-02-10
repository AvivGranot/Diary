package com.proactivediary.ui.designstudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun DesignNavBar(
    isEditMode: Boolean = false,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DiaryColors.Paper)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Left: Back arrow in edit mode, hidden in onboarding
        if (isEditMode && onBack != null) {
            Text(
                text = "\u2190",
                fontSize = 22.sp,
                color = DiaryColors.Ink,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
                    .padding(8.dp)
            )
        }

        // Center: App title
        Text(
            text = "PROACTIVE DIARY",
            fontFamily = CormorantGaramond,
            fontSize = 16.sp,
            letterSpacing = 3.sp,
            color = DiaryColors.Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )

        // Right: Heart icon (text-based, non-functional MVP)
        Text(
            text = "\u2661",
            fontSize = 20.sp,
            color = DiaryColors.Ink,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Heart - non-functional MVP */ }
                )
                .padding(8.dp)
        )
    }
}
