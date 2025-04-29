package com.example.test2.domain.usecase

import com.example.test2.data.model.Goal
import com.example.test2.data.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * 目标相关的用例集合
 * 封装所有与目标相关的业务逻辑
 */
class GoalUseCases @Inject constructor(
    private val repository: GoalRepository
) {
    /**
     * 获取所有目标
     */
    fun getAllGoals(): Flow<List<Goal>> {
        return repository.getAllGoals()
    }
    
    /**
     * 获取未完成的目标
     */
    fun getActiveGoals(): Flow<List<Goal>> {
        return repository.getGoalsByCompletionStatus(false)
    }
    
    /**
     * 获取已完成的目标
     */
    fun getCompletedGoals(): Flow<List<Goal>> {
        return repository.getGoalsByCompletionStatus(true)
    }
    
    /**
     * 获取重要目标
     */
    fun getImportantGoals(): Flow<List<Goal>> {
        return repository.getImportantGoals()
    }
    
    /**
     * 获取已逾期目标
     */
    fun getOverdueGoals(): Flow<List<Goal>> {
        return repository.getOverdueGoals()
    }
    
    /**
     * 获取未来一周内到期的目标
     */
    fun getUpcomingWeekGoals(): Flow<List<Goal>> {
        val today = Date()
        val nextWeek = Calendar.getInstance().apply {
            time = today
            add(Calendar.DAY_OF_YEAR, 7)
        }.time
        
        return repository.getUpcomingGoals(today, nextWeek)
    }
    
    /**
     * 获取未来一个月内到期的目标
     */
    fun getUpcomingMonthGoals(): Flow<List<Goal>> {
        val today = Date()
        val nextMonth = Calendar.getInstance().apply {
            time = today
            add(Calendar.MONTH, 1)
        }.time
        
        return repository.getUpcomingGoals(today, nextMonth)
    }
    
    /**
     * 根据ID获取目标
     */
    suspend fun getGoalById(goalId: Long): Goal? {
        return repository.getGoalById(goalId)
    }
    
    /**
     * 保存目标
     * @return 保存的目标ID，如果保存失败返回-1
     */
    suspend fun saveGoal(goal: Goal): Long {
        return repository.saveGoal(goal)
    }
    
    /**
     * 批量保存多个目标
     * @return 保存的目标ID列表
     */
    suspend fun saveGoals(goals: List<Goal>): List<Long> {
        return repository.saveGoals(goals)
    }
    
    /**
     * 更新目标
     * @return 是否更新成功
     */
    suspend fun updateGoal(goal: Goal): Boolean {
        return repository.updateGoal(goal)
    }
    
    /**
     * 更新目标进度
     * @return 是否更新成功
     */
    suspend fun updateGoalProgress(goalId: Long, progress: Float): Boolean {
        val clampedProgress = progress.coerceIn(0f, 1f)
        return repository.updateGoalProgress(goalId, clampedProgress)
    }
    
    /**
     * 切换目标完成状态
     * @return 是否更新成功
     */
    suspend fun toggleGoalCompletion(goalId: Long, isCompleted: Boolean): Boolean {
        return repository.updateGoalCompletionStatus(goalId, isCompleted)
    }
    
    /**
     * 切换目标重要性
     * @return 是否更新成功
     */
    suspend fun toggleGoalImportance(goalId: Long, isImportant: Boolean): Boolean {
        return repository.updateGoalImportance(goalId, isImportant)
    }
    
    /**
     * 删除目标
     * @return 是否删除成功
     */
    suspend fun deleteGoal(goalId: Long): Boolean {
        return repository.deleteGoalById(goalId)
    }
    
    /**
     * 清理已完成的目标
     * @return 删除的行数
     */
    suspend fun cleanCompletedGoals(): Int {
        return repository.deleteCompletedGoals()
    }
} 