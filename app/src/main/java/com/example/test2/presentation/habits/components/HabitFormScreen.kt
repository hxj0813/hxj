package com.example.test2.presentation.habits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.test2.data.local.entity.FrequencyType
import com.example.test2.data.local.entity.HabitCategory
import com.example.test2.data.local.entity.HabitPriority
import java.util.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.vector.ImageVector


/**
 * 习惯表单数据类
 */
data class HabitFormData(
    val id: String? = null,
    val title: String = "",
    val description: String? = null,
    val category: HabitCategory = HabitCategory.OTHER,
    val icon: String? = null,
    val color: Color = Color(0xFF4CAF50),
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val frequencyCount: Int = 1,
    val frequencyDays: List<Int> = emptyList(),
    val timeOfDay: Date? = null,
    val reminder: Boolean = false,
    val reminderTime: Date? = null,
    val priority: HabitPriority = HabitPriority.MEDIUM,
    val tags: List<String> = emptyList()
)

/**
 * 习惯表单界面
 */
@Composable
fun HabitFormScreen(
    habitData: HabitFormData = HabitFormData(),
    onSave: (HabitFormData) -> Unit,
    onCancel: () -> Unit
) {
    var formData by remember { mutableStateOf(habitData) }
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "取消"
                )
            }
            
            Text(
                text = if (formData.id == null) "创建新习惯" else "编辑习惯",
                style = MaterialTheme.typography.titleLarge
            )
            
            TextButton(
                onClick = { onSave(formData) },
                enabled = formData.title.isNotBlank()
            ) {
                Text("保存")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 标题输入
        OutlinedTextField(
            value = formData.title,
            onValueChange = { formData = formData.copy(title = it) },
            label = { Text("习惯名称") },
            placeholder = { Text("例如：每天喝水") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Edit, contentDescription = "习惯名称")
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 描述输入
        OutlinedTextField(
            value = formData.description ?: "",
            onValueChange = { formData = formData.copy(description = it.takeIf { it.isNotBlank() }) },
            label = { Text("描述（可选）") },
            placeholder = { Text("习惯的详细描述...") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            minLines = 2,
            maxLines = 4
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 类别选择
        Text(
            text = "习惯类别",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow {
            HabitCategory.values().forEach { category ->
                CategoryChip(
                    category = category,
                    selected = formData.category == category,
                    onClick = { formData = formData.copy(category = category) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 颜色选择
        Text(
            text = "习惯颜色",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow {
            habitColors.forEach { color ->
                ColorItem(
                    color = color,
                    selected = formData.color == color,
                    onClick = { formData = formData.copy(color = color) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 频率设置
        Text(
            text = "习惯频率",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FrequencySelector(
            frequencyType = formData.frequencyType,
            frequencyCount = formData.frequencyCount,
            frequencyDays = formData.frequencyDays,
            onFrequencyTypeChanged = { formData = formData.copy(frequencyType = it) },
            onFrequencyCountChanged = { formData = formData.copy(frequencyCount = it) },
            onFrequencyDaysChanged = { formData = formData.copy(frequencyDays = it) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 优先级设置
        Text(
            text = "优先级",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        PrioritySelector(
            priority = formData.priority,
            onPriorityChanged = { formData = formData.copy(priority = it) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 提醒设置
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "每日提醒",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            
            Switch(
                checked = formData.reminder,
                onCheckedChange = { formData = formData.copy(reminder = it) }
            )
        }
        
        AnimatedVisibility(visible = formData.reminder) {
            TimePickerRow(
                time = formData.reminderTime ?: getDefaultReminderTime(),
                onTimeSelected = { formData = formData.copy(reminderTime = it) }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun LazyRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
fun CategoryChip(
    category: HabitCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val categoryInfo = getCategoryInfo(category)
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = categoryInfo.icon,
                contentDescription = categoryInfo.name,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = categoryInfo.name,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ColorItem(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已选择",
                tint = getContrastColor(color)
            )
        }
    }
}

@Composable
fun FrequencySelector(
    frequencyType: FrequencyType,
    frequencyCount: Int,
    frequencyDays: List<Int>,
    onFrequencyTypeChanged: (FrequencyType) -> Unit,
    onFrequencyCountChanged: (Int) -> Unit,
    onFrequencyDaysChanged: (List<Int>) -> Unit
) {
    Column {
        // 频率类型选择器
        FrequencyTypeSelector(
            frequencyType = frequencyType,
            onFrequencyTypeChanged = onFrequencyTypeChanged
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 根据频率类型显示不同的选择器
        when (frequencyType) {
            FrequencyType.DAILY -> {
                // 每日频率不需要额外设置
                Text(
                    text = "每天完成此习惯",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FrequencyType.WEEKLY -> {
                // 每周特定天数
                Text(
                    text = "每周选择特定天数",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                WeekDaySelector(
                    selectedDays = frequencyDays,
                    onSelectionChanged = onFrequencyDaysChanged
                )
            }
            FrequencyType.MONTHLY -> {
                // 每月特定日期
                Text(
                    text = "每月选择特定日期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                MonthDaySelector(
                    selectedDays = frequencyDays,
                    onSelectionChanged = onFrequencyDaysChanged
                )
            }
            FrequencyType.CUSTOM -> {
                // 自定义频率（X天一次）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "每",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FrequencyCountSelector(
                        count = frequencyCount,
                        onCountChanged = onFrequencyCountChanged
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "天一次",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun FrequencyTypeSelector(
    frequencyType: FrequencyType,
    onFrequencyTypeChanged: (FrequencyType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FrequencyType.values().forEach { type ->
            FrequencyTypeChip(
                type = type,
                selected = frequencyType == type,
                onClick = { onFrequencyTypeChanged(type) }
            )
        }
    }
}

@Composable
fun FrequencyTypeChip(
    type: FrequencyType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val typeInfo = when (type) {
        FrequencyType.DAILY -> "每日"
        FrequencyType.WEEKLY -> "每周"
        FrequencyType.MONTHLY -> "每月"
        FrequencyType.CUSTOM -> "自定义"
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .padding(end = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = typeInfo,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun WeekDaySelector(
    selectedDays: List<Int>,
    onSelectionChanged: (List<Int>) -> Unit
) {
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDays.forEachIndexed { index, day ->
            val isSelected = selectedDays.contains(index)
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable {
                        val newSelection = if (isSelected) {
                            selectedDays - index
                        } else {
                            selectedDays + index
                        }
                        onSelectionChanged(newSelection)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MonthDaySelector(
    selectedDays: List<Int>,
    onSelectionChanged: (List<Int>) -> Unit
) {
    val daysPerRow = 7
    val totalDays = 31
    val rows = (totalDays + daysPerRow - 1) / daysPerRow
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until daysPerRow) {
                    val day = row * daysPerRow + col + 1
                    if (day <= totalDays) {
                        MonthDayItem(
                            day = day,
                            selected = selectedDays.contains(day),
                            onSelectedChanged = { selected ->
                                val newSelection = if (selected) {
                                    selectedDays + day
                                } else {
                                    selectedDays - day
                                }
                                onSelectionChanged(newSelection)
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MonthDayItem(
    day: Int,
    selected: Boolean,
    onSelectedChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onSelectedChanged(!selected) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FrequencyCountSelector(
    count: Int,
    onCountChanged: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (count > 1) onCountChanged(count - 1) },
            enabled = count > 1
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "减少"
            )
        }
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        IconButton(
            onClick = { onCountChanged(count + 1) }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "增加"
            )
        }
    }
}

@Composable
fun PrioritySelector(
    priority: HabitPriority,
    onPriorityChanged: (HabitPriority) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HabitPriority.values().forEach { priorityItem ->
            PriorityChip(
                priority = priorityItem,
                selected = priority == priorityItem,
                onClick = { onPriorityChanged(priorityItem) }
            )
        }
    }
}

@Composable
fun PriorityChip(
    priority: HabitPriority,
    selected: Boolean,
    onClick: () -> Unit
) {
    val priorityInfo = when (priority) {
        HabitPriority.HIGH -> "高" to Color(0xFFE57373)
        HabitPriority.MEDIUM -> "中" to Color(0xFFFFB74D)
        HabitPriority.LOW -> "低" to Color(0xFF81C784)
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) priorityInfo.second.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) BorderStroke(1.dp, priorityInfo.second) else null,
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = priorityInfo.first,
                color = if (selected) priorityInfo.second else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TimePickerRow(
    time: Date,
    onTimeSelected: (Date) -> Unit
) {
    // 简化版的时间选择器，实际项目中应使用MaterialTimePicker
    val calendar = Calendar.getInstance().apply { this.time = time }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Alarm,
            contentDescription = "提醒时间",
            modifier = Modifier.padding(end = 8.dp)
        )
        
        Text(
            text = String.format("%02d:%02d", hour, minute),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// 辅助函数
private fun getDefaultReminderTime(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 8)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

private fun getContrastColor(backgroundColor: Color): Color {
    val luminance = 0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue
    return if (luminance > 0.5f) Color.Black else Color.White
}

private data class CategoryInfo(
    val name: String,
    val icon: ImageVector
)

private fun getCategoryInfo(category: HabitCategory): CategoryInfo {
    return when (category) {
        HabitCategory.HEALTH -> CategoryInfo("健康", Icons.Default.Favorite)
        HabitCategory.EXERCISE -> CategoryInfo("运动", Icons.Default.DirectionsRun)
        HabitCategory.STUDY -> CategoryInfo("学习", Icons.Default.School)
        HabitCategory.WORK -> CategoryInfo("工作", Icons.Default.Work)
        HabitCategory.MINDFULNESS -> CategoryInfo("冥想", Icons.Default.SelfImprovement)
        HabitCategory.SKILL -> CategoryInfo("技能", Icons.Default.Psychology)
        HabitCategory.SOCIAL -> CategoryInfo("社交", Icons.Default.People)
        HabitCategory.OTHER -> CategoryInfo("其他", Icons.Default.MoreHoriz)
    }
}

// 预定义颜色
private val habitColors = listOf(
    Color(0xFF4CAF50), // 绿色
    Color(0xFF2196F3), // 蓝色
    Color(0xFFF44336), // 红色
    Color(0xFFFF9800), // 橙色
    Color(0xFF9C27B0), // 紫色
    Color(0xFF795548), // 棕色
    Color(0xFF607D8B), // 蓝灰色
    Color(0xFF9E9E9E)  // 灰色
) 