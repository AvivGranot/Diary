package com.proactivediary.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import com.proactivediary.ui.theme.accentColorOptions

/**
 * Layout page — replaces Design Studio.
 * Simple: dark/light toggle, accent color picker, font size.
 */
@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LayoutScreen(
    onBack: () -> Unit = {}
) {
    val extendedColors = LocalDiaryExtendedColors.current
    var selectedThemeIndex by remember { mutableIntStateOf(0) } // 0 = Dark, 1 = Light
    var selectedAccentIndex by remember { mutableIntStateOf(0) } // 0 = Mint (default)
    var selectedFontSize by remember { mutableIntStateOf(1) } // 0 = Small, 1 = Medium, 2 = Large

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DiarySpacing.xs, vertical = DiarySpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Layout",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(DiarySpacing.lg))

        // Theme Mode
        Column(modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(DiarySpacing.sm))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("Dark", "Light").forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedThemeIndex == index,
                        onClick = { selectedThemeIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(DiarySpacing.xl))

        // Accent Color
        Column(modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)) {
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(DiarySpacing.sm))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DiarySpacing.sm),
                verticalArrangement = Arrangement.spacedBy(DiarySpacing.sm)
            ) {
                accentColorOptions.forEachIndexed { index, option ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(option.color)
                            .then(
                                if (selectedAccentIndex == index)
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            )
                            .clickable { selectedAccentIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAccentIndex == index) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(DiarySpacing.xxs))

            Text(
                text = accentColorOptions.getOrNull(selectedAccentIndex)?.name ?: "Mint",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(DiarySpacing.xl))

        // Font Size
        Column(modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)) {
            Text(
                text = "Font Size",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(DiarySpacing.sm))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("Small", "Medium", "Large").forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedFontSize == index,
                        onClick = { selectedFontSize = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
