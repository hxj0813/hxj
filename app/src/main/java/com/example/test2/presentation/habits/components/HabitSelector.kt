package com.example.test2.presentation.habits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.test2.data.model.Habit

/**
 * 习惯选择器组件
 * 用于在笔记界面快速筛选特定习惯的笔记
 *
 * @param habits 习惯列表
 * @param selectedHabitId 当前选择的习惯ID
 * @param onHabitSelected 习惯选择回调
 * @param modifier Modifier修饰符
 */
@Composable
fun HabitSelector(
    habits: List<Habit>,
    selectedHabitId: String?,
    onHabitSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "全部"选项
        HabitChip(
            title = "全部笔记",
            isSelected = selectedHabitId == null || selectedHabitId.isEmpty(),
            onClick = { onHabitSelected(null) }
        )

        // 各个习惯选项
        habits.forEach { habit ->
            HabitChip(
                title = habit.name,
                isSelected = selectedHabitId == habit.id,
                onClick = { onHabitSelected(habit.id) },
                color = habit.color
            )
        }

        // 右侧间距
        Spacer(modifier = Modifier.width(8.dp))
    }
}

/**
 * 习惯筛选芯片
 *
 * @param title 标题
 * @param isSelected 是否选中
 * @param onClick 点击回调
 * @param color 颜色（可选）
 */
@Composable
fun HabitChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Int? = null
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        color != null -> androidx.compose.ui.graphics.Color(color).copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        color != null -> androidx.compose.ui.graphics.Color(color)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        color != null -> androidx.compose.ui.graphics.Color(color).copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
} 