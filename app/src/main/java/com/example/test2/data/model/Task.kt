package com.example.test2.data.model

import com.example.test2.util.DateTimeUtil
import java.util.Date
import java.util.UUID

/**
 * 任务优先级枚举
 */
enum class TaskPriority {
    LOW, 
    MEDIUM, 
    HIGH;
    
    companion object {
        fun fromInt(value: Int): TaskPriority {
            return when (value) {
                0 -> LOW
                1 -> MEDIUM
                2 -> HIGH
                else -> MEDIUM
            }
        }
        
        fun toInt(priority: TaskPriority): Int {
            return when (priority) {
                LOW -> 0
                MEDIUM -> 1
                HIGH -> 2
            }
        }
    }
}

/**
 * 任务状态枚举
 */
enum class TaskStatus {
    TODO,        // 待办
    IN_PROGRESS, // 进行中
    COMPLETED,   // 已完成
    CANCELLED    // 已取消
}

/**
 * 任务类型枚举
 */
enum class TaskType {
    CHECK_IN,    // 打卡任务
    POMODORO;    // 番茄钟任务
    
    companion object {
        fun fromInt(value: Int): TaskType {
            return when (value) {
                0 -> CHECK_IN
                1 -> POMODORO
                else -> CHECK_IN
            }
        }
        
        fun toInt(type: TaskType): Int {
            return when (type) {
                CHECK_IN -> 0
                POMODORO -> 1
            }
        }
    }
}

/**
 * 打卡频率类型枚举
 */
enum class CheckInFrequencyType {
    DAILY,       // 每日打卡次数
    WEEKLY;      // 每周打卡天数
    
    companion object {
        fun fromInt(value: Int): CheckInFrequencyType {
            return when (value) {
                0 -> DAILY
                1 -> WEEKLY
                else -> DAILY
            }
        }
        
        fun toInt(type: CheckInFrequencyType): Int {
            return when (type) {
                DAILY -> 0
                WEEKLY -> 1
            }
        }
    }
}

/**
 * 番茄钟标签枚举
 */
enum class PomodoroTag {
    STUDY,      // 学习
    EXERCISE,   // 运动
    WORK,       // 工作
    CUSTOM;     // 自定义
    
    companion object {
        fun fromInt(value: Int): PomodoroTag {
            return when (value) {
                0 -> STUDY
                1 -> EXERCISE
                2 -> WORK
                3 -> CUSTOM
                else -> STUDY
            }
        }
        
        fun toInt(tag: PomodoroTag): Int {
            return when (tag) {
                STUDY -> 0
                EXERCISE -> 1
                WORK -> 2
                CUSTOM -> 3
            }
        }
    }
    
    fun getDisplayName(): String {
        return when (this) {
            STUDY -> "学习"
            EXERCISE -> "运动"
            WORK -> "工作"
            CUSTOM -> "自定义"
        }
    }
    
    fun getColor(): Long {
        return when (this) {
            STUDY -> 0xFF4CAF50 // 绿色
            EXERCISE -> 0xFFFF9800 // 橙色
            WORK -> 0xFF2196F3 // 蓝色
            CUSTOM -> 0xFF9C27B0 // 紫色
        }
    }
}

/**
 * 打卡任务设置
 */
data class CheckInSettings(
    val frequencyType: CheckInFrequencyType = CheckInFrequencyType.DAILY,
    val frequency: Int = 1,  // 打卡次数(每日)或天数(每周)
    val dailyDeadline: Date? = null,  // 每日打卡截止时间
    val currentStreak: Int = 0,  // 当前连续打卡天数
    val bestStreak: Int = 0,  // 最佳连续打卡天数
    val completedToday: Int = 0,  // 今日已完成次数
    val completedThisWeek: Int = 0,  // 本周已完成天数
    val lastCheckInDate: Date? = null, // 最近一次打卡日期
    val totalCompletions: Int = 0  // 总打卡次数
)

/**
 * 番茄钟任务设置
 */
data class PomodoroSettings(
    val focusMinutes: Int = 25,  // 专注时长(分钟)
    val shortBreakMinutes: Int = 5,  // 短休息时长(分钟)
    val longBreakMinutes: Int = 15,  // 长休息时长(分钟)
    val sessionsBeforeLongBreak: Int = 4,  // 长休息前的番茄钟数量
    val totalCompletedSessions: Int = 0,  // 总完成番茄钟次数
    val todayCompletedSessions: Int = 0,  // 今日完成番茄钟次数
    val totalFocusMinutes: Int = 0,  // 总专注时间(分钟)
    val tag: PomodoroTag = PomodoroTag.STUDY,  // 番茄钟标签
    val customTagName: String? = null  // 自定义标签名称，仅在tag为CUSTOM时有效
)

