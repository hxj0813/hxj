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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import com.example.test2.presentation.timetracking.components.TagDialog
import com.example.test2.presentation.timetracking.components.TimeEntryCard
import com.example.test2.presentation.timetracking.components.TimerView
import com.example.test2.presentation.timetracking.TimeTrackingUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.NavController
import androidx.compose.runtime.CompositionLocalProvider
import com.example.test2.presentation.common.LocalTimeTrackingViewModel
import kotlinx.coroutines.launch
import android.util.Log

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
    
    // 提供LocalTimeTrackingViewModel
    CompositionLocalProvider(LocalTimeTrackingViewModel provides viewModel) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("时间追踪") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "选择日期"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            // 使用LazyColumn作为主要容器，可以滚动所有内容
            LazyColumn(
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 固定区域: 计时器
                item {
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
                }
                
                // 固定区域: 统计视图
                item {
                    StatisticsView(
                        statistics = state.statistics,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                
                // 固定区域: 日期显示
                item {
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
                }
                
                // 加载中状态
                if (state.isLoading) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } 
                // 空状态
                else if (state.timeEntries.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            Text(
                                text = "今天还没有时间记录",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                // 时间条目列表
                else {
                    item {
                        Text(
                            text = "今日记录",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    
                    items(
                        items = state.timeEntries,
                        key = { it.id }
                    ) { timeEntry ->
                        TimeEntryCard(
                            timeEntry = timeEntry,
                            onClick = { 
                                viewModel.onEvent(TimeTrackingEvent.SelectTimeEntry(timeEntry))
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
            
            // 处理标签对话框
            if (state.showTagDialog) {
                TagDialog(
                    tag = state.selectedTag,
                    onDismiss = { viewModel.onEvent(TimeTrackingEvent.DismissDialog) },
                    onSave = { tag ->
                        // 保存标签
                        viewModel.saveTag(tag)
                        viewModel.onEvent(TimeTrackingEvent.DismissDialog)
                    }
                )
            }
        }
    }
    
    // 显示日期选择器
    if (showDatePicker) {
        ShowDatePicker(
            initialDate = state.selectedDate,
            onDateSelected = { selectedDate ->
                viewModel.onEvent(TimeTrackingEvent.SelectDate(selectedDate))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * 日期选择器对话框
 */
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
                    // 创建日历实例并设置日期
                    val calendar = java.util.Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                        
                        // 设置为当天的00:00:00，只关注日期
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    
                    onDateSelected(calendar.time)
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