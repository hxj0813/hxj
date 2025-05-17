package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * 徽章类别枚举
 */
enum class BadgeCategory {
    STREAK,       // 连续打卡类
    COMPLETION,   // 累计完成类
    VARIETY,      // 多样性类（多种习惯）
    ACHIEVEMENT,  // 特殊成就类
    EVENT;        // 活动徽章

    companion object {
        fun fromInt(value: Int): BadgeCategory {
            return values().getOrElse(value) { ACHIEVEMENT }
        }
    }
}

/**
 * 徽章稀有度枚举
 */
enum class BadgeRarity {
    COMMON,       // 普通
    UNCOMMON,     // 不常见
    RARE,         // 稀有
    EPIC,         // 史诗
    LEGENDARY;    // 传说

    companion object {
        fun fromInt(value: Int): BadgeRarity {
            return values().getOrElse(value) { COMMON }
        }
    }
}

/**
 * 徽章实体类
 */
@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,                        // 徽章名称
    val description: String,                 // 徽章描述
    val iconName: String,                    // 图标名称（可以是Material图标名、资源ID或路径）
    val category: Int,                       // 徽章类别对应BadgeCategory的ordinal
    val rarity: Int,                         // 徽章稀有度对应BadgeRarity的ordinal
    val condition: String,                   // 获得条件描述
    val thresholdValue: Int = 0,             // 解锁阈值（如连续天数、累计完成次数等）
    val isDefault: Boolean = false,          // 是否为默认徽章
    val isSecret: Boolean = false,           // 是否为隐藏徽章（解锁前不显示条件）
    val backgroundColor: Long = 0xFF4CAF50,  // 徽章背景色
    val createdAt: Date = Date()
) {
    /**
     * 获取徽章类别枚举
     */
    fun getCategoryEnum(): BadgeCategory {
        return BadgeCategory.fromInt(category)
    }
    
    /**
     * 获取徽章稀有度枚举
     */
    fun getRarityEnum(): BadgeRarity {
        return BadgeRarity.fromInt(rarity)
    }
    
    /**
     * 获取徽章的颜色（基于稀有度）
     */
    fun getColorByRarity(): Long {
        return when (getRarityEnum()) {
            BadgeRarity.COMMON -> 0xFF9E9E9E       // 灰色
            BadgeRarity.UNCOMMON -> 0xFF4CAF50     // 绿色
            BadgeRarity.RARE -> 0xFF2196F3         // 蓝色
            BadgeRarity.EPIC -> 0xFF9C27B0         // 紫色
            BadgeRarity.LEGENDARY -> 0xFFFFB300    // 金色
        }
    }
    
    companion object {
        /**
         * 创建徽章的工厂方法
         */
        fun create(
            name: String,
            description: String,
            iconName: String,
            category: BadgeCategory,
            rarity: BadgeRarity,
            condition: String,
            thresholdValue: Int = 0,
            isSecret: Boolean = false
        ): BadgeEntity {
            return BadgeEntity(
                name = name,
                description = description,
                iconName = iconName,
                category = category.ordinal,
                rarity = rarity.ordinal,
                condition = condition,
                thresholdValue = thresholdValue,
                isSecret = isSecret
            )
        }
    }
} 