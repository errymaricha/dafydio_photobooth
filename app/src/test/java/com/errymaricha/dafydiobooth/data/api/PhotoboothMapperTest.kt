package com.errymaricha.dafydiobooth.data.api

import com.errymaricha.dafydiobooth.data.repository.toDomain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoboothMapperTest {
    @Test
    fun `voucher response maps exact backend fields to domain`() {
        val response = VerifyVoucherResponse(
            contractVersion = "2026-04-15",
            voucherCode = "VCH-001",
            voucherType = "regular",
            isValid = true,
            status = "valid",
            message = "OK",
            customerName = "Ery",
            remainingUses = 1,
        )

        val domain = response.toDomain()

        assertEquals("VCH-001", domain.code)
        assertEquals("regular", domain.type)
        assertTrue(domain.isValid)
        assertEquals("valid", domain.status)
        assertEquals("OK", domain.message)
        assertEquals("Ery", domain.customerName)
        assertEquals(1, domain.remainingUses)
    }

    @Test
    fun `quote response maps payment fields`() {
        val response = PaymentQuoteResponse(
            contractVersion = "2026-04-15",
            quoteId = "quote-1",
            amount = 35000,
            currency = "IDR",
            paymentRequired = true,
            paymentUrl = "https://pay.example/quote-1",
            expiresAt = "2026-04-15T12:00:00Z",
        )

        val domain = response.toDomain()

        assertEquals("quote-1", domain.quoteId)
        assertEquals(35000L, domain.amount)
        assertEquals("IDR", domain.currency)
        assertTrue(domain.paymentRequired)
        assertEquals("https://pay.example/quote-1", domain.paymentUrl)
    }

    @Test
    fun `nested staging quote maps total due and unlock fields`() {
        val response = PaymentQuoteResponse(
            contractVersion = "2026-04-15",
            voucherCode = "FREE-001",
            voucherType = "free",
            quote = PaymentQuotePayload(
                quoteId = "quote-free",
                subtotalAmount = 100000,
                discountAmount = 100000,
                totalDue = 0,
                currency = "IDR",
                paymentRequired = false,
                unlockPhoto = true,
                discountReason = null,
            ),
        )

        val domain = response.toDomain()

        assertEquals("quote-free", domain.quoteId)
        assertEquals(0L, domain.amount)
        assertEquals(100000L, domain.subtotalAmount)
        assertEquals(100000L, domain.discountAmount)
        assertEquals("IDR", domain.currency)
        assertTrue(domain.unlockPhoto)
    }

    @Test
    fun `manual session request serializes payment method`() {
        val request = OpenManualSessionRequest(
            customerWhatsapp = "6282118401998",
            paymentMethod = "manual",
            additionalPrintCount = 0,
        )

        val body = Json { explicitNulls = false }
            .encodeToString(OpenManualSessionRequest.serializer(), request)
        val json = Json.parseToJsonElement(body).jsonObject

        assertEquals("manual", json.getValue("payment_method").jsonPrimitive.content)
        assertEquals("6282118401998", json.getValue("customer_whatsapp").jsonPrimitive.content)
    }

    @Test
    fun `pending manual payment check does not unlock capture`() {
        val response = PaymentCheckResponse(
            contractVersion = "2026-04-17",
            sessionId = "session-pending",
            sessionCode = "SES-CQG7C6MV",
            paymentStatus = "pending",
            paymentRequired = true,
            paymentUnlocked = false,
            unlockPhoto = false,
        )

        val domain = response.toDomain()

        assertEquals("pending", domain.paymentStatus)
        assertEquals(true, domain.paymentRequired)
        assertEquals(false, domain.canUpload)
        assertEquals(false, domain.unlockPhoto)
    }
}
