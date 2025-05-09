package com.example.test2.presentation.timetracking

import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import java.util.Date

/**
 * 时间追踪事件
 * 封装了用户交互和系统事件
 */
sealed class TimeTrackingEvent {
    /**
     * 加载时间条目
     */
    object LoadTimeEntries : TimeTrackingEvent()
    
    /**
     * 加载任务
     */
    object LoadTasks : TimeTrackingEvent()
    
    /**
     * 选择日期
     */
    data class SelectDate(val date: Date) : TimeTrackingEvent()
    
    /**
     * 按分类筛选
     */
    data class FilterCategory(val category: TimeCategory?) : TimeTrackingEvent()
    
    /**
     * 设置日期范围
     */
    data class SetDateRange(val startDate: Date, val endDate: Date) : TimeTrackingEvent()
    
    /**
     * 开始时间条目
     */
    data class StartTimeEntry(val timeEntry: TimeEntry) : TimeTrackingEvent()
    
    /**
     * 停止时间条目
     */
    data class StopTimeEntry(val endTime: Date = Date()) : TimeTrackingEvent()
    
    /**
     * 添加时间条目
     */
    data class AddTimeEntry(val timeEntry: TimeEntry) : TimeTrackingEvent()
    
    /**
     * 更新时间条目
     */
    data class UpdateTimeEntry(val timeEntry: TimeEntry) : TimeTrackingEvent()
    
    /**
     * 删除时间条目
     */
    data class DeleteTimeEntry(val id: Long) : TimeTrackingEvent()
    
    /**
     * 选择时间条目
     */
    data class SelectTimeEntry(val timeEntry: TimeEntry) : TimeTrackingEvent()
    
    /**
     * 显示添加时间条目对话框
     */
    object ShowAddEntryDialog : TimeTrackingEvent()
    
    /**
     * 显示编辑时间条目对话框
     */
    data class ShowEditEntryDialog(val timeEntry: TimeEntry) : TimeTrackingEvent()
    
    /**
     * 显示筛选对话框
     */
    object ShowFilterDialog : TimeTrackingEvent()
    
    /**
     * 关闭对话框
     */
    object DismissDialog : TimeTrackingEvent()
    
    /**
     * 计算统计数据
     */
    data object CalculateStatistics : TimeTrackingEvent()
} 