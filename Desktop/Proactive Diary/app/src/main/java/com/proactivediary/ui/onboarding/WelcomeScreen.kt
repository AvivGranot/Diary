package com.proactivediary.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.PlusJakartaSans
import com.proactivediary.domain.model.TaggedContact
import com.proactivediary.ui.write.resolveContact

@Composable
fun WelcomeScreen(
    onChannelSelected: (channel: String, contact: TaggedContact?) -> Unit,
    analyticsService: AnalyticsService
) {
    val context = LocalContext.current
    val firstName = Firebase.auth.currentUser?.displayName
        ?.split(" ")?.firstOrNull() ?: "there"

    val contactPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val contact = resolveContact(context, uri)
            onChannelSelected("contacts", contact)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingScreenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = OnboardingHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.6f))

        // Welcome heading
        Text(
            text = "Welcome $firstName!",
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 34.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 40.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtext
        Text(
            text = "Share anonymous notes to your contacts\u2009\u2014\u2009positive sentiments only (negative ones won't be approved \uD83D\uDE0A)",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(0.5f))

        // Channel buttons — ink fill with brand icon tints
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // WhatsApp
            ChannelButton(
                icon = Icons.Outlined.Chat,
                label = "Send via WhatsApp",
                iconTint = Color(0xFF25D366),
                onClick = { onChannelSelected("whatsapp", null) }
            )

            // Instagram
            ChannelButton(
                icon = Icons.Outlined.CameraAlt,
                label = "Send via Instagram",
                iconTint = Color(0xFFE4405F),
                onClick = { onChannelSelected("instagram", null) }
            )

            // Browse Contacts — outlined style
            OnboardingSecondaryButton(
                text = "Browse Contacts",
                onClick = { contactPicker.launch(null) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ChannelButton(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(OnboardingInk, OnboardingButtonShape)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            label,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
    }
}
