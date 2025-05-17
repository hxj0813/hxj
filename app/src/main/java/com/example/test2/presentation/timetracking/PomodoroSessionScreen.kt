package com.example.test2.presentation.timetracking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Note
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
import com.example.test2.data.local.entity.TaskEntity
import com.example.test2.data.model.Task
import com.example.test2.data.model.TaskType
import com.example.test2.presentation.timetracking.components.PomodoroTimerView

/**
 * 番茄钟会话屏幕
 * 用于显示正在进行的番茄钟计时
 * 
 * @param taskId 任务ID
 * @param onNavigateBack 返回回调
 * @param onFinish 会话结束回调
 * @param viewModel 视图模型
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroSessionScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit,
    viewModel: TimeTrackingViewModel = hiltViewModel()
) {
    // 初始化会话
    LaunchedEffect(taskId) {
        viewModel.startPomodoroSession(taskId)
    }
    
    val state by viewModel.state.collectAsState()
    val task = state.currentTask
    
    // 颜色定义
    val focusGradient = listOf(Color(0xFFFF7F7F), Color(0xFFFF5252))
    val breakGradient = listOf(Color(0xFF98FF98), Color(0xFF66BB6A))
    val currentGradient = if (state.isBreakTime) breakGradient else focusGradient
    val backgroundColor = if (state.isBreakTime) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    
    // 时间计算
    val minutes = state.remainingTimeInSeconds / 60
    val seconds = state.remainingTimeInSeconds % 60
    val progress = 1f - (state.remainingTimeInSeconds.toFloat() / state.totalTimeInSeconds.toFloat())
    
    // 错误处理
    if (state.error != null) {
        LaunchedEffect(state.error) {
            // 在实际应用中，这里应该显示错误提示
            // 暂时只是清除错误
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (state.isBreakTime) "休息时间" else "专注时间",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isRunning) {
                            viewModel.pauseTimer()
                        }
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "白噪音",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = "笔记",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        // 背景渐变
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = currentGradient,
                        startY = 0f,
                        endY = 400f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 任务信息卡片
                AnimatedVisibility(
                    visible = task != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    task?.let { currentTask ->
                        TaskInfoCard(currentTask)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 加载状态
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    // 番茄钟计时器
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        PomodoroTimerView(
                            minutes = minutes,
                            seconds = seconds,
                            totalTimeInSeconds = state.totalTimeInSeconds,
                            progress = progress,
                            isRunning = state.isRunning,
                            isPaused = state.isPaused,
                            isBreak = state.isBreakTime,
                            currentSession = state.currentSession,
                            totalSessions = state.totalSessions,
                            onStart = { viewModel.startTimer() },
                            onPause = { viewModel.pauseTimer() },
                            onStop = { 
                                viewModel.stopTimer()
                                onFinish()
                            },
                            onSkipBreak = { viewModel.skipBreak() },
                            onFinishBreak = { viewModel.finishBreak() }
                        )
                    }
                }
                
                // 完成计时显示对话框
                if (state.sessionCompleted) {
                    SessionCompletedDialog(
                        onDismiss = { 
                            // 继续到休息状态
                            viewModel.acknowledgeSessionCompleted()
                            viewModel.startBreak()
                        },
                        onSaveAndFinish = {
                            viewModel.saveSession()
                            onFinish()
                        }
                    )
                }
            }
        }
    }
}

/**
 * 番茄钟设置数据类，用于在TaskInfoCard中显示
 */
private data class PomodoroStats(
    val todayCompletedSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val totalCompletedSessions: Int = 0
)

/**
 * 任务信息卡片
 */
@Composable
private fun TaskInfoCard(task: Task) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            
            if (!task.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // 显示统计数据（使用Task的pomodoroSettings或默认值）
            val stats = when {
                task.pomodoroSettings != null -> PomodoroStats(
                    todayCompletedSessions = task.pomodoroSettings.todayCompletedSessions,
                    totalFocusMinutes = task.pomodoroSettings.totalFocusMinutes,
                    totalCompletedSessions = task.pomodoroSettings.totalCompletedSessions
                )
                else -> PomodoroStats() // 默认值
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "今日完成",
                    value = "${stats.todayCompletedSessions}次"
                )
                
                StatItem(
                    label = "总专注时间",
                    value = "${stats.totalFocusMinutes}分钟"
                )
                
                StatItem(
                    label = "总计次数",
                    value = "${stats.totalCompletedSessions}次"
                )
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF4A90E2)
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

/**
 * 会话完成对话框
 */
@Composable
private fun SessionCompletedDialog(
    onDismiss: () -> Unit,
    onSaveAndFinish: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "专注会话完成！",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "恭喜你完成了这次专注！",
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSaveAndFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("完成")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF98FF98)
                )
            ) {
                Text("继续休息")
            }
        }
    )
}

/**
 * 获取TaskEntity对应的Task，适配两种类型
 */
private fun Task.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = id.toString(),
        title = title,
        description = description,
        taskType = type.ordinal,
        priority = priority.ordinal,
        isCompleted = isCompleted,
        dueDate = dueDate,
        color = when (type) {
            TaskType.CHECK_IN -> 0xFFFF9800.toInt()
            TaskType.POMODORO -> 0xFF4CAF50.toInt()
        }
    )
} 