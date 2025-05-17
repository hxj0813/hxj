package com.example.test2.presentation.habits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.test2.data.local.entity.BadgeCategory
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.BadgeRarity
import com.example.test2.data.local.entity.UserBadgeEntity
import java.util.*

/**
 * 徽章列表组件
 *
 * @param userBadges 用户获得的徽章列表
 * @param allBadges 所有徽章列表
 * @param onBadgeClick 徽章点击事件回调
 */
@Composable
fun BadgeList(
    userBadges: List<UserBadgeEntity>,
    allBadges: List<BadgeEntity>,
    onBadgeClick: (BadgeEntity, UserBadgeEntity) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
    ) {
        items(userBadges) { userBadge ->
            val badge = allBadges.find { it.id == userBadge.badgeId }
            if (badge != null) {
                BadgeListItem(
                    badge = badge,
                    userBadge = userBadge,
                    onClick = { onBadgeClick(badge, userBadge) }
                )
            }
        }
    }
}

/**
 * 徽章项组件
 *
 * @param badge 徽章实体
 * @param userBadge 用户徽章实体
 * @param onClick 点击事件回调
 */
@Composable
fun BadgeListItem(
    badge: BadgeEntity,
    userBadge: UserBadgeEntity,
    onClick: () -> Unit
) {
    val badgeColor = Color(badge.getColorByRarity())
    val isHighlighted = userBadge.highlighted
    
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            // 徽章图标
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f))
                    .border(
                        width = if (isHighlighted) 2.dp else 0.dp,
                        color = if (isHighlighted) badgeColor else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 使用Material图标作为徽章图标
                val icon = getMaterialIcon(badge.iconName)
                Icon(
                    imageVector = icon,
                    contentDescription = badge.name,
                    tint = badgeColor,
                    modifier = Modifier.size(32.dp)
                )
                
                // 高亮指示器
                if (isHighlighted) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(Alignment.TopEnd)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 徽章名称
            Text(
                text = badge.name,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 根据图标名称获取Material图标
 */
@Composable
fun getMaterialIcon(iconName: String) = when (iconName) {
    // 连续打卡徽章
    "streak_badge_3" -> Icons.Default.Whatshot
    "streak_badge_7" -> Icons.Default.ThumbUp
    "streak_badge_14" -> Icons.Default.Bolt
    "streak_badge_21" -> Icons.Default.AutoAwesome
    "streak_badge_30" -> Icons.Default.EmojiEvents
    "streak_badge_60" -> Icons.Default.Timelapse
    "streak_badge_100" -> Icons.Default.WorkspacePremium
    "streak_badge_365" -> Icons.Default.Diamond
    
    // 累计完成徽章
    "completion_badge_10" -> Icons.Default.Check
    "completion_badge_50" -> Icons.Default.TaskAlt
    "completion_badge_100" -> Icons.Default.CheckCircle
    "completion_badge_500" -> Icons.Default.VerifiedUser
    "completion_badge_1000" -> Icons.Default.Stars
    
    // 全局累计完成徽章
    "global_badge_50" -> Icons.Default.Insights
    "global_badge_100" -> Icons.Default.ShowChart
    "global_badge_500" -> Icons.Default.BarChart
    "global_badge_1000" -> Icons.Default.TrendingUp
    "global_badge_5000" -> Icons.Default.Leaderboard
    
    // 多样性徽章
    "variety_badge_3" -> Icons.Default.Category
    "variety_badge_5" -> Icons.Default.GridView
    "variety_badge_8" -> Icons.Default.ViewModule
    "balanced_badge" -> Icons.Default.Balance
    
    // 特殊成就徽章
    "early_bird_badge" -> Icons.Default.WbSunny
    "night_owl_badge" -> Icons.Default.Bedtime
    "perfect_week_badge" -> Icons.Default.CalendarViewWeek
    "rapid_progress_badge" -> Icons.Default.FlashOn
    
    // 默认图标
    else -> Icons.Default.EmojiEvents
}

// 预览
@Preview(showBackground = true)
@Composable
fun BadgeListPreview() {
    val badge1 = BadgeEntity(
        id = "1",
        name = "初露锋芒",
        description = "连续完成习惯3天",
        iconName = "streak_badge_3",
        category = BadgeCategory.STREAK.ordinal,
        rarity = BadgeRarity.COMMON.ordinal,
        condition = "连续完成习惯3天"
    )
    
    val badge2 = BadgeEntity(
        id = "2",
        name = "一周坚持",
        description = "连续完成习惯7天",
        iconName = "streak_badge_7",
        category = BadgeCategory.STREAK.ordinal,
        rarity = BadgeRarity.UNCOMMON.ordinal,
        condition = "连续完成习惯7天"
    )
    
    val userBadge1 = UserBadgeEntity(
        badgeId = "1",
        habitId = "habit1",
        highlighted = true
    )
    
    val userBadge2 = UserBadgeEntity(
        badgeId = "2",
        habitId = "habit1",
        highlighted = false
    )
    
    MaterialTheme {
        BadgeList(
            userBadges = listOf(userBadge1, userBadge2),
            allBadges = listOf(badge1, badge2),
            onBadgeClick = { _, _ -> }
        )
    }
} 