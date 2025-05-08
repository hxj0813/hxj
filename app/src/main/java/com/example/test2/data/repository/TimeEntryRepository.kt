package com.example.test2.data.repository

import com.example.test2.data.model.TimeEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 时间条目仓库接口
 */
interface TimeEntryRepository {
    /**
     * 获取所有时间条目
     */
    suspend fun getAllTimeEntries(): List<TimeEntry>
    
    /**
     * 按日期范围获取时间条目
     */
    suspend fun getTimeEntriesByDateRange(startDate: Date, endDate: Date): List<TimeEntry>
    
    /**
     * 获取正在进行中的时间条目
     */
    suspend fun getOngoingTimeEntry(): TimeEntry?
    
    /**
     * 插入时间条目
     */
    suspend fun insertTimeEntry(timeEntry: TimeEntry): Long
    
    /**
     * 更新时间条目
     */
    suspend fun updateTimeEntry(timeEntry: TimeEntry)
    
    /**
     * 删除时间条目
     */
    suspend fun deleteTimeEntry(id: Long)
} 