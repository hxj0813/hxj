package com.example.test2.presentation.habits.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteTag
import com.example.test2.presentation.habits.NotesState

/**
 * 笔记筛选对话框
 *
 * @param state 笔记状态
 * @param onFilterChange 筛选条件变更回调
 * @param onDismiss 关闭回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteFilterModal(
    state: NotesState,
    onFilterChange: (NoteMood?, NoteTag?, SortBy, SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    // 状态管理
    var selectedMood: NoteMood? by remember { mutableStateOf(null) }
    var selectedTag: NoteTag? by remember { mutableStateOf(null) }
    var sortBy by remember { mutableStateOf(SortBy.DATE) }
    var sortOrder by remember { mutableStateOf(SortOrder.DESCENDING) }
    
    // 从所有笔记中提取唯一标签
    val allTags = remember(state.notes) {
        state.notes.flatMap { it.tags }.distinctBy { it.id }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "筛选笔记",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
                
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // 心情筛选
                Text(
                    text = "按心情筛选",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    val moods = NoteMood.values()
                    
                    // 添加"全部"选项
                    item {
                        MoodFilterChip(
                            mood = null,
                            selected = selectedMood == null,
                            onSelected = { selectedMood = null }
                        )
                    }
                    
                    // 所有心情选项
                    items(moods) { mood ->
                        MoodFilterChip(
                            mood = mood,
                            selected = selectedMood == mood,
                            onSelected = { selectedMood = mood }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标签筛选
                if (allTags.isNotEmpty()) {
                    Text(
                        text = "按标签筛选",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // 添加"全部"选项
                        item {
                            FilterChip(
                                selected = selectedTag == null,
                                onClick = { selectedTag = null },
                                label = { Text("全部") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        
                        // 所有标签选项
                        items(allTags) { tag ->
                            FilterChip(
                                selected = selectedTag?.id == tag.id,
                                onClick = { selectedTag = tag },
                                label = { Text(tag.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 排序选项
                Text(
                    text = "排序方式",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 排序依据
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        SortByOption(
                            title = "按日期", 
                            selected = sortBy == SortBy.DATE,
                            onClick = { sortBy = SortBy.DATE }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        SortByOption(
                            title = "按标题", 
                            selected = sortBy == SortBy.TITLE,
                            onClick = { sortBy = SortBy.TITLE }
                        )
                    }
                    
                    Divider(
                        modifier = Modifier
                            .height(64.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    // 排序顺序
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        SortByOption(
                            title = "降序", 
                            selected = sortOrder == SortOrder.DESCENDING,
                            onClick = { sortOrder = SortOrder.DESCENDING }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        SortByOption(
                            title = "升序", 
                            selected = sortOrder == SortOrder.ASCENDING,
                            onClick = { sortOrder = SortOrder.ASCENDING }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 应用按钮
                Button(
                    onClick = { 
                        onFilterChange(selectedMood, selectedTag, sortBy, sortOrder)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "应用筛选"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("应用筛选", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * 心情筛选芯片
 */
@Composable
fun MoodFilterChip(
    mood: NoteMood?,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onSelected() }
            .background(
                if (selected) 
                    MaterialTheme.colorScheme.primaryContainer
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) 
                    Color.Transparent
                else 
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = CircleShape
            )
    ) {
        val text = if (mood == null) {
            "全部"
        } else {
            when (mood) {
                NoteMood.VERY_HAPPY -> "😄"
                NoteMood.HAPPY -> "🙂"
                NoteMood.NEUTRAL -> "😐"
                NoteMood.SAD -> "😔"
                NoteMood.VERY_SAD -> "😢"
                NoteMood.TIRED -> "🥱"
                NoteMood.FRUSTRATED -> "😤"
            }
        }
        
        Text(
            text = text,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 排序选项组件
 */
@Composable
fun SortByOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title)
    }
}

/**
 * 排序依据枚举
 */
enum class SortBy {
    DATE,
    TITLE
}

/**
 * 排序顺序枚举
 */
enum class SortOrder {
    ASCENDING,
    DESCENDING
} 