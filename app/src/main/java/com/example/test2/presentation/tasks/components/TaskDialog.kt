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
import com.example.test2.data.local.entity.TaskPriority as EntityTaskPriority
import com.example.test2.data.model.TaskPriority as ModelTaskPriority
import com.example.test2.data.model.TaskStatus
import com.example.test2.data.local.entity.TaskType
import com.example.test2.data.model.TaskType as ModelTaskType
import com.example.test2.presentation.theme.PrimaryDark
import com.example.test2.presentation.theme.PrimaryLight
import com.example.test2.util.DateTimeUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.test2.presentation.tasks.components.PriorityIndicator
import androidx.compose.ui.unit.sp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import com.example.test2.presentation.tasks.viewmodel.TaskManagerViewModel
import androidx.compose.runtime.collectAsState
import com.example.test2.data.local.entity.TaskTagEntity
import com.example.test2.data.local.entity.TagCategory

/**
 * 任务数据模型 - 用于传递任务编辑信息
 */
data class TaskEditorData(
    val title: String,
    val description: String?,
    val taskType: Int, // TaskType的ordinal值
    val taskPriority: Int, // TaskPriority的ordinal值
    val dueDate: Date?,
    val goalId: Long?,
    
    // 打卡任务相关设置
    val checkInFrequencyType: Int,
    val checkInFrequencyCount: Int,
    val checkInReminderEnabled: Boolean,
    val checkInReminderTime: Date?,
    
    // 番茄钟任务相关设置
    val pomodoroFocusTime: Int,
    val pomodoroShortBreak: Int,
    val pomodoroLongBreak: Int,
    val pomodoroSessionsBeforeLongBreak: Int,
    val pomodoroTagId: String?
)

