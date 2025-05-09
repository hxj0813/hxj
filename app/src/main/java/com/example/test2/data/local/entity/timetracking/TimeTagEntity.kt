package com.example.test2.data.local.entity.timetracking

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 时间标签实体类
 * 用于管理时间追踪中使用的标签
 */
@Entity(
    tableName = "time_tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class TimeTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,                        // 标签名称
    @ColumnInfo(name = "color_hex")
    val colorHex: String,                    // 标签颜色（十六进制）
    @ColumnInfo(name = "icon_name")
    val iconName: String? = null,            // 图标名称（可选）
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) 