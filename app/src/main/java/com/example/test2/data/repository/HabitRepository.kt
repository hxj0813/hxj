package com.example.test2.data.repository

import com.example.test2.data.local.dao.HabitDao
import com.example.test2.data.local.dao.HabitLogDao
import com.example.test2.data.local.entity.HabitEntity
import com.example.test2.data.local.entity.HabitLogEntity
import com.example.test2.data.local.entity.HabitCategory
import com.example.test2.data.local.entity.FrequencyType
import com.example.test2.data.local.entity.HabitPriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 习惯仓库
 * 封装数据访问层，提供习惯相关的业务逻辑
 */
@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) {
    // 习惯相关操作
    fun getAllActiveHabits(): Flow<List<HabitEntity>> = habitDao.getAllActiveHabits()
    
    fun getAllHabits(): Flow<List<HabitEntity>> = habitDao.getAllHabits()
    
    fun getHabitById(habitId: String): Flow<HabitEntity?> = habitDao.getHabitById(habitId)
    
    fun getHabitsByCategory(category: HabitCategory): Flow<List<HabitEntity>> = 
        habitDao.getHabitsByCategory(category.ordinal)
    
    fun getHabitsNotCompletedToday(): Flow<List<HabitEntity>> = habitDao.getHabitsNotCompletedToday()
    
    fun getHabitsWithStreakAtLeast(minStreak: Int): Flow<List<HabitEntity>> = 
        habitDao.getHabitsWithStreakAtLeast(minStreak)
    
    fun getHabitsWithReminderInTimeRange(startTime: Date, endTime: Date): Flow<List<HabitEntity>> =
        habitDao.getHabitsWithReminderInTimeRange(startTime, endTime)
    
    suspend fun createHabit(
        title: String,
        description: String? = null,
        category: HabitCategory = HabitCategory.OTHER,
        icon: String? = null,
        color: Long = 0xFF4CAF50,
        frequencyType: FrequencyType = FrequencyType.DAILY,
        frequencyCount: Int = 1,
        frequencyDays: List<Int> = emptyList(),
        timeOfDay: Date? = null,
        reminder: Boolean = false,
        reminderTime: Date? = null,
        priority: HabitPriority = HabitPriority.MEDIUM,
        associatedGoalId: Long? = null,
        tags: List<String> = emptyList()
    ): String {
        val id = UUID.randomUUID().toString()
        val habit = HabitEntity(
            id = id,
            title = title,
            description = description,
            category = category.ordinal,
            icon = icon,
            color = color,
            frequencyType = frequencyType.ordinal,
            frequencyCount = frequencyCount,
            frequencyDaysJson = if (frequencyDays.isNotEmpty()) { 
                com.google.gson.Gson().toJson(frequencyDays) 
            } else null,
            timeOfDay = timeOfDay,
            reminder = reminder,
            reminderTime = reminderTime,
            priority = priority.ordinal,
            associatedGoalId = associatedGoalId,
            tagsJson = if (tags.isNotEmpty()) {
                com.google.gson.Gson().toJson(tags)
            } else null
        )
        habitDao.insertHabit(habit)
        return id
    }
    
    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }
    
    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
    }
    
    suspend fun archiveHabit(habitId: String, archived: Boolean) {
        habitDao.updateHabitArchivedStatus(habitId, archived, Date())
    }
    
    /**
     * 完成习惯打卡
     * @return 返回一个Triple，包含(当前连续天数, 是否创建了新纪录, 总完成次数)
     */
    suspend fun completeHabit(
        habitId: String, 
        completed: Boolean = true,
        note: String? = null,
        mood: Int? = null,
        difficulty: Int? = null
    ): Triple<Int, Boolean, Int> {
        val now = Date()
        val habit = habitDao.getHabitById(habitId).first() ?: return Triple(0, false, 0)
        
        var newStreak = habit.currentStreak
        var createNewRecord = false
        var totalCompletionsIncrement = 0
        
        if (completed) {
            // 增加连续天数
            newStreak = habit.currentStreak + 1
            createNewRecord = newStreak > habit.bestStreak
            totalCompletionsIncrement = 1
            
            // 创建打卡记录
            val log = HabitLogEntity.create(
                habitId = habitId,
                completedDate = now,
                note = note,
                mood = mood,
                difficulty = difficulty
            )
            habitLogDao.insertLog(log)
        } else {
            // 取消完成，减少连续天数
            newStreak = 0
            
            // 删除当天的打卡记录
            val todayLog = habitLogDao.getLogForHabitOnDate(habitId, now)
            todayLog?.let { habitLogDao.deleteLog(it) }
        }
        
        // 更新习惯状态
        habitDao.updateHabitCompletion(
            habitId = habitId,
            completed = completed,
            completionDate = now,
            newStreak = newStreak,
            incrementCompletions = totalCompletionsIncrement,
            updateDate = now
        )
        
        return Triple(newStreak, createNewRecord, habit.totalCompletions + totalCompletionsIncrement)
    }
    
    /**
     * 重置所有习惯的今日完成状态
     * 通常在每天零点调用
     */
    suspend fun resetDailyCompletionStatus() {
        habitDao.resetAllHabitsCompletedToday()
    }
    
    /**
     * 检查并重置中断的连续记录
     * 当用户超过一天未完成习惯时，重置连续记录
     */
    suspend fun checkAndResetBrokenStreaks() {
        habitDao.resetBrokenStreaks(Date())
    }
    
    // 习惯日志相关操作
    fun getLogsByHabitId(habitId: String): Flow<List<HabitLogEntity>> = 
        habitLogDao.getLogsByHabitId(habitId)
    
    fun getLogsByHabitIdAndDateRange(habitId: String, startDate: Date, endDate: Date): Flow<List<HabitLogEntity>> =
        habitLogDao.getLogsByHabitIdAndDateRange(habitId, startDate, endDate)
    
    fun getLogsByDate(date: Date): Flow<List<HabitLogEntity>> = 
        habitLogDao.getLogsByDate(date)
    
    suspend fun getLogsCountInDateRange(startDate: Date, endDate: Date): Int =
        habitLogDao.getLogsCountInDateRange(startDate, endDate)
    
    suspend fun isHabitCompletedOnDate(habitId: String, date: Date): Boolean =
        habitLogDao.isHabitCompletedOnDate(habitId, date)
    
    suspend fun getLastLogForHabit(habitId: String): HabitLogEntity? =
        habitLogDao.getLastLogForHabit(habitId)
    
    suspend fun updateHabitLog(log: HabitLogEntity) {
        habitLogDao.updateLog(log)
    }
    
    suspend fun deleteHabitLog(log: HabitLogEntity) {
        habitLogDao.deleteLog(log)
    }
    
    /**
     * 获取过去N天的习惯统计数据
     */
    suspend fun getHabitStatisticsForLastDays(days: Int): Map<String, Int> {
        val calendar = Calendar.getInstance()
        val endDate = Date()
        
        calendar.time = endDate
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startDate = calendar.time
        
        val totalLogs = habitLogDao.getLogsCountInDateRange(startDate, endDate)
        val habitsWithLongStreaks = habitDao.getHabitsWithStreakAtLeast(7).first().size
        
        return mapOf(
            "totalCompletions" to totalLogs,
            "habitsWithLongStreaks" to habitsWithLongStreaks
        )
    }
    
    /**
     * 获取习惯的月度完成情况
     */
    suspend fun getHabitMonthlyCompletion(habitId: String, year: Int, month: Int): List<Boolean> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.add(Calendar.DAY_OF_MONTH, daysInMonth)
        val endDate = calendar.time
        
        val logs = habitLogDao.getLogsByHabitIdAndDateRange(habitId, startDate, endDate).first()
        
        // 计算每天是否完成
        val completionDays = mutableListOf<Boolean>()
        calendar.time = startDate
        
        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dayStart = calendar.time
            val dayCompleted = logs.any { log ->
                val logCalendar = Calendar.getInstance()
                logCalendar.time = log.completedDate
                logCalendar.get(Calendar.YEAR) == year &&
                        logCalendar.get(Calendar.MONTH) == month &&
                        logCalendar.get(Calendar.DAY_OF_MONTH) == day
            }
            completionDays.add(dayCompleted)
        }
        
        return completionDays
    }
} 