package com.example.test2.presentation.habits.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.test2.data.model.NoteImage

/**
 * 图片缩略图组件
 */
@Composable
fun ImageThumbnail(
    image: NoteImage,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    
    // 记录图片信息用于调试
    Log.d("ImageThumbnail", "处理图片 ID: ${image.id}")
    Log.d("ImageThumbnail", "图片原始URI: ${image.uri}")
    
    // 尝试从字符串解析URI - 增强版
    val imageUri = try {
        // 尝试多种URI格式
        val uri = when {
            image.uri.startsWith("file://") -> {
                Uri.parse(image.uri)
            }
            image.uri.startsWith("/") -> {
                Uri.parse("file://${image.uri}")
            }
            image.uri.startsWith("content://") -> {
                Uri.parse(image.uri)
            }
            else -> {
                // 尝试直接解析
                Log.d("ImageThumbnail", "尝试直接解析URI: ${image.uri}")
                try {
                    Uri.parse(image.uri)
                } catch (e: Exception) {
                    Log.e("ImageThumbnail", "直接解析失败，尝试添加file://前缀")
                    Uri.parse("file://${image.uri}")
                }
            }
        }
        
        Log.d("ImageThumbnail", "解析后的URI: $uri")
        // 检查文件是否存在
        if (uri.scheme == "file") {
            val path = uri.path
            if (path != null) {
                val file = java.io.File(path)
                Log.d("ImageThumbnail", "检查文件是否存在: ${file.absolutePath}, 存在: ${file.exists()}, 大小: ${if (file.exists()) file.length() else "N/A"}")
            }
        }
        uri
    } catch (e: Exception) {
        Log.e("ImageThumbnail", "无法解析URI: ${image.uri}", e)
        null
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        // 使用Coil的SubcomposeAsyncImage加载图片
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(
                    // 尝试多种数据源格式
                    when {
                        // 优先使用URI对象
                        imageUri != null -> {
                            Log.d("ImageThumbnail", "使用URI加载图片: $imageUri")
                            imageUri
                        }
                        // 如果是文件路径，尝试使用文件
                        image.uri.startsWith("/") -> {
                            val file = java.io.File(image.uri)
                            Log.d("ImageThumbnail", "使用文件路径加载图片: ${file.absolutePath}, 存在: ${file.exists()}")
                            file
                        }
                        // 最后尝试直接使用字符串
                        else -> {
                            Log.d("ImageThumbnail", "使用原始字符串加载图片: ${image.uri}")
                            image.uri
                        }
                    }
                )
                .crossfade(true)
                .build(),
            contentDescription = "笔记图片",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                // 加载中状态
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            error = {
                // 错误状态 - 增加详细日志
                Log.e("ImageThumbnail", "图片加载失败: $imageUri")
                
                // 尝试检查文件是否存在
                if (imageUri?.scheme == "file") {
                    val path = imageUri.path
                    if (path != null) {
                        val file = java.io.File(path)
                        Log.e("ImageThumbnail", "文件检查: 存在=${file.exists()}, 路径=${file.absolutePath}, 可读=${file.canRead()}")
                    }
                }
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = getRandomColor(image.id))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "图片加载失败",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        )
        
        // 删除按钮
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(color = Color.Black.copy(alpha = 0.6f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "删除图片",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 获取基于ID的随机颜色
 */
private fun getRandomColor(id: String): Color {
    val colors = listOf(
        Color(0xFF6200EE),
        Color(0xFF03DAC5),
        Color(0xFF3700B3),
        Color(0xFFBB86FC),
        Color(0xFF018786)
    )
    
    // 使用ID的哈希值来选择一个颜色
    val index = id.hashCode().rem(colors.size).let { if (it < 0) -it else it }
    return colors[index]
} 