package com.proactivediary.ui.paywall

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.proactivediary.analytics.AnalyticsService
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SubscriptionState(
    val isActive: Boolean,
    val plan: Plan,
    val trialDaysLeft: Int,
    val entryCount: Int = 0,
    val totalWords: Int = 0
)

enum class Plan { TRIAL, MONTHLY, ANNUAL, LIFETIME, EXPIRED }

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val analyticsService: AnalyticsService,
    private val entryRepository: EntryRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    val billingService = BillingService(context)

    private val _subscriptionState = MutableStateFlow(
        SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 0)
    )
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState

    private val _purchaseResult = MutableStateFlow<PurchaseResult>(PurchaseResult.Idle)
    val purchaseResult: StateFlow<PurchaseResult> = _purchaseResult

    companion object {
        /** Show paywall after this many entries (engagement-gated, not time-gated) */
        const val ENTRY_GATE_THRESHOLD = 7
    }

    init {
        billingService.initialize()
        viewModelScope.launch {
            billingService.purchaseState.collect { result ->
                _purchaseResult.value = result
                if (result is PurchaseResult.Success) {
                    val planType = when {
                        billingService.isLifetime() -> "lifetime"
                        billingService.isAnnual() -> "annual"
                        else -> "monthly"
                    }
                    analyticsService.logSubscriptionStarted(planType)
                    refreshSubscriptionState()
                }
            }
        }
        viewModelScope.launch {
            refreshSubscriptionState()
        }
    }

    suspend fun refreshSubscriptionState() {
        val state = withContext(Dispatchers.IO) { getSubscriptionState() }
        _subscriptionState.value = state
    }

    private fun getSubscriptionState(): SubscriptionState {
        // First check if user has an active paid subscription
        if (billingService.hasActiveSubscription()) {
            val plan = when {
                billingService.isLifetime() -> Plan.LIFETIME
                billingService.isAnnual() -> Plan.ANNUAL
                else -> Plan.MONTHLY
            }
            return SubscriptionState(isActive = true, plan = plan, trialDaysLeft = 0)
        }

        // Entry-count-gated trial: free until they hit ENTRY_GATE_THRESHOLD entries
        val entryCount = entryRepository.getTotalEntryCountSync()
        val totalWords = entryRepository.getTotalWordCountSync()

        if (entryCount < ENTRY_GATE_THRESHOLD) {
            val entriesLeft = ENTRY_GATE_THRESHOLD - entryCount
            return SubscriptionState(
                isActive = true,
                plan = Plan.TRIAL,
                trialDaysLeft = entriesLeft, // Repurposed: entries left, not days
                entryCount = entryCount,
                totalWords = totalWords
            )
        }

        // User has hit the gate — expired
        return SubscriptionState(
            isActive = false,
            plan = Plan.EXPIRED,
            trialDaysLeft = 0,
            entryCount = entryCount,
            totalWords = totalWords
        )
    }

    fun canWrite(): Boolean = _subscriptionState.value.isActive

    fun launchPurchase(activity: Activity, sku: String) {
        billingService.launchPurchaseFlow(activity, sku)
    }

    fun restorePurchases(onResult: (Boolean) -> Unit = {}) {
        billingService.restorePurchases { restored ->
            viewModelScope.launch {
                refreshSubscriptionState()
                onResult(restored)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingService.destroy()
    }
}
