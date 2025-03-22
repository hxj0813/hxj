package com.example.test2.presentation.habits.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.test2.data.model.NoteImage
import kotlin.random.Random

/**
 * 图片选择器组件
 * 
 * @param images 已选择的图片列表
 * @param onAddImage 添加图片回调
 * @param onRemoveImage 移除图片回调
 * @param modifier Modifier修饰符
 */
@Composable
fun ImageSelector(
    images: List<NoteImage>,
    onAddImage: () -> Unit,
    onRemoveImage: (NoteImage) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddImageHint by remember { mutableStateOf(false) }
    var expandedImage by remember { mutableStateOf<NoteImage?>(null) }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 图片预览网格
        if (images.isNotEmpty()) {
            ImageGrid(
                images = images,
                onImageClick = { expandedImage = it },
                onRemoveImage = onRemoveImage
            )
        }
        
        // 添加图片按钮
        AddImageButton(
            isExpanded = showAddImageHint,
            onExpandChange = { showAddImageHint = it },
            onAddClick = onAddImage
        )
    }
    
    // 图片预览对话框
    if (expandedImage != null) {
        ImagePreviewDialog(
            image = expandedImage!!,
            onDismiss = { expandedImage = null }
        )
    }
}

/**
 * 图片网格组件
 */
@Composable
fun ImageGrid(
    images: List<NoteImage>,
    onImageClick: (NoteImage) -> Unit,
    onRemoveImage: (NoteImage) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .let { 
                val rows = images.size / 3 + if (images.size % 3 > 0) 1 else 0
                val heightValue = rows * 120
                it.height(height = heightValue.dp)
            }
    ) {
        items(images) { image ->
            ImageThumbnail(
                image = image,
                onClick = { onImageClick(image) },
                onRemove = { onRemoveImage(image) }
            )
        }
    }
}

/**
 * 图片缩略图组件
 */
@Composable
fun ImageThumbnail(
    image: NoteImage,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        // 模拟图片加载 - 在实际应用中使用Coil或Glide加载图片
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getRandomColor(image.id))
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = "图片",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
            )
        }
        
        // 删除按钮
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
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
 * 添加图片按钮组件
 */
@Composable
fun AddImageButton(
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onExpandChange(!isExpanded) }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "添加图片",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "添加图片",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 展开/收起指示器 - 可以根据需要添加动画
                Text(
                    text = if (isExpanded) "收起" else "展开",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
            
            // 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // 图片上传区域
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onAddClick() }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "上传图片",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "点击此处选择照片",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "支持JPEG、PNG格式",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 按钮行
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onAddClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("选择图片")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 图片预览对话框
 */
@Composable
fun ImagePreviewDialog(
    image: NoteImage,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 图片标题
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = image.description ?: "图片预览",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 图片预览 - 在实际应用中使用Coil或Glide加载图片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(getRandomColor(image.id))
                ) {
                    // 模拟图片
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "图片内容",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 图片信息
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "添加日期：${image.createdAt}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (image.description != null) {
                        Text(
                            text = "描述：${image.description}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "无描述",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

/**
 * 生成随机颜色 - 用于演示图片
 */
private fun getRandomColor(seed: String): Color {
    val random = Random(seed.hashCode())
    return Color(
        red = random.nextFloat() * 0.6f + 0.2f,
        green = random.nextFloat() * 0.6f + 0.2f,
        blue = random.nextFloat() * 0.6f + 0.2f,
        alpha = 1.0f
    )
} 