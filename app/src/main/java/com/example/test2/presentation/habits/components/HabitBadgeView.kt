package com.example.test2.presentation.habits.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Trending
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.model.HabitBadge
import com.example.test2.data.model.HabitBadgeType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 习惯徽章展示组件
 *
 * @param badges 徽章列表
 * @param onBadgeClick 徽章点击回调
 * @param modifier Modifier修饰符
 */
@Composable
fun HabitBadgeView(
    badges: List<HabitBadge>,
    onBadgeClick: (HabitBadge) -> Unit,
    modifier: Modifier = Modifier
) {
    // 徽章为空时不显示
    if (badges.isEmpty()) return
    
    Column(modifier = modifier) {
        // 标题
        Text(
            text = "成就徽章",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 徽章列表
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(badges) { badge ->
                BadgeItem(
                    badge = badge,
                    onClick = { onBadgeClick(badge) }
                )
            }
        }
    }
}

/**
 * 单个徽章项
 *
 * @param badge 徽章数据
 * @param onClick 点击回调
 */
@Composable
fun BadgeItem(
    badge: HabitBadge,
    onClick: () -> Unit
) {
    // 根据徽章类型获取颜色
    val badgeColor = getBadgeColor(badge.type)
    
    // 获取悬浮效果
    val infiniteTransition = rememberInfiniteTransition(label = "BadgeAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeScale"
    )
    
    // 星光效果
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing)
        ),
        label = "StarRotation"
    )
    
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(130.dp)
            .height(170.dp)
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 徽章图标
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        if (badge.isUnlocked) {
                            this.scaleX = scale
                            this.scaleY = scale
                        }
                    }
            ) {
                // 光晕背景
                if (badge.isUnlocked) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationZ = rotation }
                    ) {
                        // 绘制放射状光芒
                        for (i in 0 until 8) {
                            rotate(degrees = i * 45f) {
                                drawLine(
                                    color = badgeColor.copy(alpha = 0.3f),
                                    start = Offset(center.x, center.y - (size.minDimension / 3)),
                                    end = Offset(center.x, center.y - (size.minDimension / 2)),
                                    strokeWidth = 4f,
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
                        .size(70.dp)
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
                            BorderStroke(
                                width = 2.dp,
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
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    // 徽章图标
                    Icon(
                        imageVector = getBadgeIcon(badge.type),
                        contentDescription = badge.title,
                        tint = if (badge.isUnlocked) badgeColor else Color.Gray,
                        modifier = Modifier
                            .size(36.dp)
                            .alpha(if (badge.isUnlocked) 1f else 0.5f)
                    )
                    
                    // 锁定图标
                    if (!badge.isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "未解锁",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 徽章标题
            Text(
                text = badge.title,
                color = if (badge.isUnlocked) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 徽章描述
            Text(
                text = badge.description,
                color = if (badge.isUnlocked) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // 解锁日期
            if (badge.isUnlocked && badge.unlockedAt != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(badge.unlockedAt),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 详细徽章展示对话框
 *
 * @param badge 徽章数据
 * @param onDismiss 关闭回调
 */
@Composable
fun BadgeDetailDialog(
    badge: HabitBadge,
    onDismiss: () -> Unit
) {
    val badgeColor = getBadgeColor(badge.type)
    
    // 动画效果
    var showDetails by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (showDetails) 1f else 0.5f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "ScaleAnimation"
    )
    
    // 启动动画
    LaunchedEffect(Unit) {
        showDetails = true
    }
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .scale(scale)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
        ) {
            // 徽章图标
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            ) {
                // 光晕背景
                if (badge.isUnlocked) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 绘制徽章后的光芒
                        val strokeWidth = 8.dp.toPx()
                        drawCircle(
                            color = badgeColor.copy(alpha = 0.2f),
                            radius = size.minDimension / 2.2f,
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // 绘制放射状光芒
                        for (i in 0 until 12) {
                            rotate(degrees = i * 30f) {
                                drawLine(
                                    color = badgeColor.copy(alpha = 0.3f),
                                    start = Offset(center.x, center.y - (size.minDimension / 2.5f)),
                                    end = Offset(center.x, center.y - (size.minDimension / 1.8f)),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
                
                // 徽章背景
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
                            BorderStroke(
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
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    // 徽章图标
                    Icon(
                        imageVector = getBadgeIcon(badge.type),
                        contentDescription = badge.title,
                        tint = if (badge.isUnlocked) badgeColor else Color.Gray,
                        modifier = Modifier
                            .size(60.dp)
                            .alpha(if (badge.isUnlocked) 1f else 0.5f)
                    )
                    
                    // 未解锁状态显示锁定图标
                    if (!badge.isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "未解锁",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
            
            // 徽章标题
            Text(
                text = badge.title,
                color = if (badge.isUnlocked) badgeColor else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 徽章状态
            val statusText = if (badge.isUnlocked) "已解锁" else "未解锁"
            Text(
                text = statusText,
                color = if (badge.isUnlocked) 
                    MaterialTheme.colorScheme.primary 
                else 
                    Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (badge.isUnlocked) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                        else 
                            Color.Gray.copy(alpha = 0.1f)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 徽章描述
            AnimatedVisibility(
                visible = showDetails,
                enter = fadeIn() + expandVertically() + scaleIn(initialScale = 0.8f)
            ) {
                Text(
                    text = badge.description,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // 解锁日期
            if (badge.isUnlocked && badge.unlockedAt != null) {
                AnimatedVisibility(
                    visible = showDetails,
                    enter = fadeIn() + expandVertically()
                ) {
                    Text(
                        text = "解锁时间: ${formatDate(badge.unlockedAt, true)}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            
            // 关闭按钮
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDismiss() }
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "关闭",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 获取徽章颜色
 */
@Composable
private fun getBadgeColor(type: HabitBadgeType): Color {
    return when (type) {
        HabitBadgeType.STARTER -> Color(0xFF4CAF50)      // 绿色
        HabitBadgeType.PERSISTENT -> Color(0xFF2196F3)   // 蓝色
        HabitBadgeType.DEDICATED -> Color(0xFF9C27B0)    // 紫色
        HabitBadgeType.MASTER -> Color(0xFFFFD700)       // 金色
        HabitBadgeType.COMEBACK -> Color(0xFFFF9800)     // 橙色
        HabitBadgeType.CONSISTENT -> Color(0xFF3F51B5)   // 深蓝色
        HabitBadgeType.EARLY_BIRD -> Color(0xFFFF5722)   // 朱红色
        HabitBadgeType.NIGHT_OWL -> Color(0xFF673AB7)    // 深紫色
        HabitBadgeType.SOCIAL -> Color(0xFF00BCD4)       // 青色
        HabitBadgeType.MILESTONE -> Color(0xFFE91E63)    // 粉色
    }
}

/**
 * 获取徽章图标
 */
private fun getBadgeIcon(type: HabitBadgeType) = when (type) {
    HabitBadgeType.STARTER -> Icons.Default.EmojiEvents
    HabitBadgeType.PERSISTENT -> Icons.Default.Trending
    HabitBadgeType.DEDICATED -> Icons.Default.EmojiEvents
    HabitBadgeType.MASTER -> Icons.Default.EmojiEvents
    HabitBadgeType.COMEBACK -> Icons.Default.EmojiEvents
    HabitBadgeType.CONSISTENT -> Icons.Default.EmojiEvents
    HabitBadgeType.EARLY_BIRD -> Icons.Default.EmojiEvents
    HabitBadgeType.NIGHT_OWL -> Icons.Default.EmojiEvents
    HabitBadgeType.SOCIAL -> Icons.Default.EmojiEvents
    HabitBadgeType.MILESTONE -> Icons.Default.EmojiEvents
}

/**
 * 格式化日期
 */
private fun formatDate(date: Date?, showTime: Boolean = false): String {
    if (date == null) return ""
    val pattern = if (showTime) "yyyy-MM-dd HH:mm" else "yyyy-MM-dd"
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
} 