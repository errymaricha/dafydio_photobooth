package com.errymaricha.dafydiobooth.ui.launch

import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchUiStateTest {
    @Test
    fun `manual payment button is disabled while request is loading`() {
        val state = LaunchUiState(loading = true)

        assertFalse(state.canSubmitManualPayment)
    }

    @Test
    fun `manual payment button is disabled while session waits for station approval`() {
        val state = LaunchUiState(
            session = pendingSession(),
            approvalStatus = "pending",
        )

        assertTrue(state.isManualPaymentWaiting)
        assertFalse(state.canSubmitManualPayment)
    }

    @Test
    fun `manual payment button is enabled again after station rejects request`() {
        val state = LaunchUiState(
            session = pendingSession(),
            approvalStatus = "rejected",
            message = "Manual payment ditolak: bukti transfer tidak valid",
        )

        assertTrue(state.isManualPaymentRejected)
        assertFalse(state.isManualPaymentWaiting)
        assertTrue(state.canSubmitManualPayment)
    }

    @Test
    fun `manual payment button is enabled again for backend status containing reject keyword`() {
        val state = LaunchUiState(
            session = pendingSession(),
            approvalStatus = "manual_payment_rejected",
            message = "Manual payment ditolak: saldo kurang",
        )

        assertTrue(state.isManualPaymentRejected)
        assertFalse(state.isManualPaymentWaiting)
        assertTrue(state.canSubmitManualPayment)
    }

    private fun pendingSession() = LaunchSession(
        sessionId = "session-1",
        sessionCode = "SES-001",
        paymentStatus = "pending",
        paymentRequired = true,
        unlockPhoto = false,
    )
}
