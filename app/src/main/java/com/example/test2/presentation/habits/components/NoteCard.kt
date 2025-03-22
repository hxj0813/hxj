package com.example.test2.presentation.habits.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.model.HabitNote
import com.example.test2.util.DateTimeUtil

/**
 * 为HabitNote添加预览内容方法
 */
private fun HabitNote.previewContent(maxLength: Int): String {
    val content = this.content ?: return "无内容"
    return if (content.length <= maxLength) content else "${content.take(maxLength)}..."
}

/**
 * 笔记卡片组件
 * 
 * @param note 笔记数据
 * @param onClick 点击回调
 * @param modifier Modifier修饰符
 */
@Composable
fun NoteCard(
    note: HabitNote,
    onClick: (HabitNote) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(note) }
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 笔记标题行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "查看详情",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 创建日期
            Text(
                text = "创建于 ${DateTimeUtil.formatDate(note.createdAt)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontStyle = FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 预览内容
            Text(
                text = note.previewContent(maxLength = if (expanded) 300 else 100),
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp,
                maxLines = if (expanded) 10 else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { expanded = !expanded }
            )
            
            // 如果内容超过限制，显示展开/收起按钮
            if (note.content.length > 100) {
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (expanded) "收起" else "展开",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { expanded = !expanded }
                        .padding(4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 底部信息行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 心情指示器
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(getMoodColor(note.mood))
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = getMoodText(note.mood),
                        fontSize = 12.sp,
                        color = getMoodColor(note.mood)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = getMoodEmoji(note.mood),
                        fontSize = 14.sp
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 图片数量指示器
                    if (note.images.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = "图片",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(2.dp))
                            
                            Text(
                                text = "${note.images.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // 标签
                    if (note.tags.isNotEmpty()) {
                        NoteTagList(tags = note.tags, maxDisplayCount = 3)
                    }
                }
            }
        }
    }
}

/**
 * 笔记细节组件
 */
@Composable
fun NoteDetail(
    note: HabitNote,
    onEdit: (HabitNote) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 笔记标题
            Text(
                text = note.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 日期和心情行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 创建日期
                Text(
                    text = "创建于 ${DateTimeUtil.formatDate(note.createdAt)}",
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 心情指示器
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mood,
                        contentDescription = "心情",
                        tint = getMoodColor(note.mood),
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "${getMoodText(note.mood)} ${getMoodEmoji(note.mood)}",
                        fontSize = 14.sp,
                        color = getMoodColor(note.mood)
                    )
                }
            }
            
            // 标签行
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "标签:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    NoteTagList(tags = note.tags, maxDisplayCount = 5)
                }
            }
            
            // 内容分隔线
            Spacer(modifier = Modifier.height(16.dp))
            
            androidx.compose.material3.Divider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 笔记内容
            Text(
                text = note.content,
                fontSize = 16.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 图片网格
            if (note.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "图片附件",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 复用图片网格组件
                ImageGrid(
                    images = note.images,
                    onImageClick = { /* 查看大图 */ },
                    onRemoveImage = { /* 只读模式，无需处理 */ }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 操作按钮区
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 关闭按钮
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable { onClose() }
                ) {
                    Text(
                        text = "关闭",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 编辑按钮
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable { onEdit(note) }
                ) {
                    Text(
                        text = "编辑",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
} 