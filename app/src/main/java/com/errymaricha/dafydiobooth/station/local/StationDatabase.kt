package com.errymaricha.dafydiobooth.station.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val templateName: String,
    val templateCode: String,
    val paperSize: String?,
    val previewUrl: String?,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "offline_queue")
data class OfflineQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val endpoint: String,
    val method: String,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY updatedAt DESC")
    fun observeTemplates(): Flow<List<TemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TemplateEntity>)
}

@Dao
interface OfflineQueueDao {
    @Query("SELECT * FROM offline_queue ORDER BY createdAt ASC")
    suspend fun pending(): List<OfflineQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: OfflineQueueEntity)

    @Query("DELETE FROM offline_queue WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM offline_queue")
    suspend fun deleteAll()
}

@Database(entities = [TemplateEntity::class, OfflineQueueEntity::class], version = 1, exportSchema = false)
abstract class StationDatabase : RoomDatabase() {
    abstract fun templateDao(): TemplateDao
    abstract fun offlineQueueDao(): OfflineQueueDao
}
