package com.example.test2.presentation.timetracking.components

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.test2.data.local.entity.TagCategory
import com.example.test2.data.local.entity.TaskTagEntity
import java.util.UUID

/**
 * 标签对话框
 * 用于创建或编辑标签
 */
@Composable
fun TagDialog(
    tag: TaskTagEntity?,
    onDismiss: () -> Unit,
    onSave: (TaskTagEntity) -> Unit
) {
    // 预设颜色
    val colors = listOf(
        0xFF4CAF50, // 绿色
        0xFF2196F3, // 蓝色
        0xFFFF9800, // 橙色
        0xFF9C27B0, // 紫色
        0xFFE91E63, // 粉色
        0xFF3F51B5, // 靛蓝色
        0xFF607D8B, // 蓝灰色
        0xFFFF5722  // 深橙色
    )
    
    // 设置初始状态
    var name by remember { mutableStateOf(tag?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(tag?.getCategoryEnum() ?: TagCategory.OTHER) }
    var selectedColor by remember { mutableStateOf(tag?.color?.toLong() ?: colors[0]) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = if (tag == null) "创建新标签" else "编辑标签",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // 标签名称输入
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("标签名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标签分类选择
                Text(
                    text = "分类",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val categories = listOf(
                    TagCategory.WORK,
                    TagCategory.STUDY,
                    TagCategory.EXERCISE,
                    TagCategory.READING,
                    TagCategory.CREATIVE,
                    TagCategory.PERSONAL,
                    TagCategory.OTHER
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.forEach { category ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { selectedCategory = category }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedCategory == category) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            Color.LightGray
                                    )
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = getCategoryName(category),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selectedCategory == category) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            Color.Gray
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 颜色选择
                Text(
                    text = "颜色",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    width = if (selectedColor == color) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // 创建或更新标签
                    val updatedTag = if (tag == null) {
                        TaskTagEntity(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            category = selectedCategory.ordinal,
                            color = selectedColor.toInt(),
                            createdAt = java.util.Date()
                        )
                    } else {
                        tag.copy(
                            name = name,
                            category = selectedCategory.ordinal,
                            color = selectedColor.toInt()
                        )
                    }
                    
                    onSave(updatedTag)
                    onDismiss()
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (tag == null) "创建" else "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 获取分类名称
 */
private fun getCategoryName(category: TagCategory): String {
    return when (category) {
        TagCategory.WORK -> "工作"
        TagCategory.STUDY -> "学习" 
        TagCategory.EXERCISE -> "运动"
        TagCategory.READING -> "阅读"
        TagCategory.CREATIVE -> "创意"
        TagCategory.PERSONAL -> "个人"
        TagCategory.OTHER -> "其他"
        TagCategory.CUSTOM -> "自定义"
    }
} 