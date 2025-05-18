package com.example.test2.data.model

import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
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
     * 将笔记图片数据转换为Firestore可存储的Map
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "uri" to uri,
            "description" to description,
            "createdAt" to Timestamp(createdAt.time / 1000, ((createdAt.time % 1000) * 1000000).toInt())
        )
    }
    
    /**
     * 将URI字符串或文件路径转换为android.net.Uri对象
     * 处理各种可能的URI格式
     */
    fun getAndroidUri(): Uri? {
        return try {
            Log.d("NoteImage", "尝试解析URI: $uri")
            
            val result = when {
                uri.startsWith("file://") -> {
                    // 已经是标准file://格式
                    Log.d("NoteImage", "URI是标准file://格式")
                    Uri.parse(uri)
                }
                uri.startsWith("/") -> {
                    // 仅绝对路径，转为file://格式
                    Log.d("NoteImage", "URI是绝对路径，转为file://格式")
                    Uri.parse("file://$uri")
                }
                uri.startsWith("content://") -> {
                    // content://格式
                    Log.d("NoteImage", "URI是content://格式")
                    Uri.parse(uri)
                }
                else -> {
                    // 其他格式，尝试直接解析
                    try {
                        Log.d("NoteImage", "尝试直接解析URI")
                        Uri.parse(uri)
                    } catch (e: Exception) {
                        Log.e("NoteImage", "直接解析失败，尝试作为文件路径处理", e)
                        // 可能是文件路径，确保它是绝对路径
                        val file = java.io.File(uri)
                        Uri.fromFile(file)
                    }
                }
            }
            
            Log.d("NoteImage", "解析结果: $result")
            
            // 如果是文件URI，验证文件是否存在
            if (result.scheme == "file") {
                val path = result.path
                if (path != null) {
                    val file = java.io.File(path)
                    val exists = file.exists()
                    Log.d("NoteImage", "文件存在: $exists, 路径: ${file.absolutePath}")
                    
                    // 如果文件不存在，尝试使用不同的解析方式
                    if (!exists) {
                        Log.w("NoteImage", "文件不存在，尝试备用解析方法")
                        
                        // 尝试从原始URI直接构建文件
                        val originalFile = java.io.File(uri)
                        if (originalFile.exists()) {
                            Log.d("NoteImage", "找到文件：${originalFile.absolutePath}")
                            return Uri.fromFile(originalFile)
                        }
                        
                        // 尝试移除可能的前缀
                        val cleanPath = uri.replace("file://", "")
                        val cleanFile = java.io.File(cleanPath)
                        if (cleanFile.exists()) {
                            Log.d("NoteImage", "清理路径后找到文件：${cleanFile.absolutePath}")
                            return Uri.fromFile(cleanFile)
                        }
                    }
                }
            }
            
            result
        } catch (e: Exception) {
            Log.e("NoteImage", "无法解析URI: $uri", e)
            null
        }
    }
    
    /**
     * 检查URI是否指向有效文件
     */
    fun isValidFile(): Boolean {
        val uri = getAndroidUri() ?: return false
        if (uri.scheme == "file") {
            val path = uri.path ?: return false
            val file = java.io.File(path)
            return file.exists() && file.isFile && file.canRead()
        }
        return false
    }
    
    /**
     * 获取文件路径（如果这是一个文件URI）
     */
    fun getFilePath(): String? {
        val uri = getAndroidUri() ?: return null
        return if (uri.scheme == "file") uri.path else null
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