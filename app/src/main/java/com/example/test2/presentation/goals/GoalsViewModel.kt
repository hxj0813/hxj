package com.example.test2.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.model.Goal
import com.example.test2.presentation.goals.GoalsEvent
import com.example.test2.presentation.goals.GoalsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 目标管理ViewModel
 */
class GoalsViewModel : ViewModel() {

    // 状态
    private val _state = MutableStateFlow(GoalsState())
    val state: StateFlow<GoalsState> = _state.asStateFlow()

    // 模拟数据
    private val dummyGoals = listOf(
        Goal(
            id = 1,
            title = "学习Kotlin和Jetpack Compose",
            description = "掌握Kotlin编程语言和Jetpack Compose UI框架，能够熟练开发Android应用",
            isLongTerm = true,
            isImportant = true,
            progress = 0.6f,
            deadline = Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000)
        ),
        Goal(
            id = 2,
            title = "坚持每日运动",
            description = "每天保持30分钟以上的运动，提高身体素质和健康水平",
            isLongTerm = true,
            isImportant = true,
            progress = 0.3f,
            deadline = Date(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000)
        ),
        Goal(
            id = 3,
            title = "完成个人成长管理系统开发",
            description = "设计并实现一个完整的个人成长管理系统，包括目标管理、任务规划、习惯形成等功能",
            isLongTerm = false,
            isImportant = true,
            progress = 0.2f,
            deadline = Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000)
        ),
        Goal(
            id = 4,
            title = "阅读《原子习惯》",
            description = "阅读完成《原子习惯》这本书，并做好读书笔记",
            isLongTerm = false,
            isImportant = false,
            progress = 0.8f,
            deadline = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000),
            isCompleted = false
        ),
        Goal(
            id = 5,
            title = "学习设计模式",
            description = "掌握常用的软件设计模式，并能在实际项目中应用",
            isLongTerm = true,
            isImportant = false,
            progress = 1.0f,
            deadline = Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000),
            isCompleted = true
        )
    )

    init {
        loadGoals()
    }

    /**
     * 处理UI事件
     */
    fun onEvent(event: GoalsEvent) {
        when (event) {
            is GoalsEvent.LoadGoals -> loadGoals()
            is GoalsEvent.FilterGoals -> filterGoals(event.filter)
            is GoalsEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is GoalsEvent.AddGoal -> addGoal(event.goal)
            is GoalsEvent.UpdateGoal -> updateGoal(event.goal)
            is GoalsEvent.DeleteGoal -> deleteGoal(event.goalId)
            is GoalsEvent.CompleteGoal -> completeGoal(event.goalId, event.isCompleted)
            is GoalsEvent.ShowAddGoalDialog -> showAddGoalDialog()
            is GoalsEvent.ShowEditGoalDialog -> showEditGoalDialog(event.goal)
            is GoalsEvent.DismissDialog -> dismissDialog()
        }
    }

    /**
     * 加载目标列表
     */
    private fun loadGoals() {
        viewModelScope.launch {
            // 显示加载状态
            _state.update { it.copy(isLoading = true) }
            
            // 模拟网络延迟
           // kotlinx.coroutines.delay(500)
            
            // 更新状态
            _state.update { currentState ->
                currentState.copy(
                    goals = dummyGoals,
                    filteredGoals = applyFilters(dummyGoals, currentState.currentFilter),
                    isLoading = false
                )
            }
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
        val query = _state.value.searchQuery.lowercase()
        
        // 首先应用搜索查询
        val searchFiltered = if (query.isBlank()) {
            goals
        } else {
            goals.filter { goal ->
                goal.title.lowercase().contains(query) || 
                goal.description.lowercase().contains(query)
            }
        }
        
        // 然后应用类别过滤
        return when (filter) {
            GoalsState.Filter.ALL -> searchFiltered
            GoalsState.Filter.IMPORTANT -> searchFiltered.filter { it.isImportant }
            GoalsState.Filter.LONG_TERM -> searchFiltered.filter { it.isLongTerm }
            GoalsState.Filter.SHORT_TERM -> searchFiltered.filter { !it.isLongTerm }
            GoalsState.Filter.COMPLETED -> searchFiltered.filter { it.isCompleted }
            GoalsState.Filter.OVERDUE -> searchFiltered.filter { !it.isCompleted && it.isOverdue() }
            GoalsState.Filter.UPCOMING -> searchFiltered.filter { !it.isCompleted && it.isUpcoming() }
        }
    }

    /**
     * 更新搜索查询
     */
    private fun updateSearchQuery(query: String) {
        _state.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredGoals = applyFilters(currentState.goals, currentState.currentFilter)
            )
        }
    }

    /**
     * 添加新目标
     */
    private fun addGoal(goal: Goal) {
        viewModelScope.launch {
            // 模拟网络保存
            kotlinx.coroutines.delay(300)
            
            // 创建新目标（模拟自增ID）
            val newGoal = goal.copy(
                id = (_state.value.goals.maxOfOrNull { it.id } ?: 0) + 1,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            // 更新状态
            val updatedGoals = _state.value.goals + newGoal
            
            _state.update { currentState ->
                currentState.copy(
                    goals = updatedGoals,
                    filteredGoals = applyFilters(updatedGoals, currentState.currentFilter),
                    showDialog = false,
                    selectedGoal = null
                )
            }
        }
    }

    /**
     * 更新目标
     */
    private fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            // 模拟网络保存
            kotlinx.coroutines.delay(300)
            
            // 更新目标
            val updatedGoals = _state.value.goals.map {
                if (it.id == goal.id) goal.copy(updatedAt = Date()) else it
            }
            
            // 更新状态
            _state.update { currentState ->
                currentState.copy(
                    goals = updatedGoals,
                    filteredGoals = applyFilters(updatedGoals, currentState.currentFilter),
                    showDialog = false,
                    selectedGoal = null
                )
            }
        }
    }

    /**
     * 删除目标
     */
    private fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            // 模拟网络删除
            kotlinx.coroutines.delay(300)
            
            // 删除目标
            val updatedGoals = _state.value.goals.filter { it.id != goalId }
            
            // 更新状态
            _state.update { currentState ->
                currentState.copy(
                    goals = updatedGoals,
                    filteredGoals = applyFilters(updatedGoals, currentState.currentFilter)
                )
            }
        }
    }

    /**
     * 完成/取消完成目标
     */
    private fun completeGoal(goalId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            // 模拟网络保存
            kotlinx.coroutines.delay(100)
            
            // 更新目标完成状态
            val updatedGoals = _state.value.goals.map {
                if (it.id == goalId) {
                    it.copy(
                        isCompleted = isCompleted,
                        progress = if (isCompleted) 1f else it.progress,
                        updatedAt = Date()
                    )
                } else {
                    it
                }
            }
            
            // 更新状态
            _state.update { currentState ->
                currentState.copy(
                    goals = updatedGoals,
                    filteredGoals = applyFilters(updatedGoals, currentState.currentFilter)
                )
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