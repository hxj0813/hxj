package com.example.test2.presentation.timetracking.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import com.example.test2.presentation.theme.PrimaryLight
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.time.Duration.Companion.seconds

/**
 * 时间计时器组件，用于显示和控制正在进行的时间追踪
 */
@Composable
fun TimerView(
    ongoingEntry: TimeEntry?,
    onStopTimer: () -> Unit,
    onStartTimer: (TimeEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var seconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(ongoingEntry != null) }
    
    // 计算已经运行的时间
    LaunchedEffect(ongoingEntry) {
        isRunning = ongoingEntry != null
        if (ongoingEntry != null) {
            // 计算已经经过的秒数
            seconds = (Date().time - ongoingEntry.startTime.time) / 1000
        } else {
            seconds = 0
        }
    }
    
    // 计时器
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            seconds++
        }
    }
    
    // 格式化时间显示
    val formattedTime = remember(seconds) {
        formatTime(seconds)
    }
    
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (ongoingEntry != null) {
                // 显示正在追踪的活动信息
                Text(
                    text = "正在追踪",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = ongoingEntry.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                ongoingEntry.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 分类标签
                CategoryChip(category = ongoingEntry.category)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 计时器显示
                Text(
                    text = formattedTime,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryLight
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 停止按钮
                FloatingActionButton(
                    onClick = { onStopTimer() },
                    containerColor = PrimaryLight,
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "停止计时",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                // 显示未开始追踪的提示
                Text(
                    text = "开始追踪你的时间",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 快速启动按钮
                QuickStartButtons(onStartTimer = onStartTimer)
            }
        }
    }
}

/**
 * 分类标签组件
 */
@Composable
fun CategoryChip(
    category: TimeCategory,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = getCategoryColors(category)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = getCategoryName(category),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 快速启动按钮组
 */
@Composable
fun QuickStartButtons(
    onStartTimer: (TimeEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickCategories = listOf(
        TimeCategory.WORK,
        TimeCategory.STUDY,
        TimeCategory.EXERCISE
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            quickCategories.forEach { category ->
                QuickStartButton(
                    category = category,
                    onClick = {
                        val title = getDefaultTitleForCategory(category)
                        val timeEntry = TimeEntry(
                            title = title,
                            category = category,
                            startTime = Date()
                        )
                        onStartTimer(timeEntry)
                    }
                )
            }
        }
    }
}

/**
 * 快速启动单个按钮
 */
@Composable
fun QuickStartButton(
    category: TimeCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, _) = getCategoryColors(category)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(backgroundColor.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "开始 ${getCategoryName(category)}",
                tint = backgroundColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = getCategoryName(category),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

/**
 * 获取分类的颜色
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
 * 获取分类的名称
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
 * 获取分类的默认标题
 */
private fun getDefaultTitleForCategory(category: TimeCategory): String {
    return when (category) {
        TimeCategory.WORK -> "工作任务"
        TimeCategory.STUDY -> "学习时间"
        TimeCategory.EXERCISE -> "锻炼时间"
        TimeCategory.REST -> "休息时间"
        TimeCategory.ENTERTAIN -> "娱乐时间"
        TimeCategory.OTHER -> "其他活动"
    }
}

/**
 * 格式化时间为小时:分钟:秒
 */
private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
} 