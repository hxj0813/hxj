package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.test2.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 习惯数据访问对象接口
 */
@Dao
interface HabitDao {
    /**
     * 获取所有未归档的习惯，按优先级排序
     */
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY priority DESC, updatedAt DESC")
    fun getAllActiveHabits(): Flow<List<HabitEntity>>
    
    /**
     * 获取所有习惯，按更新时间排序
     */
    @Query("SELECT * FROM habits ORDER BY updatedAt DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>
    
    /**
     * 根据ID获取习惯
     */
    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitById(habitId: String): Flow<HabitEntity?>
    
    /**
     * 根据类别获取未归档的习惯
     */
    @Query("SELECT * FROM habits WHERE category = :category AND isArchived = 0 ORDER BY priority DESC")
    fun getHabitsByCategory(category: Int): Flow<List<HabitEntity>>
    
    /**
     * 获取今天未完成的未归档习惯
     */
    @Query("SELECT * FROM habits WHERE completedToday = 0 AND isArchived = 0 ORDER BY priority DESC")
    fun getHabitsNotCompletedToday(): Flow<List<HabitEntity>>
    
    /**
     * 获取连续天数大于或等于指定值的未归档习惯
     */
    @Query("SELECT * FROM habits WHERE currentStreak >= :minStreak AND isArchived = 0")
    fun getHabitsWithStreakAtLeast(minStreak: Int): Flow<List<HabitEntity>>
    
    /**
     * 获取特定时间段内应该执行的习惯
     */
    @Query("SELECT * FROM habits WHERE reminderTime BETWEEN :startTime AND :endTime AND reminder = 1 AND isArchived = 0")
    fun getHabitsWithReminderInTimeRange(startTime: Date, endTime: Date): Flow<List<HabitEntity>>
    
    /**
     * 新增习惯
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)
    
    /**
     * 更新习惯
     */
    @Update
    suspend fun updateHabit(habit: HabitEntity)
    
    /**
     * 删除习惯
     */
    @Delete
    suspend fun deleteHabit(habit: HabitEntity)
    
    /**
     * 更新习惯的完成状态
     */
    @Query("UPDATE habits SET completedToday = :completed, " +
           "lastCompletedDate = :completionDate, " +
           "currentStreak = :newStreak, " +
           "bestStreak = CASE WHEN :newStreak > bestStreak THEN :newStreak ELSE bestStreak END, " +
           "totalCompletions = totalCompletions + :incrementCompletions, " +
           "updatedAt = :updateDate " +
           "WHERE id = :habitId")
    suspend fun updateHabitCompletion(
        habitId: String,
        completed: Boolean,
        completionDate: Date,
        newStreak: Int,
        incrementCompletions: Int,
        updateDate: Date
    )
    
    /**
     * 更新习惯的归档状态
     */
    @Query("UPDATE habits SET isArchived = :archived, updatedAt = :updateDate WHERE id = :habitId")
    suspend fun updateHabitArchivedStatus(habitId: String, archived: Boolean, updateDate: Date)
    
    /**
     * 重置所有习惯的今日完成状态
     */
    @Query("UPDATE habits SET completedToday = 0")
    suspend fun resetAllHabitsCompletedToday()
    
    /**
     * 更新习惯的徽章相关信息
     */
    @Query("UPDATE habits SET unlockedBadgesCount = :badgeCount, " +
           "unlockedBadgesJson = :badgesJson, " +
           "updatedAt = :updateDate " +
           "WHERE id = :habitId")
    suspend fun updateHabitBadges(
        habitId: String,
        badgeCount: Int,
        badgesJson: String,
        updateDate: Date
    )
    
    /**
     * 获取所有不同类别的活跃习惯数量
     */
    @Query("SELECT COUNT(DISTINCT category) FROM habits WHERE isArchived = 0")
    suspend fun getActiveHabitCategoriesCount(): Int
    
    /**
     * 获取总完成次数最多的习惯
     */
    @Query("SELECT * FROM habits ORDER BY totalCompletions DESC LIMIT 1")
    suspend fun getMostCompletedHabit(): HabitEntity?
    
    /**
     * 获取最长连续记录的习惯
     */
    @Query("SELECT * FROM habits ORDER BY bestStreak DESC LIMIT 1")
    suspend fun getHabitWithLongestStreak(): HabitEntity?
    
    /**
     * 更新中断的习惯连续记录
     * 当日期间隔超过1天时，需要重置当前连续记录
     */
    @Transaction
    suspend fun resetBrokenStreaks(currentDate: Date) {
        val habits = getAllHabitsWithCompletionDataSync()
        for (habit in habits) {
            val lastCompletedDate = habit.lastCompletedDate ?: continue
            val calendar = java.util.Calendar.getInstance()
            calendar.time = lastCompletedDate
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1) // 加一天
            
            if (calendar.time.before(currentDate)) {
                // 如果最后完成日期+1天早于当前日期，表示连续记录中断
                updateHabitCompletion(
                    habitId = habit.id,
                    completed = false,
                    completionDate = lastCompletedDate, // 保持最后完成日期不变
                    newStreak = 0, // 重置连续记录
                    incrementCompletions = 0, // 不增加总完成次数
                    updateDate = currentDate
                )
            }
        }
    }
    
    /**
     * 获取所有习惯及其完成数据(同步版本)
     */
    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsWithCompletionDataSync(): List<HabitEntity>
} 