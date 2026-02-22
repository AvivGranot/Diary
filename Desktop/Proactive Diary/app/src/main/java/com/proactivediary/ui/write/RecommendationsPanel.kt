package com.proactivediary.ui.write

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.domain.recommendations.DevicePhoto
import com.proactivediary.domain.recommendations.LocationSuggestion
import com.proactivediary.domain.recommendations.NearbyPlace
import com.proactivediary.domain.recommendations.RecentEntry
import com.proactivediary.domain.recommendations.RecommendationsState
import com.proactivediary.ui.theme.InstrumentSerif

private const val TAB_RECOMMENDED = 0
private const val TAB_RECENT = 1

@Composable
fun RecommendationsPanel(
    state: RecommendationsState,
    colorKey: String,
    onNearbyPlaceTapped: (NearbyPlace) -> Unit,
    onPhotoTapped: (DevicePhoto) -> Unit,
    onLocationTapped: (LocationSuggestion) -> Unit,
    onRecentEntryTapped: (String) -> Unit,
    onRequestPhotoPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = DiaryThemeConfig.textColorFor(colorKey)
    val secondaryColor = DiaryThemeConfig.secondaryTextColorFor(colorKey)
    var selectedTab by remember { mutableIntStateOf(TAB_RECOMMENDED) }

    val hasContent = state.nearbyPlaces.isNotEmpty() ||
            state.devicePhotos.isNotEmpty() ||
            state.pastLocations.isNotEmpty() ||
            state.recentEntries.isNotEmpty() ||
            !state.hasPhotoPermission

    AnimatedVisibility(visible = hasContent, enter = fadeIn(), exit = fadeOut()) {
        Column(modifier = modifier.fillMaxWidth().padding(top = 24.dp)) {
            // Chevron hint
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowUp,
                contentDescription = "Scroll for inspiration",
                tint = secondaryColor.copy(alpha = 0.25f),
                modifier = Modifier.align(Alignment.CenterHorizontally).size(20.dp)
            )

            Spacer(Modifier.height(4.dp))

            // Tab pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TabPill(
                    text = "Recommended",
                    isSelected = selectedTab == TAB_RECOMMENDED,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    onClick = { selectedTab = TAB_RECOMMENDED }
                )
                Spacer(Modifier.width(12.dp))
                TabPill(
                    text = "Recent",
                    isSelected = selectedTab == TAB_RECENT,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    onClick = { selectedTab = TAB_RECENT }
                )
            }

            Spacer(Modifier.height(16.dp))

            when (selectedTab) {
                TAB_RECOMMENDED -> RecommendedTab(
                    nearbyPlaces = state.nearbyPlaces,
                    devicePhotos = state.devicePhotos,
                    pastLocations = state.pastLocations,
                    hasPhotoPermission = state.hasPhotoPermission,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    onNearbyPlaceTapped = onNearbyPlaceTapped,
                    onPhotoTapped = onPhotoTapped,
                    onLocationTapped = onLocationTapped,
                    onRequestPhotoPermission = onRequestPhotoPermission
                )
                TAB_RECENT -> RecentTab(
                    entries = state.recentEntries,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    onEntryTapped = onRecentEntryTapped
                )
            }

            Spacer(Modifier.height(24.dp)) // Space above toolbar
        }
    }
}

