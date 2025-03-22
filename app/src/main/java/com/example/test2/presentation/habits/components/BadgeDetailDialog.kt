package com.example.test2.presentation.habits.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.model.HabitBadge
import com.example.test2.data.model.HabitBadgeType
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 徽章详情对话框
 *
 * @param badge 徽章数据
 * @param onDismiss 关闭回调
 */
@Composable
fun BadgeDetailDialog(
    badge: HabitBadge,
    onDismiss: () -> Unit
) {
    // 根据徽章类型获取颜色
    val badgeColor = getBadgeColorForDetail(badge.type)
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // 顶部关闭按钮
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // 徽章展示
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .size(120.dp)
            ) {
                // 放射状光芒
                if (badge.isUnlocked) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        // 绘制放射状光芒
                        for (i in 0 until 12) {
                            rotate(degrees = i * 30f) {
                                drawLine(
                                    color = badgeColor.copy(alpha = 0.3f),
                                    start = Offset(center.x, center.y - (size.minDimension / 4)),
                                    end = Offset(center.x, center.y - (size.minDimension / 2)),
                                    strokeWidth = 8f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
                
                // 徽章背景圆
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = if (badge.isUnlocked) {
                                    listOf(
                                        badgeColor.copy(alpha = 0.2f),
                                        badgeColor.copy(alpha = 0.5f)
                                    )
                                } else {
                                    listOf(
                                        Color.Gray.copy(alpha = 0.1f),
                                        Color.Gray.copy(alpha = 0.2f)
                                    )
                                }
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = if (badge.isUnlocked) {
                                    listOf(
                                        badgeColor.copy(alpha = 0.7f),
                                        badgeColor
                                    )
                                } else {
                                    listOf(
                                        Color.Gray.copy(alpha = 0.3f),
                                        Color.Gray.copy(alpha = 0.5f)
                                    )
                                }
                            ),
                            shape = CircleShape
                        )
                ) {
                    // 徽章图标
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (badge.isUnlocked) badgeColor else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // 徽章标题
            Text(
                text = badge.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) badgeColor else Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 徽章描述
            Text(
                text = badge.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 解锁状态
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .background(
                        if (badge.isUnlocked) badgeColor.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = if (badge.isUnlocked) {
                        "获得时间: ${badge.unlockedAt?.let { formatDate(it) } ?: "未知"}"
                    } else {
                        "未解锁"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (badge.isUnlocked) badgeColor else Color.Gray
                )
            }
            
            if (!badge.isUnlocked) {
                // 如何获得
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "如何获得这个徽章:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                Text(
                    text = getBadgeRequirement(badge.type),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 关闭按钮
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("关闭")
            }
        }
    }
}

/**
 * 格式化日期
 */
private fun formatDate(date: java.util.Date): String {
    val format = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    return format.format(date)
}

/**
 * 获取徽章颜色
 */
fun getBadgeColorForDetail(type: HabitBadgeType): Color {
    return when (type) {
        HabitBadgeType.STARTER -> Color(0xFF4CAF50)      // 绿色
        HabitBadgeType.PERSISTENT -> Color(0xFF2196F3)   // 蓝色
        HabitBadgeType.DEDICATED -> Color(0xFF9C27B0)    // 紫色
        HabitBadgeType.MASTER -> Color(0xFFFFD700)       // 金色
        HabitBadgeType.COMEBACK -> Color(0xFFFF9800)     // 橙色
        HabitBadgeType.CONSISTENT -> Color(0xFF3F51B5)   // 靛蓝色
        HabitBadgeType.EARLY_BIRD -> Color(0xFFE91E63)   // 粉色
        HabitBadgeType.NIGHT_OWL -> Color(0xFF673AB7)    // 深紫色
        HabitBadgeType.SOCIAL -> Color(0xFF00BCD4)       // 青色
        HabitBadgeType.MILESTONE -> Color(0xFFF44336)    // 红色
    }
}

/**
 * 获取徽章获取条件
 */
private fun getBadgeRequirement(type: HabitBadgeType): String {
    return when (type) {
        HabitBadgeType.STARTER -> "开始一个新习惯后自动获得"
        HabitBadgeType.PERSISTENT -> "连续完成习惯7天"
        HabitBadgeType.DEDICATED -> "连续完成习惯30天"
        HabitBadgeType.MASTER -> "连续完成习惯100天"
        HabitBadgeType.COMEBACK -> "中断后重新开始习惯"
        HabitBadgeType.CONSISTENT -> "习惯完成率达到80%以上"
        HabitBadgeType.EARLY_BIRD -> "在早上6-9点之间完成习惯"
        HabitBadgeType.NIGHT_OWL -> "在晚上9点-午夜之间完成习惯"
        HabitBadgeType.SOCIAL -> "分享你的习惯到社交媒体"
        HabitBadgeType.MILESTONE -> "达成特定的里程碑目标"
    }
} 