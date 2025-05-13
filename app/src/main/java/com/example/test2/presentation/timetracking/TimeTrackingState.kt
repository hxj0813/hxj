package com.example.test2.presentation.timetracking

import com.example.test2.data.local.entity.timetracking.TimeGoalEntity
import com.example.test2.data.local.entity.timetracking.TimeTagEntity
import com.example.test2.data.model.Task
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import java.util.Date

/**
 * 时间追踪状态
 */
data class TimeTrackingState(
    // 核心数据
    val timeEntries: List<TimeEntry> = emptyList(),
    val allTasks: List<Task> = emptyList(),
    val ongoingEntry: TimeEntry? = null,
    val selectedEntry: TimeEntry? = null,
    val allTags: List<TimeTagEntity> = emptyList(),
    val activeGoals: List<TimeGoalEntity> = emptyList(),
    
    // 筛选条件
    val selectedDate: Date = Date(),
    val dateRange: Pair<Date, Date>? = null,
    
    // UI状态
    val isLoading: Boolean = true,
    val error: String? = null,
    val showEntryDialog: Boolean = false,
    val statistics: TimeStatistics = TimeStatistics(),
    
    // 番茄钟相关状态
    val currentTask: Task? = null,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isBreakTime: Boolean = false,
    val remainingTimeInSeconds: Int = 0,
    val totalTimeInSeconds: Int = 0,
    val currentSession: Int = 1,
    val totalSessions: Int = 4,
    val sessionCompleted: Boolean = false,
    val sessionNotes: String = ""
) {
    /**
     * 获取指定日期的时间条目
     */
    fun getEntriesForDate(date: Date): List<TimeEntry> {
        return timeEntries.filter { entry ->
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
        return timeEntries
            .filter { it.category == category }
            .sumOf { it.duration }
    }
    
    companion object {
        /**
         * 创建初始状态
         */
        fun initial() = TimeTrackingState(
            isLoading = true
        )
    }
} 