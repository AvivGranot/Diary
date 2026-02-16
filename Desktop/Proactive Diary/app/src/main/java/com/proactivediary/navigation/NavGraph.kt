package com.proactivediary.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.proactivediary.data.social.QuotesRepository
import com.proactivediary.data.social.UserProfileRepository
import com.proactivediary.ui.designstudio.DesignStudioScreen
import com.proactivediary.ui.journal.EntryDetailScreen
import com.proactivediary.ui.notes.ComposeNoteScreen
import com.proactivediary.ui.notes.EnvelopeRevealScreen
import com.proactivediary.ui.notes.NoteInboxScreen
import com.proactivediary.ui.onboarding.NotificationPermissionScreen
import com.proactivediary.ui.onboarding.OnboardingGoalsScreen
import com.proactivediary.ui.onboarding.QuickAuthScreen
import com.proactivediary.ui.onboarding.QuotesPreviewScreen
import com.proactivediary.ui.onboarding.QuotesPreviewViewModel
import com.proactivediary.ui.onboarding.SocialSplashScreen
import com.proactivediary.ui.onboarding.WriteFirstNoteScreen
import com.proactivediary.ui.paywall.BillingViewModel
import com.proactivediary.ui.paywall.PaywallDialog
import com.proactivediary.ui.quotes.QuoteDetailScreen
import com.proactivediary.ui.typewriter.TypewriterScreen
import com.proactivediary.ui.export.YearInReviewScreen
import com.proactivediary.ui.onthisday.OnThisDayScreen
import com.proactivediary.ui.settings.ContactSupportScreen
import com.proactivediary.ui.wrapped.DiaryWrappedScreen
import com.proactivediary.ui.insights.ThemeEvolutionScreen
import com.proactivediary.ui.write.WriteScreen

@Composable
fun ProactiveDiaryNavHost(
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

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Routes.Typewriter.route) {
            TypewriterScreen(
                onNavigateToDesignStudio = {
                    navController.navigate(Routes.DesignStudio.createRoute(edit = false)) {
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

        composable(
            route = Routes.DesignStudio.route,
            arguments = listOf(
                navArgument("edit") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isEdit = backStackEntry.arguments?.getBoolean("edit") ?: false
            DesignStudioScreen(
                onNavigateToGoals = {
                    if (isEdit) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Routes.OnboardingGoals.route) {
                            popUpTo(Routes.DesignStudio.route) { inclusive = true }
                        }
                    }
                },
                isEditMode = isEdit
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
                    viewModel.markOnboardingComplete()
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.NotificationPermission.route) { inclusive = true }
                    }
                },
                onSkip = {
                    viewModel.markOnboardingComplete()
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.NotificationPermission.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Main.route) {
            MainScreen(
                rootNavController = navController,
                deepLinkDestination = deepLinkDestination,
                deepLinkPrompt = deepLinkPrompt,
                deepLinkGoalId = deepLinkGoalId,
                onDeepLinkConsumed = onDeepLinkConsumed,
                billingViewModel = billingViewModel
            )
        }

        // Standalone Journal screen (accessed from Settings)
        composable(Routes.Journal.route) {
            com.proactivediary.ui.journal.JournalScreen(
                onEntryClick = { entryId ->
                    navController.navigate(Routes.EntryDetail.createRoute(entryId))
                },
                onNavigateToWrite = {
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
                    monthlyPrice = billingViewModel.getMonthlyPrice()?.let { "$it/month" } ?: "$5/month",
                    annualPrice = billingViewModel.getAnnualPrice()?.let { "$it/year" } ?: "$30/year",
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

        composable(Routes.DiaryWrapped.route) {
            DiaryWrappedScreen(
                onBack = { navController.popBackStack() }
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

        // ── New Social Onboarding ──

        composable(Routes.SocialSplash.route) {
            SocialSplashScreen(
                onContinue = {
                    navController.navigate(Routes.QuickAuth.route) {
                        popUpTo(Routes.SocialSplash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.QuickAuth.route) {
            QuickAuthScreen(
                onAuthenticated = {
                    navController.navigate(Routes.WriteFirstNote.route) {
                        popUpTo(Routes.QuickAuth.route) { inclusive = true }
                    }
                },
                onSkip = {
                    viewModel.markOnboardingComplete()
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.QuickAuth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.WriteFirstNote.route) {
            WriteFirstNoteScreen(
                onContinue = {
                    navController.navigate(Routes.QuotesPreview.route) {
                        popUpTo(Routes.WriteFirstNote.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.QuotesPreview.route) {
            // QuotesPreviewScreen needs repos passed directly since it's not a ViewModel-based screen
            // We'll use hiltViewModel in QuotesPreviewScreen instead
            QuotesPreviewScreen(
                onContinue = {
                    navController.navigate(Routes.NotificationPermission.route) {
                        popUpTo(Routes.QuotesPreview.route) { inclusive = true }
                    }
                },
                quotesRepository = hiltViewModel<QuotesPreviewViewModel>().quotesRepository,
                userProfileRepository = hiltViewModel<QuotesPreviewViewModel>().userProfileRepository
            )
        }

        // ── Anonymous Notes ──

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
                onBack = { navController.popBackStack() }
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

    // Paywall dialog for edit gate
    if (showPaywall) {
        PaywallDialog(
            onDismiss = { showPaywall = false },
            monthlyPrice = billingViewModel.getMonthlyPrice()?.let { "$it/month" } ?: "$5/month",
            annualPrice = billingViewModel.getAnnualPrice()?.let { "$it/year" } ?: "$30/year",
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
