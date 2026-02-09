package com.proactivediary.ui.paywall

import org.junit.Assert.*
import org.junit.Test

class BillingServiceTest {

    // ─── SKU constants ───

    @Test
    fun `monthly SKU is pd_monthly`() {
        assertEquals("pd_monthly", BillingService.MONTHLY_SKU)
    }

    @Test
    fun `annual SKU is pd_annual`() {
        assertEquals("pd_annual", BillingService.ANNUAL_SKU)
    }

    // ─── PurchaseResult sealed class ───

    @Test
    fun `PurchaseResult Idle is singleton`() {
        assertSame(PurchaseResult.Idle, PurchaseResult.Idle)
    }

    @Test
    fun `PurchaseResult Success is singleton`() {
        assertSame(PurchaseResult.Success, PurchaseResult.Success)
    }

    @Test
    fun `PurchaseResult Pending is singleton`() {
        assertSame(PurchaseResult.Pending, PurchaseResult.Pending)
    }

    @Test
    fun `PurchaseResult Cancelled is singleton`() {
        assertSame(PurchaseResult.Cancelled, PurchaseResult.Cancelled)
    }

    @Test
    fun `PurchaseResult Error contains message`() {
        val error = PurchaseResult.Error("Network error")
        assertEquals("Network error", error.message)
    }

    @Test
    fun `PurchaseResult Error equality`() {
        val e1 = PurchaseResult.Error("fail")
        val e2 = PurchaseResult.Error("fail")
        assertEquals(e1, e2)
    }

    @Test
    fun `all PurchaseResult types are distinct`() {
        val results: List<PurchaseResult> = listOf(
            PurchaseResult.Idle,
            PurchaseResult.Success,
            PurchaseResult.Pending,
            PurchaseResult.Cancelled,
            PurchaseResult.Error("test")
        )
        assertEquals(5, results.toSet().size)
    }
}
