package com.example.test2.presentation.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.test2.data.model.TaskPriority

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
    val color = when (priority) {
        TaskPriority.LOW -> Color(0xFF8BC34A)      // 浅绿色
        TaskPriority.MEDIUM -> Color(0xFF4FC3F7)   // 浅蓝色
        TaskPriority.HIGH -> Color(0xFFFF9800)     // 橙色
    }
    
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
    )
} 