package com.example.test2.presentation.habits.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.min

/**
 * 生动形象的习惯进度条组件
 *
 * @param progress 当前进度 (0.0f - 1.0f)
 * @param targetDays 目标天数
 * @param currentDays 当前已完成天数
 * @param habitColor 习惯颜色
 * @param showMilestones 是否显示里程碑
 * @param modifier Modifier修饰符
 */
@Composable
fun HabitProgressBar(
    progress: Float,
    targetDays: Int,
    currentDays: Int,
    habitColor: Color,
    showMilestones: Boolean = true,
    modifier: Modifier = Modifier
) {
    // 动画进度值
    val animatedProgress = remember { Animatable(initialValue = 0f) }
    
    // 是否显示庆祝动画
    var showCelebration by remember { mutableStateOf(false) }
    
    // 里程碑点位置
    val milestones = remember {
        if (targetDays >= 21) {
            listOf(0.25f, 0.5f, 0.75f, 1.0f)
        } else {
            listOf(0.5f, 1.0f)
        }
    }
    
    // 启动进度动画
    LaunchedEffect(progress) {
        // 是否达成新的进度里程碑
        val previousMilestoneReached = milestones.any { animatedProgress.value >= it }
        val currentMilestoneReached = milestones.any { progress >= it && animatedProgress.value < it }
        
        // 如果达成新的里程碑且不是初始状态，显示庆祝动画
        if (currentMilestoneReached && !previousMilestoneReached && animatedProgress.value > 0) {
            showCelebration = true
        }
        
        // 动画更新进度值
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
        
        // 如果正在显示庆祝动画，等一段时间后关闭
        if (showCelebration) {
            delay(3000)
            showCelebration = false
        }
    }
    
    // 进度状态文本
    val progressText = when {
        progress >= 1.0f -> "恭喜你已完成目标！"
        progress >= 0.75f -> "即将完成目标，继续加油！"
        progress >= 0.5f -> "已完成一半，坚持下去！"
        progress >= 0.25f -> "良好的开始，保持节奏！"
        progress > 0f -> "刚刚开始，加油！"
        else -> "开始你的习惯旅程吧！"
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // 进度数值展示
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 当前天数/目标天数
            Text(
                text = "$currentDays/$targetDays 天",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            
            // 百分比
            Text(
                text = "${(progress * 100).toInt()}%",
                color = habitColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 进度条
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(habitColor.copy(alpha = 0.1f))
        ) {
            // 进度填充
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                habitColor.copy(alpha = 0.8f),
                                habitColor
                            )
                        )
                    )
            )
            
            // 里程碑标记
            if (showMilestones) {
                milestones.forEach { milestone ->
                    if (milestone < 1.0f) { // 不在终点显示里程碑点
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(milestone)
                                .align(Alignment.CenterStart)
                        ) {
                            // 里程碑点
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            }
            
            // 如果进度已达到目标，显示奖杯图标
            if (animatedProgress.value >= 1.0f) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "目标完成",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(18.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 根据进度显示不同的鼓励文字
        Text(
            text = progressText,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(if (showCelebration) 16.dp else 0.dp))
        
        // 庆祝动画
        if (showCelebration) {
            ProgressCelebration(
                habitColor = habitColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )
        }
    }
}

/**
 * 进度达成里程碑时的庆祝动画
 */
@Composable
fun ProgressCelebration(
    habitColor: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // 多个动画参数
    val conf1OffsetY = remember { Animatable(-20f) }
    val conf2OffsetY = remember { Animatable(-30f) }
    val conf3OffsetY = remember { Animatable(-40f) }
    val conf1Alpha = remember { Animatable(1f) }
    val conf2Alpha = remember { Animatable(1f) }
    val conf3Alpha = remember { Animatable(1f) }
    
    // 启动动画
    LaunchedEffect(Unit) {
        // 第一组
        conf1OffsetY.animateTo(
            targetValue = 10f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )
        conf1Alpha.animateTo(0f, animationSpec = tween(300))
        
        // 第二组
        conf2OffsetY.animateTo(
            targetValue = 20f,
            animationSpec = tween(1800, easing = FastOutSlowInEasing)
        )
        conf2Alpha.animateTo(0f, animationSpec = tween(300))
        
        // 第三组
        conf3OffsetY.animateTo(
            targetValue = 30f,
            animationSpec = tween(2100, easing = FastOutSlowInEasing)
        )
        conf3Alpha.animateTo(0f, animationSpec = tween(300))
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // 绘制庆祝动画效果
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val width = size.width
            val height = size.height
            
            // 第一组五彩纸屑
            drawConfetti(
                offsetX = width * 0.3f,
                offsetY = conf1OffsetY.value,
                alpha = conf1Alpha.value,
                color1 = habitColor,
                color2 = Color.Yellow
            )
            
            // 第二组五彩纸屑
            drawConfetti(
                offsetX = width * 0.5f,
                offsetY = conf2OffsetY.value,
                alpha = conf2Alpha.value,
                color1 = Color.Magenta,
                color2 = habitColor.copy(alpha = 0.7f)
            )
            
            // 第三组五彩纸屑
            drawConfetti(
                offsetX = width * 0.7f,
                offsetY = conf3OffsetY.value,
                alpha = conf3Alpha.value,
                color1 = Color.Cyan,
                color2 = Color.Green
            )
        }
        
        // 进度里程碑文本
        Text(
            text = "✨ 里程碑达成 ✨",
            color = habitColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 在画布上绘制五彩纸屑
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawConfetti(
    offsetX: Float,
    offsetY: Float,
    alpha: Float,
    color1: Color,
    color2: Color
) {
    val size = 8.dp.toPx()
    
    // 绘制多种形状的五彩纸屑
    translate(left = offsetX, top = offsetY) {
        // 圆形纸屑
        drawCircle(
            color = color1.copy(alpha = alpha),
            radius = size / 2,
            center = Offset(0f, 0f)
        )
        
        // 矩形纸屑
        drawRect(
            color = color2.copy(alpha = alpha),
            topLeft = Offset(size, -size),
            size = Size(size, size / 2)
        )
        
        // 三角形纸屑
        val trianglePath = Path().apply {
            moveTo(-size, size)
            lineTo(-size / 2, 0f)
            lineTo(0f, size)
            close()
        }
        drawPath(
            path = trianglePath,
            color = color1.copy(alpha = alpha)
        )
        
        // 线条纸屑
        drawLine(
            color = color2.copy(alpha = alpha),
            start = Offset(size, size),
            end = Offset(size * 2, 0f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
} 