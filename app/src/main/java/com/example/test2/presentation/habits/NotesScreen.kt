package com.example.test2.presentation.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteImage
import com.example.test2.data.model.NoteTag
import com.example.test2.data.model.NoteMood
import com.example.test2.presentation.habits.components.NoteCard
import com.example.test2.presentation.habits.components.NoteDetail
import com.example.test2.presentation.habits.components.NoteEditor
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Calendar
import java.util.UUID
import com.example.test2.presentation.components.LoadingView

/**
 * 笔记屏幕
 * 
 * @param habitId 习惯ID（如果是查看特定习惯的笔记）
 * @param onNavigateBack 返回回调
 * @param modifier Modifier修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    habitId: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 状态管理
    var isLoading by remember { mutableStateOf(true) }
    val notes = remember { mutableStateListOf<HabitNote>() }
    var selectedNote by remember { mutableStateOf<HabitNote?>(null) }
    var showNoteDetail by remember { mutableStateOf(false) }
    var showNoteEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<HabitNote?>(null) }
    var showEmptyState by remember { mutableStateOf(false) }
    
    // 顶部应用栏滚动行为
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // 模拟加载数据
    LaunchedEffect(habitId) {
        isLoading = true
        delay(1000) // 模拟网络延迟
        notes.clear()
        val sampleNotes = generateSampleNotes(habitId)
        notes.addAll(sampleNotes)
        isLoading = false
        showEmptyState = sampleNotes.isEmpty()
    }
    
    // 主界面骨架
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = habitId?.let { "习惯笔记" } ?: "所有笔记",
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* 搜索功能 */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索笔记"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingNote = null
                    showNoteEditor = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "新建笔记"
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "写笔记")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 加载状态
            if (isLoading) {
                LoadingView("正在加载笔记...")
            } 
            // 空状态
            else if (showEmptyState) {
                EmptyNotesView()
            } 
            // 笔记列表
            else {
                NotesList(
                    notes = notes,
                    onNoteClick = { note ->
                        selectedNote = note
                        showNoteDetail = true
                    }
                )
            }
        }
    }
    
    // 笔记详情对话框
    if (showNoteDetail && selectedNote != null) {
        Dialog(onDismissRequest = { showNoteDetail = false }) {
            NoteDetail(
                note = selectedNote!!,
                onEdit = { note ->
                    editingNote = note
                    showNoteDetail = false
                    showNoteEditor = true
                },
                onClose = { showNoteDetail = false }
            )
        }
    }
    
    // 笔记编辑器
    AnimatedVisibility(
        visible = showNoteEditor,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NoteEditor(
                habitId = habitId ?: "",
                existingNote = editingNote,
                onSave = { note ->
                    val index = notes.indexOfFirst { it.id == note.id }
                    if (index >= 0) {
                        notes[index] = note
                    } else {
                        notes.add(0, note)
                    }
                    showNoteEditor = false
                },
                onCancel = { showNoteEditor = false }
            )
        }
    }
}

/**
 * 笔记列表组件
 */
@Composable
fun NotesList(
    notes: List<HabitNote>,
    onNoteClick: (HabitNote) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(notes) { note ->
            NoteCard(
                note = note,
                onClick = { onNoteClick(note) }
            )
        }
    }
}

/**
 * 空笔记视图
 */
