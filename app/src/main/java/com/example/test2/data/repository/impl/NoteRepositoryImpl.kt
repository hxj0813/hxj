package com.example.test2.data.repository.impl

import com.example.test2.data.local.dao.NoteDao
import com.example.test2.data.local.entity.NoteEntity
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood
import com.example.test2.data.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * 笔记仓库实现类
 * 通过NoteDao实现笔记数据的存储与检索
 */
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

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
        val entity = NoteEntity.fromHabitNote(note.copy(updatedAt = now))
        return noteDao.updateNote(entity) > 0
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
} 