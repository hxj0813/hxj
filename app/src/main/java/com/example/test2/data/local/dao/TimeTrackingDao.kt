package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.test2.data.local.entity.timetracking.TimeEntryEntity
import com.example.test2.data.local.entity.timetracking.TimeEntryTagCrossRef
import com.example.test2.data.local.entity.timetracking.TimeGoalEntity
import com.example.test2.data.local.entity.timetracking.TimeStatEntity
import com.example.test2.data.local.entity.timetracking.TimeTagEntity
import kotlinx.coroutines.flow.Flow

/**
 * 分类统计结果
 */
data class CategoryStat(
    val category: String,
    val total: Long
)

/**
 * 时间追踪数据访问对象
 */
@Dao
interface TimeTrackingDao {
    // ==================== 时间条目 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntry(timeEntry: TimeEntryEntity): Long
    
    @Update
    suspend fun updateTimeEntry(timeEntry: TimeEntryEntity)
    
    @Delete
    suspend fun deleteTimeEntry(timeEntry: TimeEntryEntity)
    
    @Query("SELECT * FROM time_entries WHERE id = :id")
    suspend fun getTimeEntryById(id: Long): TimeEntryEntity?
    
    @Query("SELECT * FROM time_entries ORDER BY start_time DESC")
    fun getAllTimeEntries(): Flow<List<TimeEntryEntity>>
    
    @Query("SELECT * FROM time_entries WHERE end_time IS NULL")
    fun getOngoingTimeEntries(): Flow<List<TimeEntryEntity>>
    
    @Query("""
        SELECT * FROM time_entries 
        WHERE start_time >= :startTime 
        AND (end_time <= :endTime OR end_time IS NULL)
        ORDER BY start_time DESC
    """)
    fun getTimeEntriesBetween(startTime: Long, endTime: Long): Flow<List<TimeEntryEntity>>
    
    @Query("""
        SELECT * FROM time_entries 
        WHERE category = :category 
        ORDER BY start_time DESC
    """)
    fun getTimeEntriesByCategory(category: String): Flow<List<TimeEntryEntity>>
    
    @Query("""
        SELECT * FROM time_entries 
        WHERE task_id = :taskId 
        ORDER BY start_time DESC
    """)
    fun getTimeEntriesByTask(taskId: Long): Flow<List<TimeEntryEntity>>
    
    @Query("""
        SELECT * FROM time_entries 
        WHERE habit_id = :habitId 
        ORDER BY start_time DESC
    """)
    fun getTimeEntriesByHabit(habitId: Long): Flow<List<TimeEntryEntity>>
    
    @Query("""
        SELECT * FROM time_entries 
        WHERE goal_id = :goalId 
        ORDER BY start_time DESC
    """)
    fun getTimeEntriesByGoal(goalId: Long): Flow<List<TimeEntryEntity>>
    
    @Query("""
        SELECT SUM(duration_seconds) 
        FROM time_entries 
        WHERE category = :category 
        AND start_time >= :startTime 
        AND (end_time <= :endTime OR end_time IS NULL)
    """)
    suspend fun getTotalTimeByCategory(category: String, startTime: Long, endTime: Long): Long?
    
    @Query("""
        SELECT COUNT(*) 
        FROM time_entries 
        WHERE task_id = :taskId 
        AND is_pomodoro = 1 
        AND end_time IS NOT NULL
    """)
    suspend fun getCompletedPomodoroCount(taskId: Long): Int
    
    @Query("""
        SELECT COUNT(*) 
        FROM time_entries 
        WHERE is_check_in = 1 
        AND start_time >= :startOfDay 
        AND start_time < :endOfDay
    """)
    suspend fun getDailyCheckInCount(startOfDay: Long, endOfDay: Long): Int
    
    // ==================== 标签 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TimeTagEntity): Long
    
    @Update
    suspend fun updateTag(tag: TimeTagEntity)
    
    @Delete
    suspend fun deleteTag(tag: TimeTagEntity)
    
    @Query("SELECT * FROM time_tags ORDER BY name")
    fun getAllTags(): Flow<List<TimeTagEntity>>
    
    @Query("SELECT * FROM time_tags WHERE id = :id")
    suspend fun getTagById(id: Long): TimeTagEntity?
    
