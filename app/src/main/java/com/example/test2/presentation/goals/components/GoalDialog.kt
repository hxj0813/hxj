package com.example.test2.presentation.goals.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.test2.data.model.Goal
import com.example.test2.presentation.theme.AccentPink
import com.example.test2.presentation.theme.PrimaryDark
import com.example.test2.presentation.theme.PrimaryLight
import com.example.test2.util.DateTimeUtil
import java.util.Date

/**
 * 目标添加/编辑对话框
 *
 * @param goal 要编辑的目标，如果为null则为添加新目标
 * @param onDismiss 取消回调
 * @param onSave 保存回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDialog(
    goal: Goal? = null,
    onDismiss: () -> Unit,
    onSave: (Goal) -> Unit
) {
    // 目标表单状态
    var title by remember { mutableStateOf(goal?.title ?: "") }
    var description by remember { mutableStateOf(goal?.description ?: "") }
    var isLongTerm by remember { mutableStateOf(goal?.isLongTerm ?: false) }
    var isImportant by remember { mutableStateOf(goal?.isImportant ?: false) }
    var progress by remember { mutableStateOf(goal?.progress ?: 0f) }
    var deadline by remember { mutableStateOf(goal?.deadline ?: Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) } // 默认一周后
    
    // 表单验证
    val isTitleValid = title.isNotBlank()
    val isDescriptionValid = description.isNotBlank()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 对话框标题和关闭按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (goal == null) "添加新目标" else "编辑目标",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("目标标题") },
                    isError = title.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                
                if (title.isBlank()) {
                    Text(
                        text = "标题不能为空",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("目标描述") },
                    isError = description.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                
                if (description.isBlank()) {
                    Text(
                        text = "描述不能为空",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 截止日期选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "截止日期",
                        tint = PrimaryLight,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    
                    Text(
                        text = "截止日期: ${DateTimeUtil.formatDate(deadline)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // 这里可以添加日期选择器，但为简化代码，暂不实现
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(onClick = { /* 显示日期选择器 */ }) {
                        Text("更改")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 目标类型选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "长期目标",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Switch(
                        checked = isLongTerm,
                        onCheckedChange = { isLongTerm = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 重要性标记
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "标记为重要",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(
                        onClick = { isImportant = !isImportant },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isImportant) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = if (isImportant) "取消重要标记" else "标记为重要",
                            tint = if (isImportant) AccentPink else Color.Gray
                        )
                    }
                }
                
                // 如果是编辑模式，显示进度滑块
                AnimatedVisibility(
                    visible = goal != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "完成进度: ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Slider(
                            value = progress,
                            onValueChange = { progress = it },
                            valueRange = 0f..1f,
                            steps = 20,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮区
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            if (isTitleValid && isDescriptionValid) {
                                val newGoal = Goal(
                                    id = goal?.id ?: 0,
                                    title = title,
                                    description = description,
                                    isLongTerm = isLongTerm,
                                    isImportant = isImportant,
                                    progress = progress,
                                    deadline = deadline,
                                    isCompleted = goal?.isCompleted ?: false,
                                    createdAt = goal?.createdAt ?: Date(),
                                    updatedAt = Date()
                                )
                                onSave(newGoal)
                            }
                        },
                        enabled = isTitleValid && isDescriptionValid,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(if (goal == null) "添加" else "保存")
                    }
                }
            }
        }
    }
} 