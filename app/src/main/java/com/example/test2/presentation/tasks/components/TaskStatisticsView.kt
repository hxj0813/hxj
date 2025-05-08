package com.example.test2.presentation.tasks.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.local.entity.TaskPriority
import com.example.test2.data.local.entity.TaskType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/**
 * 任务统计视图
 * 
 * @param totalTasks 总任务数
 * @param completedTasks 已完成任务数
 * @param overdueTasks 逾期任务数
 * @param tasksByType 按类型分类的任务数量
 * @param tasksByPriority 按优先级分类的任务数量
 * @param recentCompletionRate 最近完成率
 * @param weeklyCompletionCounts 每周完成数量
 * @param modifier Modifier修饰符
 */
@Composable
fun TaskStatisticsView(
    totalTasks: Int,
    completedTasks: Int,
    overdueTasks: Int,
    tasksByType: Map<TaskType, Int>,
    tasksByPriority: Map<TaskPriority, Int>,
    recentCompletionRate: Float,
    weeklyCompletionCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    // 颜色定义
    val normalTaskColor = Color(0xFF4A90E2)
    val checkinTaskColor = Color(0xFF4CAF50)
    val pomodoroTaskColor = Color(0xFFFF7F7F)
    
    val highPriorityColor = Color(0xFFFF5722)
    val mediumPriorityColor = Color(0xFFFFC107)
    val lowPriorityColor = Color(0xFF8BC34A)
    
    // 计算完成率
    val completionRate = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f
    
    // 完成率动画
    val animatedCompletionRate by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(durationMillis = 1000)
    )
    
    // 最近完成率动画
    val animatedRecentCompletionRate by animateFloatAsState(
        targetValue = recentCompletionRate,
        animationSpec = tween(durationMillis = 1000)
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
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
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Insights,
                    contentDescription = null,
                    tint = Color(0xFF4A90E2),
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "任务统计",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.DarkGray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 完成率环形图
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 总体完成率
                CompletionRateCircle(
                    rate = animatedCompletionRate,
                    label = "总体完成率",
                    completedCount = completedTasks,
                    totalCount = totalTasks,
                    color = Color(0xFF4A90E2)
                )
                
                // 最近完成率
                CompletionRateCircle(
                    rate = animatedRecentCompletionRate,
                    label = "最近完成率",
                    completedCount = (recentCompletionRate * 100).toInt(),
                    totalCount = 100,
                    color = Color(0xFF4CAF50),
                    showCount = false
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 任务类型分布
            Text(
                text = "任务类型分布",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 任务类型统计
            TaskTypeDistribution(
                checkinTasksCount = tasksByType[TaskType.CHECK_IN] ?: 0,
                pomodoroTasksCount = tasksByType[TaskType.POMODORO] ?: 0,
                otherTasksCount = tasksByType.entries.filter { it.key != TaskType.CHECK_IN && it.key != TaskType.POMODORO }.sumOf { it.value },
                checkinTaskColor = checkinTaskColor,
                pomodoroTaskColor = pomodoroTaskColor,
                otherTaskColor = normalTaskColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 任务优先级分布
            Text(
                text = "任务优先级分布",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 任务优先级统计
            PriorityDistribution(
                highPriorityCount = tasksByPriority[TaskPriority.HIGH] ?: 0,
                mediumPriorityCount = tasksByPriority[TaskPriority.MEDIUM] ?: 0,
                lowPriorityCount = tasksByPriority[TaskPriority.LOW] ?: 0,
                highPriorityColor = highPriorityColor,
                mediumPriorityColor = mediumPriorityColor,
                lowPriorityColor = lowPriorityColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 每周任务完成情况
            Text(
                text = "每周任务完成情况",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 周历图表
            WeeklyCompletionChart(
                weeklyData = weeklyCompletionCounts,
                barColor = Color(0xFF4A90E2)
            )
            
            // 逾期任务提醒
            if (overdueTasks > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "您有 $overdueTasks 个任务已逾期，请尽快处理",
                            color = Color(0xFFE57373),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 完成率环形图
 */
@Composable
private fun CompletionRateCircle(
    rate: Float,
    label: String,
    completedCount: Int,
    totalCount: Int,
    color: Color,
    showCount: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 环形进度
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // 底层灰色圆环
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // 进度圆环
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val diameterOffset = strokeWidth / 2
                val arcDimen = size.width - 2 * diameterOffset
                
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = rate * 360f,
                    useCenter = false,
                    topLeft = Offset(diameterOffset, diameterOffset),
                    size = Size(arcDimen, arcDimen),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // 中心百分比文本
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(rate * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = color
                )
                
                if (showCount) {
                    Text(
                        text = "$completedCount/$totalCount",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        
        // 标签
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}

/**
 * 任务类型分布统计
 */
@Composable
private fun TaskTypeDistribution(
    checkinTasksCount: Int,
    pomodoroTasksCount: Int,
    otherTasksCount: Int,
    checkinTaskColor: Color,
    pomodoroTaskColor: Color,
    otherTaskColor: Color
) {
    val total = checkinTasksCount + pomodoroTasksCount + otherTasksCount
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 水平进度条
        if (total > 0) {
            val checkinRatio = checkinTasksCount.toFloat() / total
            val pomodoroRatio = pomodoroTasksCount.toFloat() / total
            val otherRatio = otherTasksCount.toFloat() / total
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // 打卡任务
                Box(
                    modifier = Modifier
                        .weight(checkinRatio)
                        .fillMaxHeight()
                        .background(checkinTaskColor)
                )
                
                // 番茄钟任务
                Box(
                    modifier = Modifier
                        .weight(pomodoroRatio)
                        .fillMaxHeight()
                        .background(pomodoroTaskColor)
                )
                
                // 其他任务
                Box(
                    modifier = Modifier
                        .weight(otherRatio)
                        .fillMaxHeight()
                        .background(otherTaskColor)
                )
            }
        } else {
            // 没有任务时显示空进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f))
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 图例
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 打卡任务
            LegendItem(
                color = checkinTaskColor,
                label = "打卡任务",
                count = checkinTasksCount
            )
            
            // 番茄钟任务
            LegendItem(
                color = pomodoroTaskColor,
                label = "番茄钟任务",
                count = pomodoroTasksCount
            )
            
            // 其他任务
            LegendItem(
                color = otherTaskColor,
                label = "其他任务",
                count = otherTasksCount
            )
        }
    }
}

/**
 * 优先级分布统计
 */
@Composable
private fun PriorityDistribution(
    highPriorityCount: Int,
    mediumPriorityCount: Int,
    lowPriorityCount: Int,
    highPriorityColor: Color,
    mediumPriorityColor: Color,
    lowPriorityColor: Color
) {
    val total = highPriorityCount + mediumPriorityCount + lowPriorityCount
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 水平进度条
        if (total > 0) {
            val highRatio = highPriorityCount.toFloat() / total
            val mediumRatio = mediumPriorityCount.toFloat() / total
            val lowRatio = lowPriorityCount.toFloat() / total
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // 高优先级
                Box(
                    modifier = Modifier
                        .weight(highRatio)
                        .fillMaxHeight()
                        .background(highPriorityColor)
                )
                
                // 中优先级
                Box(
                    modifier = Modifier
                        .weight(mediumRatio)
                        .fillMaxHeight()
                        .background(mediumPriorityColor)
                )
                
                // 低优先级
                Box(
                    modifier = Modifier
                        .weight(lowRatio)
                        .fillMaxHeight()
                        .background(lowPriorityColor)
                )
            }
        } else {
            // 没有任务时显示空进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f))
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 图例
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 高优先级
            LegendItem(
                color = highPriorityColor,
                label = "高优先级",
                count = highPriorityCount
            )
            
            // 中优先级
            LegendItem(
                color = mediumPriorityColor,
                label = "中优先级",
                count = mediumPriorityCount
            )
            
            // 低优先级
            LegendItem(
                color = lowPriorityColor,
                label = "低优先级",
                count = lowPriorityCount
            )
        }
    }
}

/**
 * 图例项
 */
@Composable
private fun LegendItem(
    color: Color,
    label: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 颜色标记
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // 标签和数量
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            
            Text(
                text = count.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

/**
 * 每周完成情况图表
 */
@Composable
private fun WeeklyCompletionChart(
    weeklyData: Map<String, Int>,
    barColor: Color
) {
    // 获取最大值，用于计算柱状图高度比例
    val maxValue = weeklyData.values.maxOrNull() ?: 0
    
    // 确保最大值至少为1，避免除零错误
    val normalizedMaxValue = maxValue.coerceAtLeast(1)
    
    // 对日期进行排序
    val sortedData = weeklyData.entries.sortedBy { 
        // 假设日期格式为"MM-dd"
        val parts = it.key.split("-")
        val month = parts[0].toInt()
        val day = parts[1].toInt()
        month * 100 + day  // 转换为可比较的数字
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 柱状图
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // 只显示最近7天的数据
            val displayData = if (sortedData.size > 7) {
                sortedData.takeLast(7)
            } else {
                sortedData
            }
            
            displayData.forEach { (date, count) ->
                // 柱状图项
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 高度比例，最小值为2.dp，确保即使是0也有一点高度
                    val heightRatio = count.toFloat() / normalizedMaxValue
                    val height = 100.dp * heightRatio
                    
                    // 计数
                    if (count > 0) {
                        Text(
                            text = count.toString(),
                            fontSize = 10.sp,
                            color = barColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 柱状图条
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(20.dp)
                            .height(height.coerceAtLeast(2.dp))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (count > 0) barColor else Color.LightGray.copy(alpha = 0.2f))
                    )
                    
                    // 日期标签
                    Text(
                        text = date,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
} 