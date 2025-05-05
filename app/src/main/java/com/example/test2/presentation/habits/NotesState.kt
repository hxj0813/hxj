package com.example.test2.presentation.habits

import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood

/**
 * 笔记管理状态类
 */
data class NotesState(
    val notes: List<HabitNote> = emptyList(),
    val filteredNotes: List<HabitNote> = emptyList(),
    val currentFilter: Filter = Filter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedNote: HabitNote? = null,
    val showNoteDetail: Boolean = false,
    val showNoteEditor: Boolean = false,
    val editingNote: HabitNote? = null,
    val habitId: String? = null
) {
    /**
     * 笔记过滤器
     */
    enum class Filter {
        ALL,           // 所有笔记
        PINNED,        // 顶置笔记
        TODAY,         // 今天的笔记
        LAST_WEEK,     // 最近一周的笔记
        MOOD_HAPPY,    // 开心的笔记
        MOOD_SAD,      // 难过的笔记
        MOOD_NEUTRAL,  // 平静的笔记
        MOOD_TIRED,    // 疲惫的笔记
        BY_TAG         // 按标签筛选
    }
    
    /**
     * 判断是否显示空状态
     */
    val showEmptyState: Boolean 
        get() = !isLoading && filteredNotes.isEmpty()
    
    companion object {
        /**
         * 创建初始状态
         */
        fun initial(habitId: String? = null) = NotesState(
            isLoading = true,
            habitId = habitId
        )
    }
} 