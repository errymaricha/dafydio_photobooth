package com.errymaricha.dafydiobooth.station.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryTemplateDao : TemplateDao {
    private val items = MutableStateFlow<List<TemplateEntity>>(emptyList())

    override fun observeTemplates(): Flow<List<TemplateEntity>> = items

    override suspend fun upsertAll(items: List<TemplateEntity>) {
        this.items.value = items
    }
}

class InMemoryOfflineQueueDao : OfflineQueueDao {
    private val items = mutableListOf<OfflineQueueEntity>()
    private var nextId = 1L

    override suspend fun pending(): List<OfflineQueueEntity> = items.toList()

    override suspend fun enqueue(item: OfflineQueueEntity) {
        val newItem = item.copy(id = if (item.id == 0L) nextId++ else item.id)
        items.removeAll { it.id == newItem.id }
        items.add(newItem)
    }

    override suspend fun delete(id: Long) {
        items.removeAll { it.id == id }
    }

    override suspend fun deleteAll() {
        items.clear()
    }
}
