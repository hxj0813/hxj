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
            
            // 确保目录存在且可写
            if (!imageDir.exists()) {
                val created = imageDir.mkdirs()
                android.util.Log.d("NoteImageManager", "创建图片目录: $created")
            } else {
                android.util.Log.d("NoteImageManager", "图片目录已存在: ${imageDir.absolutePath}")
                android.util.Log.d("NoteImageManager", "目录可写: ${imageDir.canWrite()}")
            }
            
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
            
            // 确认文件已保存
            if (file.exists()) {
                android.util.Log.d("NoteImageManager", "文件已保存，大小: ${file.length()} 字节")
            } else {
                android.util.Log.e("NoteImageManager", "文件保存失败，文件不存在")
            }
            
            // 尝试使用绝对路径而不是URI，可能更兼容某些设备
            val filePath = file.absolutePath
            android.util.Log.d("NoteImageManager", "返回文件路径: $filePath")
            
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            
            filePath // 直接返回文件路径，而不是URI格式
        } catch (e: Exception) {
            android.util.Log.e("NoteImageManager", "保存图片失败: ${e.message}", e)
            throw IllegalStateException("保存图片失败: ${e.message}", e)
        }
    }
    
    /**
     * 删除图片
     * @param uri 图片URI或路径
     * @return 删除是否成功
     */
    suspend fun deleteImage(uri: String): Boolean = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("NoteImageManager", "准备删除图片: $uri")
            
            val file = when {
                uri.startsWith("file://") -> {
                    File(uri.substring(7))
                }
                uri.startsWith("/") -> {
                    File(uri)
                }
                else -> {
                    // 尝试作为直接路径
                    File(uri)
                }
            }
            
            android.util.Log.d("NoteImageManager", "文件路径: ${file.absolutePath}, 存在: ${file.exists()}")
            
            if (file.exists() && file.isFile) {
                val success = file.delete()
                android.util.Log.d("NoteImageManager", "删除结果: $success")
                success
            } else {
                android.util.Log.e("NoteImageManager", "文件不存在或不是文件")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("NoteImageManager", "删除图片出错: ${e.message}", e)
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
        android.util.Log.d("NoteImageManager", "跳过清理图片，保留全部图片")
        return@withContext 0
//        var count = 0
//        try {
//            android.util.Log.d("NoteImageManager", "开始清理未使用的图片，保留列表大小: ${usedUris.size}")
//
//            // 收集所有可能的路径格式
//            val protectedPaths = mutableSetOf<String>()
//
//            for (uri in usedUris) {
//                android.util.Log.d("NoteImageManager", "处理保护的URI: $uri")
//
//                // 添加原始URI
//                protectedPaths.add(uri)
//
//                // 如果是带file://前缀的，也添加不带前缀的版本
//                if (uri.startsWith("file://")) {
//                    val pathOnly = uri.substringAfter("file://")
//                    protectedPaths.add(pathOnly)
//                    // 还需要检查文件名部分
//                    val fileName = File(pathOnly).name
//                    protectedPaths.add(fileName)
//                }
//
//                // 如果是绝对路径，添加带file://前缀的版本
//                if (uri.startsWith("/")) {
//                    protectedPaths.add("file://$uri")
//                    // 还需要检查文件名部分
//                    val fileName = File(uri).name
//                    protectedPaths.add(fileName)
//                }
//
//                // 如果只是文件名，需要特别保护
//                if (!uri.contains("/")) {
//                    protectedPaths.add(uri)
//                }
//            }
//
//            android.util.Log.d("NoteImageManager", "转换后的保护路径数量: ${protectedPaths.size}")
//
//            // 确保目录存在
//            if (!imageDir.exists()) {
//                android.util.Log.w("NoteImageManager", "图片目录不存在，无需清理")
//                return@withContext 0
//            }
//
//            // 列出所有图片文件
//            val files = imageDir.listFiles()
//            if (files == null || files.isEmpty()) {
//                android.util.Log.d("NoteImageManager", "图片目录为空，无需清理")
//                return@withContext 0
//            }
//
//            android.util.Log.d("NoteImageManager", "图片目录中有 ${files.size} 个文件")
//
//            // 检查每个文件是否在使用
//            for (file in files) {
//                if (!file.isFile) continue
//
//                val absolutePath = file.absolutePath
//                val fileName = file.name
//
//                // 仔细检查这个文件是否在保护列表中
//                val isProtected = protectedPaths.any { path ->
//                    absolutePath.endsWith(path) ||
//                    path.endsWith(absolutePath) ||
//                    absolutePath.contains(path) ||
//                    path.contains(fileName)
//                }
//
//                android.util.Log.d("NoteImageManager", "检查文件: $absolutePath, 受保护: $isProtected")
//
//                // 如果文件不在保护列表中，删除它
//                if (!isProtected) {
//                    try {
//                        val deleted = file.delete()
//                        if (deleted) {
//                            android.util.Log.d("NoteImageManager", "已删除未使用的文件: $absolutePath")
//                            count++
//                        } else {
//                            android.util.Log.e("NoteImageManager", "无法删除文件: $absolutePath")
//                        }
//                    } catch (e: Exception) {
//                        android.util.Log.e("NoteImageManager", "删除文件时出错: ${e.message}", e)
//                    }
//                } else {
//                    android.util.Log.d("NoteImageManager", "保留使用中的文件: $absolutePath")
//                }
//            }
//
//            android.util.Log.d("NoteImageManager", "清理完成，共删除 $count 个未使用的文件")
//        } catch (e: Exception) {
//            android.util.Log.e("NoteImageManager", "清理过程中出现异常: ${e.message}", e)
//        }
//
//        count
    }
} 