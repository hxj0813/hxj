package com.example.test2.presentation.habits

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteTag
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteImage
import com.example.test2.presentation.habits.components.HabitSelector
import com.example.test2.presentation.habits.components.ImageViewer
import com.example.test2.presentation.habits.components.NoteCard
import com.example.test2.presentation.habits.components.RichNoteDetail
import com.example.test2.presentation.habits.components.NoteEditor
import com.example.test2.presentation.habits.components.NoteFilterModal
import com.example.test2.presentation.habits.components.NoteItem
import com.example.test2.presentation.habits.components.NoteSearchBar
import com.example.test2.presentation.habits.components.SearchTextField
import com.example.test2.presentation.components.LoadingView
import com.example.test2.presentation.components.IndexingNotice
import com.example.test2.presentation.habits.components.ImageViewer

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
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    // 设置习惯ID，用于过滤笔记
    LaunchedEffect(habitId) {
        viewModel.setHabitId(habitId)
    }
    
    // 获取状态
    val state by viewModel.state.collectAsState()
    
    // 顶部应用栏滚动行为
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
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
                    // 同步按钮
                    if (state.isOnlineMode) {
                        IconButton(
                            onClick = { viewModel.onEvent(NotesEvent.SyncData) },
                            enabled = !state.isSyncing
                        ) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(8.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "同步数据"
                                )
                            }
                        }
                    }
                    
                    IconButton(onClick = { /* 搜索功能 - 后续实现 */ }) {
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
                onClick = { viewModel.onEvent(NotesEvent.ShowNoteEditor) },
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
            Column {
                // 显示在线/离线状态指示器
                AnimatedVisibility(visible = true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (state.isOnlineMode) 
                                    MaterialTheme.colorScheme.primaryContainer
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.isOnlineMode) {
                                Text(
                                    text = "在线模式 - 数据将同步到云端",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Text(
                                    text = "离线模式 - 数据仅保存在本地",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // 显示同步消息
                if (state.syncMessage != null) {
                    AnimatedVisibility(
                        visible = state.syncMessage != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = state.syncMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // 加载状态
                if (state.isLoading) {
                    Box(modifier = Modifier.weight(1f)) {
                        LoadingView("正在加载笔记...")
                    }
                } 
                // 空状态
                else if (state.filteredNotes.isEmpty()) {
                    Box(modifier = Modifier.weight(1f)) {
                        EmptyNotesView()
                    }
                } 
                // 笔记列表
                else {
                    Box(modifier = Modifier.weight(1f)) {
                        NotesList(
                            notes = state.filteredNotes,
                            onNoteClick = { note ->
                                viewModel.onEvent(NotesEvent.ShowNoteDetail(note))
                            }
                        )
                    }
                }
            }
            
            // 显示索引创建通知
            if (state.isIndexing) {
                IndexingNotice(
                    isVisible = true,
                    onDismiss = { viewModel.onEvent(NotesEvent.ClearIndexingState) },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
            
            // 错误显示 - 如果有错误
            state.error?.let { errorMessage ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    
    // 笔记详情对话框
    if (state.showNoteDetail && state.selectedNote != null) {
        Dialog(onDismissRequest = { viewModel.onEvent(NotesEvent.CloseNoteDetail) }) {
            RichNoteDetail(
                note = state.selectedNote!!,
                onEdit = { note ->
                    viewModel.onEvent(NotesEvent.ShowEditNoteEditor(note))
                },
                onClose = { viewModel.onEvent(NotesEvent.CloseNoteDetail) }
            )
        }
    }
    
    // 笔记编辑器
    AnimatedVisibility(
        visible = state.showNoteEditor,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NoteEditor(
                habitId = habitId ?: "",
                existingNote = state.editingNote,
                onSave = { note ->
                    if (state.editingNote != null) {
                        viewModel.onEvent(NotesEvent.UpdateNote(note))
                    } else {
                        viewModel.onEvent(NotesEvent.SaveNote(note))
                    }
                },
                onCancel = { viewModel.onEvent(NotesEvent.CloseNoteEditor) },
                onAddImage = { uri -> viewModel.onEvent(NotesEvent.AddImageToNote(uri)) },
                onRemoveImage = { image -> viewModel.onEvent(NotesEvent.RemoveImageFromNote(image)) },
                onViewImage = { image -> viewModel.onEvent(NotesEvent.ViewImage(image)) },
                isImageProcessing = state.isImageProcessing
            )
        }
    }
    
    // 图片查看器
    if (state.showImageViewer) {
        val image = state.viewingImage
        if (image != null) {
            ImageViewer(
                image = image,
                onDismiss = { viewModel.onEvent(NotesEvent.CloseImageViewer) }
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