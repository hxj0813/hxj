package com.example.test2.data.model

import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID

/**
 * 笔记数据模型
 * 记录用户笔记内容和元数据
 *
 * @property id 笔记唯一ID
 * @property title 笔记标题
 * @property content 笔记内容
 * @property mood 情绪状态
 * @property imageUrls 图片URL列表
 * @property tags 标签列表
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 * @property isFavorite 是否收藏
 * @property isPinned 是否置顶
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val mood: NoteMood? = null,
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false
) {
    /**
     * 将笔记数据转换为Firestore可存储的Map
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "content" to content,
            "mood" to mood?.name,
            "imageUrls" to imageUrls,
            "tags" to tags,
            "createdAt" to Timestamp(createdAt.time / 1000, ((createdAt.time % 1000) * 1000000).toInt()),
            "updatedAt" to Timestamp(updatedAt.time / 1000, ((updatedAt.time % 1000) * 1000000).toInt()),
            "isFavorite" to isFavorite,
            "isPinned" to isPinned
        )
    }
    
    companion object {
        /**
         * 从Firestore文档数据创建笔记数据模型
         */
        fun fromMap(data: Map<String, Any>): Note {
            val moodString = data["mood"] as? String
            val mood = moodString?.let { try { NoteMood.valueOf(it) } catch (e: Exception) { null } }
            
            return Note(
                id = data["id"] as? String ?: "",
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                mood = mood,
                imageUrls = (data["imageUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date(),
                updatedAt = (data["updatedAt"] as? Timestamp)?.toDate() ?: Date(),
                isFavorite = data["isFavorite"] as? Boolean ?: false,
                isPinned = data["isPinned"] as? Boolean ?: false
            )
        }
    }
} 