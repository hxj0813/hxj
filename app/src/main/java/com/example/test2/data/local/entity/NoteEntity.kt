package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.test2.data.local.converter.DateConverter
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteTag
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import java.util.UUID
import java.lang.reflect.Type

/**
 * 笔记数据库实体
 *
 * @property id 笔记ID，主键
 * @property title 笔记标题
 * @property content 笔记内容
 * @property mood 记录笔记时的心情
 * @property tagsJson 标签，以JSON格式存储
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 * @property isPinned 是否顶置
 */
@Entity(tableName = "notes")
@TypeConverters(DateConverter::class)
data class NoteEntity(
    @PrimaryKey
    val id: String,
    
    val title: String,
    
    val content: String,
    
    val mood: Int, // 存储NoteMood枚举的序号
    
    val tagsJson: String = "[]", // 标签以JSON格式存储
    
    val createdAt: Date,
    
    val updatedAt: Date,
    
    val isPinned: Boolean = false // 顶置功能
) {
    /**
     * 转换为领域模型
     */
    fun toHabitNote(): HabitNote {
        val tags = if (tagsJson.isNotBlank() && tagsJson != "[]") {
            try {
                // 将JSON字符串解析为标签列表
                val gson = Gson()
                val listType: Type = object : TypeToken<List<NoteTag>>() {}.type
                gson.fromJson<List<NoteTag>>(tagsJson, listType)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        
        return HabitNote(
            id = id,
            habitId = "", // 笔记不关联特定习惯
            title = title,
            content = content,
            mood = NoteMood.values()[mood],
            tags = tags,
            images = emptyList(), // 不支持图片
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        /**
         * 从领域模型创建实体
         */
        fun fromHabitNote(note: HabitNote): NoteEntity {
            val gson = Gson()
            val tagsJson = gson.toJson(note.tags)
            
            return NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                mood = note.mood.ordinal,
                tagsJson = tagsJson,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                isPinned = false // 默认不顶置
            )
        }
        
        /**
         * 创建新的空白笔记实体
         */
        fun createEmpty(): NoteEntity {
            val now = Date()
            return NoteEntity(
                id = UUID.randomUUID().toString(),
                title = "",
                content = "",
                mood = NoteMood.NEUTRAL.ordinal,
                tagsJson = "[]",
                createdAt = now,
                updatedAt = now,
                isPinned = false
            )
        }
    }
} 