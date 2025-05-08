package com.example.test2.data.repository.impl

import com.example.test2.data.model.TimeEntry
import com.example.test2.data.repository.TimeEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 时间条目仓库实现
 */
@Singleton
class TimeEntryRepositoryImpl @Inject constructor(
    // 这里需要添加DAO依赖，暂时先使用模拟数据
) : TimeEntryRepository {
    
    // 模拟内存存储
    private val inMemoryTimeEntries = mutableListOf<TimeEntry>()
    
    override suspend fun getAllTimeEntries(): List<TimeEntry> = withContext(Dispatchers.IO) {
        return@withContext inMemoryTimeEntries.toList()
    }
    
    override suspend fun getTimeEntriesByDateRange(startDate: Date, endDate: Date): List<TimeEntry> = withContext(Dispatchers.IO) {
        return@withContext inMemoryTimeEntries.filter { entry ->
            val entryDate = entry.startTime
            entryDate >= startDate && (entry.endTime ?: Date()) <= endDate
        }
    }
    
    override suspend fun getOngoingTimeEntry(): TimeEntry? = withContext(Dispatchers.IO) {
        return@withContext inMemoryTimeEntries.find { it.isOngoing() }
    }
    
    override suspend fun insertTimeEntry(timeEntry: TimeEntry): Long = withContext(Dispatchers.IO) {
        inMemoryTimeEntries.add(timeEntry)
        return@withContext timeEntry.id
    }
    
    override suspend fun updateTimeEntry(timeEntry: TimeEntry) = withContext(Dispatchers.IO) {
        val index = inMemoryTimeEntries.indexOfFirst { it.id == timeEntry.id }
        if (index != -1) {
            inMemoryTimeEntries[index] = timeEntry
        }
    }
    
    override suspend fun deleteTimeEntry(id: Long): Unit = withContext(Dispatchers.IO) {
        inMemoryTimeEntries.removeIf { it.id == id }
    }
} 