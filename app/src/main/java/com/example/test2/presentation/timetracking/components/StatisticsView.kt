package com.example.test2.presentation.timetracking.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.test2.data.model.TimeCategory
import com.example.test2.presentation.timetracking.TimeTrackingState

/**
 * 时间统计视图，用于显示时间分配情况
 */
@Composable
fun StatisticsView(
    statistics: TimeTrackingState.TimeStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "时间分布统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 总时间统计
            TotalTimeStats(
                totalHours = statistics.totalDurationInHours,
                entriesCount = statistics.totalEntries
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (statistics.categoryDurations.isNotEmpty()) {
                // 饼图
                PieChart(categoryDurations = statistics.categoryDurations)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 分类图例
                CategoryLegend(categoryDurations = statistics.categoryDurations)
            } else {
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 总时间统计组件
 */
@Composable
fun TotalTimeStats(
    totalHours: Double,
    entriesCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatBox(
            label = "总时长（小时）",
            value = String.format("%.1f", totalHours),
            modifier = Modifier.weight(1f)
        )
        
        StatBox(
            label = "记录数量",
            value = entriesCount.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 单个统计框
 */
@Composable
fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

/**
 * 饼图组件
 */
@Composable
fun PieChart(
    categoryDurations: Map<TimeCategory, Long>,
    modifier: Modifier = Modifier
) {
    // 计算总时间
    val totalTime = remember(categoryDurations) {
        categoryDurations.values.sum().toFloat()
    }
    
    // 排序后的分类
    val sortedCategories = remember(categoryDurations) {
        categoryDurations.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.value }
            .map { it.key }
    }
    
    // 计算每个分类的角度
    val sweepAngles = remember(categoryDurations, totalTime) {
        if (totalTime <= 0f) emptyMap()
        else categoryDurations.mapValues { (_, duration) -> 
            (duration.toFloat() / totalTime) * 360f 
        }
    }
    
    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(categoryDurations) {
        animationTriggered = true
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .padding(8.dp)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            
            var startAngle = -90f // 从顶部开始
            
            sortedCategories.forEach { category ->
                val sweepAngle = sweepAngles[category] ?: 0f
                if (sweepAngle > 0) {
                    val (color, _) = getCategoryColors(category)
                    
                    // 每个分类的动画进度
                    val animatedSweepAngle by animateFloatAsState(
                        targetValue = if (animationTriggered) sweepAngle else 0f,
                        animationSpec = tween(durationMillis = 800, delayMillis = 100),
                        label = "SweepAngleAnimation"
                    )
                    
                    // 绘制扇形
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = animatedSweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    
                    startAngle += sweepAngle
                }
            }
            
            // 中间的白色圆形
            drawCircle(
                color = Color.White,
                radius = radius * 0.6f,
                center = center
            )
        }
        
        // 中心文本
        if (totalTime > 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val totalHours = totalTime / 3600
                Text(
                    text = String.format("%.1f", totalHours),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                
                Text(
                    text = "总小时",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 分类图例组件
 */
@Composable
fun CategoryLegend(
    categoryDurations: Map<TimeCategory, Long>,
    modifier: Modifier = Modifier
) {
    val sortedEntries = remember(categoryDurations) {
        categoryDurations.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.value }
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        sortedEntries.forEach { (category, duration) ->
            val (color, _) = getCategoryColors(category)
            val percentage = (duration.toFloat() / categoryDurations.values.sum().toFloat()) * 100
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 颜色方块
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 分类名称
                Text(
                    text = getCategoryName(category),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                // 时长
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 百分比
                Text(
                    text = String.format("%.1f%%", percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 获取分类的颜色，与TimerView中保持一致
 */
private fun getCategoryColors(category: TimeCategory): Pair<Color, Color> {
    return when (category) {
        TimeCategory.WORK -> Pair(Color(0xFF4285F4), Color(0xFF4285F4))      // 蓝色
        TimeCategory.STUDY -> Pair(Color(0xFF0F9D58), Color(0xFF0F9D58))     // 绿色
        TimeCategory.EXERCISE -> Pair(Color(0xFFF4B400), Color(0xFFF4B400))  // 黄色
        TimeCategory.REST -> Pair(Color(0xFF7986CB), Color(0xFF7986CB))      // 淡紫色
        TimeCategory.ENTERTAIN -> Pair(Color(0xFFDB4437), Color(0xFFDB4437)) // 红色
        TimeCategory.OTHER -> Pair(Color(0xFF9E9E9E), Color(0xFF9E9E9E))     // 灰色
    }
}

/**
 * 获取分类的名称，与TimerView中保持一致
 */
private fun getCategoryName(category: TimeCategory): String {
    return when (category) {
        TimeCategory.WORK -> "工作"
        TimeCategory.STUDY -> "学习"
        TimeCategory.EXERCISE -> "锻炼"
        TimeCategory.REST -> "休息"
        TimeCategory.ENTERTAIN -> "娱乐"
        TimeCategory.OTHER -> "其他"
    }
}

/**
 * 格式化时长为小时:分钟
 */
private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    
    return if (hours > 0) {
        String.format("%dh %02dm", hours, minutes)
    } else {
        String.format("%dm", minutes)
    }
} 