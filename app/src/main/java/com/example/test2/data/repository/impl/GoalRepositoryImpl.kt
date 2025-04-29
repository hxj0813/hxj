package com.example.test2.data.repository.impl

import com.example.test2.data.local.dao.GoalDao
import com.example.test2.data.local.entity.GoalEntity
import com.example.test2.data.model.Goal
import com.example.test2.data.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

/**
 * 目标仓库实现类
 * 通过GoalDao实现目标数据的存储与检索
 */
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {

    // 查询操作
    
    override fun getAllGoals(): Flow<List<Goal>> {
        return goalDao.getAllGoals().map { entities ->
            entities.map { it.toGoal() }
        }
    }
    
    override suspend fun getGoalById(goalId: Long): Goal? {
        return goalDao.getGoalById(goalId)?.toGoal()
    }
    
    override fun getGoalsByCompletionStatus(isCompleted: Boolean): Flow<List<Goal>> {
        return goalDao.getGoalsByCompletionStatus(isCompleted).map { entities ->
            entities.map { it.toGoal() }
        }
    }
    
    override fun getImportantGoals(): Flow<List<Goal>> {
        return goalDao.getImportantGoals().map { entities ->
            entities.map { it.toGoal() }
        }
    }
    
    override fun getOverdueGoals(): Flow<List<Goal>> {
        return goalDao.getOverdueGoals().map { entities ->
            entities.map { it.toGoal() }
        }
    }
    
    override fun getUpcomingGoals(startDate: Date, endDate: Date): Flow<List<Goal>> {
        return goalDao.getUpcomingGoals(startDate, endDate).map { entities ->
            entities.map { it.toGoal() }
        }
    }
    
    override fun getGoalsByProgressRange(minProgress: Float, maxProgress: Float): Flow<List<Goal>> {
        // 将0-1的浮点数转换为0-100的整数
        val minProgressInt = (minProgress * 100).toInt()
        val maxProgressInt = (maxProgress * 100).toInt()
        
        return goalDao.getGoalsByProgressRange(minProgressInt, maxProgressInt).map { entities ->
            entities.map { it.toGoal() }
        }
    }
    
    // 插入操作
    
    override suspend fun saveGoal(goal: Goal): Long {
        return goalDao.insertGoal(GoalEntity.fromGoal(goal))
    }
    
    override suspend fun saveGoals(goals: List<Goal>): List<Long> {
        val entities = goals.map { GoalEntity.fromGoal(it) }
        return goalDao.insertGoals(entities)
    }
    
    // 更新操作
    
    override suspend fun updateGoal(goal: Goal): Boolean {
        val entity = GoalEntity.fromGoal(goal.copy(updatedAt = Date()))
        return goalDao.updateGoal(entity) > 0
    }
    
    override suspend fun updateGoalProgress(goalId: Long, progress: Float): Boolean {
        // 将0-1的浮点数转换为0-100的整数
        val progressInt = (progress * 100).toInt().coerceIn(0, 100)
        return goalDao.updateGoalProgress(goalId, progressInt) > 0
    }
    
    override suspend fun updateGoalCompletionStatus(goalId: Long, isCompleted: Boolean): Boolean {
        return goalDao.updateGoalCompletionStatus(goalId, isCompleted) > 0
    }
    
    override suspend fun updateGoalImportance(goalId: Long, isImportant: Boolean): Boolean {
        return goalDao.updateGoalImportance(goalId, isImportant) > 0
    }
    
    // 删除操作
    
    override suspend fun deleteGoal(goal: Goal): Boolean {
        val entity = GoalEntity.fromGoal(goal)
        return goalDao.deleteGoal(entity) > 0
    }
    
    override suspend fun deleteGoalById(goalId: Long): Boolean {
        return goalDao.deleteGoalById(goalId) > 0
    }
    
    override suspend fun deleteAllGoals(): Int {
        return goalDao.deleteAllGoals()
    }
    
    override suspend fun deleteCompletedGoals(): Int {
        return goalDao.deleteCompletedGoals()
    }
    
    // 关联查询（如果需要）
    
    /*
    override fun getTasksForGoal(goalId: Long): Flow<List<Task>> {
        return goalDao.getGoalWithTasks(goalId).map { goalWithTasks ->
            goalWithTasks?.tasks?.map { it.toTask() } ?: emptyList()
        }
    }
    */
} 