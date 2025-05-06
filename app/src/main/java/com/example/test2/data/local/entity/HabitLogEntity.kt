package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * 习惯打卡记录实体类
 * 记录用户每次打卡的详细信息
 */
@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class HabitLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val habitId: String,                     // 关联的习惯ID
    val completedDate: Date,                 // 完成日期
    val note: String? = null,                // 笔记/备注
    val mood: Int? = null,                   // 心情评分(1-5)
    val difficulty: Int? = null,             // 难度评分(1-5)
    val createdAt: Date = Date()             // 创建时间
) {
    /**
     * 获取心情文字描述
     */
    fun getMoodDescription(): String? {
        return mood?.let {
            when(it) {
                1 -> "很糟糕"
                2 -> "不太好"
                3 -> "一般"
                4 -> "不错"
                5 -> "很棒"
                else -> null
            }
        }
    }
    
    /**
     * 获取难度文字描述
     */
    fun getDifficultyDescription(): String? {
        return difficulty?.let {
            when(it) {
                1 -> "非常容易"
                2 -> "比较简单"
                3 -> "适中"
                4 -> "有些困难"
                5 -> "非常困难"
                else -> null
            }
        }
    }
    
    companion object {
        /**
         * 创建打卡记录的工厂方法
         */
        fun create(
            habitId: String,
            completedDate: Date = Date(),
            note: String? = null,
            mood: Int? = null,
            difficulty: Int? = null
        ): HabitLogEntity {
            return HabitLogEntity(
                habitId = habitId,
                completedDate = completedDate,
                note = note,
                mood = mood,
                difficulty = difficulty
            )
        }
    }
} 