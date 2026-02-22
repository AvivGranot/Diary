package com.proactivediary.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.activity.ActivityViewModel
import com.proactivediary.ui.components.DiaryBottomNav
import com.proactivediary.ui.journal.EntryDetailScreen
import com.proactivediary.ui.notes.NoteInboxViewModel
import com.proactivediary.ui.notes.ComposeNoteScreen
import com.proactivediary.ui.notes.EnvelopeRevealScreen
import com.proactivediary.ui.notes.NoteInboxScreen
import com.proactivediary.ui.onboarding.NotificationPermissionScreen
import com.proactivediary.ui.onboarding.OnboardingGoalsScreen
import com.proactivediary.ui.onboarding.ProfilePictureScreen
import com.proactivediary.ui.onboarding.QuickAuthScreen
import com.proactivediary.ui.onboarding.QuotesPreviewScreen
import com.proactivediary.ui.onboarding.QuotesPreviewViewModel
import com.proactivediary.ui.onboarding.WriteFirstNoteScreen
import com.proactivediary.ui.paywall.BillingViewModel
import com.proactivediary.ui.paywall.PaywallDialog
import com.proactivediary.ui.quotes.QuoteDetailScreen
import com.proactivediary.ui.typewriter.TypewriterScreen
import com.proactivediary.ui.export.YearInReviewScreen
import com.proactivediary.ui.onthisday.OnThisDayScreen
import com.proactivediary.ui.settings.ContactSupportScreen
import com.proactivediary.ui.settings.ExportScreen
import com.proactivediary.ui.settings.LayoutScreen
import com.proactivediary.ui.insights.ThemeEvolutionScreen
import com.proactivediary.ui.write.WriteScreen

// Onboarding routes where the bottom nav should be hidden
private val onboardingRoutes = setOf(
    Routes.Typewriter.route,
    Routes.QuickAuth.route,
    Routes.ProfilePicture.route,
    Routes.WriteFirstNote.route,
    Routes.QuotesPreview.route,
    Routes.OnboardingGoals.route,
    Routes.NotificationPermission.route
)

