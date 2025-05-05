package com.example.test2.presentation.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteTag
import com.example.test2.domain.usecase.NoteUseCases
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
    private val noteUseCases: NoteUseCases
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
                                NoteMood.SAD, NoteMood.FRUSTRATED -> NotesState.Filter.MOOD_SAD
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
     * 保存新笔记
     */
    private fun saveNote(note: HabitNote) {
        viewModelScope.launch {
            val result = noteUseCases.saveNote(note)
            if (result > 0) {
                loadNotes()
                closeNoteEditor()
            } else {
                _state.update { it.copy(error = "保存笔记失败") }
            }
        }
    }

    /**
     * 更新现有笔记
     */
    private fun updateNote(note: HabitNote) {
        viewModelScope.launch {
            val success = noteUseCases.updateNote(note)
            if (success) {
                loadNotes()
                closeNoteEditor()
            } else {
                _state.update { it.copy(error = "更新笔记失败") }
            }
        }
    }

    /**
     * 删除笔记
     */
    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            val success = noteUseCases.deleteNote(noteId)
            if (success) {
                loadNotes()
                closeNoteDetail()
            } else {
                _state.update { it.copy(error = "删除笔记失败") }
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
     * 显示笔记编辑器（新建）
     */
    private fun showNoteEditor() {
        _state.update { 
            it.copy(
                showNoteEditor = true,
                editingNote = null
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
                    it.mood == NoteMood.SAD || it.mood == NoteMood.FRUSTRATED 
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
} 