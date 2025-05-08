package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import java.util.UUID

// 导入Enums.kt中的枚举
import com.example.test2.data.local.entity.TaskType
import com.example.test2.data.local.entity.TaskPriority
import com.example.test2.data.local.entity.FrequencyType
import com.example.test2.data.local.entity.TagCategory

/**
 * 任务实体类
 * 定义任务的基本属性
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("goalId"),
        Index("taskType"),
        Index("isCompleted"),
        Index("dueDate")
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // 任务基本信息
    val title: String,
    val description: String? = null,
    val taskType: Int, // 使用TaskType枚举的ordinal值
    val priority: Int = TaskPriority.MEDIUM.ordinal, // 默认中优先级
    val color: Int = 0xFFFF9800.toInt(), // 默认橙色
    val icon: String? = null,
    
    // 任务状态
    val isCompleted: Boolean = false,
    val isArchived: Boolean = false,
    
    // 截止日期
    val dueDate: Date? = null,
    
    // 关联目标ID
    val goalId: Long? = null,
    
    // 时间戳
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val completedAt: Date? = null
) {
    /**
     * 获取任务类型枚举
     */
    fun getTaskTypeEnum(): TaskType {
        return TaskType.fromInt(taskType)
    }
    
    /**
     * 获取任务优先级枚举
     */
    fun getPriorityEnum(): TaskPriority {
        return TaskPriority.fromInt(priority)
    }
    
    /**
     * 判断任务是否已逾期
     */
    fun isOverdue(): Boolean {
        return !isCompleted && dueDate != null && dueDate.before(Date())
    }
    
    /**
     * 判断任务是否为今天
     */
    fun isDueToday(): Boolean {
        if (dueDate == null) return false
        
        val today = java.util.Calendar.getInstance()
        val dueCalendar = java.util.Calendar.getInstance().apply { time = dueDate }
        
        return today.get(java.util.Calendar.YEAR) == dueCalendar.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == dueCalendar.get(java.util.Calendar.DAY_OF_YEAR)
    }
    
    companion object {
        /**
         * 创建一个打卡任务
         */
        fun createCheckInTask(
            title: String,
            description: String? = null,
            priority: TaskPriority = TaskPriority.MEDIUM,
            dueDate: Date? = null,
            goalId: Long? = null,
            color: Int = 0xFFFF9800.toInt(),
            icon: String? = null
        ): TaskEntity {
            return TaskEntity(
                title = title,
                description = description,
                taskType = TaskType.CHECK_IN.ordinal,
                priority = priority.ordinal,
                dueDate = dueDate,
                goalId = goalId,
                color = color,
                icon = icon
            )
        }
        
        /**
         * 创建一个番茄钟任务
         */
        fun createPomodoroTask(
            title: String,
            description: String? = null,
            priority: TaskPriority = TaskPriority.MEDIUM,
            dueDate: Date? = null,
            goalId: Long? = null,
            color: Int = 0xFF4CAF50.toInt(),
            icon: String? = null
        ): TaskEntity {
            return TaskEntity(
                title = title,
                description = description,
                taskType = TaskType.POMODORO.ordinal,
                priority = priority.ordinal,
                dueDate = dueDate,
                goalId = goalId,
                color = color,
                icon = icon
            )
        }
    }
}
