package com.example.test2.presentation.habits.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * 徽章展示项组件
 */
@Composable
fun BadgeItem(
    badge: BadgeEntity,
    userBadge: UserBadgeEntity? = null,
    onClick: (BadgeEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUnlocked = userBadge != null
    val isHighlighted = userBadge?.highlighted == true
    
    val backgroundColor = BadgeUtils.getBackgroundColorForRarity(badge.getRarityEnum())
    val borderColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primary
    } else {
        backgroundColor.copy(alpha = 0.6f)
    }
    
    val bgColor by animateColorAsState(
        targetValue = if (isUnlocked) backgroundColor else Color.Gray.copy(alpha = 0.3f),
        label = "badgeColor"
    )
    
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(
                    width = if (isHighlighted) 3.dp else 1.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .clickable { onClick(badge) },
            contentAlignment = Alignment.Center
        ) {
            // 徽章图标
            val icon = BadgeUtils.getBadgeIcon(badge.iconName, badge.getCategoryEnum())
            Icon(
                imageVector = icon,
                contentDescription = badge.name,
                tint = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
            
            // 如果徽章有进度但未完全解锁
            if (userBadge != null && userBadge.progress < 100) {
                Text(
                    text = "${userBadge.progress}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }
        }
        
        // 徽章名称
        Text(
            text = badge.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked) MaterialTheme.colorScheme.onBackground 
                  else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
} 