package com.proactivediary.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors
import java.util.Calendar

data class ReflectionPrompt(
    val text: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionPromptsSheet(
    onDismiss: () -> Unit,
    onPromptSelected: (String) -> Unit,
    textColor: Color = DiaryColors.Ink,
    secondaryTextColor: Color = DiaryColors.Pencil,
    backgroundColor: Color = DiaryColors.Paper
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val prompts = remember { generateReflectionPrompts() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = backgroundColor,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Title
            Text(
                text = "Go Deeper",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 22.sp,
                    color = textColor
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tap a prompt to start a new reflection",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = secondaryTextColor.copy(alpha = 0.5f)
                )
            )

            Spacer(Modifier.height(20.dp))

            // Prompt cards
            prompts.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = secondaryTextColor.copy(alpha = 0.05f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPromptSelected(prompt.text) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = secondaryTextColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prompt.text,
                                style = TextStyle(
                                    fontFamily = CormorantGaramond,
                                    fontSize = 16.sp,
                                    color = textColor
                                )
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = prompt.category,
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontSize = 11.sp,
                                    color = secondaryTextColor.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun generateReflectionPrompts(): List<ReflectionPrompt> {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val allPrompts = mutableListOf<ReflectionPrompt>()

    // Time-based prompts
    if (hour < 12) {
        allPrompts.add(ReflectionPrompt("What are you most looking forward to today?", "Morning reflection"))
        allPrompts.add(ReflectionPrompt("What intention do you want to set for today?", "Morning reflection"))
    } else if (hour < 17) {
        allPrompts.add(ReflectionPrompt("What has surprised you so far today?", "Afternoon check-in"))
        allPrompts.add(ReflectionPrompt("What moment today deserves to be remembered?", "Afternoon check-in"))
    } else {
        allPrompts.add(ReflectionPrompt("What would you do differently if you could redo today?", "Evening reflection"))
        allPrompts.add(ReflectionPrompt("What are you grateful for right now?", "Evening reflection"))
    }

    // Generic deep prompts
    allPrompts.add(ReflectionPrompt("What pattern in your life are you just starting to notice?", "Self-discovery"))
    allPrompts.add(ReflectionPrompt("What conversation has stayed with you recently, and why?", "Relationships"))
    allPrompts.add(ReflectionPrompt("If your future self could read this entry, what would matter most?", "Perspective"))
    allPrompts.add(ReflectionPrompt("What are you avoiding thinking about?", "Honesty"))
    allPrompts.add(ReflectionPrompt("Describe a small moment that made you feel alive.", "Presence"))

    // Return 4 prompts -- 2 time-based + 2 random from generic
    val timeBased = allPrompts.take(2)
    val generic = allPrompts.drop(2).shuffled().take(2)
    return timeBased + generic
}
