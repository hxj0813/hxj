package com.example.test2.presentation.habits.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.test2.data.model.InlineContent
import com.example.test2.data.model.NoteImage
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 富文本编辑器组件
 *
 * @param initialContent 初始内容列表
 * @param onContentChanged 内容变更回调
 * @param onAddImage 添加图片回调
 * @param onRemoveImage 删除图片回调
 * @param modifier Modifier修饰符
 */
@Composable
fun RichTextEditor(
    initialContent: List<InlineContent>,
    onContentChanged: (List<InlineContent>) -> Unit,
    onAddImage: () -> Unit,
    onRemoveImage: (NoteImage) -> Unit,
    modifier: Modifier = Modifier
) {
    val contents = remember { mutableStateListOf<InlineContent>() }
    val coroutineScope = rememberCoroutineScope()

    // 初始化内容
    LaunchedEffect(initialContent) {
        contents.clear()
        if (initialContent.isNotEmpty()) {
            contents.addAll(initialContent)
            android.util.Log.d("RichTextEditor", "初始化内容，包含 ${initialContent.size} 个内容块")
            // 记录内容类型统计
            val textCount = contents.count { it is InlineContent.Text }
            val imageCount = contents.count { it is InlineContent.Image }
            android.util.Log.d("RichTextEditor", "内容类型: $textCount 文本块, $imageCount 图片块")
        } else {
            // 添加一个空的文本内容块
            contents.add(InlineContent.Text(content = ""))
            android.util.Log.d("RichTextEditor", "初始化为空，添加空文本块")
        }
    }

    // 当内容变更时通知回调
    LaunchedEffect(contents.toList()) {
        android.util.Log.d("RichTextEditor", "内容已变更，通知回调，${contents.size} 个内容块")
        onContentChanged(contents.toList())
    }

    Column(modifier = modifier) {
        // 内容编辑区域
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(contents.toList()) { content ->
                when (content) {
                    is InlineContent.Text -> {
                        TextEditorBlock(
                            content = content,
                            onTextChanged = { newText ->
                                val index = contents.indexOfFirst { it.contentId == content.contentId }
                                if (index >= 0) {
                                    contents[index] = InlineContent.Text(
                                        id = content.id,
                                        content = newText
                                    )
                                }
                            },
                            onInsertImageAfter = {
                                coroutineScope.launch {
                                    onAddImage()
                                }
                            }
                        )
                    }
                    is InlineContent.Image -> {
                        ImageEditorBlock(
                            content = content,
                            onRemoveImage = {
                                val index = contents.indexOfFirst { it.contentId == content.contentId }
                                if (index >= 0) {
                                    contents.removeAt(index)
                                    onRemoveImage(content.noteImage)
                                    
                                    // 如果删除后没有文本内容，添加一个空文本块
                                    if (contents.isEmpty() || contents.none { it is InlineContent.Text }) {
                                        contents.add(index, InlineContent.Text(content = ""))
                                    }
                                    // 如果相邻没有文本块，在图片后添加一个文本块
                                    else if (index >= contents.size || contents[index] !is InlineContent.Text) {
                                        contents.add(index, InlineContent.Text(content = ""))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 文本块编辑器
 */
@Composable
fun TextEditorBlock(
    content: InlineContent.Text,
    onTextChanged: (String) -> Unit,
    onInsertImageAfter: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 文本编辑器
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = content.content,
            onValueChange = onTextChanged,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
    }
}

/**
 * 图片块编辑器
 */
@Composable
fun ImageEditorBlock(
    content: InlineContent.Image,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // 图片编辑器
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 图片显示
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(content.noteImage.getAndroidUri())
                        .crossfade(true)
                        .build(),
                    contentDescription = content.altText.ifEmpty { "插入的图片" },
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )
                
                // 删除按钮
                IconButton(
                    onClick = onRemoveImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除图片",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
} 