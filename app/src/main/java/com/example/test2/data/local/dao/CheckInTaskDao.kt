package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.local.entity.CheckInTaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 打卡任务DAO接口
 * 提供对打卡任务表的访问方法
 */
@Dao
interface CheckInTaskDao {
    /**
     * 通过任务ID获取打卡任务
     */
    @Query("SELECT * FROM check_in_tasks WHERE taskId = :taskId")
    suspend fun getCheckInTaskById(taskId: String): CheckInTaskEntity?
    
    /**
     * 获取所有打卡任务
     */
    @Query("""
        SELECT check_in_tasks.* FROM check_in_tasks 
        INNER JOIN tasks ON check_in_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0
        ORDER BY tasks.isCompleted ASC, tasks.dueDate ASC
    """)
    fun getAllCheckInTasks(): Flow<List<CheckInTaskEntity>>
    
    /**
     * 获取所有当前有连续打卡记录的任务
     */
    @Query("""
        SELECT check_in_tasks.* FROM check_in_tasks 
        INNER JOIN tasks ON check_in_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0 AND check_in_tasks.currentStreak > 0
        ORDER BY check_in_tasks.currentStreak DESC
    """)
    fun getTasksWithStreak(): Flow<List<CheckInTaskEntity>>
    
    /**
     * 获取需要今日完成的任务
     */
    @Query("""
        SELECT check_in_tasks.* FROM check_in_tasks 
        INNER JOIN tasks ON check_in_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0 AND tasks.isCompleted = 0 AND check_in_tasks.completedToday = 0
        ORDER BY tasks.priority ASC, tasks.createdAt DESC
    """)
    fun getTasksDueToday(): Flow<List<CheckInTaskEntity>>
    
    /**
     * 获取已有最长打卡记录的任务
     */
    @Query("""
        SELECT check_in_tasks.* FROM check_in_tasks 
        INNER JOIN tasks ON check_in_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0
        ORDER BY check_in_tasks.bestStreak DESC
        LIMIT :limit
    """)
    fun getTasksWithBestStreak(limit: Int): Flow<List<CheckInTaskEntity>>
    
    /**
     * 插入打卡任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckInTask(checkInTask: CheckInTaskEntity)
    
    /**
     * 更新打卡任务
     */
    @Update
    suspend fun updateCheckInTask(checkInTask: CheckInTaskEntity)
    
    /**
     * 删除打卡任务
     */
    @Delete
    suspend fun deleteCheckInTask(checkInTask: CheckInTaskEntity)
    
    /**
     * 更新任务完成状态
     */
    @Query("""
        UPDATE check_in_tasks 
        SET completedToday = :completedToday, 
            lastCompletedDate = CASE WHEN :completedToday = 1 THEN :currentDate ELSE lastCompletedDate END,
            currentStreak = CASE 
                WHEN :completedToday = 1 THEN 
                    CASE 
                        WHEN lastCompletedDate IS NULL THEN 1
                        WHEN (strftime('%Y-%m-%d', :currentDate / 1000, 'unixepoch') > strftime('%Y-%m-%d', lastCompletedDate / 1000, 'unixepoch')) THEN currentStreak + 1
                        ELSE currentStreak
                    END
                ELSE currentStreak
            END,
            bestStreak = CASE 
                WHEN :completedToday = 1 AND 
                     (strftime('%Y-%m-%d', :currentDate / 1000, 'unixepoch') > strftime('%Y-%m-%d', IFNULL(lastCompletedDate, 0) / 1000, 'unixepoch')) AND 
                     currentStreak + 1 > bestStreak 
                THEN currentStreak + 1
                ELSE bestStreak
            END,
            totalCompletions = CASE 
                WHEN :completedToday = 1 AND (lastCompletedDate IS NULL OR strftime('%Y-%m-%d', :currentDate / 1000, 'unixepoch') > strftime('%Y-%m-%d', lastCompletedDate / 1000, 'unixepoch'))
                THEN totalCompletions + 1
                ELSE totalCompletions
            END
        WHERE taskId = :taskId
    """)
    suspend fun updateTaskCompletion(taskId: String, completedToday: Boolean, currentDate: Date)
    
    /**
     * 重置每日完成状态
     * 通常由定时任务在每天零点调用
     */
    @Query("UPDATE check_in_tasks SET completedToday = 0")
    suspend fun resetDailyCompletionStatus()
    
    /**
     * 重置中断的连续记录
     * 通常由定时任务在每天零点调用
     */
    @Query("""
        UPDATE check_in_tasks 
        SET currentStreak = 0,
            streakStartDate = NULL
        WHERE lastCompletedDate IS NOT NULL 
        AND strftime('%Y-%m-%d', :currentDate / 1000, 'unixepoch') > strftime('%Y-%m-%d', lastCompletedDate / 1000, 'unixepoch', '+1 day')
    """)
    suspend fun resetBrokenStreaks(currentDate: Date)
} 