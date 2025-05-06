package com.example.test2.data.model

import java.util.Date
import java.util.UUID

/**
 * 习惯徽章类型枚举
 */
enum class HabitBadgeType {
    STARTER,     // 入门徽章
    PERSISTENT,  // 坚持不懈徽章
    DEDICATED,   // 专注投入徽章
    MASTER,      // 习惯大师徽章
    COMEBACK,    // 重新回归徽章
    CONSISTENT,  // 稳定保持徽章
    EARLY_BIRD,  // 早起者徽章
    NIGHT_OWL,   // 夜猫子徽章
    SOCIAL,      // 社交分享徽章
    MILESTONE    // 里程碑徽章
}

/**
 * 习惯徽章数据模型
 */
data class HabitBadge(
    val id: String = UUID.randomUUID().toString(),  // 徽章ID
    val title: String,                              // 徽章标题
    val description: String,                        // 徽章描述
    val type: HabitBadgeType,                       // 徽章类型
    val isUnlocked: Boolean = false,                // 是否已解锁
    val unlockedAt: Date? = null,                   // 解锁时间
    val habitId: String? = null,                    // 关联的习惯ID
    val progress: Float = 0f                        // 进度（0-1）
) 