    // ==================== 标签关联 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntryTagCrossRef(crossRef: TimeEntryTagCrossRef)
    
    @Query("DELETE FROM time_entry_tag_cross_ref WHERE time_entry_id = :timeEntryId AND tag_id = :tagId")
    suspend fun deleteTimeEntryTagCrossRef(timeEntryId: Long, tagId: Long)
    
    @Query("DELETE FROM time_entry_tag_cross_ref WHERE time_entry_id = :timeEntryId")
    suspend fun deleteAllTagsForTimeEntry(timeEntryId: Long)
    
    @Query("""
        SELECT t.* FROM time_tags t
        INNER JOIN time_entry_tag_cross_ref ref ON t.id = ref.tag_id
        WHERE ref.time_entry_id = :timeEntryId
    """)
    fun getTagsForTimeEntry(timeEntryId: Long): Flow<List<TimeTagEntity>>
    
    // ==================== 统计数据 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeStatistic(timeStat: TimeStatEntity)
    
    @Update
    suspend fun updateTimeStatistic(timeStat: TimeStatEntity)
    
    @Query("DELETE FROM time_statistics WHERE id = :id")
    suspend fun deleteTimeStatistic(id: String)
    
    @Query("""
        SELECT * FROM time_statistics 
        WHERE reference_type = :referenceType 
        AND reference_id = :referenceId 
        ORDER BY date DESC
    """)
    fun getTimeStatistics(referenceType: String, referenceId: Long): Flow<List<TimeStatEntity>>
    
    @Query("""
        SELECT * FROM time_statistics 
        WHERE date >= :startDate 
        AND date <= :endDate 
        ORDER BY date ASC
    """)
    fun getTimeStatisticsBetweenDates(startDate: Long, endDate: Long): Flow<List<TimeStatEntity>>
    
    // ==================== 时间目标 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeGoal(timeGoal: TimeGoalEntity): Long
    
    @Update
    suspend fun updateTimeGoal(timeGoal: TimeGoalEntity)
    
    @Delete
    suspend fun deleteTimeGoal(timeGoal: TimeGoalEntity)
    
    @Query("SELECT * FROM time_goals ORDER BY created_at DESC")
    fun getAllTimeGoals(): Flow<List<TimeGoalEntity>>
    
    @Query("SELECT * FROM time_goals WHERE id = :id")
    suspend fun getTimeGoalById(id: Long): TimeGoalEntity?
    
    @Query("SELECT * FROM time_goals WHERE is_completed = 0 ORDER BY start_date")
    fun getActiveTimeGoals(): Flow<List<TimeGoalEntity>>
    
    @Query("""
        SELECT * FROM time_goals 
        WHERE reference_type = :referenceType 
        AND reference_id = :referenceId
        ORDER BY created_at DESC
    """)
    fun getTimeGoalsByReference(referenceType: String, referenceId: Long): Flow<List<TimeGoalEntity>>
    
    @Query("""
        SELECT * FROM time_goals 
        WHERE category = :category
        ORDER BY created_at DESC
    """)
    fun getTimeGoalsByCategory(category: String): Flow<List<TimeGoalEntity>>
    
    // ==================== 复合查询 ====================
    
    @Query("""
        SELECT category, SUM(duration_seconds) as total 
        FROM time_entries 
        WHERE start_time >= :startTime 
        AND (end_time <= :endTime OR end_time IS NULL)
        GROUP BY category
    """)
    suspend fun getCategoryBreakdown(startTime: Long, endTime: Long): List<CategoryStat>
    
    @Transaction
    @Query("""
        SELECT COUNT(*) 
        FROM time_entries 
        WHERE is_pomodoro = 1 
        AND end_time IS NOT NULL 
        AND start_time >= :startTime 
        AND end_time <= :endTime
    """)
    suspend fun getTotalCompletedPomodoros(startTime: Long, endTime: Long): Int
    
    @Query("""
        SELECT SUM(duration_seconds) 
        FROM time_entries 
        WHERE start_time >= :startTime 
        AND (end_time <= :endTime OR end_time IS NULL)
    """)
    suspend fun getTotalTrackedTime(startTime: Long, endTime: Long): Long?
} 