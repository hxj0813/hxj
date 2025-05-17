package com.example.test2.data.model

import java.util.Date

/**
 * 目标数据模型
 *
 * @property id 目标ID
 * @property title 目标标题
 * @property description 目标描述
 * @property isLongTerm 是否是长期目标
 * @property isImportant 是否重要
 * @property progress 完成进度 (0.0-1.0)
 * @property deadline 截止日期
 * @property isCompleted 是否已完成
 * @property hasLinkedTask 是否关联任务
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
data class Goal(
    val id: Long = 0,
    val title: String,
    val description: String,
    val isLongTerm: Boolean = false,
    val isImportant: Boolean = false,
    val progress: Float = 0f,
    val deadline: Date,
    val isCompleted: Boolean = false,
    val hasLinkedTask: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * 检查目标是否即将到期 (3天内)
     */
    fun isUpcoming(): Boolean {
        if (isCompleted) return false
        
        val current = Date()
        val diffInMillis = deadline.time - current.time
        val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
        
        return diffInDays in 0..3
    }

    /**
     * 检查目标是否已逾期
     */
    fun isOverdue(): Boolean {
        if (isCompleted) return false
        
        val current = Date()
        return deadline.before(current)
    }

    /**
     * 目标完成状态
     */
    fun getStatus(): GoalStatus {
        return when {
            isCompleted -> GoalStatus.COMPLETED
            isOverdue() -> GoalStatus.OVERDUE
            isUpcoming() -> GoalStatus.UPCOMING
            else -> GoalStatus.IN_PROGRESS
        }
    }
    
    /**
     * 计算基于时间的进度（适用于没有关联任务的目标）
     * 
     * 根据当前日期和创建/截止日期计算进度：
     * - 创建日期时进度为0%
     * - 截止日期时进度为100%
     * - 中间时间按比例计算
     * 
     * @return 进度值(0.0-1.0)
     */
    fun calculateTimeBasedProgress(): Float {
        // 如果已完成，直接返回1.0
        if (isCompleted) return 1.0f
        
        val now = Date()
        
        // 如果当前时间在创建日期之前，返回0
        if (now.before(createdAt)) return 0f
        
        // 如果当前时间在截止日期之后，返回1
        if (now.after(deadline)) return 1f
        
        // 计算总时间范围（截止日期 - 创建日期）
        val totalTimespan = deadline.time - createdAt.time
        
        // 计算已经过去的时间（当前时间 - 创建日期）
        val elapsedTime = now.time - createdAt.time
        
        // 计算进度比例
        return (elapsedTime.toFloat() / totalTimespan).coerceIn(0f, 1f)
    }
}

/**
 * 目标状态枚举
 */
enum class GoalStatus {
    IN_PROGRESS,  // 进行中
    UPCOMING,     // 即将到期
    OVERDUE,      // 已逾期
    COMPLETED     // 已完成
} 