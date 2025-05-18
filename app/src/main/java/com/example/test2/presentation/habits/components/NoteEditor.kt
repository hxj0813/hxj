package com.example.test2.presentation.habits.components

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteTag
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteImage
import com.example.test2.data.model.InlineContent
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.UUID

/**
 * 笔记编辑器组件
 * 
 * @param habitId 习惯ID
 * @param existingNote 现有笔记（如果是编辑模式）
 * @param onSave 保存回调
 * @param onCancel 取消回调
 * @param onAddImage 添加图片回调
 * @param onRemoveImage 删除图片回调
 * @param onViewImage 查看图片回调
 * @param isImageProcessing 是否正在处理图片
 * @param modifier Modifier修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditor(
    habitId: String,
    existingNote: HabitNote? = null,
    onSave: (HabitNote) -> Unit,
    onCancel: () -> Unit,
    onAddImage: (Uri) -> Unit = {},
    onRemoveImage: (NoteImage) -> Unit = {},
    onViewImage: (NoteImage) -> Unit = {},
    isImageProcessing: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 状态管理
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var selectedMood by remember { mutableStateOf(existingNote?.mood ?: NoteMood.NEUTRAL) }
    val selectedTags = remember { 
        mutableStateListOf<NoteTag>().apply {
            if (existingNote?.tags != null) {
                addAll(existingNote.tags)
            }
        }
    }
    
    // 记住图片列表
    val images = remember {
        mutableStateListOf<NoteImage>().apply {
            existingNote?.images?.let { addAll(it) }
        }
    }
    
    // 记住富文本内容列表
    val inlineContentList = remember { 
        mutableStateListOf<InlineContent>().apply {
            // 如果有现有笔记，使用其富文本内容
            if (existingNote != null) {
                addAll(existingNote.getInlineContentList())
            }
        }
    }
    
    // 当前焦点内容的ID
    var focusedContentId by remember { mutableStateOf<String?>(null) }
    
    // 更新纯文本内容（用于兼容旧版本）
    LaunchedEffect(inlineContentList.toList()) {
        content = InlineContent.contentListToString(inlineContentList)
    }
    
    // 图片选择器
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAddImage(it) }
    }
    
    // 是否为编辑模式
    val isEditMode = existingNote != null
    
    /**
     * 将图片添加到富文本内容
     */
    fun addImageToRichContent(newImage: NoteImage) {
        android.util.Log.d("NoteEditor", "处理新图片: ID=${newImage.id}, URI=${newImage.uri}")
        
        // 记录富文本内容信息
        android.util.Log.d("NoteEditor", "当前富文本包含 ${inlineContentList.size} 个内容块")
        
        // 创建内联图片内容
        val imageContent = InlineContent.Image(
            id = UUID.randomUUID().toString(),
            noteImage = newImage,
            altText = ""
        )
        
        // 获取当前的内容列表（转成可变列表）
        val updatedContentList = if (inlineContentList.isEmpty()) {
            // 如果内容为空，先添加一个空文本，再添加图片，然后再添加一个空文本
            android.util.Log.d("NoteEditor", "内容为空，创建新的内容结构")
            mutableListOf(
                InlineContent.Text(content = ""),
                imageContent,
                InlineContent.Text(content = "")
            )
        } else {
            // 转换为可变列表并找到最后一个文本内容
            val mutableList = inlineContentList.toMutableList()
            val textIndex = mutableList.indexOfLast { it is InlineContent.Text }
            
            if (textIndex >= 0) {
                android.util.Log.d("NoteEditor", "在文本块 $textIndex 后插入图片")
                // 在文本后插入图片
                mutableList.add(textIndex + 1, imageContent)
                // 确保图片后有文本块
                if (textIndex + 2 >= mutableList.size || mutableList[textIndex + 2] !is InlineContent.Text) {
                    android.util.Log.d("NoteEditor", "在图片后添加新文本块")
                    mutableList.add(textIndex + 2, InlineContent.Text(content = ""))
                }
            } else {
                // 没有文本块，直接添加到末尾，并确保有文本块
                android.util.Log.d("NoteEditor", "没有文本块，将图片添加到末尾")
                mutableList.add(imageContent)
                mutableList.add(InlineContent.Text(content = ""))
            }
            mutableList
        }
        
        // 更新内容列表并记录日志
        android.util.Log.d("NoteEditor", "更新后的富文本包含 ${updatedContentList.size} 个内容块")
        // 记录所有内容类型
        updatedContentList.forEachIndexed { index, content ->
            when (content) {
                is InlineContent.Text -> 
                    android.util.Log.d("NoteEditor", "Content[$index]: Text with ${content.content.length} chars")
                is InlineContent.Image -> 
                    android.util.Log.d("NoteEditor", "Content[$index]: Image with ID=${content.noteImage.id}")
            }
        }
        
        // 清除并重新添加内容
        inlineContentList.clear()
        inlineContentList.addAll(updatedContentList)
        
        // 生成并记录当前的JSON内容，用于调试
        val currentJson = InlineContent.contentListToJson(inlineContentList)
        android.util.Log.d("NoteEditor", "当前富文本JSON (${currentJson.length} chars)")
        // 验证是否包含图片内容
        if (currentJson.contains("\"type\":\"image\"")) {
            android.util.Log.d("NoteEditor", "JSON中包含图片内容")
        } else {
            android.util.Log.e("NoteEditor", "错误：JSON中未包含图片内容！")
        }
    }
    
    // 创建骨架
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isEditMode) "编辑笔记" else "添加新笔记",
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 添加图片按钮
                    IconButton(
                        onClick = { imagePicker.launch("image/*") },
                        enabled = !isImageProcessing && images.size < 10
                    ) {
                        if (isImageProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "添加图片"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        
        // 主内容
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 卡片容器
            Card(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 标题编辑器
                    TitleEditor(
                        title = title,
                        onTitleChange = { title = it }
                    )
                    
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    // 富文本编辑器
                    RichTextEditor(
                        initialContent = inlineContentList.toList(),
                        onContentChanged = { newContent ->
                            inlineContentList.clear()
                            inlineContentList.addAll(newContent)
                        },
                        onAddImage = { /* 不执行任何操作 */ },
                        onRemoveImage = { image ->
                            onRemoveImage(image)
                            images.remove(image)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 心情选择器
            MoodSelector(
                selectedMood = selectedMood,
                onMoodSelected = { selectedMood = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 标签选择器
            NoteTagSelector(
                selectedTags = selectedTags.toList(),
                onTagsChanged = { newTags ->
                    selectedTags.clear()
                    selectedTags.addAll(newTags)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 保存按钮
            Button(
                onClick = {
                    // 生成富文本内容的JSON字符串
                    val richContentJson = InlineContent.contentListToJson(inlineContentList.toList())
                    
                    val note = if (isEditMode) {
                        // 编辑模式
                        existingNote!!.copy(
                            title = title,
                            content = content, // 兼容纯文本
                            richContent = richContentJson, // 富文本内容
                            mood = selectedMood,
                            tags = selectedTags.toList(),
                            images = images.toList(), // 使用当前图片列表
                            updatedAt = LocalDateTime.now().toDate()
                        )
                    } else {
                        // 创建模式
                        HabitNote(
                            id = UUID.randomUUID().toString(),
                            habitId = habitId,
                            title = title,
                            content = content, // 兼容纯文本
                            richContent = richContentJson, // 富文本内容
                            mood = selectedMood,
                            tags = selectedTags.toList(),
                            images = images.toList(),
                            createdAt = LocalDateTime.now().toDate(),
                            updatedAt = LocalDateTime.now().toDate()
                        )
                    }
                    
                    // 记录最终保存的图片信息
                    android.util.Log.d("NoteEditor", "保存笔记，ID: ${note.id}, 标题: ${note.title}")
                    android.util.Log.d("NoteEditor", "笔记图片数量: ${note.images.size}")
                    note.images.forEachIndexed { index, image ->
                        android.util.Log.d("NoteEditor", "最终保存图片[$index]: ${image.uri}")
                    }
                    
                    // 保存笔记
                    onSave(note)
                },
                enabled = title.isNotBlank() && inlineContentList.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "保存",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (isEditMode) "保存修改" else "保存笔记",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    
    // 处理图片添加事件
    LaunchedEffect(existingNote) {
        // 当编辑的笔记发生变化时（比如添加了图片）
        existingNote?.let { note ->
            // 获取当前显示的图片ID
            val currentImageIds = inlineContentList
                .filterIsInstance<InlineContent.Image>()
                .map { it.noteImage.id }
                .toSet()
            
            // 获取笔记中的图片ID
            val noteImageIds = note.images.map { it.id }.toSet()
            
            // 找到新添加的图片
            val newImages = note.images.filter { image -> 
                image.id !in currentImageIds 
            }
            
            if (newImages.isNotEmpty()) {
                android.util.Log.d("NoteEditor", "检测到笔记中有 ${newImages.size} 张新图片，添加到富文本内容中")
                
                // 将新图片添加到富文本内容
                newImages.forEach { newImage ->
                    addImageToRichContent(newImage)
                }
            }
        }
    }
    
    // 处理图片添加事件
    LaunchedEffect(existingNote?.images) {
        // 省略原有代码...当外部图片列表更新时，更新本地图片列表
        if (existingNote != null && existingNote.images.isNotEmpty()) {
            // 保存当前图片数量用于检测变化
            val currentSize = images.size
            
            // 清除并重新添加所有图片
            images.clear()
            images.addAll(existingNote.images)
            
            android.util.Log.d("NoteEditor", "从外部更新图片列表，之前 $currentSize 张，现在 ${images.size} 张")
        }
    }
    
    // 处理新图片添加
    LaunchedEffect(images.size) {
        // 当本地图片列表大小变化时，可能是通过按钮直接添加的图片
        if (existingNote == null || images.size > (existingNote.images.size)) {
            // 找出新添加的图片
            val newImages = images.filter { image ->
                // 不在当前富文本内容中
                inlineContentList.none { content -> 
                    content is InlineContent.Image && content.noteImage.id == image.id
                }
            }
            
            android.util.Log.d("NoteEditor", "检测到本地添加的 ${newImages.size} 张新图片")
            
            // 处理所有新图片
            newImages.forEach { newImage ->
                addImageToRichContent(newImage)
            }
        }
    }
}

/**
 * 标题编辑器组件
 */
@Composable
fun TitleEditor(
    title: String,
    onTitleChange: (String) -> Unit
) {
    // 使用BasicTextField实现自定义样式
    BasicTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        textStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (title.isEmpty()) {
                    Text(
                        text = "给笔记取个标题...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }
                innerTextField()
            }
        }
    )
}

/**
 * 格式指示器组件
 */
@Composable
fun FormatIndicator(
    text: String,
    color: Color,
    isBold: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 将LocalDateTime转换为Date
 */
private fun LocalDateTime.toDate(): Date {
    return Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun NoteEditorPreview() {
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            NoteEditor(
                habitId = "sample_habit_id",
                onSave = {},
                onCancel = {}
            )
        }
    }
} 