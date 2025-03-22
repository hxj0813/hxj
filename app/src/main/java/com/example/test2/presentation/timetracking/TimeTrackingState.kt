package com.example.test2.presentation.timetracking

import com.example.test2.data.model.Task
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import java.util.Date

/**
 * 时间追踪状态
 */
data class TimeTrackingState(
    val timeEntries: List<TimeEntry> = emptyList(),
    val filteredEntries: List<TimeEntry> = emptyList(),
    val allTasks: List<Task> = emptyList(),
    val ongoingEntry: TimeEntry? = null,
    val selectedCategory: TimeCategory? = null,
    val selectedDate: Date = Date(),
    val dateRange: Pair<Date, Date>? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val showEntryDialog: Boolean = false,
    val selectedEntry: TimeEntry? = null,
    val showFilterDialog: Boolean = false,
    val statistics: TimeStatistics = TimeStatistics()
) {
    /**
     * 获取指定日期的时间条目
     */
    fun getEntriesForDate(date: Date): List<TimeEntry> {
        return filteredEntries.filter { entry ->
            val entryDate = entry.startTime
            entryDate.year == date.year && 
            entryDate.month == date.month && 
            entryDate.date == date.date
        }
    }
    
    /**
     * 获取指定分类的总时长（秒）
     */
    fun getTotalDurationByCategory(category: TimeCategory): Long {
        return filteredEntries
            .filter { it.category == category }
            .sumOf { it.duration }
    }
    
    /**
     * 时间追踪统计数据
     */
    data class TimeStatistics(
        val totalDuration: Long = 0,
        val productiveTime: Long = 0,
        val categoryBreakdown: Map<TimeCategory, Long> = emptyMap(),
        val dailyAverage: Long = 0,
        val longestStreak: Int = 0,
        val currentStreak: Int = 0,
        val mostTrackedCategory: TimeCategory? = null
    )
    
    companion object {
        /**
         * 创建初始状态
         */
        fun initial() = TimeTrackingState(
            isLoading = true
        )
    }
} 