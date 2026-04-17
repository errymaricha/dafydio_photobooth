package com.errymaricha.dafydiobooth.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class PricingCalculatorTest {
    @Test
    fun `final amount adds additional print count`() {
        val total = calculateFinalAmount(
            photoboothPrice = 50000.0,
            additionalPrintPrice = 10000.0,
            additionalPrintCount = 2,
        )

        assertEquals(70000.0, total, 0.0)
    }

    @Test
    fun `negative additional print count is treated as zero`() {
        val total = calculateFinalAmount(
            photoboothPrice = 50000.0,
            additionalPrintPrice = 10000.0,
            additionalPrintCount = -2,
        )

        assertEquals(50000.0, total, 0.0)
    }
}
