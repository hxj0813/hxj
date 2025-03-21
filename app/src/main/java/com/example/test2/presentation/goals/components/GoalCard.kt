package com.example.test2.presentation.goals.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.model.Goal
import com.example.test2.data.model.GoalStatus
import com.example.test2.presentation.theme.AccentPink
import com.example.test2.presentation.theme.PrimaryDark
import com.example.test2.presentation.theme.PrimaryLight
import com.example.test2.util.DateTimeUtil

/**
 * 目标卡片组件
 *
 * @param goal 目标数据
 * @param onEditClick 编辑点击回调
 * @param onDeleteClick 删除点击回调
 * @param onToggleCompletion 切换完成状态回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: Goal,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleCompletion: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // 状态
    val goalStatus = goal.getStatus()
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "进度动画"
    )

    // 卡片颜色和透明度
    val cardColor = when (goalStatus) {
        GoalStatus.COMPLETED -> Color(0xFFE8F5E9)
        GoalStatus.OVERDUE -> Color(0xFFFDEDED)
        GoalStatus.UPCOMING -> Color(0xFFFFF9C4)
        else -> Color.White
    }
    
    // 磨砂玻璃效果
    val frostGlassColor = cardColor.copy(alpha = 0.7f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = frostGlassColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 目标标题和状态图标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 重要标记
                    if (goal.isImportant) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "重要目标",
                            tint = AccentPink,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    
                    // 标题
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 完成状态切换
                IconButton(
                    onClick = { onToggleCompletion(!goal.isCompleted) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (goal.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (goal.isCompleted) "标记为未完成" else "标记为已完成",
                        tint = if (goal.isCompleted) PrimaryLight else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 目标描述
            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.DarkGray,
                modifier = Modifier.alpha(if (goal.isCompleted) 0.7f else 1f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x20000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryLight,
                                    AccentPink
                                )
                            )
                        )
                )
            }
            
            // 进度文本和截止日期
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryDark
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (goalStatus) {
                            GoalStatus.COMPLETED -> Icons.Default.CheckCircle
                            GoalStatus.OVERDUE -> Icons.Default.Warning
                            GoalStatus.UPCOMING -> Icons.Default.WatchLater
                            else -> Icons.Default.WatchLater
                        },
                        contentDescription = null,
                        tint = when (goalStatus) {
                            GoalStatus.COMPLETED -> Color(0xFF4CAF50)  // Green
                            GoalStatus.OVERDUE -> Color(0xFFF44336)    // Red
                            GoalStatus.UPCOMING -> Color(0xFFFF9800)   // Amber
                            else -> Color(0xFF2196F3)                 // Blue
                        },
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = DateTimeUtil.formatRemainingTime(goal.deadline),
                        style = MaterialTheme.typography.bodySmall,
                        color = when (goalStatus) {
                            GoalStatus.COMPLETED -> Color(0xFF4CAF50)  // Green
                            GoalStatus.OVERDUE -> Color(0xFFF44336)    // Red
                            GoalStatus.UPCOMING -> Color(0xFFFF9800)   // Amber
                            else -> Color(0xFF2196F3)                 // Blue
                        }
                    )
                }
            }
            
            // 分隔线
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x10000000))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // 删除按钮
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // 编辑按钮
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = PrimaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
} 