package com.example.test2.data.repository.impl

import android.content.Context
import com.example.test2.data.local.dao.NoteDao
import com.example.test2.data.local.entity.NoteEntity
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood
import com.example.test2.data.repository.NoteRepository
import com.example.test2.util.NoteImageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 笔记仓库实现类
 * 通过NoteDao实现笔记数据的存储与检索
 */
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    @ApplicationContext private val context: Context,
    private val imageManager: NoteImageManager
) : NoteRepository {

    // 图片存储目录
    private val imageDir: File by lazy {
        File(context.filesDir, "note_images").apply {
            if (!exists()) mkdirs()
        }
    }
    
    // 查询操作
    
    override fun getAllNotes(): Flow<List<HabitNote>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toHabitNote() }
        }
    }
    
    override suspend fun getNoteById(noteId: String): HabitNote? {
        return noteDao.getNoteById(noteId)?.toHabitNote()
    }
    
    override fun searchNotesByTitle(searchQuery: String): Flow<List<HabitNote>> {
        return noteDao.searchNotesByTitle(searchQuery).map { entities ->
            entities.map { it.toHabitNote() }
        }
    }
    
    override fun searchNotesByContent(searchQuery: String): Flow<List<HabitNote>> {
        return noteDao.searchNotesByContent(searchQuery).map { entities ->
            entities.map { it.toHabitNote() }
        }
    }
    
    override fun getNotesByMood(mood: NoteMood): Flow<List<HabitNote>> {
        return noteDao.getNotesByMood(mood.ordinal).map { entities ->
            entities.map { it.toHabitNote() }
        }
    }
    
    override fun getNotesInDateRange(startDate: Date, endDate: Date): Flow<List<HabitNote>> {
        return noteDao.getNotesInDateRange(startDate, endDate).map { entities ->
            entities.map { it.toHabitNote() }
        }
    }
    
    override fun getTodayNotes(): Flow<List<HabitNote>> {
        return noteDao.getTodayNotes().map { entities ->
            entities.map { it.toHabitNote() }
        }
    }
    
    override fun getLastWeekNotes(weekAgo: Date): Flow<List<HabitNote>> {
        return noteDao.getLastWeekNotes(weekAgo).map { entities ->
            entities.map { it.toHabitNote() }
        }
    }
    
    override fun getPinnedNotes(): Flow<List<HabitNote>> {
        return noteDao.getPinnedNotes().map { entities ->
            entities.map { it.toHabitNote() }
        }
    }
    
    // 插入操作
    
    override suspend fun saveNote(note: HabitNote): Long {
        val entity = NoteEntity.fromHabitNote(note)
        return noteDao.insertNote(entity)
    }
    
    override suspend fun saveNotes(notes: List<HabitNote>): List<Long> {
        val entities = notes.map { NoteEntity.fromHabitNote(it) }
        return noteDao.insertNotes(entities)
    }
    
    // 更新操作
    
    override suspend fun updateNote(note: HabitNote): Boolean {
        val now = Date()
        try {
            android.util.Log.d("NoteRepository", "更新笔记 ID: ${note.id}, 包含 ${note.images.size} 张图片")
            
            // 首先尝试获取笔记
            val existingNote = noteDao.getNoteById(note.id)
            android.util.Log.d("NoteRepository", "数据库中是否存在笔记: ${existingNote != null}")
            
            // 准备要更新的实体
            val entity = NoteEntity.fromHabitNote(note.copy(updatedAt = now))
            android.util.Log.d("NoteRepository", "转换后的实体 imagesJson 长度: ${entity.imagesJson.length}")
            
            // 尝试更新
            var result = noteDao.updateNote(entity) > 0
            android.util.Log.d("NoteRepository", "更新结果: $result")
            
            // 如果更新失败（可能是因为记录不存在），尝试插入
            if (!result) {
                android.util.Log.d("NoteRepository", "更新失败，尝试插入笔记")
                val insertResult = noteDao.insertNote(entity)
                result = insertResult > 0
                android.util.Log.d("NoteRepository", "插入结果: $result (ID: $insertResult)")
            }
            
            return result
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "更新笔记时出错: ${e.message}", e)
            return false
        }
    }
    
    override suspend fun updateNoteMood(noteId: String, mood: NoteMood): Boolean {
        return noteDao.updateNoteMood(noteId, mood.ordinal) > 0
    }
    
    override suspend fun updateNoteTitle(noteId: String, title: String): Boolean {
        return noteDao.updateNoteTitle(noteId, title) > 0
    }
    
    override suspend fun updateNoteContent(noteId: String, content: String): Boolean {
        return noteDao.updateNoteContent(noteId, content) > 0
    }
    
    override suspend fun updateNotePinStatus(noteId: String, isPinned: Boolean): Boolean {
        return noteDao.updateNotePinStatus(noteId, isPinned) > 0
    }
    
    // 删除操作
    
    override suspend fun deleteNote(note: HabitNote): Boolean {
        val entity = NoteEntity.fromHabitNote(note)
        return noteDao.deleteNote(entity) > 0
    }
    
    override suspend fun deleteNoteById(noteId: String): Boolean {
        return noteDao.deleteNoteById(noteId) > 0
    }
    
    override suspend fun deleteAllNotes(): Int {
        return noteDao.deleteAllNotes()
    }
    
    override suspend fun deleteNotesBeforeDate(date: Date): Int {
        return noteDao.deleteNotesBeforeDate(date)
    }

    /**
     * 清理未使用的图片
     * 委托给NoteImageManager处理
     */
    suspend fun cleanupUnusedImages(usedUris: List<String>): Int = withContext(Dispatchers.IO) {
        return@withContext imageManager.cleanupUnusedImages(usedUris)
    }
} 