package com.example.test2.data.local.entity.timetracking

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 时间统计实体类
 * 用于存储预计算的统计数据，提高查询效率
 */
@Entity(
    tableName = "time_statistics",
    indices = [
        Index(value = ["reference_type", "reference_id"]),
        Index(value = ["date"]),
        Index(value = ["category"])
    ]
)
data class TimeStatEntity(
    @PrimaryKey
    val id: String,                          // 统计ID（格式：type_id_date，如：task_123_20230601）
    
    @ColumnInfo(name = "reference_type")
    val referenceType: String,               // 引用类型（task/habit/goal/category）
    @ColumnInfo(name = "reference_id")
    val referenceId: Long? = null,           // 引用ID
    @ColumnInfo(name = "category")
    val category: String? = null,            // 时间分类
    
    @ColumnInfo(name = "date")
    val date: Long,                          // 统计日期（毫秒时间戳）
    @ColumnInfo(name = "total_seconds")
    val totalSeconds: Long = 0,              // 总时长（秒）
    @ColumnInfo(name = "pomodoro_count")
    val pomodoroCount: Int = 0,              // 番茄钟总数
    @ColumnInfo(name = "check_in_count")
    val checkInCount: Int = 0,               // 打卡总数
    @ColumnInfo(name = "session_count")
    val sessionCount: Int = 0,               // 会话总数
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()  // 最后更新时间
) 