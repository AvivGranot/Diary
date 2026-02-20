package com.proactivediary.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val DiaryShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Semantic shape aliases
val PillShape = RoundedCornerShape(50)
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
val CardShape = RoundedCornerShape(16.dp)
val BottomNavShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
val ChipShape = RoundedCornerShape(20.dp)
