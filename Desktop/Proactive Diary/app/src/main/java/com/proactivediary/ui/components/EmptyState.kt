package com.proactivediary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.InstrumentSerif

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary
            ),
            textAlign = TextAlign.Center
        )
    }
}
