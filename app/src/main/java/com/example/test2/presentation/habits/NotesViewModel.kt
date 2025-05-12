package com.example.test2.presentation.habits

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteTag
import com.example.test2.data.model.NoteImage
import com.example.test2.domain.usecase.NoteUseCases
import com.example.test2.util.NoteImageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * 笔记管理ViewModel
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    private val imageManager: NoteImageManager
) : ViewModel() {

    // 状态
    private val _state = MutableStateFlow(NotesState.initial())
    val state: StateFlow<NotesState> = _state.asStateFlow()
    
    // 顶置笔记缓存
    private var pinnedNotes: List<HabitNote> = emptyList()

    init {
        loadNotes()
        // 加载顶置笔记
        loadPinnedNotes()
    }

    /**
     * 处理UI事件
     */
    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.LoadNotes -> loadNotes()
            is NotesEvent.FilterNotes -> filterNotes(event.filter)
            is NotesEvent.FilterNotesByTag -> filterNotesByTag(event.tag)
            is NotesEvent.FilterNotesByMood -> filterNotesByMood(event.mood)
            is NotesEvent.SearchNotes -> searchNotes(event.query)
            is NotesEvent.SaveNote -> saveNote(event.note)
            is NotesEvent.UpdateNote -> updateNote(event.note)
            is NotesEvent.DeleteNote -> deleteNote(event.noteId)
            is NotesEvent.ToggleNotePinStatus -> toggleNotePinStatus(event.noteId, event.isPinned)
            is NotesEvent.ShowNoteDetail -> showNoteDetail(event.note)
            is NotesEvent.CloseNoteDetail -> closeNoteDetail()
            is NotesEvent.ShowNoteEditor -> showNoteEditor()
            is NotesEvent.ShowEditNoteEditor -> showEditNoteEditor(event.note)
            is NotesEvent.CloseNoteEditor -> closeNoteEditor()
            is NotesEvent.AddImageToNote -> addImageToNote(event.uri)
            is NotesEvent.RemoveImageFromNote -> removeImageFromNote(event.image)
            is NotesEvent.ViewImage -> viewImage(event.image)
            is NotesEvent.CloseImageViewer -> closeImageViewer()
        }
    }

    /**
     * 设置习惯ID筛选器（用于显示特定习惯的笔记）
     */
    fun setHabitId(habitId: String?) {
        _state.update { it.copy(habitId = habitId) }
        loadNotes()
    }
    
    /**
     * 加载顶置笔记
     */
    private fun loadPinnedNotes() {
        viewModelScope.launch {
            noteUseCases.getPinnedNotes()
                .catch { e ->
                    // 处理错误
                    _state.update { it.copy(error = e.message) }
                }
                .onEach { notes ->
                    pinnedNotes = notes
                    // 如果当前过滤器是顶置笔记，需要更新UI
                    if (_state.value.currentFilter == NotesState.Filter.PINNED) {
                        applyCurrentFilter()
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    /**
     * 加载笔记列表
     */
    private fun loadNotes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 从数据库加载所有笔记
            noteUseCases.getAllNotes()
                .catch { e ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message
                        ) 
                    }
                }
                .onEach { notes ->
                    // 如果有习惯ID筛选，过滤出相关笔记
                    val filteredByHabit = _state.value.habitId?.let { habitId ->
                        if (habitId.isNotEmpty()) {
                            notes.filter { it.habitId == habitId }
                        } else {
                            notes
                        }
                    } ?: notes
                    
                    // 应用当前的过滤器
                    val filtered = applyFilter(
                        filteredByHabit, 
                        _state.value.currentFilter,
                        _state.value.searchQuery
                    )
                    
                    _state.update { currentState ->
                        currentState.copy(
                            notes = filteredByHabit,
                            filteredNotes = filtered,
                            isLoading = false
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    /**
     * 过滤笔记
     */
    private fun filterNotes(filter: NotesState.Filter) {
        _state.update { currentState ->
            currentState.copy(
                currentFilter = filter,
                filteredNotes = applyFilter(
                    currentState.notes, 
                    filter,
                    currentState.searchQuery
                )
            )
        }
    }
    
    /**
     * 应用当前过滤器
     */
    private fun applyCurrentFilter() {
        _state.update { currentState ->
            currentState.copy(
                filteredNotes = applyFilter(
                    currentState.notes,
                    currentState.currentFilter,
                    currentState.searchQuery
                )
            )
        }
    }

    /**
     * 按标签过滤笔记
     */
    private fun filterNotesByTag(tag: NoteTag) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val filtered = _state.value.notes.filter { note ->
                note.tags.any { it.id == tag.id }
            }
            
            _state.update { 
                it.copy(
                    filteredNotes = filtered,
                    currentFilter = NotesState.Filter.BY_TAG,
                    isLoading = false
                ) 
            }
        }
    }

    /**
     * 按心情过滤笔记
     */
    private fun filterNotesByMood(mood: NoteMood) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            noteUseCases.getNotesByMood(mood)
                .catch { e ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message
                        ) 
                    }
                }
                .onEach { notes ->
                    // 如果有习惯ID筛选，继续过滤
                    val filteredByHabit = _state.value.habitId?.let { habitId ->
                        if (habitId.isNotEmpty()) {
                            notes.filter { it.habitId == habitId }
                        } else {
                            notes
                        }
                    } ?: notes
                    
                    _state.update { 
                        it.copy(
                            filteredNotes = filteredByHabit,
                            isLoading = false,
                            currentFilter = when(mood) {
                                NoteMood.HAPPY, NoteMood.VERY_HAPPY -> NotesState.Filter.MOOD_HAPPY
                                NoteMood.SAD, NoteMood.VERY_SAD, NoteMood.FRUSTRATED -> NotesState.Filter.MOOD_SAD
                                NoteMood.NEUTRAL -> NotesState.Filter.MOOD_NEUTRAL
                                NoteMood.TIRED -> NotesState.Filter.MOOD_TIRED
                            }
                        ) 
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    /**
     * 关键字搜索笔记
     */
    private fun searchNotes(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _state.update { 
                    it.copy(
                        searchQuery = "",
                        filteredNotes = applyFilter(it.notes, it.currentFilter, "")
                    )
                }
                return@launch
            }
            
            _state.update { it.copy(isLoading = true, searchQuery = query) }
            
            // 先搜索标题
            noteUseCases.searchNotesByTitle(query)
                .catch { e ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message
                        ) 
                    }
                }
                .onEach { titleMatches ->
                    // 再搜索内容
                    noteUseCases.searchNotesByContent(query)
                        .catch { e ->
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    error = e.message
                                ) 
                            }
                        }
                        .onEach { contentMatches ->
                            // 合并结果（去重）
                            val combinedResults = (titleMatches + contentMatches)
                                .distinctBy { it.id }
                            
                            // 如果有习惯ID筛选，继续过滤
                            val filteredByHabit = _state.value.habitId?.let { habitId ->
                                if (habitId.isNotEmpty()) {
                                    combinedResults.filter { it.habitId == habitId }
                                } else {
                                    combinedResults
                                }
                            } ?: combinedResults
                            
                            _state.update { 
                                it.copy(
                                    filteredNotes = filteredByHabit,
                                    isLoading = false
                                )
                            }
                        }
                        .launchIn(viewModelScope)
                }
                .launchIn(viewModelScope)
        }
    }

    /**
     * 添加图片到笔记
     */
    private fun addImageToNote(uri: Uri) {
        viewModelScope.launch {
            // 设置图片处理状态
            _state.update { it.copy(isImageProcessing = true) }
            
            try {
                Log.d("NotesViewModel", "开始添加图片: $uri")
                
                // 保存图片到本地文件系统
                val savedUri = imageManager.saveImageFromUri(uri)
                Log.d("NotesViewModel", "图片已保存到: $savedUri")
                
                // 创建图片对象
                val newImage = NoteImage(
                    id = UUID.randomUUID().toString(),
                    uri = savedUri,
                    description = "",
                    createdAt = Date()
                )
                
                // 更新当前编辑中的笔记
                val currentNote = _state.value.editingNote
                if (currentNote != null) {
                    // 确保不超过10张图片
                    if (currentNote.images.size < 10) {
                        val updatedImages = currentNote.images + newImage
                        val updatedNote = currentNote.copy(images = updatedImages)
                        
                        Log.d("NotesViewModel", "添加图片后，笔记 ${updatedNote.id} 现在有 ${updatedImages.size} 张图片")
                        
                        _state.update { 
                            it.copy(
                                editingNote = updatedNote,
                                isImageProcessing = false
                            ) 
                        }
                    } else {
                        _state.update { 
                            it.copy(
                                error = "最多只能添加10张图片",
                                isImageProcessing = false
                            ) 
                        }
                    }
                } else {
                    _state.update { it.copy(isImageProcessing = false) }
                }
            } catch (e: Exception) {
                Log.e("NotesViewModel", "添加图片失败: ${e.message}", e)
                _state.update { 
                    it.copy(
                        error = "添加图片失败: ${e.message}",
                        isImageProcessing = false
                    ) 
                }
            }
        }
    }
    
    /**
     * 从笔记中移除图片
     */
    private fun removeImageFromNote(image: NoteImage) {
        viewModelScope.launch {
            try {
                Log.d("NotesViewModel", "准备移除图片 ID: ${image.id}")
                
                // 删除文件
                imageManager.deleteImage(image.uri)
                Log.d("NotesViewModel", "图片文件已删除: ${image.uri}")
                
                // 更新状态
                val currentNote = _state.value.editingNote
                if (currentNote != null) {
                    val updatedImages = currentNote.images.filter { it.id != image.id }
                    val updatedNote = currentNote.copy(images = updatedImages)
                    
                    Log.d("NotesViewModel", "移除图片后，笔记 ${updatedNote.id} 还有 ${updatedImages.size} 张图片")
                    
                    _state.update { 
                        it.copy(editingNote = updatedNote) 
                    }
                }
            } catch (e: Exception) {
                Log.e("NotesViewModel", "删除图片失败: ${e.message}", e)
                _state.update { 
                    it.copy(error = "删除图片失败: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * 查看图片
     */
    private fun viewImage(image: NoteImage) {
        _state.update { 
            it.copy(
                viewingImage = image,
                showImageViewer = true
            ) 
        }
    }
    
    /**
     * 关闭图片查看器
     */
    private fun closeImageViewer() {
        _state.update { 
            it.copy(
                viewingImage = null,
                showImageViewer = false
            ) 
        }
    }
    
    /**
     * 清理未使用的图片
     */
    private fun cleanupUnusedImages() {
        viewModelScope.launch {
            try {
                // 收集所有笔记中使用的图片URI
                val usedImageUris = _state.value.notes
                    .flatMap { it.images }
                    .map { it.uri }
                
                Log.d("NotesViewModel", "准备清理未使用图片，当前有 ${usedImageUris.size} 个使用中的图片")
                
                // 添加当前编辑中的笔记图片
                val editingNote = _state.value.editingNote
                if (editingNote != null && editingNote.images.isNotEmpty()) {
                    val editingImages = editingNote.images.map { it.uri }
                    Log.d("NotesViewModel", "添加编辑中笔记的 ${editingImages.size} 张图片到保护列表")
                    val combinedUris = usedImageUris + editingImages
                    // 清理未使用的图片，确保不清理正在使用的图片
                    val cleanedCount = imageManager.cleanupUnusedImages(combinedUris.distinct())
                    Log.d("NotesViewModel", "清理了 $cleanedCount 张未使用的图片")
                } else {
                    // 清理未使用的图片
                    val cleanedCount = imageManager.cleanupUnusedImages(usedImageUris)
                    Log.d("NotesViewModel", "清理了 $cleanedCount 张未使用的图片")
                }
            } catch (e: Exception) {
                Log.e("NotesViewModel", "清理图片过程中出错: ${e.message}", e)
                // 忽略清理错误
            }
        }
    }
    
    /**
     * 保存笔记
     */
    private fun saveNote(note: HabitNote) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            try {
                Log.d("NotesViewModel", "Saving new note with title: ${note.title}")
                Log.d("NotesViewModel", "Note has ${note.images.size} images")
                
                val result = noteUseCases.saveNote(note)
                Log.d("NotesViewModel", "Save result: $result")
                
                if (result > 0) {
                    _state.update { 
                        it.copy(
                            showNoteEditor = false,
                            editingNote = null,
                            isSaving = false
                        ) 
                    }
                    
                    // 重新加载笔记列表
                    loadNotes()
                    
                    // 清理未使用的图片
                    cleanupUnusedImages()
                } else {
                    _state.update { 
                        it.copy(
                            error = "保存笔记失败",
                            isSaving = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error saving note: ${e.message}", e)
                _state.update { 
                    it.copy(
                        error = "保存笔记失败: ${e.message}",
                        isSaving = false
                    ) 
                }
            }
        }
    }
    
    /**
     * 更新笔记
     */
    private fun updateNote(note: HabitNote) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            try {
                // 添加详细日志
                Log.d("NotesViewModel", "开始更新笔记 ID: ${note.id}")
                Log.d("NotesViewModel", "标题: ${note.title}")
                Log.d("NotesViewModel", "内容: ${note.content.take(50)}...")
                Log.d("NotesViewModel", "图片数量: ${note.images.size}")
                
                // 获取当前编辑中的笔记
                val currentEditingNote = _state.value.editingNote
                
                // 重要：确保使用ViewModel中的最新图片列表
                val updatedNote = if (currentEditingNote != null && note.id == currentEditingNote.id) {
                    // 如果images字段在传入的note中丢失，使用当前编辑笔记的images
                    if (note.images.isEmpty() && currentEditingNote.images.isNotEmpty()) {
                        Log.d("NotesViewModel", "使用ViewModel中的图片列表: ${currentEditingNote.images.size}张")
                        note.copy(images = currentEditingNote.images)
                    } else {
                        note
                    }
                } else {
                    note
                }
                
                Log.d("NotesViewModel", "最终更新的笔记图片数量: ${updatedNote.images.size}")
                
                // 调用Repository更新笔记
                val result = noteUseCases.updateNote(updatedNote)
                Log.d("NotesViewModel", "更新结果: $result")
                
                if (result) {
                    _state.update { 
                        it.copy(
                            showNoteEditor = false,
                            editingNote = null,
                            isSaving = false
                        ) 
                    }
                    
                    // 如果正在查看此笔记的详情，更新显示
                    if (_state.value.showNoteDetail && _state.value.selectedNote?.id == note.id) {
                        _state.update { it.copy(selectedNote = updatedNote) }
                    }
                    
                    // 重新加载笔记列表
                    loadNotes()
                    
                    // 清理未使用的图片
                    cleanupUnusedImages()
                } else {
                    _state.update { 
                        it.copy(
                            error = "笔记更新失败",
                            isSaving = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                Log.e("NotesViewModel", "更新笔记失败: ${e.message}", e)
                _state.update { 
                    it.copy(
                        error = "更新笔记失败: ${e.message}",
                        isSaving = false
                    ) 
                }
            }
        }
    }
    
    /**
     * 删除笔记
     */
    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                // 获取笔记信息
                val noteToDelete = _state.value.notes.find { it.id == noteId }
                
                // 删除笔记
                noteUseCases.deleteNote(noteId)
                
                // 关闭详情视图
                if (_state.value.showNoteDetail && _state.value.selectedNote?.id == noteId) {
                    _state.update { it.copy(showNoteDetail = false, selectedNote = null) }
                }
                
                // 重新加载笔记列表
                loadNotes()
                
                // 清理未使用的图片
                cleanupUnusedImages()
            } catch (e: Exception) {
                _state.update { it.copy(error = "删除笔记失败: ${e.message}") }
            }
        }
    }

    /**
     * 显示笔记详情
     */
    private fun showNoteDetail(note: HabitNote) {
        _state.update { 
            it.copy(
                selectedNote = note,
                showNoteDetail = true
            ) 
        }
    }

    /**
     * 关闭笔记详情
     */
    private fun closeNoteDetail() {
        _state.update { 
            it.copy(
                showNoteDetail = false,
                selectedNote = null
            ) 
        }
    }

    /**
     * 显示笔记编辑器
     */
    private fun showNoteEditor() {
        val habitId = _state.value.habitId ?: ""
        
        // 创建一个新的空白笔记
        val newNote = HabitNote(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            title = "",
            content = "",
            mood = NoteMood.NEUTRAL,
            tags = emptyList(),
            images = emptyList(),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        _state.update { 
            it.copy(
                showNoteEditor = true,
                editingNote = newNote
            ) 
        }
    }

    /**
     * 显示笔记编辑器（编辑）
     */
    private fun showEditNoteEditor(note: HabitNote) {
        _state.update { 
            it.copy(
                showNoteEditor = true,
                editingNote = note
            ) 
        }
    }

    /**
     * 关闭笔记编辑器
     */
    private fun closeNoteEditor() {
        _state.update { 
            it.copy(
                showNoteEditor = false,
                editingNote = null
            ) 
        }
    }

    /**
     * 应用过滤器
     */
    private fun applyFilter(
        notes: List<HabitNote>, 
        filter: NotesState.Filter,
        searchQuery: String
    ): List<HabitNote> {
        // 如果有搜索关键字，优先使用关键字过滤
        if (searchQuery.isNotBlank()) {
            return notes.filter { note ->
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // 应用类别过滤
        return when (filter) {
            NotesState.Filter.ALL -> notes
            NotesState.Filter.PINNED -> {
                // 使用缓存的顶置笔记
                pinnedNotes
            }
            NotesState.Filter.TODAY -> {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                notes.filter { note ->
                    val noteDate = Calendar.getInstance().apply {
                        time = note.createdAt
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                    noteDate == today
                }
            }
            NotesState.Filter.LAST_WEEK -> {
                val weekAgo = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }.time
                notes.filter { it.createdAt.after(weekAgo) }
            }
            NotesState.Filter.MOOD_HAPPY -> {
                notes.filter { 
                    it.mood == NoteMood.HAPPY || it.mood == NoteMood.VERY_HAPPY 
                }
            }
            NotesState.Filter.MOOD_SAD -> {
                notes.filter { 
                    it.mood == NoteMood.SAD || it.mood == NoteMood.VERY_SAD || it.mood == NoteMood.FRUSTRATED 
                }
            }
            NotesState.Filter.MOOD_NEUTRAL -> {
                notes.filter { it.mood == NoteMood.NEUTRAL }
            }
            NotesState.Filter.MOOD_TIRED -> {
                notes.filter { it.mood == NoteMood.TIRED }
            }
            NotesState.Filter.BY_TAG -> {
                // 按标签筛选在单独的方法中处理
                notes
            }
        }
    }

    /**
     * 顶置/取消顶置笔记
     */
    private fun toggleNotePinStatus(noteId: String, isPinned: Boolean) {
        viewModelScope.launch {
            val success = noteUseCases.toggleNotePinStatus(noteId, isPinned)
            if (success) {
                loadNotes()
                // 同时更新顶置笔记缓存
                loadPinnedNotes()
            } else {
                _state.update { it.copy(error = "修改笔记顶置状态失败") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 清理未使用的图片
        cleanupUnusedImages()
    }
} 