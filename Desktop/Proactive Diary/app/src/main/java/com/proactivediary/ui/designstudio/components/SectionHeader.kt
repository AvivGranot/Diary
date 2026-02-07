package com.proactivediary.ui.designstudio.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun SectionHeader(
    number: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Number
        Text(
            text = number,
            fontSize = 11.sp,
            letterSpacing = 1.1.sp,
            color = DiaryColors.Pencil
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = title,
            fontFamily = CormorantGaramond,
            fontSize = 24.sp,
            letterSpacing = 0.3.sp,
            color = DiaryColors.Ink
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Italic subtitle
        Text(
            text = subtitle,
            fontFamily = CormorantGaramond,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
            color = DiaryColors.Pencil
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SectionDivider(
    modifier: Modifier = Modifier
) {
    Divider(
        modifier = modifier.padding(horizontal = 24.dp),
        thickness = 1.dp,
        color = Color(0xFF585858).copy(alpha = 0.15f)
    )
}
