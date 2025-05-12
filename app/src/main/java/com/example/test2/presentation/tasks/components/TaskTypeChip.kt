package com.example.test2.presentation.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.model.PomodoroSettings
import com.example.test2.data.model.PomodoroTag
import com.example.test2.data.local.entity.TaskType
import com.example.test2.data.local.entity.TaskTagEntity

/**
 * 任务类型标签组件
 * 
 * @param taskType 任务类型
 * @param pomodoroSettings 番茄钟设置，仅当taskType为POMODORO时有效
 * @param tagEntity 任务标签实体，用于显示番茄钟任务的标签名
 * @param modifier Modifier修饰符
 */
@Composable
fun TaskTypeChip(
    taskType: TaskType,
    pomodoroSettings: PomodoroSettings? = null,
    tagEntity: TaskTagEntity? = null,
    modifier: Modifier = Modifier
) {
    // 根据任务类型确定显示内容
    val chipData = when (taskType) {
        TaskType.CHECK_IN -> {
            // 标签名称优先使用 TaskTagEntity 的名称
            val tagName = tagEntity?.name ?: "日常"
            
            ChipData(
                backgroundColor = Color(0xFFE8F5E9),  // 浅绿色
                contentColor = Color(0xFF4CAF50),     // 绿色
                icon = Icons.Default.CheckCircle,
                label = "打卡任务 - $tagName"
            )
        }
        TaskType.POMODORO -> {
            // 标签名称优先使用 TaskTagEntity 的名称
            val tagName = tagEntity?.name ?: 
                if (pomodoroSettings?.tag == PomodoroTag.CUSTOM && !pomodoroSettings.customTagName.isNullOrBlank())
                    pomodoroSettings.customTagName
                else 
                    pomodoroSettings?.tag?.getDisplayName() ?: "学习"
            
            ChipData(
                backgroundColor = Color(0xFFFFF3E0),  // 浅橙色
                contentColor = Color(0xFFFF9800),     // 橙色
                icon = Icons.Default.Timer,
                label = "番茄钟 - $tagName"
            )
        }
        else -> ChipData(
            backgroundColor = Color(0xFFE3F2FD),  // 浅蓝色
            contentColor = Color(0xFF1976D2),     // 蓝色
            icon = Icons.Outlined.Assignment,
            label = "其他任务"
        )
    }
    
    // 渲染标签
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = chipData.backgroundColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // 直接显示图标，不再显示标签颜色圆点
            Icon(
                imageVector = chipData.icon,
                contentDescription = null,
                tint = chipData.contentColor,
                modifier = Modifier.size(14.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = chipData.label,
                color = chipData.contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 标签数据类
 */
private data class ChipData(
    val backgroundColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val label: String
) 