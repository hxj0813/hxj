package com.example.test2.presentation.timetracking

import com.example.test2.data.model.TimeCategory
import java.util.Date

/**
 * 时间统计数据
 */
data class TimeStatistics(
    val totalTrackedSeconds: Long = 0L,
    val totalPomodoros: Int = 0,
    val categoryBreakdown: Map<String, Long> = emptyMap(),
    val dateRange: Pair<Date, Date>? = null,
    val dailyAverage: Long = 0L,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val mostTrackedCategory: TimeCategory? = null
) {
    /**
     * 获取格式化的总追踪时间
     * @return 格式化的时间字符串
     */
    fun getFormattedTotalTime(): String {
        val hours = totalTrackedSeconds / 3600
        val minutes = (totalTrackedSeconds % 3600) / 60
        
        return if (hours > 0) {
            "${hours}小时${minutes}分钟"
        } else {
            "${minutes}分钟"
        }
    }
    
    /**
     * 获取分类占比
     * @return 分类及其占总时间的百分比
     */
    fun getCategoryPercentages(): Map<String, Float> {
        if (totalTrackedSeconds <= 0) return emptyMap()
        
        return categoryBreakdown.mapValues { (_, seconds) ->
            (seconds.toFloat() / totalTrackedSeconds) * 100
        }
    }
} 