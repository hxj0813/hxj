package com.example.test2.presentation.timetracking.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.Dialog
import com.example.test2.data.model.Task
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 时间条目对话框，用于添加或编辑时间追踪记录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeEntryDialog(
    title: String,
    initialTimeEntry: TimeEntry? = null,
    tasks: List<Task> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (TimeEntry) -> Unit
) {
    // 状态
    var titleText by remember { mutableStateOf(initialTimeEntry?.title ?: "") }
    var description by remember { mutableStateOf(initialTimeEntry?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(initialTimeEntry?.category ?: TimeCategory.WORK) }
    var selectedTaskId by remember { mutableStateOf(initialTimeEntry?.taskId) }
    
    // 日期和时间
    val calendar = Calendar.getInstance()
    
    // 开始时间
    val initialStartDate = initialTimeEntry?.startTime ?: Date()
    calendar.time = initialStartDate
    val startYear = calendar.get(Calendar.YEAR)
    val startMonth = calendar.get(Calendar.MONTH)
    val startDay = calendar.get(Calendar.DAY_OF_MONTH)
    val startHour = calendar.get(Calendar.HOUR_OF_DAY)
    val startMinute = calendar.get(Calendar.MINUTE)
    
    var startDate by remember { mutableStateOf(initialStartDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    
    val startTimePickerState = rememberTimePickerState(
        initialHour = startHour, 
        initialMinute = startMinute
    )
    
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialStartDate.time
    )
    
    // 结束时间
    val initialEndDate = initialTimeEntry?.endTime
    var endDate by remember { mutableStateOf(initialEndDate) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    var endTimePickerState: TimePickerState? = null
    var endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialEndDate?.time ?: System.currentTimeMillis()
    )
    
    if (initialEndDate != null) {
        calendar.time = initialEndDate
        val endHour = calendar.get(Calendar.HOUR_OF_DAY)
        val endMinute = calendar.get(Calendar.MINUTE)
        endTimePickerState = rememberTimePickerState(
            initialHour = endHour, 
            initialMinute = endMinute
        )
    } else {
        endTimePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY), 
            initialMinute = calendar.get(Calendar.MINUTE)
        )
    }
    
    // 下拉菜单状态
    var expandedCategoryMenu by remember { mutableStateOf(false) }
    var expandedTaskMenu by remember { mutableStateOf(false) }
    
    // 日期格式化工具
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标题输入
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述 (可选)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 分类选择
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryMenu,
                    onExpandedChange = { expandedCategoryMenu = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = getCategoryName(selectedCategory),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("分类") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryMenu)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedCategoryMenu,
                        onDismissRequest = { expandedCategoryMenu = false }
                    ) {
                        TimeCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val (color, _) = getCategoryColors(category)
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(getCategoryName(category))
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategoryMenu = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 任务选择（可选）
                if (tasks.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedTaskMenu,
                        onExpandedChange = { expandedTaskMenu = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedTaskId?.let { id ->
                                tasks.find { it.id == id }?.title ?: "选择关联任务"
                            } ?: "选择关联任务 (可选)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("关联任务") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTaskMenu)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expandedTaskMenu,
                            onDismissRequest = { expandedTaskMenu = false }
                        ) {
                            // 添加一个"无"选项
                            DropdownMenuItem(
                                text = { Text("无") },
                                onClick = {
                                    selectedTaskId = null
                                    expandedTaskMenu = false
                                }
                            )
                            
                            tasks.forEach { task ->
                                DropdownMenuItem(
                                    text = { Text(task.title) },
                                    onClick = {
                                        selectedTaskId = task.id
                                        expandedTaskMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // 时间选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 开始时间
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "开始时间",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 开始日期
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable { showStartDatePicker = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = dateFormatter.format(startDate),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 开始时间
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable { showStartTimePicker = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = timeFormatter.format(startDate),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 结束时间
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "结束时间 (可选)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 结束日期
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable { showEndDatePicker = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = endDate?.let { dateFormatter.format(it) } ?: "选择日期",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 结束时间
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable { showEndTimePicker = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = endDate?.let { timeFormatter.format(it) } ?: "选择时间",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 保存按钮
                Button(
                    onClick = {
                        // 计算持续时间（如果有结束时间）
                        val duration = if (endDate != null && startDate.before(endDate)) {
                            (endDate!!.time - startDate.time) / 1000
                        } else {
                            0L
                        }
                        
                        val timeEntry = TimeEntry(
                            id = initialTimeEntry?.id,
                            title = titleText,
                            description = description.takeIf { it.isNotBlank() },
                            category = selectedCategory,
                            startTime = startDate,
                            endTime = endDate,
                            duration = duration,
                            taskId = selectedTaskId,
                            createdAt = initialTimeEntry?.createdAt ?: Date()
                        )
                        
                        onSave(timeEntry)
                    },
                    enabled = titleText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存")
                }
            }
        }
    }
    
    // 日期选择器对话框
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        // 保留原来的时间，只更新日期
                        calendar.time = startDate
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(Calendar.MINUTE)
                        
                        calendar.timeInMillis = millis
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        
                        startDate = calendar.time
                    }
                    showStartDatePicker = false
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }
    
    // 时间选择器对话框
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // 保留原来的日期，只更新时间
                    calendar.time = startDate
                    calendar.set(Calendar.HOUR_OF_DAY, startTimePickerState.hour)
                    calendar.set(Calendar.MINUTE, startTimePickerState.minute)
                    
                    startDate = calendar.time
                    showStartTimePicker = false
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            TimePicker(state = startTimePickerState)
        }
    }
    
    // 结束日期选择器对话框
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let { millis ->
                        // 获取当前的结束时间，如果有的话
                        val currentEndDate = endDate
                        
                        // 保留原来的时间，只更新日期
                        calendar.timeInMillis = millis
                        
                        if (currentEndDate != null) {
                            val tempCalendar = Calendar.getInstance()
                            tempCalendar.time = currentEndDate
                            val hour = tempCalendar.get(Calendar.HOUR_OF_DAY)
                            val minute = tempCalendar.get(Calendar.MINUTE)
                            
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                        } else {
                            // 如果之前没有结束时间，设置当前时间
                            val now = Calendar.getInstance()
                            calendar.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                            calendar.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                        }
                        
                        endDate = calendar.time
                    }
                    showEndDatePicker = false
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
    
    // 结束时间选择器对话框
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // 获取当前的结束日期，如果有的话
                    val currentEndDate = endDate
                    
                    if (currentEndDate != null) {
                        // 保留原来的日期，只更新时间
                        calendar.time = currentEndDate
                    } else {
                        // 如果之前没有结束时间，使用当前日期
                        calendar.time = Date()
                    }
                    
                    calendar.set(Calendar.HOUR_OF_DAY, endTimePickerState!!.hour)
                    calendar.set(Calendar.MINUTE, endTimePickerState!!.minute)
                    
                    endDate = calendar.time
                    showEndTimePicker = false
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            TimePicker(state = endTimePickerState!!)
        }
    }
}

/**
 * 时间选择器对话框
 */
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
} 