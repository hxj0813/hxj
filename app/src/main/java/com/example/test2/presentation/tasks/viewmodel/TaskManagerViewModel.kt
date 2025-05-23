package com.example.test2.presentation.tasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.local.entity.CheckInTaskEntity
import com.example.test2.data.local.entity.PomodoroTaskEntity
import com.example.test2.data.local.entity.TaskEntity
import com.example.test2.data.local.entity.TaskLogEntity
import com.example.test2.data.local.entity.TaskPriority
import com.example.test2.data.local.entity.TaskTagEntity
import com.example.test2.data.local.entity.TaskType
import com.example.test2.data.model.Goal
import com.example.test2.data.repository.CheckInTaskRepository
import com.example.test2.data.repository.GoalRepository
import com.example.test2.data.repository.PomodoroTaskRepository
import com.example.test2.data.repository.TaskLogRepository
import com.example.test2.data.repository.TaskRepository
import com.example.test2.data.repository.TaskTagRepository
import com.example.test2.data.local.prefs.PreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * 任务详情状态
 */
data class TaskDetailState(
    val task: TaskEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 任务列表状态
 */
data class TaskListState(
    val activeTasks: List<TaskEntity> = emptyList(),
    val completedTasks: List<TaskEntity> = emptyList(),
    val selectedTaskId: String? = null,
    val filterType: TaskType? = null,
    val searchQuery: String = "",
    val showCompletedTasks: Boolean = false,
    val todayTasksCount: Int = 0,
    val overdueTasksCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * 任务创建/编辑状态
 */
data class TaskEditorState(
    val isCreatingTask: Boolean = false,
    val isEditingTask: Boolean = false,
    val editingTask: TaskEntity? = null,
    val newTaskTitle: String = "",
    val newTaskDescription: String = "",
    val newTaskType: TaskType = TaskType.CHECK_IN,
    val newTaskPriority: TaskPriority = TaskPriority.MEDIUM,
    val newTaskDueDate: Date? = null,
    val newTaskGoalId: Long? = null,
    
    // 打卡任务特有参数
    val newCheckInFrequencyType: Int = 0,
    val newCheckInFrequencyCount: Int = 1,
    val newCheckInFrequencyDays: List<Int> = emptyList(),
    val newCheckInReminderEnabled: Boolean = false,
    val newCheckInReminderTime: Date? = null,
    
    // 番茄任务特有参数
    val newPomodoroEstimatedCount: Int = 1,
    val newPomodoroDuration: Int = 25,
    val newPomodoroShortBreak: Int = 5,
    val newPomodoroLongBreak: Int = 15,
    val newPomodoroTagId: String? = null
) {
    // 是否正在显示任务表单
    val isShowingTaskForm: Boolean get() = isCreatingTask || isEditingTask
}

/**
 * 番茄钟计时状态
 */
data class PomodoroTimerState(
    val isRunning: Boolean = false,
    val currentTaskId: String? = null,
    val currentPhase: PomodoroPhase = PomodoroPhase.IDLE,
    val remainingTimeInSeconds: Int = 0,
    val currentPomodoroCount: Int = 0,
    val totalPomodoroCount: Int = 0,
    val pomodoroLength: Int = 25,
    val shortBreakLength: Int = 5,
    val longBreakLength: Int = 15,
    val longBreakInterval: Int = 4
)

/**
 * 番茄钟阶段
 */
enum class PomodoroPhase {
    IDLE,           // 未开始或已结束
    FOCUS,          // 专注阶段
    SHORT_BREAK,    // 短休息阶段
    LONG_BREAK      // 长休息阶段
}

/**
 * 任务管理ViewModel
 * 负责处理任务列表与任务操作的业务逻辑
 */
@HiltViewModel
class TaskManagerViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val checkInTaskRepository: CheckInTaskRepository,
    private val pomodoroTaskRepository: PomodoroTaskRepository,
    private val taskTagRepository: TaskTagRepository,
    private val taskLogRepository: TaskLogRepository,
    private val goalRepository: GoalRepository,
    private val preferencesHelper: PreferencesHelper
) : ViewModel() {
    
    // 任务列表状态
    private val _taskListState = MutableStateFlow(TaskListState(isLoading = true))
    val taskListState: StateFlow<TaskListState> = _taskListState
    
    // 任务详情状态
    private val _taskDetailState = MutableStateFlow(TaskDetailState(isLoading = false))
    val taskDetailState: StateFlow<TaskDetailState> = _taskDetailState
    
    // 任务编辑器状态
    private val _taskEditorState = MutableStateFlow(TaskEditorState())
    val taskEditorState: StateFlow<TaskEditorState> = _taskEditorState
    
    // 番茄钟计时器状态
    private val _pomodoroTimerState = MutableStateFlow(PomodoroTimerState())
    val pomodoroTimerState: StateFlow<PomodoroTimerState> = _pomodoroTimerState
    
    // 过滤类型
    private val _filterType = MutableStateFlow<TaskType?>(null)
    
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    
    // 是否显示已完成任务
    private val _showCompletedTasks = MutableStateFlow(false)
    
    // 组合任务状态流
    val combinedTaskState: StateFlow<TaskListState> = combine(
        taskRepository.getActiveTasks(),
        taskRepository.getCompletedTasks(),
        _filterType,
        _searchQuery,
        _showCompletedTasks
    ) { activeTasks, completedTasks, filterType, searchQuery, showCompletedTasks ->
        // 根据过滤类型过滤任务
        var filteredActiveTasks = when (filterType) {
            null -> activeTasks
            else -> activeTasks.filter { it.getTaskTypeEnum() == filterType }
        }
        
        var filteredCompletedTasks = when (filterType) {
            null -> completedTasks
            else -> completedTasks.filter { it.getTaskTypeEnum() == filterType }
        }
        
        // 根据搜索查询过滤任务
        if (searchQuery.isNotEmpty()) {
            filteredActiveTasks = filteredActiveTasks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                (it.description?.contains(searchQuery, ignoreCase = true) ?: false)
            }
            
            filteredCompletedTasks = filteredCompletedTasks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                (it.description?.contains(searchQuery, ignoreCase = true) ?: false)
            }
        }
        
        TaskListState(
            activeTasks = filteredActiveTasks,
            completedTasks = filteredCompletedTasks,
            filterType = filterType,
            searchQuery = searchQuery,
            showCompletedTasks = showCompletedTasks,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskListState(isLoading = true)
    )
    
    init {
        // 初始化默认标签
        initDefaultTags()
        // 加载任务统计数据
        loadTaskCounts()
    }
    
    /**
     * 设置任务类型过滤器
     */
    fun setFilterType(taskType: TaskType?) {
        _filterType.value = taskType
    }
    
    /**
     * 设置搜索查询
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * 切换是否显示已完成任务
     */
    fun toggleShowCompletedTasks() {
        _showCompletedTasks.value = !_showCompletedTasks.value
    }
    
    /**
     * 创建新的任务和相关子任务
     */
    fun createTask() {
        viewModelScope.launch {
            try {
                val state = _taskEditorState.value
                
                // 创建基础任务实体
                val taskId = UUID.randomUUID().toString()
                val taskEntity = TaskEntity(
                    id = taskId,
                    title = state.newTaskTitle,
                    description = state.newTaskDescription,
                    taskType = state.newTaskType.ordinal,
                    priority = state.newTaskPriority.ordinal,
                    dueDate = state.newTaskDueDate,
                    goalId = state.newTaskGoalId,
                    tagId = state.newPomodoroTagId,
                    createdAt = Date(),
                    updatedAt = Date()
                )
                
                // 创建基础任务
                taskRepository.createTask(taskEntity)
                
                // 根据任务类型创建相应的子任务
                when (state.newTaskType) {
                    TaskType.CHECK_IN -> {
                        // 创建打卡任务
                        val checkInTask = CheckInTaskEntity(
                            taskId = taskId,
                            frequencyType = state.newCheckInFrequencyType,
                            frequencyCount = state.newCheckInFrequencyCount,
                            frequencyDaysJson = if (state.newCheckInFrequencyDays.isNotEmpty()) {
                                com.google.gson.Gson().toJson(state.newCheckInFrequencyDays)
                            } else null,
                            reminderEnabled = state.newCheckInReminderEnabled,
                            reminderTime = state.newCheckInReminderTime,
                            todayCompletionsCount = 0
                        )
                        checkInTaskRepository.createCheckInTask(checkInTask)
                    }
                    TaskType.POMODORO -> {
                        // 创建番茄钟任务
                        val pomodoroTask = PomodoroTaskEntity(
                            taskId = taskId,
                            estimatedPomodoros = state.newPomodoroEstimatedCount,
                            pomodoroLength = state.newPomodoroDuration,
                            shortBreakLength = state.newPomodoroShortBreak,
                            longBreakLength = state.newPomodoroLongBreak,
                            tagId = state.newPomodoroTagId
                        )
                        pomodoroTaskRepository.createPomodoroTask(pomodoroTask)
                    }
                }
                
                // 重置编辑器状态
                _taskEditorState.value = TaskEditorState()
                
                // 刷新统计数据
                loadTaskCounts()
                
            } catch (e: Exception) {
                _taskListState.value = _taskListState.value.copy(
                    error = "创建任务失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 准备创建新任务
     */
    fun prepareCreateTask() {
        _taskEditorState.value = TaskEditorState(
            isCreatingTask = true,
            newTaskDueDate = Calendar.getInstance().apply { 
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        )
    }
    
    /**
     * 准备编辑任务
     */
    fun prepareEditTask(taskId: String) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null) {
                    when (task.getTaskTypeEnum()) {
                        TaskType.CHECK_IN -> {
                            val checkInTask = checkInTaskRepository.getCheckInTaskById(taskId)
                            if (checkInTask != null) {
                                _taskEditorState.value = TaskEditorState(
                                    isEditingTask = true,
                                    editingTask = task,
                                    newTaskTitle = task.title,
                                    newTaskDescription = task.description ?: "",
                                    newTaskType = task.getTaskTypeEnum(),
                                    newTaskPriority = task.getPriorityEnum(),
                                    newTaskDueDate = task.dueDate,
                                    newTaskGoalId = task.goalId,
                                    newCheckInFrequencyType = checkInTask.frequencyType,
                                    newCheckInFrequencyCount = checkInTask.frequencyCount,
                                    newCheckInFrequencyDays = checkInTask.getFrequencyDaysList(),
                                    newCheckInReminderEnabled = checkInTask.reminderEnabled,
                                    newCheckInReminderTime = checkInTask.reminderTime
                                )
                            }
                        }
                        TaskType.POMODORO -> {
                            val pomodoroTask = pomodoroTaskRepository.getPomodoroTaskById(taskId)
                            if (pomodoroTask != null) {
                                _taskEditorState.value = TaskEditorState(
                                    isEditingTask = true,
                                    editingTask = task,
                                    newTaskTitle = task.title,
                                    newTaskDescription = task.description ?: "",
                                    newTaskType = task.getTaskTypeEnum(),
                                    newTaskPriority = task.getPriorityEnum(),
                                    newTaskDueDate = task.dueDate,
                                    newTaskGoalId = task.goalId,
                                    newPomodoroEstimatedCount = pomodoroTask.estimatedPomodoros,
                                    newPomodoroDuration = pomodoroTask.pomodoroLength,
                                    newPomodoroShortBreak = pomodoroTask.shortBreakLength,
                                    newPomodoroLongBreak = pomodoroTask.longBreakLength,
                                    newPomodoroTagId = pomodoroTask.tagId
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _taskListState.value = _taskListState.value.copy(
                    error = "无法编辑任务: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 取消编辑
     */
    fun cancelEditing() {
        _taskEditorState.value = TaskEditorState()
    }
    
    /**
     * 取消编辑或创建
     */
    @Deprecated("使用 cancelEditing() 代替", ReplaceWith("cancelEditing()"))
    fun cancelEditingOrCreating() {
        cancelEditing()
    }
    
    /**
     * 设置任务编辑器字段值
     */
    fun setTaskEditorField(field: String, value: Any?) {
        val currentState = _taskEditorState.value
        
        _taskEditorState.value = when (field) {
            "title" -> currentState.copy(newTaskTitle = value as String)
            "description" -> currentState.copy(newTaskDescription = value as String)
            "taskType" -> currentState.copy(newTaskType = value as TaskType)
            "priority" -> currentState.copy(newTaskPriority = value as TaskPriority)
            "dueDate" -> currentState.copy(newTaskDueDate = value as Date?)
            "goalId" -> currentState.copy(newTaskGoalId = value as Long?)
            
            // 打卡任务特有字段
            "checkInFrequencyType" -> currentState.copy(newCheckInFrequencyType = value as Int)
            "checkInFrequencyCount" -> currentState.copy(newCheckInFrequencyCount = value as Int)
            "checkInFrequencyDays" -> currentState.copy(newCheckInFrequencyDays = value as List<Int>)
            "checkInReminderEnabled" -> currentState.copy(newCheckInReminderEnabled = value as Boolean)
            "checkInReminderTime" -> currentState.copy(newCheckInReminderTime = value as Date?)
            
            // 番茄钟任务特有字段
            "pomodoroEstimatedCount" -> currentState.copy(newPomodoroEstimatedCount = value as Int)
            "pomodoroDuration" -> currentState.copy(newPomodoroDuration = value as Int)
            "pomodoroShortBreak" -> currentState.copy(newPomodoroShortBreak = value as Int)
            "pomodoroLongBreak" -> currentState.copy(newPomodoroLongBreak = value as Int)
            "pomodoroTagId" -> currentState.copy(newPomodoroTagId = value as String?)
            
            else -> currentState
        }
    }
    
    /**
     * 删除任务
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null) {
                    // 根据任务类型删除子任务
                    when (task.getTaskTypeEnum()) {
                        TaskType.CHECK_IN -> {
                            val checkInTask = checkInTaskRepository.getCheckInTaskById(taskId)
                            if (checkInTask != null) {
                                checkInTaskRepository.deleteCheckInTask(checkInTask)
                            }
                        }
                        TaskType.POMODORO -> {
                            val pomodoroTask = pomodoroTaskRepository.getPomodoroTaskById(taskId)
                            if (pomodoroTask != null) {
                                pomodoroTaskRepository.deletePomodoroTask(pomodoroTask)
                            }
                        }
                    }
                    
                    // 删除任务日志
                    taskLogRepository.deleteLogsByTaskId(taskId)
                    
                    // 删除基础任务
                    taskRepository.deleteTask(task)
                    
                    // 刷新统计数据
                    loadTaskCounts()
                }
            } catch (e: Exception) {
                _taskListState.value = _taskListState.value.copy(
                    error = "删除任务失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 完成任务
     */
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null) {
                    val taskType = task.getTaskTypeEnum()
                    
                    when (taskType) {
                        TaskType.CHECK_IN -> {
                            // 获取打卡任务
                            val checkInTask = checkInTaskRepository.getCheckInTaskById(taskId)
                            if (checkInTask != null) {
                                println("DEBUG: 开始完成打卡任务[${taskId}], 当前打卡次数: ${checkInTask.todayCompletionsCount}/${checkInTask.frequencyCount}")
                                
                                // 更新打卡任务完成状态
                                checkInTaskRepository.updateTaskCompletion(taskId, true)
                                
                                // 创建打卡日志
                                taskLogRepository.createLog(
                                    TaskLogEntity.createCheckInLog(
                                        taskId = taskId
                                    )
                                )
                                
                                // 计算打卡任务进度
                                val progress = checkInTask.getCompletionProgress()
                                println("DEBUG: 打卡任务[${taskId}] 进度: $progress, completedToday: ${checkInTask.completedToday}")
                                
                                // 如果已完成所有打卡
                                if (checkInTask.completedToday || progress >= 1f) {
                                    println("DEBUG: 打卡任务[${taskId}] 已完成所有打卡，标记为完成")
                                    taskRepository.updateTaskCompletion(taskId, true)
                                }
                                
                                // 无论是否完成所有打卡，都更新目标进度
                                println("DEBUG: 每次打卡都更新关联目标进度")
                                updateGoalProgressIfNeeded(task)
                            }
                        }
                        TaskType.POMODORO -> {
                            // 获取番茄钟任务
                            val pomodoroTask = pomodoroTaskRepository.getPomodoroTaskById(taskId)
                            if (pomodoroTask != null) {
                                // 增加已完成番茄数
                                pomodoroTaskRepository.incrementCompletedPomodoros(taskId)
                                
                                // 如果已完成所有番茄钟，标记任务为已完成
                                if (pomodoroTask.completedPomodoros + 1 >= pomodoroTask.estimatedPomodoros) {
                                    taskRepository.updateTaskCompletion(taskId, true)
                                    
                                    // 如果关联了目标，更新目标进度
                                    updateGoalProgressIfNeeded(task)
                                }
                            } else {
                                // 更新基础任务状态
                                taskRepository.updateTaskCompletion(taskId, true)
                            }
                        }
                        else -> {
                            // 其他类型任务
                            // 更新基础任务状态
                            taskRepository.updateTaskCompletion(taskId, true)
                            
                            // 如果关联了目标，更新目标进度
                            updateGoalProgressIfNeeded(task)
                        }
                    }
                }
                
                // 刷新统计数据
                loadTaskCounts()
            } catch (e: Exception) {
                _taskListState.value = _taskListState.value.copy(
                    error = "完成任务失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 如果任务关联了目标，更新目标进度
     * 一天只更新一次进度
     */
    private suspend fun updateGoalProgressIfNeeded(task: TaskEntity) {
        // 添加调试日志，检查任务是否关联了目标
        println("DEBUG: 检查任务[${task.id}] 关联目标: ${task.goalId}")
        
        task.goalId?.let { goalId ->
            try {
                // 获取目标
                val goal = goalRepository.getGoalById(goalId) ?: return
                println("DEBUG: 找到关联目标[${goal.id}], 当前进度: ${goal.progress}")
                
                // 检查今天是否已经更新过这个目标
                val lastUpdateDate = preferencesHelper.getGoalLastUpdateDate(goalId)
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                println("DEBUG: 目标[${goalId}] 上次更新时间: ${lastUpdateDate ?: "从未更新"}")
                
                // 如果今天已经更新过，不再更新
                if (lastUpdateDate != null && isSameDay(lastUpdateDate, today)) {
                    println("DEBUG: 目标[${goalId}] 今天已经更新过，跳过")
                    return
                }
                
                // 根据任务类型和优先级计算进度增量
                val progressIncrement = calculateProgressIncrement(task)
                println("DEBUG: 任务[${task.id}] 计算的进度增量: $progressIncrement")
                
                // 计算新的进度
                val currentProgress = goal.progress
                val newProgress = (currentProgress + progressIncrement).coerceAtMost(1f)
                
                // 更新目标进度
                val updateSuccess = goalRepository.updateGoalProgress(goalId, newProgress)
                
                println("DEBUG: 目标[$goalId]进度更新：$currentProgress -> $newProgress, 更新${if (updateSuccess) "成功" else "失败"}")
                
                // 更新反馈消息
                val progressIncrease = (newProgress - currentProgress) * 100
                val formattedProgress = String.format("%.1f", progressIncrease)
                _taskListState.value = _taskListState.value.copy(
                    successMessage = "已完成任务并更新目标「${goal.title}」进度，增加${formattedProgress}%"
                )
                
                // 如果进度达到100%，标记目标为已完成
                if (newProgress >= 1f) {
                    val completeSuccess = goalRepository.updateGoalCompletionStatus(goalId, true)
                    println("DEBUG: 目标[$goalId]进度达到100%，标记为完成${if (completeSuccess) "成功" else "失败"}")
                    
                    // 更新反馈消息为目标已完成
                    _taskListState.value = _taskListState.value.copy(
                        successMessage = "恭喜！目标「${goal.title}」已完成！"
                    )
                }
                
                // 记录更新日期
                preferencesHelper.saveGoalLastUpdateDate(goalId, today)
                println("DEBUG: 保存目标[$goalId]更新日期: $today")
                
            } catch (e: Exception) {
                println("DEBUG: 更新目标进度失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 根据任务类型和优先级计算进度增量
     * 打卡任务和番茄钟任务有不同的增量计算方式
     */
    private suspend fun calculateProgressIncrement(task: TaskEntity): Float {
        // 基础增量
        var baseIncrement = 0.05f
        
        // 根据任务优先级调整增量
        val priorityMultiplier = when (task.getPriorityEnum()) {
            TaskPriority.HIGH -> 1.5f
            TaskPriority.MEDIUM -> 1.0f
            TaskPriority.LOW -> 0.8f
        }
        
        // 根据任务类型调整增量
        when (task.getTaskTypeEnum()) {
            TaskType.CHECK_IN -> {
                val checkInTask = checkInTaskRepository.getCheckInTaskById(task.id)
                if (checkInTask != null) {
                    // 对于高频率打卡任务，单次增量较小
                    val frequencyMultiplier = when {
                        checkInTask.frequencyCount > 5 -> 0.8f
                        checkInTask.frequencyCount > 2 -> 1.0f
                        else -> 1.2f
                    }
                    
                    baseIncrement *= frequencyMultiplier
                }
            }
            TaskType.POMODORO -> {
                val pomodoroTask = pomodoroTaskRepository.getPomodoroTaskById(task.id)
                if (pomodoroTask != null) {
                    // 番茄钟任务进度与完成的番茄数有关
                    val completedPomodoros = pomodoroTask.completedPomodoros
                    val totalPomodoros = pomodoroTask.estimatedPomodoros
                    
                    // 完成超过估计数量的番茄钟，获得额外奖励
                    val pomodoroMultiplier = if (completedPomodoros >= totalPomodoros) 1.2f else 1.0f
                    
                    baseIncrement *= pomodoroMultiplier
                }
            }
            else -> {
                // 普通任务保持基础增量
            }
        }
        
        // 获取关联的目标，根据目标的截止日期调整进度增量
        task.goalId?.let { goalId ->
            val goal = goalRepository.getGoalById(goalId)
            if (goal != null && !goal.isCompleted) {
                // 计算目标剩余的天数
                val today = Date()
                val remainingTimeMillis = goal.deadline.time - today.time
                val totalTimeMillis = goal.deadline.time - goal.createdAt.time
                
                // 至少保留1天的时间跨度
                val effectiveTotalDays = maxOf(1, totalTimeMillis / (24 * 60 * 60 * 1000))
                
                // 计算时间因子：时间越短，因子越大
                val timeFactor = when {
                    effectiveTotalDays <= 1 -> 4.0f  // 1天内的目标，进度增加很快
                    effectiveTotalDays <= 7 -> 2.0f  // 一周内的目标
                    effectiveTotalDays <= 30 -> 1.0f // 一个月内的目标
                    effectiveTotalDays <= 90 -> 0.5f // 三个月内的目标
                    else -> 0.25f                    // 长期目标
                }
                
                println("DEBUG: 目标[$goalId] 总时间: ${effectiveTotalDays}天, 时间因子: $timeFactor")
                
                // 应用时间因子
                baseIncrement *= timeFactor
                
                // 确保进度不会一次性完成长期目标
                val maxIncrement = minOf(0.25f, 1f / effectiveTotalDays * 5)
                baseIncrement = minOf(baseIncrement, maxIncrement)
                
                println("DEBUG: 目标[$goalId] 进度增量调整后: $baseIncrement")
            }
        }
        
        // 应用优先级乘数
        return baseIncrement * priorityMultiplier
    }
    
    /**
     * 检查两个日期是否为同一天
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * 取消完成任务
     */
    fun uncompleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                // 更新基础任务状态
                taskRepository.updateTaskCompletion(taskId, false)
                
                val task = taskRepository.getTaskById(taskId)
                if (task != null && task.getTaskTypeEnum() == TaskType.CHECK_IN) {
                    // 更新打卡任务状态
                    checkInTaskRepository.updateTaskCompletion(taskId, false)
                }
                
                // 刷新统计数据
                loadTaskCounts()
            } catch (e: Exception) {
                _taskListState.value = _taskListState.value.copy(
                    error = "取消完成任务失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 开始番茄钟
     */
    fun startPomodoro(taskId: String) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null && task.getTaskTypeEnum() == TaskType.POMODORO) {
                    val pomodoroTask = pomodoroTaskRepository.getPomodoroTaskById(taskId)
                    if (pomodoroTask != null) {
                        _pomodoroTimerState.value = PomodoroTimerState(
                            isRunning = true,
                            currentTaskId = taskId,
                            currentPhase = PomodoroPhase.FOCUS,
                            remainingTimeInSeconds = pomodoroTask.pomodoroLength * 60,
                            totalPomodoroCount = pomodoroTask.estimatedPomodoros,
                            pomodoroLength = pomodoroTask.pomodoroLength,
                            shortBreakLength = pomodoroTask.shortBreakLength,
                            longBreakLength = pomodoroTask.longBreakLength,
                            longBreakInterval = pomodoroTask.longBreakInterval
                        )
                        
                        // 这里可以启动计时服务或者使用协程处理计时逻辑
                        // 实际应用中还需要考虑应用在后台运行时的计时处理
                    }
                }
            } catch (e: Exception) {
                _taskListState.value = _taskListState.value.copy(
                    error = "开始番茄钟失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 停止番茄钟
     */
    fun stopPomodoro() {
        viewModelScope.launch {
            try {
                val state = _pomodoroTimerState.value
                if (state.isRunning && state.currentTaskId != null) {
                    // 更新番茄钟状态
                    _pomodoroTimerState.value = PomodoroTimerState()
                    
                    // 记录已完成的番茄钟
                    if (state.currentPomodoroCount > 0) {
                        pomodoroTaskRepository.addFocusTime(
                            taskId = state.currentTaskId,
                            focusMinutes = (state.pomodoroLength * state.currentPomodoroCount),
                            pomodoroCount = state.currentPomodoroCount
                        )
                        
                        // 创建番茄钟日志
                        taskLogRepository.createLog(
                            TaskLogEntity.createPomodoroLog(
                                taskId = state.currentTaskId,
                                focusMinutes = (state.pomodoroLength * state.currentPomodoroCount),
                                pomodoroCount = state.currentPomodoroCount
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _taskListState.value = _taskListState.value.copy(
                    error = "停止番茄钟失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _taskListState.value = _taskListState.value.copy(error = null)
    }
    
    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _taskListState.value = _taskListState.value.copy(successMessage = null)
    }
    
    /**
     * 加载任务统计数据
     */
    private fun loadTaskCounts() {
        viewModelScope.launch {
            try {
                // 获取今日任务数量
                val calendar = Calendar.getInstance()
                // 设置为今天的开始时间
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time
                
                // 设置为今天的结束时间
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfDay = calendar.time
                
                // 获取今日已完成任务数量
                val todayCompletedCount = taskLogRepository.getTodayCompletedTaskCount()
                
                // 直接从当前任务列表中获取今天到期的任务数量
                val activeTasks = taskRepository.getActiveTasks()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
                    .value
                
                // 计算今天已经完成和今天到期的任务总数
                val todayDueTasksCount = activeTasks.count { task ->
                    val dueDate = task.dueDate
                    if (dueDate != null) {
                        val dueCalendar = Calendar.getInstance().apply { time = dueDate }
                        val today = Calendar.getInstance()
                        
                        dueCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        dueCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                    } else false
                }
                
                // 合并今天完成的和今天到期的任务数量
                val todayTasksTotal = todayCompletedCount + todayDueTasksCount
                
                // 获取逾期任务数量
                val overdueTasksCount = taskRepository.getOverdueTasks()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
                    .value.size
                
                _taskListState.value = _taskListState.value.copy(
                    todayTasksCount = todayTasksTotal,
                    overdueTasksCount = overdueTasksCount
                )
            } catch (e: Exception) {
                // 忽略统计加载错误
                println("统计加载错误: ${e.message}")
            }
        }
    }
    
    /**
     * 获取所有标签
     */
    fun getAllTags() = taskTagRepository.getAllTags()
    
    /**
     * 获取任务标签仓库
     */
    fun getTaskTagRepository(): TaskTagRepository {
        return taskTagRepository
    }
    
    /**
     * 获取番茄钟任务仓库
     */
    fun getPomodoroTaskRepository(): PomodoroTaskRepository {
        return pomodoroTaskRepository
    }
    
    /**
     * 获取打卡任务仓库
     */
    fun getCheckInTaskRepository(): CheckInTaskRepository {
        return checkInTaskRepository
    }
    
    /**
     * 获取打卡任务信息
     * @param taskId 任务ID
     * @return 打卡任务实体，可能为null
     */
    suspend fun getCheckInTaskById(taskId: String): CheckInTaskEntity? {
        return checkInTaskRepository.getCheckInTaskById(taskId)
    }
    
    /**
     * 加载打卡任务信息用于界面初始化
     * @param taskId 任务ID
     * @param onResult 结果回调
     */
    fun loadCheckInTaskForInitialization(taskId: String, onResult: (CheckInTaskEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val checkInTask = checkInTaskRepository.getCheckInTaskById(taskId)
                onResult(checkInTask)
            } catch (e: Exception) {
                // 出现异常时返回null
                onResult(null)
            }
        }
    }
    
    /**
     * 创建自定义标签
     * @param tag 标签实体
     * @param onSuccess 创建成功后的回调
     */
    fun createCustomTag(tag: TaskTagEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // 保存标签到数据库
                taskTagRepository.createTag(tag)
                
                // 成功后执行回调
                onSuccess()
            } catch (e: Exception) {
                // 创建失败，可以添加错误处理逻辑
                _taskListState.value = _taskListState.value.copy(
                    error = "创建标签失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 初始化默认标签
     */
    private fun initDefaultTags() {
        viewModelScope.launch {
            taskTagRepository.initDefaultTags()
        }
    }
    
    /**
     * 设置是否显示已完成任务
     */
    fun setShowCompletedTasks(show: Boolean) {
        _showCompletedTasks.value = show
    }
    
    /**
     * 加载任务详情
     */
    fun loadTaskDetail(taskId: String) {
        viewModelScope.launch {
            _taskDetailState.value = TaskDetailState(isLoading = true)
            
            try {
                val task = taskRepository.getTaskById(taskId)
                
                if (task != null) {
                    // 根据任务类型加载额外信息
                    when (task.getTaskTypeEnum()) {
                        TaskType.CHECK_IN -> {
                            val checkInTask = checkInTaskRepository.getCheckInTaskById(taskId)
                            // 可以在这里为任务添加额外的打卡任务信息
                        }
                        TaskType.POMODORO -> {
                            val pomodoroTask = pomodoroTaskRepository.getPomodoroTaskById(taskId)
                            // 可以在这里为任务添加额外的番茄钟任务信息
                        }
                        else -> {
                            // 普通任务不需要额外处理
                        }
                    }
                    
                    _taskDetailState.value = TaskDetailState(task = task, isLoading = false)
                } else {
                    _taskDetailState.value = TaskDetailState(
                        isLoading = false, 
                        error = "任务不存在"
                    )
                }
            } catch (e: Exception) {
                _taskDetailState.value = TaskDetailState(
                    isLoading = false, 
                    error = "加载任务详情失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 从编辑器创建任务
     */
    fun createTaskFromEditor(
        title: String,
        description: String?,
        taskType: Int,
        taskPriority: Int,
        dueDate: Date?,
        goalId: Long?,
        
        // 打卡任务设置
        checkInFrequencyType: Int,
        checkInFrequencyCount: Int,
        checkInReminderEnabled: Boolean,
        checkInReminderTime: Date?,
        
        // 番茄钟任务设置
        pomodoroFocusTime: Int,
        pomodoroShortBreak: Int,
        pomodoroLongBreak: Int,
        pomodoroSessionsBeforeLongBreak: Int,
        pomodoroTagId: String?
    ) {
        viewModelScope.launch {
            try {
                // 创建基础任务实体
                val taskId = UUID.randomUUID().toString()
                val taskEntity = TaskEntity(
                    id = taskId,
                    title = title,
                    description = description,
                    taskType = taskType,
                    priority = taskPriority,
                    dueDate = dueDate,
                    goalId = goalId,
                    tagId = pomodoroTagId,
                    createdAt = Date(),
                    updatedAt = Date()
                )
                
                // 创建基础任务
                taskRepository.createTask(taskEntity)
                
                // 根据任务类型创建相应的子任务
                val taskTypeEnum = TaskType.values().getOrNull(taskType) ?: TaskType.CHECK_IN
                when (taskTypeEnum) {
                    TaskType.CHECK_IN -> {
                        // 创建打卡任务
                        val checkInTask = CheckInTaskEntity(
                            taskId = taskId,
                            frequencyType = checkInFrequencyType,
                            frequencyCount = checkInFrequencyCount,
                            frequencyDaysJson = null, // 默认为null，后续可根据需要添加
                            reminderEnabled = checkInReminderEnabled,
                            reminderTime = checkInReminderTime
                        )
                        checkInTaskRepository.createCheckInTask(checkInTask)
                    }
                    TaskType.POMODORO -> {
                        // 创建番茄钟任务
                        val pomodoroTask = PomodoroTaskEntity(
                            taskId = taskId,
                            estimatedPomodoros = pomodoroSessionsBeforeLongBreak,
                            pomodoroLength = pomodoroFocusTime,
                            shortBreakLength = pomodoroShortBreak,
                            longBreakLength = pomodoroLongBreak,
                            longBreakInterval = 4, // 默认值
                            tagId = pomodoroTagId
                        )
                        pomodoroTaskRepository.createPomodoroTask(pomodoroTask)
                    }
                }
                
                // 重置编辑器状态
                _taskEditorState.value = TaskEditorState()
                
            } catch (e: Exception) {
                _taskListState.value = _taskListState.value.copy(
                    error = "创建任务失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 从编辑器更新任务
     */
    fun updateTaskFromEditor(
        taskId: String,
        title: String,
        description: String?,
        taskType: Int,
        taskPriority: Int,
        dueDate: Date?,
        goalId: Long?,
        
        // 打卡任务设置
        checkInFrequencyType: Int,
        checkInFrequencyCount: Int,
        checkInReminderEnabled: Boolean,
        checkInReminderTime: Date?,
        
        // 番茄钟任务设置
        pomodoroFocusTime: Int,
        pomodoroShortBreak: Int,
        pomodoroLongBreak: Int,
        pomodoroSessionsBeforeLongBreak: Int,
        pomodoroTagId: String?
    ) {
        viewModelScope.launch {
            try {
                val task = _taskEditorState.value.editingTask
                if (task != null) {
                    // 更新基础任务实体
                    val updatedTask = task.copy(
                        title = title,
                        description = description,
                        taskType = taskType,
                        priority = taskPriority,
                        dueDate = dueDate,
                        goalId = goalId,
                        tagId = pomodoroTagId,
                        updatedAt = Date()
                    )
                    
                    // 更新基础任务
                    taskRepository.updateTask(updatedTask)
                    
                    // 根据任务类型更新相应的子任务
                    val taskTypeEnum = TaskType.values().getOrNull(taskType) ?: task.getTaskTypeEnum()
                    when (taskTypeEnum) {
                        TaskType.CHECK_IN -> {
                            // 获取原始打卡任务
                            val originalCheckInTask = checkInTaskRepository.getCheckInTaskById(task.id)
                            if (originalCheckInTask != null) {
                                // 更新打卡任务
                                val updatedCheckInTask = originalCheckInTask.copy(
                                    frequencyType = checkInFrequencyType,
                                    frequencyCount = checkInFrequencyCount,
                                    frequencyDaysJson = null, // 默认为null，后续可根据需要添加
                                    reminderEnabled = checkInReminderEnabled,
                                    reminderTime = checkInReminderTime
                                )
                                checkInTaskRepository.updateCheckInTask(updatedCheckInTask)
                            } else {
                                // 如果是从其他类型转换为打卡任务，创建新的打卡任务
                                val newCheckInTask = CheckInTaskEntity(
                                    taskId = task.id,
                                    frequencyType = checkInFrequencyType,
                                    frequencyCount = checkInFrequencyCount,
                                    frequencyDaysJson = null,
                                    reminderEnabled = checkInReminderEnabled,
                                    reminderTime = checkInReminderTime
                                )
                                checkInTaskRepository.createCheckInTask(newCheckInTask)
                            }
                        }
                        TaskType.POMODORO -> {
                            // 获取原始番茄钟任务
                            val originalPomodoroTask = pomodoroTaskRepository.getPomodoroTaskById(task.id)
                            if (originalPomodoroTask != null) {
                                // 更新番茄钟任务
                                val updatedPomodoroTask = originalPomodoroTask.copy(
                                    estimatedPomodoros = pomodoroSessionsBeforeLongBreak,
                                    pomodoroLength = pomodoroFocusTime,
                                    shortBreakLength = pomodoroShortBreak,
                                    longBreakLength = pomodoroLongBreak,
                                    tagId = pomodoroTagId
                                )
                                pomodoroTaskRepository.updatePomodoroTask(updatedPomodoroTask)
                            } else {
                                // 如果是从其他类型转换为番茄钟任务，创建新的番茄钟任务
                                val newPomodoroTask = PomodoroTaskEntity(
                                    taskId = task.id,
                                    estimatedPomodoros = pomodoroSessionsBeforeLongBreak,
                                    pomodoroLength = pomodoroFocusTime,
                                    shortBreakLength = pomodoroShortBreak,
                                    longBreakLength = pomodoroLongBreak,
                                    longBreakInterval = 4, // 默认值
                                    tagId = pomodoroTagId
                                )
                                pomodoroTaskRepository.createPomodoroTask(newPomodoroTask)
                            }
                        }
                    }
                    
                    // 重置编辑器状态
                    _taskEditorState.value = TaskEditorState()
                }
            } catch (e: Exception) {
                _taskListState.value = _taskListState.value.copy(
                    error = "更新任务失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 获取所有目标
     * @return 目标列表Flow
     */
    fun getAllGoals(): Flow<List<Goal>> {
        return goalRepository.getAllGoals()
    }
    
    /**
     * 获取目标仓库
     * @return 目标仓库
     */
    fun getGoalRepository(): GoalRepository {
        return goalRepository
    }
} 