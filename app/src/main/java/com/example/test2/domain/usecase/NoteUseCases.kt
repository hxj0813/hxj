package com.example.test2.domain.usecase

import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood
import com.example.test2.data.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * 笔记相关的用例集合
 * 封装所有与笔记相关的业务逻辑
 */
class NoteUseCases @Inject constructor(
    private val repository: NoteRepository
) {
    /**
     * 获取所有笔记
     */
    fun getAllNotes(): Flow<List<HabitNote>> {
        return repository.getAllNotes()
    }
    
    /**
     * 获取顶置的笔记
     */
    fun getPinnedNotes(): Flow<List<HabitNote>> {
        return repository.getPinnedNotes()
    }
    
    /**
     * 获取今天的笔记
     */
    fun getTodayNotes(): Flow<List<HabitNote>> {
        return repository.getTodayNotes()
    }
    
    /**
     * 获取过去一周的笔记
     */
    fun getLastWeekNotes(): Flow<List<HabitNote>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val weekAgo = calendar.time
        
        return repository.getLastWeekNotes(weekAgo)
    }
    
    /**
     * 获取特定心情的笔记
     */
    fun getNotesByMood(mood: NoteMood): Flow<List<HabitNote>> {
        return repository.getNotesByMood(mood)
    }
    
    /**
     * 根据标题搜索笔记
     */
    fun searchNotesByTitle(query: String): Flow<List<HabitNote>> {
        return repository.searchNotesByTitle(query)
    }
    
    /**
     * 根据内容搜索笔记
     */
    fun searchNotesByContent(query: String): Flow<List<HabitNote>> {
        return repository.searchNotesByContent(query)
    }
    
    /**
     * 根据ID获取笔记
     */
    suspend fun getNoteById(noteId: String): HabitNote? {
        return repository.getNoteById(noteId)
    }
    
    /**
     * 保存笔记
     * @return 保存的笔记ID，如果保存失败返回-1
     */
    suspend fun saveNote(note: HabitNote): Long {
        return repository.saveNote(note)
    }
    
    /**
     * 更新笔记
     * @return 是否更新成功
     */
    suspend fun updateNote(note: HabitNote): Boolean {
        return repository.updateNote(note)
    }
    
    /**
     * 更新笔记心情
     * @return 是否更新成功
     */
    suspend fun updateNoteMood(noteId: String, mood: NoteMood): Boolean {
        return repository.updateNoteMood(noteId, mood)
    }
    
    /**
     * 切换笔记顶置状态
     * @return 是否更新成功
     */
    suspend fun toggleNotePinStatus(noteId: String, isPinned: Boolean): Boolean {
        return repository.updateNotePinStatus(noteId, isPinned)
    }
    
    /**
     * 删除笔记
     * @return 是否删除成功
     */
    suspend fun deleteNote(noteId: String): Boolean {
        return repository.deleteNoteById(noteId)
    }
    
    /**
     * 清理旧笔记（指定日期之前的）
     * @return 删除的行数
     */
    suspend fun cleanOldNotes(date: Date): Int {
        return repository.deleteNotesBeforeDate(date)
    }
} 