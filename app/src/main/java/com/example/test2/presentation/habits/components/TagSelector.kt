package com.example.test2.presentation.habits.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.test2.data.model.NoteTag

/**
 * 标签选择器组件
 *
 * @param selectedTags 当前选择的标签列表
 * @param onTagsChanged 标签变更回调
 * @param modifier Modifier修饰符
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    selectedTags: List<NoteTag>,
    onTagsChanged: (List<NoteTag>) -> Unit,
    modifier: Modifier = Modifier
) {
    // 预设标签列表
    val predefinedTags = remember {
        listOf(
            NoteTag(id = "1", name = "进步", color = 0xFF4CAF50.toInt()),
            NoteTag(id = "2", name = "挑战", color = 0xFFFF9800.toInt()),
            NoteTag(id = "3", name = "反思", color = 0xFF2196F3.toInt()),
            NoteTag(id = "4", name = "成就", color = 0xFFE91E63.toInt()),
            NoteTag(id = "5", name = "动力", color = 0xFF9C27B0.toInt()),
            NoteTag(id = "6", name = "技巧", color = 0xFF3F51B5.toInt()),
            NoteTag(id = "7", name = "环境", color = 0xFF009688.toInt()),
            NoteTag(id = "8", name = "其他", color = 0xFF607D8B.toInt())
        )
    }
    
    // 是否展开更多标签
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "添加标签",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "${selectedTags.size}/3",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 已选标签
            if (selectedTags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(selectedTags) { tag ->
                        TagChip(
                            tag = tag,
                            selected = true,
                            onClick = {
                                // 移除标签
                                val updatedTags = selectedTags.toMutableList().apply {
                                    remove(tag)
                                }
                                onTagsChanged(updatedTags)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // 标签选择器 - 常用标签行
            if (!expanded) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 显示前4个标签
                    items(predefinedTags.take(4)) { tag ->
                        val isSelected = selectedTags.any { it.id == tag.id }
                        TagChip(
                            tag = tag,
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    // 移除标签
                                    val updatedTags = selectedTags.toMutableList().apply {
                                        removeAll { it.id == tag.id }
                                    }
                                    onTagsChanged(updatedTags)
                                } else if (selectedTags.size < 3) {
                                    // 添加标签（最多3个）
                                    onTagsChanged(selectedTags + tag)
                                }
                            },
                            enabled = !isSelected || selectedTags.size < 3
                        )
                    }
                    
                    // 展开更多按钮
                    item {
                        ExpandButton(onClick = { expanded = true })
                    }
                }
            } else {
                // 展开的标签网格
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    predefinedTags.forEach { tag ->
                        val isSelected = selectedTags.any { it.id == tag.id }
                        TagChip(
                            tag = tag,
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    // 移除标签
                                    val updatedTags = selectedTags.toMutableList().apply {
                                        removeAll { it.id == tag.id }
                                    }
                                    onTagsChanged(updatedTags)
                                } else if (selectedTags.size < 3) {
                                    // 添加标签（最多3个）
                                    onTagsChanged(selectedTags + tag)
                                }
                            },
                            enabled = !isSelected || selectedTags.size < 3
                        )
                    }
                }
                
                // 收起按钮
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { expanded = false }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "收起",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 标签芯片组件
 */
@Composable
fun TagChip(
    tag: NoteTag,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val tagColor = Color(tag.color)
    val backgroundColor = if (selected) {
        tagColor.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (selected) {
        tagColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val borderColor = if (selected) {
        tagColor
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

/**
 * 展开更多按钮
 */
@Composable
fun ExpandButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "展开更多",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = "更多",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 