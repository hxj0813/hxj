package com.example.test2.data.repository

import com.example.test2.data.model.Goal
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 目标仓库接口
 * 定义了目标数据管理相关的所有操作
 */
interface GoalRepository {
    // 查询操作
    
    /**
     * 获取所有目标
     * 按创建时间降序排列（最新创建的在前）
     */
    fun getAllGoals(): Flow<List<Goal>>
    
    /**
     * 根据ID获取目标
     */
    suspend fun getGoalById(goalId: Long): Goal?
    
    /**
     * 根据完成状态获取目标
     */
    fun getGoalsByCompletionStatus(isCompleted: Boolean): Flow<List<Goal>>
    
    /**
     * 获取重要目标
     */
    fun getImportantGoals(): Flow<List<Goal>>
    
    /**
     * 获取已逾期目标（未完成且截止日期已过）
     */
    fun getOverdueGoals(): Flow<List<Goal>>
    
    /**
     * 获取即将到期的目标（指定日期范围内）
     */
    fun getUpcomingGoals(startDate: Date, endDate: Date): Flow<List<Goal>>
    
    /**
     * 按进度范围查询目标
     */
    fun getGoalsByProgressRange(minProgress: Float, maxProgress: Float): Flow<List<Goal>>
    
    // 插入操作
    
    /**
     * 插入/保存目标
     * @return 插入的目标ID
     */
    suspend fun saveGoal(goal: Goal): Long
    
    /**
     * 插入多个目标
     * @return 插入的目标ID列表
     */
    suspend fun saveGoals(goals: List<Goal>): List<Long>
    
    // 更新操作
    
    /**
     * 更新目标
     * @return 是否更新成功
     */
    suspend fun updateGoal(goal: Goal): Boolean
    
    /**
     * 更新目标进度
     * @return 是否更新成功
     */
    suspend fun updateGoalProgress(goalId: Long, progress: Float): Boolean
    
    /**
     * 更新目标完成状态
     * @return 是否更新成功
     */
    suspend fun updateGoalCompletionStatus(goalId: Long, isCompleted: Boolean): Boolean
    
    /**
     * 更新目标重要性
     * @return 是否更新成功
     */
    suspend fun updateGoalImportance(goalId: Long, isImportant: Boolean): Boolean
    
    // 删除操作
    
    /**
     * 删除目标
     * @return 是否删除成功
     */
    suspend fun deleteGoal(goal: Goal): Boolean
    
    /**
     * 根据ID删除目标
     * @return 是否删除成功
     */
    suspend fun deleteGoalById(goalId: Long): Boolean
    
    /**
     * 删除所有目标
     * @return 删除的行数
     */
    suspend fun deleteAllGoals(): Int
    
    /**
     * 删除已完成的目标
     * @return 删除的行数
     */
    suspend fun deleteCompletedGoals(): Int
    
    // 关联查询（如果需要）
    
    /**
     * 获取目标关联的任务（如果有关联关系）
     */
    //fun getTasksForGoal(goalId: Long): Flow<List<Task>>
} 