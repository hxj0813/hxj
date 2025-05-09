package com.example.test2.presentation.timetracking

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import com.example.test2.presentation.timetracking.components.StatisticsView
import com.example.test2.presentation.timetracking.components.TimeEntryCard
import com.example.test2.presentation.timetracking.components.TimeEntryDialog
import com.example.test2.presentation.timetracking.components.TimerView
import com.example.test2.presentation.timetracking.TimeTrackingUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TimeTrackingScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TimeTrackingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // 对话框状态
    var showDatePicker by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.onEvent(TimeTrackingEvent.LoadTimeEntries)
        viewModel.onEvent(TimeTrackingEvent.LoadTasks)
    }
    
    // 时间条目对话框
    if (state.showEntryDialog) {
        TimeEntryDialog(
            title = state.selectedEntry?.let { "编辑时间记录" } ?: "添加时间记录",
            initialTimeEntry = state.selectedEntry,
            tasks = state.allTasks,
            onDismiss = {
                viewModel.onEvent(TimeTrackingEvent.DismissDialog)
            },
            onSave = { timeEntry ->
                if (state.selectedEntry != null) {
                    viewModel.onEvent(TimeTrackingEvent.UpdateTimeEntry(timeEntry))
                } else {
                    viewModel.onEvent(TimeTrackingEvent.AddTimeEntry(timeEntry))
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("时间追踪") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { viewModel.onEvent(TimeTrackingEvent.ShowFilterDialog) }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "筛选"
                        )
                    }
                    
                    IconButton(onClick = { /* 显示日期选择器 */ showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "选择日期"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // 只有在没有正在进行的时间条目时显示添加按钮
            if (state.ongoingEntry == null) {
                FloatingActionButton(
                    onClick = { viewModel.onEvent(TimeTrackingEvent.ShowAddEntryDialog) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加时间记录",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 类别选择器
            CategorySelector(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { category ->
                    viewModel.onEvent(TimeTrackingEvent.FilterCategory(category))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // 计时器视图
            TimerView(
                ongoingEntry = state.ongoingEntry,
                onStopTimer = {
                    viewModel.onEvent(TimeTrackingEvent.StopTimeEntry())
                },
                onStartTimer = { timeEntry ->
                    viewModel.onEvent(TimeTrackingEvent.StartTimeEntry(timeEntry))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // 统计视图
            StatisticsView(
                statistics = state.statistics,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // 日期显示
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(state.selectedDate),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // 时间条目列表
            if (state.isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (state.filteredEntries.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "今天还没有时间记录\n点击底部按钮添加",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(
                        items = state.filteredEntries,
                        key = { it.id }
                    ) { timeEntry ->
                        TimeEntryCard(
                            timeEntry = timeEntry,
                            onClick = { 
                                viewModel.onEvent(TimeTrackingEvent.SelectTimeEntry(timeEntry))
                            },
                            onEdit = {
                                viewModel.onEvent(TimeTrackingEvent.ShowEditEntryDialog(timeEntry))
                            },
                            onDelete = {
                                viewModel.onEvent(TimeTrackingEvent.DeleteTimeEntry(timeEntry.id))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}

/**
 * 分类选择器
 */
@Composable
fun CategorySelector(
    selectedCategory: TimeCategory?,
    onCategorySelected: (TimeCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "按分类筛选",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        // 分类选择行
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // 全部分类选项
            CategoryChip(
                category = null,
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
            
            // 各分类选项
            TimeCategory.values().forEach { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

/**
 * 分类选择芯片
 */
@Composable
fun CategoryChip(
    category: TimeCategory?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = category?.let { TimeTrackingUtils.getCategoryName(it) } ?: "全部",
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        )
    }
}

/**
 * 分类占比展示
 */
@Composable
private fun CategoryBreakdown(
    categoryPercentages: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "分类占比",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        // 简单的彩色条形图
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            categoryPercentages.forEach { (category, percentage) ->
                Box(
                    modifier = Modifier
                        .weight(percentage)
                        .fillMaxSize()
                        .background(TimeTrackingUtils.getCategoryColor(category))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 图例
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryPercentages.forEach { (category, percentage) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(TimeTrackingUtils.getCategoryColor(category), CircleShape)
                    )
                    
                    Text(
                        text = "$category ${percentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
} 