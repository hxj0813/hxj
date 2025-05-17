package com.example.test2.data.repository

import com.example.test2.data.local.entity.BadgeCategory
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.BadgeRarity
import com.example.test2.data.local.entity.UserBadgeEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 徽章仓库接口
 */
interface BadgeRepository {
    /**
     * 获取所有徽章
     */
    fun getAllBadges(): Flow<List<BadgeEntity>>
    
    /**
     * 获取指定类别的徽章
     */
    fun getBadgesByCategory(category: BadgeCategory): Flow<List<BadgeEntity>>
    
    /**
     * 获取指定稀有度的徽章
     */
    fun getBadgesByRarity(rarity: BadgeRarity): Flow<List<BadgeEntity>>
    
    /**
     * 根据ID获取徽章
     */
    suspend fun getBadgeById(badgeId: String): BadgeEntity?
    
    /**
     * 添加徽章
     */
    suspend fun insertBadge(badge: BadgeEntity): String
    
    /**
     * 批量添加徽章
     */
    suspend fun insertBadges(badges: List<BadgeEntity>)
    
    /**
     * 更新徽章信息
     */
    suspend fun updateBadge(badge: BadgeEntity)
    
    /**
     * 删除徽章
     */
    suspend fun deleteBadge(badge: BadgeEntity)
    
    /**
     * 获取用户已解锁的所有徽章
     */
    fun getUserBadges(): Flow<List<UserBadgeEntity>>
    
    /**
     * 获取用户指定类别的徽章
     */
    fun getUserBadgesByCategory(category: BadgeCategory): Flow<List<UserBadgeEntity>>
    
    /**
     * 获取用户在特定习惯上获得的徽章
     */
    fun getUserBadgesByHabit(habitId: String): Flow<List<UserBadgeEntity>>
    
    /**
     * 检查用户是否已解锁某徽章
     */
    suspend fun isUserBadgeUnlocked(badgeId: String): Boolean
    
    /**
     * 解锁徽章（授予用户徽章）
     */
    suspend fun unlockBadge(
        badgeId: String,
        habitId: String? = null,
        progress: Int = 100,
        valueWhenUnlocked: Int? = null,
        note: String? = null
    ): String
    
    /**
     * 更新用户徽章进度
     */
    suspend fun updateUserBadgeProgress(
        userBadgeId: String,
        progress: Int
    )
    
    /**
     * 获取最近解锁的徽章（用于通知）
     */
    suspend fun getRecentlyUnlockedBadges(since: Date): List<UserBadgeEntity>
    
    /**
     * 标记徽章为已查看（关闭新徽章高亮）
     */
    suspend fun markBadgeAsViewed(userBadgeId: String)
    
    /**
     * 初始化默认徽章
     */
    suspend fun initializeDefaultBadges()
} 