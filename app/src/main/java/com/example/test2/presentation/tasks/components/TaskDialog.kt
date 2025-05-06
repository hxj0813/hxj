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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.test2.data.model.CheckInFrequencyType
import com.example.test2.data.model.CheckInSettings
import com.example.test2.data.model.Goal
import com.example.test2.data.model.PomodoroSettings
import com.example.test2.data.model.PomodoroTag
import com.example.test2.data.model.Task
import com.example.test2.data.model.TaskPriority
import com.example.test2.data.model.TaskStatus
import com.example.test2.data.model.TaskType
import com.example.test2.presentation.theme.PrimaryDark
import com.example.test2.presentation.theme.PrimaryLight
import com.example.test2.util.DateTimeUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.test2.presentation.tasks.components.PriorityIndicator

/**
 * 任务添加/编辑对话框
 *
 * @param task 要编辑的任务，如果为null则为添加新任务
 * @param goals 可关联的目标列表
 * @param habits 可关联的习惯列表
 * @param onDismiss 取消回调
 * @param onSave 保存回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    task: Task? = null,
    goals: List<Goal> = emptyList(),
    habits: List<Any> = emptyList(), // 这里应该使用习惯模型，暂时用Any代替
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    // 表单状态
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var taskType by remember { mutableStateOf(task?.type ?: TaskType.NORMAL) }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) } // 默认明天
    var selectedGoalId by remember { mutableStateOf(task?.goalId) }
    var selectedHabitId by remember { mutableStateOf(task?.habitId) }
    
    // 打卡任务设置
    var checkInFrequencyType by remember { 
        mutableStateOf(task?.checkInSettings?.frequencyType ?: CheckInFrequencyType.DAILY) 
    }
    var checkInFrequency by remember { mutableStateOf(task?.checkInSettings?.frequency ?: 1) }
    var hasDailyDeadline by remember { mutableStateOf(task?.checkInSettings?.dailyDeadline != null) }
    var dailyDeadlineHour by remember { mutableStateOf(task?.checkInSettings?.dailyDeadline?.let {
        val cal = Calendar.getInstance().apply { time = it }
        cal.get(Calendar.HOUR_OF_DAY)
    } ?: 8) }
    var dailyDeadlineMinute by remember { mutableStateOf(task?.checkInSettings?.dailyDeadline?.let {
        val cal = Calendar.getInstance().apply { time = it }
        cal.get(Calendar.MINUTE)
    } ?: 0) }
    
    // 番茄钟任务设置
    var focusMinutes by remember { mutableStateOf(task?.pomodoroSettings?.focusMinutes ?: 25) }
    var shortBreakMinutes by remember { mutableStateOf(task?.pomodoroSettings?.shortBreakMinutes ?: 5) }
    var longBreakMinutes by remember { mutableStateOf(task?.pomodoroSettings?.longBreakMinutes ?: 15) }
    var sessionsBeforeLongBreak by remember { 
        mutableStateOf(task?.pomodoroSettings?.sessionsBeforeLongBreak ?: 4) 
    }
    var pomodoroTag by remember { 
        mutableStateOf(task?.pomodoroSettings?.tag ?: PomodoroTag.STUDY) 
    }
    var customTagName by remember { 
        mutableStateOf(task?.pomodoroSettings?.customTagName ?: "") 
    }
    var tagMenuExpanded by remember { mutableStateOf(false) }
    
    // 日期格式化器
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    // 各种下拉菜单状态
    var priorityMenuExpanded by remember { mutableStateOf(false) }
    var goalMenuExpanded by remember { mutableStateOf(false) }
    var habitMenuExpanded by remember { mutableStateOf(false) }
    var datePickerExpanded by remember { mutableStateOf(false) }
    var timePickerExpanded by remember { mutableStateOf(false) }
    
    // 表单验证
    val isTitleValid = title.isNotBlank()
    
    // 滚动状态
    val scrollState = rememberScrollState()
    
    // 时间选择器状态
    val timePickerState = rememberTimePickerState(
        initialHour = dailyDeadlineHour,
        initialMinute = dailyDeadlineMinute,
        is24Hour = true
    )
    
    // 当时间选择器状态变化时，更新时间
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        dailyDeadlineHour = timePickerState.hour
        dailyDeadlineMinute = timePickerState.minute
    }
    
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
                    .verticalScroll(scrollState)
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
                
                // 任务类型选择
                Text(
                    text = "任务类型",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = taskType == TaskType.NORMAL,
                        onClick = { taskType = TaskType.NORMAL },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        label = { Text("普通") }
                    )
                    SegmentedButton(
                        selected = taskType == TaskType.CHECK_IN,
                        onClick = { taskType = TaskType.CHECK_IN },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        label = { Text("打卡") }
                    )
                    SegmentedButton(
                        selected = taskType == TaskType.POMODORO,
                        onClick = { taskType = TaskType.POMODORO },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        label = { Text("番茄钟") }
                    )
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
                    value = description ?: "",
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
                
                // 日期选择器
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
                
                // 如果有可用习惯，显示习惯选择
                if (habits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { habitMenuExpanded = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "关联习惯",
                            tint = PrimaryLight,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val habitTitle = if (selectedHabitId != null) {
                            val selectedHabit = habits.find { 
                                val habitMap = it as? Map<*, *>
                                habitMap?.get("id")?.toString() == selectedHabitId 
                            }
                            if (selectedHabit != null) {
                                val habitMap = selectedHabit as? Map<*, *>
                                habitMap?.get("title")?.toString() ?: "未知习惯"
                            } else {
                                "无"
                            }
                        } else {
                            "无"
                        }
                        
                        Text(
                            text = "关联习惯: $habitTitle",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "选择习惯",
                            tint = Color.Gray
                        )
                        
                        // 习惯下拉菜单
                        DropdownMenu(
                            expanded = habitMenuExpanded,
                            onDismissRequest = { habitMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("无") },
                                onClick = { 
                                    selectedHabitId = null
                                    habitMenuExpanded = false
                                }
                            )
                            
                            habits.forEach { habit ->
                                val habitMap = habit as? Map<*, *>
                                val habitId = habitMap?.get("id")?.toString() ?: ""
                                val habitTitle = habitMap?.get("title")?.toString() ?: "未知习惯"
                                val habitDescription = habitMap?.get("description")?.toString() ?: ""
                                
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(habitTitle, fontWeight = FontWeight.Medium)
                                            if (habitDescription.isNotEmpty()) {
                                                Text(
                                                    text = habitDescription,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    },
                                    onClick = { 
                                        selectedHabitId = habitId
                                        habitMenuExpanded = false
                                    }
                                )
                            }
                        }
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
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "关联目标",
                            tint = PrimaryLight,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
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
                
                // 分隔线
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // 任务类型特定设置
                when (taskType) {
                    TaskType.CHECK_IN -> {
                        // 打卡任务特定设置
                        Text(
                            text = "打卡设置",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 打卡频率类型选择
                        Text(
                            text = "打卡频率类型",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = checkInFrequencyType == CheckInFrequencyType.DAILY,
                                onClick = { checkInFrequencyType = CheckInFrequencyType.DAILY },
                                label = { Text("每日打卡次数") }
                            )
                            
                            FilterChip(
                                selected = checkInFrequencyType == CheckInFrequencyType.WEEKLY,
                                onClick = { checkInFrequencyType = CheckInFrequencyType.WEEKLY },
                                label = { Text("每周打卡天数") }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 打卡频率设置
                        OutlinedTextField(
                            value = checkInFrequency.toString(),
                            onValueChange = { 
                                val value = it.toIntOrNull() ?: 1
                                checkInFrequency = if (value > 0) value else 1
                            },
                            label = { 
                                Text(
                                    if (checkInFrequencyType == CheckInFrequencyType.DAILY) 
                                        "每日打卡次数" 
                                    else 
                                        "每周打卡天数"
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 每日打卡截止时间
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "设置每日打卡截止时间",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Switch(
                                checked = hasDailyDeadline,
                                onCheckedChange = { hasDailyDeadline = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PrimaryLight,
                                    checkedTrackColor = PrimaryLight.copy(alpha = 0.5f)
                                )
                            )
                        }
                        
                        AnimatedVisibility(visible = hasDailyDeadline) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { timePickerExpanded = true }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "每日截止时间",
                                    tint = PrimaryLight
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                val calendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, dailyDeadlineHour)
                                    set(Calendar.MINUTE, dailyDeadlineMinute)
                                }
                                
                                Text(
                                    text = "截止时间: ${timeFormatter.format(calendar.time)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            // 时间选择器对话框
                            if (timePickerExpanded) {
                                Dialog(onDismissRequest = { timePickerExpanded = false }) {
                                    Surface(
                                        shape = MaterialTheme.shapes.extraLarge,
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "选择截止时间",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            
                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            TimePicker(state = timePickerState)
                                            
                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { timePickerExpanded = false }) {
                                                    Text("取消")
                                                }
                                                
                                                Spacer(modifier = Modifier.width(8.dp))
                                                
                                                Button(onClick = {
                                                    dailyDeadlineHour = timePickerState.hour
                                                    dailyDeadlineMinute = timePickerState.minute
                                                    timePickerExpanded = false
                                                }) {
                                                    Text("确定")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    TaskType.POMODORO -> {
                        // 番茄钟任务特定设置
                        Text(
                            text = "番茄钟设置",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 标签选择
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = if (pomodoroTag == PomodoroTag.CUSTOM) 
                                    customTagName ?: "自定义" 
                                else
                                    pomodoroTag.getDisplayName(),
                                onValueChange = { 
                                    if (pomodoroTag == PomodoroTag.CUSTOM) {
                                        customTagName = it
                                    }
                                },
                                label = { Text("标签") },
                                readOnly = pomodoroTag != PomodoroTag.CUSTOM,
                                trailingIcon = {
                                    IconButton(onClick = { tagMenuExpanded = true }) {
                                        Icon(Icons.Filled.ArrowDropDown, "展开标签选择")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            DropdownMenu(
                                expanded = tagMenuExpanded,
                                onDismissRequest = { tagMenuExpanded = false },
                                modifier = Modifier.width(250.dp)
                            ) {
                                PomodoroTag.values().forEach { tag ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .background(
                                                            color = Color(tag.getColor()),
                                                            shape = CircleShape
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(tag.getDisplayName())
                                            }
                                        },
                                        onClick = {
                                            pomodoroTag = tag
                                            tagMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // 自定义标签名称输入
                        if (pomodoroTag == PomodoroTag.CUSTOM) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customTagName ?: "",
                                onValueChange = { customTagName = it },
                                label = { Text("自定义标签名称") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 专注时长设置
                        OutlinedTextField(
                            value = focusMinutes.toString(),
                            onValueChange = { 
                                val value = it.toIntOrNull() ?: 25
                                focusMinutes = if (value > 0) value else 25
                            },
                            label = { Text("专注时长 (分钟)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 短休息时长设置
                        OutlinedTextField(
                            value = shortBreakMinutes.toString(),
                            onValueChange = { 
                                val value = it.toIntOrNull() ?: 5
                                shortBreakMinutes = if (value > 0) value else 5
                            },
                            label = { Text("短休息时长 (分钟)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 长休息时长设置
                        OutlinedTextField(
                            value = longBreakMinutes.toString(),
                            onValueChange = { 
                                val value = it.toIntOrNull() ?: 15
                                longBreakMinutes = if (value > 0) value else 15
                            },
                            label = { Text("长休息时长 (分钟)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 长休息前的番茄钟数量设置
                        OutlinedTextField(
                            value = sessionsBeforeLongBreak.toString(),
                            onValueChange = { 
                                val value = it.toIntOrNull() ?: 4
                                sessionsBeforeLongBreak = if (value > 0) value else 4
                            },
                            label = { Text("长休息前的番茄钟数量") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    else -> {
                        // 普通任务不需要特殊设置
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
                                // 创建任务对象
                                val updatedTask = createTaskFromForm(
                                    task = task,
                                    title = title,
                                    description = description,
                                    taskType = taskType,
                                    priority = priority,
                                    dueDate = dueDate,
                                    goalId = selectedGoalId,
                                    habitId = selectedHabitId,
                                    checkInFrequencyType = checkInFrequencyType,
                                    checkInFrequency = checkInFrequency,
                                    hasDailyDeadline = hasDailyDeadline,
                                    dailyDeadlineHour = dailyDeadlineHour,
                                    dailyDeadlineMinute = dailyDeadlineMinute,
                                    focusMinutes = focusMinutes,
                                    shortBreakMinutes = shortBreakMinutes,
                                    longBreakMinutes = longBreakMinutes,
                                    sessionsBeforeLongBreak = sessionsBeforeLongBreak,
                                    pomodoroTag = pomodoroTag,
                                    customTagName = customTagName,
                                    habits = habits
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
 * 根据表单创建任务对象
 */
