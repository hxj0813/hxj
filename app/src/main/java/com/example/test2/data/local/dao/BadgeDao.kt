package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.BadgeType
import kotlinx.coroutines.flow.Flow

/**
 * 徽章数据访问对象接口
 */
@Dao
interface BadgeDao {
    /**
     * 获取所有徽章
     */
    @Query("SELECT * FROM badges ORDER BY rarity DESC, name ASC")
    fun getAllBadges(): Flow<List<BadgeEntity>>
    
    /**
     * 获取非隐藏徽章
     */
    @Query("SELECT * FROM badges WHERE isHidden = 0 ORDER BY rarity DESC, name ASC")
    fun getVisibleBadges(): Flow<List<BadgeEntity>>
    
    /**
     * 根据ID获取徽章
     */
    @Query("SELECT * FROM badges WHERE id = :badgeId")
    suspend fun getBadgeById(badgeId: String): BadgeEntity?
    
    /**
     * 根据类型获取徽章列表
     */
    @Query("SELECT * FROM badges WHERE type = :type ORDER BY requiredValue ASC")
    fun getBadgesByType(type: Int): Flow<List<BadgeEntity>>
    
    /**
     * 根据稀有度获取徽章列表
     */
    @Query("SELECT * FROM badges WHERE rarity = :rarity ORDER BY name ASC")
    fun getBadgesByRarity(rarity: Int): Flow<List<BadgeEntity>>
    
    /**
     * 获取特定类别习惯的徽章
     */
    @Query("SELECT * FROM badges WHERE requiredCategoryId = :categoryId OR requiredCategoryId IS NULL")
    fun getBadgesForCategory(categoryId: Int): Flow<List<BadgeEntity>>
    
    /**
     * 获取连续打卡达到特定天数的徽章
     */
    @Query("SELECT * FROM badges WHERE type = :badgeType AND requiredValue <= :streakValue ORDER BY requiredValue DESC LIMIT 1")
    suspend fun getBadgeForStreak(badgeType: Int, streakValue: Int): BadgeEntity?
    
    /**
     * 获取所有连续打卡徽章，按所需值排序
     */
    @Query("SELECT * FROM badges WHERE type = :badgeType ORDER BY requiredValue ASC")
    fun getAllStreakBadges(badgeType: Int): Flow<List<BadgeEntity>>
    
    /**
     * 根据类别获取下一个可解锁的徽章
     */
    @Query("SELECT * FROM badges WHERE requiredCategoryId = :categoryId AND requiredValue > :currentValue ORDER BY requiredValue ASC LIMIT 1")
    suspend fun getNextBadgeForCategory(categoryId: Int, currentValue: Int): BadgeEntity?
    
    /**
     * 插入徽章
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeEntity)
    
    /**
     * 批量插入徽章
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<BadgeEntity>)
    
    /**
     * 更新徽章
     */
    @Update
    suspend fun updateBadge(badge: BadgeEntity)
    
    /**
     * 删除徽章
     */
    @Delete
    suspend fun deleteBadge(badge: BadgeEntity)
    
    /**
     * 根据类型获取最高级徽章
     */
    @Query("SELECT * FROM badges WHERE type = :type ORDER BY requiredValue DESC, rarity DESC LIMIT 1")
    suspend fun getHighestBadgeByType(type: Int): BadgeEntity?
    
    /**
     * 获取特定条件值可解锁的徽章
     */
    @Query("SELECT * FROM badges WHERE type = :type AND requiredValue <= :value ORDER BY requiredValue DESC LIMIT 1")
    suspend fun getBadgeForValue(type: Int, value: Int): BadgeEntity?
} 