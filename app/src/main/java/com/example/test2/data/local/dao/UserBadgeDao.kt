package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.local.entity.UserBadgeEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 用户徽章数据访问对象
 */
@Dao
interface UserBadgeDao {
    
    /**
     * 获取用户所有徽章
     */
    @Query("""
        SELECT ub.* FROM user_badges ub
        ORDER BY ub.unlockedAt DESC, ub.displayOrder
    """)
    fun getAllUserBadges(): Flow<List<UserBadgeEntity>>
    
    /**
     * 获取用户特定类别的徽章
     */
    @Query("""
        SELECT ub.* FROM user_badges ub
        INNER JOIN badges b ON ub.badgeId = b.id
        WHERE b.category = :category
        ORDER BY ub.unlockedAt DESC, ub.displayOrder
    """)
    fun getUserBadgesByCategory(category: Int): Flow<List<UserBadgeEntity>>
    
    /**
     * 获取用户特定习惯的徽章
     */
    @Query("""
        SELECT * FROM user_badges
        WHERE habitId = :habitId
        ORDER BY unlockedAt DESC, displayOrder
    """)
    fun getUserBadgesByHabit(habitId: String): Flow<List<UserBadgeEntity>>
    
    /**
     * 根据ID获取用户徽章
     */
    @Query("SELECT * FROM user_badges WHERE id = :userBadgeId")
    suspend fun getUserBadgeById(userBadgeId: String): UserBadgeEntity?
    
    /**
     * 根据徽章ID获取用户徽章
     */
    @Query("SELECT * FROM user_badges WHERE badgeId = :badgeId LIMIT 1")
    suspend fun getUserBadgeByBadgeId(badgeId: String): UserBadgeEntity?
    
    /**
     * 获取新解锁的徽章（高亮显示的）
     */
    @Query("SELECT * FROM user_badges WHERE highlighted = 1 ORDER BY unlockedAt DESC")
    fun getHighlightedBadges(): Flow<List<UserBadgeEntity>>
    
    /**
     * 获取最近解锁的徽章
     */
    @Query("SELECT * FROM user_badges WHERE unlockedAt >= :since ORDER BY unlockedAt DESC")
    suspend fun getRecentlyUnlockedBadges(since: Date): List<UserBadgeEntity>
    
    /**
     * 添加用户徽章
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserBadge(userBadge: UserBadgeEntity)
    
    /**
     * 批量添加用户徽章
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
     * 更新用户徽章的高亮状态
     */
    @Query("UPDATE user_badges SET highlighted = :highlighted WHERE id = :userBadgeId")
    suspend fun updateHighlightStatus(userBadgeId: String, highlighted: Boolean)
} 