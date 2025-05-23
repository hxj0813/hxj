package com.example.test2.presentation.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test2.data.local.entity.TaskEntity as Task
import com.example.test2.data.local.entity.TaskType
import com.example.test2.presentation.tasks.components.TaskCard
import com.example.test2.presentation.tasks.components.TaskDialog
import com.example.test2.presentation.tasks.viewmodel.TaskManagerViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 任务管理屏幕
 * 全新设计的现代任务管理界面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToStatistics: () -> Unit,
    initialGoalId: Long? = null,
    onTaskCreated: () -> Unit = {},
    viewModel: TaskManagerViewModel = hiltViewModel()
) {
    val state by viewModel.combinedTaskState.collectAsState()
    val editorState by viewModel.taskEditorState.collectAsState()
    val lazyListState = rememberLazyListState()
    
    // 如果有初始目标ID，自动打开任务创建对话框
    LaunchedEffect(initialGoalId) {
        if (initialGoalId != null && initialGoalId > 0) {
            // 准备创建任务
            viewModel.prepareCreateTask()
            
            // 设置关联目标
            viewModel.setTaskEditorField("goalId", initialGoalId)
        }
    }
    
    // 任务创建完成或取消后，清除initialGoalId的效果
    DisposableEffect(editorState.isShowingTaskForm) {
        // 当表单关闭时，重置导航状态
        onDispose {
            if (!editorState.isShowingTaskForm && initialGoalId != null && initialGoalId > 0) {
                // 通知任务创建过程已完成
                onTaskCreated()
            }
        }
    }
    
    // 颜色定义
    val gradientColors = listOf(Color(0xFF4A90E2), Color(0xFF357ABD))
    val accentColor = Color(0xFF4A90E2)
    val backgroundColor = Color(0xFFF5F5F5)
    val cardColor = Color.White
    
    // 日期格式化
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA) }
    val todayDate = remember { dateFormatter.format(Date()) }
    
    Scaffold(
        topBar = {
            // 顶部应用栏
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = gradientColors
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 标题和日期行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "我的任务",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = todayDate,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 搜索框
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("搜索任务...") },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "搜索") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                                focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedBorderColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 任务类型过滤器
                        FilterTabs(
                            selectedFilter = state.filterType,
                            onFilterSelected = { viewModel.setFilterType(it) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.prepareCreateTask() },
                containerColor = accentColor,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加新任务"
                )
            }
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 主内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // 已完成/未完成任务切换
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.showCompletedTasks) "已完成任务" else "待办任务",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    
                    Switch(
                        checked = state.showCompletedTasks,
                        onCheckedChange = { viewModel.toggleShowCompletedTasks() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 任务列表
                if (state.isLoading) {
                    // 加载中状态
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = accentColor)
                    }
                } else if ((state.showCompletedTasks && state.completedTasks.isEmpty()) ||
                    (!state.showCompletedTasks && state.activeTasks.isEmpty())) {
                    // 空状态
                    EmptyTasksView(
                        isCompleted = state.showCompletedTasks,
                        filterType = state.filterType
                    )
                } else {
                    // 任务列表
                    val tasks = if (state.showCompletedTasks) state.completedTasks else state.activeTasks
                    
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = tasks,
                            key = { it.id }
                        ) { task ->
                            TaskCard(
                                task = task,
                                onEditClick = { viewModel.prepareEditTask(task.id) },
                                onDelete = { viewModel.deleteTask(task.id) },
                                onToggleCompletion = { 
                                    if (task.isCompleted) {
                                        viewModel.uncompleteTask(task.id)
                                    } else {
                                        viewModel.completeTask(task.id)
                                    }
                                },
                                onCheckinClick = if (task.getTaskTypeEnum() == TaskType.CHECK_IN) {
                                    { viewModel.completeTask(task.id) }
                                } else null,
                                onStartClick = if (task.getTaskTypeEnum() == TaskType.POMODORO) {
                                    { 
                                        // 导航到番茄钟会话界面
                                        onNavigateToDetail(task.id)
                                    }
                                } else null,
                                onCardClick = { onNavigateToDetail(task.id) },
                                taskTagRepository = viewModel.getTaskTagRepository(),
                                modifier = Modifier.animateItemPlacement(
                                    animationSpec = tween(300)
                                )
                            )
                        }
                    }
                }
            }

            // 显示错误对话框
            if (state.error != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("错误") },
                    text = { Text(text = state.error ?: "") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("确定")
                        }
                    }
                )
            }
            
            // 显示成功消息Snackbar
            state.successMessage?.let { message ->
                val snackbarHostState = remember { SnackbarHostState() }
                
                LaunchedEffect(message) {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                    // 显示后清除消息
                    viewModel.clearSuccessMessage()
                }
                
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    snackbar = { data ->
                        Snackbar(
                            modifier = Modifier
                                .padding(bottom = 80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White,
                            action = {
                                TextButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                                    Text("知道了", color = Color.White)
                                }
                            }
                        ) {
                            Text(data.visuals.message)
                        }
                    }
                )
            }
            
            // 显示任务表单对话框
            if (editorState.isShowingTaskForm) {
                TaskDialog(
                    taskEntity = if (editorState.isEditingTask) editorState.editingTask else null,
                    editorState = editorState,
                    onDismiss = { viewModel.cancelEditing() },
                    onSave = { taskData ->
                        if (editorState.isEditingTask) {
                            // 更新任务
                            viewModel.updateTaskFromEditor(
                                taskId = editorState.editingTask?.id ?: "",
                                title = taskData.title,
                                description = taskData.description,
                                taskType = taskData.taskType,
                                taskPriority = taskData.taskPriority,
                                dueDate = taskData.dueDate,
                                goalId = taskData.goalId,
                                
                                // 打卡任务设置
                                checkInFrequencyType = taskData.checkInFrequencyType,
                                checkInFrequencyCount = taskData.checkInFrequencyCount,
                                checkInReminderEnabled = taskData.checkInReminderEnabled,
                                checkInReminderTime = taskData.checkInReminderTime,
                                
                                // 番茄钟任务设置
                                pomodoroFocusTime = taskData.pomodoroFocusTime,
                                pomodoroShortBreak = taskData.pomodoroShortBreak,
                                pomodoroLongBreak = taskData.pomodoroLongBreak,
                                pomodoroSessionsBeforeLongBreak = taskData.pomodoroSessionsBeforeLongBreak,
                                pomodoroTagId = taskData.pomodoroTagId
                            )
                        } else {
                            // 创建新任务
                            viewModel.createTaskFromEditor(
                                title = taskData.title,
                                description = taskData.description,
                                taskType = taskData.taskType,
                                taskPriority = taskData.taskPriority,
                                dueDate = taskData.dueDate,
                                goalId = taskData.goalId,
                                
                                // 打卡任务设置
                                checkInFrequencyType = taskData.checkInFrequencyType,
                                checkInFrequencyCount = taskData.checkInFrequencyCount,
                                checkInReminderEnabled = taskData.checkInReminderEnabled,
                                checkInReminderTime = taskData.checkInReminderTime,
                                
                                // 番茄钟任务设置
                                pomodoroFocusTime = taskData.pomodoroFocusTime,
                                pomodoroShortBreak = taskData.pomodoroShortBreak,
                                pomodoroLongBreak = taskData.pomodoroLongBreak,
                                pomodoroSessionsBeforeLongBreak = taskData.pomodoroSessionsBeforeLongBreak,
                                pomodoroTagId = taskData.pomodoroTagId
                            )
                        }
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

/**
 * 任务类型过滤器选项卡
 */
@Composable
fun FilterTabs(
    selectedFilter: TaskType?,
    onFilterSelected: (TaskType?) -> Unit
) {
    val filters = listOf(
        Triple(null, "全部", Icons.Default.Sort),
        Triple(TaskType.CHECK_IN, "打卡", Icons.Default.CheckCircle),
        Triple(TaskType.POMODORO, "番茄钟", Icons.Default.Timelapse)
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (filter, label, icon) ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(label) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.White,
                    selectedLabelColor = Color(0xFF4A90E2),
                    selectedLeadingIconColor = Color(0xFF4A90E2)
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 空任务视图
 */
@Composable
fun EmptyTasksView(
    isCompleted: Boolean,
    filterType: TaskType?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val message = when {
            isCompleted -> "暂无已完成的任务"
            filterType == TaskType.CHECK_IN -> "没有打卡任务"
            filterType == TaskType.POMODORO -> "没有番茄钟任务"
            else -> "暂无待办任务"
        }
        
        val detailMessage = when {
            isCompleted -> "完成任务后将显示在这里"
            filterType == TaskType.CHECK_IN -> "点击+创建一个新的打卡任务"
            filterType == TaskType.POMODORO -> "点击+创建一个新的番茄钟任务"
            else -> "点击+创建您的第一个任务"
        }
        
        // 图标或插图（可替换为实际资源）
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = Color(0xFFE0E0E0)
        ) {
            Icon(
                imageVector = when (filterType) {
                    TaskType.POMODORO -> Icons.Default.Timelapse
                    TaskType.CHECK_IN -> Icons.Default.CheckCircle
                    else -> Icons.Default.CalendarMonth
                },
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier
                    .padding(24.dp)
                    .size(72.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = detailMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
} 