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
        const val LIFETIME_SKU = "pd_lifetime"
    }

    private lateinit var billingClient: BillingClient

    private val _purchaseState = MutableStateFlow<PurchaseResult>(PurchaseResult.Idle)
    val purchaseState: StateFlow<PurchaseResult> = _purchaseState

    private var monthlyDetails: ProductDetails? = null
    private var annualDetails: ProductDetails? = null
    private var lifetimeDetails: ProductDetails? = null

    private var activePurchase: Purchase? = null
    private var activeProductId: String? = null

    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryExistingPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection on next user action
            }
        })
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

        // Query one-time product (lifetime)
        val inappList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(LIFETIME_SKU)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val inappParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inappList)
            .build()

        billingClient.queryProductDetailsAsync(inappParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach { details ->
                    if (details.productId == LIFETIME_SKU) {
                        lifetimeDetails = details
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
        }

        // Check one-time (lifetime) purchases
        val inappParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(inappParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val lifetime = purchases.firstOrNull { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.products.contains(LIFETIME_SKU)
                }
                if (lifetime != null) {
                    activePurchase = lifetime
                    activeProductId = LIFETIME_SKU
                    acknowledgePurchaseIfNeeded(lifetime)
                }
            }
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
        val details = when (sku) {
            MONTHLY_SKU -> monthlyDetails
            ANNUAL_SKU -> annualDetails
            LIFETIME_SKU -> lifetimeDetails
            else -> null
        } ?: return

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        // Subscriptions require an offer token; one-time purchases do not
        if (sku != LIFETIME_SKU) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        val productDetailsParamsList = listOf(productDetailsParamsBuilder.build())

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    fun hasActiveSubscription(): Boolean {
        return activePurchase?.purchaseState == Purchase.PurchaseState.PURCHASED
    }

    fun isAnnual(): Boolean {
        return activeProductId == ANNUAL_SKU
    }

    fun isLifetime(): Boolean {
        return activeProductId == LIFETIME_SKU
    }

    fun restorePurchases(onResult: (Boolean) -> Unit = {}) {
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

            // Check one-time (lifetime) purchases
            val inappParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient.queryPurchasesAsync(inappParams) { inappResult, inappPurchases ->
                if (inappResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val lifetime = inappPurchases.firstOrNull { purchase ->
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            purchase.products.contains(LIFETIME_SKU)
                    }
                    if (lifetime != null) {
                        activePurchase = lifetime
                        activeProductId = LIFETIME_SKU
                        foundActive = true
                    }
                }
                onResult(foundActive)
            }
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
