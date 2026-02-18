package com.proactivediary.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.proactivediary.ui.components.FeatureDiscoveryViewModel
import com.proactivediary.ui.components.SwipeHint
import com.proactivediary.ui.goals.GoalsScreen
import com.proactivediary.ui.settings.ReminderManagementScreen
import com.proactivediary.ui.paywall.BillingViewModel
import com.proactivediary.ui.paywall.PaywallDialog
import com.proactivediary.ui.paywall.PurchaseResult
import com.proactivediary.ui.settings.SettingsScreen
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.notes.NoteInboxViewModel
import com.proactivediary.ui.quotes.QuotesScreen
import com.proactivediary.ui.write.WriteScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSize: Int = 23
)

private const val WRITE_TAB = "write_tab"
private const val PAGE_QUOTES = 0
private const val PAGE_WRITE = 1
private const val PAGE_SETTINGS = 2

val bottomNavItems = listOf(
    BottomNavItem(Routes.Quotes.route, "Quotes", Icons.Outlined.FormatQuote),
    BottomNavItem(WRITE_TAB, "Write", Icons.Outlined.Edit, iconSize = 24),
    BottomNavItem(Routes.Settings.route, "Settings", Icons.Outlined.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    analyticsService: AnalyticsService,
    deepLinkDestination: String? = null,
    deepLinkPrompt: String? = null,
    deepLinkGoalId: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
    billingViewModel: BillingViewModel = hiltViewModel(),
    mainScreenViewModel: MainScreenViewModel = hiltViewModel(),
    discoveryViewModel: FeatureDiscoveryViewModel = hiltViewModel(),
    noteInboxViewModel: NoteInboxViewModel = hiltViewModel()
) {
    val subscriptionState by billingViewModel.subscriptionState.collectAsState()
    val isFirstPaywallView by billingViewModel.isFirstPaywallView.collectAsState()
    val currentStreak by mainScreenViewModel.currentStreak.collectAsState()
    val streakEnabled by mainScreenViewModel.streakEnabled.collectAsState()
    var showPaywall by remember { mutableStateOf(false) }
    val purchaseResult by billingViewModel.purchaseResult.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val haptic = LocalHapticFeedback.current
    val unreadNoteCount by noteInboxViewModel.unreadCount.collectAsState(initial = 0)

    // Coach mark
    val showSwipeHint by discoveryViewModel.showSwipeHint.collectAsState()

    // Overlay sub-screens (Goals, Reminders) — shown on top of pager
    var showGoals by remember { mutableStateOf(false) }
    var showReminders by remember { mutableStateOf(false) }

    // Notification prompt carried through deep link → shown as WelcomeBackOverlay → WriteScreen
    var activeNotificationPrompt by remember { mutableStateOf<String?>(null) }

    // Pager state — starts on Write tab (page 0)
    val pagerState = rememberPagerState(
        initialPage = PAGE_QUOTES,
        pageCount = { 3 }
    )

    // Track the last valid page the user was on before a paywall bounce
    var lastValidPage by remember { mutableStateOf(PAGE_QUOTES) }

    // Observe savedStateHandle for tab navigation requests (e.g., from Journal "Begin" button)
    LaunchedEffect(Unit) {
        val handle = rootNavController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("navigateToTab", -1).collect { tab ->
            if (tab >= 0) {
                pagerState.animateScrollToPage(tab)
                handle["navigateToTab"] = -1
            }
        }
    }

    // Sync pager → bottom nav: when user swipes, update selection
    // Also handle paywall gate: if user swipes to Write while expired, bounce back
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page == PAGE_WRITE && !subscriptionState.isActive) {
                    analyticsService.logPaywallShownWithTrigger("swipe_to_write")
                    showPaywall = true
                    pagerState.animateScrollToPage(lastValidPage)
                } else {
                    lastValidPage = page
                    val tabName = when (page) {
                        PAGE_QUOTES -> "quotes"
                        PAGE_WRITE -> "write"
                        PAGE_SETTINGS -> "settings"
                        else -> "unknown"
                    }
                    analyticsService.logTabSwitched(tabName)
                }
            }
    }

    // After successful payment, navigate to Write tab
    LaunchedEffect(purchaseResult) {
        if (purchaseResult is PurchaseResult.Success) {
            billingViewModel.consumePurchaseResult()
            billingViewModel.refreshSubscriptionState()
            pagerState.animateScrollToPage(PAGE_WRITE)
        }
    }

    // Handle deep link from notification (with paywall gate)
    LaunchedEffect(deepLinkDestination) {
        deepLinkDestination?.let { dest ->
            when (dest) {
                "write" -> {
                    if (subscriptionState.isActive) {
                        activeNotificationPrompt = deepLinkPrompt
                        pagerState.animateScrollToPage(PAGE_WRITE)
                    } else {
                        showPaywall = true
                    }
                }
                "goals" -> {
                    showGoals = true
                }
                "note_inbox" -> {
                    rootNavController.navigate(Routes.NoteInbox.route)
                }
            }
            onDeepLinkConsumed()
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Swipeable tab pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                key = { it }
            ) { page ->
                when (page) {
                    PAGE_QUOTES -> {
                        QuotesScreen(
                            onQuoteClick = { quoteId ->
                                rootNavController.navigate(Routes.QuoteDetail.createRoute(quoteId))
                            },
                            onSendNote = {
                                rootNavController.navigate(Routes.ComposeNote.route)
                            },
                            unreadNoteCount = unreadNoteCount,
                            onNotificationBellClick = {
                                analyticsService.logNoteInboxOpened(unreadNoteCount)
                                rootNavController.navigate(Routes.NoteInbox.route)
                            }
                        )
                    }
                    PAGE_WRITE -> {
                        WriteScreen(
                            onOpenDesignStudio = {
                                rootNavController.navigate(Routes.DesignStudio.createRoute(edit = true))
                            },
                            onEntrySaved = {
                                scope.launch {
                                    billingViewModel.refreshSubscriptionState()
                                }
                            },
                            onNavigateToEntry = { entryId ->
                                rootNavController.navigate("write?entryId=$entryId")
                            },
                            notificationPrompt = activeNotificationPrompt,
                            notificationStreak = currentStreak,
                            onNotificationPromptConsumed = { activeNotificationPrompt = null }
                        )
                    }
                    PAGE_SETTINGS -> {
                        SettingsScreen(
                            onOpenDesignStudio = {
                                rootNavController.navigate(Routes.DesignStudio.createRoute(edit = true))
                            },
                            onNavigateToGoals = {
                                showGoals = true
                            },
                            onNavigateToReminders = {
                                showReminders = true
                            },
                            onNavigateToTypewriter = {
                                rootNavController.navigate(Routes.Typewriter.route) {
                                    popUpTo(Routes.Main.route) { inclusive = true }
                                }
                            },
                            onNavigateToSupport = {
                                rootNavController.navigate(Routes.ContactSupport.createRoute("support"))
                            },
                            onNavigateToDiaryWrapped = {
                                rootNavController.navigate(Routes.DiaryWrapped.route)
                            },
                            onNavigateToThemeEvolution = {
                                rootNavController.navigate(Routes.ThemeEvolution.route)
                            },
                            onNavigateToJournal = {
                                rootNavController.navigate(Routes.Journal.route)
                            }
                        )
                    }
                }
            }

            // Goals overlay
            AnimatedVisibility(
                visible = showGoals,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                GoalsScreen(
                    onBack = { showGoals = false }
                )
            }

            // Reminders overlay
            AnimatedVisibility(
                visible = showReminders,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                ReminderManagementScreen(
                    onBack = { showReminders = false }
                )
            }

            // Coach mark — shown on top of everything
            SwipeHint(
                visible = showSwipeHint && pagerState.currentPage == PAGE_QUOTES,
                onDismiss = { discoveryViewModel.dismissSwipeHint() }
            )

            // Bell icon — notification inbox (top-right, below status bar)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 12.dp)
            ) {
                IconButton(
                    onClick = {
                        analyticsService.logNoteInboxOpened(unreadNoteCount)
                        rootNavController.navigate(Routes.NoteInbox.route)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNoteCount > 0) {
                                Badge {
                                    Text(
                                        text = "$unreadNoteCount",
                                        style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Bottom navigation — flat, flush, Instagram style
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Hairline divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(49.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(49.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (!selected) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (index == PAGE_WRITE && !subscriptionState.isActive) {
                                            showPaywall = true
                                            return@clickable
                                        }
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (selected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                )
                                if (index == PAGE_WRITE && !subscriptionState.isActive) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = "Pro",
                                        modifier = Modifier
                                            .size(10.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-2).dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Paywall dialog
    if (showPaywall) {
        PaywallDialog(
            onDismiss = {
                billingViewModel.markPaywallViewed()
                showPaywall = false
            },
            entryCount = subscriptionState.entryCount,
            totalWords = subscriptionState.totalWords,
            isFirstPaywallView = isFirstPaywallView,
            monthlyPrice = billingViewModel.getMonthlyPrice()?.let { "$it/month" } ?: "$5/month",
            annualPrice = billingViewModel.getAnnualPrice()?.let { "$it/year" } ?: "$40/year",
            onSelectPlan = { sku ->
                billingViewModel.markPaywallViewed()
                activity?.let { billingViewModel.launchPurchase(it, sku) }
                showPaywall = false
            },
            onRestore = {
                billingViewModel.restorePurchases()
                showPaywall = false
            }
        )
    }
}
