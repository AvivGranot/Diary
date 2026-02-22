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
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import com.proactivediary.ui.theme.PlusJakartaSans
import com.proactivediary.ui.theme.accentColorOptions

/**
 * Layout page — replaces Design Studio.
 * Dark/light toggle, accent color picker, font size.
 * State is persisted via LayoutViewModel → PreferenceDao.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LayoutScreen(
    onBack: () -> Unit = {},
    viewModel: LayoutViewModel = hiltViewModel()
) {
    val extendedColors = LocalDiaryExtendedColors.current
    val state = viewModel.uiState

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
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(DiarySpacing.md))

        // ── Appearance: Dark / Light visual cards ──
        Column(modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)) {
            Text(
                text = "APPEARANCE",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(DiarySpacing.sm))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Dark mode card
                ThemeModeCard(
                    label = "Dark",
                    icon = Icons.Outlined.DarkMode,
                    isSelected = state.themeMode == "dark",
                    cardBg = Color(0xFF111111),
                    contentColor = Color.White,
                    accentColor = extendedColors.accent,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setThemeMode("dark") }
                )

                // Light mode card
                ThemeModeCard(
                    label = "Light",
                    icon = Icons.Outlined.LightMode,
                    isSelected = state.themeMode == "light",
                    cardBg = Color(0xFFF5F5F5),
                    contentColor = Color(0xFF1A1A1A),
                    accentColor = extendedColors.accent,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setThemeMode("light") }
                )
            }
        }

        Spacer(modifier = Modifier.height(DiarySpacing.lg))

        // ── Accent Color ──
        Column(modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)) {
            Text(
                text = "ACCENT COLOR",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(DiarySpacing.sm))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DiarySpacing.sm),
                verticalArrangement = Arrangement.spacedBy(DiarySpacing.sm)
            ) {
                accentColorOptions.forEachIndexed { index, option ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(option.color)
                            .then(
                                if (state.accentIndex == index)
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            )
                            .clickable { viewModel.setAccentColor(index, option.key) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.accentIndex == index) {
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

            Spacer(modifier = Modifier.height(DiarySpacing.sm))

            // Accent color preview card
            val selectedAccent = accentColorOptions.getOrNull(state.accentIndex)
            if (selectedAccent != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Color name
                    Text(
                        text = selectedAccent.name,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Sample button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(selectedAccent.color)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Button",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        )
                    }

                    // Sample nav dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(selectedAccent.color)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(DiarySpacing.lg))

        // ── Font Size ──
        Column(modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)) {
            Text(
                text = "FONT SIZE",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(DiarySpacing.sm))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Small" to 0, "Medium" to 1, "Large" to 2).forEach { (label, index) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (state.fontSizeIndex == index) extendedColors.accent.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .then(
                                if (state.fontSizeIndex == index)
                                    Modifier.border(1.5.dp, extendedColors.accent, RoundedCornerShape(12.dp))
                                else Modifier
                            )
                            .clickable { viewModel.setFontSize(index) }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = when (index) { 0 -> 13.sp; 2 -> 17.sp; else -> 15.sp },
                                fontWeight = if (state.fontSizeIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (state.fontSizeIndex == index) extendedColors.accent
                                else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        // Auto-save indicator
        Text(
            text = "Changes auto-saved",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = DiarySpacing.lg)
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ThemeModeCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    cardBg: Color,
    contentColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier.border(2.dp, accentColor, RoundedCornerShape(16.dp))
                else Modifier
            )
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Mini phone preview
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBg)
                    .border(1.dp, contentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = label,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            )

            if (isSelected) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Active",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = accentColor
                    )
                )
            }
        }
    }
}
