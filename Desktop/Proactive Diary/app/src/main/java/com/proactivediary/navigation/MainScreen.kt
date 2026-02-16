package com.proactivediary.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import com.proactivediary.ui.journal.JournalScreen
import com.proactivediary.ui.settings.ReminderManagementScreen
import com.proactivediary.ui.paywall.BillingViewModel
import com.proactivediary.ui.paywall.PaywallDialog
import com.proactivediary.ui.paywall.PurchaseResult
import com.proactivediary.ui.settings.SettingsScreen
import com.proactivediary.ui.discover.DiscoverScreen
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
private const val PAGE_DISCOVER = 0
private const val PAGE_WRITE = 1
private const val PAGE_JOURNAL = 2
private const val PAGE_SETTINGS = 3

val bottomNavItems = listOf(
    BottomNavItem(Routes.Discover.route, "Discover", Icons.Outlined.Explore),
    BottomNavItem(WRITE_TAB, "Write", Icons.Outlined.Edit, iconSize = 24),
    BottomNavItem(Routes.Journal.route, "Journal", Icons.AutoMirrored.Outlined.MenuBook),
    BottomNavItem(Routes.Settings.route, "Settings", Icons.Outlined.Settings),
)

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    deepLinkDestination: String? = null,
    deepLinkPrompt: String? = null,
    deepLinkGoalId: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
    billingViewModel: BillingViewModel = hiltViewModel(),
    mainScreenViewModel: MainScreenViewModel = hiltViewModel(),
    discoveryViewModel: FeatureDiscoveryViewModel = hiltViewModel()
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

    // Coach mark
    val showSwipeHint by discoveryViewModel.showSwipeHint.collectAsState()

    // Overlay sub-screens (Goals, Reminders) — shown on top of pager
    var showGoals by remember { mutableStateOf(false) }
    var showReminders by remember { mutableStateOf(false) }

    // Notification prompt carried through deep link → shown as WelcomeBackOverlay → WriteScreen
    var activeNotificationPrompt by remember { mutableStateOf<String?>(null) }

    // Pager state — starts on Write tab (page 0)
    val pagerState = rememberPagerState(
        initialPage = PAGE_DISCOVER,
        pageCount = { 4 }
    )

    // Track the last valid page the user was on before a paywall bounce
    var lastValidPage by remember { mutableStateOf(PAGE_DISCOVER) }

    // Sync pager → bottom nav: when user swipes, update selection
    // Also handle paywall gate: if user swipes to Write while expired, bounce back
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page == PAGE_WRITE && !subscriptionState.isActive) {
                    // User swiped to Write tab but subscription is expired → gentle bounce back
                    showPaywall = true
                    pagerState.animateScrollToPage(lastValidPage)
                } else {
                    lastValidPage = page
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
            }
            onDeepLinkConsumed()
        }
    }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                )
                NavigationBar(
                    modifier = Modifier.height(56.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        val selected = pagerState.currentPage == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // Paywall gate: block Write tab when trial expired
                                    if (index == PAGE_WRITE && !subscriptionState.isActive) {
                                        showPaywall = true
                                        return@NavigationBarItem
                                    }
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            },
                            icon = {
                                Box {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(item.iconSize.dp)
                                    )
                                    if (index == PAGE_WRITE && !subscriptionState.isActive) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = "Pro",
                                            modifier = Modifier
                                                .size(12.dp)
                                                .align(Alignment.TopEnd)
                                                .offset(x = 4.dp, y = (-2).dp),
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                unselectedTextColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                indicatorColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // Swipeable tab pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                key = { it }
            ) { page ->
                when (page) {
                    PAGE_DISCOVER -> {
                        DiscoverScreen(
                            onWriteAbout = { inspiration ->
                                if (subscriptionState.isActive) {
                                    scope.launch { pagerState.animateScrollToPage(PAGE_WRITE) }
                                } else {
                                    showPaywall = true
                                }
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
                            notificationPrompt = activeNotificationPrompt,
                            notificationStreak = currentStreak,
                            onNotificationPromptConsumed = { activeNotificationPrompt = null }
                        )
                    }
                    PAGE_JOURNAL -> {
                        JournalScreen(
                            onEntryClick = { entryId ->
                                rootNavController.navigate(Routes.EntryDetail.createRoute(entryId))
                            },
                            onNavigateToWrite = {
                                if (subscriptionState.isActive) {
                                    scope.launch { pagerState.animateScrollToPage(PAGE_WRITE) }
                                } else {
                                    showPaywall = true
                                }
                            },
                            onNavigateToOnThisDay = {
                                rootNavController.navigate(Routes.OnThisDay.route)
                            },
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
                            onNavigateToYearInReview = {
                                rootNavController.navigate(Routes.YearInReview.route)
                            },
                            onNavigateToBugReport = {
                                rootNavController.navigate(Routes.ContactSupport.createRoute("bug"))
                            },
                            onNavigateToSupport = {
                                rootNavController.navigate(Routes.ContactSupport.createRoute("support"))
                            },
                            onNavigateToDiaryWrapped = {
                                rootNavController.navigate(Routes.DiaryWrapped.route)
                            },
                            onNavigateToThemeEvolution = {
                                rootNavController.navigate(Routes.ThemeEvolution.route)
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
                visible = showSwipeHint && pagerState.currentPage == PAGE_DISCOVER,
                onDismiss = { discoveryViewModel.dismissSwipeHint() }
            )
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
            annualPrice = billingViewModel.getAnnualPrice()?.let { "$it/year" } ?: "$30/year",
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
