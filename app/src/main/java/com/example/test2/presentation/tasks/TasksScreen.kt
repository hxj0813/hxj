package com.example.test2.presentation.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test2.data.model.TaskType
import com.example.test2.presentation.tasks.components.TaskCalendarView
import com.example.test2.presentation.tasks.components.TaskCard
import com.example.test2.presentation.tasks.components.TaskDialog
import com.example.test2.presentation.theme.PrimaryDark
import com.example.test2.presentation.theme.PrimaryLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 任务管理屏幕
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()
    
    // 日期格式化器
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }
    val weekDayFormatter = remember { SimpleDateFormat("EEEE", Locale.getDefault()) }
    
    // 构建过滤器选项
    val filters = listOf(
        TasksState.Filter.ALL to "所有任务",
        TasksState.Filter.TODAY to "今日任务",
        TasksState.Filter.UPCOMING to "即将到期",
        TasksState.Filter.HIGH_PRIORITY to "高优先级",
        TasksState.Filter.COMPLETED to "已完成",
        TasksState.Filter.ASSOCIATED to "关联目标"
    )
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(TasksEvent.ShowAddTaskDialog) },
                containerColor = PrimaryLight,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .shadow(8.dp, CircleShape)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加任务",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 主内容
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题和日期
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "任务规划",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark
                            )
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dateFormatter.format(state.selectedDate),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.DarkGray
                                )
                                
                                Text(
                                    text = weekDayFormatter.format(state.selectedDate),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            
                            // 任务数量统计
                            Surface(
                                color = PrimaryLight.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "${state.filteredTasks.size}个任务",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PrimaryDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
                
                // 日历视图
                TaskCalendarView(
                    selectedDate = state.selectedDate,
                    onDateSelected = { date ->
                        viewModel.onEvent(TasksEvent.SelectDate(date))
                    },
                    tasksPerDay = state.getTaskCountMap(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 过滤器
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { (filter, label) ->
                        FilterChip(
                            selected = state.currentFilter == filter,
                            onClick = { viewModel.onEvent(TasksEvent.FilterTasks(filter)) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryLight,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 任务列表
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isLoading) {
                        // 加载状态
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = PrimaryLight
                            )
                        }
                    } else if (state.filteredTasks.isEmpty()) {
                        // 空状态
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (state.currentFilter) {
                                    TasksState.Filter.ALL -> "当前日期没有任务"
                                    TasksState.Filter.TODAY -> "今天没有任务"
                                    TasksState.Filter.COMPLETED -> "没有已完成的任务"
                                    TasksState.Filter.HIGH_PRIORITY -> "没有高优先级任务"
                                    TasksState.Filter.UPCOMING -> "没有即将到期的任务"
                                    TasksState.Filter.ASSOCIATED -> "没有关联目标的任务"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    } else {
                        // 任务列表
                        LazyColumn(
                            state = lazyListState,
                            contentPadding = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = state.filteredTasks,
                                key = { it.id }
                            ) { task ->
                                TaskCard(
                                    task = task,
                                    onEditClick = {
                                        viewModel.onEvent(TasksEvent.ShowEditTaskDialog(task))
                                    },
                                    onDelete = {
                                        viewModel.onEvent(TasksEvent.DeleteTask(task.id))
                                    },
                                    onToggleCompletion = {
                                        viewModel.onEvent(TasksEvent.CompleteTask(task.id))
                                    },
                                    onCheckinClick = if (task.type == TaskType.CHECK_IN) {
                                        { viewModel.onEvent(TasksEvent.CheckinTask(task.id)) }
                                    } else null,
                                    onStartClick = if (task.type == TaskType.POMODORO) {
                                        { viewModel.onEvent(TasksEvent.StartPomodoroTask(task.id)) }
                                    } else null,
                                    modifier = Modifier.animateItemPlacement(
                                        animationSpec = tween(300)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // 显示任务表单对话框
            AnimatedVisibility(
                visible = state.showDialog,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(300)
                ),
                exit = fadeOut() + slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = tween(300)
                )
            ) {
                TaskDialog(
                    task = state.selectedTask,
                    goals = state.goals,
                    habits = state.habits,
                    onDismiss = { viewModel.onEvent(TasksEvent.DismissDialog) },
                    onSave = { task ->
                        if (state.selectedTask == null) {
                            viewModel.onEvent(TasksEvent.AddTask(task))
                        } else {
                            viewModel.onEvent(TasksEvent.UpdateTask(task))
                        }
                    }
                )
            }
        }
    }
} 