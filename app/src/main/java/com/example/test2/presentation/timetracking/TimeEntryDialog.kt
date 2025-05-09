package com.example.test2.presentation.timetracking

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.test2.presentation.timetracking.TimeTrackingUtils

/**
 * 时间条目对话框
 */
@Composable
fun TimeEntryDialog(
    timeEntry: TimeEntry?,
    onDismiss: () -> Unit,
    onSave: (TimeEntry) -> Unit
) {
    val isNewEntry = timeEntry == null
    
    // 记录表单状态
    var title by remember { mutableStateOf(timeEntry?.title ?: "") }
    var description by remember { mutableStateOf(timeEntry?.description ?: "") }
    var startDate by remember { mutableStateOf(timeEntry?.startTime ?: Date()) }
    var endDate by remember { mutableStateOf(timeEntry?.endTime) }
    var category by remember { mutableStateOf(timeEntry?.category ?: TimeCategory.WORK) }
    var tags by remember { mutableStateOf(timeEntry?.tags ?: emptyList<String>()) }
    
    // 对话框UI状态
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showTagEditor by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNewEntry) "添加时间条目" else "编辑时间条目",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    placeholder = { Text("输入活动标题") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Title,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    placeholder = { Text("输入活动描述（可选）") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null
                        )
                    },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 分类选择
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = TimeTrackingUtils.getCategoryName(category),
                        onValueChange = { },
                        label = { Text("分类") },
                        readOnly = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "选择分类"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDropdown = true }
                    )
                    
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        TimeCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(TimeTrackingUtils.getCategoryName(cat)) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TimeTrackingUtils.getCategoryColor(cat), RoundedCornerShape(4.dp))
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = TimeTrackingUtils.getCategoryName(cat)[0].toString(),
                                            color = TimeTrackingUtils.getCategoryColor(cat)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 开始时间选择
                OutlinedTextField(
                    value = formatDate(startDate),
                    onValueChange = { },
                    label = { Text("开始时间") },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "选择日期"
                                )
                            }
                            
                            IconButton(onClick = { showStartTimePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "选择时间"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 结束时间选择（对于已完成的时间条目）
                OutlinedTextField(
                    value = if (endDate != null) formatDate(endDate!!) else "进行中",
                    onValueChange = { },
                    label = { Text("结束时间") },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "选择日期"
                                )
                            }
                            
                            IconButton(onClick = { showEndTimePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "选择时间"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 保存按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            // 创建时间条目对象
                            val entry = TimeEntry(
                                id = timeEntry?.id ?: 0L,
                                title = title.trim(),
                                description = description.trim(),
                                category = category,
                                startTime = startDate,
                                endTime = endDate,
                                tags = tags
                            )
                            
                            onSave(entry)
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
    
    // 显示日期选择器
    if (showStartDatePicker) {
        ShowDatePicker(
            initialDate = startDate,
            onDateSelected = { date ->
                // 保留原时间部分
                startDate = combineDateAndTime(date, startDate)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    
    // 显示时间选择器
    if (showStartTimePicker) {
        ShowTimePicker(
            initialTime = startDate,
            onTimeSelected = { hours, minutes ->
                // 更新时间，保留原日期部分
                val calendar = Calendar.getInstance()
                calendar.time = startDate
                calendar.set(Calendar.HOUR_OF_DAY, hours)
                calendar.set(Calendar.MINUTE, minutes)
                startDate = calendar.time
                
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }
    
    // 结束日期选择器
    if (showEndDatePicker) {
        val currentEndDate = endDate ?: Date()
        
        ShowDatePicker(
            initialDate = currentEndDate,
            onDateSelected = { date ->
                // 保留原时间部分
                endDate = combineDateAndTime(date, currentEndDate)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
    
    // 结束时间选择器
    if (showEndTimePicker) {
        val currentEndDate = endDate ?: Date()
        
        ShowTimePicker(
            initialTime = currentEndDate,
            onTimeSelected = { hours, minutes ->
                // 更新时间，保留原日期部分
                val calendar = Calendar.getInstance()
                calendar.time = currentEndDate
                calendar.set(Calendar.HOUR_OF_DAY, hours)
                calendar.set(Calendar.MINUTE, minutes)
                endDate = calendar.time
                
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

/**
 * 日期选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowDatePicker(
    initialDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = initialDate
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedDateMillis = datePickerState.selectedDateMillis
                if (selectedDateMillis != null) {
                    val selectedDate = Date(selectedDateMillis)
                    onDateSelected(selectedDate)
                }
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/**
 * 时间选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowTimePicker(
    initialTime: Date,
    onTimeSelected: (hours: Int, minutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = initialTime
    
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)
    
    val timePickerState = rememberTimePickerState(
        initialHour = hours,
        initialMinute = minutes
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "选择时间",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    TextButton(
                        onClick = {
                            onTimeSelected(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

/**
 * 格式化日期时间
 */
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return formatter.format(date)
}

/**
 * 合并日期和时间
 */
private fun combineDateAndTime(date: Date, time: Date): Date {
    val dateCalendar = Calendar.getInstance()
    dateCalendar.time = date
    
    val timeCalendar = Calendar.getInstance()
    timeCalendar.time = time
    
    dateCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
    dateCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
    dateCalendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND))
    
    return dateCalendar.time
} 