@Composable
fun EmptyNotesView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.height(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "还没有笔记",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "记录你的想法和感受，跟踪习惯的进度和体验。点击下方的按钮开始创建第一篇笔记。",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

/**
 * 生成示例笔记
 */
private fun generateSampleNotes(habitId: String?): List<HabitNote> {
    val sampleNotes = mutableListOf<HabitNote>()
    
    // 如果是查看所有笔记，添加一些示例
    if (habitId == null) {
        return listOf(
            HabitNote(
                id = "note1",
                habitId = "habit1",
                title = "开始我的晨间冥想",
                content = "今天是我开始晨间冥想的第一天，感觉非常棒！我在早上7点起床，花了15分钟进行简单的呼吸冥想。" +
                        "开始时有点困难，思绪总是不停地飘走，但我努力将注意力带回到呼吸上。" +
                        "结束后，感觉整个人都清醒了，接下来的一天都充满了能量。我决定明天继续，并尝试延长到20分钟。",
                mood = NoteMood.VERY_HAPPY,
                tags = listOf(
                    NoteTag.getAllTags()[0],
                    NoteTag.getAllTags()[2]
                ),
                images = listOf(
                    NoteImage(
                        id = "img1",
                        uri = "sample_uri_1",
                        description = "我的冥想角落",
                        createdAt = getDaysAgo(1)
                    )
                ),
                createdAt = getDaysAgo(1),
                updatedAt = getDaysAgo(1)
            ),
            HabitNote(
                id = "note2",
                habitId = "habit2",
                title = "跑步日记 - 突破个人记录！",
                content = "今天在公园完成了5公里跑步，时间是23分36秒，比上周快了整整2分钟！" +
                        "天气很好，阳光明媚但不热，简直是跑步的完美天气。" +
                        "前3公里感觉很轻松，保持了稳定的配速。最后2公里开始有点吃力，但我告诉自己要坚持，结果跑出了个人最好成绩。" +
                        "回家后做了适当的拉伸，腿部有点酸痛但感觉非常满足。明天打算休息一天，让身体恢复。",
                mood = NoteMood.HAPPY,
                tags = listOf(
                    NoteTag.getAllTags()[1],
                    NoteTag.getAllTags()[4]
                ),
                images = listOf(
                    NoteImage(
                        id = "img2",
                        uri = "sample_uri_2",
                        description = "今天的跑步路线",
                        createdAt = getDaysAgo(2)
                    ),
                    NoteImage(
                        id = "img3",
                        uri = "sample_uri_3",
                        description = "跑步后的自拍",
                        createdAt = getDaysAgo(2)
                    )
                ),
                createdAt = getDaysAgo(2),
                updatedAt = getDaysAgo(2)
            ),
            HabitNote(
                id = "note3",
                habitId = "habit3",
                title = "阅读挑战 - 《原子习惯》",
                content = "今天读完了《原子习惯》的第三章，关于如何建立一个好习惯的四个步骤：提示、渴望、反应和奖励。" +
                        "作者提到，要使习惯变得明显，就需要确定何时何地执行它。我决定将阅读习惯与晚餐后的时间绑定，这样就有了一个清晰的提示。" +
                        "最有启发的是关于环境设计的部分，如何通过改变环境来让好习惯变得容易，让坏习惯变得困难。" +
                        "明天计划继续阅读第四章，并尝试应用今天学到的知识。",
                mood = NoteMood.NEUTRAL,
                tags = listOf(
                    NoteTag.getAllTags()[3],
                    NoteTag.getAllTags()[5]
                ),
                images = emptyList(),
                createdAt = getDaysAgo(3),
                updatedAt = getDaysAgo(3)
            )
        )
    }
    
    // 如果是查看特定习惯的笔记，添加相关示例
    if (habitId == "habit1") {
        sampleNotes.add(
            HabitNote(
                id = "note1",
                habitId = habitId,
                title = "开始我的晨间冥想",
                content = "今天是我开始晨间冥想的第一天，感觉非常棒！我在早上7点起床，花了15分钟进行简单的呼吸冥想。" +
                        "开始时有点困难，思绪总是不停地飘走，但我努力将注意力带回到呼吸上。" +
                        "结束后，感觉整个人都清醒了，接下来的一天都充满了能量。我决定明天继续，并尝试延长到20分钟。",
                mood = NoteMood.VERY_HAPPY,
                tags = listOf(
                    NoteTag.getAllTags()[0],
                    NoteTag.getAllTags()[2]
                ),
                images = listOf(
                    NoteImage(
                        id = "img1",
                        uri = "sample_uri_1",
                        description = "我的冥想角落",
                        createdAt = getDaysAgo(1)
                    )
                ),
                createdAt = getDaysAgo(1),
                updatedAt = getDaysAgo(1)
            )
        )
    }
    
    return sampleNotes
}

/**
 * 获取指定天数前的日期
 */
private fun getDaysAgo(days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -days)
    return calendar.time
} 