package com.example.test2.presentation.habits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.InlineContent
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteImage

/**
 * 富文本笔记详情组件
 *
 * @param note 笔记数据
 * @param onEdit 编辑回调
 * @param onClose 关闭回调
 * @param modifier Modifier修饰符
 */
@Composable
fun RichNoteDetail(
    note: HabitNote,
    onEdit: (HabitNote) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 添加详情组件的日志
    android.util.Log.d("RichNoteDetail", "显示笔记详情: ID=${note.id}, 标题=${note.title}")
    android.util.Log.d("RichNoteDetail", "笔记图片数量: ${note.images.size}")
    android.util.Log.d("RichNoteDetail", "富文本内容长度: ${note.richContent.length}")
    android.util.Log.d("RichNoteDetail", "普通文本内容长度: ${note.content.length}")
    
    // 确认笔记中是否有图片
    if (note.images.isNotEmpty()) {
        android.util.Log.d("RichNoteDetail", "笔记中包含 ${note.images.size} 张图片")
        note.images.forEachIndexed { index, image ->
            android.util.Log.d("RichNoteDetail", "图片[$index]: ID=${image.id}, URI=${image.uri}")
        }
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题栏
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 标题
                Text(
                    text = note.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                // 按钮组
                Row {
                    // 编辑按钮
                    IconButton(onClick = { onEdit(note) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // 关闭按钮
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // 日期
            Text(
                text = note.getFormattedDate(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // 内容区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // 如果有富文本内容，显示富文本
                if (note.richContent.isNotEmpty()) {
                    android.util.Log.d("RichNoteDetail", "使用富文本内容显示")
                    RichTextContent(note = note)
                } else {
                    // 否则显示普通文本
                    android.util.Log.d("RichNoteDetail", "使用普通文本内容显示")
                    Text(
                        text = note.content,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // 如果有图片但没有富文本，单独显示图片
                if (note.images.isNotEmpty() && note.richContent.isEmpty()) {
                    android.util.Log.d("RichNoteDetail", "单独显示图片列表")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 图片列表
                    note.images.forEach { image ->
                        ImageDisplay(image = image)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            
            // 心情和标签区域
            if (note.mood != null || note.tags.isNotEmpty()) {
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // 心情
                if (note.mood != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "心情: ",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when(note.mood) {
                                NoteMood.VERY_HAPPY -> "非常开心"
                                NoteMood.HAPPY -> "开心"
                                NoteMood.NEUTRAL -> "平静"
                                NoteMood.SAD -> "难过"
                                NoteMood.VERY_SAD -> "非常难过"
                                NoteMood.TIRED -> "疲惫"
                                NoteMood.FRUSTRATED -> "沮丧"
                            },
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // 标签
                if (note.tags.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "标签: ",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = note.tags.joinToString(", ") { it.name },
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 富文本内容显示
 */
@Composable
private fun RichTextContent(note: HabitNote) {
    val context = LocalContext.current
    
    // 添加调试日志
    android.util.Log.d("RichTextContent", "显示笔记 ID=${note.id}, 标题=${note.title}")
    android.util.Log.d("RichTextContent", "笔记图片数量: ${note.images.size}")
    android.util.Log.d("RichTextContent", "富文本内容长度: ${note.richContent.length}")
    
    if (note.images.isNotEmpty()) {
        note.images.forEachIndexed { index, image ->
            android.util.Log.d("RichTextContent", "图片[$index]: ID=${image.id}, URI=${image.uri}")
        }
    }
    
    val inlineContents = note.getInlineContentList()
    
    // 记录内联内容信息
    android.util.Log.d("RichTextContent", "解析出 ${inlineContents.size} 个内联内容块")
    val textCount = inlineContents.count { it is InlineContent.Text }
    val imageCount = inlineContents.count { it is InlineContent.Image }
    android.util.Log.d("RichTextContent", "内容类型: $textCount 文本块, $imageCount 图片块")
    
    // 如果没有内联内容，显示普通文本
    if (inlineContents.isEmpty()) {
        android.util.Log.w("RichTextContent", "没有解析出内联内容，显示普通文本")
        Text(
            text = note.content,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        return
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 遍历并显示内联内容
        inlineContents.forEachIndexed { index, content ->
            when (content) {
                is InlineContent.Text -> {
                    // 显示文本内容
                    if (content.content.isNotEmpty()) {
                        Text(
                            text = content.content,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                is InlineContent.Image -> {
                    // 添加图片调试日志
                    android.util.Log.d("RichTextContent", "准备显示图片 $index: ID=${content.noteImage.id}, URI=${content.noteImage.uri}")
                    val androidUri = content.noteImage.getAndroidUri()
                    android.util.Log.d("RichTextContent", "图片解析后的URI: $androidUri")
                    
                    // 显示图片内容
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(content.noteImage.getAndroidUri())
                                .crossfade(true)
                                .build(),
                            contentDescription = content.altText.ifEmpty { "笔记图片" },
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    
                    // 如果有图片描述，显示描述
                    if (content.altText.isNotEmpty()) {
                        Text(
                            text = content.altText,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 图片显示组件
 */
@Composable
private fun ImageDisplay(image: NoteImage) {
    val context = LocalContext.current
    
    // 添加日志
    android.util.Log.d("ImageDisplay", "显示图片: ID=${image.id}, URI=${image.uri}")
    val androidUri = image.getAndroidUri()
    android.util.Log.d("ImageDisplay", "解析后的URI: $androidUri")
    
    // 图片容器
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 图片显示
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(androidUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "笔记图片",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
} 