package com.proactivediary.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrackChanges
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.proactivediary.ui.goals.GoalsScreen
import com.proactivediary.ui.journal.JournalScreen
import com.proactivediary.ui.paywall.BillingViewModel
import com.proactivediary.ui.paywall.PaywallDialog
import com.proactivediary.ui.settings.SettingsScreen
import com.proactivediary.ui.write.WriteScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSize: Int = 23
)

private const val WRITE_TAB = "write_tab"

val bottomNavItems = listOf(
    BottomNavItem(WRITE_TAB, "Write", Icons.Outlined.Edit, iconSize = 24),
    BottomNavItem(Routes.Journal.route, "Journal", Icons.Outlined.MenuBook),
    BottomNavItem(Routes.Goals.route, "Goals", Icons.Outlined.TrackChanges),
    BottomNavItem(Routes.Settings.route, "Settings", Icons.Outlined.Settings),
)

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    deepLinkDestination: String? = null,
    billingViewModel: BillingViewModel = hiltViewModel()
) {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val subscriptionState by billingViewModel.subscriptionState.collectAsState()
    var showPaywall by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    // Handle deep link from notification (with paywall gate)
    LaunchedEffect(deepLinkDestination) {
        deepLinkDestination?.let { dest ->
            when (dest) {
                "write" -> {
                    if (subscriptionState.isActive) {
                        innerNavController.navigate(WRITE_TAB) {
                            popUpTo(innerNavController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        showPaywall = true
                    }
                }
                "goals" -> innerNavController.navigate(Routes.Goals.route) {
                    popUpTo(innerNavController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            Column {
                // 1px top border
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
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    // Paywall gate: block Write tab when trial expired
                                    if (item.route == WRITE_TAB && !subscriptionState.isActive) {
                                        showPaywall = true
                                        return@NavigationBarItem
                                    }
                                    innerNavController.navigate(item.route) {
                                        popUpTo(innerNavController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(item.iconSize.dp)
                                )
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
        NavHost(
            navController = innerNavController,
            startDestination = WRITE_TAB,
            modifier = Modifier.padding(padding)
        ) {
            composable(WRITE_TAB) {
                WriteScreen()
            }
            composable(Routes.Journal.route) {
                JournalScreen(
                    onEntryClick = { entryId ->
                        rootNavController.navigate(Routes.EntryDetail.createRoute(entryId))
                    }
                )
            }
            composable(Routes.Goals.route) {
                GoalsScreen()
            }
            composable(Routes.Settings.route) {
                SettingsScreen(
                    onOpenDesignStudio = {
                        rootNavController.navigate(Routes.DesignStudio.createRoute(edit = true))
                    },
                    onNavigateToGoals = {
                        innerNavController.navigate(Routes.Goals.route) {
                            popUpTo(innerNavController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToTypewriter = {
                        rootNavController.navigate(Routes.Typewriter.route) {
                            popUpTo(Routes.Main.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    // Paywall dialog
    if (showPaywall) {
        PaywallDialog(
            onDismiss = { showPaywall = false },
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
