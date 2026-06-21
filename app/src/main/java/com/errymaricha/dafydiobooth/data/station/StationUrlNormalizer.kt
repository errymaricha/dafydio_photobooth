package com.errymaricha.dafydiobooth.data.station

internal fun String.toStationBaseUrl(): String {
    val value = trim()
    if (value.isBlank()) return ""
    val withScheme = if (value.startsWith("http://") || value.startsWith("https://")) {
        value
    } else {
        "http://$value"
    }
    val withoutSlash = withScheme.trimEnd('/')
    val schemeEnd = withoutSlash.indexOf("://")
    val hostAndMaybePort = if (schemeEnd >= 0) {
        withoutSlash.substring(schemeEnd + 3)
    } else {
        withoutSlash
    }
    val hasPort = hostAndMaybePort.substringBefore('/').contains(":")
    val withDefaultPort = if (hasPort) withoutSlash else "$withoutSlash:8000"
    return "$withDefaultPort/"
}
