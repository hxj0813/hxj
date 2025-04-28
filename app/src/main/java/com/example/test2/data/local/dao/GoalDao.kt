package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.test2.data.local.entity.GoalEntity
//import com.example.test2.data.local.relation.GoalWithTasks
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 目标数据访问对象接口
 * 定义了对目标表(goals)的所有数据库操作
 */
@Dao
interface GoalDao {
    // 查询操作
    
    /**
     * 获取所有目标
     * 按创建时间降序排列（最新创建的在前）
     */
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>
    
    /**
     * 根据ID获取目标
     */
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): GoalEntity?
    
    /**
     * 根据完成状态获取目标
     */
    @Query("SELECT * FROM goals WHERE isCompleted = :isCompleted ORDER BY deadline ASC")
    fun getGoalsByCompletionStatus(isCompleted: Boolean): Flow<List<GoalEntity>>
    
    /**
     * 获取重要目标
     */
    @Query("SELECT * FROM goals WHERE isImportant = 1 ORDER BY deadline ASC")
    fun getImportantGoals(): Flow<List<GoalEntity>>
    
    /**
     * 获取已逾期目标（未完成且截止日期已过）
     */
    @Query("SELECT * FROM goals WHERE deadline < :currentDate AND isCompleted = 0")
    fun getOverdueGoals(currentDate: Date = Date()): Flow<List<GoalEntity>>
    
    /**
     * 获取即将到期的目标（指定日期范围内）
     */
    @Query("SELECT * FROM goals WHERE deadline BETWEEN :startDate AND :endDate AND isCompleted = 0")
    fun getUpcomingGoals(startDate: Date, endDate: Date): Flow<List<GoalEntity>>
    
    /**
     * 按进度范围查询目标
     */
    @Query("SELECT * FROM goals WHERE progress BETWEEN :minProgress AND :maxProgress")
    fun getGoalsByProgressRange(minProgress: Int, maxProgress: Int): Flow<List<GoalEntity>>
    
    // 插入操作
    
    /**
     * 插入单个目标
     * @return 插入的目标ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long
    
    /**
     * 插入多个目标
     * @return 插入的目标ID列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>): List<Long>
    
    // 更新操作
    
    /**
     * 更新目标
     * @return 更新的行数
     */
    @Update
    suspend fun updateGoal(goal: GoalEntity): Int
    
    /**
     * 更新目标进度
     * @return 更新的行数
     */
    @Query("UPDATE goals SET progress = :progress, updatedAt = :updatedAt WHERE id = :goalId")
    suspend fun updateGoalProgress(goalId: Long, progress: Int, updatedAt: Date = Date()): Int
    
    /**
     * 更新目标完成状态
     * @return 更新的行数
     */
    @Query("UPDATE goals SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :goalId")
    suspend fun updateGoalCompletionStatus(goalId: Long, isCompleted: Boolean, updatedAt: Date = Date()): Int
    
    /**
     * 更新目标重要性
     * @return 更新的行数
     */
    @Query("UPDATE goals SET isImportant = :isImportant, updatedAt = :updatedAt WHERE id = :goalId")
    suspend fun updateGoalImportance(goalId: Long, isImportant: Boolean, updatedAt: Date = Date()): Int
    
    // 删除操作
    
    /**
     * 删除目标
     * @return 删除的行数
     */
    @Delete
    suspend fun deleteGoal(goal: GoalEntity): Int
    
    /**
     * 根据ID删除目标
     * @return 删除的行数
     */
    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long): Int
    
    /**
     * 删除所有目标
     * @return 删除的行数
     */
    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals(): Int
    
    /**
     * 删除已完成的目标
     * @return 删除的行数
     */
    @Query("DELETE FROM goals WHERE isCompleted = 1")
    suspend fun deleteCompletedGoals(): Int
    
    // 关联查询（如果需要）
    
    /**
     * 获取目标及其关联任务
     */
//    @Transaction
//    @Query("SELECT * FROM goals WHERE id = :goalId")
//    fun getGoalWithTasks(goalId: Long): Flow<GoalWithTasks?>
    
    /**
     * 获取所有目标及其关联任务
     */
//    @Transaction
//    @Query("SELECT * FROM goals")
//    fun getAllGoalsWithTasks(): Flow<List<GoalWithTasks>>

} 