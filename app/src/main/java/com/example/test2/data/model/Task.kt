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
 * 任务数据模型
 *
 * @property id 任务ID
 * @property title 任务标题
 * @property description 任务描述
 * @property priority 任务优先级
 * @property status 任务状态
 * @property dueDate 截止日期
 * @property goalId 关联的目标ID，可为null表示独立任务
 * @property goalTitle 关联的目标标题，可为null表示独立任务
 * @property isCompleted 任务是否已完成
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
data class Task(
    val id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
    val title: String,
    val description: String? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: Date? = null,
    val goalId: Long? = null,
    val goalTitle: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
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
     * 创建任务副本
     */
    fun copy(
        id: Long = this.id,
        title: String = this.title,
        description: String? = this.description,
        priority: TaskPriority = this.priority,
        dueDate: Date? = this.dueDate,
        goalId: Long? = this.goalId,
        goalTitle: String? = this.goalTitle,
        isCompleted: Boolean = this.isCompleted
    ): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            goalId = goalId,
            goalTitle = goalTitle,
            isCompleted = isCompleted,
            createdAt = this.createdAt,
            updatedAt = Date()
        )
    }
} 