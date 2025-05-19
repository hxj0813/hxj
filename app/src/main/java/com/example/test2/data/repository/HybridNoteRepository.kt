package com.example.test2.data.repository

import android.content.Context
import android.net.Uri
import com.example.test2.data.firebase.repository.FirebaseHabitNoteRepository
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteImage
import com.example.test2.data.model.NoteMood
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 混合笔记存储库接口
 * 扩展自FirebaseHabitNoteRepository，同时支持离线和在线模式
 * 不登录也可以使用基本功能，登录后可以同步数据
 */
interface HybridNoteRepository : FirebaseHabitNoteRepository {
    /**
     * 设置当前是否处于在线模式
     * @param online 是否在线（已登录）
     */
    fun setOnlineMode(online: Boolean)
    
    /**
     * 获取当前是否处于在线模式
     * @return 是否在线
     */
    fun isOnlineMode(): Boolean
    
    /**
     * 同步本地数据到云端
     * 当用户登录后调用此方法进行数据同步
     * @return 同步结果
     */
    suspend fun syncLocalToCloud(): Result<Int>
    
    /**
     * 同步云端数据到本地
     * 当用户登录后调用此方法从云端获取数据
     * @return 同步结果
     */
    suspend fun syncCloudToLocal(): Result<Int>
} 