package com.example.test2.data.model

import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteTag
import com.example.test2.data.model.NoteImage
import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID

/**
 * 习惯笔记数据模型
 * 
 * @property id 笔记ID
 * @property habitId 关联的习惯ID
 * @property title 笔记标题
 * @property content 笔记内容
 * @property mood 记录笔记时的心情
 * @property tags 笔记标签列表
 * @property images 笔记图片列表
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 * @property isPinned 是否顶置
 */
data class HabitNote(
    val id: String,
    val habitId: String,
    val title: String,
    val content: String,
    val mood: NoteMood = NoteMood.NEUTRAL,
    val tags: List<NoteTag> = emptyList(),
    val images: List<NoteImage> = emptyList(),
    val createdAt: Date,
    val updatedAt: Date,
    val isPinned: Boolean = false
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
    
    /**
     * 将笔记数据转换为Firestore可存储的Map
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "habitId" to habitId,
            "title" to title,
            "content" to content,
            "mood" to mood.name,
            "tags" to tags.map { it.name },
            "images" to images.map { it.uri },
            "createdAt" to Timestamp(createdAt.time / 1000, ((createdAt.time % 1000) * 1000000).toInt()),
            "updatedAt" to Timestamp(updatedAt.time / 1000, ((updatedAt.time % 1000) * 1000000).toInt()),
            "isPinned" to isPinned
        )
    }
    
    companion object {
        /**
         * 从Firestore文档数据创建笔记数据模型
         */
        fun fromMap(data: Map<String, Any>): HabitNote {
            val moodString = data["mood"] as? String ?: NoteMood.NEUTRAL.name
            val mood = try { 
                NoteMood.valueOf(moodString) 
            } catch (e: Exception) { 
                NoteMood.NEUTRAL 
            }
            
            val tagNames = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val tags = tagNames.map { NoteTag(id = UUID.randomUUID().toString(), name = it) }
            
            val imageUrls = (data["images"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val images = imageUrls.mapIndexed { index, uri -> 
                NoteImage(
                    id = index.toString(),
                    uri = uri,
                    createdAt = Date()
                )
            }
            
            return HabitNote(
                id = data["id"] as? String ?: UUID.randomUUID().toString(),
                habitId = data["habitId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                mood = mood,
                tags = tags,
                images = images,
                createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date(),
                updatedAt = (data["updatedAt"] as? Timestamp)?.toDate() ?: Date(),
                isPinned = data["isPinned"] as? Boolean ?: false
            )
        }
    }
} 