/**
 * 任务数据模型
 *
 * @property id 任务ID
 * @property title 任务标题
 * @property description 任务描述
 * @property type 任务类型
 * @property priority 任务优先级
 * @property status 任务状态
 * @property dueDate 截止日期
 * @property goalId 关联的目标ID，可为null表示独立任务
 * @property goalTitle 关联的目标标题，可为null表示独立任务
 * @property habitId 关联的习惯ID，可为null表示独立任务
 * @property habitTitle 关联的习惯标题，可为null表示独立任务
 * @property isCompleted 任务是否已完成
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 * @property checkInSettings 打卡任务设置，仅当type为CHECK_IN时有效
 * @property pomodoroSettings 番茄钟任务设置，仅当type为POMODORO时有效
 */
data class Task(
    val id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
    val title: String,
    val description: String? = null,
    val type: TaskType = TaskType.CHECK_IN,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: Date? = null,
    val goalId: Long? = null,
    val goalTitle: String? = null,
    val habitId: String? = null,
    val habitTitle: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val checkInSettings: CheckInSettings? = if (type == TaskType.CHECK_IN) CheckInSettings() else null,
    val pomodoroSettings: PomodoroSettings? = if (type == TaskType.POMODORO) PomodoroSettings() else null
) {
    /**
     * 检查任务是否逾期
     */
    fun isOverdue(): Boolean {
        if (isCompleted || dueDate == null) return false
        return DateTimeUtil.isOverdue(dueDate)
    }
    
    /**
     * 检查任务是否即将到期 (3天内)
     */
    fun isUpcoming(): Boolean {
        if (isCompleted || dueDate == null) return false
        return DateTimeUtil.isUpcoming(dueDate)
    }
    
    /**
     * 检查是否是今天的任务
     */
    fun isDueToday(): Boolean {
        if (dueDate == null) return false
        
        val today = Date()
        return DateTimeUtil.isSameDay(today, dueDate)
    }
    
    /**
     * 判断任务是否是高重要性任务
     */
    fun isHighImportance(): Boolean = priority == TaskPriority.HIGH
    
    /**
     * 获取任务优先级颜色
     */
    fun getPriorityColorHex(): String = when (priority) {
        TaskPriority.LOW -> "#8BC34A"      // 浅绿色
        TaskPriority.MEDIUM -> "#4FC3F7"   // 浅蓝色
        TaskPriority.HIGH -> "#FF9800"     // 橙色
    }
    
    /**
     * 获取任务状态
     */
    fun getStatus(): TaskStatus {
        return when {
            isCompleted -> TaskStatus.COMPLETED
            else -> TaskStatus.TODO
        }
    }
    
    /**
     * 获取任务类型描述
     */
    fun getTypeDescription(): String {
        return when (type) {
            TaskType.CHECK_IN -> "打卡任务"
            TaskType.POMODORO -> "番茄钟任务"
        }
    }
    
    /**
     * 检查今天是否已完成打卡
     * 仅适用于打卡任务
     */
    fun isCheckInCompletedToday(): Boolean {
        if (type != TaskType.CHECK_IN || checkInSettings == null) return false
        
        return when (checkInSettings.frequencyType) {
            CheckInFrequencyType.DAILY -> 
                checkInSettings.completedToday >= checkInSettings.frequency
            CheckInFrequencyType.WEEKLY -> {
                // 如果今天已经打卡，则返回true
                checkInSettings.lastCheckInDate?.let {
                    DateTimeUtil.isSameDay(it, Date())
                } ?: false
            }
        }
    }
    
    /**
     * 检查本周是否已达成打卡目标
     * 仅适用于打卡任务
     */
    fun isWeeklyGoalAchieved(): Boolean {
        if (type != TaskType.CHECK_IN || 
            checkInSettings == null || 
            checkInSettings.frequencyType != CheckInFrequencyType.WEEKLY) return false
        
        return checkInSettings.completedThisWeek >= checkInSettings.frequency
    }
    
    /**
     * 创建任务副本
     */
    fun copy(
        id: Long = this.id,
        title: String = this.title,
        description: String? = this.description,
        type: TaskType = this.type,
        priority: TaskPriority = this.priority,
        dueDate: Date? = this.dueDate,
        goalId: Long? = this.goalId,
        goalTitle: String? = this.goalTitle,
        habitId: String? = this.habitId,
        habitTitle: String? = this.habitTitle,
        isCompleted: Boolean = this.isCompleted,
        checkInSettings: CheckInSettings? = this.checkInSettings,
        pomodoroSettings: PomodoroSettings? = this.pomodoroSettings
    ): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            type = type,
            priority = priority,
            dueDate = dueDate,
            goalId = goalId,
            goalTitle = goalTitle,
            habitId = habitId,
            habitTitle = habitTitle,
            isCompleted = isCompleted,
            createdAt = this.createdAt,
            updatedAt = Date(),
            checkInSettings = when (type) {
                TaskType.CHECK_IN -> checkInSettings ?: CheckInSettings()
                else -> null
            },
            pomodoroSettings = when (type) {
                TaskType.POMODORO -> pomodoroSettings ?: PomodoroSettings()
                else -> null
            }
        )
    }
} 