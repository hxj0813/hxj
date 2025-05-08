package com.example.test2.data.repository

import com.example.test2.data.local.dao.CheckInTaskDao
import com.example.test2.data.local.entity.CheckInTaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 打卡任务仓库类
 * 封装对打卡任务数据的访问逻辑
 */
@Singleton
class CheckInTaskRepository @Inject constructor(
    private val checkInTaskDao: CheckInTaskDao
) {
    /**
     * 通过任务ID获取打卡任务
     */
    suspend fun getCheckInTaskById(taskId: String): CheckInTaskEntity? {
        return checkInTaskDao.getCheckInTaskById(taskId)
    }
    
    /**
     * 获取所有打卡任务
     */
    fun getAllCheckInTasks(): Flow<List<CheckInTaskEntity>> {
        return checkInTaskDao.getAllCheckInTasks()
    }
    
    /**
     * 获取所有当前有连续打卡记录的任务
     */
    fun getTasksWithStreak(): Flow<List<CheckInTaskEntity>> {
        return checkInTaskDao.getTasksWithStreak()
    }
    
    /**
     * 获取需要今日完成的任务
     */
    fun getTasksDueToday(): Flow<List<CheckInTaskEntity>> {
        return checkInTaskDao.getTasksDueToday()
    }
    
    /**
     * 获取已有最长打卡记录的任务
     */
    fun getTasksWithBestStreak(limit: Int = 5): Flow<List<CheckInTaskEntity>> {
        return checkInTaskDao.getTasksWithBestStreak(limit)
    }
    
    /**
     * 创建新打卡任务
     */
    suspend fun createCheckInTask(checkInTask: CheckInTaskEntity) {
        checkInTaskDao.insertCheckInTask(checkInTask)
    }
    
    /**
     * 更新打卡任务
     */
    suspend fun updateCheckInTask(checkInTask: CheckInTaskEntity) {
        checkInTaskDao.updateCheckInTask(checkInTask)
    }
    
    /**
     * 删除打卡任务
     */
    suspend fun deleteCheckInTask(checkInTask: CheckInTaskEntity) {
        checkInTaskDao.deleteCheckInTask(checkInTask)
    }
    
    /**
     * 更新任务完成状态
     */
    suspend fun updateTaskCompletion(taskId: String, completedToday: Boolean) {
        checkInTaskDao.updateTaskCompletion(taskId, completedToday, Date())
    }
    
    /**
     * 重置每日完成状态
     * 通常由定时任务在每天零点调用
     */
    suspend fun resetDailyCompletionStatus() {
        checkInTaskDao.resetDailyCompletionStatus()
    }
    
    /**
     * 重置中断的连续记录
     * 通常由定时任务在每天零点调用
     */
    suspend fun resetBrokenStreaks() {
        checkInTaskDao.resetBrokenStreaks(Date())
    }
} 