package com.example.test2.data.model

/**
 * 笔记标签数据模型
 * 
 * @property id 标签ID
 * @property name 标签名称
 * @property color 标签颜色
 * @property isDefault 是否为默认标签
 */
data class NoteTag(
    val id: String,
    val name: String,
    val color: Int = 0xFF4CAF50.toInt(), // 默认绿色
    val isDefault: Boolean = false
) {
    companion object {
        /**
         * 获取所有预定义标签
         */
        fun getAllTags(): List<NoteTag> {
            return listOf(
                NoteTag(id = "1", name = "工作", color = 0xFF4CAF50.toInt(), isDefault = true),
                NoteTag(id = "2", name = "学习", color = 0xFF2196F3.toInt(), isDefault = true),
                NoteTag(id = "3", name = "生活", color = 0xFFFF9800.toInt(), isDefault = true),
                NoteTag(id = "4", name = "健康", color = 0xFFE91E63.toInt(), isDefault = true),
                NoteTag(id = "5", name = "运动", color = 0xFF9C27B0.toInt(), isDefault = true),
                NoteTag(id = "6", name = "阅读", color = 0xFF3F51B5.toInt(), isDefault = true),
                NoteTag(id = "7", name = "休闲", color = 0xFF009688.toInt(), isDefault = true),
                NoteTag(id = "8", name = "旅行", color = 0xFF607D8B.toInt(), isDefault = true),
                NoteTag(id = "9", name = "财务", color = 0xFFFFC107.toInt(), isDefault = true),
                NoteTag(id = "10", name = "其他", color = 0xFF795548.toInt(), isDefault = true)
            )
        }
    }
} 