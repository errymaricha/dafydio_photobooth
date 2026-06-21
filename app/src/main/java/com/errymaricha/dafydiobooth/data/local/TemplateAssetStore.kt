package com.errymaricha.dafydiobooth.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import com.errymaricha.dafydiobooth.BuildConfig
import com.errymaricha.dafydiobooth.data.station.toStationBaseUrl
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TemplateAssetStore(private val context: Context) {
    companion object {
        // First full download + one hard reset redownload, lalu fail-fast.
        private const val MAX_DOWNLOAD_ATTEMPTS = 2
    }

    private data class ResolvedAssetRequest(
        val downloadUrl: String,
        val hostHeader: String? = null,
    )

    private data class DownloadResult(
        val success: Boolean,
        val code: Int,
        val message: String,
    )

    private data class AssetMetadata(
        val expectedLength: Long,
        val expectedSha: String?,
    )

    private data class ParsedContentRange(
        val start: Long,
        val end: Long,
        val total: Long,
    )

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .protocols(listOf(Protocol.HTTP_1_1))
        .build()
    private val assetLocks = ConcurrentHashMap<String, Any>()
    @Volatile
    private var diagnoseEndpointSupported: Boolean? = null

    suspend fun cacheOverlay(
        templateId: String,
        overlayUrl: String?,
        stationBaseUrl: String,
        authToken: String? = null,
        forceRefresh: Boolean = false,
    ): String? = withContext(Dispatchers.IO) {
        cacheAsset(
            templateId = templateId,
            assetUrl = overlayUrl,
            stationBaseUrl = stationBaseUrl,
            authToken = authToken,
            filePrefix = "overlay",
            forceRefresh = forceRefresh,
        )
    }

    suspend fun cacheThumbnail(
        templateId: String,
        thumbnailUrl: String?,
        stationBaseUrl: String,
        authToken: String? = null,
        forceRefresh: Boolean = false,
    ): String? = withContext(Dispatchers.IO) {
        cacheAsset(
            templateId = templateId,
            assetUrl = thumbnailUrl,
            stationBaseUrl = stationBaseUrl,
            authToken = authToken,
            filePrefix = "thumbnail",
            forceRefresh = forceRefresh,
        )
    }

    suspend fun cachePreview(
        templateId: String,
        previewUrl: String?,
        stationBaseUrl: String,
        authToken: String? = null,
        forceRefresh: Boolean = false,
    ): String? = withContext(Dispatchers.IO) {
        cacheAsset(
            templateId = templateId,
            assetUrl = previewUrl,
            stationBaseUrl = stationBaseUrl,
            authToken = authToken,
            filePrefix = "preview",
            forceRefresh = forceRefresh,
        )
    }

    fun getCachedOverlayPath(templateId: String): String? {
        return getCachedAssetPath(templateId, "overlay")
    }

    fun getCachedPreviewPath(templateId: String): String? {
        return getCachedAssetPath(templateId, "preview")
    }

    fun isLocalImageValid(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return isValidImageFile(File(path))
    }

    private fun cacheAsset(
        templateId: String,
        assetUrl: String?,
        stationBaseUrl: String,
        authToken: String?,
        filePrefix: String,
        forceRefresh: Boolean,
    ): String? {
        if (assetUrl.isNullOrBlank()) return null
        val lock = assetLocks.getOrPut("$templateId:$filePrefix") { Any() }
        return synchronized(lock) {
            val cachedPath = getCachedAssetPath(templateId, filePrefix)
            val cachedFile = cachedPath?.let(::File)?.takeIf(::isValidImageFile)
            if (!forceRefresh && cachedFile != null) return@synchronized cachedFile.absolutePath
            if (forceRefresh && cachedFile != null) {
                runCatching { cachedFile.delete() }
            }
            val fallbackCachedPath = cachedPath?.let(::File)?.takeIf(::isValidImageFile)?.absolutePath
            val resolvedRequest = resolveAssetRequest(assetUrl, stationBaseUrl) ?: return@synchronized null
            val resolvedUrl = resolvedRequest.downloadUrl
            val requestAuthToken = if (isSignedAssetUrl(resolvedUrl)) null else authToken
            val requestId = buildRequestId(templateId, filePrefix, 1, !requestAuthToken.isNullOrBlank())
            val extension = resolvedUrl.substringBefore('?').substringAfterLast('.', "png").lowercase()
            val safeExtension = if (extension.length in 2..6) extension else "png"
            val targetDir = File(context.filesDir, "templates/$templateId")
            if (!targetDir.exists()) targetDir.mkdirs()
            purgeWorkingFiles(targetDir, filePrefix)
            val targetFile = File(targetDir, "$filePrefix.$safeExtension")
            val tempFile = File(targetDir, "$filePrefix-$requestId.tmp")
            val result = downloadAssetWithResume(
                signedUrl = resolvedUrl,
                hostHeader = resolvedRequest.hostHeader,
                authToken = requestAuthToken,
                outFile = tempFile,
                requestId = requestId,
                filePrefix = filePrefix,
                maxAttempts = MAX_DOWNLOAD_ATTEMPTS,
            )

            if (!result.success || !isValidImageFile(tempFile)) {
                Log.e(
                    "TemplateAssetStore",
                    "Asset gagal disimpan [$filePrefix] template=$templateId code=${result.code} msg=${result.message} url=$resolvedUrl",
                )
                runCatching { if (tempFile.exists()) tempFile.delete() }
                return@synchronized fallbackCachedPath
            }

            val promoted = promoteTempFile(
                templateDir = targetDir,
                filePrefix = filePrefix,
                targetFile = targetFile,
                tempFile = tempFile,
            )
            if (promoted != null && isValidImageFile(promoted)) {
                Log.i(
                    "TemplateAssetStore",
                    "Asset final valid [$filePrefix] path=${promoted.absolutePath} size=${promoted.length()} hash=${computeShortHash(promoted)}",
                )
                promoted.absolutePath
            } else {
                Log.e(
                    "TemplateAssetStore",
                    "Asset final invalid setelah promote [$filePrefix] template=$templateId",
                )
                runCatching { if (promoted != null && promoted.exists()) promoted.delete() }
                fallbackCachedPath
            }
        }
    }

    private fun downloadAssetWithResume(
        signedUrl: String,
        hostHeader: String?,
        authToken: String?,
        outFile: File,
        requestId: String,
        filePrefix: String,
        maxAttempts: Int = 3,
    ): DownloadResult {
        outFile.parentFile?.mkdirs()
        if (isSignedAssetUrl(signedUrl)) {
            val rangeResult = downloadAssetWithChunkedRanges(
                resolvedUrl = signedUrl,
                hostHeader = hostHeader,
                authToken = authToken,
                outFile = outFile,
                requestId = requestId,
                filePrefix = filePrefix,
                maxAttempts = maxAttempts,
            )
            if (rangeResult.success && isValidImageFile(outFile)) {
                return rangeResult
            }
            Log.w(
                "TemplateAssetStore",
                "Range-only download failed for signed URL [$filePrefix] code=${rangeResult.code} msg=${rangeResult.message}. Falling back to full download.",
            )
        }
        var lastFailure = DownloadResult(false, -1, "Failed after retries")
        repeat(maxAttempts) { index ->
            val attempt = index + 1
            runCatching { if (outFile.exists()) outFile.delete() }
            val requestBuilder = Request.Builder()
                .url(signedUrl)
                .header("Accept", "image/png")
                .header("Accept-Encoding", "identity")
                .header("Connection", "close")
                .header("Cache-Control", "no-cache")
                .header("User-Agent", buildAssetUserAgent())
                .header("X-Debug-Client", "android-template-sync")
                .header("X-Debug-Req-Id", "$requestId-a$attempt")
            if (!hostHeader.isNullOrBlank()) requestBuilder.header("Host", hostHeader)
            if (!authToken.isNullOrBlank()) requestBuilder.header("Authorization", "Bearer ${authToken.trim()}")

            val result = runCatching {
                httpClient.newCall(requestBuilder.build()).execute().use { response ->
                    val code = response.code
                    if (code != 200) {
                        return@use DownloadResult(false, code, "HTTP $code")
                    }
                    logResponseDiagnostics(
                        filePrefix = filePrefix,
                        requestId = "$requestId-a$attempt",
                        responseCode = code,
                        resolvedUrl = signedUrl,
                        useAuth = !authToken.isNullOrBlank(),
                        attempt = attempt,
                        transport = "okhttp",
                        contentLength = response.header("Content-Length")?.toLongOrNull(),
                        assetId = response.header("X-Asset-Id"),
                        assetSize = response.header("X-Asset-Size")?.toLongOrNull(),
                        assetSha = response.header("X-Asset-Sha256"),
                        assetDelivery = response.header("X-Asset-Delivery"),
                        etag = response.header("ETag"),
                        lastModified = response.header("Last-Modified"),
                    )
                    val body = response.body ?: return@use DownloadResult(false, code, "Empty body")
                    val expectedSize = response.header("X-Asset-Size")?.toLongOrNull()
                    val expectedSha = response.header("X-Asset-Sha256")?.trim()?.lowercase()
                    val contentLength = response.header("Content-Length")?.toLongOrNull()
                    val bytes = body.bytes()
                    if (contentLength != null && bytes.size.toLong() != contentLength) {
                        return@use DownloadResult(
                            false,
                            code,
                            "Body truncated expected=$contentLength actual=${bytes.size}",
                        )
                    }
                    if (expectedSize != null && bytes.size.toLong() != expectedSize) {
                        return@use DownloadResult(
                            false,
                            code,
                            "Size mismatch expected=$expectedSize actual=${bytes.size}",
                        )
                    }
                    if (!isValidImageBytes(bytes)) {
                        return@use DownloadResult(false, code, "Invalid image bytes")
                    }
                    FileOutputStream(outFile, false).use { output ->
                        output.write(bytes)
                        output.flush()
                    }
                    val finalSize = outFile.length()
                    Log.i(
                        "TemplateAssetStore",
                        "File selesai ditulis [$filePrefix] requestId=$requestId expectedLength=${expectedSize ?: -1} actualLength=$finalSize hash=${computeShortHash(outFile)} url=$signedUrl attempt=$attempt/$maxAttempts via=okhttp",
                    )
                    if (expectedSize != null && finalSize != expectedSize) {
                        return@use DownloadResult(false, code, "Size mismatch expected=$expectedSize actual=$finalSize")
                    }
                    if (!expectedSha.isNullOrBlank()) {
                        val localSha = computeSha256(outFile).orEmpty()
                        if (!localSha.equals(expectedSha, ignoreCase = true)) {
                            Log.w(
                                "TemplateAssetStore",
                                "SHA mismatch [$filePrefix] requestId=$requestId expectedSha=$expectedSha actualSha=$localSha url=$signedUrl attempt=$attempt/$maxAttempts",
                            )
                            // Jangan resume dari file yang sudah korup.
                            runCatching { outFile.delete() }
                            return@use DownloadResult(false, code, "SHA mismatch expected=$expectedSha actual=$localSha")
                        }
                    }
                    DownloadResult(true, code, "OK")
                }
            }.getOrElse { error ->
                Log.w(
                    "TemplateAssetStore",
                    "Primary asset read gagal [$filePrefix] requestId=$requestId attempt=$attempt/$maxAttempts error=${error.message}. Coba fallback streaming.",
                )
                val streamedOk = downloadWithUrlConnectionToFile(
                    resolvedUrl = signedUrl,
                    hostHeader = hostHeader,
                    authToken = authToken,
                    target = outFile,
                    filePrefix = filePrefix,
                    requestId = "$requestId-fallback-$attempt",
                    attempt = attempt,
                    useAuth = !authToken.isNullOrBlank(),
                )
                if (streamedOk && isValidImageFile(outFile)) {
                    DownloadResult(true, 200, "OK via fallback")
                } else {
                    DownloadResult(false, -1, error.message ?: "Unknown error")
                }
            }

            if (result.success) return result
            lastFailure = result
            runCatching { if (outFile.exists()) outFile.delete() }
            if (attempt < maxAttempts) {
                runCatching { Thread.sleep((attempt * 250L)) }
            }
        }
        diagnoseAssetFailure(
            signedUrl = signedUrl,
            hostHeader = hostHeader,
            authToken = authToken,
            filePrefix = filePrefix,
            requestId = requestId,
        )
        return lastFailure
    }

    private fun downloadAssetWithChunkedRanges(
        resolvedUrl: String,
        hostHeader: String?,
        authToken: String?,
        outFile: File,
        requestId: String,
        filePrefix: String,
        maxAttempts: Int,
    ): DownloadResult {
        outFile.parentFile?.mkdirs()
        var lastFailure = DownloadResult(false, -1, "Failed after retries")
        repeat(maxAttempts) { index ->
            val attempt = index + 1
            runCatching { if (outFile.exists()) outFile.delete() }
            val metadata = fetchAssetMetadataByRange(
                resolvedUrl = resolvedUrl,
                hostHeader = hostHeader,
                authToken = authToken,
                filePrefix = filePrefix,
                requestId = requestId,
                attempt = attempt,
            ) ?: run {
                lastFailure = DownloadResult(false, -1, "Missing asset metadata from range response")
                return@repeat
            }
            val downloaded = recoverFullWithChunkedRanges(
                resolvedUrl = resolvedUrl,
                hostHeader = hostHeader,
                authToken = authToken,
                target = outFile,
                expectedLength = metadata.expectedLength,
                expectedSha = metadata.expectedSha.orEmpty(),
                filePrefix = filePrefix,
                requestId = requestId,
                useAuth = !authToken.isNullOrBlank(),
                attempt = attempt,
            )
            if (downloaded && isValidImageFile(outFile)) {
                return DownloadResult(true, 206, "OK via range-only")
            }
            lastFailure = DownloadResult(false, -1, "Range-only download failed")
            runCatching { if (outFile.exists()) outFile.delete() }
            if (attempt < maxAttempts) {
                runCatching { Thread.sleep((attempt * 250L)) }
            }
        }
        diagnoseAssetFailure(
            signedUrl = resolvedUrl,
            hostHeader = hostHeader,
            authToken = authToken,
            filePrefix = filePrefix,
            requestId = requestId,
        )
        return lastFailure
    }

    private fun isValidImageBytes(bytes: ByteArray): Boolean {
        if (bytes.isEmpty()) return false
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            bounds.outWidth > 0 && bounds.outHeight > 0
        }.getOrDefault(false)
    }

    private fun promoteTempFile(
        templateDir: File,
        filePrefix: String,
        targetFile: File,
        tempFile: File,
    ): File? {
        val backupFile = File(templateDir, "$filePrefix.bak")
        runCatching {
            if (backupFile.exists()) backupFile.delete()
            if (targetFile.exists() && !targetFile.renameTo(backupFile)) {
                backupFile.delete()
            }
            if (!tempFile.renameTo(targetFile)) {
                throw IOException("Gagal memindahkan temp file ke target")
            }
            if (backupFile.exists()) backupFile.delete()
            templateDir.listFiles()
                ?.filter { it.isFile && it.name.startsWith("$filePrefix.") && it.absolutePath != targetFile.absolutePath }
                ?.forEach { stale -> stale.delete() }
            targetFile
        }.onFailure { error ->
            Log.e(
                "TemplateAssetStore",
                "Promote asset gagal [$filePrefix] target=${targetFile.absolutePath} error=${error.message}",
            )
            runCatching { if (tempFile.exists()) tempFile.delete() }
            if (!targetFile.exists() && backupFile.exists()) {
                runCatching { backupFile.renameTo(targetFile) }
            }
        }
        return targetFile.takeIf(::isValidImageFile)
    }

    private fun downloadWithOkHttpToFile(
        resolvedUrl: String,
        hostHeader: String?,
        authToken: String?,
        target: File,
        filePrefix: String,
        requestId: String,
        attempt: Int,
        useAuth: Boolean,
    ): Boolean {
        return runCatching {
            val requestBuilder = Request.Builder()
                .url(resolvedUrl)
                .header("Accept", "image/png")
                .header("Accept-Encoding", "identity")
                .header("Connection", "keep-alive")
                .header("Cache-Control", "no-cache")
                .header("User-Agent", buildAssetUserAgent())
                .header("X-Debug-Client", "android-template-sync")
                .header("X-Debug-Req-Id", requestId)
            if (!hostHeader.isNullOrBlank()) {
                requestBuilder.header("Host", hostHeader)
            }
            if (!authToken.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer ${authToken.trim()}")
            }
            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(
                        "TemplateAssetStore",
                        "Asset download gagal [$filePrefix] requestId=$requestId http=${response.code} url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=okhttp",
                    )
                    return false
                }
                logResponseDiagnostics(
                    filePrefix = filePrefix,
                    requestId = requestId,
                    responseCode = response.code,
                    resolvedUrl = resolvedUrl,
                    useAuth = useAuth,
                    attempt = attempt,
                    transport = "okhttp",
                    contentLength = response.header("Content-Length")?.toLongOrNull(),
                    assetId = response.header("X-Asset-Id"),
                    assetSize = response.header("X-Asset-Size")?.toLongOrNull(),
                    assetSha = response.header("X-Asset-Sha256"),
                )
                val contentType = response.header("Content-Type").orEmpty().lowercase()
                if (!contentType.startsWith("image/")) {
                    Log.w(
                        "TemplateAssetStore",
                        "Bukan image [$filePrefix]! requestId=$requestId type=$contentType url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=okhttp",
                    )
                }
                val body = response.body
                val expectedLength = body.contentLength().takeIf { it >= 0L }
                val expectedSha = response.header("X-Asset-Sha256")?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
                var streamError: Throwable? = null
                FileOutputStream(target, false).use { output ->
                    try {
                        body.byteStream().use { input ->
                            val buffer = ByteArray(16 * 1024)
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                if (read == 0) continue
                                output.write(buffer, 0, read)
                            }
                        }
                    } catch (error: Throwable) {
                        streamError = error
                        Log.w(
                            "TemplateAssetStore",
                            "Stream terputus [$filePrefix] requestId=$requestId url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=okhttp error=${error.message}",
                        )
                    }
                    output.flush()
                }
                val actualLength = target.length()
                if (expectedLength != null && actualLength != expectedLength) {
                    Log.w(
                        "TemplateAssetStore",
                        "Panjang file mismatch [$filePrefix] requestId=$requestId expected=$expectedLength actual=$actualLength url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=okhttp",
                    )
                    val recovered = tryRecoverWithRange(
                        resolvedUrl = resolvedUrl,
                        hostHeader = hostHeader,
                        authToken = authToken,
                        target = target,
                        expectedLength = expectedLength,
                        expectedSha = expectedSha,
                        filePrefix = filePrefix,
                        requestId = requestId,
                        useAuth = useAuth,
                        attempt = attempt,
                    )
                    if (recovered) {
                        Log.i(
                            "TemplateAssetStore",
                            "Range recovery berhasil [$filePrefix] requestId=$requestId url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS",
                        )
                    }
                }
                val finalLength = target.length()
                Log.i(
                    "TemplateAssetStore",
                    "File selesai ditulis [$filePrefix] requestId=$requestId expectedLength=${expectedLength ?: -1} actualLength=$finalLength hash=${computeShortHash(target)} url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=okhttp",
                )
                if (expectedLength != null && finalLength != expectedLength) {
                    return false
                }
                if (!expectedSha.isNullOrBlank()) {
                    val localSha = computeSha256(target)
                    if (localSha.isNullOrBlank() || !localSha.equals(expectedSha, ignoreCase = true)) {
                        Log.w(
                            "TemplateAssetStore",
                            "SHA mismatch [$filePrefix] requestId=$requestId expectedSha=$expectedSha actualSha=${localSha.orEmpty()} url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=okhttp. Hapus temp file dan ulang full download.",
                        )
                        runCatching { if (target.exists()) target.delete() }
                        return false
                    }
                }
                if (target.exists() && target.length() > 0L) {
                    true
                } else {
                    if (streamError != null) throw streamError
                    false
                }
            }
        }.onFailure { error ->
            Log.e(
                "TemplateAssetStore",
                "Asset exception [$filePrefix] requestId=$requestId url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=okhttp error=${error.message}",
            )
        }.getOrDefault(false)
    }

    private fun downloadWithUrlConnectionToFile(
        resolvedUrl: String,
        hostHeader: String?,
        authToken: String?,
        target: File,
        filePrefix: String,
        requestId: String,
        attempt: Int,
        useAuth: Boolean,
    ): Boolean {
        return runCatching {
            val connection = (URL(resolvedUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8_000
                readTimeout = 15_000
                setRequestProperty("Accept", "image/png")
                setRequestProperty("Accept-Encoding", "identity")
                setRequestProperty("Connection", "keep-alive")
                setRequestProperty("User-Agent", buildAssetUserAgent())
                setRequestProperty("X-Debug-Client", "android-template-sync")
                setRequestProperty("X-Debug-Req-Id", requestId)
                if (!hostHeader.isNullOrBlank()) {
                    setRequestProperty("Host", hostHeader)
                }
                if (!authToken.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer ${authToken.trim()}")
                }
            }
            try {
                if (connection.responseCode !in 200..299) {
                    Log.e(
                        "TemplateAssetStore",
                        "Asset download gagal [$filePrefix] requestId=$requestId http=${connection.responseCode} url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=url-connection",
                    )
                    return false
                }
                logResponseDiagnostics(
                    filePrefix = filePrefix,
                    requestId = requestId,
                    responseCode = connection.responseCode,
                    resolvedUrl = resolvedUrl,
                    useAuth = useAuth,
                    attempt = attempt,
                    transport = "url-connection",
                    contentLength = connection.contentLengthLong.takeIf { it >= 0L },
                    assetId = connection.getHeaderField("X-Asset-Id"),
                    assetSize = connection.getHeaderField("X-Asset-Size")?.toLongOrNull(),
                    assetSha = connection.getHeaderField("X-Asset-Sha256"),
                    assetDelivery = connection.getHeaderField("X-Asset-Delivery"),
                )
                val contentType = connection.contentType.orEmpty().lowercase()
                val expectedLength = connection.contentLengthLong.takeIf { it >= 0L }
                val expectedSha = connection.getHeaderField("X-Asset-Sha256")?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
                if (!contentType.startsWith("image/")) {
                    Log.w(
                        "TemplateAssetStore",
                        "Bukan image [$filePrefix]! requestId=$requestId type=$contentType url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=url-connection",
                    )
                }
                connection.inputStream.use { input ->
                    var streamError: Throwable? = null
                    FileOutputStream(target, false).use { output ->
                        try {
                            val buffer = ByteArray(8 * 1024)
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                if (read == 0) continue
                                output.write(buffer, 0, read)
                            }
                        } catch (error: Throwable) {
                            streamError = error
                            Log.w(
                                "TemplateAssetStore",
                                "Stream terputus [$filePrefix] requestId=$requestId url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=url-connection error=${error.message}",
                            )
                        }
                        output.flush()
                    }
                    if (!target.exists() || target.length() <= 0L) {
                        if (streamError != null) throw streamError
                    }
                }
                val actualLength = target.length()
                if (expectedLength != null && actualLength != expectedLength) {
                    Log.w(
                        "TemplateAssetStore",
                        "Panjang file mismatch [$filePrefix] requestId=$requestId expected=$expectedLength actual=$actualLength url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=url-connection",
                    )
                    val recovered = tryRecoverWithRange(
                        resolvedUrl = resolvedUrl,
                        hostHeader = hostHeader,
                        authToken = authToken,
                        target = target,
                        expectedLength = expectedLength,
                        expectedSha = expectedSha,
                        filePrefix = filePrefix,
                        requestId = requestId,
                        useAuth = useAuth,
                        attempt = attempt,
                    )
                    if (recovered) {
                        Log.i(
                            "TemplateAssetStore",
                            "Range recovery berhasil [$filePrefix] requestId=$requestId url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS",
                        )
                    }
                }
                val finalLength = target.length()
                Log.i(
                    "TemplateAssetStore",
                    "File selesai ditulis [$filePrefix] requestId=$requestId expectedLength=${expectedLength ?: -1} actualLength=$finalLength hash=${computeShortHash(target)} url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=url-connection",
                )
                if (expectedLength != null && finalLength != expectedLength) {
                    return false
                }
                if (!expectedSha.isNullOrBlank()) {
                    val localSha = computeSha256(target)
                    if (localSha.isNullOrBlank() || !localSha.equals(expectedSha, ignoreCase = true)) {
                        Log.w(
                            "TemplateAssetStore",
                            "SHA mismatch [$filePrefix] requestId=$requestId expectedSha=$expectedSha actualSha=${localSha.orEmpty()} url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=url-connection. Hapus temp file dan ulang full download.",
                        )
                        runCatching { if (target.exists()) target.delete() }
                        return false
                    }
                }
            } finally {
                connection.disconnect()
            }
            target.exists() && target.length() > 0L
        }.onFailure { error ->
            Log.e(
                "TemplateAssetStore",
                "Asset exception [$filePrefix] requestId=$requestId url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=url-connection error=${error.message}",
            )
        }.getOrDefault(false)
    }

    private fun getCachedAssetPath(templateId: String, filePrefix: String): String? {
        val targetDir = File(context.filesDir, "templates/$templateId")
        if (!targetDir.exists()) return null
        val valid = targetDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("$filePrefix.") && it.length() > 0L }
            ?.sortedByDescending { it.lastModified() }
            ?.firstOrNull { file ->
                if (isValidImageFile(file)) {
                    true
                } else {
                    runCatching { file.delete() }
                    false
                }
            }
        if (valid == null) {
            targetDir.listFiles()
                ?.filter { it.isFile && it.name.startsWith("$filePrefix.") }
                ?.forEach { runCatching { it.delete() } }
        }
        return valid?.absolutePath
    }

    private fun purgeWorkingFiles(targetDir: File, filePrefix: String) {
        targetDir.listFiles()
            ?.filter { it.isFile && (it.name.startsWith("$filePrefix-") || it.name == "$filePrefix.tmp" || it.name == "$filePrefix.bak") }
            ?.forEach { stale -> runCatching { stale.delete() } }
    }

    private fun isValidImageFile(file: File): Boolean {
        if (!file.exists() || file.length() <= 0L) return false
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching false

            var sample = 1
            while ((bounds.outWidth / sample) > 1024 || (bounds.outHeight / sample) > 1024) {
                sample *= 2
            }
            val opts = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = sample.coerceAtLeast(1)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return@runCatching false
            bitmap.recycle()
            true
        }.getOrDefault(false)
    }

    private fun resolveAssetRequest(rawUrl: String, stationBase: String): ResolvedAssetRequest? {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return null
        val normalizedBase = stationBase.toStationBaseUrl()
        val base = runCatching { URI(normalizedBase) }.getOrNull()
        val uri = runCatching { URI(trimmed) }.getOrNull()
        if (uri?.isAbsolute == true) {
            val host = uri.host?.lowercase()
            val isLocalHost = host == "localhost" || host == "127.0.0.1" || host == "0.0.0.0"
            val shouldRewriteToStationHost = base != null &&
                !base.host.isNullOrBlank() &&
                (
                    isLocalHost ||
                        (
                            !host.isNullOrBlank() &&
                                !host.equals(base.host, ignoreCase = true)
                        )
                    )
            if (shouldRewriteToStationHost) {
                return runCatching {
                    val rewritten = URI(
                        uri.scheme ?: base.scheme,
                        uri.userInfo,
                        base.host,
                        if (uri.port != -1) uri.port else base.port,
                        uri.path,
                        uri.query,
                        uri.fragment,
                    ).toString()
                    val originalHostHeader = if (!uri.host.isNullOrBlank()) {
                        if (uri.port != -1) "${uri.host}:${uri.port}" else uri.host
                    } else {
                        null
                    }
                    ResolvedAssetRequest(downloadUrl = rewritten, hostHeader = originalHostHeader)
                }.getOrNull()
            }
            return ResolvedAssetRequest(downloadUrl = trimmed)
        }
        if (base == null) return null
        return runCatching { ResolvedAssetRequest(downloadUrl = base.resolve(trimmed).toString()) }.getOrNull()
    }

    private fun isSignedAssetUrl(url: String): Boolean {
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        val query = uri.rawQuery.orEmpty()
        return query.contains("signature=") && query.contains("expires=")
    }

    private fun computeShortHash(file: File): String {
        if (!file.exists() || file.length() <= 0L) return "empty"
        return runCatching {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    if (read == 0) continue
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest()
                .joinToString("") { byte -> "%02x".format(byte) }
                .take(16)
        }.getOrElse { "hash-error" }
    }

    private fun computeSha256(file: File): String? {
        if (!file.exists() || file.length() <= 0L) return null
        return runCatching {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    if (read == 0) continue
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { byte -> "%02x".format(byte) }
        }.getOrNull()
    }

    private fun buildRequestId(templateId: String, filePrefix: String, attempt: Int, useAuth: Boolean): String {
        val authMode = if (useAuth) "auth" else "guest"
        val shortTemplateId = templateId.takeLast(8)
        return "$filePrefix-$shortTemplateId-$attempt-$authMode-${UUID.randomUUID().toString().take(8)}"
    }

    private fun buildAssetUserAgent(): String {
        return "DafydioBooth/${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID}; Android ${Build.VERSION.RELEASE}; ${Build.MODEL})"
    }

    private fun logResponseDiagnostics(
        filePrefix: String,
        requestId: String,
        responseCode: Int,
        resolvedUrl: String,
        useAuth: Boolean,
        attempt: Int,
        transport: String,
        contentLength: Long?,
        assetId: String?,
        assetSize: Long?,
        assetSha: String?,
        assetDelivery: String? = null,
        etag: String? = null,
        lastModified: String? = null,
    ) {
        Log.i(
            "TemplateAssetStore",
            "Header asset [$filePrefix] requestId=$requestId code=$responseCode contentLength=${contentLength ?: -1} xAssetId=${assetId.orEmpty()} xAssetSize=${assetSize ?: -1} xAssetSha=${assetSha.orEmpty()} xAssetDelivery=${assetDelivery.orEmpty()} etag=${etag.orEmpty()} lastModified=${lastModified.orEmpty()} url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS via=$transport userAgent=${buildAssetUserAgent()}",
        )
    }

    private fun diagnoseAssetFailure(
        signedUrl: String,
        hostHeader: String?,
        authToken: String?,
        filePrefix: String,
        requestId: String,
    ) {
        if (diagnoseEndpointSupported == false) return
        val uri = runCatching { URI(signedUrl) }.getOrNull() ?: return
        val segments = uri.path?.trim('/').orEmpty().split('/')
        val assetId = segments.lastOrNull().orEmpty()
        if (assetId.isBlank()) return
        val diagnoseUrl = runCatching {
            URI(
                uri.scheme,
                uri.userInfo,
                uri.host,
                uri.port,
                "/api/device/template-assets/$assetId/diagnose",
                null,
                null,
            ).toString()
        }.getOrNull() ?: return

        runCatching {
            val req = Request.Builder()
                .url(diagnoseUrl)
                .header("Accept", "application/json")
                .header("Connection", "close")
                .header("User-Agent", buildAssetUserAgent())
                .header("X-Debug-Req-Id", "$requestId-diagnose")
                .apply {
                    if (!hostHeader.isNullOrBlank()) header("Host", hostHeader)
                    if (!authToken.isNullOrBlank()) header("Authorization", "Bearer ${authToken.trim()}")
                }
                .build()
            httpClient.newCall(req).execute().use { response ->
                val payload = response.body?.string().orEmpty().take(400)
                if (response.code == 404) {
                    diagnoseEndpointSupported = false
                    Log.i(
                        "TemplateAssetStore",
                        "Diagnose endpoint not supported by server. Skip next diagnose calls. requestId=$requestId url=$diagnoseUrl",
                    )
                    return@use
                }
                if (response.isSuccessful) {
                    diagnoseEndpointSupported = true
                }
                Log.w(
                    "TemplateAssetStore",
                    "Diagnose asset [$filePrefix] requestId=$requestId code=${response.code} url=$diagnoseUrl payload=$payload",
                )
            }
        }.onFailure { error ->
            Log.w(
                "TemplateAssetStore",
                "Diagnose asset gagal [$filePrefix] requestId=$requestId url=$diagnoseUrl error=${error.message}",
            )
        }
    }

    private fun fetchAssetMetadataByRange(
        resolvedUrl: String,
        hostHeader: String?,
        authToken: String?,
        filePrefix: String,
        requestId: String,
        attempt: Int,
    ): AssetMetadata? {
        val metadataRequestId = "$requestId-meta-a$attempt"
        val requestBuilder = Request.Builder()
            .url(resolvedUrl)
            .header("Accept", "image/png")
            .header("Accept-Encoding", "identity")
            .header("Connection", "close")
            .header("Cache-Control", "no-cache")
            .header("User-Agent", buildAssetUserAgent())
            .header("X-Debug-Client", "android-template-sync")
            .header("X-Debug-Req-Id", metadataRequestId)
            .header("Range", "bytes=0-0")
        if (!hostHeader.isNullOrBlank()) requestBuilder.header("Host", hostHeader)
        if (!authToken.isNullOrBlank()) requestBuilder.header("Authorization", "Bearer ${authToken.trim()}")

        return runCatching {
            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                val code = response.code
                val contentRange = response.header("Content-Range").orEmpty()
                if (code != 206) {
                    Log.w(
                        "TemplateAssetStore",
                        "Metadata range gagal [$filePrefix] requestId=$metadataRequestId code=$code contentRange=$contentRange url=$resolvedUrl",
                    )
                    return@use null
                }
                logResponseDiagnostics(
                    filePrefix = filePrefix,
                    requestId = metadataRequestId,
                    responseCode = code,
                    resolvedUrl = resolvedUrl,
                    useAuth = !authToken.isNullOrBlank(),
                    attempt = attempt,
                    transport = "range-metadata",
                    contentLength = response.header("Content-Length")?.toLongOrNull(),
                    assetId = response.header("X-Asset-Id"),
                    assetSize = response.header("X-Asset-Size")?.toLongOrNull(),
                    assetSha = response.header("X-Asset-Sha256"),
                    assetDelivery = response.header("X-Asset-Delivery"),
                    etag = response.header("ETag"),
                    lastModified = response.header("Last-Modified"),
                )
                response.body?.close()
                val expectedLength = response.header("X-Asset-Size")?.toLongOrNull()
                    ?: parseTotalLengthFromContentRange(contentRange)
                if (expectedLength == null || expectedLength <= 0L) {
                    Log.w(
                        "TemplateAssetStore",
                        "Metadata range tanpa ukuran valid [$filePrefix] requestId=$metadataRequestId contentRange=$contentRange url=$resolvedUrl",
                    )
                    return@use null
                }
                AssetMetadata(
                    expectedLength = expectedLength,
                    expectedSha = response.header("X-Asset-Sha256")?.trim()?.lowercase()?.takeIf { it.isNotBlank() },
                )
            }
        }.onFailure { error ->
            Log.w(
                "TemplateAssetStore",
                "Metadata range exception [$filePrefix] requestId=$metadataRequestId url=$resolvedUrl error=${error.message}",
            )
        }.getOrNull()
    }

    private fun parseTotalLengthFromContentRange(contentRange: String): Long? {
        val total = contentRange.substringAfter('/', "").trim()
        return total.toLongOrNull()
    }

    private fun parseContentRangeHeader(contentRange: String): ParsedContentRange? {
        val cleaned = contentRange.trim()
        if (!cleaned.startsWith("bytes ")) return null
        val rangePart = cleaned.removePrefix("bytes ").substringBefore('/').trim()
        val totalPart = cleaned.substringAfter('/', "").trim()
        val start = rangePart.substringBefore('-', "").trim().toLongOrNull() ?: return null
        val end = rangePart.substringAfter('-', "").trim().toLongOrNull() ?: return null
        val total = totalPart.toLongOrNull() ?: return null
        return ParsedContentRange(start = start, end = end, total = total)
    }

    private fun tryRecoverWithRange(
        resolvedUrl: String,
        hostHeader: String?,
        authToken: String?,
        target: File,
        expectedLength: Long,
        expectedSha: String?,
        filePrefix: String,
        requestId: String,
        useAuth: Boolean,
        attempt: Int,
    ): Boolean {
        val maxRangeRetries = 3
        repeat(maxRangeRetries) { idx ->
            val currentLength = target.length()
            if (currentLength >= expectedLength) return true
            val rangeRequestId = "$requestId-r${idx + 1}"
            Log.i(
                "TemplateAssetStore",
                "Mulai range recovery [$filePrefix] requestId=$rangeRequestId current=$currentLength expected=$expectedLength url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS",
            )
            val requestBuilder = Request.Builder()
                .url(resolvedUrl)
                .header("Accept", "image/png")
                .header("Accept-Encoding", "identity")
                .header("Connection", "keep-alive")
                .header("Cache-Control", "no-cache")
                .header("User-Agent", buildAssetUserAgent())
                .header("X-Debug-Client", "android-template-sync")
                .header("X-Debug-Req-Id", rangeRequestId)
                .header("Range", "bytes=$currentLength-")
            if (!hostHeader.isNullOrBlank()) {
                requestBuilder.header("Host", hostHeader)
            }
            if (!authToken.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer ${authToken.trim()}")
            }
            val recovered = runCatching {
                httpClient.newCall(requestBuilder.build()).execute().use { response ->
                    val code = response.code
                    val acceptRanges = response.header("Accept-Ranges").orEmpty()
                    val contentRange = response.header("Content-Range").orEmpty()
                    if (code == 416) {
                        Log.w(
                            "TemplateAssetStore",
                            "Range invalid (416) [$filePrefix] requestId=$rangeRequestId current=$currentLength expected=$expectedLength -> reset partial file",
                        )
                        runCatching { if (target.exists()) target.delete() }
                        return@use false
                    }
                    if (code != 206) {
                        Log.w(
                            "TemplateAssetStore",
                            "Range recovery gagal [$filePrefix] requestId=$rangeRequestId code=$code acceptRanges=$acceptRanges contentRange=$contentRange current=$currentLength expected=$expectedLength url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS",
                        )
                        return@use false
                    }
                    FileOutputStream(target, true).use { output ->
                        response.body.byteStream().use { input ->
                            val buffer = ByteArray(8 * 1024)
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                if (read == 0) continue
                                output.write(buffer, 0, read)
                            }
                        }
                        output.flush()
                    }
                    Log.i(
                        "TemplateAssetStore",
                        "Range chunk diterima [$filePrefix] requestId=$rangeRequestId code=$code acceptRanges=$acceptRanges contentRange=$contentRange now=${target.length()} expected=$expectedLength",
                    )
                    true
                }
            }.onFailure { error ->
                Log.w(
                    "TemplateAssetStore",
                    "Range recovery exception [$filePrefix] requestId=$rangeRequestId current=$currentLength expected=$expectedLength url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS error=${error.message}",
                )
            }.getOrDefault(false)
            if (!recovered) return@repeat
            val finalLength = target.length()
            val shaOk = if (!expectedSha.isNullOrBlank() && finalLength >= expectedLength) {
                computeSha256(target)?.equals(expectedSha, ignoreCase = true) == true
            } else {
                true
            }
            if (finalLength >= expectedLength && shaOk) {
                return true
            }
            Log.w(
                "TemplateAssetStore",
                "Range recovery belum lengkap [$filePrefix] requestId=$rangeRequestId finalLength=$finalLength expected=$expectedLength hash=${computeShortHash(target)} url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS",
            )
        }
        return false
    }

    private fun recoverFullWithChunkedRanges(
        resolvedUrl: String,
        hostHeader: String?,
        authToken: String?,
        target: File,
        expectedLength: Long,
        expectedSha: String?,
        filePrefix: String,
        requestId: String,
        useAuth: Boolean,
        attempt: Int,
    ): Boolean {
        val chunkSize = 64L * 1024L
        runCatching { if (target.exists()) target.delete() }
        var offset = 0L
        while (offset < expectedLength) {
            val end = minOf(offset + chunkSize - 1L, expectedLength - 1L)
            val chunkRequestId = "$requestId-c${offset}"
            val expectedChunkLength = end - offset + 1L
            val chunkFile = File(target.parentFile, "${target.nameWithoutExtension}-$chunkRequestId.part")
            runCatching { if (chunkFile.exists()) chunkFile.delete() }
            val requestBuilder = Request.Builder()
                .url(resolvedUrl)
                .header("Accept", "image/png")
                .header("Accept-Encoding", "identity")
                .header("Connection", "keep-alive")
                .header("Cache-Control", "no-cache")
                .header("User-Agent", buildAssetUserAgent())
                .header("X-Debug-Client", "android-template-sync")
                .header("X-Debug-Req-Id", chunkRequestId)
                .header("Range", "bytes=$offset-$end")
            if (!hostHeader.isNullOrBlank()) {
                requestBuilder.header("Host", hostHeader)
            }
            if (!authToken.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer ${authToken.trim()}")
            }
            val chunkOk = runCatching {
                httpClient.newCall(requestBuilder.build()).execute().use { response ->
                    val code = response.code
                    val contentLength = response.header("Content-Length")?.toLongOrNull()
                    val contentRange = response.header("Content-Range").orEmpty()
                    val parsedRange = parseContentRangeHeader(contentRange)
                    if (code != 206) {
                        Log.w(
                            "TemplateAssetStore",
                            "Chunk range gagal [$filePrefix] requestId=$chunkRequestId code=$code expectedRange=bytes $offset-$end/$expectedLength",
                        )
                        return@use false
                    }
                    if (contentLength != expectedChunkLength) {
                        Log.w(
                            "TemplateAssetStore",
                            "Chunk length mismatch [$filePrefix] requestId=$chunkRequestId expected=$expectedChunkLength actual=${contentLength ?: -1}",
                        )
                        return@use false
                    }
                    if (parsedRange == null || parsedRange.start != offset || parsedRange.end != end || parsedRange.total != expectedLength) {
                        Log.w(
                            "TemplateAssetStore",
                            "Chunk content-range mismatch [$filePrefix] requestId=$chunkRequestId expected=$offset-$end/$expectedLength actual=$contentRange",
                        )
                        return@use false
                    }
                    var actualRead = 0L
                    FileOutputStream(chunkFile, false).use { output ->
                        response.body.byteStream().use { input ->
                            val buffer = ByteArray(8 * 1024)
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                if (read == 0) continue
                                output.write(buffer, 0, read)
                                actualRead += read.toLong()
                            }
                        }
                        output.flush()
                    }
                    if (actualRead != expectedChunkLength || !chunkFile.exists() || chunkFile.length() != expectedChunkLength) {
                        Log.w(
                            "TemplateAssetStore",
                            "Chunk read mismatch [$filePrefix] requestId=$chunkRequestId expected=$expectedChunkLength actualRead=$actualRead fileLength=${chunkFile.length()}",
                        )
                        runCatching { if (chunkFile.exists()) chunkFile.delete() }
                        return@use false
                    }
                    FileOutputStream(target, true).use { output ->
                        chunkFile.inputStream().use { input -> input.copyTo(output) }
                        output.flush()
                    }
                    runCatching { if (chunkFile.exists()) chunkFile.delete() }
                    true
                }
            }.onFailure { error ->
                Log.w(
                    "TemplateAssetStore",
                    "Chunk range exception [$filePrefix] requestId=$chunkRequestId error=${error.message}",
                )
                runCatching { if (chunkFile.exists()) chunkFile.delete() }
            }.getOrDefault(false)
            if (!chunkOk) {
                runCatching { if (chunkFile.exists()) chunkFile.delete() }
                return false
            }
            offset = end + 1L
        }
        val finalLength = target.length()
        val finalSha = computeSha256(target)
        val shaOk = expectedSha.isNullOrBlank() || finalSha.equals(expectedSha, ignoreCase = true)
        val ok = finalLength == expectedLength && shaOk
        if (ok) {
            Log.i(
                "TemplateAssetStore",
                "Chunked range recovery berhasil [$filePrefix] requestId=$requestId finalLength=$finalLength finalSha=${finalSha.orEmpty()} url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS",
            )
        } else {
            Log.w(
                "TemplateAssetStore",
                "Chunked range recovery gagal [$filePrefix] requestId=$requestId finalLength=$finalLength expectedLength=$expectedLength finalSha=${finalSha.orEmpty()} expectedSha=${expectedSha.orEmpty()} url=$resolvedUrl auth=$useAuth attempt=$attempt/$MAX_DOWNLOAD_ATTEMPTS",
            )
        }
        return ok
    }
}

