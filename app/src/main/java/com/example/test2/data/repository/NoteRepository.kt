package com.example.test2.data.repository

import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 笔记仓库接口
 * 定义了笔记数据管理相关的所有操作
 */
interface NoteRepository {
    // 查询操作
    
    /**
     * 获取所有笔记
     * 按顶置状态和创建时间降序排列（顶置的在前，同时按创建时间降序）
     */
    fun getAllNotes(): Flow<List<HabitNote>>
    
    /**
     * 根据ID获取笔记
     */
    suspend fun getNoteById(noteId: String): HabitNote?
    
    /**
     * 根据标题搜索笔记
     */
    fun searchNotesByTitle(searchQuery: String): Flow<List<HabitNote>>
    
    /**
     * 根据内容搜索笔记
     */
    fun searchNotesByContent(searchQuery: String): Flow<List<HabitNote>>
    
    /**
     * 获取特定心情的笔记
     */
    fun getNotesByMood(mood: NoteMood): Flow<List<HabitNote>>
    
    /**
     * 获取指定日期范围内的笔记
     */
    fun getNotesInDateRange(startDate: Date, endDate: Date): Flow<List<HabitNote>>
    
    /**
     * 获取今天创建的笔记
     */
    fun getTodayNotes(): Flow<List<HabitNote>>
    
    /**
     * 获取最近一周的笔记
     */
    fun getLastWeekNotes(weekAgo: Date): Flow<List<HabitNote>>
    
    /**
     * 获取所有顶置的笔记
     */
    fun getPinnedNotes(): Flow<List<HabitNote>>
    
    // 插入操作
    
    /**
     * 保存单个笔记
     * @return 保存的笔记ID
     */
    suspend fun saveNote(note: HabitNote): Long
    
    /**
     * 保存多个笔记
     * @return 保存的笔记ID列表
     */
    suspend fun saveNotes(notes: List<HabitNote>): List<Long>
    
    // 更新操作
    
    /**
     * 更新笔记
     * @return 是否更新成功
     */
    suspend fun updateNote(note: HabitNote): Boolean
    
    /**
     * 更新笔记的心情
     * @return 是否更新成功
     */
    suspend fun updateNoteMood(noteId: String, mood: NoteMood): Boolean
    
    /**
     * 更新笔记的标题
     * @return 是否更新成功
     */
    suspend fun updateNoteTitle(noteId: String, title: String): Boolean
    
    /**
     * 更新笔记的内容
     * @return 是否更新成功
     */
    suspend fun updateNoteContent(noteId: String, content: String): Boolean
    
    /**
     * 更新笔记的顶置状态
     * @return 是否更新成功
     */
    suspend fun updateNotePinStatus(noteId: String, isPinned: Boolean): Boolean
    
    // 删除操作
    
    /**
     * 删除笔记
     * @return 是否删除成功
     */
    suspend fun deleteNote(note: HabitNote): Boolean
    
    /**
     * 根据ID删除笔记
     * @return 是否删除成功
     */
    suspend fun deleteNoteById(noteId: String): Boolean
    
    /**
     * 删除所有笔记
     * @return 删除的行数
     */
    suspend fun deleteAllNotes(): Int
    
    /**
     * 删除特定日期之前的笔记
     * @return 删除的行数
     */
    suspend fun deleteNotesBeforeDate(date: Date): Int
} 