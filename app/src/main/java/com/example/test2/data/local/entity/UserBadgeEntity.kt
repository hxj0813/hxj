package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * 用户徽章实体类
 * 记录用户获得的徽章及其相关信息
 */
@Entity(
    tableName = "user_badges",
    foreignKeys = [
        ForeignKey(
            entity = BadgeEntity::class,
            parentColumns = ["id"],
            childColumns = ["badgeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("badgeId")]
)
data class UserBadgeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val badgeId: String,                     // 关联的徽章ID
    val habitId: String? = null,             // 关联的习惯ID（如果是特定习惯的徽章）
    val unlockedAt: Date = Date(),           // 解锁时间
    val progress: Int = 100,                 // 完成进度，100表示完全解锁
    val highlighted: Boolean = true,         // 是否标记为重点（新获得的徽章）
    val displayOrder: Int = 0,               // 显示顺序
    val valueWhenUnlocked: Int? = null,      // 解锁时的值（如连续天数）
    val note: String? = null                 // 解锁说明
) {
    /**
     * 判断徽章是否完全解锁
     */
    fun isFullyUnlocked(): Boolean {
        return progress >= 100
    }
    
    /**
     * 获取进度百分比
     */
    fun getProgressPercentage(): Int {
        return when {
            progress <= 0 -> 0
            progress >= 100 -> 100
            else -> progress
        }
    }
    
    companion object {
        /**
         * 创建用户徽章的工厂方法
         */
        fun create(
            badgeId: String,
            habitId: String? = null,
            progress: Int = 100,
            valueWhenUnlocked: Int? = null,
            note: String? = null
        ): UserBadgeEntity {
            return UserBadgeEntity(
                badgeId = badgeId,
                habitId = habitId,
                progress = progress,
                valueWhenUnlocked = valueWhenUnlocked,
                note = note
            )
        }
    }
} 