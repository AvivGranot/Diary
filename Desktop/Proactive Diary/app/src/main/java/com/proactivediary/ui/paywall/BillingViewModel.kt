package com.proactivediary.ui.paywall

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.proactivediary.analytics.AnalyticsService
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
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

enum class Plan { TRIAL, MONTHLY, ANNUAL, EXPIRED }

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val analyticsService: AnalyticsService,
    private val entryRepository: EntryRepository,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext context: Context
) : ViewModel() {

    val billingService = BillingService(context)

    private val loggedTrialMilestones = mutableSetOf<Int>()

    private val _subscriptionState = MutableStateFlow(
        SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 0)
    )
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState

    private val _purchaseResult = MutableStateFlow<PurchaseResult>(PurchaseResult.Idle)
    val purchaseResult: StateFlow<PurchaseResult> = _purchaseResult

    private val _isFirstPaywallView = MutableStateFlow(true)
    val isFirstPaywallView: StateFlow<Boolean> = _isFirstPaywallView

    companion object {
        /** Show paywall after this many entries — disabled until 1K DAU (set to effectively infinite) */
        const val ENTRY_GATE_THRESHOLD = 999_999

        // Preference keys for offline cache
        private const val KEY_CACHED_PLAN = "billing_cached_plan"
        private const val KEY_CACHED_ACTIVE = "billing_cached_active"
    }

    init {
        // Load cached state first (instant, works offline)
        viewModelScope.launch {
            loadCachedState()
            // Check if user has seen the paywall before
            val seen = withContext(Dispatchers.IO) {
                preferenceDao.getSync("paywall_viewed")?.value == "true"
            }
            _isFirstPaywallView.value = !seen
        }

        // Set callback so BillingService notifies us after querying purchases
        billingService.onPurchasesQueried = {
            viewModelScope.launch {
                refreshSubscriptionState()
            }
        }

        billingService.initialize()

        viewModelScope.launch {
            billingService.purchaseState.collect { result ->
                _purchaseResult.value = result
                if (result is PurchaseResult.Success) {
                    val planType = when {
                        billingService.isAnnual() -> "annual"
                        else -> "monthly"
                    }
                    analyticsService.logSubscriptionStarted(planType)
                    refreshSubscriptionState()
                }
            }
        }
    }

    /** Load cached subscription state from Room (offline-safe). */
    private suspend fun loadCachedState() {
        withContext(Dispatchers.IO) {
            val cachedPlan = preferenceDao.getSync(KEY_CACHED_PLAN)?.value
            val cachedActive = preferenceDao.getSync(KEY_CACHED_ACTIVE)?.value

            if (cachedPlan != null && cachedActive != null) {
                val plan = try { Plan.valueOf(cachedPlan) } catch (_: Exception) { Plan.TRIAL }
                val isActive = cachedActive == "true"
                val entryCount = entryRepository.getTotalEntryCountSync()
                val totalWords = entryRepository.getTotalWordCountSync()
                val entriesLeft = if (plan == Plan.TRIAL) (ENTRY_GATE_THRESHOLD - entryCount).coerceAtLeast(0) else 0

                _subscriptionState.value = SubscriptionState(
                    isActive = isActive,
                    plan = plan,
                    trialDaysLeft = entriesLeft,
                    entryCount = entryCount,
                    totalWords = totalWords
                )
            } else {
                // No cache yet — compute from entry count (works offline, no billing needed)
                val state = getSubscriptionState()
                _subscriptionState.value = state
            }
        }
    }

    /** Cache the current subscription state to Room for offline resilience. */
    private suspend fun cacheState(state: SubscriptionState) {
        withContext(Dispatchers.IO) {
            preferenceDao.insertSync(PreferenceEntity(KEY_CACHED_PLAN, state.plan.name))
            preferenceDao.insertSync(PreferenceEntity(KEY_CACHED_ACTIVE, state.isActive.toString()))
        }
    }

    suspend fun refreshSubscriptionState() {
        val state = withContext(Dispatchers.IO) { getSubscriptionState() }
        _subscriptionState.value = state
        cacheState(state)
    }

    private fun getSubscriptionState(): SubscriptionState {
        // First check if user has an active paid subscription
        if (billingService.hasActiveSubscription()) {
            val plan = when {
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
            // Log trial milestones (deduplicated)
            if (entryCount in listOf(5, 10, 15, 20, 25) && entryCount !in loggedTrialMilestones) {
                loggedTrialMilestones.add(entryCount)
                analyticsService.logTrialMilestone(entryCount, entriesLeft)
            }
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

    fun markPaywallViewed() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferenceDao.insertSync(PreferenceEntity("paywall_viewed", "true"))
            }
            _isFirstPaywallView.value = false
        }
    }

    /** Real price from Play Console, or null if not yet loaded. */
    fun getMonthlyPrice(): String? = billingService.getMonthlyPrice()
    fun getAnnualPrice(): String? = billingService.getAnnualPrice()

    fun consumePurchaseResult() {
        _purchaseResult.value = PurchaseResult.Idle
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