private fun createTaskFromForm(
    task: Task?,
    title: String,
    description: String?,
    taskType: TaskType,
    priority: TaskPriority,
    dueDate: Date?,
    goalId: Long?,
    habitId: String?,
    checkInFrequencyType: CheckInFrequencyType,
    checkInFrequency: Int,
    hasDailyDeadline: Boolean,
    dailyDeadlineHour: Int,
    dailyDeadlineMinute: Int,
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    sessionsBeforeLongBreak: Int,
    pomodoroTag: PomodoroTag,
    customTagName: String?,
    habits: List<Any>
): Task {
    // 创建每日截止时间
    val dailyDeadline = if (hasDailyDeadline) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, dailyDeadlineHour)
            set(Calendar.MINUTE, dailyDeadlineMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    } else null
    
    // 创建打卡设置
    val checkInSettings = if (taskType == TaskType.CHECK_IN) {
        CheckInSettings(
            frequencyType = checkInFrequencyType,
            frequency = checkInFrequency,
            dailyDeadline = dailyDeadline,
            currentStreak = task?.checkInSettings?.currentStreak ?: 0,
            bestStreak = task?.checkInSettings?.bestStreak ?: 0,
            completedToday = task?.checkInSettings?.completedToday ?: 0,
            completedThisWeek = task?.checkInSettings?.completedThisWeek ?: 0,
            lastCheckInDate = task?.checkInSettings?.lastCheckInDate
        )
    } else null
    
    // 创建番茄钟设置
    val pomodoroSettings = if (taskType == TaskType.POMODORO) {
        PomodoroSettings(
            focusMinutes = focusMinutes,
            shortBreakMinutes = shortBreakMinutes,
            longBreakMinutes = longBreakMinutes,
            sessionsBeforeLongBreak = sessionsBeforeLongBreak,
            totalCompletedSessions = task?.pomodoroSettings?.totalCompletedSessions ?: 0,
            todayCompletedSessions = task?.pomodoroSettings?.todayCompletedSessions ?: 0,
            totalFocusMinutes = task?.pomodoroSettings?.totalFocusMinutes ?: 0,
            tag = pomodoroTag,
            customTagName = customTagName
        )
    } else null
    
    // 查找习惯标题
    val habitTitle = if (habitId != null) {
        val selectedHabit = habits.find { 
            val habitMap = it as? Map<*, *>
            habitMap?.get("id")?.toString() == habitId 
        }
        if (selectedHabit != null) {
            val habitMap = selectedHabit as? Map<*, *>
            habitMap?.get("title")?.toString()
        } else {
            task?.habitTitle
        }
    } else {
        null
    }
    
    // 保留任务中的目标标题
    val goalTitle = if (goalId != null && goalId == task?.goalId) {
        task.goalTitle
    } else null
    
    return Task(
        id = task?.id ?: 0L,
        title = title,
        description = description,
        type = taskType,
        priority = priority,
        dueDate = dueDate,
        goalId = goalId,
        goalTitle = goalTitle,
        habitId = habitId,
        habitTitle = habitTitle,
        isCompleted = task?.isCompleted ?: false,
        createdAt = task?.createdAt ?: Date(),
        updatedAt = Date(),
        checkInSettings = checkInSettings,
        pomodoroSettings = pomodoroSettings
    )
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