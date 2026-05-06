package com.errymaricha.dafydiobooth.ui.booth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DisconnectStationReducerTest {

    @Test
    fun `buildDisconnectedState clears station session related fields`() {
        val initial = BoothUiState(
            step = BoothStep.Camera,
            authToken = "secret-token",
            isStationConnected = true,
            eventStatusMessage = "connected",
            errorMessage = "old error",
            uploadedSessionPhotosBySlot = mapOf(1 to "photo-1"),
        )

        val result = buildDisconnectedState(initial)

        assertEquals(BoothStep.Settings, result.step)
        assertEquals("", result.authToken)
        assertFalse(result.isStationConnected)
        assertEquals("Disconnected dari Photobooth Station.", result.eventStatusMessage)
        assertNull(result.errorMessage)
        assertTrue(result.uploadedSessionPhotosBySlot.isEmpty())
        assertNull(result.session)
        assertNull(result.voucher)
        assertNull(result.quote)
        assertNull(result.paymentStatus)
    }
}
