package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * 任务日志表
 * 记录任务的完成历史
 */
@Entity(
    tableName = "task_logs",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId"), Index("completedDate")]
)
data class TaskLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val taskType: Int,
    val completedDate: Date = Date(),
    val focusMinutes: Int? = null,   // 专注时间(分钟)
    val pomodoroCount: Int? = null,  // 完成的番茄钟数量
    val note: String? = null,        // 备注
    val mood: Int? = null            // 心情评分(1-5)
) {
    /**
     * 获取任务类型枚举
     */
    fun getTaskTypeEnum(): TaskType {
        return TaskType.fromInt(taskType)
    }
    
    /**
     * 获取格式化的完成日期（仅日期部分）
     */
    fun getFormattedDate(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formatter.format(completedDate)
    }
    
    /**
     * 获取格式化的完成时间（时间部分）
     */
    fun getFormattedTime(): String {
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return formatter.format(completedDate)
    }
    
    companion object {
        /**
         * 创建打卡任务日志
         */
        fun createCheckInLog(
            taskId: String,
            note: String? = null,
            mood: Int? = null
        ): TaskLogEntity {
            return TaskLogEntity(
                taskId = taskId,
                taskType = TaskType.CHECK_IN.ordinal,
                note = note,
                mood = mood
            )
        }
        
        /**
         * 创建番茄钟任务日志
         */
        fun createPomodoroLog(
            taskId: String,
            focusMinutes: Int,
            pomodoroCount: Int = 1,
            note: String? = null,
            mood: Int? = null
        ): TaskLogEntity {
            return TaskLogEntity(
                taskId = taskId,
                taskType = TaskType.POMODORO.ordinal,
                focusMinutes = focusMinutes,
                pomodoroCount = pomodoroCount,
                note = note,
                mood = mood
            )
        }
    }
} 