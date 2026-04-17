package com.errymaricha.dafydiobooth.domain.usecase

fun calculateFinalAmount(
    photoboothPrice: Double,
    additionalPrintPrice: Double,
    additionalPrintCount: Int,
): Double = photoboothPrice + (additionalPrintPrice * additionalPrintCount.coerceAtLeast(0))
