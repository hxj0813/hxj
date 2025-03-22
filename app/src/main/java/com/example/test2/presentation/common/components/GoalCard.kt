package com.example.test2.presentation.common.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.model.Goal
import com.example.test2.presentation.theme.AccentGreen
import com.example.test2.presentation.theme.AccentPink
import com.example.test2.presentation.theme.PrimaryDark
import com.example.test2.presentation.theme.PrimaryLight
import com.example.test2.util.DateTimeUtil
import kotlin.math.min

/**
 * 目标卡片组件
 * 
 * @param goal 目标数据
 * @param onEdit 编辑按钮点击回调
 * @param onDelete 删除按钮点击回调
 * @param onToggleImportant 切换重要性回调
 * @param onClick 卡片点击回调
 */
@Composable
fun GoalCard(
    goal: Goal,
    onEdit: (Goal) -> Unit = {},
    onDelete: (Goal) -> Unit = {},
    onToggleImportant: (Goal) -> Unit = {},
    onClick: (Goal) -> Unit = {}
) {
    // 确保进度值在0到1之间
    val progress = min(1f, goal.progress)
    
    // 进度条动画
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )
    
    // 完成状态的脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "completedPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    // 卡片实现
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(goal) }
            // 重要目标有特殊边框
            .then(
                if (goal.isImportant) {
                    Modifier.border(
                        width = 2.dp,
                        color = AccentPink,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        // 磨砂玻璃效果通过设置半透明背景和模糊实现
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.7f) // 半透明背景
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .blur(radius = 0.dp) // 内容不模糊，只有背景模糊
        ) {
            // 顶部区域：目标类型和重要性标记
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 目标类型标签
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (goal.isLongTerm) 
                        PrimaryLight.copy(alpha = 0.2f) 
                    else 
                        AccentGreen.copy(alpha = 0.2f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = if (goal.isLongTerm) "长期目标" else "短期目标",
                        color = if (goal.isLongTerm) PrimaryDark else Color(0xFF2E8B57),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // 重要性星标
                IconButton(
                    onClick = { onToggleImportant(goal) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (goal.isImportant) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (goal.isImportant) "取消重要标记" else "标记为重要",
                        tint = if (goal.isImportant) AccentPink else Color.Gray
                    )
                }
            }
            
            // 目标标题
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
            
            // 截止日期
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "截止日期",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "截止日期: ${DateTimeUtil.formatDate(goal.deadline)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            // 目标描述
            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )
            
            // 进度条容器
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                // 进度条 - 根据完成状态使用不同的渐变色和动画
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (goal.isCompleted) 1f else animatedProgress)
                        .height(10.dp)
                        .alpha(if (goal.isCompleted) pulseAlpha else 1f)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (goal.isCompleted) {
                                    listOf(Color(0xFF85D6B0), AccentGreen)
                                } else {
                                    listOf(PrimaryLight, PrimaryDark)
                                }
                            )
                        )
                )
            }
            
            // 底部操作区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 完成状态或进度百分比
                if (goal.isCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "已完成",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "已完成",
                            tint = AccentGreen,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(16.dp)
                        )
                    }
                } else {
                    Text(
                        text = "${(goal.progress * 100).toInt()}% 完成",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // 编辑和删除按钮
                Row(
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { onEdit(goal) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "编辑",
                            tint = Color.Gray
                        )
                    }
                    
                    IconButton(
                        onClick = { onDelete(goal) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "删除",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
} 