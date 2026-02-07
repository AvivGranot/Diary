package com.proactivediary.ui.paywall

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SubscriptionState(
    val isActive: Boolean,
    val plan: Plan,
    val trialDaysLeft: Int
)

enum class Plan { TRIAL, MONTHLY, ANNUAL, EXPIRED }

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao,
    @ApplicationContext context: Context
) : ViewModel() {

    val billingService = BillingService(context)

    private val _subscriptionState = MutableStateFlow(
        SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 7)
    )
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState

    private val _purchaseResult = MutableStateFlow<PurchaseResult>(PurchaseResult.Idle)
    val purchaseResult: StateFlow<PurchaseResult> = _purchaseResult

    init {
        billingService.initialize()
        viewModelScope.launch {
            billingService.purchaseState.collect { result ->
                _purchaseResult.value = result
                if (result is PurchaseResult.Success) {
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
        val trialStart = preferenceDao.getSync("trial_start_date")?.value?.toLongOrNull()

        if (trialStart == null) {
            val now = System.currentTimeMillis()
            preferenceDao.insertSync(PreferenceEntity("trial_start_date", now.toString()))
            return SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 7)
        }

        val daysSinceStart = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - trialStart
        ).toInt()
        val trialDaysLeft = (7 - daysSinceStart).coerceAtLeast(0)

        if (trialDaysLeft > 0) {
            return SubscriptionState(true, Plan.TRIAL, trialDaysLeft)
        }

        if (billingService.hasActiveSubscription()) {
            val plan = if (billingService.isAnnual()) Plan.ANNUAL else Plan.MONTHLY
            return SubscriptionState(true, plan, 0)
        }

        return SubscriptionState(false, Plan.EXPIRED, 0)
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
