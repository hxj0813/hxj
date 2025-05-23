package com.example.test2.presentation.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test2.data.model.Goal
import com.example.test2.presentation.goals.components.GoalCard
import com.example.test2.presentation.goals.components.GoalDialog
import com.example.test2.presentation.theme.PrimaryLight

/**
 * 目标管理屏幕
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()

    // 构建过滤器选项
    val filters = listOf(
        GoalsState.Filter.ALL to "所有目标",
        GoalsState.Filter.IMPORTANT to "重要目标",
        GoalsState.Filter.LONG_TERM to "长期目标",
        GoalsState.Filter.SHORT_TERM to "短期目标",
        GoalsState.Filter.UPCOMING to "即将到期",
        GoalsState.Filter.OVERDUE to "已逾期",
        GoalsState.Filter.COMPLETED to "已完成"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(GoalsEvent.ShowAddGoalDialog) },
                containerColor = PrimaryLight,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加目标"
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
            Column(modifier = Modifier.fillMaxSize()) {
                // 标题和搜索
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
                            text = "我的目标",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { query ->
                                viewModel.onEvent(GoalsEvent.UpdateSearchQuery(query))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("搜索目标...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "搜索"
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
                
                // 过滤器
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { (filter, label) ->
                        FilterChip(
                            selected = state.currentFilter == filter,
                            onClick = { viewModel.onEvent(GoalsEvent.FilterGoals(filter)) },
                            label = { Text(label) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 目标列表
                Box(modifier = Modifier.weight(1f)) {
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
                    } else if (state.filteredGoals.isEmpty()) {
                        // 空状态
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (state.currentFilter) {
                                    GoalsState.Filter.ALL -> "您还没有添加任何目标"
                                    GoalsState.Filter.COMPLETED -> "没有已完成的目标"
                                    GoalsState.Filter.IMPORTANT -> "没有标记为重要的目标"
                                    GoalsState.Filter.LONG_TERM -> "没有长期目标"
                                    GoalsState.Filter.SHORT_TERM -> "没有短期目标"
                                    GoalsState.Filter.UPCOMING -> "没有即将到期的目标"
                                    GoalsState.Filter.OVERDUE -> "没有逾期的目标"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    } else {
                        // 目标列表
                        LazyColumn(
                            state = lazyListState,
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = state.filteredGoals,
                                key = { it.id }
                            ) { goal ->
                                GoalCard(
                                    goal = goal,
                                    onEditClick = {
                                        viewModel.onEvent(GoalsEvent.ShowEditGoalDialog(goal))
                                    },
                                    onDeleteClick = {
                                        viewModel.onEvent(GoalsEvent.DeleteGoal(goal.id))
                                    },
                                    onToggleCompletion = { isCompleted ->
                                        viewModel.onEvent(GoalsEvent.CompleteGoal(goal.id, isCompleted))
                                    },
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                        }
                    }
                }
            }
            
            // 显示目标表单对话框
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
                GoalDialog(
                    goal = state.selectedGoal,
                    onDismiss = { viewModel.onEvent(GoalsEvent.DismissDialog) },
                    onSave = { goal ->
                        if (state.selectedGoal == null) {
                            viewModel.onEvent(GoalsEvent.AddGoal(goal))
                        } else {
                            viewModel.onEvent(GoalsEvent.UpdateGoal(goal))
                        }
                    }
                )
            }
        }
    }
}

/**
 * 过滤选项芯片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = label
    )
} 