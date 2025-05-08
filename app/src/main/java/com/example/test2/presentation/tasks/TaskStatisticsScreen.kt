package com.example.test2.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Insights
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
import com.example.test2.data.local.entity.TaskPriority
import com.example.test2.data.local.entity.TaskType
import com.example.test2.presentation.tasks.components.TaskStatisticsView
import com.example.test2.presentation.tasks.viewmodel.TaskManagerViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 任务统计屏幕
 * 
 * @param onNavigateBack 返回回调
 * @param viewModel 视图模型
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskStatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaskManagerViewModel = hiltViewModel()
) {
    // 收集任务列表状态
    val taskListState by viewModel.combinedTaskState.collectAsState()
    val scrollState = rememberScrollState()
    
    // 获取所有任务（活跃任务和已完成任务）
    val allTasks = taskListState.activeTasks + taskListState.completedTasks
    
    // 计算统计数据
    val totalTasks = allTasks.size
    val completedTasks = taskListState.completedTasks.size
    val overdueTasks = taskListState.activeTasks.count { task -> task.isOverdue() }
    
    // 获取按类型分类的任务数量
    val tasksByType = allTasks.groupBy { task -> task.getTaskTypeEnum() }
        .mapValues { entry -> entry.value.size }
    
    // 获取按优先级分类的任务数量
    val tasksByPriority = allTasks.groupBy { task -> task.getPriorityEnum() }
        .mapValues { entry -> entry.value.size }
    
    // 计算最近完成率（过去7天）
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, -7)
    val oneWeekAgo = calendar.time
    
    val recentTasks = allTasks.filter { task -> task.createdAt.after(oneWeekAgo) }
    val recentCompletedTasks = recentTasks.count { task -> task.isCompleted }
    val recentCompletionRate = if (recentTasks.isNotEmpty()) {
        recentCompletedTasks.toFloat() / recentTasks.size.toFloat()
    } else {
        0f
    }
    
    // 创建每周完成数量的映射
    val weeklyCompletionCounts = mutableMapOf<String, Int>()
    val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
    
    // 初始化过去7天的数据
    for (i in 6 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -i)
        val dateKey = dateFormat.format(cal.time)
        weeklyCompletionCounts[dateKey] = 0
    }
    
    // 填充实际数据
    taskListState.completedTasks.forEach { task ->
        val dateKey = dateFormat.format(task.updatedAt)
        if (weeklyCompletionCounts.containsKey(dateKey)) {
            weeklyCompletionCounts[dateKey] = weeklyCompletionCounts[dateKey]!! + 1
        }
    }
    
    // 渐变背景色
    val gradientColors = listOf(Color(0xFF4A90E2), Color(0xFF357ABD))
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务统计") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = 0f,
                        endY = 400f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // 标题和描述
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "您的任务完成情况",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "查看您的任务执行统计和进展",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
                
                // 任务概览卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 总任务数
                        StatisticItem(
                            icon = Icons.Outlined.Insights,
                            value = totalTasks.toString(),
                            label = "总任务数",
                            iconColor = Color(0xFF4A90E2)
                        )
                        
                        // 已完成任务
                        StatisticItem(
                            icon = Icons.Outlined.CheckCircle,
                            value = completedTasks.toString(),
                            label = "已完成",
                            iconColor = Color(0xFF4CAF50)
                        )
                        
                        // 逾期任务
                        StatisticItem(
                            icon = Icons.Outlined.CalendarMonth,
                            value = overdueTasks.toString(),
                            label = "已逾期",
                            iconColor = Color(0xFFE57373)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 任务统计视图
                TaskStatisticsView(
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    overdueTasks = overdueTasks,
                    tasksByType = tasksByType,
                    tasksByPriority = tasksByPriority,
                    recentCompletionRate = recentCompletionRate,
                    weeklyCompletionCounts = weeklyCompletionCounts
                )
                
                // 完成习惯提示
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "任务提示",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF4CAF50)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "坚持完成任务是提高工作效率的关键。尝试每天在固定时间处理任务，养成良好的工作习惯。",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (completedTasks > 0) {
                            Text(
                                text = "您已经完成了 $completedTasks 个任务，继续保持！",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4CAF50)
                            )
                        } else {
                            Text(
                                text = "从完成一个简单的任务开始吧！",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
                
                // 底部间距
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 值
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.DarkGray
        )
        
        // 标签
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
} 