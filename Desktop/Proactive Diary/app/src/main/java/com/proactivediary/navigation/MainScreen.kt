package com.proactivediary.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.home.DiaryHomeScreen
import com.proactivediary.ui.journal.JournalScreen
import com.proactivediary.ui.paywall.BillingViewModel
import com.proactivediary.ui.paywall.PaywallDialog
import com.proactivediary.ui.paywall.PurchaseResult
import com.proactivediary.ui.profile.ProfileScreen
import com.proactivediary.ui.settings.GoalsAndRemindersScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

// 3-tab layout: Journal | Diary (center) | Profile
private const val PAGE_JOURNAL = 0
private const val PAGE_DIARY = 1
private const val PAGE_PROFILE = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    analyticsService: AnalyticsService,
    deepLinkDestination: String? = null,
    deepLinkPrompt: String? = null,
    deepLinkGoalId: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
    onTabChanged: (Int) -> Unit = {},
    billingViewModel: BillingViewModel = hiltViewModel(),
    mainScreenViewModel: MainScreenViewModel = hiltViewModel()
) {
    val subscriptionState by billingViewModel.subscriptionState.collectAsState()
    val isFirstPaywallView by billingViewModel.isFirstPaywallView.collectAsState()
    var showPaywall by remember { mutableStateOf(false) }
    val purchaseResult by billingViewModel.purchaseResult.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    // Overlay sub-screen (Goals & Reminders) — shown on top of pager
    var showGoalsAndReminders by remember { mutableStateOf(false) }

    // Notification prompt carried through deep link
    var activeNotificationPrompt by remember { mutableStateOf<String?>(null) }

    // 3-page pager — starts on Diary tab (center)
    val pagerState = rememberPagerState(
        initialPage = PAGE_DIARY,
        pageCount = { 3 }
    )

    // Track last valid page for paywall bounce
    var lastValidPage by remember { mutableStateOf(PAGE_DIARY) }

    // Observe savedStateHandle for tab navigation requests (from NavGraph bottom nav)
    LaunchedEffect(Unit) {
        val handle = rootNavController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("navigateToTab", -1).collect { tab ->
            if (tab >= 0) {
                pagerState.animateScrollToPage(tab)
                handle["navigateToTab"] = -1
            }
        }
    }

    // Sync pager → analytics + notify parent of tab changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                lastValidPage = page
                onTabChanged(page)
                val tabName = when (page) {
                    PAGE_JOURNAL -> "journal"
                    PAGE_DIARY -> "diary"
                    PAGE_PROFILE -> "profile"
                    else -> "unknown"
                }
                analyticsService.logTabSwitched(tabName)
            }
    }

    // After successful payment, navigate to diary tab
    LaunchedEffect(purchaseResult) {
        if (purchaseResult is PurchaseResult.Success) {
            billingViewModel.consumePurchaseResult()
            billingViewModel.refreshSubscriptionState()
            pagerState.animateScrollToPage(PAGE_DIARY)
        }
    }

    // Handle deep link from notification
    LaunchedEffect(deepLinkDestination) {
        deepLinkDestination?.let { dest ->
            when (dest) {
                "write" -> {
                    activeNotificationPrompt = deepLinkPrompt
                    pagerState.animateScrollToPage(PAGE_DIARY)
                }
                "goals" -> {
                    showGoalsAndReminders = true
                }
            }
            onDeepLinkConsumed()
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 3-tab swipeable pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                key = { it }
            ) { page ->
                when (page) {
                    PAGE_JOURNAL -> {
                        JournalScreen(
                            onEntryClick = { entryId ->
                                rootNavController.navigate(Routes.EntryDetail.createRoute(entryId))
                            },
                            onEditEntry = { entryId ->
                                rootNavController.navigate(Routes.Write.create(entryId))
                            },
                            onNavigateToWrite = {
                                scope.launch { pagerState.animateScrollToPage(PAGE_DIARY) }
                            },
                            onNavigateToOnThisDay = {
                                rootNavController.navigate(Routes.OnThisDay.route)
                            },
                            onNavigateToRecentlyDeleted = {
                                rootNavController.navigate(Routes.RecentlyDeleted.route)
                            },
                            onNavigateToMap = {
                                rootNavController.navigate(Routes.PlacesMap.route)
                            },
                            onBack = null
                        )
                    }
                    PAGE_DIARY -> {
                        DiaryHomeScreen(
                            onSearchClick = {
                                scope.launch { pagerState.animateScrollToPage(PAGE_JOURNAL) }
                            },
                            onWriteClick = {
                                rootNavController.navigate(Routes.Write.create())
                            },
                            onEntryClick = { entryId ->
                                rootNavController.navigate(Routes.EntryDetail.createRoute(entryId))
                            },
                            onEditEntry = { entryId ->
                                rootNavController.navigate(Routes.Write.create(entryId))
                            },
                            onNavigateToJournal = {
                                scope.launch { pagerState.animateScrollToPage(PAGE_JOURNAL) }
                            }
                        )
                    }
                    PAGE_PROFILE -> {
                        ProfileScreen(
                            onNavigateToSettings = {
                                rootNavController.navigate(Routes.Settings.route)
                            },
                            onNavigateToGoals = { showGoalsAndReminders = true },
                            onNavigateToLayout = {
                                rootNavController.navigate(Routes.Layout.route)
                            },
                            onNavigateToExport = {
                                rootNavController.navigate(Routes.ExportData.route)
                            },
                            onNavigateToSupport = {
                                rootNavController.navigate(Routes.ContactSupport.createRoute("support"))
                            },
                            onNavigateToThemeEvolution = {
                                rootNavController.navigate(Routes.ThemeEvolution.route)
                            },
                            onNavigateToJournal = {
                                scope.launch { pagerState.animateScrollToPage(PAGE_JOURNAL) }
                            },
                            onSignOut = {
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                            }
                        )
                    }
                }
            }

            // Goals & Reminders overlay
            AnimatedVisibility(
                visible = showGoalsAndReminders,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                                if (totalDrag > 200f) {
                                    showGoalsAndReminders = false
                                    totalDrag = 0f
                                }
                            }
                        )
                    }) {
                    GoalsAndRemindersScreen(
                        onBack = { showGoalsAndReminders = false }
                    )
                }
            }

            // Bottom nav is now at NavGraph level (persistent across all screens)
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
            annualPrice = billingViewModel.getAnnualPrice()?.let { "$it/year" } ?: "$19.99/year",
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
