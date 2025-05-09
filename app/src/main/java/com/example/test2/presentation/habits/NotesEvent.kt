package com.example.test2.presentation.habits

import android.net.Uri
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteTag
import com.example.test2.data.model.NoteImage

/**
 * 笔记管理事件
 */
sealed class NotesEvent {
    /**
     * 加载笔记列表
     */
    object LoadNotes : NotesEvent()
    
    /**
     * 过滤笔记
     */
    data class FilterNotes(val filter: NotesState.Filter) : NotesEvent()
    
    /**
     * 按标签过滤笔记
     */
    data class FilterNotesByTag(val tag: NoteTag) : NotesEvent()
    
    /**
     * 按心情过滤笔记
     */
    data class FilterNotesByMood(val mood: NoteMood) : NotesEvent()
    
    /**
     * 按关键字搜索笔记
     */
    data class SearchNotes(val query: String) : NotesEvent()
    
    /**
     * 添加/保存笔记
     */
    data class SaveNote(val note: HabitNote) : NotesEvent()
    
    /**
     * 更新笔记
     */
    data class UpdateNote(val note: HabitNote) : NotesEvent()
    
    /**
     * 删除笔记
     */
    data class DeleteNote(val noteId: String) : NotesEvent()
    
    /**
     * 顶置/取消顶置笔记
     */
    data class ToggleNotePinStatus(val noteId: String, val isPinned: Boolean) : NotesEvent()
    
    /**
     * 显示笔记详情
     */
    data class ShowNoteDetail(val note: HabitNote) : NotesEvent()
    
    /**
     * 关闭笔记详情
     */
    object CloseNoteDetail : NotesEvent()
    
    /**
     * 显示笔记编辑器（新建笔记）
     */
    object ShowNoteEditor : NotesEvent()
    
    /**
     * 显示笔记编辑器（编辑现有笔记）
     */
    data class ShowEditNoteEditor(val note: HabitNote) : NotesEvent()
    
    /**
     * 关闭笔记编辑器
     */
    object CloseNoteEditor : NotesEvent()
    
    /**
     * 添加图片到笔记
     */
    data class AddImageToNote(val uri: Uri) : NotesEvent()
    
    /**
     * 从笔记中移除图片
     */
    data class RemoveImageFromNote(val image: NoteImage) : NotesEvent()
    
    /**
     * 查看图片
     */
    data class ViewImage(val image: NoteImage) : NotesEvent()
    
    /**
     * 关闭图片查看器
     */
    object CloseImageViewer : NotesEvent()
} 