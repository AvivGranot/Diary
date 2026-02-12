package com.proactivediary.ui.suggestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.suggestions.Suggestion
import com.proactivediary.domain.suggestions.SuggestionType
import com.proactivediary.ui.theme.CormorantGaramond

@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    textColor: Color,
    secondaryTextColor: Color,
    cardColor: Color,
    onClick: (Suggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardColor)
            .clickable { onClick(suggestion) }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Icon(
            imageVector = iconForType(suggestion.type),
            contentDescription = null,
            tint = secondaryTextColor.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.title,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 16.sp,
                    color = textColor
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = suggestion.subtitle,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    color = secondaryTextColor.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic
                )
            )
            if (suggestion.sourceLabel != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = suggestion.sourceLabel,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 11.sp,
                        color = secondaryTextColor.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

private fun iconForType(type: SuggestionType): ImageVector {
    return when (type) {
        SuggestionType.LOCATION -> Icons.Outlined.LocationOn
        SuggestionType.WEATHER -> Icons.Outlined.Cloud
        SuggestionType.STREAK -> Icons.Outlined.LocalFireDepartment
        SuggestionType.TIME_OF_DAY -> Icons.Outlined.Schedule
        SuggestionType.ON_THIS_DAY -> Icons.Outlined.CalendarToday
        SuggestionType.MOOD_PATTERN -> Icons.Outlined.Mood
        SuggestionType.WRITING_HABIT -> Icons.Outlined.EditNote
    }
}
