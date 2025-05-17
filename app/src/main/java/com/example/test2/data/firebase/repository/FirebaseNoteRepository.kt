package com.example.test2.data.firebase.repository

import android.net.Uri
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.Note
import com.example.test2.data.model.NoteImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase笔记存储库接口
 * 定义与笔记相关的云存储操作
 */
interface FirebaseNoteRepository {
    /**
     * 获取用户所有笔记
     * @return 笔记列表的Flow
     */
    fun getNotes(): Flow<List<Note>>
    
    /**
     * 获取特定笔记
     * @param noteId 笔记ID
     * @return 笔记对象的Flow
     */
    fun getNote(noteId: String): Flow<Note?>
    
    /**
     * 添加或更新笔记
     * @param note 笔记对象
     * @return 成功则返回笔记ID，失败则返回异常
     */
    suspend fun saveNote(note: Note): Result<String>
    
    /**
     * 删除笔记
     * @param noteId 笔记ID
     * @return 成功则返回Unit，失败则返回异常
     */
    suspend fun deleteNote(noteId: String): Result<Unit>
    
    /**
     * 获取用户习惯笔记
     * @param habitId 习惯ID
     * @return 习惯笔记列表的Flow
     */
    fun getHabitNotes(habitId: String): Flow<List<HabitNote>>
    
    /**
     * 添加或更新习惯笔记
     * @param habitNote 习惯笔记对象
     * @return 成功则返回笔记ID，失败则返回异常
     */
    suspend fun saveHabitNote(habitNote: HabitNote): Result<String>
    
    /**
     * 删除习惯笔记
     * @param habitNoteId 习惯笔记ID
     * @return 成功则返回Unit，失败则返回异常
     */
    suspend fun deleteHabitNote(habitNoteId: String): Result<Unit>
    
    /**
     * 上传笔记图片
     * @param imageFile 图片文件
     * @param noteId 关联的笔记ID
     * @return 成功则返回图片URL，失败则返回异常
     */
    suspend fun uploadNoteImage(imageFile: File, noteId: String): Result<NoteImage>
    
    /**
     * 删除笔记图片
     * @param imageUrl 图片URL
     * @return 成功则返回Unit，失败则返回异常
     */
    suspend fun deleteNoteImage(imageUrl: String): Result<Unit>
}

/**
 * Firebase笔记存储库实现
 * 实现笔记相关的云存储具体操作
 */
@Singleton
class FirebaseNoteRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : FirebaseNoteRepository {
    
    override fun getNotes(): Flow<List<Note>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close(IllegalStateException("用户未登录"))
            return@callbackFlow
        }
        
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val notes = snapshot?.documents?.mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null
                    Note.fromMap(data).copy(id = document.id)
                } ?: emptyList()
                
                trySend(notes)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    override fun getNote(noteId: String): Flow<Note?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close(IllegalStateException("用户未登录"))
            return@callbackFlow
        }
        
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("notes")
            .document(noteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val data = snapshot?.data
                if (snapshot != null && data != null) {
                    val note = Note.fromMap(data).copy(id = snapshot.id)
                    trySend(note)
                } else {
                    trySend(null)
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    override suspend fun saveNote(note: Note): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("用户未登录"))
            
            val noteMap = note.toMap()
            val noteId = note.id.ifEmpty { UUID.randomUUID().toString() }
            
            // 保存笔记
            firestore.collection("users")
                .document(userId)
                .collection("notes")
                .document(noteId)
                .set(noteMap)
                .await()
            
            Result.success(noteId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("用户未登录"))
            
            // 删除笔记
            firestore.collection("users")
                .document(userId)
                .collection("notes")
                .document(noteId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getHabitNotes(habitId: String): Flow<List<HabitNote>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close(IllegalStateException("用户未登录"))
            return@callbackFlow
        }
        
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("habitNotes")
            .whereEqualTo("habitId", habitId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val habitNotes = snapshot?.documents?.mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null
                    HabitNote.fromMap(data).copy(id = document.id)
                } ?: emptyList()
                
                trySend(habitNotes)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    override suspend fun saveHabitNote(habitNote: HabitNote): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("用户未登录"))
            
            val habitNoteMap = habitNote.toMap()
            val habitNoteId = habitNote.id.ifEmpty { UUID.randomUUID().toString() }
            
            // 保存习惯笔记
            firestore.collection("users")
                .document(userId)
                .collection("habitNotes")
                .document(habitNoteId)
                .set(habitNoteMap)
                .await()
            
            Result.success(habitNoteId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteHabitNote(habitNoteId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("用户未登录"))
            
            // 删除习惯笔记
            firestore.collection("users")
                .document(userId)
                .collection("habitNotes")
                .document(habitNoteId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun uploadNoteImage(imageFile: File, noteId: String): Result<NoteImage> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("用户未登录"))
            
            val imageId = UUID.randomUUID().toString()
            val storageRef = storage.reference
                .child("users")
                .child(userId)
                .child("notes")
                .child(noteId)
                .child("images")
                .child("$imageId.jpg")
            
            // 上传图片
            val uploadTask = storageRef.putFile(Uri.fromFile(imageFile))
            val downloadUrl = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }.await()
            
            val noteImage = NoteImage(
                id = imageId,
                uri = downloadUrl.toString(),
                description = "上传于 ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(Date())}"
            )
            
            // 将图片信息保存到Firestore
            firestore.collection("users")
                .document(userId)
                .collection("noteImages")
                .document(imageId)
                .set(noteImage.toMap())
                .await()
            
            Result.success(noteImage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNoteImage(imageUrl: String): Result<Unit> {
        return try {
            // 从下载URL中提取存储路径
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            
            // 删除图片
            storageRef.delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 