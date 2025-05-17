@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.test2.presentation.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test2.data.local.entity.FrequencyType
import com.example.test2.data.local.entity.HabitCategory
import com.example.test2.data.local.entity.HabitEntity
import com.example.test2.presentation.habits.components.HabitFormData
import com.example.test2.presentation.habits.components.HabitFormScreen
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 习惯列表界面
 */
@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val habits by viewModel.filteredHabits.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 简化的顶部栏，不包含搜索和筛选图标
            HabitsTopBar()
            
            // 分类筛选栏
            CategoryFilterBar(
                selectedCategory = state.currentFilter,
                onCategorySelected = { viewModel.setCategoryFilter(it) }
            )
            
            // 习惯列表
            if (habits.isEmpty()) {
                EmptyHabitsView(
                    onCreateHabit = { viewModel.showAddHabitForm() }
                )
            } else {
                HabitsList(
                    habits = habits,
                    onHabitClick = { /* 查看详情 */ },
                    onCompleteClick = { habit, completed -> 
                        viewModel.completeHabit(habit.id, completed) 
                    },
                    onEditClick = { viewModel.showEditHabitForm(it) },
                    onArchiveClick = { habit -> 
                        viewModel.archiveHabit(habit.id, !habit.isArchived) 
                    },
                    onDeleteClick = { viewModel.deleteHabit(it) }
                )
            }
        }
        
        // 添加按钮
        FloatingActionButton(
            onClick = { viewModel.showAddHabitForm() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加习惯"
            )
        }
        
        // 添加/编辑习惯表单
        AnimatedVisibility(
            visible = state.showAddEditForm,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                state.currentEditHabit?.let { formData ->
                    HabitFormScreen(
                        habitData = formData,
                        onSave = { viewModel.saveHabit(it) },
                        onCancel = { viewModel.hideForm() }
                    )
                }
            }
        }
        
        // 错误提示
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
        
        // 显示错误消息
        LaunchedEffect(state.error) {
            state.error?.let { 
                snackbarHostState.showSnackbar(
                    message = it,
                    actionLabel = "关闭",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearError()
            }
        }
    }
}

@Composable
fun HabitsTopBar() {
    TopAppBar(
        title = { Text("习惯养成") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun CategoryFilterBar(
    selectedCategory: HabitCategory?,
    onCategorySelected: (HabitCategory?) -> Unit
) {
    val categories = remember { HabitCategory.values().toList() }
    val scrollState = rememberScrollState()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 全部分类选项
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("全部") },
                leadingIcon = if (selectedCategory == null) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
            
            // 各个分类选项
            categories.forEach { category ->
                val categoryInfo = getCategoryInfo(category)
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(categoryInfo.name) },
                    leadingIcon = if (selectedCategory == category) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        {
                            Icon(
                                imageVector = categoryInfo.icon,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    }
                )
            }
        }
        
        Divider()
    }
}

@Composable
fun EmptyHabitsView(
    onCreateHabit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "还没有习惯",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "开始添加习惯，养成良好生活方式",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onCreateHabit,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("创建第一个习惯")
        }
    }
}

@Composable
fun HabitsList(
    habits: List<HabitEntity>,
    onHabitClick: (HabitEntity) -> Unit,
    onCompleteClick: (HabitEntity, Boolean) -> Unit,
    onEditClick: (HabitEntity) -> Unit,
    onArchiveClick: (HabitEntity) -> Unit,
    onDeleteClick: (HabitEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(habits, key = { it.id }) { habit ->
            HabitItem(
                habit = habit,
                onClick = { onHabitClick(habit) },
                onCompleteClick = { completed -> onCompleteClick(habit, completed) },
                onEditClick = { onEditClick(habit) },
                onArchiveClick = { onArchiveClick(habit) },
                onDeleteClick = { onDeleteClick(habit) }
            )
        }
        
        // 添加底部间距，避免FAB遮挡
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HabitItem(
    habit: HabitEntity,
    onClick: () -> Unit,
    onCompleteClick: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val categoryInfo = getCategoryInfo(habit.getCategoryEnum())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = if (habit.isArchived) 0.7f else 1f
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 上部分：习惯信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 复选框
                Checkbox(
                    checked = habit.completedToday,
                    onCheckedChange = { onCompleteClick(!habit.completedToday) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(habit.color.toInt()),
                        uncheckedColor = Color(habit.color.toInt()).copy(alpha = 0.6f)
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 习惯信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (habit.isArchived) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.onSurface
                    )
                    
                    habit.description?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = if (habit.isArchived) 0.6f else 0.8f
                                )
                            )
                        }
                    }
                    
                    // 分类和频率信息
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 分类标签
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryInfo.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = categoryInfo.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        
                        // 频率信息
                        Text(
                            text = getFrequencyText(habit),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 连续天数和展开按钮
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(habit.color.toInt()).copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "连续 ${habit.currentStreak} 天",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(habit.color.toInt())
                        )
                    }
                    
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "收起" else "展开",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 展开部分：更多信息和操作按钮
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Divider()
                    
                    // 统计信息
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "最长连续",
                            value = "${habit.bestStreak} 天",
                            icon = Icons.Default.Timeline
                        )
                        
                        StatItem(
                            label = "累计完成",
                            value = "${habit.totalCompletions} 次",
                            icon = Icons.Default.CheckCircle
                        )
                        
                        StatItem(
                            label = "开始于",
                            value = SimpleDateFormat("MM/dd", Locale.getDefault())
                                .format(habit.startDate),
                            icon = Icons.Default.DateRange
                        )
                    }
                    
                    Divider()
                    
                    // 操作按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            text = "编辑",
                            icon = Icons.Default.Edit,
                            onClick = onEditClick
                        )
                        
                        ActionButton(
                            text = if (habit.isArchived) "恢复" else "归档",
                            icon = if (habit.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            onClick = onArchiveClick
                        )
                        
                        ActionButton(
                            text = "删除",
                            icon = Icons.Default.Delete,
                            onClick = onDeleteClick,
                            color = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

// 辅助函数
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

private fun getFrequencyText(habit: HabitEntity): String {
    return when (habit.getFrequencyTypeEnum()) {
        FrequencyType.DAILY -> "每日"
        FrequencyType.WEEKLY -> {
            val days = habit.getFrequencyDaysList()
            if (days.isEmpty()) "每周" else "每周${days.size}天"
        }
        FrequencyType.MONTHLY -> {
            val days = habit.getFrequencyDaysList()
            if (days.isEmpty()) "每月" else "每月${days.size}天"
        }
        FrequencyType.CUSTOM -> "每${habit.frequencyCount}天"
    }
} 