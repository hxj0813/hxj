package com.example.test2.presentation.timetracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.model.Task
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import com.example.test2.util.DateTimeUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * 时间追踪ViewModel
 */
class TimeTrackingViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(TimeTrackingState.initial())
    val state: StateFlow<TimeTrackingState> = _state.asStateFlow()
    
    init {
        loadTimeEntries()
        loadTasks()
        calculateStatistics()
    }
    
    /**
     * 处理事件
     */
    fun onEvent(event: TimeTrackingEvent) {
        when (event) {
            is TimeTrackingEvent.LoadTimeEntries -> loadTimeEntries()
            is TimeTrackingEvent.LoadTasks -> loadTasks()
            is TimeTrackingEvent.SelectDate -> selectDate(event.date)
            is TimeTrackingEvent.FilterCategory -> filterByCategory(event.category)
            is TimeTrackingEvent.SetDateRange -> setDateRange(event.startDate, event.endDate)
            is TimeTrackingEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is TimeTrackingEvent.StartTimeEntry -> startTimeEntry(event.timeEntry)
            is TimeTrackingEvent.StopTimeEntry -> stopTimeEntry(event.endTime)
            is TimeTrackingEvent.AddTimeEntry -> addTimeEntry(event.timeEntry)
            is TimeTrackingEvent.UpdateTimeEntry -> updateTimeEntry(event.timeEntry)
            is TimeTrackingEvent.DeleteTimeEntry -> deleteTimeEntry(event.id)
            is TimeTrackingEvent.ShowAddEntryDialog -> showAddEntryDialog()
            is TimeTrackingEvent.ShowEditEntryDialog -> showEditEntryDialog(event.timeEntry)
            is TimeTrackingEvent.ShowFilterDialog -> showFilterDialog()
            is TimeTrackingEvent.DismissDialog -> dismissDialog()
            is TimeTrackingEvent.CalculateStatistics -> calculateStatistics()
            is TimeTrackingEvent.SelectTimeEntry -> selectTimeEntry(event.timeEntry)
        }
    }
    
    /**
     * 加载时间条目
     */
    private fun loadTimeEntries() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 模拟网络延迟
            delay(800)
            
            // 模拟数据
            val dummyEntries = generateDummyTimeEntries()
            
            _state.update { 
                it.copy(
                    timeEntries = dummyEntries,
                    filteredEntries = dummyEntries,
                    isLoading = false
                )
            }
            
            // 检查是否有正在进行的时间条目
            val ongoingEntry = dummyEntries.find { it.isOngoing() }
            if (ongoingEntry != null) {
                _state.update { it.copy(ongoingEntry = ongoingEntry) }
            }
            
            // 应用当前筛选条件
            applyFilters()
            
            // 计算统计数据
            calculateStatistics()
        }
    }
    
    /**
     * 加载任务
     */
    private fun loadTasks() {
        viewModelScope.launch {
            // 模拟网络延迟
            delay(500)
            
            // 模拟任务数据
            val dummyTasks = generateDummyTasks()
            
            _state.update { 
                it.copy(allTasks = dummyTasks) 
            }
        }
    }
    
    /**
     * 选择日期
     */
    private fun selectDate(date: Date) {
        _state.update { 
            it.copy(selectedDate = date)
        }
        applyFilters()
    }
    
    /**
     * 按分类筛选
     */
    private fun filterByCategory(category: TimeCategory?) {
        _state.update { 
            it.copy(selectedCategory = category)
        }
        applyFilters()
    }
    
    /**
     * 设置日期范围
     */
    private fun setDateRange(startDate: Date, endDate: Date) {
        _state.update { 
            it.copy(dateRange = Pair(startDate, endDate))
        }
        applyFilters()
    }
    
    /**
     * 更新搜索查询
     */
    private fun updateSearchQuery(query: String) {
        _state.update { 
            it.copy(searchQuery = query)
        }
        applyFilters()
    }
    
    /**
     * 开始新的时间条目
     */
    private fun startTimeEntry(timeEntry: TimeEntry) {
        viewModelScope.launch {
            // 如果已有正在进行的时间条目，先停止它
            _state.value.ongoingEntry?.let { 
                stopTimeEntry()
            }
            
            // 模拟网络延迟
            delay(300)
            
            _state.update {
                it.copy(
                    ongoingEntry = timeEntry,
                    timeEntries = it.timeEntries + timeEntry
                )
            }
            
            applyFilters()
        }
    }
    
    /**
     * 停止正在进行的时间条目
     */
    private fun stopTimeEntry(endTime: Date = Date()) {
        viewModelScope.launch {
            val ongoingEntry = _state.value.ongoingEntry ?: return@launch
            
            // 模拟网络延迟
            delay(300)
            
            // 更新时间条目，设置结束时间和持续时间
            val completedEntry = ongoingEntry.complete(endTime)
            
            // 更新时间条目列表
            val updatedEntries = _state.value.timeEntries.map { 
                if (it.id == ongoingEntry.id) completedEntry else it 
            }
            
            _state.update {
                it.copy(
                    ongoingEntry = null,
                    timeEntries = updatedEntries,
                    filteredEntries = applyFiltersToEntries(updatedEntries)
                )
            }
            
            calculateStatistics()
        }
    }
    
    /**
     * 添加时间条目
     */
    private fun addTimeEntry(timeEntry: TimeEntry) {
        viewModelScope.launch {
            // 模拟网络延迟
            delay(300)
            
            val updatedEntries = _state.value.timeEntries + timeEntry
            
            _state.update {
                it.copy(
                    timeEntries = updatedEntries,
                    filteredEntries = applyFiltersToEntries(updatedEntries),
                    showEntryDialog = false
                )
            }
            
            calculateStatistics()
        }
    }
    
    /**
     * 更新时间条目
     */
    private fun updateTimeEntry(timeEntry: TimeEntry) {
        viewModelScope.launch {
            // 模拟网络延迟
            delay(300)
            
            // 更新时间条目列表
            val updatedEntries = _state.value.timeEntries.map { 
                if (it.id == timeEntry.id) timeEntry else it 
            }
            
            _state.update {
                it.copy(
                    timeEntries = updatedEntries,
                    filteredEntries = applyFiltersToEntries(updatedEntries),
                    showEntryDialog = false,
                    selectedEntry = null
                )
            }
            
            calculateStatistics()
        }
    }
    
    /**
     * 删除时间条目
     */
    private fun deleteTimeEntry(id: Long) {
        viewModelScope.launch {
            // 模拟网络延迟
            delay(300)
            
            // 更新时间条目列表
            val updatedEntries = _state.value.timeEntries.filter { it.id != id }
            
            _state.update {
                it.copy(
                    timeEntries = updatedEntries,
                    filteredEntries = applyFiltersToEntries(updatedEntries)
                )
            }
            
            calculateStatistics()
        }
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
        _state.update {
            it.copy(showFilterDialog = true)
        }
    }
    
    /**
     * 关闭对话框
     */
    private fun dismissDialog() {
        _state.update {
            it.copy(
                showEntryDialog = false,
                showFilterDialog = false,
                selectedEntry = null
            )
        }
    }
    
    /**
     * 选择时间条目
     */
    private fun selectTimeEntry(timeEntry: TimeEntry) {
        _state.update { it.copy(selectedEntry = timeEntry) }
    }
    
    /**
     * 应用筛选条件
     */
    private fun applyFilters() {
        val filteredEntries = applyFiltersToEntries(_state.value.timeEntries)
        _state.update { 
            it.copy(filteredEntries = filteredEntries)
        }
    }
    
    /**
     * 应用筛选条件到时间条目列表
     */
    private fun applyFiltersToEntries(entries: List<TimeEntry>): List<TimeEntry> {
        var filtered = entries
        
        // 按日期筛选
        val state = _state.value
        if (state.dateRange != null) {
            val (startDate, endDate) = state.dateRange
            filtered = filtered.filter { 
                it.startTime.after(startDate) && it.startTime.before(endDate)
            }
        } else {
            // 仅筛选选定日期
            filtered = filtered.filter { entry ->
                DateTimeUtil.isSameDay(entry.startTime, state.selectedDate)
            }
        }
        
        // 按分类筛选
        state.selectedCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }
        
        // 按搜索词筛选
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter { 
                it.title.lowercase().contains(query) || 
                it.description?.lowercase()?.contains(query) ?: false
            }
        }
        
        return filtered
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
            
            // 计算最常用的分类
            val mostTrackedCategory = categoryBreakdown.entries
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
            
            val statistics = TimeTrackingState.TimeStatistics(
                totalDuration = totalDuration,
                productiveTime = productiveTime,
                categoryBreakdown = categoryBreakdown,
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
        
        for (i in 1 until sortedDays.size) {
            val prevDay = Calendar.getInstance().apply {
                set(Calendar.YEAR, sortedDays[i-1].first)
                set(Calendar.MONTH, sortedDays[i-1].second)
                set(Calendar.DAY_OF_MONTH, sortedDays[i-1].third)
            }
            val currDay = Calendar.getInstance().apply {
                set(Calendar.YEAR, sortedDays[i].first)
                set(Calendar.MONTH, sortedDays[i].second)
                set(Calendar.DAY_OF_MONTH, sortedDays[i].third)
            }
            
            // 计算两天之间的差值
            val diff = (currDay.timeInMillis - prevDay.timeInMillis) / (24 * 60 * 60 * 1000)
            
            if (diff == 1L) {
                // 连续的一天
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                // 不连续，重置计数
                currentStreak = 1
            }
        }
        
        return longestStreak
    }
    
    /**
     * 生成演示用的时间条目数据
     */
    private fun generateDummyTimeEntries(): List<TimeEntry> {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        // 昨天
        val yesterday = Calendar.getInstance().apply { 
            add(Calendar.DAY_OF_YEAR, -1)
        }
        
        // 前天
        val dayBeforeYesterday = Calendar.getInstance().apply { 
            add(Calendar.DAY_OF_YEAR, -2)
        }
        
        // 一小时前（可能正在进行）
        val oneHourAgo = Calendar.getInstance().apply { 
            add(Calendar.HOUR_OF_DAY, -1)
        }
        
        return listOf(
            // 今天的条目
            TimeEntry(
                id = 1,
                title = "项目开发",
                description = "实现用户登录功能",
                category = TimeCategory.WORK,
                startTime = oneHourAgo.time,
                endTime = null, // 正在进行
                taskId = 1
            ),
            TimeEntry(
                id = 2,
                title = "阅读技术书籍",
                description = "《Clean Architecture》第5章",
                category = TimeCategory.STUDY,
                startTime = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }.time,
                endTime = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 10)
                    set(Calendar.MINUTE, 30)
                }.time
            ),
            TimeEntry(
                id = 3,
                title = "晨跑",
                description = "5公里慢跑",
                category = TimeCategory.EXERCISE,
                startTime = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 7)
                    set(Calendar.MINUTE, 0)
                }.time,
                endTime = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 7)
                    set(Calendar.MINUTE, 45)
                }.time
            ),
            
            // 昨天的条目
            TimeEntry(
                id = 4,
                title = "团队会议",
                description = "讨论项目进度",
                category = TimeCategory.WORK,
                startTime = yesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 14)
                    set(Calendar.MINUTE, 0)
                }.time,
                endTime = yesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 15)
                    set(Calendar.MINUTE, 30)
                }.time
            ),
            TimeEntry(
                id = 5,
                title = "学习Kotlin协程",
                description = "视频教程和实践",
                category = TimeCategory.STUDY,
                startTime = yesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 20)
                    set(Calendar.MINUTE, 0)
                }.time,
                endTime = yesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 22)
                    set(Calendar.MINUTE, 0)
                }.time
            ),
            
            // 前天的条目
            TimeEntry(
                id = 6,
                title = "编写文档",
                description = "项目需求文档",
                category = TimeCategory.WORK,
                startTime = dayBeforeYesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 10)
                    set(Calendar.MINUTE, 0)
                }.time,
                endTime = dayBeforeYesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 30)
                }.time
            ),
            TimeEntry(
                id = 7,
                title = "瑜伽课",
                description = "放松和伸展",
                category = TimeCategory.EXERCISE,
                startTime = dayBeforeYesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 18)
                    set(Calendar.MINUTE, 0)
                }.time,
                endTime = dayBeforeYesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 19)
                    set(Calendar.MINUTE, 0)
                }.time
            ),
            TimeEntry(
                id = 8,
                title = "看电影",
                description = "《盗梦空间》",
                category = TimeCategory.ENTERTAIN,
                startTime = dayBeforeYesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 20)
                    set(Calendar.MINUTE, 0)
                }.time,
                endTime = dayBeforeYesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 22)
                    set(Calendar.MINUTE, 30)
                }.time
            )
        )
    }
    
    /**
     * 生成演示用的任务数据
     */
    private fun generateDummyTasks(): List<Task> {
        return listOf(
            Task(
                id = 1,
                title = "完成用户登录功能",
                description = "实现用户登录和注册界面"
            ),
            Task(
                id = 2,
                title = "修复应用崩溃Bug",
                description = "处理在Android 10设备上的闪退问题"
            ),
            Task(
                id = 3,
                title = "准备团队会议",
                description = "整理本周工作进展，准备周五的团队会议演示"
            )
        )
    }
} 