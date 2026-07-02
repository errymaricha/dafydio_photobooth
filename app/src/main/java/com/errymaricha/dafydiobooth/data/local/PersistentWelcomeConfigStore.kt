package com.errymaricha.dafydiobooth.data.local

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object PersistentWelcomeConfigStore {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Serializable
    data class WelcomeConfigEntry(
        val bgUri: String,
        val isVideo: Boolean
    )

    @Serializable
    data class WelcomeConfigMap(
        val mappings: Map<String, WelcomeConfigEntry> = emptyMap()
    )

    fun saveMapping(context: Context, eventCode: String, bgUri: String, isVideo: Boolean) {
        if (eventCode.isBlank()) return
        val currentMappings = loadAllMappings(context).toMutableMap()
        if (bgUri.isBlank()) {
            currentMappings.remove(eventCode)
        } else {
            currentMappings[eventCode] = WelcomeConfigEntry(bgUri, isVideo)
        }
        writeMappings(context, WelcomeConfigMap(currentMappings))
    }

    fun getMapping(context: Context, eventCode: String): Pair<String, Boolean>? {
        if (eventCode.isBlank()) return null
        val mappings = loadAllMappings(context)
        val entry = mappings[eventCode] ?: return null
        return Pair(entry.bgUri, entry.isVideo)
    }

    private fun loadAllMappings(context: Context): Map<String, WelcomeConfigEntry> {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} = ?"
            val selectionArgs = arrayOf("welcome_config.json", "${Environment.DIRECTORY_DOWNLOADS}/DafydioBooth/")
            try {
                resolver.query(contentUri, null, selection, selectionArgs, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                        val id = cursor.getLong(idCol)
                        val fileUri = ContentUris.withAppendedId(contentUri, id)
                        resolver.openInputStream(fileUri)?.use { input ->
                            val jsonStr = input.bufferedReader().use { it.readText() }
                            return json.decodeFromString<WelcomeConfigMap>(jsonStr).mappings
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DafydioBooth")
            val file = File(dir, "welcome_config.json")
            if (file.exists()) {
                try {
                    val jsonStr = file.readText()
                    return json.decodeFromString<WelcomeConfigMap>(jsonStr).mappings
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return emptyMap()
    }

    private fun writeMappings(context: Context, configMap: WelcomeConfigMap) {
        val resolver = context.contentResolver
        val jsonStr = json.encodeToString(WelcomeConfigMap.serializer(), configMap)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} = ?"
            val selectionArgs = arrayOf("welcome_config.json", "${Environment.DIRECTORY_DOWNLOADS}/DafydioBooth/")
            
            var fileUri: Uri? = null
            try {
                resolver.query(contentUri, null, selection, selectionArgs, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                        val id = cursor.getLong(idCol)
                        fileUri = ContentUris.withAppendedId(contentUri, id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (fileUri == null) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, "welcome_config.json")
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/DafydioBooth")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                try {
                    fileUri = resolver.insert(contentUri, values)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (fileUri != null) {
                try {
                    resolver.openOutputStream(fileUri!!, "rwt")?.use { output ->
                        output.write(jsonStr.toByteArray())
                    }
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.IS_PENDING, 0)
                    }
                    resolver.update(fileUri!!, values, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DafydioBooth")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "welcome_config.json")
            try {
                file.writeText(jsonStr)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun copyUriToPublicStorage(context: Context, uri: Uri, isVideo: Boolean, eventCode: String): String? {
        return try {
            val resolver = context.contentResolver
            val extension = when (resolver.getType(uri)) {
                "video/mp4" -> "mp4"
                "video/mkv" -> "mkv"
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> uri.toString().substringAfterLast('.', "dat")
            }
            val displayName = "welcome_${eventCode}_${System.currentTimeMillis()}.$extension"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentUri = if (isVideo) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                val relativePath = if (isVideo) {
                    "${Environment.DIRECTORY_MOVIES}/DafydioBooth"
                } else {
                    "${Environment.DIRECTORY_PICTURES}/DafydioBooth"
                }
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, resolver.getType(uri))
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val targetUri = resolver.insert(contentUri, values) ?: return null
                resolver.openOutputStream(targetUri)?.use { output ->
                    resolver.openInputStream(uri)?.use { input ->
                        input.copyTo(output)
                    }
                }
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(targetUri, values, null, null)
                targetUri.toString()
            } else {
                val targetDir = File(
                    Environment.getExternalStoragePublicDirectory(
                        if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES
                    ),
                    "DafydioBooth"
                )
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, displayName)
                resolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                targetFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
