package com.example.test2.presentation.habits.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.test2.data.local.entity.BadgeCategory
import com.example.test2.data.local.entity.BadgeRarity

/**
 * 徽章工具类，统一处理徽章相关的通用功能
 */
object BadgeUtils {
    
    /**
     * 获取与徽章稀有度相关的背景颜色
     */
    @Composable
    fun getBackgroundColorForRarity(rarity: BadgeRarity): Color {
        return when (rarity) {
            BadgeRarity.COMMON -> Color(0xFF9E9E9E)       // 灰色
            BadgeRarity.UNCOMMON -> Color(0xFF4CAF50)     // 绿色
            BadgeRarity.RARE -> Color(0xFF2196F3)         // 蓝色
            BadgeRarity.EPIC -> Color(0xFF9C27B0)         // 紫色
            BadgeRarity.LEGENDARY -> Color(0xFFFFB300)    // 金色
        }
    }
    
    /**
     * 获取与徽章类别相关的图标
     */
    @Composable
    fun getBadgeIcon(iconName: String, category: BadgeCategory): ImageVector {
        return when (category) {
            BadgeCategory.STREAK -> Icons.Default.LocalFireDepartment
            BadgeCategory.COMPLETION -> Icons.Default.CheckCircle
            BadgeCategory.VARIETY -> Icons.Default.ShowChart
            BadgeCategory.ACHIEVEMENT -> Icons.Default.MilitaryTech
            BadgeCategory.EVENT -> Icons.Default.EmojiEvents
        }
    }
    
    /**
     * 获取徽章稀有度文本
     */
    @Composable
    fun getRarityText(rarity: BadgeRarity): String {
        return when (rarity) {
            BadgeRarity.COMMON -> "普通"
            BadgeRarity.UNCOMMON -> "不常见"
            BadgeRarity.RARE -> "稀有"
            BadgeRarity.EPIC -> "史诗"
            BadgeRarity.LEGENDARY -> "传说"
        }
    }
} 