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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TimeTrackingScreen(
    viewModel: TimeTrackingViewModel = viewModel()
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
            // 搜索栏
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { query ->
                    viewModel.onEvent(TimeTrackingEvent.UpdateSearchQuery(query))
                },
                placeholder = { Text("搜索时间记录...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 时间条目列表标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "今日记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 选中的日期显示
                val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
                Text(
                    text = dateFormat.format(state.selectedDate ?: Date()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // 时间条目列表
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Text(
                    text = state.error ?: "发生未知错误",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            } else if (state.filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无时间记录",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
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
                        key = { it.id ?: it.hashCode() }
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
                                viewModel.onEvent(TimeTrackingEvent.DeleteTimeEntry(timeEntry.id!!))
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
 * 类别选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: TimeCategory?,
    onCategorySelected: (TimeCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(null) + TimeCategory.values().toList()
    
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        categories.forEachIndexed { index, category ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = categories.size
                ),
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    if (category == null) {
                        Text("全部")
                    } else {
                        Text(TimeTrackingUtils.getCategoryName(category))
                    }
                }
            )
        }
    }
} 