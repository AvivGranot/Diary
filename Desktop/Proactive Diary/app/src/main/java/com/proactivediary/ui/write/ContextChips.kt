package com.proactivediary.ui.write

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LocationChip(
    locationName: String,
    latitude: Double? = null,
    longitude: Double? = null,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .then(
                if (latitude != null && longitude != null) {
                    Modifier.clickable {
                        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(locationName)})")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    }
                } else Modifier
            )
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = textColor.copy(alpha = 0.5f)
        )
        Text(
            text = locationName,
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun WeatherChip(
    temperature: Double,
    condition: String,
    weatherIcon: String,
    textColor: Color,
    useFahrenheit: Boolean = true,
    modifier: Modifier = Modifier
) {
    val displayTemp = if (useFahrenheit) {
        "%.0f\u00B0F".format(temperature * 9.0 / 5.0 + 32)
    } else {
        "%.0f\u00B0C".format(temperature)
    }

    val icon = weatherCodeToIcon(weatherIcon)

    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = textColor.copy(alpha = 0.5f)
        )
        Text(
            text = "$displayTemp $condition",
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.5f)
            )
        )
    }
}

private fun weatherCodeToIcon(code: String): ImageVector {
    return when (code.toIntOrNull() ?: 0) {
        0, 1 -> Icons.Outlined.WbSunny
        2, 3, 45, 48 -> Icons.Outlined.Cloud
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> Icons.Outlined.WaterDrop
        71, 73, 75, 77, 85, 86 -> Icons.Outlined.AcUnit
        95, 96, 99 -> Icons.Outlined.Thunderstorm
        else -> Icons.Outlined.Grain
    }
}
