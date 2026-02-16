package com.proactivediary.ui.quotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.DiaryColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeQuoteSheet(
    content: String,
    wordCount: Int,
    isSubmitting: Boolean,
    error: String?,
    onContentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DiaryColors.Paper
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Share Your Wisdom",
                style = MaterialTheme.typography.titleMedium,
                color = DiaryColors.Ink
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Write a short quote that inspires",
                style = MaterialTheme.typography.bodySmall,
                color = DiaryColors.Pencil,
                fontStyle = FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text(
                        "The best way to predict the future is to create it.",
                        fontStyle = FontStyle.Italic,
                        color = DiaryColors.Pencil.copy(alpha = 0.4f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DiaryColors.Parchment,
                    unfocusedContainerColor = DiaryColors.Parchment,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = DiaryColors.Ink,
                    unfocusedTextColor = DiaryColors.Ink,
                    cursorColor = DiaryColors.ElectricIndigo
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Word counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "$wordCount/${QuotesViewModel.MAX_QUOTE_WORDS} words",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (wordCount >= QuotesViewModel.MAX_QUOTE_WORDS)
                        DiaryColors.CoralRed else DiaryColors.Pencil
                )
            }

            if (error != null) {
                Text(
                    text = error,
                    color = DiaryColors.CoralRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = content.isNotBlank() && !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DiaryColors.ElectricIndigo
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Publish", fontSize = 16.sp)
                }
            }
        }
    }
}
