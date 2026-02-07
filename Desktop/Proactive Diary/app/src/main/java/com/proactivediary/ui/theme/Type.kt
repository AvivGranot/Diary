package com.proactivediary.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.proactivediary.R

val CormorantGaramond = FontFamily(
    Font(R.font.cormorant_garamond_regular, FontWeight.Normal),
    Font(R.font.cormorant_garamond_italic, FontWeight.Normal, FontStyle.Italic)
)

val DiaryTypography = Typography(
    // Section titles: Cormorant Garamond 24sp
    headlineLarge = TextStyle(
        fontFamily = CormorantGaramond,
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.3.sp,
        lineHeight = 31.2.sp
    ),
    // Typewriter quote: Cormorant Garamond italic 20sp
    headlineMedium = TextStyle(
        fontFamily = CormorantGaramond,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        lineHeight = 30.sp
    ),
    // Body text: Roboto 14sp
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.4.sp
    ),
    // Body medium
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    ),
    // Body small
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp
    ),
    // Section numbers: Roboto 11sp
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 1.1.sp
    ),
    // Nav logo: Cormorant Garamond 16sp
    titleMedium = TextStyle(
        fontFamily = CormorantGaramond,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 3.sp
    ),
    // Large title
    titleLarge = TextStyle(
        fontFamily = CormorantGaramond,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 28.sp
    ),
    // Labels
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    ),
    // Label large
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 1.sp
    )
)
