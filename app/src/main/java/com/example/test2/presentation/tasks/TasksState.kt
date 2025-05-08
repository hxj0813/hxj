package com.example.test2.presentation.tasks

import com.example.test2.data.model.Goal
import com.example.test2.data.model.Habit
import com.example.test2.data.model.Task
import java.util.Date

/**
 * 任务UI状态类
 * 保存任务列表界面的所有状态信息
 */
data class TasksUiState(
    // 任务数据
    val activeTasks: List<Task> = emptyList(),     // 活跃的任务列表
    val completedTasks: List<Task> = emptyList(),  // 已完成的任务列表
    
    // 统计数据
    val todayTasksCount: Int = 0,                  // 今日任务数量
    val overdueTasksCount: Int = 0,                // 逾期任务数量
    
    // UI状态
    val isLoading: Boolean = false,                // 是否正在加载
    val error: String? = null,                     // 错误信息
    val filterType: FilterType = FilterType.ALL,   // 过滤类型
    val searchQuery: String = "",                  // 搜索关键字
    val showCompletedTasks: Boolean = false,       // 是否显示已完成任务
    val showDialog: Boolean = false,               // 是否显示任务对话框
    
    // 关联数据
    val goals: List<Goal> = emptyList(),           // 目标列表
    val habits: List<Habit> = emptyList()          // 习惯列表
) {
    /**
     * 任务过滤类型
     */
    enum class FilterType {
        ALL,        // 全部任务
        TODAY,      // 今日任务
        POMODORO,   // 番茄钟任务
        CHECKIN     // 打卡任务
    }
} 