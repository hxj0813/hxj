package com.example.test2.presentation.habits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.UserBadgeEntity

/**
 * 徽章集合展示组件
 */
@Composable
fun BadgeCollection(
    badges: List<Pair<BadgeEntity, UserBadgeEntity?>>,
    onBadgeClick: (BadgeEntity, UserBadgeEntity?) -> Unit
) {
    if (badges.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无徽章",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(badges) { (badge, userBadge) ->
                BadgeGridItem(
                    badge = badge,
                    isUnlocked = userBadge != null,
                    progress = userBadge?.progress ?: 0,
                    onClick = { onBadgeClick(badge, userBadge) }
                )
            }
        }
    }
}

/**
 * 单个徽章项 (网格样式)
 */
@Composable
fun BadgeGridItem(
    badge: BadgeEntity,
    isUnlocked: Boolean,
    progress: Int = 100,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isUnlocked) {
        BadgeUtils.getBackgroundColorForRarity(badge.getRarityEnum())
    } else {
        Color.Gray.copy(alpha = 0.3f)
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        // 徽章图标
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (isUnlocked) {
                val icon = BadgeUtils.getBadgeIcon(badge.iconName, badge.getCategoryEnum())
                Icon(
                    imageVector = icon,
                    contentDescription = badge.name,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                
                // 只有在明确有部分进度的情况下显示进度指示器
                // 进度值在0-99之间才显示进度圈
                if (isUnlocked && progress > 0 && progress < 100) {
                    CircularProgressIndicator(
                        progress = progress / 100f,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(4.dp),
                        strokeWidth = 2.dp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            } else {
                // 未解锁状态
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "未解锁",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 徽章名称
        Text(
            text = badge.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // 徽章稀有度
        Text(
            text = BadgeUtils.getRarityText(badge.getRarityEnum()),
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked) backgroundColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            maxLines = 1
        )
    }
} 