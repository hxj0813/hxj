package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import java.time.ZoneId
import com.example.test2.data.local.entity.FrequencyType

/**
 * 习惯类别枚举
 */
enum class HabitCategory {
    HEALTH,      // 健康
    EXERCISE,    // 运动
    STUDY,       // 学习
    WORK,        // 工作
    MINDFULNESS, // 心灵成长
    SKILL,       // 技能培养
    SOCIAL,      // 社交
    OTHER;       // 其他
    
    companion object {
        fun fromInt(value: Int): HabitCategory {
            return values().getOrElse(value) { OTHER }
        }
    }
}

/**
 * 习惯优先级枚举
 */
enum class HabitPriority {
    HIGH,        // 高优先级
    MEDIUM,      // 中优先级
    LOW;         // 低优先级
    
    companion object {
        fun fromInt(value: Int): HabitPriority {
            return values().getOrElse(value) { MEDIUM }
        }
    }
}

/**
 * 习惯实体类
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val category: Int, // 存储HabitCategory的ordinal
    val icon: String? = null,
    val color: Long = 0xFF4CAF50, // 默认绿色
    
    val frequencyType: Int, // 存储FrequencyType的ordinal
    val frequencyCount: Int = 1,
    val frequencyDaysJson: String? = null, // JSON格式存储天数
    val timeOfDay: Date? = null,
    val reminder: Boolean = false,
    val reminderTime: Date? = null,
    
    val startDate: Date = Date(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val completedToday: Boolean = false,
    val lastCompletedDate: Date? = null,
    
    val isArchived: Boolean = false,
    val priority: Int = HabitPriority.MEDIUM.ordinal,
    val notes: String? = null,
    val associatedGoalId: Long? = null,
    val tagsJson: String? = null, // JSON格式存储标签
    
    val skipDatesJson: String? = null, // JSON格式存储跳过日期
    val milestones: String? = null, // JSON格式存储里程碑值
    val unlockedBadgesCount: Int = 0, // 此习惯解锁的徽章数量
    val unlockedBadgesJson: String? = null, // JSON格式存储已解锁的徽章ID列表
    
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    // 辅助函数，将JSON字符串转换为频率天数列表
    fun getFrequencyDaysList(): List<Int> {
        return if (frequencyDaysJson.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                Gson().fromJson(frequencyDaysJson, object : TypeToken<List<Int>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // 获取标签列表
    fun getTagsList(): List<String> {
        return if (tagsJson.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                Gson().fromJson(tagsJson, object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // 获取跳过日期列表
    fun getSkipDatesList(): List<Date> {
        return if (skipDatesJson.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                Gson().fromJson(skipDatesJson, object : TypeToken<List<Date>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // 获取里程碑列表
    fun getMilestonesList(): List<Int> {
        return if (milestones.isNullOrEmpty()) {
            listOf(7, 21, 66, 100, 365) // 默认里程碑
        } else {
            try {
                Gson().fromJson(milestones, object : TypeToken<List<Int>>() {}.type)
            } catch (e: Exception) {
                listOf(7, 21, 66, 100, 365)
            }
        }
    }
    
    // 获取已解锁徽章ID列表
    fun getUnlockedBadgesList(): List<String> {
        return if (unlockedBadgesJson.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                Gson().fromJson(unlockedBadgesJson, object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // 获取习惯类别
    fun getCategoryEnum(): HabitCategory {
        return HabitCategory.fromInt(category)
    }
    
    // 获取频率类型
    fun getFrequencyTypeEnum(): FrequencyType {
        return FrequencyType.fromInt(frequencyType)
    }
    
    // 获取优先级
    fun getPriorityEnum(): HabitPriority {
        return HabitPriority.fromInt(priority)
    }

    // 计算完成率
    fun getCompletionRate(): Float {
        val daysSinceStart = ChronoUnit.DAYS.between(
            startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        )
        
        if (daysSinceStart <= 0) return 0f
        return totalCompletions.toFloat() / daysSinceStart.toFloat()
    }
    
    // 检查今天是否应该完成此习惯
    fun shouldCompleteToday(): Boolean {
        // 存档的习惯不需要完成
        if (isArchived) return false
        
        val today = Date()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = today
        
        return when (getFrequencyTypeEnum()) {
            FrequencyType.DAILY -> true
            FrequencyType.WEEKLY -> {
                // 检查今天是否在频率天数列表中
                val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1 // 调整为0-6
                getFrequencyDaysList().contains(dayOfWeek)
            }
            FrequencyType.MONTHLY -> {
                // 检查今天是否是指定的月份日期
                val dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                getFrequencyDaysList().contains(dayOfMonth)
            }
            FrequencyType.CUSTOM -> false // 自定义频率需要特殊处理
        }
    }
    
    // 转换为领域模型对象（此处略去，实际应用中需要定义Habit领域模型类）
    
    companion object {
        // 从领域模型创建实体对象（此处略去，实际应用中需要实现）
        
        // 创建习惯实体的工厂方法
        fun create(
            title: String,
            description: String? = null,
            category: HabitCategory = HabitCategory.OTHER,
            frequencyType: FrequencyType = FrequencyType.DAILY,
            frequencyDays: List<Int> = emptyList(),
            priority: HabitPriority = HabitPriority.MEDIUM
        ): HabitEntity {
            return HabitEntity(
                title = title,
                description = description,
                category = category.ordinal,
                frequencyType = frequencyType.ordinal,
                frequencyDaysJson = Gson().toJson(frequencyDays),
                priority = priority.ordinal
            )
        }
    }
} 