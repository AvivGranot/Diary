package com.proactivediary.ui.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.proactivediary.analytics.AnalyticsService

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NotebookAnimationScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    analyticsService: AnalyticsService
) {
    var showContinue by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        analyticsService.logOnboardingNotifShown()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            analyticsService.logOnboardingNotifGranted()
            onContinue()
        } else {
            analyticsService.logOnboardingNotifDenied()
            onSkip()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3F0))
            .navigationBarsPadding()
    ) {
        // WebView takes all available space above the button area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                        setBackgroundColor(android.graphics.Color.parseColor("#F5F3F0"))
                        clearCache(true)

                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onAnimationComplete() {
                                // Post to main thread to update Compose state
                                post { showContinue = true }
                            }
                        }, "Android")

                        webViewClient = WebViewClient()
                        loadUrl("file:///android_asset/notebook-animation/index.html")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Continue button — fades in after animation completes
        AnimatedVisibility(
            visible = showContinue,
            enter = fadeIn(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF313131))
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            analyticsService.logOnboardingNotifGranted()
                            onContinue()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Continue",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
