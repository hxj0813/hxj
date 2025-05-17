package com.example.test2.data.firebase.model

import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteImage
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteTag
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Firebase笔记数据模型
 * 用于在Firestore中存储笔记数据
 *
 * @property id 笔记ID
 * @property userId 用户ID
 * @property title 笔记标题
 * @property content 笔记内容
 * @property mood 心情状态
 * @property tags 标签列表
 * @property imagePaths 图片路径列表
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 * @property isPinned 是否顶置
 * @property isDeleted 是否已删除（用于软删除）
 */
data class FirebaseNote(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val mood: Int = NoteMood.NEUTRAL.ordinal,
    val tags: List<Map<String, Any>> = emptyList(),
    val imagePaths: List<Map<String, Any>> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false
) {
    /**
     * 转换为应用内使用的领域模型
     */
    fun toHabitNote(): HabitNote {
        // 转换标签
        val noteTags = tags.map { tagMap ->
            NoteTag(
                id = tagMap["id"] as? String ?: "",
                name = tagMap["name"] as? String ?: "",
                color = (tagMap["color"] as? Long)?.toInt() ?: 0xFF4CAF50.toInt(),
                isDefault = tagMap["isDefault"] as? Boolean ?: false
            )
        }
        
        // 转换图片
        val noteImages = imagePaths.map { imageMap ->
            NoteImage(
                id = imageMap["id"] as? String ?: "",
                uri = imageMap["uri"] as? String ?: "",
                description = imageMap["description"] as? String ?: "",
                createdAt = (imageMap["createdAt"] as? Timestamp)?.toDate() ?: Date()
            )
        }
        
        return HabitNote(
            id = id,
            habitId = "", // 云笔记不直接关联习惯
            title = title,
            content = content,
            mood = NoteMood.values()[mood],
            tags = noteTags,
            images = noteImages,
            createdAt = createdAt.toDate(),
            updatedAt = updatedAt.toDate(),
            isPinned = isPinned
        )
    }
    
    companion object {
        /**
         * 从领域模型创建Firebase笔记
         */
        fun fromHabitNote(note: HabitNote, userId: String): FirebaseNote {
            // 转换标签为Map
            val tagMaps = note.tags.map { tag ->
                mapOf(
                    "id" to tag.id,
                    "name" to tag.name,
                    "color" to tag.color,
                    "isDefault" to tag.isDefault
                )
            }
            
            // 转换图片为Map
            val imageMaps = note.images.map { image ->
                mapOf(
                    "id" to image.id,
                    "uri" to image.uri,
                    "description" to image.description,
                    "createdAt" to Timestamp(image.createdAt.time / 1000, 0)
                )
            }
            
            return FirebaseNote(
                id = note.id,
                userId = userId,
                title = note.title,
                content = note.content,
                mood = note.mood.ordinal,
                tags = tagMaps,
                imagePaths = imageMaps,
                createdAt = Timestamp(note.createdAt.time / 1000, 0),
                updatedAt = Timestamp(note.updatedAt.time / 1000, 0),
                isPinned = note.isPinned,
                isDeleted = false
            )
        }
    }
} 