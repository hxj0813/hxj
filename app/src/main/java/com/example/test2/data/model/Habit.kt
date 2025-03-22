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
    MONTHLY     // 每月
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
 * 习惯成就徽章类型
 */
enum class HabitBadgeType {
    STARTER,        // 初学者（开始一个新习惯）
    PERSISTENT,     // 坚持者（连续7天）
    DEDICATED,      // 专注者（连续30天）
    MASTER,         // 大师（连续100天）
    COMEBACK,       // 回归者（中断后重新开始）
    CONSISTENT,     // 稳定者（完成率80%以上）
    EARLY_BIRD,     // 早起鸟（清晨完成习惯）
    NIGHT_OWL,      // 夜猫子（晚上完成习惯）
    SOCIAL,         // 社交达人（分享习惯）
    MILESTONE       // 里程碑（自定义成就）
}

/**
 * 习惯成就徽章数据类
 */
data class HabitBadge(
    val id: String = UUID.randomUUID().toString(),
    val type: HabitBadgeType,
    val title: String,
    val description: String,
    val iconUrl: String,
    val unlockedAt: Date? = null,
    val isUnlocked: Boolean = false
)

/**
 * 习惯数据模型
 */
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val title: String,                              // 习惯标题
    val description: String? = null,                // 习惯描述
    val category: HabitCategory = HabitCategory.OTHER, // 习惯类别
    val frequency: HabitFrequency = HabitFrequency.DAILY, // 打卡频率
    val targetDays: Int = 21,                       // 目标天数（默认21天形成习惯）
    val isRemindable: Boolean = false,              // 是否需要提醒
    val reminderTime: Date? = null,                 // 提醒时间
    val startDate: Date = Date(),                   // 开始日期
    val checkInRecords: List<Date> = emptyList(),   // 打卡记录
    val badges: List<HabitBadge> = emptyList(),     // 获得的成就徽章
    val color: Long = 0xFF4A90E2,                   // 习惯颜色（默认蓝色）
    val icon: String = "default_habit",             // 习惯图标
    val currentStreak: Int = 0,                     // 当前连续打卡天数
    val longestStreak: Int = 0,                     // 最长连续打卡天数
    val totalCheckIns: Int = 0,                     // 总打卡次数
    val completionRate: Float = 0f,                 // 完成率
    val notes: List<HabitNote> = emptyList(),       // 习惯笔记
    val isArchived: Boolean = false,                // 是否归档
    val createdAt: Date = Date(),                   // 创建时间
    val updatedAt: Date = Date()                    // 更新时间
) {
    /**
     * 今天是否已打卡
     */
    fun isCheckedInToday(): Boolean {
        val today = Date()
        return checkInRecords.any { record ->
            isSameDay(record, today)
        }
    }
    
    /**
     * 计算进度百分比
     */
    fun calculateProgress(): Float {
        return (totalCheckIns.toFloat() / targetDays).coerceIn(0f, 1f)
    }
    
    /**
     * 判断是否已完成目标天数
     */
    fun isCompleted(): Boolean {
        return totalCheckIns >= targetDays
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

/**
 * 习惯笔记数据类
 */
data class HabitNote(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val content: String,
    val mood: Int = 3, // 1-5 表示心情，3为中性
    val createdAt: Date = Date()
) 