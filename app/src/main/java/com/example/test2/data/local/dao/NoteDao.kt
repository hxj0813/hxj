package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 笔记数据访问对象接口
 * 定义了对笔记表(notes)的所有数据库操作
 */
@Dao
interface NoteDao {
    // 查询操作
    
    /**
     * 获取所有笔记
     * 按顶置状态和创建时间降序排列（顶置的在前，同时按创建时间降序）
     */
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    /**
     * 根据ID获取笔记
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?
    
    /**
     * 根据标题搜索笔记
     */
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :searchQuery || '%' ORDER BY isPinned DESC, createdAt DESC")
    fun searchNotesByTitle(searchQuery: String): Flow<List<NoteEntity>>
    
    /**
     * 根据内容搜索笔记
     */
    @Query("SELECT * FROM notes WHERE content LIKE '%' || :searchQuery || '%' ORDER BY isPinned DESC, createdAt DESC")
    fun searchNotesByContent(searchQuery: String): Flow<List<NoteEntity>>
    
    /**
     * 获取特定心情的笔记
     */
    @Query("SELECT * FROM notes WHERE mood = :mood ORDER BY isPinned DESC, createdAt DESC")
    fun getNotesByMood(mood: Int): Flow<List<NoteEntity>>
    
    /**
     * 获取指定日期范围内的笔记
     */
    @Query("SELECT * FROM notes WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY isPinned DESC, createdAt DESC")
    fun getNotesInDateRange(startDate: Date, endDate: Date): Flow<List<NoteEntity>>
    
    /**
     * 获取今天创建的笔记
     */
    @Query("SELECT * FROM notes WHERE date(createdAt / 1000, 'unixepoch', 'localtime') = date(:today / 1000, 'unixepoch', 'localtime') ORDER BY isPinned DESC, createdAt DESC")
    fun getTodayNotes(today: Date = Date()): Flow<List<NoteEntity>>
    
    /**
     * 获取最近一周的笔记
     */
    @Query("SELECT * FROM notes WHERE createdAt >= :weekAgo ORDER BY isPinned DESC, createdAt DESC")
    fun getLastWeekNotes(weekAgo: Date): Flow<List<NoteEntity>>
    
    /**
     * 获取所有顶置的笔记
     */
    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY createdAt DESC")
    fun getPinnedNotes(): Flow<List<NoteEntity>>
    
    // 插入操作
    
    /**
     * 插入单个笔记
     * @return 插入的笔记ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long
    
    /**
     * 插入多个笔记
     * @return 插入的笔记ID列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>): List<Long>
    
    // 更新操作
    
    /**
     * 更新笔记
     * @return 更新的行数
     */
    @Update
    suspend fun updateNote(note: NoteEntity): Int
    
    /**
     * 更新笔记的心情
     * @return 更新的行数
     */
    @Query("UPDATE notes SET mood = :mood, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateNoteMood(noteId: String, mood: Int, updatedAt: Date = Date()): Int
    
    /**
     * 更新笔记的标题
     * @return 更新的行数
     */
    @Query("UPDATE notes SET title = :title, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateNoteTitle(noteId: String, title: String, updatedAt: Date = Date()): Int
    
    /**
     * 更新笔记的内容
     * @return 更新的行数
     */
    @Query("UPDATE notes SET content = :content, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateNoteContent(noteId: String, content: String, updatedAt: Date = Date()): Int
    
    /**
     * 更新笔记的顶置状态
     * @return 更新的行数
     */
    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateNotePinStatus(noteId: String, isPinned: Boolean, updatedAt: Date = Date()): Int
    
    // 删除操作
    
    /**
     * 删除笔记
     * @return 删除的行数
     */
    @Delete
    suspend fun deleteNote(note: NoteEntity): Int
    
    /**
     * 根据ID删除笔记
     * @return 删除的行数
     */
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String): Int
    
    /**
     * 删除所有笔记
     * @return 删除的行数
     */
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes(): Int
    
    /**
     * 删除特定日期之前的笔记
     * @return 删除的行数
     */
    @Query("DELETE FROM notes WHERE createdAt < :date")
    suspend fun deleteNotesBeforeDate(date: Date): Int
} 