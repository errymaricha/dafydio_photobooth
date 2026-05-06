package com.errymaricha.dafydiobooth.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class TemplateSqliteStore(context: Context) {
    private val dbHelper = TemplateDbHelper(context.applicationContext)
    private val _templates = MutableStateFlow(loadTemplates())
    val templates: StateFlow<List<StoredTemplate>> = _templates.asStateFlow()

    suspend fun replaceTemplates(items: List<StoredTemplate>) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(TemplateDbHelper.TABLE_TEMPLATES, null, null)
            items.forEach { item ->
                db.insert(TemplateDbHelper.TABLE_TEMPLATES, null, item.toContentValues())
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        _templates.value = loadTemplates()
    }

    suspend fun updateOverlayLocalPath(templateId: String, overlayLocalPath: String) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(TemplateDbHelper.COL_OVERLAY_LOCAL_PATH, overlayLocalPath)
        }
        dbHelper.writableDatabase.update(
            TemplateDbHelper.TABLE_TEMPLATES,
            values,
            "${TemplateDbHelper.COL_TEMPLATE_ID} = ?",
            arrayOf(templateId),
        )
        _templates.value = loadTemplates()
    }

    private fun loadTemplates(): List<StoredTemplate> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TemplateDbHelper.TABLE_TEMPLATES,
            null,
            null,
            null,
            null,
            null,
            "${TemplateDbHelper.COL_TEMPLATE_NAME} ASC",
        )
        cursor.use {
            val list = mutableListOf<StoredTemplate>()
            while (it.moveToNext()) {
                list += StoredTemplate(
                    templateId = it.getStringOrEmpty(TemplateDbHelper.COL_TEMPLATE_ID),
                    templateCode = it.getStringOrEmpty(TemplateDbHelper.COL_TEMPLATE_CODE),
                    templateName = it.getStringOrEmpty(TemplateDbHelper.COL_TEMPLATE_NAME),
                    category = it.getStringOrNull(TemplateDbHelper.COL_CATEGORY),
                    paperSize = it.getStringOrNull(TemplateDbHelper.COL_PAPER_SIZE),
                    canvasWidth = it.getIntOrZero(TemplateDbHelper.COL_CANVAS_WIDTH),
                    canvasHeight = it.getIntOrZero(TemplateDbHelper.COL_CANVAS_HEIGHT),
                    thumbnailUrl = it.getStringOrNull(TemplateDbHelper.COL_THUMBNAIL_URL),
                    thumbnailLocalPath = it.getStringOrNull(TemplateDbHelper.COL_THUMBNAIL_LOCAL_PATH),
                    previewUrl = it.getStringOrNull(TemplateDbHelper.COL_PREVIEW_URL),
                    previewLocalPath = it.getStringOrNull(TemplateDbHelper.COL_PREVIEW_LOCAL_PATH),
                    overlayUrl = it.getStringOrNull(TemplateDbHelper.COL_OVERLAY_URL),
                    overlayLocalPath = it.getStringOrNull(TemplateDbHelper.COL_OVERLAY_LOCAL_PATH),
                    configJson = it.getStringOrNull(TemplateDbHelper.COL_CONFIG_JSON),
                    slotsJson = it.getStringOrEmpty(TemplateDbHelper.COL_SLOTS_JSON),
                )
            }
            return list
        }
    }

    private fun StoredTemplate.toContentValues(): ContentValues {
        return ContentValues().apply {
            put(TemplateDbHelper.COL_TEMPLATE_ID, templateId)
            put(TemplateDbHelper.COL_TEMPLATE_CODE, templateCode)
            put(TemplateDbHelper.COL_TEMPLATE_NAME, templateName)
            put(TemplateDbHelper.COL_CATEGORY, category)
            put(TemplateDbHelper.COL_PAPER_SIZE, paperSize)
            put(TemplateDbHelper.COL_CANVAS_WIDTH, canvasWidth)
            put(TemplateDbHelper.COL_CANVAS_HEIGHT, canvasHeight)
            put(TemplateDbHelper.COL_THUMBNAIL_URL, thumbnailUrl)
            put(TemplateDbHelper.COL_THUMBNAIL_LOCAL_PATH, thumbnailLocalPath)
            put(TemplateDbHelper.COL_PREVIEW_URL, previewUrl)
            put(TemplateDbHelper.COL_PREVIEW_LOCAL_PATH, previewLocalPath)
            put(TemplateDbHelper.COL_OVERLAY_URL, overlayUrl)
            put(TemplateDbHelper.COL_OVERLAY_LOCAL_PATH, overlayLocalPath)
            put(TemplateDbHelper.COL_CONFIG_JSON, configJson)
            put(TemplateDbHelper.COL_SLOTS_JSON, slotsJson)
        }
    }
}

private class TemplateDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_TEMPLATES (
                $COL_TEMPLATE_ID TEXT PRIMARY KEY,
                $COL_TEMPLATE_CODE TEXT NOT NULL,
                $COL_TEMPLATE_NAME TEXT NOT NULL,
                $COL_CATEGORY TEXT,
                $COL_PAPER_SIZE TEXT,
                $COL_CANVAS_WIDTH INTEGER NOT NULL,
                $COL_CANVAS_HEIGHT INTEGER NOT NULL,
                $COL_THUMBNAIL_URL TEXT,
                $COL_THUMBNAIL_LOCAL_PATH TEXT,
                $COL_PREVIEW_URL TEXT,
                $COL_PREVIEW_LOCAL_PATH TEXT,
                $COL_OVERLAY_URL TEXT,
                $COL_OVERLAY_LOCAL_PATH TEXT,
                $COL_CONFIG_JSON TEXT,
                $COL_SLOTS_JSON TEXT NOT NULL
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TEMPLATES")
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "dafydio_booth.db"
        private const val DB_VERSION = 3
        const val TABLE_TEMPLATES = "templates"
        const val COL_TEMPLATE_ID = "template_id"
        const val COL_TEMPLATE_CODE = "template_code"
        const val COL_TEMPLATE_NAME = "template_name"
        const val COL_CATEGORY = "category"
        const val COL_PAPER_SIZE = "paper_size"
        const val COL_CANVAS_WIDTH = "canvas_width"
        const val COL_CANVAS_HEIGHT = "canvas_height"
        const val COL_THUMBNAIL_URL = "thumbnail_url"
        const val COL_THUMBNAIL_LOCAL_PATH = "thumbnail_local_path"
        const val COL_PREVIEW_URL = "preview_url"
        const val COL_PREVIEW_LOCAL_PATH = "preview_local_path"
        const val COL_OVERLAY_URL = "overlay_url"
        const val COL_OVERLAY_LOCAL_PATH = "overlay_local_path"
        const val COL_CONFIG_JSON = "config_json"
        const val COL_SLOTS_JSON = "slots_json"
    }
}

private fun android.database.Cursor.getStringOrNull(columnName: String): String? {
    val idx = getColumnIndex(columnName)
    if (idx == -1 || isNull(idx)) return null
    return getString(idx)
}

private fun android.database.Cursor.getStringOrEmpty(columnName: String): String {
    return getStringOrNull(columnName).orEmpty()
}

private fun android.database.Cursor.getIntOrZero(columnName: String): Int {
    val idx = getColumnIndex(columnName)
    if (idx == -1 || isNull(idx)) return 0
    return getInt(idx)
}
