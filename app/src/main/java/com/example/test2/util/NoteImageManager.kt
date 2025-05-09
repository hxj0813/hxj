package com.example.test2.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 笔记图片管理工具类
 * 负责处理笔记图片的保存、加载和删除
 */
@Singleton
class NoteImageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 图片存储目录
    private val imageDir: File by lazy {
        File(context.filesDir, "note_images").apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 保存Bitmap图片并返回URI
     * @param bitmap 要保存的图片
     * @return 保存后的图片URI
     */
    suspend fun saveImage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val filename = "img_${UUID.randomUUID()}.jpg"
        val file = File(imageDir, filename)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        
        // 返回内部文件URI
        "file://${file.absolutePath}"
    }
    
    /**
     * 从外部URI保存图片
     * @param uri 外部图片URI
     * @return 保存后的图片URI
     */
    suspend fun saveImageFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("NoteImageManager", "开始从URI保存图片: $uri")
            
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            android.util.Log.d("NoteImageManager", "图片已加载到内存: ${bitmap.width}x${bitmap.height}")
            
            // 根据图片大小进行适当的压缩
            val scaledBitmap = if (bitmap.width > 1920 || bitmap.height > 1920) {
                val scale = 1920f / maxOf(bitmap.width, bitmap.height)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else {
                bitmap
            }
            
            // 保存图片
            val filename = "img_${UUID.randomUUID()}.jpg"
            val file = File(imageDir, filename)
            android.util.Log.d("NoteImageManager", "准备保存到文件: ${file.absolutePath}")
            
            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            
            // 统一返回file://格式的URI
            val resultUri = "file://${file.absolutePath}"
            android.util.Log.d("NoteImageManager", "图片已保存，返回URI: $resultUri")
            
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            
            resultUri
        } catch (e: Exception) {
            android.util.Log.e("NoteImageManager", "保存图片失败: ${e.message}", e)
            throw IllegalStateException("保存图片失败: ${e.message}", e)
        }
    }
    
    /**
     * 删除图片
     * @param uri 图片URI
     * @return 删除是否成功
     */
    suspend fun deleteImage(uri: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (uri.startsWith("file://")) {
                val path = uri.substringAfter("file://")
                val file = File(path)
                if (file.exists() && file.isFile) {
                    file.delete()
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 加载图片
     * @param uri 图片URI
     * @return 加载的位图，如果加载失败则返回null
     */
    suspend fun loadImage(uri: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (uri.startsWith("file://")) {
                val path = uri.substringAfter("file://")
                BitmapFactory.decodeFile(path)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 清理未使用的图片
     * @param usedUris 仍在使用的图片URI列表
     * @return 清理的图片数量
     */
    suspend fun cleanupUnusedImages(usedUris: List<String>): Int = withContext(Dispatchers.IO) {
        var count = 0
        try {
            val usedPaths = usedUris
                .filter { it.startsWith("file://") }
                .map { it.substringAfter("file://") }
                .toSet()
            
            imageDir.listFiles()?.forEach { file ->
                if (file.isFile && file.absolutePath !in usedPaths) {
                    if (file.delete()) {
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            // 忽略清理过程中的错误
        }
        count
    }
} 