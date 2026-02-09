package com.proactivediary.ui.storelisting.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.storelisting.ScreenshotFrame
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors
import com.proactivediary.ui.theme.ProactiveDiaryTheme
import com.proactivediary.ui.theme.diaryColorOptions

@Composable
fun Screenshot05DesignStudio() {
    ScreenshotFrame(headline = "Make it yours") {
        DesignStudioContent()
    }
}

@Composable
private fun DesignStudioContent() {
    val inkColor = DiaryColors.Ink
    val pencilColor = DiaryColors.Pencil
    val selectedIndex = 2 // Sage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiaryColors.Paper)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Section: Color
        Text(
            text = "Color",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 20.sp,
                color = inkColor
            )
        )

        Spacer(Modifier.height(12.dp))

        // Color swatch grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(diaryColorOptions) { option ->
                val isSelected = option.name == "Sage"
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(option.color)
                        .then(
                            if (isSelected) {
                                Modifier.border(2.5.dp, inkColor, CircleShape)
                            } else {
                                Modifier.border(1.dp, pencilColor.copy(alpha = 0.2f), CircleShape)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Text(
                            text = option.name,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = inkColor
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Mini preview pane
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DiaryColors.Sage)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Morning reflections",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 16.sp,
                        color = inkColor
                    )
                )
                Spacer(Modifier.height(4.dp))
                // Fake lined text
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(top = 14.dp)
                            .background(pencilColor.copy(alpha = 0.12f))
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Section: Canvas Style
        Text(
            text = "Canvas Style",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 20.sp,
                color = inkColor
            )
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Lined", "Dotted", "Grid").forEachIndexed { index, label ->
                val isSelected = index == 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(inkColor)
                            } else {
                                Modifier
                                    .background(Color.Transparent)
                                    .border(1.dp, pencilColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            }
                        )
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = if (isSelected) DiaryColors.Parchment else pencilColor
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Section: Font Size
        Text(
            text = "Font Size",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 20.sp,
                color = inkColor
            )
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("S", "M", "L").forEachIndexed { index, label ->
                val isSelected = index == 1 // M selected
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(inkColor)
                            } else {
                                Modifier
                                    .background(Color.Transparent)
                                    .border(1.dp, pencilColor.copy(alpha = 0.3f), CircleShape)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = if (isSelected) DiaryColors.Parchment else pencilColor
                        )
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun Screenshot05DesignStudioPreview() {
    ProactiveDiaryTheme {
        Screenshot05DesignStudio()
    }
}
