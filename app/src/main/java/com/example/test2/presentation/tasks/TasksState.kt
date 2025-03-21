package com.example.test2.presentation.tasks

import com.example.test2.data.model.Goal
import com.example.test2.data.model.Task
import com.example.test2.data.model.TaskPriority
import com.example.test2.data.model.TaskStatus
import java.util.Date

/**
 * 任务管理状态类
 *
 * @property tasks 全部任务列表
 * @property filteredTasks 筛选后的任务列表
 * @property selectedDate 选中的日期
 * @property searchQuery 搜索查询
 * @property currentFilter 当前筛选条件
 * @property isLoading 是否正在加载
 * @property error 错误信息
 * @property showDialog 是否显示对话框
 * @property selectedTask 当前选中的任务
 * @property goals 可关联的目标列表
 */
data class TasksState(
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val selectedDate: Date = Date(),
    val searchQuery: String = "",
    val currentFilter: Filter = Filter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDialog: Boolean = false,
    val selectedTask: Task? = null,
    val goals: List<Goal> = emptyList(),
    // 任务拖拽相关状态
    val isDragging: Boolean = false
) {
    /**
     * 任务筛选器
     */
    enum class Filter {
        ALL,            // 所有任务
        TODAY,          // 今日任务
        UPCOMING,       // 即将到期（3天内）
        COMPLETED,      // 已完成
        HIGH_PRIORITY,  // 高优先级
        ASSOCIATED      // 关联目标的任务
    }
    
    /**
     * 任务分组
     */
    enum class GroupBy {
        NONE,       // 不分组
        PRIORITY,   // 按优先级分组
        STATUS,     // 按状态分组
        DATE,       // 按日期分组
        GOAL        // 按目标分组
    }
    
    /**
     * 计算当日任务数量
     */
    fun getTaskCountForDate(date: Date): Int {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        
        return tasks.count { task ->
            val taskCalendar = java.util.Calendar.getInstance()
            taskCalendar.time = task.dueDate
            
            taskCalendar.get(java.util.Calendar.YEAR) == year &&
            taskCalendar.get(java.util.Calendar.MONTH) == month &&
            taskCalendar.get(java.util.Calendar.DAY_OF_MONTH) == day
        }
    }
    
    /**
     * 获取任务日历热图数据
     */
    fun getTaskCountMap(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        
        tasks.forEach { task ->
            val dateString = dateFormat.format(task.dueDate)
            result[dateString] = (result[dateString] ?: 0) + 1
        }
        
        return result
    }
    
    companion object {
        /**
         * 创建初始状态
         */
        fun initial() = TasksState(isLoading = true)
    }
} 