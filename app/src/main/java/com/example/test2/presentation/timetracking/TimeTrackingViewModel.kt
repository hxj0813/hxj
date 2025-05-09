package com.example.test2.presentation.timetracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.local.entity.timetracking.TimeGoalEntity
import com.example.test2.data.local.entity.timetracking.TimeTagEntity
import com.example.test2.data.model.Task
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import com.example.test2.data.repository.TimeTrackingRepository
import com.example.test2.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import java.util.UUID

// 添加 TimeStatistics 导入
import com.example.test2.presentation.timetracking.TimeStatistics
import com.example.test2.data.model.TaskType
import com.example.test2.data.model.TaskPriority

/**
 * 时间追踪ViewModel
 * 管理时间追踪界面的状态和业务逻辑
 */
@HiltViewModel
class TimeTrackingViewModel @Inject constructor(
    private val repository: TimeTrackingRepository
) : ViewModel() {
    
    // UI状态
    private val _state = MutableStateFlow(TimeTrackingState())
    val state: StateFlow<TimeTrackingState> = _state
    
    // 记录当前的Job，用于取消
    private var currentJob: Job? = null
    
    // 计时器Job
    private var timerJob: Job? = null
    
    init {
        loadTimeEntries()
        loadAllTags()
        loadActiveGoals()
        loadStatistics()
    }
    
    /**
     * 处理时间追踪相关事件
     * @param event 时间追踪事件
     */
    fun onEvent(event: TimeTrackingEvent) {
        when (event) {
            is TimeTrackingEvent.LoadTimeEntries -> loadTimeEntries()
            is TimeTrackingEvent.LoadTasks -> loadTasks()
            is TimeTrackingEvent.SelectDate -> selectDate(event.date)
            is TimeTrackingEvent.FilterCategory -> filterCategory(event.category)
            is TimeTrackingEvent.SetDateRange -> setDateRange(event.startDate, event.endDate)
            is TimeTrackingEvent.StartTimeEntry -> startTimeEntry(event.timeEntry)
            is TimeTrackingEvent.StopTimeEntry -> stopTimeEntry(event.endTime)
            is TimeTrackingEvent.AddTimeEntry -> addTimeEntry(event.timeEntry)
            is TimeTrackingEvent.UpdateTimeEntry -> updateTimeEntry(event.timeEntry)
            is TimeTrackingEvent.DeleteTimeEntry -> deleteTimeEntry(event.id)
            is TimeTrackingEvent.SelectTimeEntry -> selectTimeEntry(event.timeEntry)
            is TimeTrackingEvent.ShowAddEntryDialog -> showAddEntryDialog()
            is TimeTrackingEvent.ShowEditEntryDialog -> showEditEntryDialog(event.timeEntry)
            is TimeTrackingEvent.ShowFilterDialog -> showFilterDialog()
            is TimeTrackingEvent.DismissDialog -> dismissDialog()
            is TimeTrackingEvent.CalculateStatistics -> calculateStatistics()
        }
    }
    
    /**
     * 加载时间条目
     */
    private fun loadTimeEntries() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            repository.getTodayTimeEntries().collect { entries ->
                _state.update {
                    val filteredEntries = filterEntries(entries)
                    val ongoingEntry = entries.find { it.isOngoing() }
                    
                    it.copy(
                        timeEntries = entries,
                        filteredEntries = filteredEntries,
                        ongoingEntry = ongoingEntry,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * 加载任务数据
     */
    private fun loadTasks() {
        // 实际项目中，从任务仓库加载任务数据
        // 此处简化实现
    }
    
    /**
     * 选择日期
     * @param date 所选日期
     */
    private fun selectDate(date: Date) {
        _state.update { it.copy(selectedDate = date) }
        
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 计算所选日期的起止时间
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time
            
            // 加载指定日期的时间条目
            repository.getTimeEntriesBetween(startOfDay, endOfDay).collect { entries ->
                _state.update {
                    val filteredEntries = filterEntries(entries)
                    it.copy(
                        timeEntries = entries,
                        filteredEntries = filteredEntries,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * 按分类筛选
     * @param category 分类
     */
    private fun filterCategory(category: TimeCategory?) {
        _state.update { 
            it.copy(
                selectedCategory = category,
                filteredEntries = filterEntries(it.timeEntries)
            )
        }
    }
    
    /**
     * 设置日期范围
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    private fun setDateRange(startDate: Date, endDate: Date) {
        _state.update { it.copy(dateRange = Pair(startDate, endDate)) }
        
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 加载指定日期范围的时间条目
            repository.getTimeEntriesBetween(startDate, endDate).collect { entries ->
                _state.update {
                    val filteredEntries = filterEntries(entries)
                    it.copy(
                        timeEntries = entries,
                        filteredEntries = filteredEntries,
                        isLoading = false
                    )
                }
            }
            
            // 加载统计数据
            loadStatisticsForRange(startDate, endDate)
        }
    }
    
    /**
     * 开始新的时间条目
     * @param timeEntry 时间条目
     */
    private fun startTimeEntry(timeEntry: TimeEntry) {
        viewModelScope.launch {
            // 先停止所有正在进行的时间条目
            repository.stopOngoingTimeEntries()
            
            // 保存新的时间条目
            repository.saveTimeEntry(timeEntry)
            
            // 更新状态
            _state.update { it.copy(ongoingEntry = timeEntry) }
        }
    }
    
    /**
     * 停止正在进行的时间条目
     * @param endTime 结束时间
     */
    private fun stopTimeEntry(endTime: Date) {
        val ongoingEntry = _state.value.ongoingEntry ?: return
        
        viewModelScope.launch {
            // 计算时间条目的持续时间
            val completedEntry = ongoingEntry.complete(endTime)
            
            // 更新时间条目
            repository.updateTimeEntry(completedEntry)
            
            // 更新状态
            _state.update { it.copy(ongoingEntry = null) }
            
            // 更新关联的时间目标进度
            updateGoalsProgress(completedEntry)
        }
    }
    
    /**
     * 添加新的时间条目
     * @param timeEntry 时间条目
     */
    private fun addTimeEntry(timeEntry: TimeEntry) {
        viewModelScope.launch {
            repository.saveTimeEntry(timeEntry)
            _state.update { it.copy(showEntryDialog = false) }
            
            // 更新关联的时间目标进度
            updateGoalsProgress(timeEntry)
        }
    }
    
    /**
     * 更新时间条目
     * @param timeEntry 时间条目
     */
    private fun updateTimeEntry(timeEntry: TimeEntry) {
        viewModelScope.launch {
            repository.updateTimeEntry(timeEntry)
            _state.update { 
                it.copy(
                    showEntryDialog = false,
                    selectedEntry = null
                )
            }
        }
    }
    
    /**
     * 删除时间条目
     * @param id 时间条目ID
     */
    private fun deleteTimeEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteTimeEntry(id)
            _state.update { 
                it.copy(
                    selectedEntry = null,
                    filteredEntries = it.filteredEntries.filter { entry -> entry.id != id }
                )
            }
        }
    }
    
    /**
     * 选择时间条目
     * @param timeEntry 时间条目
     */
    private fun selectTimeEntry(timeEntry: TimeEntry) {
        _state.update { it.copy(selectedEntry = timeEntry) }
    }
    
    /**
     * 显示添加时间条目对话框
     */
    private fun showAddEntryDialog() {
        _state.update { 
            it.copy(
                showEntryDialog = true,
                selectedEntry = null
            )
        }
    }
    
    /**
     * 显示编辑时间条目对话框
     * @param timeEntry 时间条目
     */
    private fun showEditEntryDialog(timeEntry: TimeEntry) {
        _state.update { 
            it.copy(
                showEntryDialog = true,
                selectedEntry = timeEntry
            )
        }
    }
    
    /**
     * 显示筛选对话框
     */
    private fun showFilterDialog() {
        _state.update { it.copy(showFilterDialog = true) }
    }
    
    /**
     * 关闭对话框
     */
    private fun dismissDialog() {
        _state.update { 
            it.copy(
                showEntryDialog = false,
                showFilterDialog = false
            )
        }
    }
    
    /**
     * 加载所有标签
     */
    private fun loadAllTags() {
        viewModelScope.launch {
            repository.getAllTags().collect { tags ->
                _state.update { it.copy(allTags = tags) }
            }
        }
    }
    
    /**
     * 加载活跃的时间目标
     */
    private fun loadActiveGoals() {
        viewModelScope.launch {
            repository.getActiveTimeGoals().collect { goals ->
                _state.update { it.copy(activeGoals = goals) }
            }
        }
    }
    
    /**
     * 加载统计数据
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            // 获取本周的日期范围
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfWeek = calendar.time
            
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val endOfWeek = calendar.time
            
            // 加载本周的统计数据
            loadStatisticsForRange(startOfWeek, endOfWeek)
        }
    }
    
    /**
     * 加载指定日期范围的统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    private suspend fun loadStatisticsForRange(startDate: Date, endDate: Date) {
        try {
            // 获取分类统计
            val categoryBreakdown = repository.getCategoryBreakdown(startDate, endDate)
            
            // 获取总番茄钟数
            val totalPomodoros = repository.getTotalCompletedPomodoros(startDate, endDate)
            
            // 获取总追踪时间
            val totalTrackedTime = repository.getTotalTrackedTime(startDate, endDate)
            
            // 更新统计状态
            _state.update { 
                it.copy(
                    statistics = TimeStatistics(
                        totalTrackedSeconds = totalTrackedTime,
                        totalPomodoros = totalPomodoros,
                        categoryBreakdown = categoryBreakdown,
                        dateRange = Pair(startDate, endDate)
                    )
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = "加载统计数据失败: ${e.message}") }
        }
    }
    
    /**
     * 过滤时间条目
     * @param entries 原始时间条目列表
     * @return 过滤后的时间条目列表
     */
    private fun filterEntries(entries: List<TimeEntry>): List<TimeEntry> {
        val category = _state.value.selectedCategory
        
        return if (category != null) {
            entries.filter { it.category == category }
        } else {
            entries
        }
    }
    
    /**
     * 更新时间目标进度
     * @param completedEntry 完成的时间条目
     */
    private suspend fun updateGoalsProgress(completedEntry: TimeEntry) {
        // 查找与该时间条目相关的目标
        val relatedGoals = _state.value.activeGoals.filter { goal ->
            // 按分类匹配
            val categoryMatch = goal.category == completedEntry.category.name
            
            // 按引用ID匹配
            val refMatch = when (goal.referenceType) {
                "task" -> goal.referenceId == completedEntry.taskId
                else -> false
            }
            
            categoryMatch || refMatch
        }
        
        // 更新每个相关目标的进度
        relatedGoals.forEach { goal ->
            repository.updateTimeGoalProgress(goal.id, completedEntry.duration)
        }
    }
    
    /**
     * 计算统计数据
     */
    private fun calculateStatistics() {
        viewModelScope.launch {
            val entries = _state.value.timeEntries
            
            if (entries.isEmpty()) return@launch
            
            val totalDuration = entries.sumOf { it.duration }
            
            // 计算生产性时间（工作和学习）
            val productiveTime = entries
                .filter { it.category == TimeCategory.WORK || it.category == TimeCategory.STUDY }
                .sumOf { it.duration }
            
            // 按分类统计时间
            val categoryBreakdown = entries
                .groupBy { it.category }
                .mapValues { (_, entries) -> entries.sumOf { it.duration } }
                .mapKeys { it.key.name } // 转换为 Map<String, Long>
            
            // 计算最常用的分类
            val mostTrackedCategory = entries
                .groupBy { it.category }
                .mapValues { (_, entries) -> entries.sumOf { it.duration } }
                .maxByOrNull { it.value }
                ?.key
            
            // 计算日均时间
            val firstEntryDate = entries.minByOrNull { it.startTime }?.startTime ?: Date()
            val daysCount = ((Date().time - firstEntryDate.time) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
            val dailyAverage = totalDuration / daysCount
            
            // 计算连续记录的天数（当前连续和最长连续）
            // 这个计算比较复杂，暂时使用简化版本
            val currentStreak = calculateCurrentStreak(entries)
            val longestStreak = calculateLongestStreak(entries)
            
            val statistics = TimeStatistics(
                totalTrackedSeconds = totalDuration,
                totalPomodoros = 0, // 暂时设为0，根据需要修改
                categoryBreakdown = categoryBreakdown,
                dateRange = null,
                dailyAverage = dailyAverage,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                mostTrackedCategory = mostTrackedCategory
            )
            
            _state.update { it.copy(statistics = statistics) }
        }
    }
    
    /**
     * 计算当前连续记录天数
     */
    private fun calculateCurrentStreak(entries: List<TimeEntry>): Int {
        if (entries.isEmpty()) return 0
        
        // 将时间条目按日期分组
        val entriesByDay = entries.groupBy { entry ->
            val cal = Calendar.getInstance().apply { time = entry.startTime }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
        
        val today = Calendar.getInstance()
        val todayKey = Triple(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        
        var streak = 0
        var currentDay = today
        
        // 检查今天是否有记录
        val hasEntryToday = entriesByDay.containsKey(todayKey)
        if (!hasEntryToday) {
            // 如果今天没有记录，检查昨天
            currentDay.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        // 从当前日期向前数，直到找到没有记录的一天
        while (true) {
            val key = Triple(
                currentDay.get(Calendar.YEAR),
                currentDay.get(Calendar.MONTH),
                currentDay.get(Calendar.DAY_OF_MONTH)
            )
            
            if (entriesByDay.containsKey(key)) {
                streak++
                currentDay.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        
        return streak
    }
    
    /**
     * 计算最长连续记录天数
     */
    private fun calculateLongestStreak(entries: List<TimeEntry>): Int {
        if (entries.isEmpty()) return 0
        
        // 将时间条目按日期分组
        val entriesByDay = entries.groupBy { entry ->
            val cal = Calendar.getInstance().apply { time = entry.startTime }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
        
        // 按日期排序
        val sortedDays = entriesByDay.keys.sortedWith { day1, day2 ->
            val cal1 = Calendar.getInstance().apply {
                set(Calendar.YEAR, day1.first)
                set(Calendar.MONTH, day1.second)
                set(Calendar.DAY_OF_MONTH, day1.third)
            }
            val cal2 = Calendar.getInstance().apply {
                set(Calendar.YEAR, day2.first)
                set(Calendar.MONTH, day2.second)
                set(Calendar.DAY_OF_MONTH, day2.third)
            }
            cal1.compareTo(cal2)
        }
        
        if (sortedDays.isEmpty()) return 0
        
        var longestStreak = 1
        var currentStreak = 1
        var previousDay = sortedDays[0]
        
        for (i in 1 until sortedDays.size) {
            val currentDay = sortedDays[i]
            
            val cal1 = Calendar.getInstance().apply {
                set(Calendar.YEAR, previousDay.first)
                set(Calendar.MONTH, previousDay.second)
                set(Calendar.DAY_OF_MONTH, previousDay.third)
            }
            
            val cal2 = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentDay.first)
                set(Calendar.MONTH, currentDay.second)
                set(Calendar.DAY_OF_MONTH, currentDay.third)
            }
            
            // 检查是否是连续的一天
            cal1.add(Calendar.DAY_OF_YEAR, 1)
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
            
            previousDay = currentDay
        }
        
        return longestStreak
    }
    
    /**
     * 开始番茄钟会话
     * @param taskId 任务ID
     */
    fun startPomodoroSession(taskId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // 从任务仓库加载任务
                // 简化实现：创建一个示例任务
                val task = Task(
                    id = taskId.toLongOrNull() ?: UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
                    title = "示例任务 $taskId",
                    description = "这是一个示例任务",
                    type = TaskType.POMODORO,
                    priority = TaskPriority.MEDIUM,
                    dueDate = Date(),
                    isCompleted = false
                )
                
                // 设置番茄钟会话
                _state.update { 
                    it.copy(
                        currentTask = task,
                        totalTimeInSeconds = 25 * 60, // 25分钟
                        remainingTimeInSeconds = 25 * 60,
                        totalSessions = 4,
                        currentSession = 1,
                        isRunning = false,
                        isPaused = false,
                        isBreakTime = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "加载任务失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 开始计时器
     */
    fun startTimer() {
        // 取消之前的计时器
        timerJob?.cancel()
        
        timerJob = viewModelScope.launch {
            _state.update { it.copy(isRunning = true, isPaused = false) }
            
            while (_state.value.remainingTimeInSeconds > 0) {
                delay(1000)
                _state.update { 
                    it.copy(remainingTimeInSeconds = it.remainingTimeInSeconds - 1)
                }
            }
            
            // 时间结束
            if (_state.value.isBreakTime) {
                // 休息结束
                finishBreak()
            } else {
                // 工作时间结束
                _state.update { 
                    it.copy(
                        isRunning = false,
                        sessionCompleted = true
                    )
                }
            }
        }
    }
    
    /**
     * 暂停计时器
     */
    fun pauseTimer() {
        timerJob?.cancel()
        _state.update { it.copy(isRunning = false, isPaused = true) }
    }
    
    /**
     * 停止计时器
     */
    fun stopTimer() {
        timerJob?.cancel()
        _state.update { 
            it.copy(
                isRunning = false,
                isPaused = false,
                sessionCompleted = true
            )
        }
    }
    
    /**
     * 跳过休息
     */
    fun skipBreak() {
        timerJob?.cancel()
        
        // 判断是否是最后一个会话
        if (_state.value.currentSession >= _state.value.totalSessions) {
            // 所有会话已完成
            _state.update { 
                it.copy(
                    isRunning = false,
                    isPaused = false,
                    isBreakTime = false,
                    sessionCompleted = true
                )
            }
        } else {
            // 切换到下一个番茄钟
            _state.update { 
                it.copy(
                    currentSession = it.currentSession + 1,
                    isBreakTime = false,
                    remainingTimeInSeconds = 25 * 60,
                    isRunning = false,
                    isPaused = false
                )
            }
        }
    }
    
    /**
     * 休息时间结束
     */
    fun finishBreak() {
        // 判断是否是最后一个会话
        if (_state.value.currentSession >= _state.value.totalSessions) {
            // 所有会话已完成
            _state.update { 
                it.copy(
                    isRunning = false,
                    isPaused = false,
                    isBreakTime = false,
                    sessionCompleted = true
                )
            }
        } else {
            // 切换到下一个番茄钟
            _state.update { 
                it.copy(
                    currentSession = it.currentSession + 1,
                    isBreakTime = false,
                    remainingTimeInSeconds = 25 * 60,
                    isRunning = false,
                    isPaused = false
                )
            }
        }
    }
    
    /**
     * 确认会话完成
     */
    fun acknowledgeSessionCompleted() {
        _state.update { it.copy(sessionCompleted = false) }
    }
    
    /**
     * 保存会话
     * @param notes 会话笔记
     */
    fun saveSession(notes: String = "") {
        val task = _state.value.currentTask ?: return
        
        viewModelScope.launch {
            try {
                // 创建时间条目
                val timeEntry = TimeEntry(
                    id = 0,
                    title = "番茄钟: ${task.title}",
                    description = "完成了 ${_state.value.currentSession} 个番茄钟。$notes",
                    startTime = Date(System.currentTimeMillis() - _state.value.totalTimeInSeconds * 1000),
                    endTime = Date(),
                    duration = _state.value.totalTimeInSeconds.toLong(),
                    category = TimeCategory.STUDY,
                    taskId = task.id,
                    tags = listOf()
                )
                
                // 保存时间条目
                repository.saveTimeEntry(timeEntry)
                
                // 重置状态
                _state.update { 
                    it.copy(
                        sessionCompleted = false,
                        currentTask = null,
                        currentSession = 1,
                        totalSessions = 4,
                        isRunning = false,
                        isPaused = false,
                        isBreakTime = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "保存会话失败: ${e.message}") }
            }
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 