package com.example.test2.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.repository.HybridNoteRepository
import com.example.test2.presentation.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 数据同步状态
 */
data class DataSyncState(
    val isOnlineMode: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncResult: String? = null,
    val syncCount: Int = 0,
    val error: String? = null
)

/**
 * 数据同步ViewModel
 * 管理在线/离线模式切换和数据同步
 */
@HiltViewModel
class DataSyncViewModel @Inject constructor(
    private val hybridRepository: HybridNoteRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val _syncState = MutableStateFlow(DataSyncState())
    val syncState: StateFlow<DataSyncState> = _syncState.asStateFlow()
    
    init {
        // 初始化时根据登录状态设置模式
        val isLoggedIn = auth.currentUser != null
        _syncState.value = _syncState.value.copy(isOnlineMode = isLoggedIn)
        hybridRepository.setOnlineMode(isLoggedIn)
        
        // 监听登录状态变化
        auth.addAuthStateListener { firebaseAuth ->
            val newLoginState = firebaseAuth.currentUser != null
            if (newLoginState != _syncState.value.isOnlineMode) {
                setOnlineMode(newLoginState)
                
                // 如果是登录成功，自动同步数据
                if (newLoginState) {
                    syncDataAfterLogin()
                }
            }
        }
    }
    
    /**
     * 设置在线模式
     */
    fun setOnlineMode(online: Boolean) {
        viewModelScope.launch {
            _syncState.value = _syncState.value.copy(
                isOnlineMode = online,
                error = null
            )
            hybridRepository.setOnlineMode(online)
            Log.d("DataSyncViewModel", "设置在线模式: $online")
        }
    }
    
    /**
     * 登录后同步数据
     * 将本地数据同步到云端
     */
    fun syncDataAfterLogin() {
        if (!_syncState.value.isOnlineMode || auth.currentUser == null) {
            _syncState.value = _syncState.value.copy(
                error = "未登录，无法同步数据"
            )
            return
        }
        
        viewModelScope.launch {
            _syncState.value = _syncState.value.copy(
                isSyncing = true,
                error = null
            )
            
            try {
                // 先从云端同步到本地
                val cloudToLocalResult = hybridRepository.syncCloudToLocal()
                
                if (cloudToLocalResult.isSuccess) {
                    val cloudCount = cloudToLocalResult.getOrDefault(0)
                    Log.d("DataSyncViewModel", "从云端同步了 $cloudCount 条笔记")
                    
                    // 再从本地同步到云端
                    val localToCloudResult = hybridRepository.syncLocalToCloud()
                    
                    if (localToCloudResult.isSuccess) {
                        val localCount = localToCloudResult.getOrDefault(0)
                        Log.d("DataSyncViewModel", "向云端同步了 $localCount 条笔记")
                        
                        _syncState.value = _syncState.value.copy(
                            isSyncing = false,
                            lastSyncResult = "同步成功：从云端同步 $cloudCount 条笔记，向云端同步 $localCount 条笔记",
                            syncCount = cloudCount + localCount,
                            error = null
                        )
                    } else {
                        throw localToCloudResult.exceptionOrNull() ?: Exception("向云端同步失败")
                    }
                } else {
                    throw cloudToLocalResult.exceptionOrNull() ?: Exception("从云端同步失败")
                }
            } catch (e: Exception) {
                Log.e("DataSyncViewModel", "同步数据失败", e)
                _syncState.value = _syncState.value.copy(
                    isSyncing = false,
                    error = "同步失败：${e.message}"
                )
            }
        }
    }
    
    /**
     * 手动触发同步
     */
    fun syncData() {
        syncDataAfterLogin()
    }
    
    /**
     * 清除同步错误
     */
    fun clearSyncError() {
        _syncState.value = _syncState.value.copy(error = null)
    }
} 