package com.proactivediary.ui.designstudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun StickyFooter(
    isEditMode: Boolean,
    isSaving: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonText = when {
        isSaving -> "SAVING..."
        isEditMode -> "SAVE CHANGES"
        else -> "START WRITING"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(DiaryColors.Paper)
            .padding(horizontal = 32.dp, vertical = 12.dp)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isSaving) DiaryColors.Pencil else DiaryColors.Ink
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !isSaving,
                    onClick = onTap
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buttonText,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
