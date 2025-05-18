package com.example.test2.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.test2.data.firebase.repository.FirebaseHabitNoteRepository
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteImage
import com.example.test2.data.model.NoteMood
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 笔记用例集合
 * 连接UI层和数据层的桥梁
 */
class NoteUseCases @Inject constructor(
    private val noteRepository: FirebaseHabitNoteRepository
) {
    /**
     * 获取所有笔记
     */
    fun getAllNotes(): Flow<List<HabitNote>> {
        return noteRepository.getAllNotes()
    }
    
    /**
     * 获取特定习惯的笔记
     */
    fun getNotesByHabit(habitId: String): Flow<List<HabitNote>> {
        return noteRepository.getNotesByHabit(habitId)
    }
    
    /**
     * 按心情获取笔记
     */
    fun getNotesByMood(mood: NoteMood): Flow<List<HabitNote>> {
        return noteRepository.getNotesByMood(mood)
    }
    
    /**
     * 获取顶置笔记
     */
    fun getPinnedNotes(): Flow<List<HabitNote>> {
        return noteRepository.getPinnedNotes()
    }
    
    /**
     * 获取具体笔记内容
     */
    fun getNoteById(id: String): Flow<HabitNote?> {
        return noteRepository.getNoteById(id)
    }
    
    /**
     * 保存笔记
     */
    suspend fun saveNote(note: HabitNote): Result<String> {
        return noteRepository.saveNote(note)
    }
    
    /**
     * 删除笔记
     */
    suspend fun deleteNote(noteId: String): Result<Unit> {
        return noteRepository.deleteNote(noteId)
    }
    
    /**
     * 更新笔记的顶置状态
     */
    suspend fun updateNotePinStatus(noteId: String, isPinned: Boolean): Result<Unit> {
        return noteRepository.updateNotePinStatus(noteId, isPinned)
    }
    
    /**
     * 上传笔记图片
     */
    suspend fun uploadNoteImage(uri: Uri, context: Context): Result<NoteImage> {
        return noteRepository.uploadNoteImage(uri, context)
    }
    
    /**
     * 删除笔记图片
     */
    suspend fun deleteNoteImage(imageUrl: String): Result<Unit> {
        return noteRepository.deleteNoteImage(imageUrl)
    }
} 