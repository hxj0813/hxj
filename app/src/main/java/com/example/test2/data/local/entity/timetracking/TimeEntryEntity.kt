package com.example.test2.data.local.entity.timetracking

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 时间条目实体类
 * 用于记录所有时间追踪数据
 */
@Entity(
    tableName = "time_entries",
    indices = [
        Index(value = ["start_time"]),
        Index(value = ["category"]),
        Index(value = ["task_id"]),
        Index(value = ["habit_id"]),
        Index(value = ["is_pomodoro"]),
        Index(value = ["is_check_in"])
    ]
)
data class TimeEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 基本信息
    val title: String,                      // 时间条目标题
    val description: String? = null,        // 描述（可选）
    @ColumnInfo(name = "category")
    val category: String,                   // 分类（对应TimeCategory枚举）
    
    // 时间信息
    @ColumnInfo(name = "start_time")
    val startTime: Long,                    // 开始时间（毫秒时间戳）
    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,              // 结束时间（毫秒时间戳，null表示正在进行）
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Long = 0,          // 持续时间（秒）
    
    // 关联信息
    @ColumnInfo(name = "task_id")
    val taskId: Long? = null,               // 关联的任务ID（可选）
    @ColumnInfo(name = "habit_id")
    val habitId: Long? = null,              // 关联的习惯ID（可选）
    @ColumnInfo(name = "goal_id")
    val goalId: Long? = null,               // 关联的目标ID（可选）
    
    // 番茄钟相关
    @ColumnInfo(name = "pomodoro_count")
    val pomodoroCount: Int = 0,             // 完成的番茄钟数量
    @ColumnInfo(name = "is_pomodoro")
    val isPomodoro: Boolean = false,        // 是否为番茄钟记录
    
    // 打卡相关
    @ColumnInfo(name = "is_check_in")
    val isCheckIn: Boolean = false,         // 是否为打卡记录
    
    // 标签（以JSON字符串存储）
    @ColumnInfo(name = "tags")
    val tags: String = "[]",                // 标签列表（JSON格式）
    
    // 元数据
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_manual")
    val isManual: Boolean = false,          // 是否为手动记录（而非自动记录）
    
    // 同步相关
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,          // 是否已同步到云端
    @ColumnInfo(name = "sync_id")
    val syncId: String? = null              // 云端同步ID
) 