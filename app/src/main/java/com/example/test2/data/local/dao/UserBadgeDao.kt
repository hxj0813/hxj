package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.test2.data.local.entity.UserBadgeEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 用户徽章数据访问对象接口
 */
@Dao
interface UserBadgeDao {
    /**
     * 获取所有用户已解锁的徽章
     */
    @Query("SELECT * FROM user_badges ORDER BY unlockedAt DESC")
    fun getAllUserBadges(): Flow<List<UserBadgeEntity>>
    
    /**
     * 获取被标记为重点的徽章（新获得的）
     */
    @Query("SELECT * FROM user_badges WHERE highlighted = 1 ORDER BY unlockedAt DESC")
    fun getHighlightedBadges(): Flow<List<UserBadgeEntity>>
    
    /**
     * 根据ID获取用户徽章
     */
    @Query("SELECT * FROM user_badges WHERE id = :userBadgeId")
    suspend fun getUserBadgeById(userBadgeId: String): UserBadgeEntity?
    
    /**
     * 根据徽章ID获取用户徽章
     */
    @Query("SELECT * FROM user_badges WHERE badgeId = :badgeId")
    suspend fun getUserBadgeByBadgeId(badgeId: String): UserBadgeEntity?
    
    /**
     * 获取特定习惯的所有徽章
     */
    @Query("SELECT * FROM user_badges WHERE habitId = :habitId ORDER BY unlockedAt DESC")
    fun getBadgesForHabit(habitId: String): Flow<List<UserBadgeEntity>>
    
    /**
     * 获取特定时间段内解锁的徽章
     */
    @Query("SELECT * FROM user_badges WHERE unlockedAt BETWEEN :startDate AND :endDate ORDER BY unlockedAt DESC")
    fun getBadgesInDateRange(startDate: Date, endDate: Date): Flow<List<UserBadgeEntity>>
    
    /**
     * 获取用户徽章总数
     */
    @Query("SELECT COUNT(*) FROM user_badges")
    suspend fun getUserBadgesCount(): Int
    
    /**
     * 获取特定习惯的徽章数量
     */
    @Query("SELECT COUNT(*) FROM user_badges WHERE habitId = :habitId")
    suspend fun getBadgesCountForHabit(habitId: String): Int
    
    /**
     * 检查用户是否已拥有特定徽章
     */
    @Query("SELECT EXISTS(SELECT 1 FROM user_badges WHERE badgeId = :badgeId)")
    suspend fun hasUserBadge(badgeId: String): Boolean
    
    /**
     * 检查特定习惯是否已拥有特定徽章
     */
    @Query("SELECT EXISTS(SELECT 1 FROM user_badges WHERE badgeId = :badgeId AND habitId = :habitId)")
    suspend fun hasHabitBadge(badgeId: String, habitId: String): Boolean
    
    /**
     * 插入用户徽章
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserBadge(userBadge: UserBadgeEntity)
    
    /**
     * 批量插入用户徽章
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserBadges(userBadges: List<UserBadgeEntity>)
    
    /**
     * 更新用户徽章
     */
    @Update
    suspend fun updateUserBadge(userBadge: UserBadgeEntity)
    
    /**
     * 删除用户徽章
     */
    @Delete
    suspend fun deleteUserBadge(userBadge: UserBadgeEntity)
    
    /**
     * 取消所有徽章的重点标记
     */
    @Query("UPDATE user_badges SET highlighted = 0")
    suspend fun clearAllHighlights()
    
    /**
     * 更新指定徽章的重点标记状态
     */
    @Query("UPDATE user_badges SET highlighted = :highlighted WHERE id = :userBadgeId")
    suspend fun updateHighlightStatus(userBadgeId: String, highlighted: Boolean)
    
    /**
     * 更新徽章进度
     */
    @Query("UPDATE user_badges SET progress = :progress WHERE id = :userBadgeId")
    suspend fun updateBadgeProgress(userBadgeId: String, progress: Int)
    
    /**
     * 删除特定习惯的所有徽章
     */
    @Query("DELETE FROM user_badges WHERE habitId = :habitId")
    suspend fun deleteAllBadgesForHabit(habitId: String)
    
    /**
     * 获取最近解锁的徽章
     */
    @Query("SELECT * FROM user_badges ORDER BY unlockedAt DESC LIMIT :limit")
    suspend fun getRecentlyUnlockedBadges(limit: Int): List<UserBadgeEntity>
} 