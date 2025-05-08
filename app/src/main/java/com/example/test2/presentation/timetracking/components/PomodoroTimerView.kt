package com.example.test2.presentation.timetracking.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.MoodBad
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 番茄钟计时视图
 * 
 * @param minutes 当前分钟数
 * @param seconds 当前秒数
 * @param totalTimeInSeconds 总计时时间（秒）
 * @param progress 当前进度 (0.0-1.0)
 * @param isRunning 是否正在计时
 * @param isPaused 是否暂停
 * @param isBreak 是否处于休息状态
 * @param currentSession 当前番茄钟会话数
 * @param totalSessions 总番茄钟会话数
 * @param onStart 开始按钮点击回调
 * @param onPause 暂停按钮点击回调
 * @param onStop 停止按钮点击回调
 * @param onSkipBreak 跳过休息按钮点击回调
 * @param onFinishBreak 结束休息按钮点击回调
 */
@Composable
fun PomodoroTimerView(
    minutes: Int,
    seconds: Int,
    totalTimeInSeconds: Int,
    progress: Float,
    isRunning: Boolean,
    isPaused: Boolean,
    isBreak: Boolean,
    currentSession: Int,
    totalSessions: Int,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSkipBreak: (() -> Unit)? = null,
    onFinishBreak: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    
    // 颜色定义
    val focusColor = Color(0xFFFF7F7F)  // 专注模式颜色（浅珊瑚色）
    val breakColor = Color(0xFF98FF98)  // 休息模式颜色（薄荷绿）
    
    val currentColor = if (isBreak) breakColor else focusColor
    val backgroundColor = if (isBreak) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    
    // 完成角度动画
    val animatedProgress = remember { Animatable(0f) }
    
    // 添加脉动动画效果
    val animatedSize = remember { Animatable(1f) }
    LaunchedEffect(isRunning) {
        if (isRunning) {
            animatedSize.animateTo(
                targetValue = 1f,
                animationSpec = tween(300, easing = LinearEasing)
            )
        }
    }
    
    // 添加计时器每秒的视觉反馈
    LaunchedEffect(minutes, seconds, isRunning) {
        if (isRunning) {
            animatedSize.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(150, easing = LinearEasing)
            )
            animatedSize.animateTo(
                targetValue = 1f,
                animationSpec = tween(150, easing = LinearEasing)
            )
        }
    }
    
    LaunchedEffect(progress) {
        coroutineScope.launch {
            animatedProgress.animateTo(
                targetValue = progress,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearEasing
                )
            )
        }
    }

    // 主容器
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 状态指示
        Text(
            text = if (isBreak) "休息时间" else "专注时间",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = currentColor
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 会话计数 - 改进显示效果
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(0.8f),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalSessions) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(
                                if (index == currentSession - 1) 16.dp else 12.dp
                            )
                            .clip(CircleShape)
                            .background(
                                if (index < currentSession) currentColor
                                else Color.LightGray.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 番茄钟计时器 - 添加大小动画
        Box(
            modifier = Modifier
                .size(280.dp * animatedSize.value)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // 底层圆环（背景）
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 20.dp.toPx()
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // 进度圆环 - 使用渐变色和光效
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 20.dp.toPx()
                val diameterOffset = strokeWidth / 2
                val arcDimen = size.width - 2 * diameterOffset
                
                // 绘制主进度
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            currentColor,
                            currentColor.copy(alpha = 0.7f)
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress.value * 360f,
                    useCenter = false,
                    topLeft = Offset(diameterOffset, diameterOffset),
                    size = Size(arcDimen, arcDimen),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // 添加进度末端的高光点
                if (animatedProgress.value > 0) {
                    val angle = (-90f + animatedProgress.value * 360f) * (PI / 180f)
                    val radius = (size.width - strokeWidth) / 2
                    val x = (size.width / 2 + cos(angle) * radius).toFloat()
                    val y = (size.height / 2 + sin(angle) * radius).toFloat()
                    
                    drawCircle(
                        color = Color.White,
                        radius = strokeWidth / 2,
                        center = Offset(x, y)
                    )
                }
            }
            
            // 内部填充
            Box(
                modifier = Modifier
                    .size(220.dp * animatedSize.value)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = 0.2f),
                                backgroundColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 时间文本
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentColor
                    )
                    
                    Text(
                        text = if (isBreak) "放松一下眼睛和身体" else "专注于当前任务",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 控制按钮 - 改进样式
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isBreak) {
                // 休息时的控制按钮
                Button(
                    onClick = { onSkipBreak?.invoke() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray.copy(alpha = 0.3f),
                        contentColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("跳过休息")
                }
                
                FloatingActionButton(
                    onClick = { onFinishBreak?.invoke() },
                    containerColor = breakColor,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Coffee,
                        contentDescription = "结束休息",
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else {
                // 专注时的控制按钮
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "停止",
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                FloatingActionButton(
                    onClick = if (isRunning && !isPaused) onPause else onStart,
                    containerColor = focusColor,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    ),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning && !isPaused) "暂停" else "开始",
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                IconButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                ) {
                    // 占位图标，保持布局对称
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 心情反馈（只在休息时显示）
        if (isBreak) {
            Text(
                text = "这次专注感觉如何？",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MoodButton(
                    icon = Icons.Rounded.MoodBad,
                    label = "困难",
                    color = Color(0xFFE57373),
                    onClick = {}
                )
                
                MoodButton(
                    icon = Icons.Rounded.SentimentSatisfied,
                    label = "一般",
                    color = Color(0xFFFFB74D),
                    onClick = {}
                )
                
                MoodButton(
                    icon = Icons.Rounded.SentimentVerySatisfied,
                    label = "很好",
                    color = Color(0xFF81C784),
                    onClick = {}
                )
            }
        }
    }
}

/**
 * 心情反馈按钮
 */
@Composable
private fun MoodButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
} 