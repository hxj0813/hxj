package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.test2.data.local.entity.TaskEntity
import com.example.test2.data.local.entity.TaskPriority
import com.example.test2.data.local.entity.TaskType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 任务DAO接口
 * 提供对基础任务表的访问方法
 */
@Dao
interface TaskDao {
    /**
     * 获取所有活动任务（未完成且未归档）
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isArchived = 0 ORDER BY dueDate ASC, priority ASC, createdAt DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>
    
    /**
     * 获取所有已完成但未归档的任务
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND isArchived = 0 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>
    
    /**
     * 获取所有已归档的任务
     */
    @Query("SELECT * FROM tasks WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedTasks(): Flow<List<TaskEntity>>
    
    /**
     * 根据任务类型获取任务列表
     */
    @Query("SELECT * FROM tasks WHERE taskType = :taskType AND isArchived = 0 ORDER BY isCompleted ASC, dueDate ASC")
    fun getTasksByType(taskType: Int): Flow<List<TaskEntity>>
    
    /**
     * 获取与特定目标关联的任务
     */
    @Query("SELECT * FROM tasks WHERE goalId = :goalId AND isArchived = 0 ORDER BY isCompleted ASC, dueDate ASC")
    fun getTasksByGoalId(goalId: Long): Flow<List<TaskEntity>>
    
    /**
     * 获取所有待办任务（未完成且有截止日期）
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isArchived = 0 AND dueDate IS NOT NULL ORDER BY dueDate ASC")
    fun getDueTasks(): Flow<List<TaskEntity>>
    
    /**
     * 获取今日待办任务（未完成且截止日期为今天）
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isArchived = 0 AND dueDate BETWEEN :startOfDay AND :endOfDay ORDER BY priority ASC")
    fun getTodayTasks(startOfDay: Date, endOfDay: Date): Flow<List<TaskEntity>>
    
    /**
     * 获取逾期任务（未完成且截止日期已过）
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isArchived = 0 AND dueDate < :currentDate ORDER BY dueDate ASC")
    fun getOverdueTasks(currentDate: Date): Flow<List<TaskEntity>>
    
    /**
     * 搜索任务（标题和描述）
     */
    @Query("SELECT * FROM tasks WHERE isArchived = 0 AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY isCompleted ASC, dueDate ASC")
    fun searchTasks(query: String): Flow<List<TaskEntity>>
    
    /**
     * 通过ID获取单个任务
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?
    
    /**
     * 获取任务数量统计
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = :isCompleted AND isArchived = 0")
    suspend fun getTaskCount(isCompleted: Boolean): Int
    
    /**
     * 获取任务类型统计
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE taskType = :taskType AND isArchived = 0")
    suspend fun getTaskTypeCount(taskType: Int): Int
    
    /**
     * 插入任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long
    
    /**
     * 更新任务
     */
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    /**
     * 删除任务
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    /**
     * 更新任务完成状态
     */
    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean, completedAt: Date?, updatedAt: Date)
    
    /**
     * 归档任务
     */
    @Query("UPDATE tasks SET isArchived = 1, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun archiveTask(taskId: String, updatedAt: Date)
    
    /**
     * 取消归档任务
     */
    @Query("UPDATE tasks SET isArchived = 0, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun unarchiveTask(taskId: String, updatedAt: Date)
    
    /**
     * 批量删除已归档任务
     */
    @Query("DELETE FROM tasks WHERE isArchived = 1")
    suspend fun deleteAllArchivedTasks()
    
    /**
     * 批量删除已完成任务
     */
    @Query("DELETE FROM tasks WHERE isCompleted = 1 AND isArchived = 0")
    suspend fun deleteAllCompletedTasks()
} 