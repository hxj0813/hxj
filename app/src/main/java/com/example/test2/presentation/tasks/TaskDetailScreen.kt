package com.example.test2.presentation.tasks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test2.data.local.entity.TaskEntity as Task
import com.example.test2.data.local.entity.TaskPriority
import com.example.test2.data.local.entity.TaskType
import com.example.test2.data.local.entity.TaskTagEntity
import com.example.test2.data.local.entity.PomodoroTaskEntity
import com.example.test2.presentation.components.SuccessDialog
import com.example.test2.presentation.tasks.viewmodel.TaskManagerViewModel
import com.example.test2.util.DateTimeUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * 任务详情屏幕
 * 
 * @param taskId 任务ID
 * @param onNavigateBack 返回回调
 * @param onEdit 编辑任务回调
 * @param onStart 开始任务回调（番茄钟任务）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onEdit: (Task) -> Unit,
    onStart: (String) -> Unit,
    viewModel: TaskManagerViewModel = hiltViewModel()
) {
    // 加载任务详情
    LaunchedEffect(taskId) {
        viewModel.loadTaskDetail(taskId)
    }
    
    val taskDetailState by viewModel.taskDetailState.collectAsState()
    val task = taskDetailState.task
    val scrollState = rememberScrollState()
    
    // 添加状态
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 添加成功对话框状态
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    // 颜色定义
    val taskTypeColors = mapOf(
        TaskType.CHECK_IN to Color(0xFF4CAF50),   // 绿色
        TaskType.POMODORO to Color(0xFFFF7F7F)    // 珊瑚色
    )
    
    val currentColor = taskTypeColors[task?.getTaskTypeEnum()] ?: Color(0xFF4A90E2)
    val gradientColors = listOf(currentColor, currentColor.copy(alpha = 0.7f))
    
    // 显示成功对话框
    if (showSuccessDialog) {
        SuccessDialog(
            message = successMessage,
            onDismiss = {
                showSuccessDialog = false
                onNavigateBack()
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 编辑按钮
                    IconButton(
                        onClick = { task?.let { onEdit(it) } }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "编辑"
                        )
                    }
                    
                    // 更多选项菜单
                    var expanded by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多选项"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("删除任务") },
                            onClick = {
                                expanded = false
                                task?.let { 
                                    viewModel.deleteTask(it.id)
                                    successMessage = "任务已删除"
                                    showSuccessDialog = true
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFE57373)
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        task?.let { currentTask ->
            // 背景颜色渐变
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = gradientColors,
                            startY = 0f,
                            endY = 300f
                        )
                    )
            ) {
                // 任务详情内容
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                ) {
                    // 标题和描述区域
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = currentTask.title,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (!currentTask.description.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentTask.description,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                    }
                    
                    // 任务内容区域（卡片形式）
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // 基本信息
                            InfoSection(
                                title = "基本信息",
                                content = {
                                    // 任务类型
                                    InfoItem(
                                        icon = when (currentTask.getTaskTypeEnum()) {
                                            TaskType.CHECK_IN -> Icons.Outlined.CheckCircle
                                            TaskType.POMODORO -> Icons.Outlined.Timelapse
                                        },
                                        label = "任务类型",
                                        value = when (currentTask.getTaskTypeEnum()) {
                                            TaskType.CHECK_IN -> "打卡任务"
                                            TaskType.POMODORO -> "番茄钟任务"
                                        },
                                        color = currentColor
                                    )
                                    
                                    // 创建时间
                                    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
                                    InfoItem(
                                        icon = Icons.Default.DateRange,
                                        label = "创建时间",
                                        value = dateFormat.format(currentTask.createdAt),
                                        color = Color.Gray
                                    )
                                         
                                    // 截止日期
                                    if (currentTask.dueDate != null) {
                                        InfoItem(
                                            icon = Icons.Outlined.CalendarToday,
                                            label = "截止日期",
                                            value = dateFormat.format(currentTask.dueDate),
                                            color = if (currentTask.isOverdue()) Color(0xFFE57373) else Color.Gray
                                        )
                                    }
                                    
                                    // 优先级
                                    val priorityColor = when (currentTask.getPriorityEnum()) {
                                        TaskPriority.LOW -> Color(0xFF8BC34A)
                                        TaskPriority.MEDIUM -> Color(0xFFFFC107)
                                        TaskPriority.HIGH -> Color(0xFFFF5722)
                                    }
                                    
                                    InfoItem(
                                        icon = Icons.Default.Flag,
                                        label = "优先级",
                                        value = when (currentTask.getPriorityEnum()) {
                                            TaskPriority.LOW -> "低"
                                            TaskPriority.MEDIUM -> "中"
                                            TaskPriority.HIGH -> "高"
                                        },
                                        color = priorityColor
                                    )
                                    
                                    // 标签信息
                                    if (currentTask.tagId != null) {
                                        // 获取标签信息
                                        var tagInfo by remember { mutableStateOf<TaskTagEntity?>(null) }
                                        
                                        // 获取标签详情
                                        LaunchedEffect(currentTask.tagId) {
                                            tagInfo = viewModel.getTaskTagRepository().getTagById(currentTask.tagId)
                                        }
                                        
                                        // 如果获取到标签信息，则显示
                                        tagInfo?.let { tag ->
                                            InfoItem(
                                                icon = Icons.Default.Label,
                                                label = "标签",
                                                value = tag.name,
                                                color = Color(tag.color)
                                            )
                                        }
                                    }
                                    
                                    // 目标关联
                                    if (currentTask.goalId != null) {
                                        InfoItem(
                                            icon = Icons.Default.Star,
                                            label = "关联目标",
                                            value = "目标 ID: ${currentTask.goalId}",
                                            color = Color(0xFF9C27B0)
                                        )
                                    }
                                }
                            )
                            
                            // 任务类型特有信息区域
                            when (currentTask.getTaskTypeEnum()) {
                                TaskType.CHECK_IN -> {
                                    // 获取打卡任务信息
                                    val checkInTaskId = currentTask.id
                                    // 这里我们应该使用viewModel获取CheckInTaskEntity
                                    // 这里简化处理，使用基础数据显示
                                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                                    
                                    InfoSection(
                                        title = "打卡统计",
                                        content = {
                                            // 简化的打卡任务信息
                                            InfoItem(
                                                icon = Icons.Default.Repeat,
                                                label = "打卡频率",
                                                value = "每日打卡",
                                                color = currentColor
                                            )
                                            
                                            InfoItem(
                                                icon = Icons.Default.Whatshot,
                                                label = "当前连续打卡",
                                                value = "0 天",
                                                color = Color(0xFFFF9800)
                                            )
                                            
                                            InfoItem(
                                                icon = Icons.Default.EmojiEvents,
                                                label = "最佳连续打卡",
                                                value = "0 天",
                                                color = Color(0xFFFFD700)
                                            )
                                            
                                            InfoItem(
                                                icon = Icons.Default.CheckCircle,
                                                label = "总打卡次数",
                                                value = "0 次",
                                                color = Color(0xFF4CAF50)
                                            )
                                            
                                            // 当前进度条
                                            ProgressSection(
                                                title = "今日进度: 0/1",
                                                progress = 0f,
                                                color = currentColor
                                            )
                                        }
                                    )
                                }
                                TaskType.POMODORO -> {
                                    // 获取番茄钟任务信息
                                    val pomodoroTaskId = currentTask.id
                                    // 使用viewModel获取PomodoroTaskEntity
                                    var pomodoroTask by remember { mutableStateOf<PomodoroTaskEntity?>(null) }
                                    
                                    // 获取番茄钟任务详情
                                    LaunchedEffect(pomodoroTaskId) {
                                        pomodoroTask = viewModel.getPomodoroTaskRepository().getPomodoroTaskById(pomodoroTaskId)
                                    }
                                    
                                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                                    
                                    InfoSection(
                                        title = "番茄钟设置",
                                        content = {
                                            // 番茄钟时长
                                            InfoItem(
                                                icon = Icons.Outlined.AccessTime,
                                                label = "番茄钟时长",
                                                value = "${pomodoroTask?.pomodoroLength ?: 25} 分钟",
                                                color = currentColor
                                            )
                                            
                                            // 休息设置
                                            InfoItem(
                                                icon = Icons.Default.BreakfastDining,
                                                label = "休息设置",
                                                value = "短休息 ${pomodoroTask?.shortBreakLength ?: 5} 分钟，长休息 ${pomodoroTask?.longBreakLength ?: 15} 分钟",
                                                color = Color(0xFF98FF98)
                                            )
                                            
                                            // 长休息间隔
                                            InfoItem(
                                                icon = Icons.Default.SwapHoriz,
                                                label = "长休息间隔",
                                                value = "每 ${pomodoroTask?.longBreakInterval ?: 4} 个番茄钟后",
                                                color = Color(0xFF98FF98)
                                            )
                                            
                                            // 总专注时间
                                            InfoItem(
                                                icon = Icons.Default.Timer,
                                                label = "总专注时间",
                                                value = "${pomodoroTask?.totalFocusTime ?: 0} 分钟",
                                                color = Color(0xFFFF7F7F)
                                            )
                                            
                                            // 总完成次数
                                            InfoItem(
                                                icon = Icons.Default.DoneAll,
                                                label = "总完成次数",
                                                value = "${pomodoroTask?.completedPomodoros ?: 0} 次",
                                                color = Color(0xFFFF7F7F)
                                            )
                                            
                                            // 今日完成次数 - 这个需要额外逻辑计算，简化处理
                                            InfoItem(
                                                icon = Icons.Default.Today,
                                                label = "今日完成次数",
                                                value = pomodoroTask?.let { task ->
                                                    if (task.lastSessionDate != null && DateTimeUtil.isToday(task.lastSessionDate)) {
                                                        "${task.completedPomodoros} 次"
                                                    } else {
                                                        "0 次"
                                                    }
                                                } ?: "0 次",
                                                color = Color(0xFFFF7F7F)
                                            )
                                        }
                                    )
                                }
                                else -> {
                                    // 普通任务无特殊信息
                                }
                            }
                            
                            // 操作按钮区域
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (currentTask.getTaskTypeEnum()) {
                                    TaskType.CHECK_IN -> {
                                        Button(
                                            onClick = { 
                                                viewModel.completeTask(currentTask.id)
                                                successMessage = "打卡成功！继续保持！"
                                                showSuccessDialog = true
                                            },
                                            enabled = !currentTask.isCompleted,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50),
                                                disabledContainerColor = Color.LightGray
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = if (currentTask.isCompleted) "已完成打卡" else "立即打卡",
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                    TaskType.POMODORO -> {
                                        Button(
                                            onClick = { onStart(currentTask.id) },
                                            enabled = !currentTask.isCompleted,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFF7F7F),
                                                disabledContainerColor = Color.LightGray
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = if (currentTask.isCompleted) "已完成番茄钟" else "开始专注",
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                    else -> {
                                        Button(
                                            onClick = { 
                                                viewModel.completeTask(currentTask.id)
                                                successMessage = "任务已完成！"
                                                showSuccessDialog = true
                                            },
                                            enabled = !currentTask.isCompleted,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4A90E2),
                                                disabledContainerColor = Color.LightGray
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = if (currentTask.isCompleted) "已完成任务" else "标记为已完成",
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } ?: run {
            // 加载中或任务不存在
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4A90E2))
            }
        }
    }
}

/**
 * 信息分区
 */
@Composable
private fun InfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.DarkGray
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        content()
    }
}

/**
 * 信息项
 */
@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 文本内容
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }
    }
}

/**
 * 进度条部分
 */
@Composable
private fun ProgressSection(
    title: String,
    progress: Float,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 进度条
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
    }
} 