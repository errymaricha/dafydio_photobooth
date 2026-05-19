package com.errymaricha.dafydiobooth.ui.booth.external

import android.content.Context
import android.os.Environment
import java.io.File

class ExternalCaptureStore(private val context: Context) {
    @Volatile
    private var previewSlot: Int = 0

    private fun outputDir(): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "DafydioBooth")
        dir.mkdirs()
        return dir
    }

    fun saveJpeg(bytes: ByteArray): String? {
        val outputFile = File(outputDir(), "canon-${System.currentTimeMillis()}.jpg")
        return runCatching {
            outputFile.writeBytes(bytes)
            outputFile.absolutePath
        }.getOrNull()
    }

    fun savePreviewJpeg(bytes: ByteArray): String? {
        previewSlot = 1 - previewSlot
        val outputFile = File(outputDir(), "canon-preview-$previewSlot.jpg")
        return runCatching {
            outputFile.writeBytes(bytes)
            outputFile.absolutePath
        }.getOrNull()
    }
}
