package com.example.test2.data.repository

import com.example.test2.data.local.dao.DailyCount
import com.example.test2.data.local.dao.DailyMinutes
import com.example.test2.data.local.dao.TaskLogDao
import com.example.test2.data.local.entity.TaskLogEntity
import com.example.test2.data.local.entity.TaskType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 任务日志仓库类
 * 封装对任务日志数据的访问逻辑
 */
@Singleton
class TaskLogRepository @Inject constructor(
    private val taskLogDao: TaskLogDao
) {
    /**
     * 获取指定任务的所有日志
     */
    fun getTaskLogs(taskId: String): Flow<List<TaskLogEntity>> {
        return taskLogDao.getTaskLogs(taskId)
    }
    
    /**
     * 获取指定日期范围内的任务日志
     */
    fun getLogsInDateRange(startDate: Date, endDate: Date): Flow<List<TaskLogEntity>> {
        return taskLogDao.getLogsInDateRange(startDate, endDate)
    }
    
    /**
     * 获取指定日期范围内的特定类型任务日志
     */
    fun getLogsByTypeInDateRange(
        taskType: TaskType,
        startDate: Date, 
        endDate: Date
    ): Flow<List<TaskLogEntity>> {
        return taskLogDao.getLogsByTypeInDateRange(taskType.ordinal, startDate, endDate)
    }
    
    /**
     * 获取特定任务的日志计数
     */
    suspend fun getTaskLogCount(taskId: String): Int {
        return taskLogDao.getTaskLogCount(taskId)
    }
    
    /**
     * 获取今日已完成的任务数量
     */
    suspend fun getTodayCompletedTaskCount(): Int {
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
        
        return taskLogDao.getTodayCompletedTaskCount(startOfDay, endOfDay)
    }
    
    /**
     * 获取特定任务类型的今日已完成任务数量
     */
    suspend fun getTodayCompletedTaskCountByType(taskType: TaskType): Int {
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
        
        return taskLogDao.getTodayCompletedTaskCountByType(taskType.ordinal, startOfDay, endOfDay)
    }
    
    /**
     * 获取今日总专注时间
     */
    suspend fun getTodayTotalFocusTime(): Int {
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
        
        return taskLogDao.getTodayTotalFocusTime(startOfDay, endOfDay) ?: 0
    }
    
    /**
     * 获取指定日期范围内的每日完成任务数量
     * 用于统计图表数据
     */
    suspend fun getDailyTaskCompletionCounts(startDate: Date, endDate: Date): Map<String, Int> {
        val dailyCounts = taskLogDao.getDailyTaskCompletionCounts(startDate, endDate)
        return dailyCounts.associate { it.date to it.count }
    }
    
    /**
     * 获取指定日期范围内的每日专注时间
     * 用于统计图表数据
     */
    suspend fun getDailyFocusMinutes(startDate: Date, endDate: Date): Map<String, Int> {
        val dailyMinutes = taskLogDao.getDailyFocusMinutes(startDate, endDate)
        return dailyMinutes.associate { it.date to it.minutes }
    }
    
    /**
     * 获取心情评分平均值
     */
    suspend fun getAverageMood(startDate: Date, endDate: Date): Float {
        return taskLogDao.getAverageMood(startDate, endDate) ?: 0f
    }
    
    /**
     * 创建任务日志
     */
    suspend fun createLog(log: TaskLogEntity) {
        taskLogDao.insertLog(log)
    }
    
    /**
     * 更新任务日志
     */
    suspend fun updateLog(log: TaskLogEntity) {
        taskLogDao.updateLog(log)
    }
    
    /**
     * 删除任务日志
     */
    suspend fun deleteLog(log: TaskLogEntity) {
        taskLogDao.deleteLog(log)
    }
    
    /**
     * 删除指定任务的所有日志
     */
    suspend fun deleteLogsByTaskId(taskId: String) {
        taskLogDao.deleteLogsByTaskId(taskId)
    }
    
    /**
     * 根据ID获取日志
     */
    suspend fun getLogById(logId: String): TaskLogEntity? {
        return taskLogDao.getLogById(logId)
    }
    
    /**
     * 更新日志备注
     */
    suspend fun updateLogNote(logId: String, note: String?) {
        taskLogDao.updateLogNote(logId, note)
    }
    
    /**
     * 更新日志心情评分
     */
    suspend fun updateLogMood(logId: String, mood: Int?) {
        taskLogDao.updateLogMood(logId, mood)
    }
    
    /**
     * 获取最近一周的日期范围
     */
    fun getLastWeekDateRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        // 当前日期（结束日期）
        val endDate = calendar.time
        
        // 往前推7天（开始日期）
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        return Pair(startDate, endDate)
    }
    
    /**
     * 获取最近一个月的日期范围
     */
    fun getLastMonthDateRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        // 当前日期（结束日期）
        val endDate = calendar.time
        
        // 往前推30天（开始日期）
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        return Pair(startDate, endDate)
    }
} 