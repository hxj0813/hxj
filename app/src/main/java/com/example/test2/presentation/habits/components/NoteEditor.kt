package com.example.test2.presentation.habits.components

import android.content.res.Configuration
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.test2.data.model.NoteImage
import com.example.test2.data.model.NoteTag
import com.example.test2.data.model.NoteMood
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
 * @param modifier Modifier修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditor(
    habitId: String,
    existingNote: HabitNote? = null,
    onSave: (HabitNote) -> Unit,
    onCancel: () -> Unit,
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
    val selectedImages = remember { 
        mutableStateListOf<NoteImage>().apply {
            if (existingNote?.images != null) {
                addAll(existingNote.images)
            }
        }
    }
    
    // 是否为编辑模式
    val isEditMode = existingNote != null
    
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
                    
                    // 内容编辑器
                    ContentEditor(
                        content = content,
                        onContentChange = { content = it }
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
            TagSelector(
                selectedTags = selectedTags.toList(),
                onTagsChanged = { newTags ->
                    selectedTags.clear()
                    selectedTags.addAll(newTags)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 图片选择器
            ImageSelector(
                images = selectedImages.toList(),
                onAddImage = {
                    // 模拟添加图片 - 在实际应用中会启动图片选择器
                    val newImage = NoteImage(
                        id = UUID.randomUUID().toString(),
                        uri = "sample_uri_${selectedImages.size}",
                        description = "示例图片 ${selectedImages.size + 1}",
                        createdAt = LocalDateTime.now().toDate()
                    )
                    selectedImages.add(newImage)
                },
                onRemoveImage = { selectedImages.remove(it) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 保存按钮
            Button(
                onClick = {
                    val note = if (isEditMode) {
                        existingNote!!.copy(
                            title = title,
                            content = content,
                            mood = selectedMood,
                            tags = selectedTags.toList(),
                            images = selectedImages.toList(),
                            updatedAt = LocalDateTime.now().toDate()
                        )
                    } else {
                        HabitNote(
                            id = UUID.randomUUID().toString(),
                            habitId = habitId,
                            title = title,
                            content = content,
                            mood = selectedMood,
                            tags = selectedTags.toList(),
                            images = selectedImages.toList(),
                            createdAt = LocalDateTime.now().toDate(),
                            updatedAt = LocalDateTime.now().toDate()
                        )
                    }
                    onSave(note)
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
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
 * 内容编辑器组件
 */
@Composable
fun ContentEditor(
    content: String,
    onContentChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 编辑器工具栏（可以实现富文本编辑功能，这里只是一个装饰）
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            // 样式指示器 - 在实际应用中可以实现富文本功能
            FormatIndicator("B", MaterialTheme.colorScheme.primary, isBold = true)
            FormatIndicator("I", MaterialTheme.colorScheme.primary)
            FormatIndicator("U", MaterialTheme.colorScheme.primary)
        }

        // 使用BasicTextField实现自定义样式
        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // 可以在此处处理完成操作
                }
            ),
            decorationBox = { innerTextField ->
                Box {
                    // 占位文本
                    if (content.isEmpty()) {
                        Text(
                            text = "在这里记录你的想法和感受...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 24.sp
                        )
                    }
                    
                    // 实际的文本字段
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
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