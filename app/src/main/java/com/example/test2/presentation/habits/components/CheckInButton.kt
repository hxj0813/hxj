package com.example.test2.presentation.habits.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.random.Random

/**
 * 有趣味性的习惯打卡按钮，支持不同状态的动画效果
 *
 * @param isCheckedIn 是否已打卡
 * @param currentStreak 当前连续天数
 * @param habitColor 习惯颜色
 * @param onCheckIn 打卡回调
 * @param onCancelCheckIn 取消打卡回调
 * @param modifier Modifier修饰符
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CheckInButton(
    isCheckedIn: Boolean,
    currentStreak: Int,
    habitColor: Color,
    onCheckIn: () -> Unit,
    onCancelCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 状态
    var showCompletionEffect by remember { mutableStateOf(false) }
    var showStreakAnimation by remember { mutableStateOf(false) }
    
    // 点击动画
    val scale by animateFloatAsState(
        targetValue = if (showCompletionEffect) 1.2f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing
        ),
        label = "ScaleAnimation"
    )
    
    // 光晕效果动画
    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAnimation"
    )
    
    // 脉动效果
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAnimation"
    )
    
    // 按钮效果处理
    LaunchedEffect(isCheckedIn) {
        if (isCheckedIn) {
            showCompletionEffect = true
            delay(400)
            showStreakAnimation = true
            delay(1500)
            showStreakAnimation = false
            showCompletionEffect = false
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        // 连续打卡天数显示
        AnimatedVisibility(
            visible = showStreakAnimation && currentStreak > 1,
            enter = slideInVertically(initialOffsetY = { -50 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -50 }) + fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(habitColor.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = habitColor,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "连续 $currentStreak 天",
                    color = habitColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 主打卡按钮
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                habitColor.copy(alpha = 0.7f),
                                habitColor
                            )
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(color = habitColor),
                    onClick = {
                        if (isCheckedIn) {
                            onCancelCheckIn()
                        } else {
                            onCheckIn()
                        }
                    }
                )
                .scale(if (!isCheckedIn) pulseScale else scale)
                .then(
                    if (!isCheckedIn) {
                        Modifier.drawBehind {
                            drawCircle(
                                color = habitColor.copy(alpha = glowAlpha * 0.3f),
                                radius = size.maxDimension / 1.7f
                            )
                        }
                    } else Modifier
                )
        ) {
            // 创建光芒效果
            if (showCompletionEffect) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val radius = size.minDimension / 2
                    
                    // 绘制动态光芒
                    for (i in 0 until 8) {
                        rotate(degrees = i * 45f) {
                            drawLine(
                                color = habitColor.copy(alpha = 0.8f),
                                start = Offset(center.x, center.y - radius * 0.7f),
                                end = Offset(center.x, center.y - radius * 1.3f),
                                strokeWidth = 4f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
            
            // 按钮内容动画
            AnimatedContent(
                targetState = isCheckedIn,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) + 
                    scaleIn(initialScale = 0.8f, animationSpec = tween(200)) with
                    fadeOut(animationSpec = tween(200)) + 
                    scaleOut(targetScale = 0.8f, animationSpec = tween(200))
                },
                label = "CheckButtonContent"
            ) { checked ->
                if (checked) {
                    // 已打卡状态
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(habitColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已打卡",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    // 未打卡状态
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Text(
                            text = "打卡",
                            color = habitColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 完成提示文本
        AnimatedVisibility(
            visible = showCompletionEffect,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = getRandomEncouragement(),
                color = habitColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 随机获取一条鼓励性文字
 */
private fun getRandomEncouragement(): String {
    val messages = listOf(
        "太棒了！坚持就是胜利！",
        "很好！今天的努力明天会看到效果！",
        "成功打卡！继续保持！",
        "恭喜你，又完成了一天！",
        "今天的坚持，明天的习惯！",
        "做得好！一步一个脚印！",
        "坚持的力量是无穷的！",
        "每一次打卡都是成长！",
        "你做到了！为自己点赞！"
    )
    
    // 根据时间段返回不同的消息
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "早安！${messages[Random.nextInt(messages.size)]}"
        hour < 18 -> "奋斗的一天！${messages[Random.nextInt(messages.size)]}"
        else -> "晚上好！${messages[Random.nextInt(messages.size)]}"
    }
} 