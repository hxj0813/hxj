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
    
    // 尝试从字符串解析URI
    val imageUri = try {
        // 使用NoteImage的新方法获取Android URI
        val uri = image.getAndroidUri()
        Log.d("ImageThumbnail", "加载图片URI: $uri")
        uri
    } catch (e: Exception) {
        Log.e("ImageThumbnail", "解析URI失败: ${image.uri}", e)
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
                .data(imageUri)
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
                // 错误状态
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = getRandomColor(image.id))
                ) {
                    Log.e("ImageThumbnail", "图片加载失败: ${image.uri}")
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