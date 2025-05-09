package com.example.test2.presentation.habits.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.test2.data.model.NoteMood

/**
 * 心情选择器组件
 * 
 * @param selectedMood 当前选中的心情
 * @param onMoodSelected 心情选择回调
 * @param modifier Modifier修饰符
 */
@Composable
fun MoodSelector(
    selectedMood: NoteMood,
    onMoodSelected: (NoteMood) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoodDialog by remember { mutableStateOf(false) }
    
    // 心情信息行（显示当前选中的心情）
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = getMoodColor(selectedMood).copy(alpha = 0.1f),
        modifier = modifier
            .fillMaxWidth()
            .clickable { showMoodDialog = true }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 心情图标
            Icon(
                imageVector = Icons.Default.Mood,
                contentDescription = "心情",
                tint = getMoodColor(selectedMood),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 心情文本
            Column {
                Text(
                    text = "当前心情",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Text(
                    text = getMoodText(selectedMood),
                    fontWeight = FontWeight.Medium,
                    color = getMoodColor(selectedMood)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 心情表情
            Text(
                text = getMoodEmoji(selectedMood),
                fontSize = 24.sp
            )
        }
    }
    
    // 心情选择对话框
    if (showMoodDialog) {
        MoodSelectionDialog(
            selectedMood = selectedMood,
            onMoodSelected = { 
                onMoodSelected(it)
                showMoodDialog = false
            },
            onDismiss = { showMoodDialog = false }
        )
    }
}

/**
 * 心情选择对话框
 */
@Composable
fun MoodSelectionDialog(
    selectedMood: NoteMood,
    onMoodSelected: (NoteMood) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 对话框标题
                Text(
                    text = "选择你的心情",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 心情选项
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteMood.values().forEach { mood ->
                        MoodOption(
                            mood = mood,
                            isSelected = mood == selectedMood,
                            onClick = { onMoodSelected(mood) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 关闭按钮
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onDismiss() }
                    ) {
                        Text(
                            text = "取消",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 心情选项组件
 */
@Composable
fun MoodOption(
    mood: NoteMood,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val moodColor = getMoodColor(mood)
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            moodColor.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "BackgroundColor"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Scale"
    )
    
    Surface(
        color = backgroundColor,
        contentColor = if (isSelected) moodColor else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            BorderStroke(1.dp, moodColor.copy(alpha = 0.5f))
        } else {
            null
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 心情文本
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 心情表情
                Text(
                    text = getMoodEmoji(mood),
                    fontSize = 24.sp
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 心情描述
                Text(
                    text = getMoodText(mood),
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
            
            // 选中指示器
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(moodColor)
                )
            }
        }
    }
}

/**
 * 获取心情对应的文本
 */
@Composable
fun getMoodText(mood: NoteMood): String {
    return when (mood) {
        NoteMood.VERY_HAPPY -> "非常开心"
        NoteMood.HAPPY -> "开心"
        NoteMood.NEUTRAL -> "平静"
        NoteMood.TIRED -> "疲惫"
        NoteMood.FRUSTRATED -> "挫折"
        NoteMood.SAD -> "难过"
        NoteMood.VERY_SAD -> "非常难过"
    }
}

/**
 * 获取心情对应的表情符号
 */
@Composable
fun getMoodEmoji(mood: NoteMood): String {
    return when (mood) {
        NoteMood.VERY_HAPPY -> "😄"
        NoteMood.HAPPY -> "😊"
        NoteMood.NEUTRAL -> "😐"
        NoteMood.TIRED -> "😴"
        NoteMood.FRUSTRATED -> "😤"
        NoteMood.SAD -> "😔"
        NoteMood.VERY_SAD -> "😢"
    }
}

/**
 * 获取心情对应的颜色
 */
@Composable
fun getMoodColor(mood: NoteMood): Color {
    return when (mood) {
        NoteMood.VERY_HAPPY -> Color(0xFF4CAF50)  // 绿色
        NoteMood.HAPPY -> Color(0xFF8BC34A)       // 浅绿色
        NoteMood.NEUTRAL -> Color(0xFF03A9F4)     // 蓝色
        NoteMood.TIRED -> Color(0xFF9E9E9E)       // 灰色
        NoteMood.FRUSTRATED -> Color(0xFFFF9800)  // 橙色
        NoteMood.SAD -> Color(0xFF9C27B0)         // 紫色
        NoteMood.VERY_SAD ->Color(0xFF3F51B5)     //靛蓝色
    }
} 