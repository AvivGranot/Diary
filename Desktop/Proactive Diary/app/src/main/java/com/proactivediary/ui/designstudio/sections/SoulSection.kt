package com.proactivediary.ui.designstudio.sections

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.designstudio.components.ColorChip
import com.proactivediary.ui.designstudio.components.SectionDivider
import com.proactivediary.ui.designstudio.components.SectionHeader
import com.proactivediary.ui.theme.PlusJakartaSans
import com.proactivediary.ui.theme.classicColorOptions
import com.proactivediary.ui.theme.diaryColorOptions

@Composable
fun SoulSection(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            number = "01",
            title = "Soul",
            subtitle = "Choose the colour that speaks to you"
        )

        // Vibrant colors (default)
        Text(
            text = "VIBRANT",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                color = Color(0xFF64748B)
            ),
            modifier = Modifier.fillMaxWidth()
                .then(Modifier.padding(horizontal = 24.dp))
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = diaryColorOptions,
                key = { it.key }
            ) { option ->
                ColorChip(
                    color = option.color,
                    isSelected = option.key == selectedColor,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onColorSelected(option.key)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Classic colors
        Text(
            text = "CLASSIC",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                color = Color(0xFF64748B)
            ),
            modifier = Modifier.fillMaxWidth()
                .then(Modifier.padding(horizontal = 24.dp))
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = classicColorOptions,
                key = { it.key }
            ) { option ->
                ColorChip(
                    color = option.color,
                    isSelected = option.key == selectedColor,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onColorSelected(option.key)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        SectionDivider()

        Spacer(modifier = Modifier.height(28.dp))
    }
}
