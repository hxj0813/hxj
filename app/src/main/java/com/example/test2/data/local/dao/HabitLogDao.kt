package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 习惯打卡记录数据访问对象接口
 */
@Dao
interface HabitLogDao {
    /**
     * 获取指定习惯的所有打卡记录，按日期倒序排列
     */
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY completedDate DESC")
    fun getLogsByHabitId(habitId: String): Flow<List<HabitLogEntity>>
    
    /**
     * 获取指定习惯在日期范围内的打卡记录
     */
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND completedDate BETWEEN :startDate AND :endDate ORDER BY completedDate DESC")
    fun getLogsByHabitIdAndDateRange(habitId: String, startDate: Date, endDate: Date): Flow<List<HabitLogEntity>>
    
    /**
     * 获取指定日期的所有习惯打卡记录
     */
    @Query("SELECT * FROM habit_logs WHERE date(completedDate/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    fun getLogsByDate(date: Date): Flow<List<HabitLogEntity>>
    
    /**
     * 获取所有习惯在日期范围内的打卡记录总数
     */
    @Query("SELECT COUNT(*) FROM habit_logs WHERE completedDate BETWEEN :startDate AND :endDate")
    suspend fun getLogsCountInDateRange(startDate: Date, endDate: Date): Int
    
    /**
     * 检查指定习惯在指定日期是否已打卡
     */
    @Query("SELECT EXISTS(SELECT 1 FROM habit_logs WHERE habitId = :habitId AND date(completedDate/1000, 'unixepoch') = date(:date/1000, 'unixepoch'))")
    suspend fun isHabitCompletedOnDate(habitId: String, date: Date): Boolean
    
    /**
     * 获取指定习惯的最后一次打卡记录
     */
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY completedDate DESC LIMIT 1")
    suspend fun getLastLogForHabit(habitId: String): HabitLogEntity?
    
    /**
     * 获取指定习惯在特定日期的打卡记录
     */
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date(completedDate/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getLogForHabitOnDate(habitId: String, date: Date): HabitLogEntity?
    
    /**
     * 获取日期范围内情绪评分最高的打卡记录
     */
    @Query("SELECT * FROM habit_logs WHERE mood IS NOT NULL AND completedDate BETWEEN :startDate AND :endDate ORDER BY mood DESC LIMIT 1")
    suspend fun getBestMoodLogInDateRange(startDate: Date, endDate: Date): HabitLogEntity?
    
    /**
     * 插入打卡记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity)
    
    /**
     * 批量插入打卡记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<HabitLogEntity>)
    
    /**
     * 更新打卡记录
     */
    @Update
    suspend fun updateLog(log: HabitLogEntity)
    
    /**
     * 删除打卡记录
     */
    @Delete
    suspend fun deleteLog(log: HabitLogEntity)
    
    /**
     * 删除指定习惯的所有打卡记录
     */
    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteAllLogsForHabit(habitId: String)
    
    /**
     * 获取打卡记录的统计数据（心情平均值）
     */
    @Query("SELECT AVG(mood) FROM habit_logs WHERE habitId = :habitId AND mood IS NOT NULL")
    suspend fun getAverageMoodForHabit(habitId: String): Float?
    
    /**
     * 获取指定习惯的连续打卡天数
     */
    @Query("SELECT COUNT(*) FROM (SELECT DISTINCT date(completedDate/1000, 'unixepoch') as completion_date FROM habit_logs WHERE habitId = :habitId ORDER BY completion_date DESC)")
    suspend fun getCompletionDaysCount(habitId: String): Int
} 