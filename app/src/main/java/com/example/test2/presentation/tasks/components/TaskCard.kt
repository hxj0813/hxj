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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.local.entity.TaskEntity as Task
import com.example.test2.data.local.entity.TaskPriority
import com.example.test2.data.local.entity.TaskType
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.filled.Assignment

/**
 * 现代化的任务卡片组件
 *
 * @param task 任务对象
 * @param onToggleCompletion 切换完成状态的回调
 * @param onDelete 删除任务的回调
 * @param onEditClick 编辑任务的回调
 * @param onCheckinClick 打卡任务的回调，仅对CHECK_IN类型任务生效
 * @param onStartClick 开始任务的回调，仅对POMODORO类型任务生效
 * @param onCardClick 导航到任务详情的回调
 * @param modifier Modifier修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onEditClick: () -> Unit,
    onCheckinClick: (() -> Unit)? = null,
    onStartClick: (() -> Unit)? = null,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 颜色定义
    val taskTypeColors = mapOf(
        TaskType.CHECK_IN to Color(0xFF4CAF50),    // 绿色
        TaskType.POMODORO to Color(0xFFFF7F7F)     // 粉色
    )
    val defaultTypeColor = Color(0xFF4A90E2)       // 蓝色，默认颜色
    
    val priorityColors = mapOf(
        TaskPriority.LOW to Color(0xFF8BC34A),     // 浅绿色
        TaskPriority.MEDIUM to Color(0xFFFFC107),  // 琥珀色
        TaskPriority.HIGH to Color(0xFFFF5722)     // 深橙色
    )
    
    // 获取任务类型和优先级颜色
    val typeColor = taskTypeColors[task.getTaskTypeEnum()] ?: defaultTypeColor
    val priorityColor = priorityColors[task.getPriorityEnum()] ?: Color.Gray
    
    // 日期格式化
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val dueDateText = task.dueDate?.let { dateFormat.format(it) } ?: "无截止日期"
    
    // 是否显示操作菜单
    var showActions by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { 
                // 如果已经展开操作菜单，则收起；否则导航到详情页面
                if (showActions) {
                    showActions = false
                } else {
                    onCardClick()
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 任务内容区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 完成状态复选框
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleCompletion() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = typeColor,
                        uncheckedColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(24.dp)
                )
                
                // 任务标题和描述
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = if (task.isCompleted) Color.Gray else Color.DarkGray,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (!task.description.isNullOrEmpty()) {
                        Text(
                            text = task.description,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // 任务详细信息（截止日期、类型等）
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 任务类型标签
                        TaskTypeChip(task.getTaskTypeEnum())
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // 优先级指示器
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(priorityColor)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // 截止日期
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = "截止日期",
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .size(14.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = dueDateText,
                                fontSize = 12.sp,
                                color = if (task.isOverdue()) Color(0xFFE57373) else Color.Gray
                            )
                        }
                    }
                }
                
                // 任务类型图标
                TaskTypeIcon(
                    taskType = task.getTaskTypeEnum(),
                    color = typeColor,
                    isCompleted = task.isCompleted
                )
            }
            
            // 任务操作区域（仅在展开时显示）
            AnimatedVisibility(visible = showActions) {
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 编辑按钮
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "编辑",
                            tint = Color(0xFF4A90E2)
                        )
                    }
                    
                    // 删除按钮
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "删除",
                            tint = Color(0xFFE57373)
                        )
                    }
                    
                    // 根据任务类型显示不同操作按钮
                    when (task.getTaskTypeEnum()) {
                        TaskType.CHECK_IN -> {
                            // 打卡按钮
                            IconButton(
                                onClick = { onCheckinClick?.invoke() },
                                enabled = onCheckinClick != null && !task.isCompleted
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "打卡",
                                    tint = if (onCheckinClick != null && !task.isCompleted) 
                                        Color(0xFF4CAF50) else Color.Gray
                                )
                            }
                        }
                        TaskType.POMODORO -> {
                            // 开始番茄钟按钮
                            IconButton(
                                onClick = { onStartClick?.invoke() },
                                enabled = onStartClick != null && !task.isCompleted
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "开始番茄钟",
                                    tint = if (onStartClick != null && !task.isCompleted) 
                                        Color(0xFFFF7F7F) else Color.Gray
                                )
                            }
                        }
                        else -> {
                            // 默认处理：无特殊操作按钮
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 任务类型图标
 */
@Composable
fun TaskTypeIcon(
    taskType: TaskType,
    color: Color,
    isCompleted: Boolean
) {
    val icon = when (taskType) {
        TaskType.CHECK_IN -> if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.CheckCircle
        TaskType.POMODORO -> if (isCompleted) Icons.Outlined.Timer else Icons.Default.Timer
        else -> Icons.Default.Assignment  // 默认图标，用于未知任务类型
    }
    
    Icon(
        imageVector = icon,
        contentDescription = "任务类型",
        tint = if (isCompleted) Color.Gray else color,
        modifier = Modifier.size(24.dp)
    )
} 