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
import com.proactivediary.ui.activity.ActivityScreen
import com.proactivediary.ui.activity.ActivityViewModel
import com.proactivediary.ui.home.DiaryHomeScreen
import com.proactivediary.ui.notes.NoteInboxScreen
import com.proactivediary.ui.notes.NoteInboxViewModel
import com.proactivediary.ui.paywall.BillingViewModel
import com.proactivediary.ui.paywall.PaywallDialog
import com.proactivediary.ui.paywall.PurchaseResult
import com.proactivediary.ui.profile.ProfileScreen
import com.proactivediary.ui.quotes.QuotesScreen
import com.proactivediary.ui.settings.GoalsAndRemindersScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

// 5-tab layout: Quotes | Notes | Diary (center) | Activity | Profile
private const val PAGE_QUOTES = 0
private const val PAGE_NOTES = 1
private const val PAGE_DIARY = 2
private const val PAGE_ACTIVITY = 3
private const val PAGE_PROFILE = 4

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
    mainScreenViewModel: MainScreenViewModel = hiltViewModel(),
    noteInboxViewModel: NoteInboxViewModel = hiltViewModel(),
    activityViewModel: ActivityViewModel = hiltViewModel()
) {
    val subscriptionState by billingViewModel.subscriptionState.collectAsState()
    val isFirstPaywallView by billingViewModel.isFirstPaywallView.collectAsState()
    var showPaywall by remember { mutableStateOf(false) }
    val purchaseResult by billingViewModel.purchaseResult.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val unreadNoteCount by noteInboxViewModel.unreadCount.collectAsState(initial = 0)
    val activityBadgeCount by activityViewModel.unreadCount.collectAsState()

    // Overlay sub-screen (Goals & Reminders) — shown on top of pager
    var showGoalsAndReminders by remember { mutableStateOf(false) }

    // Notification prompt carried through deep link
    var activeNotificationPrompt by remember { mutableStateOf<String?>(null) }

    // 5-page pager — starts on Quotes tab (social first)
    val pagerState = rememberPagerState(
        initialPage = PAGE_QUOTES,
        pageCount = { 5 }
    )

    // Track last valid page for paywall bounce
    var lastValidPage by remember { mutableStateOf(PAGE_QUOTES) }

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
                    PAGE_QUOTES -> "quotes"
                    PAGE_NOTES -> "notes"
                    PAGE_DIARY -> "diary"
                    PAGE_ACTIVITY -> "activity"
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
                "note_inbox" -> {
                    rootNavController.navigate(Routes.NoteInbox.route)
                }
            }
            onDeepLinkConsumed()
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 5-tab swipeable pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 2,
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
                    PAGE_NOTES -> {
                        NoteInboxScreen(
                            onBack = null, // No back button — it's a tab
                            onNoteClick = { noteId ->
                                rootNavController.navigate(Routes.EnvelopeReveal.createRoute(noteId))
                            },
                            onComposeNote = {
                                rootNavController.navigate(Routes.ComposeNote.route)
                            }
                        )
                    }
                    PAGE_DIARY -> {
                        DiaryHomeScreen(
                            onSearchClick = {
                                rootNavController.navigate(Routes.Journal.route)
                            },
                            onWriteClick = {
                                rootNavController.navigate(Routes.Write.create())
                            },
                            onEntryClick = { entryId ->
                                rootNavController.navigate(Routes.EntryDetail.createRoute(entryId))
                            },
                            onNavigateToJournal = {
                                rootNavController.navigate(Routes.Journal.route)
                            }
                        )
                    }
                    PAGE_ACTIVITY -> {
                        ActivityScreen()
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
                                rootNavController.navigate(Routes.Journal.route)
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
