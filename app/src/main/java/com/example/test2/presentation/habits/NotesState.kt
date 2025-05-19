package com.example.test2.presentation.habits

import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteImage
import java.io.File

/**
 * 笔记管理状态类
 */
data class NotesState(
    val notes: List<HabitNote> = emptyList(),
    val filteredNotes: List<HabitNote> = emptyList(),
    val currentFilter: Filter = Filter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isImageProcessing: Boolean = false,
    val isIndexing: Boolean = false, // 是否正在创建索引
    val error: String? = null,
    val selectedNote: HabitNote? = null,
    val showNoteDetail: Boolean = false,
    val showNoteEditor: Boolean = false,
    val editingNote: HabitNote? = null,
    val habitId: String? = null,
    val viewingImage: NoteImage? = null, // 当前查看的图片
    val showImageViewer: Boolean = false, // 是否显示图片查看器
    val currentEditingNote: HabitNote? = null, // 当前正在编辑的笔记
    val tempImages: List<NoteImage> = emptyList(), // 临时图片列表
    val currentViewingImage: NoteImage? = null, // 当前正在查看的图片
    val isImageViewerVisible: Boolean = false, // 图片查看器是否可见
    val isOnlineMode: Boolean = false,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
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
        BY_TAG,        // 按标签筛选
        WEEK,          // 本周的笔记
        TAG,           // 按标签筛选（搭配tagFilter使用）
        MOOD_HAPPY_M,  // 开心的笔记（搭配moodFilter使用）
        MOOD_SAD_M,    // 难过的笔记（搭配moodFilter使用）
        MOOD_NEUTRAL_M, // 平静的笔记（搭配moodFilter使用）
        MOOD_TIRED_M,   // 疲惫的笔记（搭配moodFilter使用）
        SEARCH         // 搜索结果（搭配searchQuery使用）
    }
    
    /**
     * 判断是否显示空状态
     */
    val showEmptyState: Boolean 
        get() = !isLoading && filteredNotes.isEmpty() && error == null
    
    /**
     * 判断是否正在处理过程中
     */
    val isProcessing: Boolean
        get() = isLoading || isSaving || isImageProcessing
    
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