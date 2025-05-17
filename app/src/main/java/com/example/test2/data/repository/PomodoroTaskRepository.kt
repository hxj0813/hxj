package com.example.test2.data.repository

import com.example.test2.data.local.dao.PomodoroTaskDao
import com.example.test2.data.local.dao.TaskLogDao
import com.example.test2.data.local.entity.PomodoroTaskEntity
import com.example.test2.data.local.entity.TagCategory
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar

/**
 * 番茄钟任务仓库类
 * 封装对番茄钟任务数据的访问逻辑
 */
@Singleton
class PomodoroTaskRepository @Inject constructor(
    private val pomodoroTaskDao: PomodoroTaskDao,
    private val taskLogDao: TaskLogDao
) {
    /**
     * 通过任务ID获取番茄钟任务
     */
    suspend fun getPomodoroTaskById(taskId: String): PomodoroTaskEntity? {
        return pomodoroTaskDao.getPomodoroTaskById(taskId)
    }
    
    /**
     * 获取所有番茄钟任务
     */
    fun getAllPomodoroTasks(): Flow<List<PomodoroTaskEntity>> {
        return pomodoroTaskDao.getAllPomodoroTasks()
    }
    
    /**
     * 获取按标签分类的任务
     */
    fun getTasksByCategory(category: TagCategory): Flow<List<PomodoroTaskEntity>> {
        return pomodoroTaskDao.getTasksByCategory(category.ordinal)
    }
    
    /**
     * 获取按标签ID的任务
     */
    fun getTasksByTagId(tagId: String): Flow<List<PomodoroTaskEntity>> {
        return pomodoroTaskDao.getTasksByTagId(tagId)
    }
    
    /**
     * 获取今日有专注时间的任务
     */
    fun getTodayFocusTasks(): Flow<List<PomodoroTaskEntity>> {
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
        
        return pomodoroTaskDao.getTodayFocusTasks(startOfDay, endOfDay)
    }
    
    /**
     * 获取专注时间最长的任务
     */
    fun getMostFocusedTasks(limit: Int = 5): Flow<List<PomodoroTaskEntity>> {
        return pomodoroTaskDao.getMostFocusedTasks(limit)
    }
    
    /**
     * 按类别获取累计专注时间
     */
    suspend fun getTotalFocusTimeByCategory(category: TagCategory): Long {
        return pomodoroTaskDao.getTotalFocusTimeByCategory(category.ordinal) ?: 0L
    }
    
    /**
     * 获取总专注时间
     */
    suspend fun getTotalFocusTime(): Long {
        return pomodoroTaskDao.getTotalFocusTime() ?: 0L
    }
    
    /**
     * 获取特定日期范围内的专注时间
     */
    suspend fun getFocusTimeInDateRange(startDate: Date, endDate: Date): Long {
        return pomodoroTaskDao.getFocusTimeInDateRange(startDate, endDate) ?: 0L
    }
    
    /**
     * 获取今日特定任务的完成番茄钟数量
     * @param taskId 任务ID
     * @return 今日完成的番茄钟数量
     */
    suspend fun getTodayPomodoroCount(taskId: String): Int {
        val calendar = Calendar.getInstance()
        // 设置为今天的开始时间
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        // 设置为今天的结束时间
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time
        
        // 从任务日志表中查询今日这个任务的番茄钟完成数量
        return taskLogDao.getTaskPomodoroCountInDateRange(taskId, startOfDay, endOfDay) ?: 0
    }
    
    /**
     * 创建番茄钟任务
     */
    suspend fun createPomodoroTask(pomodoroTask: PomodoroTaskEntity) {
        pomodoroTaskDao.insertPomodoroTask(pomodoroTask)
    }
    
    /**
     * 更新番茄钟任务
     */
    suspend fun updatePomodoroTask(pomodoroTask: PomodoroTaskEntity) {
        pomodoroTaskDao.updatePomodoroTask(pomodoroTask)
    }
    
    /**
     * 删除番茄钟任务
     */
    suspend fun deletePomodoroTask(pomodoroTask: PomodoroTaskEntity) {
        pomodoroTaskDao.deletePomodoroTask(pomodoroTask)
    }
    
    /**
     * 增加番茄钟专注时间
     */
    suspend fun addFocusTime(taskId: String, focusMinutes: Int, pomodoroCount: Int = 1) {
        pomodoroTaskDao.addFocusTime(taskId, focusMinutes, pomodoroCount, Date())
    }
    
    /**
     * 重置所有番茄钟任务的已完成番茄数
     * 通常由定时任务在每天零点调用
     */
    suspend fun resetAllCompletedPomodoros() {
        pomodoroTaskDao.resetAllCompletedPomodoros()
    }
    
    /**
     * 增加已完成番茄钟数量
     */
    suspend fun incrementCompletedPomodoros(taskId: String) {
        pomodoroTaskDao.incrementCompletedPomodoros(taskId)
    }
    
    /**
     * 修改任务标签
     */
    suspend fun updateTaskTag(taskId: String, tagId: String?, category: TagCategory) {
        pomodoroTaskDao.updateTaskTag(taskId, tagId, category.ordinal)
    }
} 