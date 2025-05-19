package com.example.test2.data.repository.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.test2.data.firebase.repository.FirebaseHabitNoteRepository
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteImage
import com.example.test2.data.model.NoteMood
import com.example.test2.data.repository.HybridNoteRepository
import com.example.test2.data.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 混合笔记存储库实现
 * 支持在线和离线模式，可以自动切换和同步数据
 */
@Singleton
class HybridNoteRepositoryImpl @Inject constructor(
    private val localRepository: NoteRepository,
    private val firebaseRepository: FirebaseHabitNoteRepository,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : HybridNoteRepository {

    private val TAG = "HybridNoteRepository"
    
    // 是否处于在线模式的状态
    private val onlineMode = AtomicBoolean(false)
    
    init {
        // 初始化时根据登录状态设置模式
        onlineMode.set(auth.currentUser != null)
        
        // 监听登录状态变化
        auth.addAuthStateListener { firebaseAuth ->
            val isLoggedIn = firebaseAuth.currentUser != null
            setOnlineMode(isLoggedIn)
            
            // 如果登录状态发生变化，并且是登录成功
            if (isLoggedIn && !onlineMode.get()) {
                Log.d(TAG, "用户已登录，将尝试同步数据")
                // 这里不直接调用同步方法，因为在回调中执行协程不太合适
                // 登录成功后，ViewModel应该调用同步方法
            }
        }
    }
    
    override fun setOnlineMode(online: Boolean) {
        Log.d(TAG, "设置在线模式: $online")
        onlineMode.set(online)
    }
    
    override fun isOnlineMode(): Boolean {
        return onlineMode.get()
    }
    
    override fun getAllNotes(): Flow<List<HabitNote>> {
        return if (onlineMode.get() && auth.currentUser != null) {
            Log.d(TAG, "在线模式获取所有笔记")
            firebaseRepository.getAllNotes()
                .catch {
                    Log.e(TAG, "在线获取笔记失败，回退到本地模式", it)
                    emitAll(localRepository.getAllNotes())
                }
        } else {
            Log.d(TAG, "离线模式获取所有笔记")
            localRepository.getAllNotes()
        }
    }
    
    override fun getNotesByHabit(habitId: String): Flow<List<HabitNote>> {
        return if (onlineMode.get() && auth.currentUser != null) {
            firebaseRepository.getNotesByHabit(habitId)
                .catch {
                    Log.e(TAG, "在线获取习惯笔记失败，回退到本地模式", it)
                    // 本地仓库没有直接的按习惯ID查询方法，这里用过滤实现
                    emitAll(
                        localRepository.getAllNotes().map { notes ->
                            notes.filter { it.habitId == habitId }
                        }
                    )
                }
        } else {
            // 本地仓库通过过滤实现按习惯查询
            localRepository.getAllNotes().map { notes ->
                notes.filter { it.habitId == habitId }
            }
        }
    }
    
    override fun getNotesByMood(mood: NoteMood): Flow<List<HabitNote>> {
        return if (onlineMode.get() && auth.currentUser != null) {
            firebaseRepository.getNotesByMood(mood)
                .catch {
                    Log.e(TAG, "在线获取心情笔记失败，回退到本地模式", it)
                    emitAll(localRepository.getNotesByMood(mood))
                }
        } else {
            localRepository.getNotesByMood(mood)
        }
    }
    
    override fun getPinnedNotes(): Flow<List<HabitNote>> {
        return if (onlineMode.get() && auth.currentUser != null) {
            firebaseRepository.getPinnedNotes()
                .catch {
                    Log.e(TAG, "在线获取置顶笔记失败，回退到本地模式", it)
                    emitAll(localRepository.getPinnedNotes())
                }
        } else {
            localRepository.getPinnedNotes()
        }
    }
    
    override fun getNoteById(id: String): Flow<HabitNote?> {
        return if (onlineMode.get() && auth.currentUser != null) {
            firebaseRepository.getNoteById(id)
                .catch {
                    Log.e(TAG, "在线获取笔记详情失败，回退到本地模式", it)
                    emitAll(flow {
                        emit(localRepository.getNoteById(id))
                    })
                }
        } else {
            flow {
                emit(localRepository.getNoteById(id))
            }
        }
    }
    
    override suspend fun saveNote(note: HabitNote): Result<String> {
        // 首先保存到本地
        val localResult = try {
            val id = localRepository.saveNote(note)
            if (id > 0) Result.success(note.id) else Result.failure(Exception("本地保存失败"))
        } catch (e: Exception) {
            Log.e(TAG, "本地保存笔记失败", e)
            Result.failure(e)
        }
        
        // 如果在线模式，同时保存到云端
        if (onlineMode.get() && auth.currentUser != null) {
            try {
                val cloudResult = firebaseRepository.saveNote(note)
                return cloudResult
            } catch (e: Exception) {
                Log.e(TAG, "云端保存笔记失败", e)
                // 如果云端保存失败但本地成功，仍然返回成功
                if (localResult.isSuccess) {
                    return localResult
                }
                return Result.failure(e)
            }
        }
        
        return localResult
    }
    
    override suspend fun deleteNote(noteId: String): Result<Unit> {
        // 首先从本地删除
        val localResult = try {
            val success = localRepository.deleteNoteById(noteId)
            if (success) Result.success(Unit) else Result.failure(Exception("本地删除失败"))
        } catch (e: Exception) {
            Log.e(TAG, "本地删除笔记失败", e)
            Result.failure(e)
        }
        
        // 如果在线模式，同时从云端删除
        if (onlineMode.get() && auth.currentUser != null) {
            try {
                val cloudResult = firebaseRepository.deleteNote(noteId)
                return cloudResult
            } catch (e: Exception) {
                Log.e(TAG, "云端删除笔记失败", e)
                // 如果云端删除失败但本地成功，仍然返回成功
                if (localResult.isSuccess) {
                    return localResult
                }
                return Result.failure(e)
            }
        }
        
        return localResult
    }
    
    override suspend fun updateNotePinStatus(noteId: String, isPinned: Boolean): Result<Unit> {
        // 首先在本地更新
        val localResult = try {
            val success = localRepository.updateNotePinStatus(noteId, isPinned)
            if (success) Result.success(Unit) else Result.failure(Exception("本地更新置顶状态失败"))
        } catch (e: Exception) {
            Log.e(TAG, "本地更新笔记置顶状态失败", e)
            Result.failure(e)
        }
        
        // 如果在线模式，同时在云端更新
        if (onlineMode.get() && auth.currentUser != null) {
            try {
                val cloudResult = firebaseRepository.updateNotePinStatus(noteId, isPinned)
                return cloudResult
            } catch (e: Exception) {
                Log.e(TAG, "云端更新笔记置顶状态失败", e)
                // 如果云端更新失败但本地成功，仍然返回成功
                if (localResult.isSuccess) {
                    return localResult
                }
                return Result.failure(e)
            }
        }
        
        return localResult
    }
    
    override suspend fun uploadNoteImage(uri: Uri, context: Context): Result<NoteImage> {
        // 本地图片管理在NoteRepositoryImpl中已经实现，复用该逻辑
        
        // 如果在线模式，上传到云端
        if (onlineMode.get() && auth.currentUser != null) {
            try {
                return firebaseRepository.uploadNoteImage(uri, context)
            } catch (e: Exception) {
                Log.e(TAG, "云端上传图片失败", e)
                // 云端失败后不再尝试本地保存，因为没有合适的本地接口
                return Result.failure(e)
            }
        } else {
            // 离线模式下，返回本地URI作为NoteImage
            // 这里可能需要复制图片到应用内部存储
            // 由于本地存储库没有暴露uploadNoteImage方法，这里临时创建一个NoteImage对象
            val localUri = uri.toString()
            val localImage = NoteImage(
                id = UUID.randomUUID().toString(),
                uri = localUri,
                createdAt = Date() // 使用当前日期而不是时间戳
            )
            return Result.success(localImage)
        }
    }
    
    override suspend fun deleteNoteImage(imageUrl: String): Result<Unit> {
        // 如果在线模式，从云端删除
        if (onlineMode.get() && auth.currentUser != null && imageUrl.startsWith("https://")) {
            try {
                return firebaseRepository.deleteNoteImage(imageUrl)
            } catch (e: Exception) {
                Log.e(TAG, "云端删除图片失败", e)
                return Result.failure(e)
            }
        }
        
        // 本地图片删除通过其他机制处理
        // 本地图片URL通常是content://或file://格式
        return Result.success(Unit)
    }
    
    override suspend fun syncLocalToCloud(): Result<Int> {
        if (!onlineMode.get() || auth.currentUser == null) {
            return Result.failure(IllegalStateException("未登录，无法同步到云端"))
        }
        
        var syncCount = 0
        try {
            // 获取所有本地笔记
            val localNotes = localRepository.getAllNotes().firstOrNull() ?: emptyList()
            
            // 逐个上传到云端
            for (note in localNotes) {
                val result = firebaseRepository.saveNote(note)
                if (result.isSuccess) {
                    syncCount++
                }
            }
            
            Log.d(TAG, "同步完成，成功同步 $syncCount 条笔记到云端")
            return Result.success(syncCount)
        } catch (e: Exception) {
            Log.e(TAG, "同步到云端失败", e)
            return Result.failure(e)
        }
    }
    
    override suspend fun syncCloudToLocal(): Result<Int> {
        if (!onlineMode.get() || auth.currentUser == null) {
            return Result.failure(IllegalStateException("未登录，无法从云端同步"))
        }
        
        var syncCount = 0
        try {
            // 获取所有云端笔记
            val cloudNotes = firebaseRepository.getAllNotes().firstOrNull() ?: emptyList()
            
            // 逐个保存到本地
            for (note in cloudNotes) {
                val id = localRepository.saveNote(note)
                if (id > 0) {
                    syncCount++
                }
            }
            
            Log.d(TAG, "同步完成，成功从云端同步 $syncCount 条笔记")
            return Result.success(syncCount)
        } catch (e: Exception) {
            Log.e(TAG, "从云端同步失败", e)
            return Result.failure(e)
        }
    }
} 