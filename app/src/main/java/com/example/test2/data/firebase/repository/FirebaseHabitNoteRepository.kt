package com.example.test2.data.firebase.repository

import android.net.Uri
import android.content.Context
import android.util.Log
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteImage
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteTag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase习惯笔记存储库接口
 * 定义与习惯笔记相关的云存储操作
 */
interface FirebaseHabitNoteRepository {
    /**
     * 获取所有笔记
     */
    fun getAllNotes(): Flow<List<HabitNote>>
    
    /**
     * 获取特定习惯的笔记
     */
    fun getNotesByHabit(habitId: String): Flow<List<HabitNote>>
    
    /**
     * 按心情获取笔记
     */
    fun getNotesByMood(mood: NoteMood): Flow<List<HabitNote>>
    
    /**
     * 获取顶置笔记
     */
    fun getPinnedNotes(): Flow<List<HabitNote>>
    
    /**
     * 获取具体笔记内容
     */
    fun getNoteById(id: String): Flow<HabitNote?>
    
    /**
     * 保存笔记
     */
    suspend fun saveNote(note: HabitNote): Result<String>
    
    /**
     * 删除笔记
     */
    suspend fun deleteNote(noteId: String): Result<Unit>
    
    /**
     * 更新笔记的顶置状态
     */
    suspend fun updateNotePinStatus(noteId: String, isPinned: Boolean): Result<Unit>
    
    /**
     * 上传笔记图片
     */
    suspend fun uploadNoteImage(uri: Uri, context: Context): Result<NoteImage>
    
    /**
     * 删除笔记图片
     */
    suspend fun deleteNoteImage(imageUrl: String): Result<Unit>
}

/**
 * Firebase习惯笔记存储库实现
 * 实现习惯笔记相关的云存储具体操作
 */
@Singleton
class FirebaseHabitNoteRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : FirebaseHabitNoteRepository {

    override fun getAllNotes(): Flow<List<HabitNote>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close(IllegalStateException("用户未登录"))
            return@callbackFlow
        }
        
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("habitNotes")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // 忽略索引错误，只发送其他类型的错误
                    if (!error.message.toString().contains("FAILED_PRECONDITION") && 
                        !error.message.toString().contains("requires an index")) {
                        close(error)
                    } else {
                        // 对于索引错误，发送空列表而不是关闭流
                        trySend(emptyList())
                        // 输出日志但不中断流
                        Log.w("FirebaseHabitNote", "等待索引创建完成: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                
                val notes = snapshot?.documents?.mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null
                    try {
                        HabitNote.fromMap(data).copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(notes)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    override fun getNotesByHabit(habitId: String): Flow<List<HabitNote>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close(IllegalStateException("用户未登录"))
            return@callbackFlow
        }
        
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("habitNotes")
            .whereEqualTo("habitId", habitId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // 忽略索引错误，只发送其他类型的错误
                    if (!error.message.toString().contains("FAILED_PRECONDITION") && 
                        !error.message.toString().contains("requires an index")) {
                        close(error)
                    } else {
                        // 对于索引错误，发送空列表而不是关闭流
                        trySend(emptyList())
                        // 输出日志但不中断流
                        Log.w("FirebaseHabitNote", "等待索引创建完成: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                
                val notes = snapshot?.documents?.mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null
                    try {
                        HabitNote.fromMap(data).copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(notes)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    override fun getNotesByMood(mood: NoteMood): Flow<List<HabitNote>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close(IllegalStateException("用户未登录"))
            return@callbackFlow
        }
        
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("habitNotes")
            .whereEqualTo("mood", mood.name)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // 忽略索引错误，只发送其他类型的错误
                    if (!error.message.toString().contains("FAILED_PRECONDITION") && 
                        !error.message.toString().contains("requires an index")) {
                        close(error)
                    } else {
                        // 对于索引错误，发送空列表而不是关闭流
                        trySend(emptyList())
                        // 输出日志但不中断流
                        Log.w("FirebaseHabitNote", "等待索引创建完成: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                
                val notes = snapshot?.documents?.mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null
                    try {
                        HabitNote.fromMap(data).copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(notes)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    override fun getPinnedNotes(): Flow<List<HabitNote>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close(IllegalStateException("用户未登录"))
            return@callbackFlow
        }
        
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("habitNotes")
            .whereEqualTo("isPinned", true)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // 忽略索引错误，只发送其他类型的错误
                    if (!error.message.toString().contains("FAILED_PRECONDITION") && 
                        !error.message.toString().contains("requires an index")) {
                        close(error)
                    } else {
                        // 对于索引错误，发送空列表而不是关闭流
                        trySend(emptyList())
                        // 输出日志但不中断流
                        Log.w("FirebaseHabitNote", "等待索引创建完成: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                
                val notes = snapshot?.documents?.mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null
                    try {
                        HabitNote.fromMap(data).copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(notes)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    override fun getNoteById(id: String): Flow<HabitNote?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close(IllegalStateException("用户未登录"))
            return@callbackFlow
        }
        
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("habitNotes")
            .document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val data = snapshot?.data
                if (snapshot != null && data != null) {
                    try {
                        val note = HabitNote.fromMap(data).copy(id = snapshot.id)
                        trySend(note)
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    override suspend fun saveNote(note: HabitNote): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("用户未登录"))
            
            val noteMap = note.toMap()
            val noteId = note.id.ifEmpty { UUID.randomUUID().toString() }
            
            // 保存笔记
            firestore.collection("users")
                .document(userId)
                .collection("habitNotes")
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
                .collection("habitNotes")
                .document(noteId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateNotePinStatus(noteId: String, isPinned: Boolean): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("用户未登录"))
            
            // 更新顶置状态
            firestore.collection("users")
                .document(userId)
                .collection("habitNotes")
                .document(noteId)
                .update("isPinned", isPinned)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun uploadNoteImage(uri: Uri, context: Context): Result<NoteImage> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("用户未登录"))
            
            // 创建唯一图片ID
            val imageId = UUID.randomUUID().toString()
            
            // 从URI获取输入流
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("无法打开图片"))
            
            // 创建存储引用
            val storageRef = storage.reference
                .child("users")
                .child(userId)
                .child("noteImages")
                .child("$imageId.jpg")
            
            // 上传图片
            val uploadTask = storageRef.putStream(inputStream)
            val downloadUrl = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }.await()
            
            // 创建NoteImage对象
            val noteImage = NoteImage(
                id = imageId,
                uri = downloadUrl.toString(),
                description = "上传于 ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}",
                createdAt = java.util.Date()
            )
            
            Result.success(noteImage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNoteImage(imageUrl: String): Result<Unit> {
        return try {
            // 从下载URL中提取存储引用
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            
            // 删除图片
            storageRef.delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 