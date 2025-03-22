package com.example.test2.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.model.Goal
import com.example.test2.data.model.Task
import com.example.test2.data.model.TaskPriority
import com.example.test2.data.model.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * 任务管理ViewModel
 */
class TasksViewModel : ViewModel() {

    // 状态
    private val _state = MutableStateFlow(TasksState.initial())
    val state: StateFlow<TasksState> = _state.asStateFlow()

    // 模拟任务数据
    private val dummyTasks = generateDummyTasks()
    
    // 模拟目标数据
    private val dummyGoals = generateDummyGoals()

    init {
        loadTasks()
        loadGoals()
    }

    /**
     * 处理UI事件
     */
    fun onEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.LoadTasks -> loadTasks()
            is TasksEvent.LoadGoals -> loadGoals()
            is TasksEvent.SelectDate -> selectDate(event.date)
            is TasksEvent.FilterTasks -> filterTasks(event.filter)
            is TasksEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is TasksEvent.AddTask -> addTask(event.task)
            is TasksEvent.UpdateTask -> updateTask(event.task)
            is TasksEvent.DeleteTask -> deleteTask(event.taskId)
            is TasksEvent.CompleteTask -> completeTask(event.taskId, event.isCompleted)
            is TasksEvent.ShowAddTaskDialog -> showAddTaskDialog()
            is TasksEvent.ShowEditTaskDialog -> showEditTaskDialog(event.task)
            is TasksEvent.DismissDialog -> dismissDialog()
            is TasksEvent.DragStateChanged -> updateDragState(event.isDragging)
        }
    }

    /**
     * 加载任务列表
     */
    private fun loadTasks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 模拟网络延迟
            kotlinx.coroutines.delay(500)
            
            _state.update { 
                val tasks = dummyTasks
                it.copy(
                    isLoading = false,
                    tasks = tasks,
                    filteredTasks = filterTasksByDate(tasks, it.selectedDate)
                )
            }
        }
    }

    /**
     * 加载目标列表
     */
    private fun loadGoals() {
        viewModelScope.launch {
            // 模拟网络延迟
            kotlinx.coroutines.delay(300)
            
            _state.update { it.copy(goals = dummyGoals) }
        }
    }

    /**
     * 选择日期
     */
    private fun selectDate(date: Date) {
        _state.update { 
            val filteredTasks = filterTasksByDate(it.tasks, date)
            it.copy(
                selectedDate = date,
                filteredTasks = filteredTasks
            )
        }
    }

    /**
     * 筛选任务
     */
    private fun filterTasks(filter: TasksState.Filter) {
        _state.update { 
            val filteredTasks = when (filter) {
                TasksState.Filter.ALL -> it.tasks
                TasksState.Filter.TODAY -> it.tasks.filter { task -> task.isDueToday() }
                TasksState.Filter.UPCOMING -> {
                    val calendar = Calendar.getInstance()
                    val threeDaysLater = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, 3)
                    }
                    
                    it.tasks.filter { task -> 
                        task.dueDate?.let { date ->
                            date.after(calendar.time) && date.before(threeDaysLater.time)
                        } ?: false
                    }
                }
                TasksState.Filter.COMPLETED -> it.tasks.filter { task -> task.isCompleted }
                TasksState.Filter.HIGH_PRIORITY -> it.tasks.filter { task -> task.isHighImportance() }
                TasksState.Filter.ASSOCIATED -> it.tasks.filter { task -> task.goalId != null }
            }
            
            it.copy(
                currentFilter = filter,
                filteredTasks = applySearchFilter(filteredTasks, it.searchQuery)
            )
        }
    }

    /**
     * 更新搜索查询
     */
    private fun updateSearchQuery(query: String) {
        _state.update {
            val filteredTasks = applySearchFilter(
                when (it.currentFilter) {
                    TasksState.Filter.ALL -> it.tasks
                    else -> filterTasks(it.tasks, it.currentFilter)
                },
                query
            )
            
            it.copy(
                searchQuery = query,
                filteredTasks = filteredTasks
            )
        }
    }

    /**
     * 添加任务
     */
    private fun addTask(task: Task) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 模拟网络延迟
            kotlinx.coroutines.delay(500)
            
            val newTask = task.copy(
                id = Random.nextLong(1000, 9999),
                createdAt = Date(),
                updatedAt = Date()
            )
            
            val updatedTasks = _state.value.tasks + newTask
            
            _state.update { 
                it.copy(
                    isLoading = false,
                    tasks = updatedTasks,
                    filteredTasks = filterTasksByDate(updatedTasks, it.selectedDate),
                    showDialog = false
                )
            }
        }
    }

    /**
     * 更新任务
     */
    private fun updateTask(task: Task) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 模拟网络延迟
            kotlinx.coroutines.delay(500)
            
            val updatedTask = task.copy(updatedAt = Date())
            val updatedTasks = _state.value.tasks.map { 
                if (it.id == task.id) updatedTask else it 
            }
            
            _state.update { 
                it.copy(
                    isLoading = false,
                    tasks = updatedTasks,
                    filteredTasks = filterTasksByDate(updatedTasks, it.selectedDate),
                    showDialog = false,
                    selectedTask = null
                )
            }
        }
    }

    /**
     * 删除任务
     */
    private fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 模拟网络延迟
            kotlinx.coroutines.delay(500)
            
            val updatedTasks = _state.value.tasks.filter { it.id != taskId }
            
            _state.update { 
                it.copy(
                    isLoading = false,
                    tasks = updatedTasks,
                    filteredTasks = filterTasksByDate(updatedTasks, it.selectedDate)
                )
            }
        }
    }

    /**
     * 切换任务完成状态
     */
    private fun completeTask(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            // 模拟网络延迟
            kotlinx.coroutines.delay(300)
            
            val updatedTasks = _state.value.tasks.map { 
                if (it.id == taskId) {
                    it.copy(
                        isCompleted = isCompleted,
                        updatedAt = Date()
                    )
                } else {
                    it
                }
            }
            
            _state.update { 
                it.copy(
                    tasks = updatedTasks,
                    filteredTasks = filterTasksByCurrentCriteria(updatedTasks)
                )
            }
        }
    }

    /**
     * 显示添加任务对话框
     */
    private fun showAddTaskDialog() {
        _state.update { it.copy(showDialog = true, selectedTask = null) }
    }

    /**
     * 显示编辑任务对话框
     */
    private fun showEditTaskDialog(task: Task) {
        _state.update { it.copy(showDialog = true, selectedTask = task) }
    }

    /**
     * 关闭对话框
     */
    private fun dismissDialog() {
        _state.update { it.copy(showDialog = false, selectedTask = null) }
    }

    /**
     * 更新拖拽状态
     */
    private fun updateDragState(isDragging: Boolean) {
        _state.update { it.copy(isDragging = isDragging) }
    }

    /**
     * 根据日期筛选任务
     */
    private fun filterTasksByDate(tasks: List<Task>, date: Date): List<Task> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        return tasks.filter { task ->
            task.dueDate?.let { dueDate ->
                val taskCalendar = Calendar.getInstance()
                taskCalendar.time = dueDate
                
                taskCalendar.get(Calendar.YEAR) == year &&
                taskCalendar.get(Calendar.MONTH) == month &&
                taskCalendar.get(Calendar.DAY_OF_MONTH) == day
            } ?: false
        }
    }

    /**
     * 根据筛选条件过滤任务
     */
    private fun filterTasks(tasks: List<Task>, filter: TasksState.Filter): List<Task> {
        return when (filter) {
            TasksState.Filter.ALL -> tasks
            TasksState.Filter.TODAY -> tasks.filter { it.isDueToday() }
            TasksState.Filter.UPCOMING -> {
                val calendar = Calendar.getInstance()
                val threeDaysLater = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 3)
                }
                
                tasks.filter { 
                    it.dueDate?.let { date ->
                        date.after(calendar.time) && date.before(threeDaysLater.time)
                    } ?: false
                }
            }
            TasksState.Filter.COMPLETED -> tasks.filter { it.isCompleted }
            TasksState.Filter.HIGH_PRIORITY -> tasks.filter { it.isHighImportance() }
            TasksState.Filter.ASSOCIATED -> tasks.filter { it.goalId != null }
        }
    }

    /**
     * 应用搜索过滤器
     */
    private fun applySearchFilter(tasks: List<Task>, query: String): List<Task> {
        if (query.isBlank()) return tasks
        
        val lowerCaseQuery = query.trim().lowercase()
        return tasks.filter { task -> 
            task.title.lowercase().contains(lowerCaseQuery) ||
            task.description?.lowercase()?.contains(lowerCaseQuery) ?: false
        }
    }

    /**
     * 根据当前条件过滤任务
     */
    private fun filterTasksByCurrentCriteria(tasks: List<Task>): List<Task> {
        val state = _state.value
        val filteredByDate = filterTasksByDate(tasks, state.selectedDate)
        val filteredByOtherCriteria = filterTasks(filteredByDate, state.currentFilter)
        return applySearchFilter(filteredByOtherCriteria, state.searchQuery)
    }

    /**
     * 生成模拟任务数据
     */
    private fun generateDummyTasks(): List<Task> {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        val nextWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }
        
        return listOf(
            Task(
                id = 1,
                title = "准备周会演示文稿",
                description = "整理本周工作进展，准备周五的团队会议演示",
                priority = TaskPriority.HIGH,
                dueDate = tomorrow.time,
                goalId = 1,
                isCompleted = false
            ),
            Task(
                id = 2,
                title = "完成项目文档",
                description = "完善项目需求文档和技术方案",
                priority = TaskPriority.MEDIUM,
                dueDate = nextWeek.time,
                isCompleted = false
            ),
            Task(
                id = 3,
                title = "修复登录界面bug",
                description = "修复用户登录时可能出现的崩溃问题",
                priority = TaskPriority.HIGH,
                dueDate = today.time,
                isCompleted = false
            ),
            Task(
                id = 4,
                title = "健身1小时",
                description = "进行力量训练和有氧运动",
                priority = TaskPriority.LOW,
                dueDate = yesterday.time,
                isCompleted = true
            ),
            Task(
                id = 5,
                title = "阅读《原子习惯》",
                description = "阅读第5-8章并做笔记",
                priority = TaskPriority.MEDIUM,
                dueDate = today.time,
                goalId = 4,
                isCompleted = false
            ),
            Task(
                id = 6,
                title = "学习Kotlin协程",
                description = "研究协程的高级用法和实际应用场景",
                priority = TaskPriority.HIGH,
                dueDate = nextWeek.time,
                goalId = 1,
                isCompleted = false
            ),
            Task(
                id = 7,
                title = "购买生日礼物",
                description = "为朋友准备生日礼物",
                priority = TaskPriority.MEDIUM,
                dueDate = tomorrow.time,
                isCompleted = false
            ),
            Task(
                id = 8,
                title = "回复重要邮件",
                description = "回复客户关于项目进展的邮件",
                priority = TaskPriority.HIGH,
                dueDate = yesterday.time,
                isCompleted = true
            ),
            Task(
                id = 9,
                title = "制定下周工作计划",
                description = "规划下周的工作任务和目标",
                priority = TaskPriority.MEDIUM,
                dueDate = Calendar.getInstance().apply { 
                    set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                }.time,
                isCompleted = false
            ),
            Task(
                id = 10,
                title = "整理工作台",
                description = "清理和整理工作环境",
                priority = TaskPriority.LOW,
                dueDate = today.time,
                isCompleted = false
            )
        )
    }

    /**
     * 生成模拟目标数据
     */
    private fun generateDummyGoals(): List<Goal> {
        return listOf(
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
                deadline = Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000)
            ),
            Goal(
                id = 4,
                title = "阅读《原子习惯》",
                description = "阅读完成《原子习惯》这本书，并做好读书笔记",
                isLongTerm = false,
                isImportant = false,
                progress = 0.8f,
                deadline = Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000)
            )
        )
    }
} 