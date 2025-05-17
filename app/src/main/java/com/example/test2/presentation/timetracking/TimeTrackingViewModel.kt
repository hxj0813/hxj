package com.example.test2.presentation.timetracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.local.entity.timetracking.TimeGoalEntity
import com.example.test2.data.local.entity.timetracking.TimeTagEntity
import com.example.test2.data.local.entity.TaskTagEntity
import com.example.test2.data.model.Task
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import com.example.test2.data.repository.TimeTrackingRepository
import com.example.test2.data.repository.PomodoroTaskRepository
import com.example.test2.data.repository.TaskRepository
import com.example.test2.data.repository.TaskTagRepository
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
import com.example.test2.data.local.entity.PomodoroTaskEntity
import com.example.test2.data.local.entity.TagCategory

/**
 * 时间追踪ViewModel
 * 管理时间追踪界面的状态和业务逻辑
 */
@HiltViewModel
class TimeTrackingViewModel @Inject constructor(
    private val repository: TimeTrackingRepository,
    private val pomodoroTaskRepository: PomodoroTaskRepository,
    private val taskRepository: TaskRepository,
    private val taskTagRepository: TaskTagRepository
) : ViewModel() {
    
    // UI状态
    private val _state = MutableStateFlow(TimeTrackingState())
    val state: StateFlow<TimeTrackingState> = _state
    
    // 记录当前的Job，用于取消
    private var currentJob: Job? = null
    
    // 计时器Job
    private var timerJob: Job? = null
    
    // 标签状态
    val taskTags = taskTagRepository.getAllTags()
    
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
            is TimeTrackingEvent.SetDateRange -> setDateRange(event.startDate, event.endDate)
            is TimeTrackingEvent.StartTimeEntry -> startTimeEntry(event.timeEntry)
            is TimeTrackingEvent.StopTimeEntry -> stopTimeEntry(event.endTime)
            is TimeTrackingEvent.UpdateTimeEntry -> updateTimeEntry(event.timeEntry)
            is TimeTrackingEvent.DeleteTimeEntry -> deleteTimeEntry(event.id)
            is TimeTrackingEvent.SelectTimeEntry -> selectTimeEntry(event.timeEntry)
            is TimeTrackingEvent.ShowEditEntryDialog -> showEditEntryDialog(event.timeEntry)
            is TimeTrackingEvent.ShowTagDialog -> showTagDialog(event.tag)
            is TimeTrackingEvent.DismissDialog -> dismissDialog()
            is TimeTrackingEvent.CalculateStatistics -> calculateStatistics()
            else -> {} // 忽略不支持的事件
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
                    val ongoingEntry = entries.find { it.isOngoing() }
                    
                    it.copy(
                        timeEntries = entries,
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
            val (startOfDay, endOfDay) = getDateTimeRange(date)
            
            // 加载指定日期的时间条目
            repository.getTimeEntriesBetween(startOfDay, endOfDay).collect { entries ->
                _state.update {
                    it.copy(
                        timeEntries = entries,
                        isLoading = false
                    )
                }
            }
            
            // 加载所选日期的统计数据
            loadStatisticsForRange(startOfDay, endOfDay)
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
                    it.copy(
                        timeEntries = entries,
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
            
            // 刷新统计数据
            refreshCurrentDateStatistics()
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
            
            // 刷新统计数据
            refreshCurrentDateStatistics()
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
            
            // 刷新统计数据
            refreshCurrentDateStatistics()
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
                    selectedEntry = null
                )
            }
            
            // 刷新统计数据
            refreshCurrentDateStatistics()
        }
    }
    
    /**
     * 选择时间条目
     * @param timeEntry 时间条目
     */
    private fun selectTimeEntry(timeEntry: TimeEntry?) {
        _state.update { it.copy(selectedEntry = timeEntry) }
    }
    
    /**
     * 显示编辑时间条目对话框
     * @param timeEntry 时间条目
     */
    private fun showEditEntryDialog(timeEntry: TimeEntry?) {
        _state.update { 
            it.copy(
                selectedEntry = timeEntry,
                showEntryDialog = true
            )
        }
    }
    
    /**
     * 显示标签对话框
     */
    private fun showTagDialog(tag: TaskTagEntity?) {
        _state.update { 
            it.copy(
                selectedTag = tag,
                showTagDialog = true
            )
        }
    }
    
    /**
     * 关闭所有对话框
     */
    private fun dismissDialog() {
        _state.update { 
            it.copy(
                showEntryDialog = false,
                showTagDialog = false,
                selectedEntry = null,
                selectedTag = null
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
            println("开始加载统计数据: ${startDate} - ${endDate}")
            
            // 获取分类统计
            val categoryBreakdown = repository.getCategoryBreakdown(startDate, endDate)
            println("分类统计加载完成: ${categoryBreakdown}")
            
            // 获取总番茄钟数
            val totalPomodoros = repository.getTotalCompletedPomodoros(startDate, endDate)
            println("总番茄钟数: ${totalPomodoros}")
            
            // 获取总追踪时间
            val totalTrackedTime = repository.getTotalTrackedTime(startDate, endDate)
            println("总追踪时间: ${totalTrackedTime}秒")
            
            // 创建一个新的TimeStatistics对象，并添加一个随机标识，确保状态变化被检测到
            val statisticsTimestamp = System.currentTimeMillis()
            val newStatistics = TimeStatistics(
                totalTrackedSeconds = totalTrackedTime,
                totalPomodoros = totalPomodoros,
                categoryBreakdown = categoryBreakdown.toMutableMap().also { 
                    // 添加一个微小的随机偏移，确保状态更新被检测到
                    it["_timestamp"] = statisticsTimestamp % 10 // 只保留最后一位数字，影响微乎其微
                },
                dateRange = Pair(startDate, endDate)
            )
            
            // 更新统计状态
            _state.update { 
                it.copy(statistics = newStatistics)
            }
            println("统计状态已更新，时间戳: $statisticsTimestamp")
        } catch (e: Exception) {
            println("加载统计数据失败: ${e.message}")
            _state.update { it.copy(error = "加载统计数据失败: ${e.message}") }
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
            
            // 按分类统计时间 - 优先使用标签，没有标签则使用分类
            val categoryBreakdown = entries
                .groupBy { entry -> 
                    if (entry.tags.isNotEmpty()) {
                        // 使用第一个标签作为分类
                        entry.tags.first()
                    } else {
                        // 使用分类名称
                        entry.category.name
                    }
                }
                .mapValues { (_, entries) -> entries.sumOf { it.duration } }
            
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
                
                // 获取番茄钟任务设置
                val pomodoroTask = pomodoroTaskRepository.getPomodoroTaskById(taskId)
                
                // 获取番茄钟时间设置，如果没有找到任务则使用默认值
                val focusMinutes = pomodoroTask?.pomodoroLength ?: 25
                val shortBreak = pomodoroTask?.shortBreakLength ?: 5
                val longBreak = pomodoroTask?.longBreakLength ?: 15
                val sessionsCount = pomodoroTask?.estimatedPomodoros ?: 4
                
                // 尝试从任务仓库获取任务详情
                val dbTask = taskRepository.getTaskById(taskId)
                
                // 创建一个任务对象，优先使用数据库中的任务信息
                val task = Task(
                    id = taskId.toLongOrNull() ?: 0,
                    title = dbTask?.title ?: "专注任务",  // 使用数据库中的标题，PomodoroTaskEntity没有title属性
                    description = dbTask?.description ?: "",  // 使用数据库中的描述，PomodoroTaskEntity没有description属性
                    type = TaskType.POMODORO,
                    priority = TaskPriority.MEDIUM,
                    dueDate = Date(),
                    isCompleted = false
                )
                
                // 设置番茄钟会话
                _state.update { 
                    it.copy(
                        currentTask = task,
                        totalTimeInSeconds = focusMinutes * 60, // 转换为秒
                        remainingTimeInSeconds = focusMinutes * 60,
                        totalSessions = sessionsCount,
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
     * 开始休息时间（公共方法）
     */
    fun startBreak() {
        startBreakPrivate()
    }

    /**
     * 开始休息时间
     */
    private fun startBreakPrivate() {
        timerJob?.cancel()
        
        val currentState = _state.value
        
        // 从状态中获取任务ID
        val taskId = currentState.currentTask?.id?.toString() ?: ""
        
        // 使用协程启动以访问挂起函数
        viewModelScope.launch {
            try {
                // 获取番茄钟任务详情
                val pomodoroTask = pomodoroTaskRepository.getPomodoroTaskById(taskId)
                
                // 判断是否需要长休息（使用longBreakInterval或默认值4）
                val longBreakInterval = pomodoroTask?.longBreakInterval ?: 4
                val isLongBreak = currentState.currentSession % longBreakInterval == 0
                
                // 设置休息时间（使用设置的值或默认值）
                val breakTimeMinutes = if (isLongBreak) {
                    pomodoroTask?.longBreakLength ?: 15 // 长休息时间
                } else {
                    pomodoroTask?.shortBreakLength ?: 5  // 短休息时间
                }
                
                _state.update {
                    it.copy(
                        isBreakTime = true,
                        remainingTimeInSeconds = breakTimeMinutes * 60,
                        isRunning = false,
                        isPaused = false
                    )
                }
                
                // 自动启动休息计时器
                startTimer()
                
            } catch (e: Exception) {
                // 出错时使用默认设置
                val isLongBreak = currentState.currentSession % 4 == 0
                val breakTimeMinutes = if (isLongBreak) 15 else 5
                
                _state.update {
                    it.copy(
                        isBreakTime = true,
                        remainingTimeInSeconds = breakTimeMinutes * 60,
                        isRunning = false,
                        isPaused = false
                    )
                }
                
                // 自动启动休息计时器
                startTimer()
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
                // 工作时间结束，开始休息时间
                if (_state.value.currentSession < _state.value.totalSessions) {
                    startBreakPrivate()
                } else {
                    // 最后一个番茄钟完成
                    _state.update { 
                        it.copy(
                            isRunning = false,
                            sessionCompleted = true
                        )
                    }
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
            val focusMinutes = _state.value.totalTimeInSeconds / 60 // 使用初始设置的专注时间
            _state.update { 
                it.copy(
                    currentSession = it.currentSession + 1,
                    isBreakTime = false,
                    remainingTimeInSeconds = focusMinutes * 60,
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
            val focusMinutes = _state.value.totalTimeInSeconds / 60 // 使用初始设置的专注时间
            _state.update { 
                it.copy(
                    currentSession = it.currentSession + 1,
                    isBreakTime = false,
                    remainingTimeInSeconds = focusMinutes * 60,
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
    fun saveSession(notes: String? = null) {
        val task = _state.value.currentTask ?: return
        
        viewModelScope.launch {
            try {
                // 获取番茄钟任务信息
                val pomodoroTask = pomodoroTaskRepository.getPomodoroTaskById(task.id.toString())
                
                println("正在保存番茄钟会话，任务ID: ${task.id}")
                if (pomodoroTask != null) {
                    println("找到番茄钟任务，标签ID: ${pomodoroTask.tagId}")
                } else {
                    println("未找到番茄钟任务")
                }
                
                // 确定标签名称
                var tagName: String? = null
                
                if (pomodoroTask != null) {
                    // 1. 首先检查自定义标签名称
                    if (!pomodoroTask.customTagName.isNullOrBlank()) {
                        tagName = pomodoroTask.customTagName
                        println("使用自定义标签名称: $tagName")
                    } 
                    // 2. 然后检查标签ID
                    else if (pomodoroTask.tagId != null) {
                        val tag = taskTagRepository.getTagById(pomodoroTask.tagId)
                        if (tag != null) {
                            tagName = tag.name
                            println("使用标签ID找到标签: $tagName")
                        } else {
                            println("标签ID不存在: ${pomodoroTask.tagId}")
                        }
                    }
                    // 3. 最后使用分类名称作为标签
                    if (tagName == null) {
                        // 使用分类的英文名称作为标签
                        val tagCategory = pomodoroTask.getTagCategoryEnum()
                        tagName = when (tagCategory) {
                            TagCategory.WORK -> "工作"
                            TagCategory.STUDY -> "学习"
                            TagCategory.EXERCISE -> "运动"
                            TagCategory.READING -> "阅读"
                            TagCategory.CREATIVE -> "创意"
                            TagCategory.PERSONAL -> "个人发展"
                            else -> "其他"
                        }
                        println("使用分类名称作为标签: $tagName")
                    }
                }
                
                // 使用FOCUS分类来确保在时间条目列表中能正确显示标签
                val category = TimeCategory.FOCUS
                
                // 创建时间条目，确保设置标签和分类
                val timeEntry = TimeEntry(
                    id = 0,
                    title = "番茄钟: ${task.title}",
                    description = "完成了 ${_state.value.currentSession} 个番茄钟",
                    startTime = Date(System.currentTimeMillis() - _state.value.totalTimeInSeconds * 1000),
                    endTime = Date(),
                    duration = _state.value.totalTimeInSeconds.toLong(),
                    category = category, // 使用FOCUS分类
                    taskId = task.id,
                    // 设置标签列表
                    tags = if (tagName != null) listOf(tagName) else listOf(),
                    // 保存标签ID
                    tagId = pomodoroTask?.tagId
                )
                
                println("创建时间条目: ${timeEntry.title}")
                println("标签: ${timeEntry.tags}")
                println("分类: ${timeEntry.category}")
                
                // 保存时间条目
                repository.saveTimeEntry(timeEntry)
                
                // 更新番茄钟任务完成情况
                if (pomodoroTask != null) {
                    // 计算专注时间（分钟）
                    val focusMinutes = _state.value.currentSession * pomodoroTask.pomodoroLength
                    
                    // 更新番茄钟任务
                    pomodoroTaskRepository.addFocusTime(
                        taskId = task.id.toString(),
                        focusMinutes = focusMinutes,
                        pomodoroCount = _state.value.currentSession
                    )
                    
                    // 检查任务是否已完成所有预计的番茄钟
                    checkAndUpdateTaskCompletion(pomodoroTask)
                }
                
                // 刷新统计数据以立即更新环形图
                refreshCurrentDateStatistics()
                
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
     * 检查并更新任务完成状态
     * 如果任务的estimatedPomodoros小于等于completedPomodoros，则将任务标记为已完成
     */
    private suspend fun checkAndUpdateTaskCompletion(pomodoroTask: PomodoroTaskEntity) {
        try {
            // 如果已完成的番茄钟数量大于或等于预估数量，则标记任务为已完成
            if (pomodoroTask.completedPomodoros >= pomodoroTask.estimatedPomodoros) {
                // 更新任务状态为已完成
                taskRepository.updateTaskCompletion(pomodoroTask.taskId, true)
                
                println("任务 ${pomodoroTask.taskId} 已完成所有预估番茄钟，已标记为已完成")
            }
        } catch (e: Exception) {
            println("更新任务完成状态失败: ${e.message}")
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    /**
     * 计算指定日期的起止时间范围
     * @param date 指定日期
     * @return 包含起止时间的Pair
     */
    private fun getDateTimeRange(date: Date): Pair<Date, Date> {
        val calendar = Calendar.getInstance().apply { time = date }
        val startOfDay = calendar.clone() as Calendar
        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
        startOfDay.set(Calendar.MINUTE, 0)
        startOfDay.set(Calendar.SECOND, 0)
        startOfDay.set(Calendar.MILLISECOND, 0)
        
        val endOfDay = calendar.clone() as Calendar
        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
        endOfDay.set(Calendar.MINUTE, 59)
        endOfDay.set(Calendar.SECOND, 59)
        endOfDay.set(Calendar.MILLISECOND, 999)
        
        return Pair(startOfDay.time, endOfDay.time)
    }
    
    /**
     * 刷新当前选择日期的统计数据
     */
    private fun refreshCurrentDateStatistics() {
        viewModelScope.launch {
            // 刷新总体统计数据
            loadStatistics()
            
            // 刷新当前选择日期的统计数据
            val (startOfDay, endOfDay) = getDateTimeRange(_state.value.selectedDate)
            loadStatisticsForRange(startOfDay, endOfDay)
        }
    }
    
    /**
     * 保存标签
     */
    fun saveTag(tag: TaskTagEntity) {
        viewModelScope.launch {
            try {
                if (tag.id.isNotEmpty()) {
                    // 更新现有标签
                    taskTagRepository.updateTag(tag)
                } else {
                    // 创建新标签
                    taskTagRepository.createTag(tag)
                }
            } catch (e: Exception) {
                // 处理错误
                _state.update { 
                    it.copy(error = "保存标签时出错: ${e.message}") 
                }
            }
        }
    }
} 