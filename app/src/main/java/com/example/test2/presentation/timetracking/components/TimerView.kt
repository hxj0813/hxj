package com.example.test2.presentation.timetracking.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.local.entity.TagCategory
import com.example.test2.data.local.entity.TaskTagEntity
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import com.example.test2.presentation.common.LocalTimeTrackingViewModel
import com.example.test2.presentation.theme.PrimaryLight
import com.example.test2.presentation.timetracking.TimeTrackingEvent
import com.example.test2.presentation.timetracking.TimeTrackingUtils
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.time.Duration.Companion.seconds

/**
 * 时间计时器组件，用于显示和控制正在进行的时间追踪
 */
@Composable
fun TimerView(
    ongoingEntry: TimeEntry?,
    onStopTimer: () -> Unit,
    onStartTimer: (TimeEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    // 获取ViewModel
    val viewModel = LocalTimeTrackingViewModel.current
    val state by viewModel.state.collectAsState()
    val taskTags by viewModel.taskTags.collectAsState(initial = emptyList())
    
    var seconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(ongoingEntry != null) }
    var showTagOptions by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(TimeCategory.WORK) }
    var activityTitle by remember { mutableStateOf("") }
    var selectedTagId by remember { mutableStateOf<String?>(null) }
    
    // 计算已经运行的时间
    LaunchedEffect(ongoingEntry) {
        isRunning = ongoingEntry != null
        if (ongoingEntry != null) {
            // 计算已经经过的秒数
            seconds = (Date().time - ongoingEntry.startTime.time) / 1000
        } else {
            seconds = 0
        }
    }
    
    // 计时器
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            seconds++
        }
    }
    
    // 格式化时间显示
    val formattedTime = remember(seconds) {
        formatTime(seconds)
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isRunning) {
                // 显示正在进行的时间条目
                Text(
                    text = ongoingEntry?.title ?: "正在计时...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 显示标签（如果有）
                if (ongoingEntry?.tagId != null) {
                    val tag = taskTags.find { it.id == ongoingEntry.tagId }
                    if (tag != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(tag.color))
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = tag.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 时间显示
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryLight
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 停止按钮
                FloatingActionButton(
                    onClick = onStopTimer,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "停止"
                    )
                }
            } else {
                // 创建新的时间条目
                OutlinedTextField(
                    value = activityTitle,
                    onValueChange = { activityTitle = it },
                    label = { Text("你在做什么？") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 分类选择
                Text(
                    text = "选择分类",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 分类选项
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TimeCategory.values().forEach { category ->
                        CategoryOption(
                            category = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 显示或隐藏标签选项的按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTagOptions = !showTagOptions }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "添加标签",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Icon(
                        imageVector = if (showTagOptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (showTagOptions) "隐藏标签" else "显示标签"
                    )
                }
                
                // 标签选项
                AnimatedVisibility(
                    visible = showTagOptions,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    CustomTagOptions(
                        tags = taskTags,
                        selectedTagId = selectedTagId,
                        onTagSelected = { selectedTagId = it },
                        onAddNewTag = { viewModel.onEvent(TimeTrackingEvent.ShowTagDialog(null)) }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 开始计时按钮
                FloatingActionButton(
                    onClick = {
                        val title = if (activityTitle.isBlank()) "未命名时间记录" else activityTitle
                        val timeEntry = TimeEntry(
                            title = title,
                            startTime = Date(),
                            category = selectedCategory,
                            tagId = selectedTagId
                        )
                        onStartTimer(timeEntry)
                    },
                    containerColor = PrimaryLight,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "开始"
                    )
                }
            }
        }
    }
}

/**
 * 分类选项
 */
@Composable
private fun CategoryOption(
    category: TimeCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(TimeTrackingUtils.getCategoryColor(category).copy(alpha = if (isSelected) 1f else 0.6f))
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = TimeTrackingUtils.getCategoryName(category),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 自定义标签选项
 */
@Composable
private fun CustomTagOptions(
    tags: List<TaskTagEntity>,
    selectedTagId: String?,
    onTagSelected: (String?) -> Unit,
    onAddNewTag: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 120.dp)
        ) {
            // 添加新标签的选项
            item {
                CustomTagItem(
                    tag = null,
                    isSelected = false,
                    onClick = onAddNewTag
                )
            }
            
            // 现有标签
            items(tags) { tag ->
                CustomTagItem(
                    tag = tag,
                    isSelected = tag.id == selectedTagId,
                    onClick = { onTagSelected(if (tag.id == selectedTagId) null else tag.id) }
                )
            }
        }
    }
}

/**
 * 自定义标签项
 */
@Composable
private fun CustomTagItem(
    tag: TaskTagEntity?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .height(40.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (tag == null) 
                    MaterialTheme.colorScheme.surfaceVariant 
                else 
                    Color(tag.color).copy(alpha = if (isSelected) 1f else 0.6f)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (tag == null) {
            // 添加新标签
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加新标签",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // 显示标签名称
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (isLightColor(Color(tag.color))) Color.Black else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 判断颜色是否为浅色
 */
private fun isLightColor(color: Color): Boolean {
    val darkness = 1 - (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return darkness < 0.5
}

/**
 * 格式化时间显示
 */
private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}
