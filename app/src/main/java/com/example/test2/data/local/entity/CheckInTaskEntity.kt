package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import com.example.test2.data.local.entity.FrequencyType
import com.example.test2.data.local.entity.TaskEntity

/**
 * 打卡任务实体类
 * 定义打卡任务的特有属性
 */
@Entity(
    tableName = "check_in_tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CheckInTaskEntity(
    @PrimaryKey
    val taskId: String, // 关联的基础任务ID
    
    // 频率相关
    val frequencyType: Int = FrequencyType.DAILY.ordinal, // 频率类型
    val frequencyCount: Int = 1, // 频率次数（每天几次、每周几天、每月几天）
    val frequencyDaysJson: String? = null, // 指定的天数（JSON格式）
    
    // 统计数据
    val currentStreak: Int = 0, // 当前连续打卡
    val bestStreak: Int = 0, // 最佳连续打卡
    val totalCompletions: Int = 0, // 总完成次数
    
    // 状态相关
    val completedToday: Boolean = false, // 今日是否已完成
    val lastCompletedDate: Date? = null, // 最后一次完成时间
    val streakStartDate: Date? = null, // 连续打卡开始日期
    
    // 提醒设置
    val reminderEnabled: Boolean = false, // 是否启用提醒
    val reminderTime: Date? = null // 提醒时间
) {
    /**
     * 获取频率类型枚举
     */
    fun getFrequencyTypeEnum(): FrequencyType {
        return FrequencyType.fromInt(frequencyType)
    }
    
    /**
     * 获取频率天数列表
     */
    fun getFrequencyDaysList(): List<Int> {
        if (frequencyDaysJson.isNullOrEmpty()) return emptyList()
        
        return try {
            val type = object : TypeToken<List<Int>>() {}.type
            Gson().fromJson(frequencyDaysJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 判断今天是否需要完成
     */
    fun isDueToday(): Boolean {
        if (completedToday) return false
        
        val today = java.util.Calendar.getInstance()
        
        return when(getFrequencyTypeEnum()) {
            FrequencyType.DAILY -> true // 每天都需要完成
            FrequencyType.WEEKLY -> {
                // 检查今天是否是指定的完成日
                val dayOfWeek = today.get(java.util.Calendar.DAY_OF_WEEK) - 1 // 转换为0-6表示
                getFrequencyDaysList().contains(dayOfWeek)
            }
            FrequencyType.MONTHLY -> {
                // 检查今天的日期是否是指定的完成日
                val dayOfMonth = today.get(java.util.Calendar.DAY_OF_MONTH)
                getFrequencyDaysList().contains(dayOfMonth)
            }
            FrequencyType.CUSTOM -> {
                // 自定义频率需要特殊处理
                // 简化实现：如果今天距离上次完成超过 frequencyCount 天，则需要完成
                if (lastCompletedDate == null) return true
                
                val lastCompletedCalendar = java.util.Calendar.getInstance().apply { time = lastCompletedDate }
                val daysDiff = (today.timeInMillis - lastCompletedCalendar.timeInMillis) / (24 * 60 * 60 * 1000)
                daysDiff >= frequencyCount
            }
        }
    }
    
    /**
     * 获取完成进度
     */
    fun getCompletionProgress(): Float {
        return when(getFrequencyTypeEnum()) {
            FrequencyType.DAILY -> if (completedToday) 1f else 0f
            FrequencyType.WEEKLY -> {
                val completedDays = if (completedToday) 1 else 0
                completedDays.toFloat() / frequencyCount.toFloat()
            }
            FrequencyType.MONTHLY -> {
                val completedDays = if (completedToday) 1 else 0
                completedDays.toFloat() / frequencyCount.toFloat()
            }
            FrequencyType.CUSTOM -> if (completedToday) 1f else 0f
        }
    }
    
    companion object {
        /**
         * 创建一个每日打卡任务
         */
        fun createDaily(
            taskId: String,
            reminderEnabled: Boolean = false,
            reminderTime: Date? = null
        ): CheckInTaskEntity {
            return CheckInTaskEntity(
                taskId = taskId,
                frequencyType = FrequencyType.DAILY.ordinal,
                frequencyCount = 1,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime
            )
        }
        
        /**
         * 创建一个每周打卡任务
         */
        fun createWeekly(
            taskId: String,
            daysOfWeek: List<Int>, // 0-6 表示周日至周六
            reminderEnabled: Boolean = false,
            reminderTime: Date? = null
        ): CheckInTaskEntity {
            val daysJson = Gson().toJson(daysOfWeek)
            
            return CheckInTaskEntity(
                taskId = taskId,
                frequencyType = FrequencyType.WEEKLY.ordinal,
                frequencyCount = daysOfWeek.size,
                frequencyDaysJson = daysJson,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime
            )
        }
        
        /**
         * 创建一个每月打卡任务
         */
        fun createMonthly(
            taskId: String,
            daysOfMonth: List<Int>, // 1-31 表示每月的日期
            reminderEnabled: Boolean = false,
            reminderTime: Date? = null
        ): CheckInTaskEntity {
            val daysJson = Gson().toJson(daysOfMonth)
            
            return CheckInTaskEntity(
                taskId = taskId,
                frequencyType = FrequencyType.MONTHLY.ordinal,
                frequencyCount = daysOfMonth.size,
                frequencyDaysJson = daysJson,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime
            )
        }
        
        /**
         * 创建一个自定义间隔的打卡任务
         */
        fun createCustom(
            taskId: String,
            intervalDays: Int, // 间隔天数
            reminderEnabled: Boolean = false,
            reminderTime: Date? = null
        ): CheckInTaskEntity {
            return CheckInTaskEntity(
                taskId = taskId,
                frequencyType = FrequencyType.CUSTOM.ordinal,
                frequencyCount = intervalDays,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime
            )
        }
    }
} 