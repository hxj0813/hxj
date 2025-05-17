package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.local.entity.PomodoroTaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 番茄钟任务DAO接口
 * 提供对番茄钟任务表的访问方法
 */
@Dao
interface PomodoroTaskDao {
    /**
     * 通过任务ID获取番茄钟任务
     */
    @Query("SELECT * FROM pomodoro_tasks WHERE taskId = :taskId")
    suspend fun getPomodoroTaskById(taskId: String): PomodoroTaskEntity?
    
    /**
     * 获取所有番茄钟任务
     */
    @Query("""
        SELECT pomodoro_tasks.* FROM pomodoro_tasks 
        INNER JOIN tasks ON pomodoro_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0
        ORDER BY tasks.isCompleted ASC, tasks.dueDate ASC
    """)
    fun getAllPomodoroTasks(): Flow<List<PomodoroTaskEntity>>
    
    /**
     * 获取按标签分类的任务
     */
    @Query("""
        SELECT pomodoro_tasks.* FROM pomodoro_tasks 
        INNER JOIN tasks ON pomodoro_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0 AND pomodoro_tasks.tagCategory = :category
        ORDER BY tasks.isCompleted ASC, tasks.dueDate ASC
    """)
    fun getTasksByCategory(category: Int): Flow<List<PomodoroTaskEntity>>
    
    /**
     * 获取按标签ID的任务
     */
    @Query("""
        SELECT pomodoro_tasks.* FROM pomodoro_tasks 
        INNER JOIN tasks ON pomodoro_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0 AND pomodoro_tasks.tagId = :tagId
        ORDER BY tasks.isCompleted ASC, tasks.dueDate ASC
    """)
    fun getTasksByTagId(tagId: String): Flow<List<PomodoroTaskEntity>>
    
    /**
     * 获取今日有专注时间的任务
     */
    @Query("""
        SELECT pomodoro_tasks.* FROM pomodoro_tasks 
        INNER JOIN tasks ON pomodoro_tasks.taskId = tasks.id
        WHERE pomodoro_tasks.lastSessionDate BETWEEN :startOfDay AND :endOfDay
        ORDER BY pomodoro_tasks.totalFocusTime DESC
    """)
    fun getTodayFocusTasks(startOfDay: Date, endOfDay: Date): Flow<List<PomodoroTaskEntity>>
    
    /**
     * 获取专注时间最长的任务
     */
    @Query("""
        SELECT pomodoro_tasks.* FROM pomodoro_tasks 
        INNER JOIN tasks ON pomodoro_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0
        ORDER BY pomodoro_tasks.totalFocusTime DESC
        LIMIT :limit
    """)
    fun getMostFocusedTasks(limit: Int): Flow<List<PomodoroTaskEntity>>
    
    /**
     * 按类别获取累计专注时间
     */
    @Query("""
        SELECT SUM(totalFocusTime) FROM pomodoro_tasks 
        INNER JOIN tasks ON pomodoro_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0 AND pomodoro_tasks.tagCategory = :category
    """)
    suspend fun getTotalFocusTimeByCategory(category: Int): Long?
    
    /**
     * 获取总专注时间
     */
    @Query("""
        SELECT SUM(totalFocusTime) FROM pomodoro_tasks 
        INNER JOIN tasks ON pomodoro_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0
    """)
    suspend fun getTotalFocusTime(): Long?
    
    /**
     * 获取特定日期范围内的专注时间
     */
    @Query("""
        SELECT SUM(totalFocusTime) FROM pomodoro_tasks 
        INNER JOIN tasks ON pomodoro_tasks.taskId = tasks.id
        WHERE tasks.isArchived = 0 AND pomodoro_tasks.lastSessionDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getFocusTimeInDateRange(startDate: Date, endDate: Date): Long?
    
    /**
     * 插入番茄钟任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroTask(pomodoroTask: PomodoroTaskEntity)
    
    /**
     * 更新番茄钟任务
     */
    @Update
    suspend fun updatePomodoroTask(pomodoroTask: PomodoroTaskEntity)
    
    /**
     * 删除番茄钟任务
     */
    @Delete
    suspend fun deletePomodoroTask(pomodoroTask: PomodoroTaskEntity)
    
    /**
     * 增加番茄钟专注时间
     */
    @Query("""
        UPDATE pomodoro_tasks
        SET totalFocusTime = totalFocusTime + :focusMinutes,
            completedPomodoros = completedPomodoros + :pomodoroCount,
            lastSessionDate = :sessionDate
        WHERE taskId = :taskId
    """)
    suspend fun addFocusTime(taskId: String, focusMinutes: Int, pomodoroCount: Int, sessionDate: Date)
    
    /**
     * 增加已完成的番茄钟数量
     */
    @Query("""
        UPDATE pomodoro_tasks
        SET completedPomodoros = completedPomodoros + 1
        WHERE taskId = :taskId
    """)
    suspend fun incrementCompletedPomodoros(taskId: String)
    
    /**
     * 重置所有番茄钟任务的已完成番茄数
     * 通常由定时任务在每天零点调用
     */
    @Query("UPDATE pomodoro_tasks SET completedPomodoros = 0")
    suspend fun resetAllCompletedPomodoros()
    
    /**
     * 修改任务标签
     */
    @Query("UPDATE pomodoro_tasks SET tagId = :tagId, tagCategory = :category WHERE taskId = :taskId")
    suspend fun updateTaskTag(taskId: String, tagId: String?, category: Int)
} 