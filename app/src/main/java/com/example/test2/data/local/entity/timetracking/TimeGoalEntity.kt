package com.example.test2.data.local.entity.timetracking

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 时间目标实体类
 * 用于设置和跟踪时间目标
 */
@Entity(
    tableName = "time_goals",
    indices = [
        Index(value = ["category"]),
        Index(value = ["reference_type", "reference_id"]),
        Index(value = ["is_completed"])
    ]
)
data class TimeGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,                       // 目标标题
    val description: String? = null,         // 描述
    
    @ColumnInfo(name = "target_seconds")
    val targetSeconds: Long,                 // 目标时间（秒）
    @ColumnInfo(name = "current_seconds")
    val currentSeconds: Long = 0,            // 当前已完成时间（秒）
    
    @ColumnInfo(name = "category")
    val category: String? = null,            // 目标分类
    @ColumnInfo(name = "reference_type")
    val referenceType: String? = null,       // 引用类型
    @ColumnInfo(name = "reference_id")
    val referenceId: Long? = null,           // 引用ID
    
    @ColumnInfo(name = "start_date")
    val startDate: Long,                     // 开始日期
    @ColumnInfo(name = "end_date")
    val endDate: Long? = null,               // 结束日期（可选）
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,        // 是否已完成
    @ColumnInfo(name = "completion_date")
    val completionDate: Long? = null,        // 完成日期
    
    @ColumnInfo(name = "is_recurring")
    val isRecurring: Boolean = false,        // 是否为循环目标
    @ColumnInfo(name = "recurrence_type")
    val recurrenceType: String? = null,      // 循环类型（daily/weekly/monthly）
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) 