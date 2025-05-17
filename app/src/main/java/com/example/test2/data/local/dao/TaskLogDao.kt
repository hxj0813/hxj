package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.local.entity.TaskLogEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 任务日志DAO接口
 * 提供对任务日志表的访问方法
 */
@Dao
interface TaskLogDao {
    /**
     * 获取指定任务的所有日志
     */
    @Query("SELECT * FROM task_logs WHERE taskId = :taskId ORDER BY completedDate DESC")
    fun getTaskLogs(taskId: String): Flow<List<TaskLogEntity>>
    
    /**
     * 获取指定日期范围内的任务日志
     */
    @Query("SELECT * FROM task_logs WHERE completedDate BETWEEN :startDate AND :endDate ORDER BY completedDate DESC")
    fun getLogsInDateRange(startDate: Date, endDate: Date): Flow<List<TaskLogEntity>>
    
    /**
     * 获取指定日期范围内的特定类型任务日志
     */
    @Query("SELECT * FROM task_logs WHERE taskType = :taskType AND completedDate BETWEEN :startDate AND :endDate ORDER BY completedDate DESC")
    fun getLogsByTypeInDateRange(taskType: Int, startDate: Date, endDate: Date): Flow<List<TaskLogEntity>>
    
    /**
     * 获取特定任务的日志计数
     */
    @Query("SELECT COUNT(*) FROM task_logs WHERE taskId = :taskId")
    suspend fun getTaskLogCount(taskId: String): Int
    
    /**
     * 获取今日已完成的任务数量
     */
    @Query("SELECT COUNT(DISTINCT taskId) FROM task_logs WHERE completedDate BETWEEN :startOfDay AND :endOfDay")
    suspend fun getTodayCompletedTaskCount(startOfDay: Date, endOfDay: Date): Int
    
    /**
     * 获取特定任务类型的今日已完成任务数量
     */
    @Query("SELECT COUNT(DISTINCT taskId) FROM task_logs WHERE taskType = :taskType AND completedDate BETWEEN :startOfDay AND :endOfDay")
    suspend fun getTodayCompletedTaskCountByType(taskType: Int, startOfDay: Date, endOfDay: Date): Int
    
    /**
     * 获取今日总专注时间
     */
    @Query("SELECT SUM(focusMinutes) FROM task_logs WHERE completedDate BETWEEN :startOfDay AND :endOfDay")
    suspend fun getTodayTotalFocusTime(startOfDay: Date, endOfDay: Date): Int?
    
    /**
     * 获取指定日期范围内的每日完成任务数量
     * 返回格式为 List<DailyCount>
     */
    @Query("""
        SELECT strftime('%Y-%m-%d', completedDate / 1000, 'unixepoch') as date, COUNT(DISTINCT taskId) as count 
        FROM task_logs 
        WHERE completedDate BETWEEN :startDate AND :endDate 
        GROUP BY date 
        ORDER BY date
    """)
    suspend fun getDailyTaskCompletionCounts(startDate: Date, endDate: Date): List<DailyCount>
    
    /**
     * 获取指定日期范围内的每日专注时间
     * 返回格式为 List<DailyMinutes>
     */
    @Query("""
        SELECT strftime('%Y-%m-%d', completedDate / 1000, 'unixepoch') as date, SUM(focusMinutes) as minutes 
        FROM task_logs 
        WHERE completedDate BETWEEN :startDate AND :endDate 
        GROUP BY date 
        ORDER BY date
    """)
    suspend fun getDailyFocusMinutes(startDate: Date, endDate: Date): List<DailyMinutes>
    
    /**
     * 获取心情评分平均值
     */
    @Query("""
        SELECT AVG(mood) 
        FROM task_logs 
        WHERE mood IS NOT NULL AND completedDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getAverageMood(startDate: Date, endDate: Date): Float?
    
    /**
     * 插入任务日志
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TaskLogEntity)
    
    /**
     * 更新任务日志
     */
    @Update
    suspend fun updateLog(log: TaskLogEntity)
    
    /**
     * 删除任务日志
     */
    @Delete
    suspend fun deleteLog(log: TaskLogEntity)
    
    /**
     * 删除指定任务的所有日志
     */
    @Query("DELETE FROM task_logs WHERE taskId = :taskId")
    suspend fun deleteLogsByTaskId(taskId: String)
    
    /**
     * 根据ID获取日志
     */
    @Query("SELECT * FROM task_logs WHERE id = :logId")
    suspend fun getLogById(logId: String): TaskLogEntity?
    
    /**
     * 更新日志备注
     */
    @Query("UPDATE task_logs SET note = :note WHERE id = :logId")
    suspend fun updateLogNote(logId: String, note: String?)
    
    /**
     * 更新日志心情评分
     */
    @Query("UPDATE task_logs SET mood = :mood WHERE id = :logId")
    suspend fun updateLogMood(logId: String, mood: Int?)
    
    /**
     * 获取特定任务在指定日期范围内完成的番茄钟数量
     */
    @Query("""
        SELECT SUM(pomodoroCount) 
        FROM task_logs 
        WHERE taskId = :taskId AND completedDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getTaskPomodoroCountInDateRange(taskId: String, startDate: Date, endDate: Date): Int?
}

/**
 * 每日完成任务数量数据类
 */
data class DailyCount(
    val date: String,
    val count: Int
)

/**
 * 每日专注时间数据类
 */
data class DailyMinutes(
    val date: String,
    val minutes: Int
) 