@Composable
fun ProactiveDiaryNavHost(
    analyticsService: AnalyticsService,
    navController: NavHostController = rememberNavController(),
    viewModel: NavViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
    deepLinkDestination: String? = null,
    deepLinkPrompt: String? = null,
    deepLinkGoalId: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val startDestination by viewModel.startDestination.collectAsState()
    val subscriptionState by billingViewModel.subscriptionState.collectAsState()
    var showPaywall by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    // Badge counts for bottom nav
    val noteInboxViewModel: NoteInboxViewModel = hiltViewModel()
    val activityViewModel: ActivityViewModel = hiltViewModel()
    val unreadNoteCount by noteInboxViewModel.unreadCount.collectAsState(initial = 0)
    val activityBadgeCount by activityViewModel.unreadCount.collectAsState()

    // Track selected tab index for the bottom nav
    var selectedTabIndex by remember { mutableIntStateOf(2) } // Default: Diary (center)

    // Observe current route to decide whether to show bottom nav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav by remember(currentRoute) {
        derivedStateOf {
            currentRoute != null && currentRoute !in onboardingRoutes
        }
    }

    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination!!
        ) {
            // ── Onboarding ──
            // Flow: Typewriter → QuickAuth → ProfilePicture → WriteFirstNote → QuotesPreview →
            //       OnboardingGoals → NotificationPermission → Main

            composable(Routes.Typewriter.route) {
                TypewriterScreen(
                    onNavigateToDesignStudio = {
                        navController.navigate(Routes.WriteFirstNote.create("onboarding_goals")) {
                            popUpTo(Routes.Typewriter.route) { inclusive = true }
                        }
                    },
                    onNavigateToMain = {
                        navController.navigate(Routes.Main.route) {
                            popUpTo(Routes.Typewriter.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.OnboardingGoals.route) {
                OnboardingGoalsScreen(
                    onDone = {
                        navController.navigate(Routes.NotificationPermission.route) {
                            popUpTo(Routes.OnboardingGoals.route) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(Routes.NotificationPermission.route) {
                            popUpTo(Routes.OnboardingGoals.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.NotificationPermission.route) {
                NotificationPermissionScreen(
                    onContinue = {
                        analyticsService.logOnboardingCompleted(designCustomized = false, goalsSet = 0)
                        viewModel.markOnboardingComplete()
                        navController.navigate(Routes.Main.route) {
                            popUpTo(Routes.NotificationPermission.route) { inclusive = true }
                        }
                    },
                    onSkip = {
                        analyticsService.logOnboardingCompleted(designCustomized = false, goalsSet = 0)
                        viewModel.markOnboardingComplete()
                        navController.navigate(Routes.Main.route) {
                            popUpTo(Routes.NotificationPermission.route) { inclusive = true }
                        }
                    },
                    analyticsService = analyticsService
                )
            }

            // ── Main (5-tab) ──

            composable(Routes.Main.route) {
                MainScreen(
                    rootNavController = navController,
                    analyticsService = analyticsService,
                    deepLinkDestination = deepLinkDestination,
                    deepLinkPrompt = deepLinkPrompt,
                    deepLinkGoalId = deepLinkGoalId,
                    onDeepLinkConsumed = onDeepLinkConsumed,
                    billingViewModel = billingViewModel,
                    onTabChanged = { index -> selectedTabIndex = index }
                )
            }

            // ── Standalone screens (pushed on top of Main) ──

            composable(Routes.Journal.route) {
                com.proactivediary.ui.journal.JournalScreen(
                    onEntryClick = { entryId ->
                        navController.navigate(Routes.EntryDetail.createRoute(entryId))
                    },
                    onNavigateToWrite = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle?.set("navigateToTab", 2) // PAGE_DIARY
                        navController.popBackStack()
                    },
                    onNavigateToOnThisDay = {
                        navController.navigate(Routes.OnThisDay.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.EntryDetail.route,
                arguments = listOf(
                    navArgument("entryId") { type = NavType.StringType }
                )
            ) {
                EntryDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { entryId ->
                        navController.navigate(Routes.Write.create(entryId))
                    },
                    canEdit = subscriptionState.isActive
                )
            }

            // Write with entryId — for editing from EntryDetail
            composable(
                route = Routes.Write.route,
                arguments = listOf(
                    navArgument("entryId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                WriteScreen()
            }

            // Settings (standalone, pushed from Profile tab gear icon)
            composable(Routes.Settings.route) {
                com.proactivediary.ui.settings.SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToTypewriter = {
                        navController.navigate(Routes.Typewriter.route) {
                            popUpTo(Routes.Main.route) { inclusive = true }
                        }
                    }
                )
            }

            // Goals & Reminders (combined screen)
            composable(Routes.Goals.route) {
                com.proactivediary.ui.settings.GoalsAndRemindersScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Reminders route also points to combined screen
            composable(Routes.Reminders.route) {
                com.proactivediary.ui.settings.GoalsAndRemindersScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Layout (replaces Design Studio)
            composable(Routes.Layout.route) {
                LayoutScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Export Data
            composable(Routes.ExportData.route) {
                ExportScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.YearInReview.route) {
                var showYearPaywall by remember { mutableStateOf(false) }
                val yearActivity = (LocalContext.current as? Activity)

                YearInReviewScreen(
                    onBack = { navController.popBackStack() },
                    isPremium = subscriptionState.isActive,
                    onShowPaywall = { showYearPaywall = true }
                )

                if (showYearPaywall) {
                    PaywallDialog(
                        onDismiss = { showYearPaywall = false },
                        monthlyPrice = billingViewModel.getMonthlyPrice()?.let { "$it/month" } ?: "\$5/month",
                        annualPrice = billingViewModel.getAnnualPrice()?.let { "$it/year" } ?: "\$40/year",
                        onSelectPlan = { sku ->
                            yearActivity?.let { billingViewModel.launchPurchase(it, sku) }
                            showYearPaywall = false
                        },
                        onRestore = {
                            billingViewModel.restorePurchases()
                            showYearPaywall = false
                        }
                    )
                }
            }

            composable(Routes.OnThisDay.route) {
                OnThisDayScreen(
                    onBack = { navController.popBackStack() },
                    onEntryTap = { entryId ->
                        navController.navigate(Routes.EntryDetail.createRoute(entryId))
                    }
                )
            }

            composable(Routes.ThemeEvolution.route) {
                ThemeEvolutionScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.ContactSupport.route,
                arguments = listOf(navArgument("category") {
                    type = NavType.StringType
                    defaultValue = "support"
                })
            ) {
                ContactSupportScreen(onBack = { navController.popBackStack() })
            }

            // ── Social Onboarding ──

            composable(Routes.QuickAuth.route) {
                QuickAuthScreen(
                    onAuthenticated = {
                        navController.navigate(Routes.ProfilePicture.route) {
                            popUpTo(Routes.QuickAuth.route) { inclusive = true }
                        }
                    },
                    onSkip = {
                        viewModel.markOnboardingComplete()
                        navController.navigate(Routes.Main.route) {
                            popUpTo(Routes.QuickAuth.route) { inclusive = true }
                        }
                    },
                    analyticsService = analyticsService
                )
            }

            composable(Routes.ProfilePicture.route) {
                ProfilePictureScreen(
                    onContinue = {
                        navController.navigate(Routes.WriteFirstNote.create()) {
                            popUpTo(Routes.ProfilePicture.route) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(Routes.WriteFirstNote.create()) {
                            popUpTo(Routes.ProfilePicture.route) { inclusive = true }
                        }
                    },
                    analyticsService = analyticsService
                )
            }

            composable(
                route = Routes.WriteFirstNote.route,
                arguments = listOf(navArgument("next") {
                    type = NavType.StringType
                    defaultValue = "quotes_preview"
                })
            ) { backStackEntry ->
                val nextRoute = backStackEntry.arguments?.getString("next") ?: "quotes_preview"
                WriteFirstNoteScreen(
                    onContinue = {
                        val destination = if (nextRoute == "onboarding_goals") {
                            Routes.OnboardingGoals.route
                        } else {
                            Routes.QuotesPreview.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Routes.WriteFirstNote.route) { inclusive = true }
                        }
                    },
                    analyticsService = analyticsService
                )
            }

            composable(Routes.QuotesPreview.route) {
                val quotesPreviewVM = hiltViewModel<QuotesPreviewViewModel>()
                QuotesPreviewScreen(
                    onContinue = {
                        navController.navigate(Routes.NotificationPermission.route) {
                            popUpTo(Routes.QuotesPreview.route) { inclusive = true }
                        }
                    },
                    analyticsService = analyticsService,
                    quotesRepository = quotesPreviewVM.quotesRepository,
                    userProfileRepository = quotesPreviewVM.userProfileRepository
                )
            }

            // ── Anonymous Notes (standalone routes) ──

            composable(Routes.ComposeNote.route) {
                ComposeNoteScreen(
                    onBack = { navController.popBackStack() },
                    onNoteSent = { navController.popBackStack() }
                )
            }

            composable(Routes.NoteInbox.route) {
                NoteInboxScreen(
                    onBack = { navController.popBackStack() },
                    onNoteClick = { noteId ->
                        navController.navigate(Routes.EnvelopeReveal.createRoute(noteId))
                    },
                    onComposeNote = {
                        navController.navigate(Routes.ComposeNote.route)
                    }
                )
            }

            composable(
                route = Routes.EnvelopeReveal.route,
                arguments = listOf(navArgument("noteId") { type = NavType.StringType })
            ) {
                EnvelopeRevealScreen(
                    onBack = { navController.popBackStack() },
                    onSendOneBack = {
                        navController.navigate(Routes.ComposeNote.route)
                    }
                )
            }

            // ── Quote Detail ──

            composable(
                route = Routes.QuoteDetail.route,
                arguments = listOf(navArgument("quoteId") { type = NavType.StringType })
            ) {
                QuoteDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // ── Persistent Bottom Nav (Instagram-style) ──
        // Shown on all screens EXCEPT onboarding
        AnimatedVisibility(
            visible = showBottomNav,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            DiaryBottomNav(
                selectedIndex = selectedTabIndex,
                onTabSelected = { index ->
                    selectedTabIndex = index
                    // If we're on a sub-screen, pop back to Main first
                    if (currentRoute != Routes.Main.route) {
                        navController.popBackStack(Routes.Main.route, inclusive = false)
                    }
                    // Set the tab via savedStateHandle
                    navController.currentBackStackEntry
                        ?.savedStateHandle?.set("navigateToTab", index)
                },
                activityBadgeCount = activityBadgeCount,
                notesBadgeCount = unreadNoteCount
            )
        }
    }

    // Paywall dialog for edit gate
    if (showPaywall) {
        PaywallDialog(
            onDismiss = { showPaywall = false },
            monthlyPrice = billingViewModel.getMonthlyPrice()?.let { "$it/month" } ?: "\$5/month",
            annualPrice = billingViewModel.getAnnualPrice()?.let { "$it/year" } ?: "\$40/year",
            onSelectPlan = { sku ->
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
