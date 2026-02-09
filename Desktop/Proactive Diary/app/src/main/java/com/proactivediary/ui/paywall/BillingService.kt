package com.proactivediary.ui.paywall

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillingService(private val context: Context) : PurchasesUpdatedListener {

    companion object {
        const val MONTHLY_SKU = "pd_monthly"
        const val ANNUAL_SKU = "pd_annual"
        private const val MAX_RETRY_COUNT = 3
    }

    private lateinit var billingClient: BillingClient

    private val _purchaseState = MutableStateFlow<PurchaseResult>(PurchaseResult.Idle)
    val purchaseState: StateFlow<PurchaseResult> = _purchaseState

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private var monthlyDetails: ProductDetails? = null
    private var annualDetails: ProductDetails? = null

    private var activePurchase: Purchase? = null
    private var activeProductId: String? = null

    private var retryCount = 0

    /** Callback invoked after queryExistingPurchases completes (or fails). */
    var onPurchasesQueried: (() -> Unit)? = null

    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .build()
            )
            .build()

        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isConnected.value = true
                    retryCount = 0
                    queryProductDetails()
                    queryExistingPurchases()
                } else {
                    _isConnected.value = false
                    onPurchasesQueried?.invoke()
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
                // Auto-reconnect with bounded retries
                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++
                    startConnection()
                }
            }
        })
    }

    /** Re-establish connection if disconnected (e.g. called before a user action). */
    fun ensureConnected() {
        if (::billingClient.isInitialized && !billingClient.isReady) {
            retryCount = 0
            startConnection()
        }
    }

    private fun queryProductDetails() {
        // Query subscription products (monthly + annual)
        val subsList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MONTHLY_SKU)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(ANNUAL_SKU)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subsList)
            .build()

        billingClient.queryProductDetailsAsync(subsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach { details ->
                    when (details.productId) {
                        MONTHLY_SKU -> monthlyDetails = details
                        ANNUAL_SKU -> annualDetails = details
                    }
                }
            }
        }
    }

    private fun queryExistingPurchases() {
        // Check subscription purchases
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(subsParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val active = purchases.firstOrNull { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (active != null) {
                    activePurchase = active
                    activeProductId = active.products.firstOrNull()
                    acknowledgePurchaseIfNeeded(active)
                }
            }
            // Notify ViewModel that purchase query is done (so it can cache)
            onPurchasesQueried?.invoke()
        }
    }

    private fun acknowledgePurchaseIfNeeded(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { /* acknowledged */ }
        }
    }

    fun launchPurchaseFlow(activity: Activity, sku: String) {
        // Ensure connected before launching
        if (!billingClient.isReady) {
            ensureConnected()
            return
        }

        val details = when (sku) {
            MONTHLY_SKU -> monthlyDetails
            ANNUAL_SKU -> annualDetails
            else -> null
        } ?: return

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    fun getMonthlyPrice(): String? {
        return monthlyDetails?.subscriptionOfferDetails
            ?.firstOrNull()?.pricingPhases?.pricingPhaseList
            ?.firstOrNull()?.formattedPrice
    }

    fun getAnnualPrice(): String? {
        return annualDetails?.subscriptionOfferDetails
            ?.firstOrNull()?.pricingPhases?.pricingPhaseList
            ?.firstOrNull()?.formattedPrice
    }

    fun hasActiveSubscription(): Boolean {
        return activePurchase?.purchaseState == Purchase.PurchaseState.PURCHASED
    }

    fun isAnnual(): Boolean {
        return activeProductId == ANNUAL_SKU
    }

    fun restorePurchases(onResult: (Boolean) -> Unit = {}) {
        if (!billingClient.isReady) {
            onResult(false)
            return
        }

        var foundActive = false

        // Check subscriptions
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(subsParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val active = purchases.firstOrNull { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (active != null) {
                    activePurchase = active
                    activeProductId = active.products.firstOrNull()
                    foundActive = true
                }
            }
            onResult(foundActive)
        }
    }

    fun hasPendingPurchase(): Boolean {
        return activePurchase?.purchaseState == Purchase.PurchaseState.PENDING
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.firstOrNull()?.let { purchase ->
                    when (purchase.purchaseState) {
                        Purchase.PurchaseState.PURCHASED -> {
                            activePurchase = purchase
                            activeProductId = purchase.products.firstOrNull()
                            acknowledgePurchaseIfNeeded(purchase)
                            _purchaseState.value = PurchaseResult.Success
                        }
                        Purchase.PurchaseState.PENDING -> {
                            activePurchase = purchase
                            activeProductId = purchase.products.firstOrNull()
                            _purchaseState.value = PurchaseResult.Pending
                        }
                        else -> { }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseResult.Cancelled
            }
            else -> {
                _purchaseState.value = PurchaseResult.Error(
                    billingResult.debugMessage ?: "Purchase failed"
                )
            }
        }
    }

    fun destroy() {
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}

sealed class PurchaseResult {
    data object Idle : PurchaseResult()
    data object Success : PurchaseResult()
    data object Pending : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}
