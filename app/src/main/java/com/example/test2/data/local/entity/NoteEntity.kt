package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.test2.data.local.converter.DateConverter
import com.example.test2.data.model.HabitNote
import com.example.test2.data.model.NoteMood
import com.example.test2.data.model.NoteTag
import com.example.test2.data.model.NoteImage
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
 * @property imagesJson 图片，以JSON格式存储
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
    
    val imagesJson: String = "[]", // 图片以JSON格式存储
    
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
        
        val images = if (imagesJson.isNotBlank() && imagesJson != "[]") {
            try {
                // 将JSON字符串解析为图片列表
                val gson = Gson()
                val listType: Type = object : TypeToken<List<NoteImage>>() {}.type
                gson.fromJson<List<NoteImage>>(imagesJson, listType)
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
            images = images,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isPinned = isPinned
        )
    }
    
    companion object {
        /**
         * 从领域模型创建实体
         */
        fun fromHabitNote(note: HabitNote): NoteEntity {
            android.util.Log.d("NoteEntity", "开始转换笔记: ${note.id}, 标题: ${note.title}")
            android.util.Log.d("NoteEntity", "原始笔记有 ${note.images.size} 张图片")
            
            try {
                val gson = Gson()
                val tagsJson = gson.toJson(note.tags)
                
                // 转换图片列表为JSON
                android.util.Log.d("NoteEntity", "开始转换图片列表为JSON...")
                val imageList = note.images
                for (i in imageList.indices) {
                    android.util.Log.d("NoteEntity", "图片 $i: ID=${imageList[i].id}, URI=${imageList[i].uri}")
                }
                
                val imagesJson = gson.toJson(note.images)
                android.util.Log.d("NoteEntity", "转换完成，JSON长度为 ${imagesJson.length} 字符")
                
                return NoteEntity(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    mood = note.mood.ordinal,
                    tagsJson = tagsJson,
                    imagesJson = imagesJson,
                    createdAt = note.createdAt,
                    updatedAt = note.updatedAt,
                    isPinned = note.isPinned // 保留原有顶置状态
                )
            } catch (e: Exception) {
                android.util.Log.e("NoteEntity", "转换笔记时出错: ${e.message}", e)
                // 出错时仍然创建一个实体，但图片列表为空
                return NoteEntity(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    mood = note.mood.ordinal,
                    tagsJson = "[]",
                    imagesJson = "[]",
                    createdAt = note.createdAt,
                    updatedAt = note.updatedAt,
                    isPinned = note.isPinned
                )
            }
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
                imagesJson = "[]",
                createdAt = now,
                updatedAt = now,
                isPinned = false
            )
        }
    }
} 