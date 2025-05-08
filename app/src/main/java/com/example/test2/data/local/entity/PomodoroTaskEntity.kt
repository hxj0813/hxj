package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date
import com.example.test2.data.local.entity.TagCategory

/**
 * 番茄钟任务表
 * 继承自基础任务表，添加番茄钟任务特有字段
 */
@Entity(
    tableName = "pomodoro_tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["taskId"]
)
data class PomodoroTaskEntity(
    val taskId: String, // 关联基础任务表
    
    // 时间设置字段
    val estimatedPomodoros: Int = 1,
    val completedPomodoros: Int = 0,
    val pomodoroLength: Int = 25,  // 默认25分钟
    val shortBreakLength: Int = 5, // 默认5分钟
    val longBreakLength: Int = 15, // 默认15分钟
    val longBreakInterval: Int = 4, // 默认4个番茄钟后长休息
    
    // 标签字段
    val tagId: String? = null, // 关联到标签表
    val tagCategory: Int = TagCategory.OTHER.ordinal,
    val customTagName: String? = null, // 自定义标签名称
    
    // 统计字段
    val totalFocusTime: Long = 0, // 总专注时间(分钟)
    val lastSessionDate: Date? = null
) {
    /**
     * 获取标签分类枚举
     */
    fun getTagCategoryEnum(): TagCategory {
        return TagCategory.fromInt(tagCategory)
    }
    
    /**
     * 获取完成进度
     */
    fun getCompletionProgress(): Float {
        if (estimatedPomodoros <= 0) return 0f
        return completedPomodoros.toFloat() / estimatedPomodoros.toFloat()
    }
    
    /**
     * 获取已专注小时数
     */
    fun getTotalFocusHours(): Float {
        return totalFocusTime / 60f
    }
    
    /**
     * 判断是否今日已专注
     */
    fun isFocusedToday(): Boolean {
        if (lastSessionDate == null) return false
        
        val today = java.util.Calendar.getInstance()
        val sessionCalendar = java.util.Calendar.getInstance().apply { time = lastSessionDate }
        
        return today.get(java.util.Calendar.YEAR) == sessionCalendar.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == sessionCalendar.get(java.util.Calendar.DAY_OF_YEAR)
    }
    
    companion object {
        /**
         * 创建番茄钟任务实体
         */
        fun create(
            taskId: String,
            estimatedPomodoros: Int = 1,
            pomodoroLength: Int = 25,
            tagCategory: TagCategory = TagCategory.OTHER,
            customTagName: String? = null
        ): PomodoroTaskEntity {
            return PomodoroTaskEntity(
                taskId = taskId,
                estimatedPomodoros = estimatedPomodoros,
                pomodoroLength = pomodoroLength,
                tagCategory = tagCategory.ordinal,
                customTagName = customTagName
            )
        }
        
        /**
         * 创建一个工作类番茄钟任务
         */
        fun createWorkTask(
            taskId: String,
            estimatedPomodoros: Int = 4,
            tagId: String? = null
        ): PomodoroTaskEntity {
            return PomodoroTaskEntity(
                taskId = taskId,
                estimatedPomodoros = estimatedPomodoros,
                tagCategory = TagCategory.WORK.ordinal,
                tagId = tagId
            )
        }
        
        /**
         * 创建一个学习类番茄钟任务
         */
        fun createStudyTask(
            taskId: String,
            estimatedPomodoros: Int = 4,
            tagId: String? = null
        ): PomodoroTaskEntity {
            return PomodoroTaskEntity(
                taskId = taskId,
                estimatedPomodoros = estimatedPomodoros,
                tagCategory = TagCategory.STUDY.ordinal,
                tagId = tagId
            )
        }
        
        /**
         * 创建一个自定义类型的番茄钟任务
         */
        fun createCustomTask(
            taskId: String,
            estimatedPomodoros: Int = 4,
            pomodoroLength: Int = 25,
            shortBreakLength: Int = 5,
            longBreakLength: Int = 15,
            tagCategory: TagCategory = TagCategory.OTHER,
            tagId: String? = null,
            customTagName: String? = null
        ): PomodoroTaskEntity {
            return PomodoroTaskEntity(
                taskId = taskId,
                estimatedPomodoros = estimatedPomodoros,
                pomodoroLength = pomodoroLength,
                shortBreakLength = shortBreakLength,
                longBreakLength = longBreakLength,
                tagCategory = tagCategory.ordinal,
                tagId = tagId,
                customTagName = customTagName
            )
        }
    }
} 