package com.proactivediary.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.proactivediary.R

// ── Font families ────────────────────────────────────────────────────
val InstrumentSerif = FontFamily(
    Font(R.font.instrument_serif_regular, FontWeight.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic)
)

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold)
)

// Fix: CormorantGaramond alias now correctly points to InstrumentSerif (was broken, pointed to PlusJakartaSans)
val CormorantGaramond = InstrumentSerif

// ── Typography scale ─────────────────────────────────────────────────
// Headlines/Display: Instrument Serif (serif) — elegant, editorial
// Title/Body/Label: Plus Jakarta Sans (sans-serif) — clean, modern
val DiaryTypography = Typography(
    // Display styles — Instrument Serif
    displayLarge = TextStyle(
        fontFamily = InstrumentSerif,
        fontSize = 57.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = InstrumentSerif,
        fontSize = 45.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = InstrumentSerif,
        fontSize = 36.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 44.sp
    ),

    // Headline styles — Instrument Serif
    headlineLarge = TextStyle(
        fontFamily = InstrumentSerif,
        fontSize = 32.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = InstrumentSerif,
        fontSize = 28.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = InstrumentSerif,
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 32.sp
    ),

    // Title styles — Plus Jakarta Sans
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles — Plus Jakarta Sans
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles — Plus Jakarta Sans
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
