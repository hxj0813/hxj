package com.example.test2.data.repository

import com.example.test2.data.local.dao.TaskDao
import com.example.test2.data.local.entity.TaskEntity
import com.example.test2.data.local.entity.TaskType
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 任务仓库类
 * 封装对任务数据的访问逻辑
 */
@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    /**
     * 获取所有活动任务（未完成且未归档）
     */
    fun getActiveTasks(): Flow<List<TaskEntity>> {
        return taskDao.getActiveTasks()
    }
    
    /**
     * 获取所有已完成但未归档的任务
     */
    fun getCompletedTasks(): Flow<List<TaskEntity>> {
        return taskDao.getCompletedTasks()
    }
    
    /**
     * 获取所有已归档的任务
     */
    fun getArchivedTasks(): Flow<List<TaskEntity>> {
        return taskDao.getArchivedTasks()
    }
    
    /**
     * 根据任务类型获取任务列表
     */
    fun getTasksByType(taskType: TaskType): Flow<List<TaskEntity>> {
        return taskDao.getTasksByType(taskType.ordinal)
    }
    
    /**
     * 获取与特定目标关联的任务
     */
    fun getTasksByGoalId(goalId: Long): Flow<List<TaskEntity>> {
        return taskDao.getTasksByGoalId(goalId)
    }
    
    /**
     * 获取所有待办任务（未完成且有截止日期）
     */
    fun getDueTasks(): Flow<List<TaskEntity>> {
        return taskDao.getDueTasks()
    }
    
    /**
     * 获取今日待办任务（未完成且截止日期为今天）
     */
    fun getTodayTasks(): Flow<List<TaskEntity>> {
        val calendar = java.util.Calendar.getInstance()
        // 设置为今天的开始时间
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        // 设置为今天的结束时间
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time
        
        return taskDao.getTodayTasks(startOfDay, endOfDay)
    }
    
    /**
     * 获取逾期任务（未完成且截止日期已过）
     */
    fun getOverdueTasks(): Flow<List<TaskEntity>> {
        return taskDao.getOverdueTasks(Date())
    }
    
    /**
     * 搜索任务（标题和描述）
     */
    fun searchTasks(query: String): Flow<List<TaskEntity>> {
        return taskDao.searchTasks(query)
    }
    
    /**
     * 通过ID获取单个任务
     */
    suspend fun getTaskById(taskId: String): TaskEntity? {
        return taskDao.getTaskById(taskId)
    }
    
    /**
     * 获取任务数量统计
     */
    suspend fun getTaskCount(isCompleted: Boolean): Int {
        return taskDao.getTaskCount(isCompleted)
    }
    
    /**
     * 获取任务类型统计
     */
    suspend fun getTaskTypeCount(taskType: TaskType): Int {
        return taskDao.getTaskTypeCount(taskType.ordinal)
    }
    
    /**
     * 创建新任务
     */
    suspend fun createTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }
    
    /**
     * 更新任务
     */
    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }
    
    /**
     * 删除任务
     */
    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }
    
    /**
     * 更新任务完成状态
     */
    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        val completedAt = if (isCompleted) Date() else null
        taskDao.updateTaskCompletion(taskId, isCompleted, completedAt, Date())
    }
    
    /**
     * 归档任务
     */
    suspend fun archiveTask(taskId: String) {
        taskDao.archiveTask(taskId, Date())
    }
    
    /**
     * 取消归档任务
     */
    suspend fun unarchiveTask(taskId: String) {
        taskDao.unarchiveTask(taskId, Date())
    }
    
    /**
     * 批量删除已归档任务
     */
    suspend fun deleteAllArchivedTasks() {
        taskDao.deleteAllArchivedTasks()
    }
    
    /**
     * 批量删除已完成任务
     */
    suspend fun deleteAllCompletedTasks() {
        taskDao.deleteAllCompletedTasks()
    }
} 