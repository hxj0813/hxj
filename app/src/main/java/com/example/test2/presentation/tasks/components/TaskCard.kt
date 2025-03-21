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
import com.example.test2.presentation.theme.CompletedGreen
import com.example.test2.presentation.theme.ErrorRed
import com.example.test2.presentation.theme.InfoBlue
import com.example.test2.presentation.theme.OverdueRed
import com.example.test2.presentation.theme.PrimaryDark
import com.example.test2.presentation.theme.PrimaryLight
import com.example.test2.presentation.theme.WarningAmber
import com.example.test2.util.DateTimeUtil
import kotlin.math.roundToInt

/**
 * 任务卡片组件
 */
@Composable
fun TaskCard(
    task: Task,
    onToggleCompletion: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDragStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // 用于处理滑动手势
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        label = "偏移动画"
    )
    
    // 任务完成时的视觉效果
    val cardAlpha = if (task.isCompleted) 0.7f else 1f
    val textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
    
    // 根据任务优先级和状态确定颜色
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> ErrorRed
        TaskPriority.MEDIUM -> WarningAmber
        TaskPriority.LOW -> InfoBlue
    }
    
    val statusColor = when {
        task.isCompleted -> CompletedGreen
        task.isOverdue() -> OverdueRed
        else -> Color.Transparent
    }
    
    // 边框颜色
    val borderColor = when {
        task.isCompleted -> CompletedGreen.copy(alpha = 0.5f)
        task.isOverdue() -> OverdueRed.copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // 滑动菜单背景
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ErrorRed.copy(alpha = 0.1f))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = ErrorRed,
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
            )
        }
        
        // 主卡片内容
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .alpha(cardAlpha)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { onDragStateChanged(true) },
                        onDragEnd = {
                            if (offsetX < -200) {
                                onDeleteClick()
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
                // 任务完成状态复选框
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onToggleCompletion,
                    colors = CheckboxDefaults.colors(
                        checkedColor = CompletedGreen,
                        uncheckedColor = PrimaryLight
                    ),
                    modifier = Modifier.scale(1.2f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
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
                }
                
                // 优先级指示器
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(priorityColor)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 编辑按钮
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑任务",
                        tint = PrimaryDark.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 任务优先级色块指示器
 *
 * @param priority 任务优先级
 * @param modifier 修饰符
 */
@Composable
fun PriorityIndicator(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    val color = Color(android.graphics.Color.parseColor(when (priority) {
        TaskPriority.LOW -> "#8BC34A"      // 浅绿色
        TaskPriority.MEDIUM -> "#4FC3F7"   // 浅蓝色
        TaskPriority.HIGH -> "#F44336"     // 红色
    }))
    
    Box(
        modifier = modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
    )
} 