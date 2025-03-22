package com.example.test2.data.model

import java.util.Date
import java.util.UUID

/**
 * 笔记情绪类型
 */
enum class NoteMood {
    VERY_HAPPY,   // 非常开心
    HAPPY,        // 开心
    NEUTRAL,      // 平静
    TIRED,        // 疲惫
    FRUSTRATED,   // 挫折
    SAD           // 难过
}

/**
 * 笔记标签
 */
data class NoteTag(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long
) {
    companion object {
        // 预设标签
        val PROGRESS = NoteTag(name = "进展", color = 0xFF4CAF50)
        val CHALLENGE = NoteTag(name = "挑战", color = 0xFFFF9800)
        val REFLECTION = NoteTag(name = "反思", color = 0xFF2196F3)
        val ACHIEVEMENT = NoteTag(name = "成就", color = 0xFFE91E63)
        val MOTIVATION = NoteTag(name = "动力", color = 0xFF9C27B0)
        val TECHNIQUE = NoteTag(name = "技巧", color = 0xFF3F51B5)
        val ENVIRONMENT = NoteTag(name = "环境", color = 0xFF009688)
        val OTHER = NoteTag(name = "其他", color = 0xFF607D8B)
        
        // 获取所有预设标签
        fun getAllTags(): List<NoteTag> = listOf(
            PROGRESS, CHALLENGE, REFLECTION, ACHIEVEMENT,
            MOTIVATION, TECHNIQUE, ENVIRONMENT, OTHER
        )
    }
}

/**
 * 笔记图片
 */
data class NoteImage(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val description: String = "",
    val createdAt: Date = Date()
)

/**
 * 习惯笔记数据类
 */
data class HabitNote(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,                    // 关联的习惯ID
    val title: String = "",                 // 笔记标题
    val content: String,                    // 笔记内容
    val mood: NoteMood = NoteMood.NEUTRAL,  // 记录时的心情
    val tags: List<NoteTag> = emptyList(),  // 笔记标签
    val images: List<NoteImage> = emptyList(), // 笔记图片
    val createdAt: Date = Date(),           // 创建时间
    val updatedAt: Date = Date()            // 更新时间
) {
    /**
     * 获取笔记预览（最多显示100个字符）
     */
    fun getPreview(): String {
        return if (content.length > 100) {
            "${content.substring(0, 97)}..."
        } else {
            content
        }
    }
    
    /**
     * 获取笔记的格式化日期
     */
    fun getFormattedDate(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return formatter.format(createdAt)
    }
    
    /**
     * 检查笔记是否今天创建的
     */
    fun isCreatedToday(): Boolean {
        val today = java.util.Calendar.getInstance()
        val noteDate = java.util.Calendar.getInstance().apply {
            time = createdAt
        }
        
        return today.get(java.util.Calendar.YEAR) == noteDate.get(java.util.Calendar.YEAR) &&
               today.get(java.util.Calendar.DAY_OF_YEAR) == noteDate.get(java.util.Calendar.DAY_OF_YEAR)
    }
} 