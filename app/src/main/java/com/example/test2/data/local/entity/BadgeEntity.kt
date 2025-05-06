package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import java.util.UUID

/**
 * 徽章类型枚举
 */
enum class BadgeType {
    STREAK,       // 连续完成习惯徽章
    COMPLETION,   // 累计完成徽章
    VARIETY,      // 多样性徽章（完成不同类型的习惯）
    MILESTONE,    // 里程碑徽章
    SPECIAL;      // 特殊成就
    
    companion object {
        fun fromInt(value: Int): BadgeType {
            return values().getOrElse(value) { SPECIAL }
        }
    }
}

/**
 * 徽章难度等级枚举
 */
enum class BadgeRarity {
    COMMON,      // 普通
    UNCOMMON,    // 不常见
    RARE,        // 稀有
    EPIC,        // 史诗
    LEGENDARY;   // 传说
    
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
    val type: Int,                           // 徽章类型
    val rarity: Int,                         // 稀有度
    val icon: String,                        // 图标名称
    val color: Long,                         // 徽章颜色
    val requiredValue: Int,                  // 获取所需值（如连续天数、完成次数等）
    val requiredCategoryId: Int? = null,     // 所需习惯类别ID（如果是特定类别徽章）
    val isHidden: Boolean = false,           // 是否隐藏徽章（解锁前不显示）
    val conditionsJson: String? = null,      // 获取条件JSON格式（复杂条件）
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * 获取徽章类型
     */
    fun getBadgeType(): BadgeType {
        return BadgeType.fromInt(type)
    }
    
    /**
     * 获取徽章稀有度
     */
    fun getBadgeRarity(): BadgeRarity {
        return BadgeRarity.fromInt(rarity)
    }
    
    /**
     * 获取特殊条件列表
     */
    fun getConditions(): Map<String, Any> {
        return if (conditionsJson.isNullOrEmpty()) {
            emptyMap()
        } else {
            try {
                Gson().fromJson(conditionsJson, object : TypeToken<Map<String, Any>>() {}.type)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
    
    /**
     * 获取稀有度颜色
     */
    fun getRarityColor(): Long {
        return when (getBadgeRarity()) {
            BadgeRarity.COMMON -> 0xFF9E9E9E     // 灰色
            BadgeRarity.UNCOMMON -> 0xFF4CAF50   // 绿色
            BadgeRarity.RARE -> 0xFF2196F3       // 蓝色
            BadgeRarity.EPIC -> 0xFF9C27B0       // 紫色
            BadgeRarity.LEGENDARY -> 0xFFFF9800  // 橙色
        }
    }
    
    /**
     * 获取徽章类型图标
     */
    fun getTypeIcon(): String {
        return when (getBadgeType()) {
            BadgeType.STREAK -> "ic_streak_badge"
            BadgeType.COMPLETION -> "ic_completion_badge"
            BadgeType.VARIETY -> "ic_variety_badge"
            BadgeType.MILESTONE -> "ic_milestone_badge"
            BadgeType.SPECIAL -> "ic_special_badge"
        }
    }
    
    companion object {
        /**
         * 创建徽章的工厂方法
         */
        fun create(
            name: String,
            description: String,
            type: BadgeType,
            rarity: BadgeRarity,
            icon: String,
            color: Long,
            requiredValue: Int,
            requiredCategoryId: Int? = null,
            isHidden: Boolean = false,
            conditions: Map<String, Any>? = null
        ): BadgeEntity {
            return BadgeEntity(
                name = name,
                description = description,
                type = type.ordinal,
                rarity = rarity.ordinal,
                icon = icon,
                color = color,
                requiredValue = requiredValue,
                requiredCategoryId = requiredCategoryId,
                isHidden = isHidden,
                conditionsJson = conditions?.let { Gson().toJson(it) }
            )
        }
    }
} 