package com.example.test2.presentation.tasks.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.model.Task
import com.example.test2.data.model.TaskPriority
import com.example.test2.data.model.TaskType
import com.example.test2.data.model.CheckInFrequencyType
import com.example.test2.data.model.PomodoroTag
import com.example.test2.presentation.theme.CompletedGreen
import com.example.test2.presentation.theme.ErrorRed
import com.example.test2.presentation.theme.InfoBlue
import com.example.test2.presentation.theme.OverdueRed
import com.example.test2.presentation.theme.PrimaryDark
import com.example.test2.presentation.theme.PrimaryLight
import com.example.test2.presentation.theme.WarningAmber
import com.example.test2.util.DateTimeUtil
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.test2.presentation.tasks.components.PriorityIndicator
import com.example.test2.presentation.tasks.components.TaskTypeChip

/**
 * 任务卡片组件
 *
 * @param task 任务对象
 * @param onToggleCompletion 切换完成状态回调
 * @param onDelete 删除任务回调
 * @param onEditClick 编辑任务回调
 * @param onCheckinClick 打卡回调，用于打卡任务
 * @param onStartClick 开始回调，用于番茄钟任务
 * @param modifier Modifier修饰符
 */
@Composable
fun TaskCard(
    task: Task,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onEditClick: () -> Unit,
    onCheckinClick: (() -> Unit)? = null,
    onStartClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 拖动状态
    var offsetX by remember { mutableStateOf(0f) }
    val deleteThreshold = -200f
    
    // 拖动状态动画
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
    
    // 拖动时的背景颜色插值
    val backgroundColor = when {
        offsetX < deleteThreshold -> ErrorRed
        else -> Color.Transparent
    }
    
    // 用于显示删除图标的透明度
    val deleteIconAlpha = when {
        offsetX < -100f -> (-offsetX - 100f) / 100f
        else -> 0f
    }
    
    // 拖动状态改变回调
    val onDragStateChanged: (Boolean) -> Unit = { _ -> }
    
    // 任务的文本样式
    val textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
    
    // 优先级颜色
    val priorityColor = when (task.priority) {
        TaskPriority.LOW -> Color(0xFF8BC34A)      // 浅绿色
        TaskPriority.MEDIUM -> Color(0xFF4FC3F7)   // 浅蓝色
        TaskPriority.HIGH -> Color(0xFFFF9800)     // 橙色
    }
    
    // 边框颜色
    val borderColor = if (task.isHighImportance() && !task.isCompleted) {
        priorityColor
    } else {
        Color.Transparent
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // 拖动背景
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundColor)
                .padding(end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除任务",
                tint = Color.White,
                modifier = Modifier
                    .alpha(deleteIconAlpha)
                    .size(24.dp)
            )
        }
        
        // 任务卡片
        Card(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .fillMaxWidth()
                .clickable { onEditClick() }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < deleteThreshold) {
                                onDelete()
                            }
                            offsetX = 0f
                            onDragStateChanged(false)
                        },
                        onDragCancel = {
                            offsetX = 0f
                            onDragStateChanged(false)
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffsetX = offsetX + dragAmount
                            offsetX = when {
                                newOffsetX < -350 -> -350f
                                newOffsetX > 0 -> 0f
                                else -> newOffsetX
                            }
                        }
                    )
                }
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    }
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 任务内容区域
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 标题和描述
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = textDecoration
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (!task.description.isNullOrBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray,
                                textDecoration = textDecoration
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.alpha(0.8f)
                        )
                    }
                    
                    // 截止日期
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // 显示任务截止时间与当前的关系
                        val timeText = task.dueDate?.let {
                            if (task.isCompleted) {
                                "已完成"
                            } else {
                                DateTimeUtil.formatRemainingTime(it)
                            }
                        } ?: "无截止时间"
                        
                        Surface(
                            color = when {
                                task.isCompleted -> CompletedGreen.copy(alpha = 0.1f)
                                task.isOverdue() -> OverdueRed.copy(alpha = 0.1f)
                                task.isUpcoming() -> WarningAmber.copy(alpha = 0.1f)
                                else -> PrimaryLight.copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = timeText,
                                fontSize = 12.sp,
                                color = when {
                                    task.isCompleted -> CompletedGreen
                                    task.isOverdue() -> OverdueRed
                                    task.isUpcoming() -> WarningAmber.copy(alpha = 0.8f)
                                    else -> PrimaryDark
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        
                        // 显示关联目标
                        if (task.goalId != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "关联目标",
                                tint = PrimaryLight,
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = "已关联目标",
                                fontSize = 12.sp,
                                color = PrimaryLight
                            )
                        }
                    }
                    
                    // 任务类型标签
                    if (task.type != TaskType.NORMAL) {
                        TaskTypeChip(
                            taskType = task.type,
                            pomodoroSettings = task.pomodoroSettings,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // 显示打卡任务信息
                    if (task.type == TaskType.CHECK_IN && task.checkInSettings != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val checkInSettings = task.checkInSettings
                        val frequencyText = when (checkInSettings.frequencyType) {
                            CheckInFrequencyType.DAILY -> "每日${checkInSettings.frequency}次"
                            CheckInFrequencyType.WEEKLY -> "每周${checkInSettings.frequency}天"
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = frequencyText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            // 显示当前完成情况
                            if (checkInSettings.frequencyType == CheckInFrequencyType.DAILY) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(今日已完成: ${checkInSettings.completedToday}/${checkInSettings.frequency})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(本周已完成: ${checkInSettings.completedThisWeek}/${checkInSettings.frequency}天)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // 如果有每日截止时间
                        if (checkInSettings.dailyDeadline != null) {
                            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = "每日${timeFormatter.format(checkInSettings.dailyDeadline)}前",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // 显示连续打卡信息
                        if (checkInSettings.currentStreak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "已连续打卡: ${checkInSettings.currentStreak}天",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // 显示番茄钟任务信息
                    if (task.type == TaskType.POMODORO && task.pomodoroSettings != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val pomodoroSettings = task.pomodoroSettings
                        
                        // 显示标签
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(pomodoroSettings.tag.getColor()))
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = if (pomodoroSettings.tag == PomodoroTag.CUSTOM && !pomodoroSettings.customTagName.isNullOrBlank()) 
                                    pomodoroSettings.customTagName
                                else 
                                    pomodoroSettings.tag.getDisplayName(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(pomodoroSettings.tag.getColor()).copy(alpha = 0.8f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = "${pomodoroSettings.focusMinutes}分钟/次",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "今日完成: ${pomodoroSettings.todayCompletedSessions}次",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        
                        // 显示总完成次数
                        if (pomodoroSettings.totalCompletedSessions > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "总计完成: ${pomodoroSettings.totalCompletedSessions}次 (${pomodoroSettings.totalFocusMinutes}分钟)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // 显示关联习惯
                    if (task.habitId != null && task.habitTitle != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = "习惯: ${task.habitTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // 优先级指示器
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(priorityColor)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 根据任务类型显示对应的操作按钮
                when (task.type) {
                    TaskType.CHECK_IN -> {
                        // 打卡任务显示打卡按钮
                        IconButton(
                            onClick = { onCheckinClick?.invoke() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "打卡",
                                tint = CompletedGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    TaskType.POMODORO -> {
                        // 番茄钟任务显示开始按钮
                        IconButton(
                            onClick = { onStartClick?.invoke() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "开始番茄钟",
                                tint = Color(task.pomodoroSettings?.tag?.getColor() ?: 0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    else -> {
                        // 普通任务显示完成按钮
                        IconButton(
                            onClick = onToggleCompletion,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.Clear else Icons.Default.Done,
                                contentDescription = if (task.isCompleted) "取消完成" else "标记完成",
                                tint = if (task.isCompleted) OverdueRed else CompletedGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 