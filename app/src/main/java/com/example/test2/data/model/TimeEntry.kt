package com.example.test2.data.model

import java.util.Date
import java.util.UUID

/**
 * 时间追踪条目数据模型
 * 
 * @property id 唯一标识符
 * @property title 标题
 * @property description 描述（可选）
 * @property category 分类
 * @property startTime 开始时间
 * @property endTime 结束时间（为null表示正在进行）
 * @property duration 持续时间（秒）
 * @property taskId 关联的任务ID（可选）
 * @property goalId 关联的目标ID（可选）
 * @property tags 标签列表
 * @property createdAt 创建时间
 */
data class TimeEntry(
    val id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
    val title: String,
    val description: String? = null,
    val category: TimeCategory = TimeCategory.OTHER,
    val startTime: Date,
    val endTime: Date? = null,
    val duration: Long = calculateDuration(startTime, endTime),
    val taskId: Long? = null,
    val goalId: Long? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Date = Date()
) {
    /**
     * 检查时间条目是否正在进行
     */
    fun isOngoing(): Boolean = endTime == null
    
    /**
     * 创建新的时间条目，设置结束时间并计算持续时间
     */
    fun complete(endTime: Date = Date()): TimeEntry {
        return this.copy(
            endTime = endTime,
            duration = calculateDuration(startTime, endTime)
        )
    }
    
    companion object {
        /**
         * 计算持续时间（秒）
         */
        fun calculateDuration(startTime: Date, endTime: Date?): Long {
            if (endTime == null) {
                return (Date().time - startTime.time) / 1000
            }
            return (endTime.time - startTime.time) / 1000
        }
        
        /**
         * 格式化持续时间为可读字符串
         */
        fun formatDuration(durationInSeconds: Long): String {
            val hours = durationInSeconds / 3600
            val minutes = (durationInSeconds % 3600) / 60
            val seconds = durationInSeconds % 60
            
            return when {
                hours > 0 -> String.format("%d小时%02d分钟", hours, minutes)
                minutes > 0 -> String.format("%d分钟%02d秒", minutes, seconds)
                else -> String.format("%d秒", seconds)
            }
        }
    }
} 