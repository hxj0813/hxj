package com.example.test2.data.repository

import com.example.test2.data.local.dao.BadgeDao
import com.example.test2.data.local.dao.UserBadgeDao
import com.example.test2.data.local.entity.BadgeCategory
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.BadgeRarity
import com.example.test2.data.local.entity.UserBadgeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 徽章仓库接口实现
 */
@Singleton
class BadgeRepositoryImpl @Inject constructor(
    private val badgeDao: BadgeDao,
    private val userBadgeDao: UserBadgeDao
) : BadgeRepository {
    
    /**
     * 获取所有徽章
     */
    override fun getAllBadges(): Flow<List<BadgeEntity>> {
        return badgeDao.getAllBadges()
    }
    
    /**
     * 获取指定类别的徽章
     */
    override fun getBadgesByCategory(category: BadgeCategory): Flow<List<BadgeEntity>> {
        return badgeDao.getBadgesByCategory(category.ordinal)
    }
    
    /**
     * 获取指定稀有度的徽章
     */
    override fun getBadgesByRarity(rarity: BadgeRarity): Flow<List<BadgeEntity>> {
        return badgeDao.getBadgesByRarity(rarity.ordinal)
    }
    
    /**
     * 根据ID获取徽章
     */
    override suspend fun getBadgeById(badgeId: String): BadgeEntity? {
        return badgeDao.getBadgeById(badgeId)
    }
    
    /**
     * 添加徽章
     */
    override suspend fun insertBadge(badge: BadgeEntity): String {
        badgeDao.insertBadge(badge)
        return badge.id
    }
    
    /**
     * 批量添加徽章
     */
    override suspend fun insertBadges(badges: List<BadgeEntity>) {
        badgeDao.insertBadges(badges)
    }
    
    /**
     * 更新徽章信息
     */
    override suspend fun updateBadge(badge: BadgeEntity) {
        badgeDao.updateBadge(badge)
    }
    
    /**
     * 删除徽章
     */
    override suspend fun deleteBadge(badge: BadgeEntity) {
        badgeDao.deleteBadge(badge)
    }
    
    /**
     * 获取用户已解锁的所有徽章
     */
    override fun getUserBadges(): Flow<List<UserBadgeEntity>> {
        return userBadgeDao.getAllUserBadges()
    }
    
    /**
     * 获取用户指定类别的徽章
     */
    override fun getUserBadgesByCategory(category: BadgeCategory): Flow<List<UserBadgeEntity>> {
        return userBadgeDao.getUserBadgesByCategory(category.ordinal)
    }
    
    /**
     * 获取用户在特定习惯上获得的徽章
     */
    override fun getUserBadgesByHabit(habitId: String): Flow<List<UserBadgeEntity>> {
        return userBadgeDao.getUserBadgesByHabit(habitId)
    }
    
    /**
     * 检查用户是否已解锁某徽章
     */
    override suspend fun isUserBadgeUnlocked(badgeId: String): Boolean {
        return userBadgeDao.getUserBadgeByBadgeId(badgeId) != null
    }
    
    /**
     * 解锁徽章（授予用户徽章）
     */
    override suspend fun unlockBadge(
        badgeId: String,
        habitId: String?,
        progress: Int,
        valueWhenUnlocked: Int?,
        note: String?
    ): String {
        val userBadge = UserBadgeEntity(
            badgeId = badgeId,
            habitId = habitId,
            progress = progress,
            valueWhenUnlocked = valueWhenUnlocked,
            note = note
        )
        
        userBadgeDao.insertUserBadge(userBadge)
        return userBadge.id
    }
    
    /**
     * 更新用户徽章进度
     */
    override suspend fun updateUserBadgeProgress(
        userBadgeId: String,
        progress: Int
    ) {
        val userBadge = userBadgeDao.getUserBadgeById(userBadgeId)
        if (userBadge != null) {
            userBadgeDao.updateUserBadge(
                userBadge.copy(progress = progress)
            )
        }
    }
    
    /**
     * 获取最近解锁的徽章（用于通知）
     */
    override suspend fun getRecentlyUnlockedBadges(since: Date): List<UserBadgeEntity> {
        return userBadgeDao.getRecentlyUnlockedBadges(since)
    }
    
    /**
     * 标记徽章为已查看（关闭新徽章高亮）
     */
    override suspend fun markBadgeAsViewed(userBadgeId: String) {
        val userBadge = userBadgeDao.getUserBadgeById(userBadgeId)
        if (userBadge != null && userBadge.highlighted) {
            userBadgeDao.updateUserBadge(
                userBadge.copy(highlighted = false)
            )
        }
    }
    
    /**
     * 初始化默认徽章
     */
    override suspend fun initializeDefaultBadges() {
        // 此方法通常由BadgeService调用
        // 在这里只是一个空方法
    }
} 