/**
 * 任务添加/编辑对话框 - 与TaskManagerViewModel集成版本
 *
 * @param taskEntity 任务实体，用于编辑已有任务
 * @param editorState 任务编辑器状态
 * @param onDismiss 取消回调
 * @param onSave 保存回调，返回TaskEditorData
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    taskEntity: com.example.test2.data.local.entity.TaskEntity? = null,
    editorState: com.example.test2.presentation.tasks.viewmodel.TaskEditorState,
    onDismiss: () -> Unit,
    onSave: (TaskEditorData) -> Unit,
    viewModel: TaskManagerViewModel
) {
    // 表单状态 - 优先使用editorState中的值，其次是taskEntity中的值
    var title by remember { mutableStateOf(editorState.newTaskTitle.ifEmpty { taskEntity?.title ?: "" }) }
    var description by remember { mutableStateOf(editorState.newTaskDescription.ifEmpty { taskEntity?.description ?: "" }) }
    var taskType by remember { mutableStateOf(editorState.newTaskType) }
    var priority by remember { mutableStateOf(editorState.newTaskPriority) }
    var dueDate by remember { mutableStateOf(editorState.newTaskDueDate ?: taskEntity?.dueDate ?: Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) }
    var selectedGoalId by remember { mutableStateOf(editorState.newTaskGoalId ?: taskEntity?.goalId) }
    
    // 打卡任务设置
    var checkInFrequencyType by remember { mutableStateOf(editorState.newCheckInFrequencyType) }
    var checkInFrequency by remember { mutableStateOf(editorState.newCheckInFrequencyCount) }
    var hasDailyDeadline by remember { mutableStateOf(editorState.newCheckInReminderEnabled) }
    var dailyDeadlineHour by remember { mutableStateOf(8) } // 默认8点
    var dailyDeadlineMinute by remember { mutableStateOf(0) } // 默认0分
    
    // 如果有提醒时间，则解析小时和分钟
    LaunchedEffect(editorState.newCheckInReminderTime) {
        editorState.newCheckInReminderTime?.let { reminderTime ->
            val calendar = Calendar.getInstance().apply { time = reminderTime }
            dailyDeadlineHour = calendar.get(Calendar.HOUR_OF_DAY)
            dailyDeadlineMinute = calendar.get(Calendar.MINUTE)
        }
    }
    
    // 番茄钟任务设置
    var focusMinutes by remember { mutableStateOf(editorState.newPomodoroDuration) }
    var shortBreakMinutes by remember { mutableStateOf(editorState.newPomodoroShortBreak) }
    var longBreakMinutes by remember { mutableStateOf(editorState.newPomodoroLongBreak) }
    var sessionsBeforeLongBreak by remember { mutableStateOf(editorState.newPomodoroEstimatedCount) }
    var pomodoroTagId by remember { mutableStateOf(editorState.newPomodoroTagId) }
    
    // 日期格式化器（移除旧的，使用DateTimeUtil）
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    // 各种下拉菜单状态
    var priorityMenuExpanded by remember { mutableStateOf(false) }
    var goalMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
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
                        text = if (editorState.isEditingTask) "编辑任务" else "添加新任务",
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
                        selected = taskType == TaskType.CHECK_IN,
                        onClick = { taskType = TaskType.CHECK_IN },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("打卡") }
                    )
                    SegmentedButton(
                        selected = taskType == TaskType.POMODORO,
                        onClick = { taskType = TaskType.POMODORO },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
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
                        .clickable { showDatePicker = true },
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
                        text = "截止日期: ${DateTimeUtil.formatDate(dueDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("更改")
                    }
                }
                
                // 优先级选择
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { priorityMenuExpanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val priorityColor = when (priority) {
                        EntityTaskPriority.LOW -> Color(0xFF8BC34A)
                        EntityTaskPriority.MEDIUM -> Color(0xFF4FC3F7)
                        EntityTaskPriority.HIGH -> Color(0xFFFF9800)
                        else -> Color.Gray
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val priorityText = when (priority) {
                        EntityTaskPriority.LOW -> "低优先级"
                        EntityTaskPriority.MEDIUM -> "中优先级"
                        EntityTaskPriority.HIGH -> "高优先级"
                        else -> "中优先级"
                    }
                    
                    Text(
                        text = "优先级: $priorityText",
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
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF8BC34A))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("低优先级") 
                                }
                            },
                            onClick = { 
                                priority = EntityTaskPriority.LOW
                                priorityMenuExpanded = false
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4FC3F7))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("中优先级") 
                                }
                            },
                            onClick = { 
                                priority = EntityTaskPriority.MEDIUM
                                priorityMenuExpanded = false
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF9800))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("高优先级") 
                                }
                            },
                            onClick = { 
                                priority = EntityTaskPriority.HIGH
                                priorityMenuExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 标签选择
                Text(
                    text = "任务标签",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 标签选项
                var tagMenuExpanded by remember { mutableStateOf(false) }
                
                // 从 ViewModel 获取标签列表
                val allTags by viewModel.getAllTags().collectAsState(initial = emptyList())
                
                // 使用数据库中的标签
                // 如果标签列表为空，使用默认标签作为备选
                val tagOptions = if (allTags.isNotEmpty()) {
                    allTags
                } else {
                    // 使用默认标签作为备选
                    listOf(
                        TaskTagEntity(
                            id = "STUDY",
                            name = "学习",
                            category = TagCategory.STUDY.ordinal,
                            color = 0xFF2196F3.toInt()
                        ),
                        TaskTagEntity(
                            id = "WORK",
                            name = "工作",
                            category = TagCategory.WORK.ordinal,
                            color = 0xFF4CAF50.toInt()
                        ),
                        TaskTagEntity(
                            id = "EXERCISE",
                            name = "运动",
                            category = TagCategory.EXERCISE.ordinal,
                            color = 0xFFFF9800.toInt()
                        ),
                        TaskTagEntity(
                            id = "READING",
                            name = "阅读",
                            category = TagCategory.READING.ordinal,
                            color = 0xFF9C27B0.toInt()
                        ),
                        TaskTagEntity(
                            id = "CREATIVE",
                            name = "创意",
                            category = TagCategory.CREATIVE.ordinal,
                            color = 0xFFE91E63.toInt()
                        ),
                        TaskTagEntity(
                            id = "PERSONAL",
                            name = "个人发展",
                            category = TagCategory.PERSONAL.ordinal,
                            color = 0xFF3F51B5.toInt()
                        ),
                        TaskTagEntity(
                            id = "OTHER",
                            name = "其他",
                            category = TagCategory.OTHER.ordinal,
                            color = 0xFF607D8B.toInt()
                        )
                    )
                }
                
                // 默认选择"学习"标签或者传入的标签
                var selectedTagIndex by remember { mutableStateOf(
                    if (pomodoroTagId != null) {
                        tagOptions.indexOfFirst { it.id == pomodoroTagId }.takeIf { it >= 0 } ?: 0
                    } else {
                        // 默认选择学习标签
                        tagOptions.indexOfFirst { it.category == TagCategory.STUDY.ordinal }.takeIf { it >= 0 } ?: 0
                    }
                ) }
                
                // 更新pomodoroTagId
                LaunchedEffect(selectedTagIndex, tagOptions) {
                    if (tagOptions.isNotEmpty() && selectedTagIndex < tagOptions.size) {
                        pomodoroTagId = tagOptions[selectedTagIndex].id
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tagMenuExpanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 显示选中的标签颜色
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(tagOptions.getOrNull(selectedTagIndex)?.color ?: 0xFF2196F3.toInt()))
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 显示选中的标签名称
                    Text(
                        text = "标签: ${tagOptions.getOrNull(selectedTagIndex)?.name ?: "学习"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "选择标签",
                        tint = Color.Gray
                    )
                    
                    // 标签下拉菜单
                    DropdownMenu(
                        expanded = tagMenuExpanded,
                        onDismissRequest = { tagMenuExpanded = false }
                    ) {
                        tagOptions.forEachIndexed { index, tag ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(tag.color))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(tag.name) 
                                    }
                                },
                                onClick = { 
                                    selectedTagIndex = index
                                    tagMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // 根据任务类型显示不同的设置面板
                when (taskType) {
                    TaskType.CHECK_IN -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 打卡任务设置
                        Text(
                            text = "打卡设置",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 打卡频率
                        Text(
                            text = "打卡频率",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 打卡频率类型选择
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 每日打卡
                            FilterChip(
                                selected = checkInFrequencyType == 0,
                                onClick = { checkInFrequencyType = 0 },
                                label = { Text("每日") },
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // 每周打卡
                            FilterChip(
                                selected = checkInFrequencyType == 1,
                                onClick = { checkInFrequencyType = 1 },
                                label = { Text("每周") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 打卡次数选择
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (checkInFrequencyType == 0) "每日打卡次数" else "每周打卡天数",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // 减少按钮
                            IconButton(
                                onClick = { 
                                    if (checkInFrequency > 1) {
                                        checkInFrequency-- 
                                    }
                                }
                            ) {
                                Text("-", fontSize = 20.sp)
                            }
                            
                            // 次数显示
                            Text(
                                text = checkInFrequency.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // 增加按钮
                            IconButton(
                                onClick = { 
                                    if (checkInFrequency < (if (checkInFrequencyType == 0) 10 else 7)) {
                                        checkInFrequency++ 
                                    }
                                }
                            ) {
                                Text("+", fontSize = 20.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 是否设置提醒
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "每日打卡提醒",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Switch(
                                checked = hasDailyDeadline,
                                onCheckedChange = { hasDailyDeadline = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryLight
                                )
                            )
                        }
                        
                        // 提醒时间选择
                        AnimatedVisibility(visible = hasDailyDeadline) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { timePickerExpanded = true },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "提醒时间",
                                        tint = PrimaryLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "提醒时间: ${String.format("%02d:%02d", dailyDeadlineHour, dailyDeadlineMinute)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "选择时间",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                    TaskType.POMODORO -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 番茄钟任务设置
                        Text(
                            text = "番茄钟设置",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 专注时长
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "专注时长(分钟)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // 减少按钮
                            IconButton(
                                onClick = { 
                                    if (focusMinutes > 5) {
                                        focusMinutes -= 5
                                    }
                                }
                            ) {
                                Text("-", fontSize = 20.sp)
                            }
                            
                            // 时长显示
                            Text(
                                text = focusMinutes.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // 增加按钮
                            IconButton(
                                onClick = { 
                                    if (focusMinutes < 60) {
                                        focusMinutes += 5
                                    }
                                }
                            ) {
                                Text("+", fontSize = 20.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 短休息时长
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "短休息时长(分钟)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // 减少按钮
                            IconButton(
                                onClick = { 
                                    if (shortBreakMinutes > 1) {
                                        shortBreakMinutes--
                                    }
                                }
                            ) {
                                Text("-", fontSize = 20.sp)
                            }
                            
                            // 时长显示
                            Text(
                                text = shortBreakMinutes.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // 增加按钮
                            IconButton(
                                onClick = { 
                                    if (shortBreakMinutes < 15) {
                                        shortBreakMinutes++
                                    }
                                }
                            ) {
                                Text("+", fontSize = 20.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 长休息时长
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "长休息时长(分钟)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // 减少按钮
                            IconButton(
                                onClick = { 
                                    if (longBreakMinutes > 5) {
                                        longBreakMinutes -= 5
                                    }
                                }
                            ) {
                                Text("-", fontSize = 20.sp)
                            }
                            
                            // 时长显示
                            Text(
                                text = longBreakMinutes.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // 增加按钮
                            IconButton(
                                onClick = { 
                                    if (longBreakMinutes < 30) {
                                        longBreakMinutes += 5
                                    }
                                }
                            ) {
                                Text("+", fontSize = 20.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 长休息间隔
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "长休息间隔(个番茄钟)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // 减少按钮
                            IconButton(
                                onClick = { 
                                    if (sessionsBeforeLongBreak > 1) {
                                        sessionsBeforeLongBreak--
                                    }
                                }
                            ) {
                                Text("-", fontSize = 20.sp)
                            }
                            
                            // 次数显示
                            Text(
                                text = sessionsBeforeLongBreak.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // 增加按钮
                            IconButton(
                                onClick = { 
                                    if (sessionsBeforeLongBreak < 8) {
                                        sessionsBeforeLongBreak++
                                    }
                                }
                            ) {
                                Text("+", fontSize = 20.sp)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // 取消按钮
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 保存按钮
                    Button(
                        onClick = {
                            // 只有标题有效才能保存
                            if (isTitleValid) {
                                // 准备提醒时间
                                val reminderTime = if (hasDailyDeadline) {
                                    Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, dailyDeadlineHour)
                                        set(Calendar.MINUTE, dailyDeadlineMinute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.time
                                } else null
                                
                                // 创建任务数据对象
                                val taskData = TaskEditorData(
                                    title = title,
                                    description = description.takeIf { it.isNotBlank() },
                                    taskType = taskType.ordinal,
                                    taskPriority = priority.ordinal,
                                    dueDate = dueDate,
                                    goalId = selectedGoalId,
                                    
                                    // 打卡任务设置
                                    checkInFrequencyType = checkInFrequencyType,
                                    checkInFrequencyCount = checkInFrequency,
                                    checkInReminderEnabled = hasDailyDeadline,
                                    checkInReminderTime = reminderTime,
                                    
                                    // 番茄钟任务设置
                                    pomodoroFocusTime = focusMinutes,
                                    pomodoroShortBreak = shortBreakMinutes,
                                    pomodoroLongBreak = longBreakMinutes,
                                    pomodoroSessionsBeforeLongBreak = sessionsBeforeLongBreak,
                                    pomodoroTagId = pomodoroTagId
                                )
                                
                                onSave(taskData)
                            }
                        },
                        enabled = isTitleValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryLight,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }

    // 在 Dialog 的末尾添加日期选择器对话框
    if (showDatePicker) {
        ShowDatePicker(
            initialDate = dueDate,
            onDateSelected = { 
                dueDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

// 添加日期选择器对话框组件，与 GoalDialog 中的一致
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowDatePicker(
    initialDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
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

// 转换TaskPriority枚举类型
private fun convertToModelPriority(entityPriority: EntityTaskPriority): ModelTaskPriority {
    return when (entityPriority) {
        EntityTaskPriority.LOW -> ModelTaskPriority.LOW
        EntityTaskPriority.MEDIUM -> ModelTaskPriority.MEDIUM
        EntityTaskPriority.HIGH -> ModelTaskPriority.HIGH
        else -> ModelTaskPriority.MEDIUM
    }
}

private fun convertToEntityPriority(modelPriority: ModelTaskPriority): EntityTaskPriority {
    return when (modelPriority) {
        ModelTaskPriority.LOW -> EntityTaskPriority.LOW
        ModelTaskPriority.MEDIUM -> EntityTaskPriority.MEDIUM
        ModelTaskPriority.HIGH -> EntityTaskPriority.HIGH
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
    priority: EntityTaskPriority,
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
    
    // 将实体层TaskType转换为模型层ModelTaskType
    val modelTaskType = when (taskType) {
        TaskType.CHECK_IN -> ModelTaskType.CHECK_IN
        TaskType.POMODORO -> ModelTaskType.POMODORO
        else -> ModelTaskType.CHECK_IN  // 默认值
    }
    
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
        type = modelTaskType,
        priority = convertToModelPriority(priority),
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