package com.example.test2.presentation.goals

import com.example.test2.data.model.Goal

/**
 * 目标管理状态类
 */
data class GoalsState(
    val goals: List<Goal> = emptyList(),
    val filteredGoals: List<Goal> = emptyList(),
    val searchQuery: String = "",
    val currentFilter: Filter = Filter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDialog: Boolean = false,
    val selectedGoal: Goal? = null
) {
    /**
     * 目标过滤器
     */
    enum class Filter {
        ALL,            // 所有
        IMPORTANT,      // 重要的
        LONG_TERM,      // 长期
        SHORT_TERM,     // 短期
        COMPLETED,      // 已完成
        UPCOMING,       // 即将到期
        OVERDUE         // 已逾期
    }
    
    companion object {
        /**
         * 创建初始状态
         */
        fun initial() = GoalsState(isLoading = true)
    }
} 