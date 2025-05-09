package com.example.test2.data.model

import android.net.Uri
import android.util.Log
import java.util.Date

/**
 * 笔记图片数据模型
 * 
 * @property id 图片ID
 * @property uri 图片URI，存储本地文件路径
 * @property description 图片描述
 * @property createdAt 创建时间
 */
data class NoteImage(
    val id: String,
    val uri: String,
    val description: String = "",
    val createdAt: Date = Date()
) {
    /**
     * 将URI字符串转换为android.net.Uri对象
     * 处理各种可能的URI格式
     */
    fun getAndroidUri(): Uri? {
        return try {
            if (uri.startsWith("file://")) {
                Uri.parse(uri)
            } else if (uri.startsWith("/")) {
                Uri.parse("file://$uri")
            } else {
                // 尝试直接解析
                Uri.parse(uri)
            }
        } catch (e: Exception) {
            Log.e("NoteImage", "无法解析URI: $uri", e)
            null
        }
    }
    
    /**
     * 检查URI是否有效
     */
    fun hasValidUri(): Boolean {
        return uri.isNotEmpty() && getAndroidUri() != null
    }
    
    companion object {
        /**
         * 创建一个格式化的URI
         */
        fun formatUri(uri: String): String {
            return if (uri.startsWith("file://")) {
                uri
            } else if (uri.startsWith("/")) {
                "file://$uri"
            } else {
                uri
            }
        }
    }
} 