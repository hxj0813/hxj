package com.example.test2.presentation.goals

import com.example.test2.data.model.Goal

/**
 * 目标管理事件
 */
sealed class GoalsEvent {
    /**
     * 加载目标列表
     */
    object LoadGoals : GoalsEvent()
    
    /**
     * 过滤目标
     */
    data class FilterGoals(val filter: GoalsState.Filter) : GoalsEvent()
    
    /**
     * 添加目标
     */
    data class AddGoal(val goal: Goal) : GoalsEvent()
    
    /**
     * 更新目标
     */
    data class UpdateGoal(val goal: Goal) : GoalsEvent()
    
    /**
     * 删除目标
     */
    data class DeleteGoal(val goalId: Long) : GoalsEvent()
    
    /**
     * 标记目标为完成/未完成
     */
    data class CompleteGoal(val goalId: Long, val isCompleted: Boolean) : GoalsEvent()
    
    /**
     * 显示添加目标对话框
     */
    object ShowAddGoalDialog : GoalsEvent()
    
    /**
     * 显示编辑目标对话框
     */
    data class ShowEditGoalDialog(val goal: Goal) : GoalsEvent()
    
    /**
     * 关闭对话框
     */
    object DismissDialog : GoalsEvent()
} 