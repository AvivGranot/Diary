package com.proactivediary.ui.write

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.domain.model.Mood

@Composable
fun WriteToolbar(
    selectedMood: Mood?,
    onMoodSelected: (Mood?) -> Unit,
    wordCount: Int,
    showWordCount: Boolean,
    colorKey: String,
    textureKey: String = "paper",
    modifier: Modifier = Modifier
) {
    val bgColor = DiaryThemeConfig.textureColorForKey(textureKey)
    val secondaryColor = DiaryThemeConfig.textureSecondaryTextColor(textureKey)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(bgColor)
    ) {
        // Top divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(secondaryColor.copy(alpha = 0.15f))
                .align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Mood selector
            MoodSelector(
                selectedMood = selectedMood,
                onMoodSelected = onMoodSelected
            )

            // Right: Word count
            if (showWordCount) {
                Text(
                    text = "$wordCount words",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = secondaryColor
                    )
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
        }
    }
}

@Composable
internal fun TagInputDialog(
    currentTags: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var tagText by remember { mutableStateOf(currentTags.joinToString(", ")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tags",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Separate tags with commas",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        color = Color(0xFF585858)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = tagText,
                    onValueChange = { tagText = it },
                    placeholder = {
                        Text(
                            text = "personal, gratitude, goals...",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 14.sp,
                                color = Color(0xFF585858).copy(alpha = 0.5f)
                            )
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF585858),
                        unfocusedIndicatorColor = Color(0xFF585858).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp
                    ),
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsed = tagText
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                onConfirm(parsed)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