@Composable
private fun TabPill(
    text: String,
    isSelected: Boolean,
    textColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) textColor.copy(alpha = 0.08f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) textColor else secondaryColor.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun RecommendedTab(
    nearbyPlaces: List<NearbyPlace>,
    devicePhotos: List<DevicePhoto>,
    pastLocations: List<LocationSuggestion>,
    hasPhotoPermission: Boolean,
    textColor: Color,
    secondaryColor: Color,
    onNearbyPlaceTapped: (NearbyPlace) -> Unit,
    onPhotoTapped: (DevicePhoto) -> Unit,
    onLocationTapped: (LocationSuggestion) -> Unit,
    onRequestPhotoPermission: () -> Unit
) {
    // Nearby Places
    if (nearbyPlaces.isNotEmpty()) {
        SectionHeader("Nearby Places", secondaryColor)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(nearbyPlaces) { place ->
                NearbyPlaceCard(
                    place = place,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    onClick = { onNearbyPlaceTapped(place) }
                )
            }
        }
        Spacer(Modifier.height(20.dp))
    }

    // Your Photos
    SectionHeader("Your Photos", secondaryColor)
    if (hasPhotoPermission && devicePhotos.isNotEmpty()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(devicePhotos) { photo ->
                PhotoCard(
                    photo = photo,
                    secondaryColor = secondaryColor,
                    onClick = { onPhotoTapped(photo) }
                )
            }
        }
    } else if (!hasPhotoPermission) {
        PermissionCard(
            text = "Grant access to see your photos",
            secondaryColor = secondaryColor,
            onClick = onRequestPhotoPermission
        )
    } else {
        EmptyHint("No recent photos", secondaryColor)
    }

    Spacer(Modifier.height(20.dp))

    // Places You've Been
    if (pastLocations.isNotEmpty()) {
        SectionHeader("Places You've Been", secondaryColor)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(pastLocations) { loc ->
                LocationCard(
                    location = loc,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    onClick = { onLocationTapped(loc) }
                )
            }
        }
    }
}

@Composable
private fun RecentTab(
    entries: List<RecentEntry>,
    textColor: Color,
    secondaryColor: Color,
    onEntryTapped: (String) -> Unit
) {
    if (entries.isEmpty()) {
        EmptyHint("No entries yet", secondaryColor)
        return
    }

    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entries.forEach { entry ->
            RecentEntryCard(
                entry = entry,
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = { onEntryTapped(entry.id) }
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, color: Color) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = InstrumentSerif,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
            color = color.copy(alpha = 0.5f)
        ),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
    )
}

@Composable
private fun NearbyPlaceCard(
    place: NearbyPlace,
    textColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = secondaryColor.copy(alpha = 0.05f),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Place,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = textColor.copy(alpha = 0.6f)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val typeLabel = place.types.firstOrNull()
                    ?.replace("_", " ")
                    ?.replaceFirstChar { it.uppercase() }
                if (typeLabel != null) {
                    Text(
                        text = typeLabel,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            color = secondaryColor.copy(alpha = 0.5f)
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoCard(
    photo: DevicePhoto,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Box {
            AsyncImage(
                model = photo.uri,
                contentDescription = "Photo",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            // Date overlay at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = DateUtils.getRelativeTimeSpanString(
                        photo.dateTaken,
                        System.currentTimeMillis(),
                        DateUtils.DAY_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE
                    ).toString(),
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun LocationCard(
    location: LocationSuggestion,
    textColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(70.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = secondaryColor.copy(alpha = 0.05f),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = textColor.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = DateUtils.getRelativeTimeSpanString(
                        location.lastVisited,
                        System.currentTimeMillis(),
                        DateUtils.DAY_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE
                    ).toString(),
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 11.sp,
                        color = secondaryColor.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
private fun RecentEntryCard(
    entry: RecentEntry,
    textColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = secondaryColor.copy(alpha = 0.04f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue accent dot (mood feature removed)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title.ifBlank { entry.excerpt },
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.title.isNotBlank() && entry.excerpt.isNotBlank()) {
                    Text(
                        text = entry.excerpt,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = secondaryColor.copy(alpha = 0.6f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = DateUtils.getRelativeTimeSpanString(
                    entry.createdAt,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString(),
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    color = secondaryColor.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun PermissionCard(
    text: String,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = secondaryColor.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = secondaryColor.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    color = secondaryColor.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
private fun EmptyHint(text: String, secondaryColor: Color) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = FontFamily.Default,
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            color = secondaryColor.copy(alpha = 0.3f)
        ),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}
