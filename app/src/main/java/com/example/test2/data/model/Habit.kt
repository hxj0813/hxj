package com.example.test2.data.model

import java.util.Date
import java.util.UUID

/**
 * 习惯养成频率类型
 */
enum class HabitFrequency {
    DAILY,      // 每天
    WEEKDAYS,   // 工作日
    WEEKLY,     // 每周
    MONTHLY,    // 每月
    CUSTOM      // 自定义
}

/**
 * 习惯类型
 */
enum class HabitCategory {
    HEALTH,     // 健康
    STUDY,      // 学习
    WORK,       // 工作
    SPORTS,     // 运动
    READING,    // 阅读
    MEDITATION, // 冥想
    OTHER       // 其他
}

/**
 * 习惯数据模型
 *
 * @property id 习惯ID
 * @property name 习惯名称
 * @property description 习惯描述
 * @property color 习惯颜色
 * @property icon 习惯图标
 * @property frequency 频率，例如每天、每周几次等
 * @property reminderTime 提醒时间
 * @property startDate 开始日期
 * @property currentStreak 当前连续天数
 * @property bestStreak 最佳连续天数
 * @property totalCompletions 总完成次数
 * @property completedToday 今天是否已完成
 * @property lastCompletedDate 最后完成日期
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
data class Habit(
    val id: String,
    val name: String,
    val description: String = "",
    val color: Int = 0xFF4CAF50.toInt(), // 默认绿色
    val icon: String? = null,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val reminderTime: Date? = null,
    val startDate: Date = Date(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val completedToday: Boolean = false,
    val lastCompletedDate: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * 今天是否已打卡
     */
    fun isCheckedInToday(): Boolean {
        return completedToday
    }
    
    /**
     * 计算进度百分比
     * 
     * @param targetDays 目标天数，默认为21天习惯养成周期
     * @return 完成百分比（0.0-1.0）
     */
    fun calculateProgress(targetDays: Int = 21): Float {
        return (totalCompletions.toFloat() / targetDays).coerceIn(0f, 1f)
    }
    
    /**
     * 判断是否已完成目标天数
     * 
     * @param targetDays 目标天数，默认为21天习惯养成周期
     * @return 是否完成目标
     */
    fun isCompleted(targetDays: Int = 21): Boolean {
        return totalCompletions >= targetDays
    }
    
    /**
     * 计算习惯已开始的天数
     */
    fun daysSinceStart(): Int {
        val today = Date()
        val diffTime = today.time - startDate.time
        return (diffTime / (24 * 60 * 60 * 1000)).toInt()
    }
    
    companion object {
        /**
         * 判断两个日期是否是同一天
         */
        fun isSameDay(date1: Date, date2: Date): Boolean {
            val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
            val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
            return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                   cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
        }
    }
} 
