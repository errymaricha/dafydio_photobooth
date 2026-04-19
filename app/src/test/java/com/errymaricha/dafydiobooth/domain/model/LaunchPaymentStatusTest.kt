package com.errymaricha.dafydiobooth.domain.model

import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchPaymentStatusTest {
    @Test
    fun `status containing reject keyword is treated as rejected`() {
        val status = LaunchPaymentStatus(
            sessionId = "session-1",
            sessionCode = "SES-001",
            paymentStatus = "manual_payment_rejected",
            canUpload = false,
            paymentRequired = true,
            unlockPhoto = false,
        )

        assertTrue(status.isRejected)
    }

    @Test
    fun `reviewed rejection reason is treated as rejected even when payment status is pending`() {
        val status = LaunchPaymentStatus(
            sessionId = "session-1",
            sessionCode = "SES-001",
            paymentStatus = "pending",
            canUpload = false,
            paymentRequired = true,
            unlockPhoto = false,
            rejectionReason = "Bukti transfer tidak valid",
            reviewedAt = "2026-04-19T14:16:25.000000Z",
        )

        assertTrue(status.isRejected)
    }

    @Test
    fun `review status rejected overrides pending payment status`() {
        val status = LaunchPaymentStatus(
            sessionId = "session-1",
            sessionCode = "SES-001",
            paymentStatus = "pending",
            reviewStatus = "rejected",
            canUpload = false,
            paymentRequired = true,
            unlockPhoto = false,
            rejectionReason = "hahahalin",
            reviewer = "Super Admin",
            reviewedAt = "2026-04-19T14:16:25.000000Z",
        )

        assertTrue(status.isRejected)
        assertTrue(status.displayStatus == "rejected")
    }
}
