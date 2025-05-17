package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.local.entity.BadgeEntity
import kotlinx.coroutines.flow.Flow

/**
 * 徽章数据访问对象接口
 */
@Dao
interface BadgeDao {
    /**
     * 获取所有徽章
     */
    @Query("SELECT * FROM badges ORDER BY category, rarity")
    fun getAllBadges(): Flow<List<BadgeEntity>>
    
    /**
     * 获取指定类别的徽章
     */
    @Query("SELECT * FROM badges WHERE category = :category ORDER BY rarity")
    fun getBadgesByCategory(category: Int): Flow<List<BadgeEntity>>
    
    /**
     * 获取指定稀有度的徽章
     */
    @Query("SELECT * FROM badges WHERE rarity = :rarity ORDER BY category")
    fun getBadgesByRarity(rarity: Int): Flow<List<BadgeEntity>>
    
    /**
     * 根据ID获取徽章
     */
    @Query("SELECT * FROM badges WHERE id = :badgeId")
    suspend fun getBadgeById(badgeId: String): BadgeEntity?
    
    /**
     * 获取所有默认徽章
     */
    @Query("SELECT * FROM badges WHERE isDefault = 1")
    fun getDefaultBadges(): Flow<List<BadgeEntity>>
    
    /**
     * 获取非隐藏徽章
     */
    @Query("SELECT * FROM badges WHERE isSecret = 0 ORDER BY category, rarity")
    fun getVisibleBadges(): Flow<List<BadgeEntity>>
    
    /**
     * 获取所有连续打卡徽章，按所需值排序
     */
    @Query("SELECT * FROM badges WHERE category = :badgeCategory ORDER BY thresholdValue ASC")
    fun getAllStreakBadges(badgeCategory: Int): Flow<List<BadgeEntity>>
    
    /**
     * 根据类别获取下一个可解锁的徽章
     */
    @Query("SELECT * FROM badges WHERE category = :categoryId AND thresholdValue > :currentValue ORDER BY thresholdValue ASC LIMIT 1")
    suspend fun getNextBadgeForCategory(categoryId: Int, currentValue: Int): BadgeEntity?
    
    /**
     * 添加徽章
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeEntity)
    
    /**
     * 批量添加徽章
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
     * 根据类别获取最高级徽章
     */
    @Query("SELECT * FROM badges WHERE category = :category ORDER BY thresholdValue DESC, rarity DESC LIMIT 1")
    suspend fun getHighestBadgeByType(category: Int): BadgeEntity?
    
    /**
     * 获取特定条件值可解锁的徽章
     */
    @Query("SELECT * FROM badges WHERE category = :category AND thresholdValue <= :value ORDER BY thresholdValue DESC LIMIT 1")
    suspend fun getBadgeForValue(category: Int, value: Int): BadgeEntity?
} 