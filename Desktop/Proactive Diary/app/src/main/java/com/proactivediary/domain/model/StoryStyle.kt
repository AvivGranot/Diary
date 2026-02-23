package com.proactivediary.domain.model

import androidx.compose.ui.graphics.Color

enum class StoryStyle(
    val key: String,
    val displayName: String,
    val description: String,
    val accentColor: Color,
    val promptSuffix: String
) {
    DREAMY(
        "dreamy", "Dreamy", "Soft, ethereal atmosphere",
        Color(0xFFB39DDB),
        "dreamy ethereal soft focus, pastel colors, gentle light rays, impressionist painting style"
    ),
    CINEMATIC(
        "cinematic", "Cinematic", "Film-like dramatic lighting",
        Color(0xFF90A4AE),
        "cinematic film still, dramatic lighting, wide angle, shallow depth of field, moody atmosphere"
    ),
    MINIMAL(
        "minimal", "Minimal", "Clean, simple composition",
        Color(0xFFE0E0E0),
        "minimalist composition, clean lines, muted tones, lots of negative space, modern art"
    ),
    VINTAGE(
        "vintage", "Vintage", "Retro film grain look",
        Color(0xFFBCAAA4),
        "vintage film photograph, warm faded tones, film grain, 1970s aesthetic, nostalgic"
    ),
    NATURE(
        "nature", "Nature", "Lush natural scenery",
        Color(0xFF81C784),
        "lush natural landscape, golden hour lighting, vivid colors, national geographic style"
    ),
    ABSTRACT(
        "abstract", "Abstract", "Bold shapes and colors",
        Color(0xFFFF8A65),
        "abstract art, bold geometric shapes, vibrant colors, contemporary digital art"
    )
}

enum class StoryAspectRatio(
    val key: String,
    val label: String,
    val apiSize: String,
    val widthPx: Int,
    val heightPx: Int
) {
    SQUARE("square", "Square", "1024x1024", 1024, 1024),
    STORY("story", "Story", "1024x1792", 1024, 1792),
    LANDSCAPE("landscape", "Wide", "1792x1024", 1792, 1024)
}
