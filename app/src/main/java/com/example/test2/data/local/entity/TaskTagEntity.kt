package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * 任务标签表
 */
@Entity(tableName = "task_tags")
data class TaskTagEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: Int = TagCategory.OTHER.ordinal,
    val color: Int = 0xFF607D8B.toInt(), // 默认灰色
    val icon: String? = null,
    val isDefault: Boolean = false,
    val order: Int = 0,
    val createdAt: Date = Date()
) {
    /**
     * 获取标签分类枚举
     */
    fun getCategoryEnum(): TagCategory {
        return TagCategory.fromInt(category)
    }
    
    companion object {
        /**
         * 创建标签实体
         */
        fun create(
            name: String,
            category: TagCategory = TagCategory.OTHER,
            color: Long = 0xFF607D8B, // 默认灰色
            icon: String? = null,
            isDefault: Boolean = false,
            order: Int = 0
        ): TaskTagEntity {
            return TaskTagEntity(
                name = name,
                category = category.ordinal,
                color = color.toInt(),
                icon = icon,
                isDefault = isDefault,
                order = order,
                createdAt = Date()
            )
        }
    }
} 