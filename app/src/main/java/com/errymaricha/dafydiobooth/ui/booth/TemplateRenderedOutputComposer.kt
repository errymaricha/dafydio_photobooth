package com.errymaricha.dafydiobooth.ui.booth

import android.content.Context
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class TemplateRenderedOutputComposer(private val context: Context) {
    private data class ResolvedAssetRequest(
        val downloadUrl: String,
        val hostHeader: String? = null,
    )

    private val httpClient = OkHttpClient()

    suspend fun compose(state: BoothUiState): File? = withContext(Dispatchers.IO) {
        val canvasWidth = state.selectedTemplateCanvasWidth.takeIf { it > 0 } ?: return@withContext null
        val canvasHeight = state.selectedTemplateCanvasHeight.takeIf { it > 0 } ?: return@withContext null
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val overlayLocalSource = state.selectedTemplateOverlayLocalPath?.takeIf { it.isNotBlank() }
            ?.let(::File)
            ?.takeIf { it.exists() && it.length() > 0L }
            ?.absolutePath
        val hasOverlay = !overlayLocalSource.isNullOrBlank() || !state.selectedTemplateOverlayUrl.isNullOrBlank()

        val previewLocalSource = state.selectedTemplatePreviewLocalPath
            ?.takeIf { it.isNotBlank() }
            ?.let(::File)
            ?.takeIf { it.exists() && it.length() > 0L }
            ?.absolutePath
        if (!hasOverlay && previewLocalSource.isNullOrBlank() && !state.selectedTemplatePreviewUrl.isNullOrBlank()) {
            Log.e(
                "TemplateRenderedOutput",
                "Preview lokal belum tersedia untuk template=${state.selectedTemplateId} remote=${state.selectedTemplatePreviewUrl}",
            )
            bitmap.recycle()
            return@withContext null
        }
        if (!hasOverlay) {
            decodeBitmap(
                source = previewLocalSource,
                authToken = state.authToken,
                stationBaseUrl = state.stationIp,
                reqWidth = canvasWidth,
                reqHeight = canvasHeight,
            )?.use { preview ->
                canvas.drawBitmap(preview, null, Rect(0, 0, canvasWidth, canvasHeight), null)
            }
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val androidFilter = when (state.selectedColorFilter) {
            ColorFilterType.Normal -> null
            ColorFilterType.Bw -> {
                val cm = android.graphics.ColorMatrix().apply { setSaturation(0f) }
                android.graphics.ColorMatrixColorFilter(cm)
            }
            ColorFilterType.Vintage -> {
                val matrix = floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f,     0f,     0f,     1f, 0f
                )
                android.graphics.ColorMatrixColorFilter(matrix)
            }
            ColorFilterType.Cool -> {
                val matrix = floatArrayOf(
                    0.8f, 0f,   0f,   0f, 0f,
                    0f,   1.0f, 0f,   0f, 0f,
                    0f,   0f,   1.2f, 0f, 0f,
                    0f,   0f,   0f,   1f, 0f
                )
                android.graphics.ColorMatrixColorFilter(matrix)
            }
        }
        paint.colorFilter = androidFilter

        state.selectedTemplateSlots.sortedBy { it.slotIndex }.forEach { slot ->
            val photoPath = state.capturedPhotosBySlot[slot.sourceSlotIndex] ?: return@forEach
            val photo = decodeBitmap(photoPath) ?: return@forEach
            photo.use {
                drawPhotoInSlot(canvas, paint, it, slot)
            }
        }

        if (overlayLocalSource.isNullOrBlank() && !state.selectedTemplateOverlayUrl.isNullOrBlank()) {
            Log.e(
                "TemplateRenderedOutput",
                "Overlay lokal belum tersedia untuk template=${state.selectedTemplateId} remote=${state.selectedTemplateOverlayUrl}",
            )
            bitmap.recycle()
            return@withContext null
        }
        val overlayBitmap = decodeBitmap(
            source = overlayLocalSource,
            authToken = state.authToken,
            stationBaseUrl = state.stationIp,
            reqWidth = canvasWidth,
            reqHeight = canvasHeight,
        )
        if ((!overlayLocalSource.isNullOrBlank() || !state.selectedTemplateOverlayUrl.isNullOrBlank()) && overlayBitmap == null) {
            val sourceInfo = "local=$overlayLocalSource remote=${state.selectedTemplateOverlayUrl}"
            Log.e("TemplateRenderedOutput", "Overlay decode gagal untuk source=$sourceInfo")
            bitmap.recycle()
            return@withContext null
        }
        overlayBitmap?.use { overlay ->
            canvas.drawBitmap(overlay, null, Rect(0, 0, canvasWidth, canvasHeight), null)
        }

        val targetDir = File(context.cacheDir, "rendered_output").apply { mkdirs() }
        val targetFile = File(targetDir, "session_render_${System.currentTimeMillis()}.png")
        FileOutputStream(targetFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.flush()
        }
        bitmap.recycle()
        return@withContext targetFile.takeIf { it.exists() && it.length() > 0L }
    }

    suspend fun saveToGallery(sourceFile: File): String? = withContext(Dispatchers.IO) {
        if (!sourceFile.exists() || sourceFile.length() <= 0L) return@withContext null
        val displayName = "dafydio_render_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/DafydioBooth")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext null
            return@withContext runCatching {
                resolver.openOutputStream(uri)?.use { output ->
                    sourceFile.inputStream().use { input -> input.copyTo(output) }
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri.toString()
            }.getOrNull()
        }

        val targetDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "DafydioBooth",
        )
        if (!targetDir.exists()) targetDir.mkdirs()
        val targetFile = File(targetDir, displayName)
        return@withContext runCatching {
            sourceFile.inputStream().use { input ->
                targetFile.outputStream().use { output -> input.copyTo(output) }
            }
            targetFile.absolutePath
        }.getOrNull()
    }

    private fun drawPhotoInSlot(
        canvas: Canvas,
        paint: Paint,
        photo: Bitmap,
        slot: TemplateSlotLayout,
    ) {
        val dest = RectF(
            slot.x.toFloat(),
            slot.y.toFloat(),
            slot.x.toFloat() + slot.width.toFloat(),
            slot.y.toFloat() + slot.height.toFloat(),
        )
        val radius = slot.borderRadius.toFloat().coerceAtLeast(0f)
        val src = centerCropRect(
            sourceWidth = photo.width,
            sourceHeight = photo.height,
            destWidth = slot.width.coerceAtLeast(1),
            destHeight = slot.height.coerceAtLeast(1),
        )
        canvas.save()
        canvas.rotate(slot.rotation.toFloat(), dest.centerX(), dest.centerY())
        val clipPath = Path().apply {
            addRoundRect(dest, radius, radius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)
        canvas.drawBitmap(photo, src, dest, paint)
        canvas.restore()
    }

    private fun centerCropRect(
        sourceWidth: Int,
        sourceHeight: Int,
        destWidth: Int,
        destHeight: Int,
    ): Rect {
        val sourceAspect = sourceWidth.toFloat() / sourceHeight.toFloat()
        val destAspect = destWidth.toFloat() / destHeight.toFloat()
        return if (sourceAspect > destAspect) {
            val cropWidth = (sourceHeight * destAspect).toInt().coerceAtLeast(1)
            val left = ((sourceWidth - cropWidth) / 2).coerceAtLeast(0)
            Rect(left, 0, (left + cropWidth).coerceAtMost(sourceWidth), sourceHeight)
        } else {
            val cropHeight = (sourceWidth / destAspect).toInt().coerceAtLeast(1)
            val top = ((sourceHeight - cropHeight) / 2).coerceAtLeast(0)
            Rect(0, top, sourceWidth, (top + cropHeight).coerceAtMost(sourceHeight))
        }
    }

    private fun decodeBitmap(
        source: String?,
        authToken: String? = null,
        stationBaseUrl: String? = null,
        reqWidth: Int = 0,
        reqHeight: Int = 0,
    ): Bitmap? {
        if (source.isNullOrBlank()) return null
        val value = source.trim()
        return runCatching {
            when {
                value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true) -> {
                    val requestInfo = resolveAssetRequest(value, stationBaseUrl.orEmpty())
                    val resolvedUrl = requestInfo?.downloadUrl ?: value
                    val hostHeader = requestInfo?.hostHeader
                    var decoded: Bitmap? = null
                    val useAuthModes = if (authToken.isNullOrBlank()) listOf(false) else listOf(true, false)
                    repeat(3) { attemptIndex ->
                        if (decoded != null) return@repeat
                        val attempt = attemptIndex + 1
                        useAuthModes.forEach { useAuth ->
                            if (decoded != null) return@forEach
                            val requestBuilder = Request.Builder()
                                .url(resolvedUrl)
                                .header("Accept", "image/*,*/*;q=0.8")
                                .header("Accept-Encoding", "identity")
                                .header("Connection", "close")
                                .header("Cache-Control", "no-cache")
                            if (!hostHeader.isNullOrBlank()) {
                                requestBuilder.header("Host", hostHeader)
                            }
                            if (useAuth && !authToken.isNullOrBlank()) {
                                requestBuilder.header("Authorization", "Bearer ${authToken.trim()}")
                            }
                            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                                if (!response.isSuccessful) {
                                    Log.e(
                                        "TemplateRenderedOutput",
                                        "HTTP gagal code=${response.code} auth=$useAuth attempt=$attempt/3 url=$resolvedUrl",
                                    )
                                    return@use
                                }
                                val contentType = response.header("Content-Type").orEmpty().lowercase()
                                if (!contentType.startsWith("image/")) {
                                    Log.w(
                                        "TemplateRenderedOutput",
                                        "Content-Type bukan image type=$contentType auth=$useAuth attempt=$attempt/3 url=$resolvedUrl",
                                    )
                                    // Tetap lanjut: beberapa endpoint storage mengembalikan octet-stream.
                                }
                                val bytes = response.body?.bytes() ?: return@use
                                decoded = decodeSampledByteArray(bytes, reqWidth, reqHeight)
                                if (decoded == null) {
                                    Log.e(
                                        "TemplateRenderedOutput",
                                        "Decode bytes gagal size=${bytes.size} auth=$useAuth attempt=$attempt/3 url=$resolvedUrl",
                                    )
                                }
                            }
                        }
                        if (decoded == null && attempt < 3) {
                            runCatching { Thread.sleep((attempt * 250).toLong()) }
                        }
                    }
                    decoded
                }
                value.startsWith("content://", ignoreCase = true) -> {
                    val uri = android.net.Uri.parse(value)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bytes = stream.readBytes()
                        decodeSampledByteArray(bytes, reqWidth, reqHeight)
                    }
                }
                else -> decodeSampledFile(value, reqWidth, reqHeight)
            }
        }.onFailure { error ->
            Log.e("TemplateRenderedOutput", "decodeBitmap gagal source=$value error=${error.message}")
        }.getOrNull()
    }

    private fun decodeSampledFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val decoded = if (reqWidth <= 0 || reqHeight <= 0) {
            BitmapFactory.decodeFile(path)
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inSampleSize = calculateInSampleSize(bounds, reqWidth, reqHeight)
                inJustDecodeBounds = false
            }
            BitmapFactory.decodeFile(path, opts)
        } ?: return null
        return applyExifOrientation(decoded, path)
    }

    private fun decodeSampledByteArray(bytes: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (reqWidth <= 0 || reqHeight <= 0) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val opts = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = calculateInSampleSize(bounds, reqWidth, reqHeight)
            inJustDecodeBounds = false
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun applyExifOrientation(bitmap: Bitmap, path: String): Bitmap {
        val orientation = runCatching {
            ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED) {
            return bitmap
        }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }

        return runCatching {
            val transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (transformed != bitmap) {
                bitmap.recycle()
            }
            transformed
        }.getOrElse {
            bitmap
        }
    }

    private fun resolveAssetRequest(rawUrl: String, stationBase: String): ResolvedAssetRequest? {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return null
        val base = runCatching { URI(stationBase.trim()) }.getOrNull()
        val uri = runCatching { URI(trimmed) }.getOrNull()
        if (uri?.isAbsolute == true) {
            val host = uri.host?.lowercase()
            val isLocalHost = host == "localhost" || host == "127.0.0.1" || host == "0.0.0.0"
            if (isLocalHost && base != null && !base.host.isNullOrBlank()) {
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
                    val hasSignedQuery = uri.rawQuery?.let { query ->
                        query.contains("signature=") && query.contains("expires=")
                    } == true
                    val originalHostHeader = if (hasSignedQuery) {
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
}

private inline fun Bitmap.use(block: (Bitmap) -> Unit) {
    try {
        block(this)
    } finally {
        if (!isRecycled) recycle()
    }
}
