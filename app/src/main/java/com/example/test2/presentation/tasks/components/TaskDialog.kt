package com.example.test2.presentation.tasks.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.test2.data.model.Goal
import com.example.test2.data.model.Task
import com.example.test2.data.model.TaskPriority
import com.example.test2.data.model.TaskStatus
import com.example.test2.presentation.theme.PrimaryDark
import com.example.test2.presentation.theme.PrimaryLight
import com.example.test2.util.DateTimeUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 任务添加/编辑对话框
 *
 * @param task 要编辑的任务，如果为null则为添加新任务
 * @param goals 可关联的目标列表
 * @param onDismiss 取消回调
 * @param onSave 保存回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    task: Task? = null,
    goals: List<Goal> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    // 表单状态
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) } // 默认明天
    var selectedGoalId by remember { mutableStateOf(task?.goalId) }
    
    // 日期格式化器
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }
    
    // 优先级下拉菜单状态
    var priorityMenuExpanded by remember { mutableStateOf(false) }
    
    // 目标下拉菜单状态
    var goalMenuExpanded by remember { mutableStateOf(false) }
    
    // 日期选择器展开状态
    var datePickerExpanded by remember { mutableStateOf(false) }
    
    // 表单验证
    val isTitleValid = title.isNotBlank()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
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
                        text = if (task == null) "添加新任务" else "编辑任务",
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
                    label = { Text("任务标题") },
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
                    label = { Text("任务描述 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 截止日期选择
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerExpanded = !datePickerExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "截止日期",
                        tint = PrimaryLight,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "截止日期: ${dateFormatter.format(dueDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Icon(
                        imageVector = if (datePickerExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (datePickerExpanded) "收起" else "展开",
                        tint = Color.Gray
                    )
                }
                
                // 简单的日期选择器（可以替换为更完善的日期选择器组件）
                AnimatedVisibility(
                    visible = datePickerExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        val calendar = Calendar.getInstance().apply {
                            time = dueDate
                        }
                        
                        // 预设选项
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DateOption(
                                text = "今天",
                                onClick = {
                                    dueDate = Calendar.getInstance().time
                                    datePickerExpanded = false
                                }
                            )
                            
                            DateOption(
                                text = "明天",
                                onClick = {
                                    val tomorrow = Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_MONTH, 1)
                                    }
                                    dueDate = tomorrow.time
                                    datePickerExpanded = false
                                }
                            )
                            
                            DateOption(
                                text = "后天",
                                onClick = {
                                    val dayAfterTomorrow = Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_MONTH, 2)
                                    }
                                    dueDate = dayAfterTomorrow.time
                                    datePickerExpanded = false
                                }
                            )
                            
                            DateOption(
                                text = "下周",
                                onClick = {
                                    val nextWeek = Calendar.getInstance().apply {
                                        add(Calendar.WEEK_OF_YEAR, 1)
                                    }
                                    dueDate = nextWeek.time
                                    datePickerExpanded = false
                                }
                            )
                        }
                        
                        // 这里可以添加更多日期选择器功能
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 优先级选择
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { priorityMenuExpanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriorityIndicator(
                        priority = priority,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "优先级: ${
                            when(priority) {
                                TaskPriority.LOW -> "低"
                                TaskPriority.MEDIUM -> "中"
                                TaskPriority.HIGH -> "高"
                            }
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "选择优先级",
                        tint = Color.Gray
                    )
                    
                    // 优先级下拉菜单
                    DropdownMenu(
                        expanded = priorityMenuExpanded,
                        onDismissRequest = { priorityMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PriorityIndicator(
                                        priority = TaskPriority.LOW,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("低优先级") 
                                }
                            },
                            onClick = { 
                                priority = TaskPriority.LOW
                                priorityMenuExpanded = false
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PriorityIndicator(
                                        priority = TaskPriority.MEDIUM,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("中优先级") 
                                }
                            },
                            onClick = { 
                                priority = TaskPriority.MEDIUM
                                priorityMenuExpanded = false
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PriorityIndicator(
                                        priority = TaskPriority.HIGH,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("高优先级") 
                                }
                            },
                            onClick = { 
                                priority = TaskPriority.HIGH
                                priorityMenuExpanded = false
                            }
                        )
                    }
                }
                
                // 如果有可用目标，显示目标选择
                if (goals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { goalMenuExpanded = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "关联目标: ${goals.find { it.id == selectedGoalId }?.title ?: "无"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "选择目标",
                            tint = Color.Gray
                        )
                        
                        // 目标下拉菜单
                        DropdownMenu(
                            expanded = goalMenuExpanded,
                            onDismissRequest = { goalMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("无") },
                                onClick = { 
                                    selectedGoalId = null
                                    goalMenuExpanded = false
                                }
                            )
                            
                            goals.forEach { goal ->
                                DropdownMenuItem(
                                    text = { Text(goal.title) },
                                    onClick = { 
                                        selectedGoalId = goal.id
                                        goalMenuExpanded = false
                                    }
                                )
                            }
                        }
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
                            if (isTitleValid) {
                                val updatedTask = Task(
                                    id = task?.id ?: 0,
                                    title = title,
                                    description = description,
                                    priority = priority,
                                    dueDate = dueDate,
                                    goalId = selectedGoalId,
                                    isCompleted = task?.isCompleted ?: false,
                                    createdAt = task?.createdAt ?: Date(),
                                    updatedAt = Date()
                                )
                                onSave(updatedTask)
                            }
                        },
                        enabled = isTitleValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryLight
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(if (task == null) "添加" else "保存")
                    }
                }
            }
        }
    }
}

/**
 * 日期选项按钮
 */
@Composable
private fun DateOption(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = Color(0xFFE3F2FD)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryDark,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
} 