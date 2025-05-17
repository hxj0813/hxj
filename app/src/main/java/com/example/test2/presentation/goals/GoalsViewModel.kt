package com.example.test2.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.model.Goal
import com.example.test2.domain.usecase.GoalUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import android.util.Log

/**
 * 目标管理ViewModel
 */
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalUseCases: GoalUseCases
) : ViewModel() {

    // 状态
    private val _state = MutableStateFlow(GoalsState())
    val state: StateFlow<GoalsState> = _state.asStateFlow()
    
    // 进度更新Job
    private var progressUpdateJob: Job? = null

    init {
        loadGoals()
        // 启动定期进度更新
        startProgressUpdateTimer()
    }
    
    override fun onCleared() {
        super.onCleared()
        // 取消进度更新Job
        progressUpdateJob?.cancel()
    }

    /**
     * 处理UI事件
     */
    fun onEvent(event: GoalsEvent) {
        when (event) {
            is GoalsEvent.LoadGoals -> loadGoals()
            is GoalsEvent.FilterGoals -> filterGoals(event.filter)
            is GoalsEvent.AddGoal -> addGoal(event.goal)
            is GoalsEvent.UpdateGoal -> updateGoal(event.goal)
            is GoalsEvent.DeleteGoal -> deleteGoal(event.goalId)
            is GoalsEvent.CompleteGoal -> completeGoal(event.goalId, event.isCompleted)
            is GoalsEvent.ShowAddGoalDialog -> showAddGoalDialog()
            is GoalsEvent.ShowEditGoalDialog -> showEditGoalDialog(event.goal)
            is GoalsEvent.DismissDialog -> dismissDialog()
            is GoalsEvent.RefreshProgress -> updateTimeBasedGoalProgress()
        }
    }

    /**
     * 启动定期进度更新定时器
     * 每小时更新一次没有关联任务的目标进度
     */
    private fun startProgressUpdateTimer() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                updateTimeBasedGoalProgress()
                // 每小时更新一次
                delay(60 * 60 * 1000L)
            }
        }
    }

    /**
     * 更新没有关联任务的目标的基于时间的进度
     */
    private fun updateTimeBasedGoalProgress() {
        viewModelScope.launch {
            Log.d("GoalsViewModel", "更新基于时间的目标进度")
            
            // 获取所有非完成且没有关联任务的目标
            val uncompletedGoals = _state.value.goals.filter { 
                !it.isCompleted && !it.hasLinkedTask
            }
            
            // 逐个更新目标的进度
            uncompletedGoals.forEach { goal ->
                // 计算基于时间的进度
                val timeBasedProgress = goal.calculateTimeBasedProgress()
                
                // 如果计算的进度与当前进度不同，则更新
                if (timeBasedProgress != goal.progress) {
                    Log.d("GoalsViewModel", "目标 ${goal.id} 基于时间的进度: $timeBasedProgress")
                    
                    // 判断是否需要标记为已完成
                    val needToComplete = timeBasedProgress >= 1.0f
                    
                    // 如果进度达到100%，则同时标记为已完成
                    if (needToComplete) {
                        goalUseCases.toggleGoalCompletion(goal.id, true)
                        Log.d("GoalsViewModel", "目标 ${goal.id} 已自动标记为完成")
                    } else {
                        // 否则只更新进度
                        goalUseCases.updateGoalProgress(goal.id, timeBasedProgress)
                    }
                }
            }
            
            // 由于进度已更新，需要重新加载目标列表
            if (uncompletedGoals.isNotEmpty()) {
                loadGoals()
            }
        }
    }

    /**
     * 加载目标列表
     */
    private fun loadGoals() {
        viewModelScope.launch {
            // 显示加载状态
            _state.update { it.copy(isLoading = true) }
            
            // 从数据库加载数据
            goalUseCases.getAllGoals().onEach { goals ->
                _state.update { currentState ->
                    currentState.copy(
                        goals = goals,
                        filteredGoals = applyFilters(goals, currentState.currentFilter),
                        isLoading = false
                    )
                }
                
                // 每次加载数据后更新无任务目标的进度
                updateTimeBasedGoalProgress()
            }.launchIn(viewModelScope)
        }
    }

    /**
     * 过滤目标
     */
    private fun filterGoals(filter: GoalsState.Filter) {
        _state.update { currentState ->
            currentState.copy(
                currentFilter = filter,
                filteredGoals = applyFilters(currentState.goals, filter)
            )
        }
    }

    /**
     * 应用过滤器
     */
    private fun applyFilters(goals: List<Goal>, filter: GoalsState.Filter): List<Goal> {
        // 应用类别过滤
        return when (filter) {
            GoalsState.Filter.ALL -> goals
            GoalsState.Filter.IMPORTANT -> goals.filter { it.isImportant }
            GoalsState.Filter.LONG_TERM -> goals.filter { it.isLongTerm }
            GoalsState.Filter.SHORT_TERM -> goals.filter { !it.isLongTerm }
            GoalsState.Filter.COMPLETED -> goals.filter { it.isCompleted }
            GoalsState.Filter.OVERDUE -> goals.filter { !it.isCompleted && it.isOverdue() }
            GoalsState.Filter.UPCOMING -> goals.filter { !it.isCompleted && it.isUpcoming() }
        }
    }

    /**
     * 添加新目标
     */
    private fun addGoal(goal: Goal) {
        viewModelScope.launch {
            // 保存到数据库
            val goalId = goalUseCases.saveGoal(goal)
            
            if (goalId > 0) {
                // 保存成功，重新加载数据
                loadGoals()
                
                // 更新状态 - 关闭对话框
                _state.update { currentState ->
                    currentState.copy(
                        showDialog = false,
                        selectedGoal = null
                    )
                }
            }
        }
    }

    /**
     * 更新目标
     */
    private fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            // 更新数据库
            val success = goalUseCases.updateGoal(goal)
            
            if (success) {
                // 更新成功，重新加载数据
                loadGoals()
                
                // 更新状态 - 关闭对话框
                _state.update { currentState ->
                    currentState.copy(
                        showDialog = false,
                        selectedGoal = null
                    )
                }
            }
        }
    }

    /**
     * 删除目标
     */
    private fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            // 从数据库删除
            val success = goalUseCases.deleteGoal(goalId)
            
            if (success) {
                // 删除成功，重新加载数据
                loadGoals()
            }
        }
    }

    /**
     * 完成/取消完成目标
     */
    private fun completeGoal(goalId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            // 更新数据库中的完成状态
            val success = goalUseCases.toggleGoalCompletion(goalId, isCompleted)
            
            if (success) {
                // 更新成功，重新加载数据
                loadGoals()
            }
        }
    }
    
    /**
     * 显示添加目标对话框
     */
    private fun showAddGoalDialog() {
        _state.update { it.copy(showDialog = true, selectedGoal = null) }
    }
    
    /**
     * 显示编辑目标对话框
     */
    private fun showEditGoalDialog(goal: Goal) {
        _state.update { it.copy(showDialog = true, selectedGoal = goal) }
    }
    
    /**
     * 关闭对话框
     */
    private fun dismissDialog() {
        _state.update { it.copy(showDialog = false, selectedGoal = null) }
    }
} 