package com.example.test2.presentation.tasks

import com.example.test2.data.model.Task
import java.util.Date

/**
 * 任务管理事件
 */
sealed class TasksEvent {
    /**
     * 加载任务列表
     */
    object LoadTasks : TasksEvent()
    
    /**
     * 按日期筛选任务
     */
    data class SelectDate(val date: Date) : TasksEvent()
    
    /**
     * 过滤任务
     */
    data class FilterTasks(val filter: TasksState.Filter) : TasksEvent()
    
    /**
     * 更新搜索查询
     */
    data class UpdateSearchQuery(val query: String) : TasksEvent()
    
    /**
     * 添加任务
     */
    data class AddTask(val task: Task) : TasksEvent()
    
    /**
     * 更新任务
     */
    data class UpdateTask(val task: Task) : TasksEvent()
    
    /**
     * 删除任务
     */
    data class DeleteTask(val taskId: Long) : TasksEvent()
    
    /**
     * 标记任务完成状态
     */
    data class CompleteTask(val taskId: Long, val isCompleted: Boolean) : TasksEvent()
    
    /**
     * 显示添加任务对话框
     */
    object ShowAddTaskDialog : TasksEvent()
    
    /**
     * 显示编辑任务对话框
     */
    data class ShowEditTaskDialog(val task: Task) : TasksEvent()
    
    /**
     * 关闭对话框
     */
    object DismissDialog : TasksEvent()
    
    /**
     * 拖拽状态变化
     */
    data class DragStateChanged(val isDragging: Boolean) : TasksEvent()
    
    /**
     * 加载目标列表（用于任务关联）
     */
    object LoadGoals : TasksEvent()
} 